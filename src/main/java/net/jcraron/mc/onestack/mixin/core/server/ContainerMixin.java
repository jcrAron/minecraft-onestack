package net.jcraron.mc.onestack.mixin.core.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.jcraron.mc.onestack.config.value.CountNumberValue;
import net.minecraft.world.Container;

@Mixin(Container.class)
public interface ContainerMixin {
	/** 
	 * @reason overwrite the max size in default. respect instances that override this function
	 * @author jcrAron
	 * */
	@Overwrite
	default int getMaxStackSize() {
		return CountNumberValue.JAVA_VALUE_MAX;
	}
}
