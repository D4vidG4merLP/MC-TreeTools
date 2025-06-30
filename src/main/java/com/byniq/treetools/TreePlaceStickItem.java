package com.byniq.treetools;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Blocks;

import java.util.HashSet;
import java.util.Set;

public class TreePlaceStickItem extends Item {

    public static final Set<BlockPos> placedSaplings = new HashSet<>();

    public TreePlaceStickItem() {
        super(new Item.Properties().stacksTo(1));
    }

    public static ItemStack create(int range, int density) {
        ItemStack stack = new ItemStack(ModItems.TREE_STICK.get());
        stack.getOrCreateTag().putInt("Range", range);
        stack.getOrCreateTag().putInt("Density", density);
        return stack;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.PASS;

        ItemStack stack = context.getItemInHand();
        int range = stack.getOrCreateTag().getInt("Range");
        int density = stack.getOrCreateTag().getInt("Density");

        BlockPos center = context.getClickedPos().above(); // plant on ground
        RandomSource random = level.getRandom();

        int count = 0;
        for (int dx = -range; dx <= range; dx++) {
            for (int dz = -range; dz <= range; dz++) {
                if (Math.sqrt(dx * dx + dz * dz) > range) continue;
                if (random.nextInt(100) >= density) continue;

                BlockPos pos = center.offset(dx, 0, dz);
                if (level.getBlockState(pos).isAir() &&
                        level.getBlockState(pos.below()).isSolid()) {
                    level.setBlock(pos, Blocks.OAK_SAPLING.defaultBlockState(), 3);
                    placedSaplings.add(pos.immutable());
                    count++;
                }
            }
        }

        context.getPlayer().displayClientMessage(
                net.minecraft.network.chat.Component.literal("Planted " + count + " saplings."), true);
        return InteractionResult.SUCCESS;
    }
}
