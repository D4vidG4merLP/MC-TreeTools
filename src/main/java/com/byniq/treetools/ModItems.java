package com.byniq.treetools;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.eventbus.api.IEventBus;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, "treetools"); // ← "trees" must match your mod ID

    public static final RegistryObject<Item> TREE_STICK =
            ITEMS.register("tree_stick", TreePlaceStickItem::new);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
