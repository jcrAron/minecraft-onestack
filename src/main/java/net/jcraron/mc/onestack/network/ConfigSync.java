package net.jcraron.mc.onestack.network;

import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.function.Supplier;

import com.electronwill.nightconfig.toml.TomlFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

public class ConfigSync {
	public final static ConfigSync INSTANCE = new ConfigSync();

	public static record SingleConfig(String filename, byte[] data) {
		private static void writeToBuffer(SingleConfig config, FriendlyByteBuf buffer) {
			buffer.writeUtf(config.filename());
			buffer.writeByteArray(config.data());
		}

		private static SingleConfig readFromBuffer(FriendlyByteBuf buffer) {
			String filename = buffer.readUtf();
			byte[] bytes = buffer.readByteArray();
			return new SingleConfig(filename, bytes);
		}
	}
	private final ConfigTracker tracker;
	private ConfigSync() {
		tracker = ConfigTracker.INSTANCE;
	}

	public void registerToChannel(SimpleChannel channel, int messageIndex) {
		channel.registerMessage(messageIndex, SingleConfig.class, SingleConfig::writeToBuffer,
				SingleConfig::readFromBuffer, this::receiveSyncedConfig);
	}

	/** @param tracker ConfigTracker.INSTANCE */
	public static SingleConfig getConfig(ConfigTracker tracker, ForgeConfigSpec spec) {
		for (ModConfig config : tracker.configSets().get(ModConfig.Type.SERVER)) {
			if (config.getSpec() != spec) {
				continue;
			}
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			TomlFormat.instance().createWriter().write(config.getConfigData(), bytes);
			return new SingleConfig(config.getFileName(), bytes.toByteArray());
		}
		return null;
	}

	private void receiveSyncedConfig(SingleConfig config, Supplier<NetworkEvent.Context> contextSupplier) {
		loadConfig(config);
		contextSupplier.get().setPacketHandled(true);
	}

	/** @param tracker ConfigTracker.INSTANCE */
	private void loadConfig(SingleConfig config) {
		if (!Minecraft.getInstance().isLocalServer()) {
			Optional.ofNullable(tracker.fileMap().get(config.filename()))
					.ifPresent(mc -> mc.acceptSyncedConfig(config.data()));
		}
	}
}
