package net.jcraron.mc.onestack;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.jcraron.mc.onestack.config.FileConfigDefine;
import net.jcraron.mc.onestack.config.MaxCountData;
import net.jcraron.mc.onestack.network.ConfigSync;
import net.jcraron.mc.onestack.network.SendSetMaxCount;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(OneStackMod.MODID)
public class OneStackMod {
	public static final String MODID = "onestack";
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel CHANNEL_CONFIG = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(OneStackMod.MODID, "syncconfig"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals);

	public OneStackMod() {
		MinecraftForge.EVENT_BUS.register(this);
		FileConfigDefine.register();
		ConfigSync.INSTANCE.registerToChannel(CHANNEL_CONFIG, 0);
		SendSetMaxCount.INSTANCE.registerToChannel(CHANNEL_CONFIG, 1);
	}

	public static int getMaxCount(ItemStack itemstack) {
		return MaxCountData.getMaxCount(itemstack.getItem());
	}

	public static int getMaxCount(Item item) {
		return MaxCountData.getMaxCount(item);
	}

	public static void setMaxCount(Item item, int count) {
		if (Minecraft.getInstance().isLocalServer()) {
			MaxCountData.setMaxCount(item, count);
			FileConfigDefine.setMaxCount(item, count, count <= 0);
		} else {
			CHANNEL_CONFIG.sendToServer(new SendSetMaxCount.SetMaxCount(item, count));
		}
		LOGGER.info("Set max count: {} to {} ", ForgeRegistries.ITEMS.getKey(item).toString(),
				count >= 1 ? count : "default");
	}

	public static void setMaxCountToDefault(Item item) {
		setMaxCount(item, -1);
	}
}
