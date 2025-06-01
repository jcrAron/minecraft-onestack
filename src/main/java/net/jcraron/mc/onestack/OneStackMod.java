package net.jcraron.mc.onestack;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;

import net.jcraron.mc.onestack.api.OneStackConfig;
import net.jcraron.mc.onestack.command.MaxCountCommand;
import net.jcraron.mc.onestack.config.RootConfig;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod(OneStackMod.MODID)
public class OneStackMod {
	public static final String MODID = "onestack";
	private static final Logger LOGGER = LogUtils.getLogger();

	public final static TagKey<Item> TAG_STACKABLE = ItemTags.create(new ResourceLocation("forge", "stackable"));

	public OneStackMod() {
		RootConfig.INSTANCE.registerConfig();
	}

	@Mod.EventBusSubscriber(modid = OneStackMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class ForgeEvents {
		@SubscribeEvent
		public static void registerCommands(RegisterCommandsEvent event) {
			CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
			CommandBuildContext context = event.getBuildContext();
			dispatcher.register(MaxCountCommand.createCommand(context));
		}

		@SuppressWarnings("deprecation")
		@SubscribeEvent
		public static void appendStackableTag(TagsUpdatedEvent event) {
			Registry<Item> itemsRegistry = event.getRegistryAccess().registry(Registries.ITEM).get();
			List<Holder<Item>> list = itemsRegistry.stream().filter(item -> item.getMaxStackSize() > 1)
					.map(Item::builtInRegistryHolder).collect(Collectors.toList());
			itemsRegistry.getOrCreateTag(TAG_STACKABLE).bind(list);
			LOGGER.info("added tag: {}", TAG_STACKABLE.toString());
			OneStackConfig.cleanCache();
		}
	}

	@Mod.EventBusSubscriber(modid = OneStackMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class ModEvents {
		@SubscribeEvent
		public static void onLoading(final ModConfigEvent.Loading event) {
			RootConfig.INSTANCE.load();
		}

		@SubscribeEvent
		public static void onReload(final ModConfigEvent.Reloading event) {
			RootConfig.INSTANCE.reload();
		}
	}

}
