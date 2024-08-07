package net.jcraron.mc.onestack.config;

import java.util.List;

import net.jcraron.mc.onestack.config.value.ConfigValueWrapper;
import net.jcraron.mc.onestack.config.value.MaxCountValue;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class DefaultConfig implements ConfigHandle {

	private ConfigValueWrapper<Integer, Object> defaultMaxCount;

	@Override
	public void registerTo(ForgeConfigSpec.Builder builder, List<String> base) {
		ConfigValue<Object> defaultMaxCountConfig = builder.comment(
				String.format("Valid value: \"%s\", \"%s\", 1 <= maxCount <= %s", MaxCountValue.CONFIG_VALUE_MAX,
						MaxCountValue.CONFIG_VALUE_DEFAULT, MaxCountValue.JAVA_VALUE_MAX))
				.define(ConfigHandle.withNextPath(base, "maxCount"), MaxCountValue.INSTANCE.defaultJson(),
						MaxCountValue.INSTANCE::isVaildJsonValue);

		this.defaultMaxCount = MaxCountValue.INSTANCE.wrapConfig(defaultMaxCountConfig);
	}

	public ConfigValueWrapper<Integer, Object> getCommonMaxCount() {
		return defaultMaxCount;
	}

	@Override
	public void load() {
	}

	@Override
	public void reload() {
	}
}
