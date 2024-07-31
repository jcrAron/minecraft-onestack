package net.jcraron.mc.onestack.mixin.patch.vanilla;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Mixin(BucketItem.class)
public class BucketItemMixin {
	@Inject(method = "getEmptySuccessItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
	private static void getEmptySuccessItem(ItemStack fluidBucket, Player player,
			CallbackInfoReturnable<ItemStack> info) {
		if (!player.getAbilities().instabuild) {
			fluidBucket.shrink(1);
			ItemStack it = new ItemStack(Items.BUCKET);
			if (!player.addItem(it)) {
				player.drop(it, false, false);
			}
		}
		info.setReturnValue(fluidBucket);
	}
}
