package com.minecraft.mod;

import net.minecraft.resources.Identifier;

public class MinecraftMod {

	public static final String MOD_ID = "minecraft_mod";

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}