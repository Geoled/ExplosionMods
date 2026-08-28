package com.expomod;

import net.fabricmc.api.ModInitializer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Expomod implements ModInitializer {
public static final String MOD_ID = "expomod";

// This logger is used to write text to the console and the log file.
// It is considered best practice to use your mod id as the logger's name.
// That way, it's clear which mod wrote info, warnings, and errors.
public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

// New Explosive Blocks
public static Block C4_BLOCK;
public static Block ANFO_BLOCK;
public static Block NITROGLYCERIN_BLOCK;
public static Block THERMITE_BLOCK;
public static Block FUEL_AIR_BOMB_BLOCK;

// New Explosive Items
public static Item C4_ITEM;
public static Item ANFO_ITEM;
public static Item NITROGLYCERIN_ITEM;
public static Item THERMITE_ITEM;
public static Item FUEL_AIR_BOMB_ITEM;
public static Item DETONATOR_ITEM;
public static Item REMOTE_DETONATOR_ITEM;

@Override
public void onInitialize() {
// Register new explosive blocks
registerBlocks();

// Register new explosive items
registerItems();

LOGGER.info("Realistic Explosions Mod initialized successfully!");
LOGGER.info("Added realistic explosion system and new explosives: C4, ANFO, Nitroglycerin, Thermite, and Fuel-Air Bomb");
}

private void registerBlocks() {
// C4 - Plastic explosive, very high velocity detonation
C4_BLOCK = registerBlock("c4_block", new Block(
BlockBehaviour.Properties.of()
.mapColor(MapColor.COLOR_BLACK)
.strength(0.5F)
.noCollission()
.instabreak()
));

// ANFO - Ammonium Nitrate Fuel Oil, large blast radius
ANFO_BLOCK = registerBlock("anfo_block", new Block(
BlockBehaviour.Properties.of()
.mapColor(MapColor.COLOR_LIGHT_GRAY)
.strength(1.0F)
.sound(SoundType.SAND)
));

// Nitroglycerin - Extremely sensitive liquid explosive
NITROGLYCERIN_BLOCK = registerBlock("nitroglycerin_block", new Block(
BlockBehaviour.Properties.of()
.mapColor(MapColor.COLOR_YELLOW)
.strength(0.2F)
.noCollission()
.instabreak()
));

// Thermite - Incendiary composition, extreme heat
THERMITE_BLOCK = registerBlock("thermite_block", new Block(
BlockBehaviour.Properties.of()
.mapColor(MapColor.COLOR_RED)
.strength(1.5F)
.sound(SoundType.METAL)
));

// Fuel-Air Bomb - Massive overpressure explosion
FUEL_AIR_BOMB_BLOCK = registerBlock("fuel_air_bomb_block", new Block(
BlockBehaviour.Properties.of()
.mapColor(MapColor.COLOR_GREEN)
.strength(2.0F)
.sound(SoundType.METAL)
));

LOGGER.info("Registered explosive blocks");
}

private void registerItems() {
// C4 Item
C4_ITEM = registerItem("c4", new BlockItem(C4_BLOCK, 
new Item.Properties()));

// ANFO Item
ANFO_ITEM = registerItem("anfo", new BlockItem(ANFO_BLOCK, 
new Item.Properties()));

// Nitroglycerin Item
NITROGLYCERIN_ITEM = registerItem("nitroglycerin", new BlockItem(NITROGLYCERIN_BLOCK, 
new Item.Properties()));

// Thermite Item
THERMITE_ITEM = registerItem("thermite", new BlockItem(THERMITE_BLOCK, 
new Item.Properties()));

// Fuel-Air Bomb Item
FUEL_AIR_BOMB_ITEM = registerItem("fuel_air_bomb", new BlockItem(FUEL_AIR_BOMB_BLOCK, 
new Item.Properties()));

// Detonator - Used to trigger C4 and other explosives
DETONATOR_ITEM = registerItem("detonator", new Item(
new Item.Properties()
.stacksTo(1)
));

// Remote Detonator - Advanced triggering device
REMOTE_DETONATOR_ITEM = registerItem("remote_detonator", new Item(
new Item.Properties()
.stacksTo(1)
));

LOGGER.info("Registered explosive items");
}

private Block registerBlock(String name, Block block) {
return Registry.register(BuiltInRegistries.BLOCK, id(name), block);
}

private Item registerItem(String name, Item item) {
return Registry.register(BuiltInRegistries.ITEM, id(name), item);
}

public static ResourceLocation id(String path) {
return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
}
}
