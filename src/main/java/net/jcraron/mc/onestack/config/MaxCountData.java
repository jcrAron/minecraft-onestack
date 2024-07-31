package net.jcraron.mc.onestack.config;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.item.Item;

public class MaxCountData {
	private static Map<Item, Integer> Config = new HashMap<>();

	public static void clear() {
		Config.clear();
	}

	/** @param maxCount must be greater than or equal to 1 */
	public static void setMaxCount(Item item, int maxCount) {
		setRawMaxCount(item, maxCount);
	}

	/** @return -1 if there is not setting in configure file */
	public static Integer getMaxCount(Item item) {
		return Config.getOrDefault(item, -1);
	}

	static void setRawMaxCount(Item item, int maxCount) {
		if (maxCount >= 1) {
			Config.put(item, maxCount);
		} else {
			Config.remove(item);
		}
	}
}
