package net.jcraron.mc.onestack.network;

import java.util.function.Supplier;

import net.jcraron.mc.onestack.api.MaxCountConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;

public class SendSetMaxCount {
	public final static SendSetMaxCount INSTANCE = new SendSetMaxCount();

	public static record SetMaxCount(Item item, int count) {
		private static void writeToBuffer(SetMaxCount config, FriendlyByteBuf buffer) {
			String itemName = ForgeRegistries.ITEMS.getKey(config.item()).toString();
			buffer.writeUtf(itemName);
			buffer.writeInt(config.count());
		}

		private static SetMaxCount readFromBuffer(FriendlyByteBuf buffer) {
			String itemName = buffer.readUtf();
			int count = buffer.readInt();
			Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
			return new SetMaxCount(item, count);
		}
	}

	private SendSetMaxCount() {
	}

	public void registerToChannel(SimpleChannel channel, int messageIndex) {
		channel.registerMessage(messageIndex, SetMaxCount.class, SetMaxCount::writeToBuffer,
				SetMaxCount::readFromBuffer, this::receive);
	}

	private void receive(SetMaxCount config, Supplier<NetworkEvent.Context> contextSupplier) {
		MaxCountConfig.setMaxCount(config.item(), config.count());
		contextSupplier.get().setPacketHandled(true);
	}
}
