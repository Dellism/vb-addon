package me.vbaddon.modules;

import me.vbaddon.VBAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.List;

public class SpawnProoferPlus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("Range for block placement and rendering")
        .defaultValue(3)
        .min(0)
        .build()
    );

    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("blocks")
        .description("Block to use for spawn proofing")
        .defaultValue(Blocks.TORCH, Blocks.STONE_BUTTON, Blocks.STONE_SLAB)
        .filter(this::filterBlocks)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay in ticks between placing blocks")
        .defaultValue(0)
        .min(0)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotates towards the blocks being placed.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Which spawn types should be spawn proofed.")
        .defaultValue(Mode.Both)
        .build()
    );

    private final Setting<Boolean> newMobSpawnLightLevel = sgGeneral.add(new BoolSetting.Builder()
        .name("new-mob-spawn-light-level")
        .description("Use the new (1.18+) mob spawn behavior")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> ignoreLight = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-light-level")
        .description("Ignore light level and spawnproof all spawnable blocks")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> doBlocksToSpawnproof = sgGeneral.add(new BoolSetting.Builder()
        .name("do-blocks-to-spawnproof")
        .description("only place spawnproof blocks over these blocks")
        .defaultValue(false)
        .build()
    );

    private final Setting<List<Block>> blocksToSpawnproof = sgGeneral.add(new BlockListSetting.Builder()
        .name("blocks-to-spawnproof")
        .description("only place spawnproof blocks over these blocks")
        .defaultValue(Blocks.NETHER_BRICKS)
        .visible(doBlocksToSpawnproof::get)
        .build()
    );


    private final Pool<BlockPos.Mutable> spawnPool = new Pool<>(BlockPos.Mutable::new);
    private final List<BlockPos.Mutable> spawns = new ArrayList<>();
    private int ticksWaited;

    public SpawnProoferPlus() {
        super(VBAddon.CATEGORY, "spawn-proofer-plus", "Automatically spawnproofs unlit areas.");
    }

    @EventHandler
    private void onTickPre(TickEvent.Pre event) {
        // Delay
        if (delay.get() != 0 && ticksWaited < delay.get() - 1) {
            return;
        }

        // Find slot
        boolean foundBlock = InvUtils.testInHotbar(itemStack -> blocks.get().contains(Block.getBlockFromItem(itemStack.getItem())));
        if (!foundBlock) {
            error("Found none of the chosen blocks in hotbar");
            toggle();
            return;
        }

        // Find spawn locations
        for (BlockPos.Mutable blockPos : spawns) spawnPool.free(blockPos);
        spawns.clear();

        int lightLevel = ignoreLight.get() ? 17 : newMobSpawnLightLevel.get() ? 0 : 7;
        BlockIterator.register(range.get(), range.get(), (blockPos, blockState) -> {
            BlockUtils.MobSpawn spawn = BlockUtils.isValidMobSpawn(blockPos, blockState, lightLevel);

            if ((spawn == BlockUtils.MobSpawn.Always && (mode.get() == Mode.Always || mode.get() == Mode.Both)) ||
                spawn == BlockUtils.MobSpawn.Potential && (mode.get() == Mode.Potential || mode.get() == Mode.Both)) {
                if (doBlocksToSpawnproof.get()){
                    if (blocksToSpawnproof.get().contains(mc.world.getBlockState(blockPos.down()).getBlock())) {
                        spawns.add(spawnPool.get().set(blockPos));
                    }
                } else {
                    spawns.add(spawnPool.get().set(blockPos));
                }
            }
        });
    }

    @EventHandler
    private void onTickPost(TickEvent.Post event) {
        // Delay
        if (delay.get() != 0 && ticksWaited < delay.get() - 1) {
            ticksWaited++;
            return;
        }

        if (spawns.isEmpty()) return;

        // Find slot
        FindItemResult block = InvUtils.findInHotbar(itemStack -> blocks.get().contains(Block.getBlockFromItem(itemStack.getItem())));
        if (!block.found()) {
            error("Found none of the chosen blocks in hotbar");
            toggle();
            return;
        }

        // Place blocks
        if (delay.get() == 0) {
            for (BlockPos blockPos : spawns) BlockUtils.place(blockPos, block, rotate.get(), -50, false);
        }
        else {
            // Check if light source
            if (isLightSource(Block.getBlockFromItem(mc.player.getInventory().getStack(block.slot()).getItem()))) {

                // Find lowest light level
                int lowestLightLevel = 16;
                BlockPos.Mutable selectedBlockPos = spawns.getFirst();

                for (BlockPos blockPos : spawns) {
                    int lightLevel = mc.world.getLightLevel(blockPos);
                    if (lightLevel < lowestLightLevel) {
                        lowestLightLevel = lightLevel;
                        selectedBlockPos.set(blockPos);
                    }
                }

                BlockUtils.place(selectedBlockPos, block, rotate.get(), -50, false);
            }
            else {
                BlockUtils.place(spawns.getFirst(), block, rotate.get(), -50, false);
            }
        }

        ticksWaited = 0;
    }

    private boolean filterBlocks(Block block) {
        return isNonOpaqueBlock(block) || isLightSource(block);
    }

    private boolean isNonOpaqueBlock(Block block) {
        return block instanceof ButtonBlock ||
            block instanceof SlabBlock ||
            block instanceof AbstractPressurePlateBlock ||
            block instanceof TransparentBlock ||
            block instanceof TripwireBlock ||
            block instanceof CarpetBlock ||
            block instanceof LeverBlock ||
            block instanceof AbstractRedstoneGateBlock ||
            block instanceof AbstractRailBlock;
    }

    private boolean isLightSource(Block block) {
        return block.getDefaultState().getLuminance() > 0;
    }

    public enum Mode {
        Always,
        Potential,
        Both,
        None
    }
}
