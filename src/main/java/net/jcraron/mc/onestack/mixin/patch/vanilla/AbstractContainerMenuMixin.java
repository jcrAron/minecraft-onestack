package net.jcraron.mc.onestack.mixin.patch.vanilla;

import java.util.Optional;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.player.Player;
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

	@Redirect(method = "doClick(IILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;tryRemove(IILnet/minecraft/world/entity/player/Player;)Ljava/util/Optional;"))
	public Optional<ItemStack> doClick(Slot slot, int p_150642_, int p_150643_, Player p_150644_) {
		int count = slot.getItem().getCount();
		if (count == Integer.MAX_VALUE && p_150642_ == (count + 1) / 2) {
			p_150642_ = count / 2 + 1;
		}
		return slot.tryRemove(p_150642_, p_150643_, p_150644_);
	}
}
