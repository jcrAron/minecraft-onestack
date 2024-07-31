package net.jcraron.mc.onestack.mixin.patch.vanilla;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {
	@Inject(method = "getQuickCraftPlaceCount(Ljava/util/Set;ILnet/minecraft/world/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
	private static void getQuickCraftPlaceCount(Set<Slot> p_279393_, int p_279288_, ItemStack p_279172_,
			CallbackInfoReturnable<Integer> info) {
		if (p_279288_ != 0) {
			return;
		}
		info.setReturnValue(p_279172_.getCount() / p_279393_.size());
	}
}
