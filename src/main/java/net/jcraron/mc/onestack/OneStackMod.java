package net.jcraron.mc.onestack;

import net.jcraron.mc.onestack.config.RootConfig;
import net.jcraron.mc.onestack.network.ConfigSync;
import net.jcraron.mc.onestack.network.SendSetMaxCount;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod(OneStackMod.MODID)
public class OneStackMod {
	public static final String MODID = "onestack";
//	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel CHANNEL_CONFIG = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(OneStackMod.MODID, "syncconfig"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals);

	public OneStackMod() {
		MinecraftForge.EVENT_BUS.register(this);
		RootConfig.register();
		ConfigSync.INSTANCE.registerToChannel(CHANNEL_CONFIG, 0);
		SendSetMaxCount.INSTANCE.registerToChannel(CHANNEL_CONFIG, 1);
	}
}
