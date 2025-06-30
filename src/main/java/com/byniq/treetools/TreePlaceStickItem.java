package com.byniq.treetools;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
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
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        BlockPos center = context.getClickedPos().above(); // plant on top of clicked block

        ItemStack stick = context.getItemInHand();
        CompoundTag tag = stick.getOrCreateTag();
        int range = tag.getInt("Range");
        int density = tag.getInt("Density");

        ItemStack offhand = player.getOffhandItem();

        if (!(offhand.getItem() instanceof BlockItem blockItem) ||
                !(blockItem.getBlock() instanceof SaplingBlock sapling)) {
            player.displayClientMessage(Component.literal("You must hold a sapling in your offhand."), true);
            return InteractionResult.FAIL;
        }

        BlockState saplingState = blockItem.getBlock().defaultBlockState();

        int placed = 0;
        Random rand = new Random();

        for (int dx = -range; dx <= range; dx++) {
            for (int dz = -range; dz <= range; dz++) {
                if (dx * dx + dz * dz > range * range) continue; // circle radius check
                if (rand.nextInt(100) >= density) continue;

                BlockPos pos = center.offset(dx, 0, dz);

                // Check suitable ground
                BlockPos ground = pos.below();
                BlockState groundState = level.getBlockState(ground);
                BlockState stateAtPos = level.getBlockState(pos);

                if (stateAtPos.isAir() &&
                        groundState.is(BlockTags.DIRT) || groundState.is(Blocks.GRASS_BLOCK)) {

                    level.setBlockAndUpdate(pos, saplingState);
                    TreePlaceStickItem.placedSaplings.add(pos.immutable());
                    placed++;
                }
            }
        }

        player.displayClientMessage(Component.literal("Placed " + placed + " saplings."), true);
        return InteractionResult.SUCCESS;
    }

    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getOrCreateTag();

        if (tag.contains("Range") && tag.contains("Density")) {
            int range = tag.getInt("Range");
            int density = tag.getInt("Density");

            tooltip.add(Component.literal("Range: " + range).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Density: " + density + "%").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal("Unconfigured").withStyle(ChatFormatting.RED));
        }

        super.appendHoverText(stack, world, tooltip, flag);
    }
}
