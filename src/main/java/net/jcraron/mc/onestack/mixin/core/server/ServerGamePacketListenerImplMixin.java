package net.jcraron.mc.onestack.mixin.core.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
	/** less than 64 */
	private final static int TRUE = 0;
	/** greater than 64 */
	private final static int FALSE = 100;

	@Redirect(method = "handleSetCreativeModeSlot(Lnet/minecraft/network/protocol/game/ServerboundSetCreativeModeSlotPacket;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getCount()I"))
	public int handleSetCreativeModeSlot(ItemStack self) {
		return self.getCount() <= self.getMaxStackSize() ? TRUE : FALSE;
	}
}
