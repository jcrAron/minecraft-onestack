package net.jcraron.mc.onestack.config.value;

public abstract class ValueWrapper<CLASS, JSON> {
	private ValueHandle<CLASS, JSON> valueHandle;

	public ValueWrapper(ValueHandle<CLASS, JSON> valueHandle) {
		this.valueHandle = valueHandle;
	}

	public ValueHandle<CLASS, JSON> getValueHandle() {
		return valueHandle;
	}

	public abstract CLASS get();

	public abstract void set(CLASS value);
}
