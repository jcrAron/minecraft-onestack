package net.jcraron.mc.onestack.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.slf4j.Logger;

import com.electronwill.nightconfig.core.Config;
import com.mojang.logging.LogUtils;

import net.jcraron.mc.onestack.OneStackMod;
import net.jcraron.mc.onestack.config.value.MaxCountEntryHandle;
import net.jcraron.mc.onestack.config.value.MaxCountEntryHandle.Entry;
import net.jcraron.mc.onestack.config.value.MaxCountEntryHandle.EntryKey;
import net.jcraron.mc.onestack.config.value.MaxCountEntryHandle.EntryValue;
import net.jcraron.mc.onestack.config.value.MaxCountValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

public class ItemsEntryConfig implements ConfigHandle {

	private static final Logger LOGGER = LogUtils.getLogger();
	private ForgeConfigSpec.ConfigValue<List<? extends Config>> ITEM_LIST;
	private Map<Item, Integer> cache;
	private Map<EntryKey, EntryValue> entries;
	private SimpleChannel channel;
	private Runnable saveFile;

	public ItemsEntryConfig(Runnable saveFile) {
		this.entries = new HashMap<>();
		this.cache = new HashMap<>();
		this.saveFile = saveFile;
	}

	/**
	 * @return -1 means applying the max count defined by{@link Item#getMaxStackSize(ItemStack)}
	 */
	public int getMaxCount(ItemStack itemstack) {
		Item item = itemstack.getItem();
		Integer cacheResult = cache.get(item);
		if (cacheResult != null) {
			return cacheResult;
		}
		EntryValue itemValue = entries.get(EntryKey.of(item));
		EntryValue tagValue = itemstack.getTags()
				.map(EntryKey::of).filter(entries::containsKey).map(entries::get)
				.max((v1, v2) -> Integer.compare(v1.getPriority(), v2.getPriority())).orElse(null);
		int result = MaxCountValue.JAVA_VALUE_DEFAULT;
		if (itemValue != null && tagValue != null) {
			result = itemValue.getPriority() > tagValue.getPriority() ? itemValue.getCount() : tagValue.getCount();
		} else if (itemValue != null ^ tagValue != null) {
			result = itemValue != null ? itemValue.getCount() : tagValue.getCount();
		}
		cache.put(item, result);
		return result;
	}

