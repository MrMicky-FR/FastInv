# FastInv
FastInv is a complete Bukkit API allowing you to easily create menus with clickable items all in one class and with useful features.

Works with all Bukkit versions from 1.7.10 to 1.12.2.

**Java 8 is a prerequisite for FastInv!**

FastInv also come with a optional [ItemBuilder](src/main/java/fr/mrmicky/fastinv/ItemBuilder.java) (only for 1.8+) so you can quickly create ItemStack.

You can find the JavaDoc [here](https://mrmicky.fr/fastinv/)

## How to use
Using FastInv is really easy. Start with adding the [FastInv](src/main/java/fr/mrmicky/fastinv/FastInv.java) class to your project. Then add `FastInv.init(this);` in your `onEnable` method of your plugin like this:
```java
@Override
public void onEnable() {
	FastInv.init(this);
}
```

Now you can create new FastInv instances and add items. Below you can see a quick example of an animated inventory:
```java
Random random = new Random();
FastInv inv = new FastInv(54, "Custom Menu");
inv.addItem(22, new ItemStack(Material.NAME_TAG), event -> inv.addItem(new ItemStack(Material.OBSIDIAN)))
	.onUpdate(20, () -> inv.addItem(int1, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) random.nextInt(15))))
	.onUpdate(10, () -> inv.addItem(int2, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) random.nextInt(15))))
	.open((Player) sender);
```

Or if you prefer you can make a class that extends `FastInv` and do all operation in this class like [this example](src/main/java/fr/mrmicky/fastinvexample/ExempleFullClassInventory.java)

If you want to prevent the players to close the inventory, cancel the FastInvCloseEvent:
```java
inv.onClose(event -> event.setCancelled(true));
```

If you do not want to cancel the InventoryClickEvent when a player clicks, use:
```java
inv.onClick(event -> event.setCancelled(false));
```

By default, when all players close the inventory, all tasks will be cancel. Though, if you
want to use the same inventory multiples times, you can disable this feature as follow:
```java
inv.setCancelTasksOnClose(false);
```
Do not forget to cancel the tasks when you are done with the inventory!
```java
inv.cancelTasks();
```

You can get the FastInv object from a Bukkit inventory, just verify that its holder is an instance of FastInv and then cast it.
```java
if (bukkitInv.getHolder() instanceof FastInv) {
	FastInv fastInv = (FastInv) bukkitInv.getHolder();
	// Your code
}
```

## Features
* The API is in a single small class (less than 500 lines with the doc).
* Custom inventory (size, title and type).
* Items with custom ClickEvent.
* Easy to use.
* Update task with configurable delay.
* Option to prevent player to close the inventory.
* Supports multiple update tasks with configurable delay.
* The Bukkit methods can still be use with a FastInv.
* All methods are thread-safe.

## Version 2 Changelog
* Replacing a listener per FastInv instance with a single listener.
* With this, inventories no longer need to be destroyed, only tasks will be cancel.
* Replace `setWillDestroy(destroy)` with `setCancelTasksOnClose(cancelTasksOnClose)`, and `destroy()` with `cancelTasks()`.
* `getViewers()` was removed, use `getInventory().getViewers()` instead.
* You can verify if a Bukkit inventory is a FastInv inventory by doing `bukkitInv.getHolder() instanceof FastInv` (and you can cast it to have the FastInv instance).
