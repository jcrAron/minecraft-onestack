package net.jcraron.mc.onestack.config;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.electronwill.nightconfig.core.Config;
import com.mojang.logging.LogUtils;

import net.jcraron.mc.onestack.OneStackMod;
import net.jcraron.mc.onestack.network.ConfigSync;
import net.jcraron.mc.onestack.network.ConfigSync.SingleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = OneStackMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FileConfigDefine {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final ForgeConfigSpec.ConfigValue<List<? extends Config>> ITEM_LIST;
	static final ForgeConfigSpec ROOT_SPEC;

	static final String KEY_ITEM_NAME = "itemName";
	static final String KEY_MAX_COUNT = "maxCount";

	static {
		Builder ROOT = new ForgeConfigSpec.Builder();
		ITEM_LIST = ROOT.comment(" 1 <= maxCount <= " + Integer.MAX_VALUE).defineListAllowEmpty("item",
				FileConfigDefine::createDefaultItemList, FileConfigDefine::elementValidator);
		ROOT_SPEC = ROOT.build();
	}

	private static List<Config> createDefaultItemList() {
		List<Config> list = new ArrayList<>();
		list.add(createConfig(Items.COBBLESTONE, Integer.MAX_VALUE));
		list.add(createConfig(Items.NETHERRACK, Integer.MAX_VALUE));
		return list;
	}

	public static void register() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, FileConfigDefine.ROOT_SPEC);
	}

	private static boolean elementValidator(Object object) {
		if (!(object instanceof Config)) {
			return false;
		}
		Config config = (Config) object;
		if (!config.contains(KEY_ITEM_NAME) || !config.contains(KEY_MAX_COUNT)) {
			return false;
		}
		if (!(config.get(KEY_ITEM_NAME) instanceof String itemName)
				|| !ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName))) {
			return false;
		}
		if (!(config.get(KEY_MAX_COUNT) instanceof Integer maxCount) || maxCount <= 0) {
			return false;
		}
		return true;
	}

	public static void setMaxCount(Item item, int maxCount, boolean toDefault) {
		@SuppressWarnings("unchecked")
		List<Config> items = (List<Config>) ITEM_LIST.get();
		if (toDefault) {
			items.removeIf((config) -> getItemByName(config.get(KEY_ITEM_NAME)) == item);
		} else {
			boolean replace = false;
			for (Config config : items) {
				if (getItemByName(config.get(KEY_ITEM_NAME)) == item) {
					config.set(KEY_MAX_COUNT, maxCount);
					replace = true;
				}
			}
			if (!replace) {
				items.add(createConfig(item, maxCount));
			}
		}
		ROOT_SPEC.save();
		syncToClient();
	}

	private static void loadEntry(Config config) {
		String itemLocation = config.get(KEY_ITEM_NAME);
		int maxCount = config.get(KEY_MAX_COUNT);
		Item item = getItemByName(itemLocation);
		MaxCountData.setRawMaxCount(item, maxCount);
	}

	private static Item getItemByName(String name) {
		return ForgeRegistries.ITEMS.getValue(new ResourceLocation(name));
	}

	@SubscribeEvent
	static void onLoading(final ModConfigEvent.Loading event) {
		MaxCountData.clear();
		ITEM_LIST.get().stream().forEach(FileConfigDefine::loadEntry);
		syncToClient();
	}

	@SubscribeEvent
	static void onReload(final ModConfigEvent.Reloading event) {
		MaxCountData.clear();
		ITEM_LIST.get().stream().forEach(FileConfigDefine::loadEntry);
		syncToClient();
	}

	private static Config createConfig(String itemLocation, int maxCount) {
		Config config = Config.inMemory();
		config.set(KEY_ITEM_NAME, itemLocation);
		config.set(KEY_MAX_COUNT, maxCount);
		return config;
	}

	private static Config createConfig(Item item, int maxCount) {
		return createConfig(ForgeRegistries.ITEMS.getKey(item).toString(), maxCount);
	}

	private static void syncToClient() {
		if (Minecraft.getInstance().isLocalServer()) {
			LOGGER.info("sync max count config to players");
			if (ServerLifecycleHooks.getCurrentServer() == null) {
				LOGGER.debug("ServerLifecycleHooks.getCurrentServer() == null");
				return;
			}
			SingleConfig data = ConfigSync.getConfig(ConfigTracker.INSTANCE, FileConfigDefine.ROOT_SPEC);
			for (ServerPlayer serverPlayer : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
				LOGGER.info("sync config to player: {}", serverPlayer.getDisplayName());
				OneStackMod.CHANNEL_CONFIG.send(PacketDistributor.PLAYER.with(() -> serverPlayer), data);
			}
		}
	}
}
