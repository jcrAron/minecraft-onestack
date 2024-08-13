package net.jcraron.mc.onestack;

import net.jcraron.mc.onestack.command.MaxCountCommand;
import net.jcraron.mc.onestack.config.RootConfig;
import net.jcraron.mc.onestack.tag.StackableTag;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(OneStackMod.MODID)
public class OneStackMod {
	public static final String MODID = "onestack";
//	private static final Logger LOGGER = LogUtils.getLogger();

	public OneStackMod() {
		MinecraftForge.EVENT_BUS.register(StackableTag.class);
		MinecraftForge.EVENT_BUS.register(MaxCountCommand.class);
		MinecraftForge.EVENT_BUS.register(RootConfig.class);
		RootConfig.registerConfig();
	}
}
