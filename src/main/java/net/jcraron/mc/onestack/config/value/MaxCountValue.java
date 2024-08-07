package net.jcraron.mc.onestack.config.value;

public class MaxCountValue implements ValueHandle<Integer, Object> {
	public final static MaxCountValue INSTANCE = new MaxCountValue();
	public final static String CONFIG_VALUE_MAX = "max";
	public final static String CONFIG_VALUE_DEFAULT = "default";
	public final static int JAVA_VALUE_MAX = Integer.MAX_VALUE;
	public final static int JAVA_VALUE_DEFAULT = -1;
	private final static int JAVA_VALUE_UNKNOWN = -2;

	@Override
	public Integer defaultObject() {
		return JAVA_VALUE_DEFAULT;
	}

	@Override
	public boolean isVaildJsonValue(Object rawValue) {
		if (rawValue == null) {
			return false;
		}
		int code = toObject(rawValue);
		return code == JAVA_VALUE_DEFAULT || code == JAVA_VALUE_MAX || code >= 1;
	}

	@Override
	public Integer toObject(Object rawValue) {
		int code = JAVA_VALUE_DEFAULT;
		if (rawValue instanceof String strValue) {
			code = parseStringToCode(strValue);
		} else if (rawValue instanceof Integer intValue) {
			code = intValue;
		}
		return code >= 1 ? code : JAVA_VALUE_DEFAULT;
	}

	@Override
	public Object toJsonValue(Integer object) {
		if (!isVaildJsonValue(object)) {
			return defaultObject();
		} else if (object == JAVA_VALUE_DEFAULT) {
			return CONFIG_VALUE_DEFAULT;
		} else if (object == JAVA_VALUE_MAX) {
			return CONFIG_VALUE_MAX;
		} else {
			return object;
		}
	}

	private static int parseStringToCode(String strValue) {
		strValue = strValue.toLowerCase();
		switch (strValue.toLowerCase()) {
		case CONFIG_VALUE_MAX:
			return JAVA_VALUE_MAX;
		case CONFIG_VALUE_DEFAULT:
			return JAVA_VALUE_DEFAULT;
		}
		try {
			int num = Integer.parseInt(strValue);
			if (num == Integer.MAX_VALUE) {
				return JAVA_VALUE_MAX;
			} else if (num >= 1) {
				return num;
			}
		} catch (NumberFormatException e) {
		}
		return JAVA_VALUE_UNKNOWN;
	}
}
