package net.jcraron.mc.onestack.mixin.core.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;

@Mixin(Containers.class)
public class ContainersMixin {

	@Redirect(method = "Lnet/minecraft/world/Containers;dropItemStack(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V",at=@At(value= "INVOKE",target = "Lnet/minecraft/world/item/ItemStack;split(I)Lnet/minecraft/world/item/ItemStack;"))
	private static ItemStack splitItemStack(ItemStack self, int oldValue) {
		ItemStack copy = self.copy();
		self.setCount(0);
		return copy;
	}
}
