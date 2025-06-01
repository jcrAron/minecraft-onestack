package net.jcraron.mc.onestack.api;

import net.jcraron.mc.onestack.config.RootConfig;
import net.jcraron.mc.onestack.config.value.MaxCountEntryHandle;
import net.jcraron.mc.onestack.config.value.MaxCountEntryHandle.MaxCountEntry;
import net.jcraron.mc.onestack.config.value.CountNumberValue;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class OneStackConfig {

	public final static int PRIORITY_DEFAULT_ITEM = MaxCountEntryHandle.DEFAULT_PRIORITY_ITEM;
	public final static int PRIORITY_DEFAULT_TAG = MaxCountEntryHandle.DEFAULT_PRIORITY_TAG;

	/** The max count that you can config */
	public final static int COUNT_MAX = CountNumberValue.JAVA_VALUE_MAX;

	/**
	 * When this value is set on configure, this represents the result of applying
	 * {@link Item#getMaxStackSize(ItemStack)}.
	 */
	public final static int COUNT_DEFAULT = CountNumberValue.JAVA_VALUE_DEFAULT;

	/**
	 * @return max count in this mod configure. If {@link OneStackConfig#DEFAULT} is returned this means it will apply
	 *         default value ({@link Item#getMaxStackSize(ItemStack)}).
	 */
	public static int getMaxCount(ItemStack itemstack) {
		return RootConfig.INSTANCE.ITEMS_CONFIG.getMaxCount(itemstack);
	}

	/**
	 * @return default value ({@link Item#getMaxStackSize(ItemStack)})
	 */
	@SuppressWarnings("deprecation")
	public static int getDefaultMaxCount(Item item) {
		return item.getMaxStackSize();
	}

	/**
	 * Set max count of specific item.
	 * 
	 * @param value    must be <i>positive integer</i> or {@link OneStackConfig#COUNT_MAX} or
	 *                 {@link OneStackConfig#COUNT_DEFAULT}
	 * @param priority The larger the number, the higher the priority. see also
	 *                 {@link OneStackConfig#PRIORITY_DEFAULT_TAG}, {@link OneStackConfig#PRIORITY_DEFAULT_ITEM}
	 * @see OneStackConfig#set(TagKey, int, int)
	 */
	public static void set(Item item, int value, int priority) {
		RootConfig.INSTANCE.ITEMS_CONFIG.setMaxCount(MaxCountEntry.of(item, value, priority));
	}

	/**
	 * Set max count of specific item tag.
	 * 
	 * @param value    must be <i>positive integer</i> or {@link OneStackConfig#COUNT_MAX} or
	 *                 {@link OneStackConfig#COUNT_DEFAULT}
	 * @param priority The larger the number, the higher the priority. see also
	 *                 {@link OneStackConfig#PRIORITY_DEFAULT_TAG}, {@link OneStackConfig#PRIORITY_DEFAULT_ITEM}
	 * @see OneStackConfig#set(Item, int, int)
	 */
	public static void set(TagKey<Item> tag, int value, int priority) {
		RootConfig.INSTANCE.ITEMS_CONFIG.setMaxCount(MaxCountEntry.of(tag, value, priority));
	}

	/**
	 * Unset max count of specific item.
	 * 
	 * @see OneStackConfig#unset(TagKey)
	 */
	public static void unset(Item item) {
		RootConfig.INSTANCE.ITEMS_CONFIG.setMaxCount(MaxCountEntry.of(item));
	}

	/**
	 * Unset max count of specific item tag.
	 * 
	 * @see OneStackConfig#unset(Item)
	 */
	public static void unset(TagKey<Item> tag) {
		RootConfig.INSTANCE.ITEMS_CONFIG.setMaxCount(MaxCountEntry.of(tag));
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
