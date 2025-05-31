package net.jcraron.mc.onestack.config;

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

	public ConfigSync() {
		this(ConfigTracker.INSTANCE);
	}

	public ConfigSync(ConfigTracker tracker) {
		this.tracker = tracker;
	}

	public void registerToChannel(SimpleChannel channel, int messageIndex) {
		channel.registerMessage(messageIndex, ConfigFile.class, ConfigFile::writeToBuffer,
				ConfigFile::readFromBuffer, this::receiveSyncedConfig);
	}

	/** @param tracker ConfigTracker.INSTANCE */
	public static ConfigFile getConfig(ConfigTracker tracker, ForgeConfigSpec spec) {
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

	/** @param tracker ConfigTracker.INSTANCE */
	public ConfigFile getConfig(ForgeConfigSpec spec) {
		return getConfig(this.tracker, spec);
	}

	private void receiveSyncedConfig(ConfigFile config, Supplier<NetworkEvent.Context> contextSupplier) {
		if (!Minecraft.getInstance().isLocalServer()) {
			Optional.ofNullable(tracker.fileMap().get(config.filename()))
					.ifPresent(mc -> mc.acceptSyncedConfig(config.data()));
		}
		contextSupplier.get().setPacketHandled(true);
	}
}
