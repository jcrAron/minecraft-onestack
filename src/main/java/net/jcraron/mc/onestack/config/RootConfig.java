package net.jcraron.mc.onestack.config;

import java.util.List;

import net.jcraron.mc.onestack.OneStackMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class RootConfig {
	public final static RootConfig INSTANCE = new RootConfig();
	public final ItemsEntryConfig ITEMS_CONFIG;
	private final ForgeConfigSpec CONFIG_SPEC;

	private final String PROTOCOL_VERSION = "2.0.0";
	private final SimpleChannel CONFIG_CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(OneStackMod.MODID, "syncconfig"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals);
	public final ConfigFileSync CONFIG_FILE_SYNC;

	public RootConfig() {
		CONFIG_FILE_SYNC = new ConfigFileSync(CONFIG_CHANNEL, 0);
		ITEMS_CONFIG = new ItemsEntryConfig(this::saveFile, CONFIG_CHANNEL, 1);
		Builder ROOT = new ForgeConfigSpec.Builder();
		ITEMS_CONFIG.registerToSpec(ROOT, List.of("items"));
		CONFIG_SPEC = ROOT.build();
	}

	public void load() {
		ITEMS_CONFIG.load();
		CONFIG_FILE_SYNC.syncToClient(CONFIG_SPEC);
	}

	public void reload() {
		ITEMS_CONFIG.reload();
		CONFIG_FILE_SYNC.syncToClient(CONFIG_SPEC);
	}

	public void registerConfig() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CONFIG_SPEC);
	}

	public void saveFile() {
		CONFIG_SPEC.save();
	}

}