	public void setMaxCount(Entry entry) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!minecraft.isLocalServer()) {
			LOGGER.info("send MaxCountEntry config to server");
			channel.sendToServer(entry);
		} else {
			rawSetConfig(entry, minecraft.isLocalServer());
			LOGGER.info("send MaxCountEntry config to client");
			UUID localPlayerUUID = Optional.ofNullable(minecraft.player).map(LocalPlayer::getUUID).orElse(null);
			for (ServerPlayer serverPlayer : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
				if (localPlayerUUID != null && localPlayerUUID.equals(serverPlayer.getUUID())) {
					continue;
				}
				LOGGER.info("sync MaxCountEntry config to player: {}", serverPlayer.getDisplayName());
				channel.send(PacketDistributor.PLAYER.with(() -> serverPlayer), entry);
			}
		}
	}

	private void rawSetConfig(Entry entry, boolean save2Config) {
		LOGGER.info("set an item entry at {} config ", OneStackMod.MODID);
		LOGGER.info("clean {} config cache ", OneStackMod.MODID);
		cache.clear();
		if (entry.getValue() == null) {
			entries.remove(entry.getKey());
		} else {
			entries.put(entry.getKey(), entry.getValue());
		}
		if (!save2Config) {
			return;
		}
		LOGGER.info("save an item entry to {} config file", OneStackMod.MODID);
		@SuppressWarnings("unchecked")
		List<Config> items = (List<Config>) ITEM_LIST.get();
		if (entry.getValue() == null) {
			items.removeIf((config) -> MaxCountEntryHandle.INSTANCE.toObject(config).getKey().equals(entry.getKey()));
		} else {
			boolean replace = false;
			Config newConfig = MaxCountEntryHandle.INSTANCE.toJsonValue(entry);
			for (Iterator<Config> it = items.iterator(); it.hasNext();) {
				Config config = it.next();
				if (!MaxCountEntryHandle.INSTANCE.toObject(config).getKey().equals(entry.getKey())) {
					continue;
				}
				if (!replace) {
					config.clear();
					config.putAll(newConfig);
					replace = true;
				} else {
					it.remove();
				}
			}
			if (!replace) {
				items.add(newConfig);
			}
		}
		saveFile.run();
	}

	@Override
	public void registerTo(Builder builder, List<String> path) {
		String key_maxcount = String.format("%s can be \"max\" or \"default\" or positive integer (1~%d)",
				MaxCountEntryHandle.KEY_MAX_COUNT, MaxCountValue.JAVA_VALUE_MAX);
		String key_priority = String.format("The larger the number \"priority\", the higher the priority.",
				MaxCountEntryHandle.KEY_PRIORITY);
		ITEM_LIST = builder.comment(key_maxcount, key_priority).defineListAllowEmpty(path,
				ItemsEntryConfig::createDefaultItemList,
				ItemsEntryConfig::validator);
	}

	@Override
	public void load() {
		LOGGER.info("loading {} config", OneStackMod.MODID);
		LOGGER.info("clean {} config cache ", OneStackMod.MODID);
		cache.clear();
		entries.clear();
		boolean hasRepeat = false;
		@SuppressWarnings("unchecked")
		List<Config> items = (List<Config>) ITEM_LIST.get();
		for (Iterator<Config> it = items.iterator(); it.hasNext();) {
			Config config = it.next();
			Entry entry = MaxCountEntryHandle.INSTANCE.toObject(config);
			if (entries.containsKey(entry.getKey())) {
				it.remove();
				hasRepeat = true;
			} else {
				rawSetConfig(entry, false);
			}
		}
		if (hasRepeat) {
			saveFile.run();
		}

	}

	@Override
	public void reload() {
		load();
	}

	private static List<Config> createDefaultItemList() {
		List<Config> list = new ArrayList<>();
		list.add(createItemConfig(OneStackMod.TAG_STACKABLE, MaxCountValue.CONFIG_VALUE_DEFAULT));
		list.add(createItemConfig(Items.POTION, MaxCountValue.CONFIG_VALUE_MAX));
		list.add(createItemConfig(Items.LINGERING_POTION, MaxCountValue.CONFIG_VALUE_MAX));
		list.add(createItemConfig(Items.SPLASH_POTION, MaxCountValue.CONFIG_VALUE_MAX));
		list.add(createItemConfig(Items.MUSHROOM_STEW, MaxCountValue.CONFIG_VALUE_MAX));
		return list;
	}

	private static Config createItemConfig(Item item, Object jsonMaxCount) {
		Entry entry = Entry.of(item, MaxCountValue.INSTANCE.toObject(jsonMaxCount),
				MaxCountEntryHandle.DEFAULT_PRIORITY_ITEM);
		return MaxCountEntryHandle.INSTANCE.toJsonValue(entry);
	}

	private static Config createItemConfig(TagKey<Item> tag, Object jsonMaxCount) {
		Entry entry = Entry.of(tag, MaxCountValue.INSTANCE.toObject(jsonMaxCount),
				MaxCountEntryHandle.DEFAULT_PRIORITY_TAG);
		return MaxCountEntryHandle.INSTANCE.toJsonValue(entry);
	}

	private static boolean validator(Object object) {
		if (!(object instanceof Config)) {
			return false;
		}
		return MaxCountEntryHandle.INSTANCE.isVaildJsonValue((Config) object);
	}

	public void registerToChannel(SimpleChannel channel, int messageIndex) {
		channel.registerMessage(messageIndex, Entry.class, Entry::writeToBuffer, Entry::readFromBuffer, this::receive);
		this.channel = channel;
	}

	private void receive(Entry entry, Supplier<NetworkEvent.Context> contextSupplier) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.isLocalServer()) {
			LOGGER.info("received new entry from client");
			setMaxCount(entry);
		} else {
			rawSetConfig(entry, minecraft.isLocalServer());
		}
		contextSupplier.get().setPacketHandled(true);
	}

}
