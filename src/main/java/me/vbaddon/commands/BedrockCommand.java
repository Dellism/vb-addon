package me.vbaddon.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * The Meteor Client command API uses the <a href="https://github.com/Mojang/brigadier">same command system as Minecraft does</a>.
 */
public class BedrockCommand extends Command {
    /**
     * The {@code name} parameter should be in kebab-case.
     */
    public BedrockCommand() {
        super("bedrock", "Copies current chunk as config for nether bedrock cracker.");
        info("initialized command");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            info("started working");
            if (!mc.world.getDimension().hasCeiling()) {
                info("only works in the nether");
                return SINGLE_SUCCESS;
            }
            Chunk chunk = mc.world.getChunk(mc.player.getBlockPos());
            StringBuilder builder1 = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 16; j++) {
                    if (chunk.getBlockState(new BlockPos(i, 4, j)) == Blocks.BEDROCK.getDefaultState()) {
                        builder1.append(chunk.getPos().getBlockPos(i, 4, j).getX());
                        builder1.append(" 4 ");
                        builder1.append(chunk.getPos().getBlockPos(i, 4, j).getZ());
                        builder1.append(" Bedrock\n");
                    } else {
                        builder1.append(chunk.getPos().getBlockPos(i, 4, j).getX());
                        builder1.append(" 4 ");
                        builder1.append(chunk.getPos().getBlockPos(i, 4, j).getZ());
                        builder1.append(" Other\n");
                    }
                    if (chunk.getBlockState(new BlockPos(i, 123, j)) == Blocks.BEDROCK.getDefaultState()) {
                        builder1.append(chunk.getPos().getBlockPos(i, 123, j).getX());
                        builder1.append(" 123 ");
                        builder1.append(chunk.getPos().getBlockPos(i, 123, j).getZ());
                        builder1.append(" Bedrock\n");
                    } else {
                        builder1.append(chunk.getPos().getBlockPos(i, 123, j).getX());
                        builder1.append(" 123 ");
                        builder1.append(chunk.getPos().getBlockPos(i, 123, j).getZ());
                        builder1.append(" Other\n");
                    }
                }
            }
            mc.keyboard.setClipboard(builder1.toString());
            info("copied to clipboard");
            return SINGLE_SUCCESS;
        });
    }
}
