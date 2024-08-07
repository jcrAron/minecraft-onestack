package net.jcraron.mc.onestack.api;

import net.jcraron.mc.onestack.config.RootConfig;
import net.jcraron.mc.onestack.config.value.MaxCountValue;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MaxCountConfig {
	/** The max count that you can config */
	public final static int MAX = MaxCountValue.JAVA_VALUE_MAX;

	/**
	 * When this value is set on configure, this represents the result of applying
	 * {@link Item#getMaxStackSize(ItemStack)}.
	 */
	public final static int DEFAULT = MaxCountValue.JAVA_VALUE_DEFAULT;

	/**
	 * @return {@link MaxCountConfig#DEFAULT}, this represents the result of
	 *         applying {@link Item#getMaxStackSize(ItemStack)}.
	 */
	public static int getMaxCount(ItemStack itemstack) {
		Integer value = RootConfig.INSTANCE.ITEMS_CONFIG.getMaxCount(itemstack);
		if (value != null) {
			return value;
		}
		int defaultDefine = itemstack.getItem().getMaxStackSize(itemstack);
		if (defaultDefine != 1) {
			return RootConfig.INSTANCE.DEFAULT_CONFIG.getCommonMaxCount().get();
		}
		return DEFAULT;
	}

	/** Set max count of common item that original max count greater than 1. */
	public static void setCommonMaxCount(int value) {
		RootConfig.INSTANCE.DEFAULT_CONFIG.getCommonMaxCount().set(value);
	}

	/**
	 * Set max count of specific item. this priority is higher than CommonMaxCount.
	 */
	public static void setMaxCount(Item item, int value) {
		RootConfig.INSTANCE.ITEMS_CONFIG.setMaxCount(item, value);
	}

	/** TODO on next minor version */
	@SuppressWarnings("unused")
	@Deprecated
	private static void setMaxCount(Item item, String[] withTag, int value, int priority) {
		setMaxCount(item, value);
	}
}
