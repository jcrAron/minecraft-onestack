package net.jcraron.mc.onestack.mixin.core.client;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {

	@Shadow
	private PoseStack pose;

	@Redirect(method = {
			"renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I"))
	public int renderItemCount(GuiGraphics gui, Font p_283343_, @Nullable String p_281896_, int p_283569_,
			int p_283418_, int p_281560_, boolean p_282130_) {
		int textWidth = p_283343_.width(p_281896_);
		int maxWidth = (int) (p_283343_.width("999") * 0.9f);
		float scale = maxWidth > textWidth ? 1f : (maxWidth / (float) textWidth);
		this.pose.scale(scale, scale, 1f);
		return gui.drawString(p_283343_, p_281896_, (p_283569_ + textWidth) / scale - textWidth,
				(p_283418_ + p_283343_.lineHeight - 3) / scale - p_283343_.lineHeight + 3, p_281560_, p_282130_);
	}

	@ModifyVariable(method = {
			"renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V" }, at = @At("STORE"), ordinal = 1)
	public String setItemCount(String s, Font p_282005_, ItemStack p_283349_, int p_282641_, int p_282146_,
			@Nullable String p_282803) {
		String countText = stackCount(p_283349_);
		if (p_282803 == null) {
			return countText;
		} else if (p_282803.lastIndexOf('\u00a7') == 0) {
			return p_282803.substring(0, 2) + countText;
		} else {
			return p_282803;
		}
	}

	private static String stackCount(ItemStack it) {
		if (it == null || it.isEmpty() || it.getCount() <= 1) {
			return null;
		}
		int count = it.getCount();
		String result = null;
		if (count >= 1e9) {
			result = format(count / 1e9, "B");
		} else if (count >= 1e6) {
			result = format(count / 1e6, "M");
		} else if (count >= 1e3) {
			result = format(count / 1e3, "K");
		} else {
			result = Integer.toString(count);
		}
		return result;
	}

	private final static DecimalFormat FORMATER;
	static {
		FORMATER = new DecimalFormat("#.##");
		FORMATER.setRoundingMode(RoundingMode.FLOOR);
	}

	private static String format(double number, String suffix) {
		if (number < 10) {
			number = Math.floor(number * 100) / 100;
		} else if (number < 100) {
			number = Math.floor(number * 10) / 10;
		} else if (number < 1000) {
			number = (int) number;
		}
		return FORMATER.format(number) + suffix;
	}
}
