package net.jcraron.mc.onestack.config.value;

import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

/**
 * <h1>JSON basic type:</h1>
 * <ul>
 * <li>string</li>
 * <li>number</li>
 * <li>boolean</li>
 * <li>null</li>
 * <li>JSON object ({@link com.electronwill.nightconfig.core.Config})</li>
 * <li>array ({@link java.util.List}&lt;Config&gt;)</li>
 * </ul>
 * 
 * @param <CLASS> Java Object type
 * @param <JSON>  JSON basic type
 */
public interface ValueHandle<CLASS, JSON> {
	public CLASS defaultObject();

	public default JSON defaultJson() {
		return toJsonValue(defaultObject());
	}

	/**
	 * @param jsonValue
	 */
	public boolean isVaildJsonValue(JSON jsonValue);

	/**
	 * @param jsonValue
	 * @return object that should be modifiable
	 * @throws IllegalArgumentException if rawValue is invalid.
	 */
	public CLASS toObject(JSON jsonValue);

	/**
	 * @param javaObject
	 * @return jsonValue
	 * @throws IllegalArgumentException if object is invalid.
	 */
	public JSON toJsonValue(CLASS javaObject);

	public default ConfigValueWrapper<CLASS, JSON> wrapConfig(ConfigValue<JSON> config) {
		return new ConfigValueWrapper<>(this, config);
	}
}
