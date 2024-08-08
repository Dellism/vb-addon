package me.vbaddon.modules;

import com.jcraft.jorbis.Block;
import me.vbaddon.VBAddon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.input.WIntEdit;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.starscript.compiler.Expr;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationCalculator;

import java.util.ArrayList;
import java.util.List;

public class ElytraExplore extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public ElytraExplore() {
        super(VBAddon.CATEGORY, "elytra-explore", "Explores overworld terrain with elytra in spiral manner.");
    }
    List<BlockPos> waypoints = new ArrayList<>();
    float desiredPitch = 0, desiredYaw = 0;

    /*@Override
    public WWidget getWidget(GuiTheme theme) {
        WHorizontalList list = theme.horizontalList();
        //WButton selectFile = list.add(theme.button("Select File")).expandX().widget();
        list.add(theme.label("X coord: ")).widget();
        WIntEdit intEditX = list.add(theme.intEdit(startX, 0, 30000000, true)).widget();
        intEditX.actionOnRelease = this::updateList;
        list.add(theme.label("Z coord: ")).widget();
        WIntEdit intEditZ = list.add(theme.intEdit(startZ, 0, 30000000, true)).widget();
        intEditZ.actionOnRelease = this::updateList;
        WButton setHereButton = list.add(theme.button("Set Here")).widget();
        setHereButton.action = () -> {
            if (mc.player == null) return;
            intEditX.set((int)mc.player.getX());
            intEditZ.set((int)mc.player.getZ());
        };
        //WBlockPosEdit blockPosEdit = list.add(theme.blockPosEdit(startingPoint)).widget();
        //blockPosEdit.actionOnRelease = this::updateList;

        return list;
    }*/
    @Override
    public WWidget getWidget(GuiTheme theme) {
        WHorizontalList list = theme.horizontalList();
        WButton setHereButton = list.add(theme.button("Set Here")).expandX().widget();
        setHereButton.action = () -> {
            if (mc.player == null) return;
            startX.set((int)mc.player.getX());
            startZ.set((int)mc.player.getZ());
        };
        return list;
    }

    private final Setting<Integer> serverRenderDistance = sgGeneral.add(new IntSetting.Builder()
        .name("server-render-distance")
        .description("Render distance of the server that is taken into consideration.")
        .sliderMin(2)
        .sliderMax(16)
        .defaultValue(6)
        .build()
    );
    private final Setting<Integer> radius = sgGeneral.add(new IntSetting.Builder()
        .name("radius")
        .description("radius of terrain to explore")
        .sliderMin(200)
        .sliderMax(50000)
        .defaultValue(500)
        .build()
    );
    private final Setting<Integer> height = sgGeneral.add(new IntSetting.Builder()
        .name("height")
        .description("height to fly above")
        .sliderMin(-64)
        .sliderMax(350)
        .defaultValue(270)
        .onChanged(h -> updateList())
        .build()
    );
    private final Setting<Integer> startX = sgGeneral.add(new IntSetting.Builder()
        .name("start-x")
        .description("startx")
        .noSlider()
        .defaultValue(0)
        .onChanged(x -> updateList())
        .build()
    );
    private final Setting<Integer> startZ = sgGeneral.add(new IntSetting.Builder()
        .name("start-z")
        .description("startz")
        .noSlider()
        .defaultValue(0)
        //.onChanged(z -> updateList())
        .build()
    );
    private final Setting<Boolean> render = sgGeneral.add(new BoolSetting.Builder()
        .name("render")
        .description("whether to render the line")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> renderSegments = sgGeneral.add(new IntSetting.Builder()
        .name("render-segments")
        .description("how many next segments of the path to draw")
        .visible(render::get)
        .sliderRange(1, 64)
        .defaultValue(10)
        .build()
    );
    private final Setting<Double> stepYaw = sgGeneral.add(new DoubleSetting.Builder()
        .name("rotation-speed-yaw")
        .description("rotation in degrees per tick")
        .sliderRange(1, 30)
        .defaultValue(6)
        .build()
    );
    private final Setting<Double> stepPitch = sgGeneral.add(new DoubleSetting.Builder()
        .name("rotation-speed-pitch")
        .description("rotation in degrees per tick")
        .sliderRange(1, 30)
        .defaultValue(1.5)
        .build()
    );
    private void updateList() {
        waypoints.clear();
        BlockPos startingPoint = new BlockPos(startX.get(), height.get(), startZ.get());
        BlockPos.Mutable blockPosMutable = startingPoint.mutableCopy();
        waypoints.add(startingPoint);
        //ChunkPos pos = new ChunkPos(startingPoint);
        for (int counter = 1; Math.abs(waypoints.getLast().getX() - startingPoint.getX()) < radius.get(); counter++) {
            for (int i = 0; i < counter; i++) {
                blockPosMutable.move(Direction.NORTH, 16 * 2 * serverRenderDistance.get()); // * 2 to account for the fact that it's a radius
                waypoints.add(blockPosMutable.toImmutable());
            }
            for (int i = 0; i < counter; i++) {
                blockPosMutable.move(Direction.WEST, 16 * 2 * serverRenderDistance.get());
                waypoints.add(blockPosMutable.toImmutable());
            }
            counter++;
            for (int i = 0; i < counter; i++) {
                blockPosMutable.move(Direction.SOUTH, 16 * 2 * serverRenderDistance.get());
                waypoints.add(blockPosMutable.toImmutable());
            }
            for (int i = 0; i < counter; i++) {
                blockPosMutable.move(Direction.EAST, 16 * 2 * serverRenderDistance.get());
                waypoints.add(blockPosMutable.toImmutable());
            }
        }
    }

    private void updateRotations(BlockPos pos) {
        desiredPitch = (float) Rotations.getPitch(pos);
        desiredYaw = (float) Rotations.getYaw(pos);
    }

    @Override
    public void onActivate() {
        updateList();
    }

    @Override
    public void onDeactivate() {
        waypoints.clear();
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!render.get()) return;
        //BlockPos startingPoint = new BlockPos(startX.get(), height.get(), startZ.get());
        /*for (BlockPos w : waypoints) {
            event.renderer.box(w, Color.CYAN, Color.CYAN, ShapeMode.Both, 0);
        }
        event.renderer.box(startingPoint, Color.CYAN, Color.CYAN, ShapeMode.Both, 0);
        */
        int counter = 0;
        for (int i = 1; i < waypoints.size(); i++) {
            if (counter > renderSegments.get()) break;
            BlockPos point1 = waypoints.get(i);
            BlockPos point2 = waypoints.get(i-1);

            event.renderer.line(point1.getX() + 0.5, point1.getY() + 0.5, point1.getZ() + 0.5,
                point2.getX() + 0.5, point2.getY() + 0.5, point2.getZ() + 0.5, Color.CYAN);
            event.renderer.box(waypoints.get(i-1), Color.WHITE, Color.WHITE, ShapeMode.Both, 0);
            counter++;
        }
    }
    @EventHandler
    private void onTick(TickEvent.Post event) {
        int count = 0;
        boolean remove = false;
        for (; count < waypoints.size(); count++) {
            if (Math.pow(waypoints.get(count).getX() - mc.player.getX(), 2) <= 25 &&
                Math.pow(waypoints.get(count).getZ() - mc.player.getZ(), 2) <= 25) {
                    remove = true;
                    break;
            }
        }
        if (remove) {
            for (int i = 0; i <= count; i++) { waypoints.removeFirst(); }
        }
        //System.out.println("balls");

        updateRotations(waypoints.getFirst());
        mc.player.setYaw(MathHelper.stepTowards(mc.player.getYaw(), desiredYaw, stepYaw.get().floatValue()));
        mc.player.setPitch(MathHelper.stepTowards(mc.player.getPitch(), desiredPitch, stepPitch.get().floatValue()));

    }
}
