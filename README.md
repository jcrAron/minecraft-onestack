One Stack
===

### NEW TAG
新增物品標籤 forge:stackable<br/>
這個標籤會自動添加到原始堆疊上限為1的物品上

### Command
你可以用指令設定每種物品的堆疊上限 (1~2147483646)<br/>
priority的數字越大，優先度越高。如果沒有填入則根據item/tag來決定優先度，item預設為1000，tag預設為0
```
/maxcount set <item|tag> <count|max|default> [priority]
```
```
/maxcount unset <item|tag>
/maxcount set minecraft:copper_ingot 999
/maxcount set minecraft:iron_ingot default
/maxcount set #forge:stackable max 
/maxcount unset minecraft:iron_ingot
```

### Config
你也可以在配置檔案中設定堆疊上限 (1~2147483646)<br/>
設置檔案在存檔目錄底下，可以在遊戲途中修改配置
```
saves\<your saved>\serverconfig\onestack-server.toml
```

```
[[items]]
	tag = "forge:stackable"
	priority = 0
	maxCount = "default"
[[items]]
	name = "minecraft:mushroom_stew"
	priority = 1000
	maxCount = "max"
[[items]]
	name = "minecraft:iron_ingot"
	priority = 1000
	maxCount = 12345
```

### Install
Server和Client都要安裝

### API
查看[github](https://github.com/jcrAron/minecraft-onestack/tree/forge-1.20.1/src/main/java/net/jcraron/mc/onestack/api)
