package net.jcraron.mc.onestack.config;

import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.slf4j.Logger;

import com.electronwill.nightconfig.toml.TomlFormat;
import com.google.common.base.Objects;
import com.mojang.logging.LogUtils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

public class ConfigFileSync {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static record ConfigFile(String filename, byte[] data) {
		private static void writeToBuffer(ConfigFile config, FriendlyByteBuf buffer) {
			buffer.writeUtf(config.filename());
			buffer.writeByteArray(config.data());
		}

		private static ConfigFile readFromBuffer(FriendlyByteBuf buffer) {
			String filename = buffer.readUtf();
			byte[] bytes = buffer.readByteArray();
			return new ConfigFile(filename, bytes);
		}
	}

	public final ConfigTracker tracker;
	public final SimpleChannel channel;

	public ConfigFileSync(SimpleChannel channel, int channelIndex) {
		this(ConfigTracker.INSTANCE, channel, channelIndex);
	}

	public ConfigFileSync(ConfigTracker tracker, SimpleChannel channel, int channelIndex) {
		this.tracker = tracker;
		this.channel = channel;
		registerToChannel(channel, channelIndex);
	}

	private void registerToChannel(SimpleChannel channel, int messageIndex) {
		channel.registerMessage(messageIndex, ConfigFile.class, ConfigFile::writeToBuffer,
				ConfigFile::readFromBuffer, this::receiveConfig);
	}

	/** @param tracker ConfigTracker.INSTANCE */
	public ConfigFile getConfig(ForgeConfigSpec spec) {
		return getConfig(this.tracker, spec);
	}

	public void syncToClient(ForgeConfigSpec spec) {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null) {
			LOGGER.debug("ServerLifecycleHooks.getCurrentServer() == null");
			return;
		}
		LOGGER.info("sync the full config file to players");
		ConfigFileSync.ConfigFile data = getConfig(spec);
		UUID localPlayerUUID = server.getSingleplayerProfile() != null ? server.getSingleplayerProfile().getId() : null;
		LOGGER.debug("server player: {}", localPlayerUUID);
		for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
			if (Objects.equal(localPlayerUUID, serverPlayer.getUUID())) {
				continue;
			}
			LOGGER.info("sync the full config file to player: {}", serverPlayer.getDisplayName());
			this.channel.send(PacketDistributor.PLAYER.with(() -> serverPlayer), data);
		}
	}

	/** @param tracker ConfigTracker.INSTANCE */
	private static ConfigFile getConfig(ConfigTracker tracker, ForgeConfigSpec spec) {
		for (ModConfig config : tracker.configSets().get(ModConfig.Type.SERVER)) {
			if (config.getSpec() != spec) {
				continue;
			}
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			TomlFormat.instance().createWriter().write(config.getConfigData(), bytes);
			return new ConfigFile(config.getFileName(), bytes.toByteArray());
		}
		return null;
	}

	private void receiveConfig(ConfigFile config, Supplier<NetworkEvent.Context> contextSupplier) {
		if (ServerLifecycleHooks.getCurrentServer() != null) {
			Optional.ofNullable(tracker.fileMap().get(config.filename()))
					.ifPresent(mc -> mc.acceptSyncedConfig(config.data()));
		}
		contextSupplier.get().setPacketHandled(true);
	}
}
