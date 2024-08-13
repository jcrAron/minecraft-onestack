package net.jcraron.mc.onestack.tag;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class StackableTag {
	public final static TagKey<Item> TAG_STACKABLE = ItemTags.create(new ResourceLocation("forge", "stackable"));

	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public static void appendStackableTag(TagsUpdatedEvent event) {
		Registry<Item> itemsRegistry = event.getRegistryAccess().registry(Registries.ITEM).get();
		List<Holder<Item>> list = itemsRegistry.stream().filter(item -> item.getMaxStackSize() > 1)
				.map(Item::builtInRegistryHolder).collect(Collectors.toList());
		itemsRegistry.getOrCreateTag(TAG_STACKABLE).bind(list);
	}
}
