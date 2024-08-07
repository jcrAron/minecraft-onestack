package net.jcraron.mc.onestack.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.electronwill.nightconfig.core.Config;

import net.jcraron.mc.onestack.config.value.MaxCountValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemListConfig implements ConfigHandle {

	private static final String KEY_ITEM_NAME = "itemName";
	private static final String KEY_MAX_COUNT = "maxCount";

	private ForgeConfigSpec.ConfigValue<List<? extends Config>> ITEM_LIST;
	private Map<Item, Integer> itemMap;

	public ItemListConfig() {
		this.itemMap = new HashMap<>();
	}

	/** @return null if not setting */
	public Integer getMaxCount(ItemStack itemstack) {
		return itemMap.get(itemstack.getItem());
	}

	public void setMaxCount(Item item, int value) {
		@SuppressWarnings("unchecked")
		List<Config> items = (List<Config>) ITEM_LIST.get();
		if (value == MaxCountValue.JAVA_VALUE_DEFAULT || !MaxCountValue.INSTANCE.isVaildJsonValue(value)) {
			items.removeIf((config) -> getItemByName(config.get(KEY_ITEM_NAME)) == item);
		} else {
			boolean replace = false;
			for (Config config : items) {
				if (getItemByName(config.get(KEY_ITEM_NAME)) == item) {
					config.set(KEY_MAX_COUNT, MaxCountValue.INSTANCE.toJsonValue(value));
					replace = true;
				}
			}
			if (!replace) {
				items.add(createItemConfig(item, value));
			}
		}
		setCacheMaxCount(item, value);
		// TODO save
	}

	@Override
	public void registerTo(Builder builder, List<String> path) {
		ITEM_LIST = builder.defineListAllowEmpty(path, ItemListConfig::createDefaultItemList,
				ItemListConfig::validator);
	}

	private void loadEntry(Config config) {
		String itemLocation = config.get(KEY_ITEM_NAME);
		Object maxCount = config.get(KEY_MAX_COUNT);
		setCacheMaxCount(getItemByName(itemLocation), MaxCountValue.INSTANCE.toObject(maxCount));
	}

	private void setCacheMaxCount(Item item, int maxCount) {
		if (maxCount >= 1) {
			itemMap.put(item, maxCount);
		} else {
			itemMap.remove(item);
		}
	}

	@Override
	public void load() {
		itemMap.clear();
		ITEM_LIST.get().stream().forEach(this::loadEntry);

	}

	@Override
	public void reload() {
		load();
	}

	private static List<Config> createDefaultItemList() {
		List<Config> list = new ArrayList<>();
		list.add(createItemConfig(Items.POTION, MaxCountValue.CONFIG_VALUE_MAX));
		list.add(createItemConfig(Items.LINGERING_POTION, MaxCountValue.CONFIG_VALUE_MAX));
		list.add(createItemConfig(Items.SPLASH_POTION, MaxCountValue.CONFIG_VALUE_MAX));
		list.add(createItemConfig(Items.MUSHROOM_STEW, MaxCountValue.CONFIG_VALUE_MAX));
		return list;
	}

	private static Config createItemConfig(Item item, Object jsonMaxCount) {
//		MaxCountValue.checkValid(maxCount);
		Config config = Config.inMemory();
		String itemLocation = ForgeRegistries.ITEMS.getKey(item).toString();
		config.set(KEY_ITEM_NAME, itemLocation);
		config.set(KEY_MAX_COUNT, jsonMaxCount);
		return config;
	}

	private static boolean validator(Object object) {
		if (!(object instanceof Config)) {
			return false;
		}
		Config config = (Config) object;
		if (!config.contains(KEY_ITEM_NAME) || !(config.get(KEY_ITEM_NAME) instanceof String itemName)
				|| !ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName))) {
			return false;
		}
		if (!config.contains(KEY_MAX_COUNT) || !MaxCountValue.INSTANCE.isVaildJsonValue(config.get(KEY_MAX_COUNT))) {
			return false;
		}
		return true;
	}

	private Item getItemByName(String itemLocation) {
		return ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemLocation));
	}

}
