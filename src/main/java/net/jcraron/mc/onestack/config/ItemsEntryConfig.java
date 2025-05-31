package net.jcraron.mc.onestack.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import org.slf4j.Logger;

import com.electronwill.nightconfig.core.Config;
import com.mojang.logging.LogUtils;

import net.jcraron.mc.onestack.OneStackMod;
import net.jcraron.mc.onestack.config.value.CountNumberValue;
import net.jcraron.mc.onestack.config.value.MaxCountEntryHandle;
import net.jcraron.mc.onestack.config.value.MaxCountEntryHandle.ItemOrTag;
import net.jcraron.mc.onestack.config.value.MaxCountEntryHandle.MaxCountEntry;
import net.jcraron.mc.onestack.config.value.MaxCountEntryHandle.MaxCountValue;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

public class ItemsEntryConfig implements ConfigHandle {

	static final Logger LOGGER = LogUtils.getLogger();
	private ForgeConfigSpec.ConfigValue<List<? extends Config>> ITEM_LIST;
	private Map<Item, Integer> cache;
	private Map<ItemOrTag, MaxCountValue> entries;
	private Runnable saveFile;
	private EntrySync sync;

	public ItemsEntryConfig(Runnable saveFile, SimpleChannel channel, int channelIndex) {
		this.cache = new HashMap<>();
		this.entries = new HashMap<>();
		this.saveFile = saveFile;
		this.sync = new EntrySync(channel, channelIndex);
	}

	public void cleanCache() {
		LOGGER.info("clean {} config cache", OneStackMod.MODID);
		cache.clear();
	}

	private void cleanEntries() {
		LOGGER.info("clean {} config entries", OneStackMod.MODID);
		entries.clear();
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
		MaxCountValue itemValue = entries.get(ItemOrTag.of(item));
		MaxCountValue tagValue = itemstack.getTags()
				.map(ItemOrTag::of).filter(entries::containsKey).map(entries::get)
				.max((v1, v2) -> Integer.compare(v1.getPriority(), v2.getPriority())).orElse(null);
		int result = CountNumberValue.JAVA_VALUE_DEFAULT;
		if (itemValue != null && tagValue != null) {
			result = itemValue.getPriority() > tagValue.getPriority() ? itemValue.getCount() : tagValue.getCount();
		} else if (itemValue != null ^ tagValue != null) {
			result = itemValue != null ? itemValue.getCount() : tagValue.getCount();
		}
		cache.put(item, result);
		return result;
	}

	public void setMaxCount(MaxCountEntry maxCountEntry) {
		if (ServerLifecycleHooks.getCurrentServer() == null) {
			LOGGER.info("send MaxCountEntry config to server");
			sync.sendToServer(maxCountEntry);
		} else {
			sync.sendToPlayers(maxCountEntry);
			setMaxCount_entries(maxCountEntry);
			setMaxCount_savefile(maxCountEntry);
		}
	}

