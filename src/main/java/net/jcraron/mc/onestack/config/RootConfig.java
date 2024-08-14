package net.jcraron.mc.onestack.config;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.jcraron.mc.onestack.OneStackMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

public class RootConfig {
	private static final Logger LOGGER = LogUtils.getLogger();
	public final static RootConfig INSTANCE = new RootConfig();
	public final ItemsEntryConfig ITEMS_CONFIG;
	private final ForgeConfigSpec ROOT_SPEC;

	private final String PROTOCOL_VERSION = "2.0.0";
	private final SimpleChannel CONFIG_CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(OneStackMod.MODID, "syncconfig"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals);
	public final ConfigSync CONFIG_SYNC;

	public RootConfig() {
		Builder ROOT = new ForgeConfigSpec.Builder();
		ITEMS_CONFIG = new ItemsEntryConfig(this::saveFile);
		ITEMS_CONFIG.registerTo(ROOT, List.of("items"));
		ITEMS_CONFIG.registerToChannel(CONFIG_CHANNEL, 1);
		ROOT_SPEC = ROOT.build();
		CONFIG_SYNC = new ConfigSync();
		CONFIG_SYNC.registerToChannel(CONFIG_CHANNEL, 0);
	}

	public void registerConfig() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ROOT_SPEC);
	}

	public void saveFile() {
		ROOT_SPEC.save();
	}

	public void syncToClient() {
		Minecraft minecraft = Minecraft.getInstance();
		if (!minecraft.isLocalServer()) {
			return;
		}
		LOGGER.info("sync max count config to players");
		if (ServerLifecycleHooks.getCurrentServer() == null) {
			LOGGER.debug("ServerLifecycleHooks.getCurrentServer() == null");
			return;
		}
		ConfigSync.SingleConfig data = CONFIG_SYNC.getConfig(ROOT_SPEC);
		UUID localPlayerUUID = Optional.ofNullable(minecraft.player).map(LocalPlayer::getUUID).orElse(null);
		for (ServerPlayer serverPlayer : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
			if (localPlayerUUID != null && localPlayerUUID.equals(serverPlayer.getUUID())) {
				continue;
			}
			LOGGER.info("sync config to player: {}", serverPlayer.getDisplayName());
			this.CONFIG_CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), data);
		}
	}
}
