package net.jcraron.mc.onestack.mixin.patch.vanilla;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;

@Mixin(BundleItem.class)
public class BundleItemMixin {
	@Inject(method = "getWeight(Lnet/minecraft/world/item/ItemStack;)I", at = @At(value = "RETURN", ordinal = 2), cancellable = true)
	private static void getWeight(ItemStack item, CallbackInfoReturnable<Integer> info) {
		if (item.getMaxStackSize() >= 64) {
			info.setReturnValue(1);
		}
	}
}
