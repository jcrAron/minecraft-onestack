package net.jcraron.mc.onestack.mixin.patch.vanilla;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

@Mixin(BowlFoodItem.class)
public class BowlFoodItemMixin {

	/**
	 * fix: Eating the food that count be greater than 1 that will become an bowl
	 */
	@Inject(method = "finishUsingItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
	public void finishUsingItem(ItemStack p_40684_, Level p_40685_, LivingEntity entity,
			CallbackInfoReturnable<ItemStack> info, ItemStack remainingFood) {
		if (entity instanceof Player player && !player.getAbilities().instabuild) {
			ItemStack it = new ItemStack(Items.BOWL);
			if (!player.addItem(it)) {
				player.drop(it, false, false);
			}
		}
		info.setReturnValue(remainingFood);
	}
}
