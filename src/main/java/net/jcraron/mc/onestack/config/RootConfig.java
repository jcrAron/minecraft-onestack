package net.jcraron.mc.onestack.config;

import java.util.List;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.jcraron.mc.onestack.OneStackMod;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

public class RootConfig {
	private static final Logger LOGGER = LogUtils.getLogger();
	public final static RootConfig INSTANCE = new RootConfig();
	public final ItemListConfig ITEMS_CONFIG;
	private final ForgeConfigSpec ROOT_SPEC;

	private final String PROTOCOL_VERSION = "2.0.0";
	private final SimpleChannel CONFIG_CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(OneStackMod.MODID, "syncconfig"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals);

	public RootConfig() {
		Builder ROOT = new ForgeConfigSpec.Builder();
		ITEMS_CONFIG = new ItemListConfig(this::saveFile);
		ITEMS_CONFIG.registerTo(ROOT, List.of("items"));
		ITEMS_CONFIG.registerToChannel(CONFIG_CHANNEL, 1);
		ROOT_SPEC = ROOT.build();
		ConfigSync.INSTANCE.registerToChannel(CONFIG_CHANNEL, 0);
	}

	public static void registerConfig() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, RootConfig.INSTANCE.ROOT_SPEC);
	}

	@SubscribeEvent
	static void onLoading(final ModConfigEvent.Loading event) {
		RootConfig.INSTANCE.ITEMS_CONFIG.load();
		RootConfig.INSTANCE.syncToClient();
	}

	@SubscribeEvent
	static void onReload(final ModConfigEvent.Reloading event) {
		RootConfig.INSTANCE.ITEMS_CONFIG.reload();
		RootConfig.INSTANCE.syncToClient();
	}

	public void saveFile() {
		ROOT_SPEC.save();
	}

	public void syncToClient() {
		if (!Minecraft.getInstance().isLocalServer()) {
			return;
		}
		LOGGER.info("sync max count config to players");
		if (ServerLifecycleHooks.getCurrentServer() == null) {
			LOGGER.debug("ServerLifecycleHooks.getCurrentServer() == null");
			return;
		}
		ConfigSync.SingleConfig data = ConfigSync.getConfig(ConfigTracker.INSTANCE, ROOT_SPEC);
		for (ServerPlayer serverPlayer : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
			LOGGER.info("sync config to player: {}", serverPlayer.getDisplayName());
			this.CONFIG_CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), data);
		}
	}
}
