package com.byniq.treetools;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class TreeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("trees")
                .then(Commands.literal("clear")
                        .then(Commands.argument("range", IntegerArgumentType.integer(1, 100))
                                .executes(ctx -> {
                                    int range = IntegerArgumentType.getInteger(ctx, "range");
                                    CommandSourceStack source = ctx.getSource();
                                    ServerLevel world = source.getLevel();
                                    BlockPos center = BlockPos.containing(source.getPosition());

                                    int cleared = clearTrees(world, center, range);
                                    source.sendSuccess(() ->
                                            Component.literal("Cleared " + cleared + " tree blocks."), true);
                                    return 1;
                                })))
                .then(Commands.literal("place")
                        .then(Commands.argument("range", IntegerArgumentType.integer(1, 100))
                                .then(Commands.argument("density", IntegerArgumentType.integer(0, 100))
                                        .executes(ctx -> {
                                            CommandSourceStack source = ctx.getSource();
                                            source.sendSuccess(() -> Component.literal("[DEBUG] /trees place command executed"), true);

                                            int range = IntegerArgumentType.getInteger(ctx, "range");
                                            int density = IntegerArgumentType.getInteger(ctx, "density");
                                            source.sendSuccess(() -> Component.literal("[DEBUG] Parsed range=" + range + ", density=" + density), true);

                                            ItemStack stick;
                                            try {
                                                stick = TreePlaceStickItem.create(range, density);
                                            } catch (Exception e) {
                                                source.sendFailure(Component.literal("[ERROR] Failed to create stick: " + e));
                                                e.printStackTrace();
                                                return 0;
                                            }

                                            source.sendSuccess(() -> Component.literal("[DEBUG] Created stick with tag: " + stick.getTag()), true);

                                            if (source.getPlayer() != null) {
                                                try {
                                                    source.getPlayer().addItem(stick);
                                                    source.sendSuccess(() -> Component.literal("[DEBUG] Stick given to player"), true);
                                                } catch (Exception e) {
                                                    source.sendFailure(Component.literal("[ERROR] Failed to give stick: " + e));
                                                    e.printStackTrace();
                                                    return 0;
                                                }
                                                return 1;
                                            } else {
                                                source.sendFailure(Component.literal("[ERROR] Command must be run by a player"));
                                                return 0;
                                            }
                                        }))))

                .then(Commands.literal("grow")
                        .executes(ctx -> {
                            CommandSourceStack source = ctx.getSource();
                            ServerLevel world = source.getLevel();

                            int grown = 0;
                            for (BlockPos pos : TreePlaceStickItem.placedSaplings) {
                                if (world.getBlockState(pos).getBlock() == Blocks.OAK_SAPLING) {
                                    Blocks.OAK_SAPLING.randomTick(world.getBlockState(pos), world, pos, world.getRandom());
                                    grown++;
                                }
                            }

                            TreePlaceStickItem.placedSaplings.clear();
                            int finalGrown = grown;
                            source.sendSuccess(() -> Component.literal("Grew " + finalGrown + " trees."), true);
                            return 1;
                        })));
    }


    private static int clearTrees(ServerLevel world, BlockPos center, int range) {
        Set<BlockPos> visited = new HashSet<>();
        int cleared = 0;

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    if (distance > range) continue;

                    BlockPos current = center.offset(x, y, z);
                    if (!visited.contains(current) && isTreeBlock(world.getBlockState(current).getBlock())) {
                        cleared += floodFillRemoveTree(world, current, visited);
                    }
                }
            }
        }

        return cleared;
    }

    private static int floodFillRemoveTree(ServerLevel world, BlockPos start, Set<BlockPos> visited) {
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(start);
        int removed = 0;

        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            if (visited.contains(pos)) continue;
            visited.add(pos);

            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (isTreeBlock(block)) {
                world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                removed++;

                for (BlockPos offset : getAdjacent(pos)) {
                    if (!visited.contains(offset) && isTreeBlock(world.getBlockState(offset).getBlock())) {
                        queue.add(offset);
                    }
                }
            }
        }

        return removed;
    }

    private static List<BlockPos> getAdjacent(BlockPos pos) {
        List<BlockPos> neighbors = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx != 0 || dy != 0 || dz != 0) {
                        neighbors.add(pos.offset(dx, dy, dz));
                    }
                }
            }
        }
        return neighbors;
    }

    private static boolean isTreeBlock(Block block) {
        return block == Blocks.OAK_LOG || block == Blocks.SPRUCE_LOG || block == Blocks.BIRCH_LOG ||
                block == Blocks.JUNGLE_LOG || block == Blocks.ACACIA_LOG || block == Blocks.DARK_OAK_LOG ||
                block == Blocks.MANGROVE_LOG || block == Blocks.CHERRY_LOG ||
                block == Blocks.OAK_LEAVES || block == Blocks.SPRUCE_LEAVES || block == Blocks.BIRCH_LEAVES ||
                block == Blocks.JUNGLE_LEAVES || block == Blocks.ACACIA_LEAVES || block == Blocks.DARK_OAK_LEAVES ||
                block == Blocks.MANGROVE_LEAVES || block == Blocks.CHERRY_LEAVES;
    }


}
