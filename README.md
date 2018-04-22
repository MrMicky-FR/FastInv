# FastInv
FastInv is a complete Bukkit API allowing you to easily create menus with clickable items all in one class and with useful features

Works with all Bukkit versions from 1.7.10 to 1.12.2.

You need Java 8

## How to use
Using FastInv is really easy, just add the [FastInv class](blob/master/src/main/java/fr/mrmicky/fastinv/FastInv.java) in your projet and add `FastInv.init(this);` in the `onEnable` of your plugin like this:
```java
@Override
public void onEnable() {
	FastInv.init(this);
}
```

Then you can create a new FastInv and add items,  this is just a quick exemple of inventory with animations11
```java
FastInv inv = new FastInv(54, "Custom Menu");
inv.addItem(22, new ItemStack(Material.NAME_TAG), e -> inv.addItem(new ItemStack(Material.OBSIDIAN)))
	.onUpdate(20, () -> inv.addItem(int1, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) r.nextInt(15))))
	.onUpdate(10, () -> inv.addItem(int2, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) r.nextInt(15))))
	.open((Player) sender);
```

If you want to prevent the players to close inventory just cancel the FastInvCloseEvent:
```java
inv.onClose(event -> event.setCancelled(true));
```

If you don't want to cancel the InventoryClickEvent when a player click just do this:
```java
inv.onClick(e -> e.setCancelled(false));
```

By default, when all players close the inventory, all tasks will be cancel, 
but if you want to use the same inventory multiples times, you just need to add this
```java
inv.setCancelTasksOnClose(false);
```
And when you want to cancel the tasks:
```java
inv.cancelTasks();
```

You can get the FastInv object from a Bukkit inventory, just verify that his holder is an instance of FastInv and cast it.
```java
if (bukkitInv.getHolder() instanceof FastInv) {
	FastInv fastInv = (FastInv) bukkitInv;
	// Your code
}
```

## Features
* The API is in a single small class (less than 500 lines with the doc)
* Custom inventorie (size, title and type)
* Items with custom ClickEvent
* Easy to use
* Update task with configurable delay
* Option to prevent player to close the inventory
* Supports multiple update tasks with configurable delay
* The Bukkit methods can still be use with a FastInv

## v2 changelog
* Replacing a listener per FastInv instance with a single listener
* With this, inventories no longer need to be destroyed, only tasks will be cancel
* Replace `setWillDestroy(destroy)` with `setCancelTasksOnClose(cancelTasksOnClose)`, and `destroy()` with `cancelTasks()`
* `getViewers()` was removed, use `getInventory().getViewers()` instead
* You can verify if a Bukkit inventory is a FastInv inventory by doing `bukkitInv.getHolder() instanceof FastInv` (and you can cast it to have the FastInv instance)