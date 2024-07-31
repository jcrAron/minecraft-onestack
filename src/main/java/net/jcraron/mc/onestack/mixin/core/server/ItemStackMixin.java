package net.jcraron.mc.onestack.mixin.core.server;

import java.text.NumberFormat;
import java.util.List;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.jcraron.mc.onestack.OneStackMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	@Shadow
	private int count;

	@Redirect(method = {
			"save(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;putByte(Ljava/lang/String;B)V"))
	public void save(CompoundTag tag, String key, byte byteValue) {
		if ("Count".equals(key)) {
			tag.putInt("Count", this.count);
		} else {
			tag.putByte(key, byteValue);
		}
	}

	@Inject(method = { "<init>(Lnet/minecraft/nbt/CompoundTag;)V" }, at = @At("RETURN"))
	private void load(CompoundTag tag, CallbackInfo ci) {
		this.count = tag.getInt("Count");
	}

	@Redirect(method = {
			"getTooltipLines(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;appendHoverText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Ljava/util/List;Lnet/minecraft/world/item/TooltipFlag;)V"))
	public void appendTooltip(Item self, ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_,
			TooltipFlag p_41424_) {
		self.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
		if (this.count >= 1000) {
			p_41423_.add(
					Component.translatable("stack count: " + NumberFormat.getIntegerInstance().format(this.count)));
		}
	}

	@Inject(method = "getMaxStackSize()I", at = @At("HEAD"), cancellable = true)
	public void getMaxStackSize(CallbackInfoReturnable<Integer> info) {
		int maxCount = OneStackMod.getMaxCount((ItemStack) (Object) this);
		if (maxCount > 0) {
			info.setReturnValue(Math.max(this.count, maxCount));
		}
	}
}
