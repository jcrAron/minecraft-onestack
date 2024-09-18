package net.jcraron.mc.onestack.mixin.core.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.jcraron.mc.onestack.config.value.MaxCountValue;
import net.minecraft.world.Container;

@Mixin(Container.class)
public interface ContainerMixin {
	@Overwrite
	default int getMaxStackSize() {
		return MaxCountValue.JAVA_VALUE_MAX;
	}
}
