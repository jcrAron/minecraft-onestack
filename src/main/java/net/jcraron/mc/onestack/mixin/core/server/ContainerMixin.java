package net.jcraron.mc.onestack.mixin.core.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.world.Container;

@Mixin(Container.class)
public interface ContainerMixin {
	@Overwrite
	default int getMaxStackSize() {
		return Integer.MAX_VALUE;
	}
}
