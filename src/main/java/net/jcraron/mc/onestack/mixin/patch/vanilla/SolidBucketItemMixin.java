package net.jcraron.mc.onestack.mixin.patch.vanilla;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SolidBucketItem;

@Mixin(SolidBucketItem.class)
public class SolidBucketItemMixin {

	@Redirect(method = "useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", 
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V"))
	public void useOn(Player player, InteractionHand p_21009_, ItemStack p_21010_) {
		if (!player.addItem(p_21010_)) {
			player.drop(p_21010_, false);
		}
	}
}
