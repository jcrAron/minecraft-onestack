package net.jcraron.mc.onestack.api;

import net.jcraron.mc.onestack.config.RootConfig;
import net.jcraron.mc.onestack.config.value.MaxCountEntryHandle;
import net.jcraron.mc.onestack.config.value.MaxCountEntryHandle.Entry;
import net.jcraron.mc.onestack.config.value.MaxCountValue;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MaxCountConfig {

	public final static int PRIORITY_DEFAULT_ITEM = MaxCountEntryHandle.DEFAULT_PRIORITY_ITEM;
	public final static int PRIORITY_DEFAULT_TAG = MaxCountEntryHandle.DEFAULT_PRIORITY_TAG;

	/** The max count that you can config */
	public final static int COUNT_MAX = MaxCountValue.JAVA_VALUE_MAX;

	/**
	 * When this value is set on configure, this represents the result of applying
	 * {@link Item#getMaxStackSize(ItemStack)}.
	 */
	public final static int COUNT_DEFAULT = MaxCountValue.JAVA_VALUE_DEFAULT;

	/**
	 * @return {@link MaxCountConfig#DEFAULT}, this represents the result of applying
	 *         {@link Item#getMaxStackSize(ItemStack)}.
	 */
	public static int getMaxCount(ItemStack itemstack) {
		return RootConfig.INSTANCE.ITEMS_CONFIG.getMaxCount(itemstack);
	}

	/**
	 * Set max count of specific item.
	 * 
	 * @param value    must be <i>positive integer</i> or {@link MaxCountConfig#COUNT_MAX} or
	 *                 {@link MaxCountConfig#COUNT_DEFAULT}
	 * @param priority The larger the number, the higher the priority. see also
	 *                 {@link MaxCountConfig#PRIORITY_DEFAULT_TAG}, {@link MaxCountConfig#PRIORITY_DEFAULT_ITEM}
	 * @see MaxCountConfig#set(TagKey, int, int)
	 */
	public static void set(Item item, int value, int priority) {
		RootConfig.INSTANCE.ITEMS_CONFIG.setMaxCount(Entry.of(item, value, priority));
	}

	/**
	 * Set max count of specific item tag.
	 * 
	 * @param value    must be <i>positive integer</i> or {@link MaxCountConfig#COUNT_MAX} or
	 *                 {@link MaxCountConfig#COUNT_DEFAULT}
	 * @param priority The larger the number, the higher the priority. see also
	 *                 {@link MaxCountConfig#PRIORITY_DEFAULT_TAG}, {@link MaxCountConfig#PRIORITY_DEFAULT_ITEM}
	 * @see MaxCountConfig#set(Item, int, int)
	 */
	public static void set(TagKey<Item> tag, int value, int priority) {
		RootConfig.INSTANCE.ITEMS_CONFIG.setMaxCount(Entry.of(tag, value, priority));
	}

	/**
	 * Unset max count of specific item.
	 * 
	 * @see MaxCountConfig#unset(TagKey)
	 */
	public static void unset(Item item) {
		RootConfig.INSTANCE.ITEMS_CONFIG.setMaxCount(Entry.of(item));
	}

	/**
	 * Unset max count of specific item tag.
	 * 
	 * @see MaxCountConfig#unset(Item)
	 */
	public static void unset(TagKey<Item> tag) {
		RootConfig.INSTANCE.ITEMS_CONFIG.setMaxCount(Entry.of(tag));
	}

	/**
	 * reload config
	 */
	public static void reload() {
		RootConfig.INSTANCE.reload();
	}

	/**
	 * clean cache
	 */
	public static void cleanCache() {
		RootConfig.INSTANCE.ITEMS_CONFIG.cleanCache();
	}
}
