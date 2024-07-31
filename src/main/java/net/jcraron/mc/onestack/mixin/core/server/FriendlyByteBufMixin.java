package net.jcraron.mc.onestack.mixin.core.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;

@Mixin(FriendlyByteBuf.class)
public class FriendlyByteBufMixin {
	@Redirect(method = {
			"writeItemStack(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/network/FriendlyByteBuf;" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;writeByte(I)Lio/netty/buffer/ByteBuf;"))
	public ByteBuf writeItemStackCount(FriendlyByteBuf self, int p_130470_) {
		return self.writeInt(p_130470_);
	}

	@Redirect(method = {
			"readItem()Lnet/minecraft/world/item/ItemStack;" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;readByte()B"))
	public byte readByte(FriendlyByteBuf self) {
		return 0;
	}

	@ModifyVariable(method = { "readItem()Lnet/minecraft/world/item/ItemStack;" }, at = @At(value = "STORE"))
	public int readItemStackCount(int oldValue) {
		return ((FriendlyByteBuf) (Object) this).readInt();
	}
}
