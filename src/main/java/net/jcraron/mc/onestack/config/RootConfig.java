package net.jcraron.mc.onestack.config;

import java.util.List;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.jcraron.mc.onestack.OneStackMod;
import net.jcraron.mc.onestack.network.ConfigSync;
import net.jcraron.mc.onestack.network.ConfigSync.SingleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = OneStackMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RootConfig {
	private static final Logger LOGGER = LogUtils.getLogger();
	public final static RootConfig INSTANCE = new RootConfig();
	public final DefaultConfig DEFAULT_CONFIG;
	public final ItemListConfig ITEMS_CONFIG;
	private final ForgeConfigSpec ROOT_SPEC;

	public RootConfig() {
		Builder ROOT = new ForgeConfigSpec.Builder();
		DEFAULT_CONFIG = new DefaultConfig();
		DEFAULT_CONFIG.registerTo(ROOT, List.of("common"));
		ITEMS_CONFIG = new ItemListConfig();
		ITEMS_CONFIG.registerTo(ROOT, List.of("item"));
		ROOT_SPEC = ROOT.build();
	}

	public static void register() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, RootConfig.INSTANCE.ROOT_SPEC);
	}

	@SubscribeEvent
	static void onLoading(final ModConfigEvent.Loading event) {
		RootConfig.INSTANCE.DEFAULT_CONFIG.load();
		RootConfig.INSTANCE.ITEMS_CONFIG.load();
		RootConfig.INSTANCE.syncToClient();
	}

	@SubscribeEvent
	static void onReload(final ModConfigEvent.Reloading event) {
		RootConfig.INSTANCE.DEFAULT_CONFIG.reload();
		RootConfig.INSTANCE.ITEMS_CONFIG.reload();
		RootConfig.INSTANCE.syncToClient();
	}

	public void save() {
		ROOT_SPEC.save();
		syncToClient();
	}

	private void syncToClient() {
		if (Minecraft.getInstance().isLocalServer()) {
			LOGGER.info("sync max count config to players");
			if (ServerLifecycleHooks.getCurrentServer() == null) {
				LOGGER.debug("ServerLifecycleHooks.getCurrentServer() == null");
				return;
			}
			SingleConfig data = ConfigSync.getConfig(ConfigTracker.INSTANCE, ROOT_SPEC);
			for (ServerPlayer serverPlayer : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
				LOGGER.info("sync config to player: {}", serverPlayer.getDisplayName());
				OneStackMod.CHANNEL_CONFIG.send(PacketDistributor.PLAYER.with(() -> serverPlayer), data);
			}
		}
	}
}
