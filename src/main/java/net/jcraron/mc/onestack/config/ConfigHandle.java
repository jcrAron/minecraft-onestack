package net.jcraron.mc.onestack.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.ForgeConfigSpec;

interface ConfigHandle {

	public void registerTo(ForgeConfigSpec.Builder builder, List<String> base);

	public void load();

	public void reload();

	public static List<String> withNextPath(List<String> base, String... paths) {
		List<String> next = List.of(paths);
		if (base == null || base.isEmpty()) {
			return next;
		}
		List<String> modifiableList = new ArrayList<>(base);
		modifiableList.addAll(next);
		return modifiableList;
	}
}
