package net.jcraron.mc.onestack.config.value;

import java.util.Objects;

import javax.annotation.Nullable;

import com.electronwill.nightconfig.core.Config;

import net.jcraron.mc.onestack.config.value.MaxCountEntryHandle.MaxCountEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class MaxCountEntryHandle implements ValueHandle<MaxCountEntry, Config> {

	public static final String KEY_ITEM_NAME = "name";
	public static final String KEY_ITEM_TAG = "tag";
	public static final String KEY_MAX_COUNT = "maxCount";
	public static final String KEY_PRIORITY = "priority";
	public static final int DEFAULT_PRIORITY_ITEM = 1000;
	public static final int DEFAULT_PRIORITY_TAG = 0;
	public final static MaxCountEntryHandle INSTANCE = new MaxCountEntryHandle();

	@Override
	public boolean isVaildObject(MaxCountEntry javaObject) {
		return javaObject.getItemTag() != null;
	}

	@Override
	public MaxCountEntry defaultObject() {
		return null;
	}

	@Override
	public boolean isVaildJsonValue(Config jsonValue) {
		if (!ItemOrTag.isValid(jsonValue)) {
			return false;
		}
		if (!MaxCountValue.isValid(jsonValue)) {
			return false;
		}
		return true;
	}

	@Override
	public MaxCountEntry toObject(Config jsonValue) {
		if (!isVaildJsonValue(jsonValue)) {
			throw new IllegalArgumentException("invalid value: " + jsonValue.toString());
		}
		ItemOrTag key = ItemOrTag.createObject(jsonValue);
		MaxCountValue value = MaxCountValue.createObject(jsonValue);
		return new MaxCountEntry(key, value);
	}

	@Override
	public Config toJsonValue(MaxCountEntry maxCountEntry) {
		Config config = Config.inMemory();
		maxCountEntry.itemOrTag.serial2Config(config);
		maxCountEntry.maxCountValue.serial2Config(config);
		return config;
	}

	public final static class MaxCountEntry {
		private ItemOrTag itemOrTag;
		@Nullable
		private MaxCountValue maxCountValue;

		private MaxCountEntry(ItemOrTag key, MaxCountValue value) {
			this.itemOrTag = key;
			this.maxCountValue = value;
		}

		public static MaxCountEntry of(Item item, int maxcount, int priority) {
			ItemOrTag key = ItemOrTag.of(item);
			MaxCountValue value = new MaxCountValue(maxcount, priority);
			return new MaxCountEntry(key, value);
		}

		public static MaxCountEntry of(TagKey<Item> tag, int maxcount, int priority) {
			ItemOrTag key = ItemOrTag.of(tag);
			MaxCountValue value = new MaxCountValue(maxcount, priority);
			return new MaxCountEntry(key, value);
		}

		public static MaxCountEntry of(Item item) {
			return new MaxCountEntry(ItemOrTag.of(item), null);
		}

		public static MaxCountEntry of(TagKey<Item> tag) {
			return new MaxCountEntry(ItemOrTag.of(tag), null);
		}

		public static void writeToBuffer(MaxCountEntry maxCountEntry, FriendlyByteBuf buffer) {
			ItemOrTag.writeToBuffer(maxCountEntry.itemOrTag, buffer);
			MaxCountValue.writeToBuffer(maxCountEntry.maxCountValue, buffer);
		}

		public static MaxCountEntry readFromBuffer(FriendlyByteBuf buffer) {
			ItemOrTag key = ItemOrTag.readFromBuffer(buffer);
			MaxCountValue value = MaxCountValue.readFromBuffer(buffer);
			return new MaxCountEntry(key, value);
		}

		public ItemOrTag getItemTag() {
			return itemOrTag;
		}

		@Nullable
		public MaxCountValue getValue() {
			return maxCountValue;
		}

		@Override
		public String toString() {
			return "MaxCountEntry [itemTag=" + itemOrTag + ", maxCount=" + maxCountValue + "]";
		}

	}

	public final static class MaxCountValue {
		private int count;
		private int priority;

		private MaxCountValue(int count, int priority) {
			// check valid
			if (!CountNumberValue.INSTANCE.isVaildObject(count)) {
				throw new IllegalArgumentException("count must between 1 and " + CountNumberValue.JAVA_VALUE_MAX);
			}
			this.count = count;
			this.priority = priority;
		}

		public MaxCountValue copy() {
			return new MaxCountValue(count, priority);
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

		private static void writeToBuffer(MaxCountValue entry, FriendlyByteBuf buffer) {
			buffer.writeBoolean(entry != null); // check the MaxCount is exist
			if (entry != null) {
				buffer.writeInt(entry.count);
				buffer.writeInt(entry.priority);
			}
		}

		private static MaxCountValue readFromBuffer(FriendlyByteBuf buffer) {
			boolean isExist = buffer.readBoolean();
			if (isExist) {
				int count = buffer.readInt();
				int priority = buffer.readInt();
				return new MaxCountValue(count, priority);
			} else {
				return null;
			}
		}

		private void serial2Config(Config config) {
			config.set(KEY_MAX_COUNT, CountNumberValue.INSTANCE.toJsonValue(this.getCount()));
			config.set(KEY_PRIORITY, this.getPriority());
		}

		private static boolean isValid(Config config) {
			return config.contains(KEY_MAX_COUNT) && CountNumberValue.INSTANCE.isVaildJsonValue(config.get(KEY_MAX_COUNT));
		}

		private static MaxCountValue createObject(Config config) {
			Integer maxcount = CountNumberValue.INSTANCE.toObject(config.get(KEY_MAX_COUNT));
			int priority = config.getIntOrElse(KEY_PRIORITY, 0);
			return new MaxCountValue(maxcount, priority);
		}

		@Override
		public String toString() {
			return "MaxCountValue [count=" + count + ", priority=" + priority + "]";
		}
	}

	public final static class ItemOrTag {
		private Type resourceType;
		private ResourceLocation resource;

		private ItemOrTag(Type resourceType, ResourceLocation resource) {
			this.resourceType = resourceType;
			this.resource = resource;
		}

		public static ItemOrTag of(Item item) {
			return new ItemOrTag(Type.ITEM, ForgeRegistries.ITEMS.getKey(item));
		}

		public static ItemOrTag of(TagKey<Item> tag) {
			return new ItemOrTag(Type.TAG, tag.location());
		}

		public Type resourceType() {
			return resourceType;
		}

		public ResourceLocation resource() {
			return resource;
		}

		private static void writeToBuffer(ItemOrTag entry, FriendlyByteBuf buffer) {
			buffer.writeUtf(entry.resourceType.name());
			buffer.writeUtf(entry.resource.toString());
		}

		private static ItemOrTag readFromBuffer(FriendlyByteBuf buffer) {
			Type resourceType = Type.valueOf(buffer.readUtf());
			ResourceLocation resource = new ResourceLocation(buffer.readUtf());
			return new ItemOrTag(resourceType, resource);
		}

		private void serial2Config(Config config) {
			String configKey = switch (this.resourceType()) {
			case ITEM -> KEY_ITEM_NAME;
			case TAG -> KEY_ITEM_TAG;
			};
			config.set(configKey, this.resource().toString());
		}

		private static ItemOrTag createObject(Config config) {
			String name = config.get(KEY_ITEM_NAME);
			if (name != null) {
				return new ItemOrTag(Type.ITEM, new ResourceLocation(name));
			}
			String tag = config.get(KEY_ITEM_TAG);
			if (tag != null) {
				return new ItemOrTag(Type.TAG, new ResourceLocation(tag));
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
			ItemOrTag other = (ItemOrTag) obj;
			return Objects.equals(resource, other.resource) && resourceType == other.resourceType;
		}

		@Override
		public String toString() {
			return "ItemOrTag [resourceType=" + resourceType + ", resource=" + resource + "]";
		}
	}

	public enum Type {
		ITEM,
		TAG;
	}
}