	private void setMaxCount_savefile(MaxCountEntry maxCountEntry) {
		@SuppressWarnings("unchecked")
		List<Config> items = (List<Config>) ITEM_LIST.get();
		if (maxCountEntry.getValue() == null) {
			items.removeIf(
					(config) -> MaxCountEntryHandle.INSTANCE.toObject(config).getItemTag()
							.equals(maxCountEntry.getItemTag()));
		} else {
			boolean replace = false;
			Config newConfig = MaxCountEntryHandle.INSTANCE.toJsonValue(maxCountEntry);
			for (Iterator<Config> it = items.iterator(); it.hasNext();) {
				Config config = it.next();
				if (!MaxCountEntryHandle.INSTANCE.toObject(config).getItemTag().equals(maxCountEntry.getItemTag())) {
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
		LOGGER.info("save a {} to {} config file", maxCountEntry, OneStackMod.MODID);
	}

	private void setMaxCount_entries(MaxCountEntry maxCountEntry) {
		LOGGER.info("set an {} at {} config ", maxCountEntry.toString(), OneStackMod.MODID);
		cleanCache();
		if (maxCountEntry.getValue() == null) {
			entries.remove(maxCountEntry.getItemTag());
		} else {
			entries.put(maxCountEntry.getItemTag(), maxCountEntry.getValue());
		}
	}

	@Override
	public void registerToSpec(ForgeConfigSpec.Builder builder, List<String> path) {
		String key_maxcount = String.format("%s can be \"max\" or \"default\" or positive integer (1~%d)",
				MaxCountEntryHandle.KEY_MAX_COUNT, CountNumberValue.JAVA_VALUE_MAX);
		String key_priority = String.format("The larger the number \"priority\", the higher the priority.",
				MaxCountEntryHandle.KEY_PRIORITY);
		ITEM_LIST = builder.comment(key_maxcount, key_priority).defineListAllowEmpty(path,
				ItemsEntryConfig::createDefaultItemList,
				ItemsEntryConfig::validator);
	}

	@Override
	public void load() {
		LOGGER.info("{} config loading", OneStackMod.MODID);
		cleanCache();
		cleanEntries();
		boolean hasRepeat = false;
		@SuppressWarnings("unchecked")
		List<Config> items = (List<Config>) ITEM_LIST.get();
		for (Iterator<Config> it = items.iterator(); it.hasNext();) {
			Config config = it.next();
			MaxCountEntry maxCountEntry = MaxCountEntryHandle.INSTANCE.toObject(config);
			if (entries.containsKey(maxCountEntry.getItemTag())) {
				it.remove();
				hasRepeat = true;
			} else {
				setMaxCount_entries(maxCountEntry);
			}
		}
		if (hasRepeat) {
			saveFile.run();
		}
		LOGGER.info("{} config loaded", OneStackMod.MODID);
	}

	@Override
	public void reload() {
		load();
	}

	private static List<Config> createDefaultItemList() {
		List<Config> list = new ArrayList<>();
		list.add(createEntry(OneStackMod.TAG_STACKABLE, CountNumberValue.CONFIG_VALUE_DEFAULT));
		list.add(createEntry(Items.POTION, CountNumberValue.CONFIG_VALUE_MAX));
		list.add(createEntry(Items.LINGERING_POTION, CountNumberValue.CONFIG_VALUE_MAX));
		list.add(createEntry(Items.SPLASH_POTION, CountNumberValue.CONFIG_VALUE_MAX));
		list.add(createEntry(Items.MUSHROOM_STEW, CountNumberValue.CONFIG_VALUE_MAX));
		return list;
	}

	private static Config createEntry(Item item, Object jsonMaxCount) {
		MaxCountEntry maxCountEntry = MaxCountEntry.of(item, CountNumberValue.INSTANCE.toObject(jsonMaxCount),
				MaxCountEntryHandle.DEFAULT_PRIORITY_ITEM);
		return MaxCountEntryHandle.INSTANCE.toJsonValue(maxCountEntry);
	}

	private static Config createEntry(TagKey<Item> tag, Object jsonMaxCount) {
		MaxCountEntry maxCountEntry = MaxCountEntry.of(tag, CountNumberValue.INSTANCE.toObject(jsonMaxCount),
				MaxCountEntryHandle.DEFAULT_PRIORITY_TAG);
		return MaxCountEntryHandle.INSTANCE.toJsonValue(maxCountEntry);
	}

	private static boolean validator(Object object) {
		if (!(object instanceof Config)) {
			return false;
		}
		return MaxCountEntryHandle.INSTANCE.isVaildJsonValue((Config) object);
	}

	private class EntrySync {
		private static final Logger LOGGER = LogUtils.getLogger();
		private SimpleChannel channel;

		EntrySync(SimpleChannel channel, int channelIndex) {
			this.channel = channel;
			registerToChannel(channel, channelIndex);
		}

		private void registerToChannel(SimpleChannel channel, int channelIndex) {
			channel.registerMessage(channelIndex, MaxCountEntry.class, MaxCountEntry::writeToBuffer,
					MaxCountEntry::readFromBuffer, this::receive);
		}

		private void receive(MaxCountEntry maxCountEntry, Supplier<NetworkEvent.Context> contextSupplier) {
			setMaxCount_entries(maxCountEntry);
			if (ServerLifecycleHooks.getCurrentServer() != null) {
				EntrySync.LOGGER.info("received {} from client", maxCountEntry.toString());
				sendToPlayers(maxCountEntry);
				setMaxCount_savefile(maxCountEntry);
			}
			contextSupplier.get().setPacketHandled(true);
		}

		public void sendToServer(MaxCountEntry maxCountEntry) {
			channel.sendToServer(maxCountEntry);
		}

		public void sendToPlayers(MaxCountEntry maxCountEntry) {
			EntrySync.LOGGER.info("send a MaxCountEntry config to players");
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			UUID localPlayerUUID = server.getSingleplayerProfile() != null ? server.getSingleplayerProfile().getId()
					: null;
			for (ServerPlayer serverPlayer : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
				if (localPlayerUUID != null && localPlayerUUID.equals(serverPlayer.getUUID())) {
					continue;
				}
				EntrySync.LOGGER.info("sync {} config to player: {}",
						maxCountEntry.toString(),
						serverPlayer.getDisplayName());
				channel.send(PacketDistributor.PLAYER.with(() -> serverPlayer), maxCountEntry);
			}
		}
	}
}
