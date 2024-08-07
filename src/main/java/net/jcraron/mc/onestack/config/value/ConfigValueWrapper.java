package net.jcraron.mc.onestack.config.value;

import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

/**
 * @param <JSON> Type of
 *               {@link net.minecraftforge.common.ForgeConfigSpec.ConfigValue}
 */
public class ConfigValueWrapper<CLASS, JSON> extends ValueWrapper<CLASS, JSON> {
	private ConfigValue<JSON> config;

	public ConfigValueWrapper(ValueHandle<CLASS, JSON> valueHandle, ConfigValue<JSON> config) {
		super(valueHandle);
		this.config = config;
	}

	public ConfigValue<JSON> getConfig() {
		return config;
	}

	public CLASS get() {
		return this.getValueHandle().toObject(config.get());
	}

	public void set(CLASS value) {
		config.set(this.getValueHandle().toJsonValue(value));
	}

}