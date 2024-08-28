package net.jcraron.mc.onestack.config.value;

import java.util.Objects;

import javax.annotation.Nullable;

import com.electronwill.nightconfig.core.Config;

import net.jcraron.mc.onestack.config.value.MaxCountEntryHandle.Entry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class MaxCountEntryHandle implements ValueHandle<Entry, Config> {

	public static final String KEY_ITEM_NAME = "name";
	public static final String KEY_ITEM_TAG = "tag";
	public static final String KEY_MAX_COUNT = "maxCount";
	public static final String KEY_PRIORITY = "priority";
	public static final int DEFAULT_PRIORITY_ITEM = 1000;
	public static final int DEFAULT_PRIORITY_TAG = 0;
	public final static MaxCountEntryHandle INSTANCE = new MaxCountEntryHandle();

	@Override
	public boolean isVaildObject(Entry javaObject) {
		return javaObject.getKey() != null;
	}

	@Override
	public Entry defaultObject() {
		return null;
	}

	@Override
	public boolean isVaildJsonValue(Config jsonValue) {
		if (!EntryKey.isValid(jsonValue)) {
			return false;
		}
		if (!EntryValue.isValid(jsonValue)) {
			return false;
		}
		return true;
	}

	@Override
	public Entry toObject(Config jsonValue) {
		if (!isVaildJsonValue(jsonValue)) {
			throw new IllegalArgumentException("invalid value: " + jsonValue.toString());
		}
		EntryKey key = EntryKey.createObject(jsonValue);
		EntryValue value = EntryValue.createObject(jsonValue);
		return new Entry(key, value);
	}

	@Override
	public Config toJsonValue(Entry entry) {
		Config config = Config.inMemory();
		entry.key.serial2Config(config);
		entry.value.serial2Config(config);
		return config;
	}

	public final static class Entry {
		private EntryKey key;
		@Nullable
		private EntryValue value;

		private Entry(EntryKey key, EntryValue value) {
			this.key = key;
			this.value = value;
		}

		public static Entry of(Item item, int maxcount, int priority) {
			EntryKey key = EntryKey.of(item);
			EntryValue value = new EntryValue(maxcount, priority);
			return new Entry(key, value);
		}

		public static Entry of(TagKey<Item> tag, int maxcount, int priority) {
			EntryKey key = EntryKey.of(tag);
			EntryValue value = new EntryValue(maxcount, priority);
			return new Entry(key, value);
		}

		public static Entry of(Item item) {
			return new Entry(EntryKey.of(item), null);
		}

		public static Entry of(TagKey<Item> tag) {
			return new Entry(EntryKey.of(tag), null);
		}

		public static void writeToBuffer(Entry entry, FriendlyByteBuf buffer) {
			EntryKey.writeToBuffer(entry.key, buffer);
			EntryValue.writeToBuffer(entry.value, buffer);
		}

		public static Entry readFromBuffer(FriendlyByteBuf buffer) {
			EntryKey key = EntryKey.readFromBuffer(buffer);
			EntryValue value = EntryValue.readFromBuffer(buffer);
			return new Entry(key, value);
		}

		public EntryKey getKey() {
			return key;
		}

		@Nullable
		public EntryValue getValue() {
			return value;
		}

	}

	public final static class EntryValue {
		private int count;
		private int priority;

		private EntryValue(int count, int priority) {
			// check valid
			if (!MaxCountValue.INSTANCE.isVaildObject(count)) {
				throw new IllegalArgumentException("count must between 1 and " + (Integer.MAX_VALUE - 1));
			}
			this.count = count;
			this.priority = priority;
		}

		public EntryValue copy() {
			return new EntryValue(count, priority);
		}

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}

		public int getPriority() {
			return priority;
		}

		public void setPriority(int priority) {
			this.priority = priority;
		}

		private static void writeToBuffer(EntryValue entry, FriendlyByteBuf buffer) {
			buffer.writeBoolean(entry != null); // check the EntryValue is exist
			if (entry != null) {
				buffer.writeInt(entry.count);
				buffer.writeInt(entry.priority);
			}
		}

		private static EntryValue readFromBuffer(FriendlyByteBuf buffer) {
			boolean isExist = buffer.readBoolean();
			if (isExist) {
				int count = buffer.readInt();
				int priority = buffer.readInt();
				return new EntryValue(count, priority);
			} else {
				return null;
			}
		}

		private void serial2Config(Config config) {
			config.set(KEY_MAX_COUNT, MaxCountValue.INSTANCE.toJsonValue(this.getCount()));
			config.set(KEY_PRIORITY, this.getPriority());
		}

		private static boolean isValid(Config config) {
			return config.contains(KEY_MAX_COUNT) && MaxCountValue.INSTANCE.isVaildJsonValue(config.get(KEY_MAX_COUNT));
		}

		private static EntryValue createObject(Config config) {
			Integer maxcount = MaxCountValue.INSTANCE.toObject(config.get(KEY_MAX_COUNT));
			int priority = config.getIntOrElse(KEY_PRIORITY, 0);
			return new EntryValue(maxcount, priority);
		}
	}

	public final static class EntryKey {
		private Type resourceType;
		private ResourceLocation resource;

		private EntryKey(Type resourceType, ResourceLocation resource) {
			this.resourceType = resourceType;
			this.resource = resource;
		}

		public static EntryKey of(Item item) {
			return new EntryKey(Type.ITEM, ForgeRegistries.ITEMS.getKey(item));
		}

		public static EntryKey of(TagKey<Item> tag) {
			return new EntryKey(Type.TAG, tag.location());
		}

		public Type resourceType() {
			return resourceType;
		}

		public ResourceLocation resource() {
			return resource;
		}

		private static void writeToBuffer(EntryKey entry, FriendlyByteBuf buffer) {
			buffer.writeUtf(entry.resourceType.name());
			buffer.writeUtf(entry.resource.toString());
		}

		private static EntryKey readFromBuffer(FriendlyByteBuf buffer) {
			Type resourceType = Type.valueOf(buffer.readUtf());
			ResourceLocation resource = new ResourceLocation(buffer.readUtf());
			return new EntryKey(resourceType, resource);
		}

		private void serial2Config(Config config) {
			String configKey = switch (this.resourceType()) {
			case ITEM -> KEY_ITEM_NAME;
			case TAG -> KEY_ITEM_TAG;
			};
			config.set(configKey, this.resource().toString());
		}

		private static EntryKey createObject(Config config) {
			String name = config.get(KEY_ITEM_NAME);
			if (name != null) {
				return new EntryKey(Type.ITEM, new ResourceLocation(name));
			}
			String tag = config.get(KEY_ITEM_TAG);
			if (tag != null) {
				return new EntryKey(Type.TAG, new ResourceLocation(tag));
			}
			return null;
		}

		private static boolean isValid(Config config) {
			return config.contains(KEY_ITEM_NAME) ^ config.contains(KEY_ITEM_TAG);
		}

		@Override
		public int hashCode() {
			return Objects.hash(resource, resourceType);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EntryKey other = (EntryKey) obj;
			return Objects.equals(resource, other.resource) && resourceType == other.resourceType;
		}
	}

	public enum Type {
		ITEM,
		TAG;
	}
}
