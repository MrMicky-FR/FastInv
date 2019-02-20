# FastInv
[![Discord](https://img.shields.io/discord/390919659874156560.svg?colorB=7289da&label=discord&logo=discord&logoColor=white)](https://discord.gg/q9UwaBT)

Small and easy inventory API with 1.7 to 1.13.2 support for Bukkit plugins !

FastInv also come with a [ItemBuilder](src/main/java/fr/mrmicky/fastinv/ItemBuilder.java) (only for 1.8+) so you can quickly create ItemStack.

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

## How to use
Using FastInv is really easy. Start with adding the [FastInv](src/main/java/fr/mrmicky/fastinv/FastInv.java) class to your project. Then add `FastInv.init(this);` in your `onEnable` method of your plugin like this:
```java
@Override
public void onEnable() {
	FastInv.init(this);
}
```

Now you can create a class that extends `FastInv` like this
```java
public class ExempleFullClassInventory extends FastInv {

    private Random random = new Random();

    public ExempleFullClassInventory() {
        super(27, "Exemple with FastInv");

        addItem(0, 26, new ItemStack(Material.STAINED_GLASS_PANE), e -> e.getItem().setDurability((short) random.nextInt(16)));
    }
}
```

And just call the open method for open it
```java
new ExempleFullClassInventory().open(player);
```

Or if you prefer you can just create a new FastInv like this
```java
Random random = new Random();
FastInv inv = new FastInv(54, "Custom Menu");
inv.addItem(22, new ItemStack(Material.NAME_TAG), event -> inv.addItem(new ItemStack(Material.OBSIDIAN)))
	.onUpdate(20, () -> inv.addItem(int1, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) random.nextInt(15))))
	.onUpdate(10, () -> inv.addItem(int2, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) random.nextInt(15))))
	.open((Player) sender);
```

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

## Changelog
### Version 2.1
* Replace `FastInvClickListener` with `Consumer<FastInvClickEvent>` and `FastInvCloseListener` with `Consumer<FastInvCloseEvent>`

### Version 2
* Replacing a listener per FastInv instance with a single listener.
* With this, inventories no longer need to be destroyed, only tasks will be cancel.
* Replace `setWillDestroy(destroy)` with `setCancelTasksOnClose(cancelTasksOnClose)`, and `destroy()` with `cancelTasks()`.
* `getViewers()` was removed, use `getInventory().getViewers()` instead.
* You can verify if a Bukkit inventory is a FastInv inventory by doing `bukkitInv.getHolder() instanceof FastInv` (and you can cast it to have the FastInv instance).
