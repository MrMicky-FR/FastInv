# FastInv changelog

## Version 3.0 (21/02/2019)
* Removed the update/task system. If you need one just use the `onOpen` and `onClose` to create a similar one like this
```
    BukkitTask task = getServer().getScheduler().runTaskTimer(this, () -> {}, 10, 10);
    inv.addCloseHandler(e -> task.cancel());
```
* Removed the 'builder' style: methods like `addItem` now return `void` (more lines will keep your code cleaner and will not kill you)
* Split the FastInv class in 2 class (3 with the ItemBuilder)
* Remove the async support, _if_ you need it you can really easily do one yourself (this prevent the creation of `Runnable` everywhere)
* Renamed most of the methods to match Bukkit names
* Removed the FastInv events, now it just use the Bukkit events
* Add `removeItem(int slot)` and `removeItems(int... slots)`, it's cleaner than adding a `null` item
* Better `ItemBuilder`
* If you have any problem with this new version you can contact me on Discord (link in the [README](README.md))

## Version 2.1 (20/12/2018)
* Replace `FastInvClickListener` with `Consumer<FastInvClickEvent>` and `FastInvCloseListener` with `Consumer<FastInvCloseEvent>`

## Version 2.0 (22/04/2018)
* Replacing a listener per FastInv instance with a single listener.
* With this, inventories no longer need to be destroyed, only tasks will be cancel.
* Replace `setWillDestroy(destroy)` with `setCancelTasksOnClose(cancelTasksOnClose)`, and `destroy()` with `cancelTasks()`.
* `getViewers()` was removed, use `getInventory().getViewers()` instead.
* You can verify if a Bukkit inventory is a FastInv inventory by doing `bukkitInv.getHolder() instanceof FastInv` (and then you can cast it to have the FastInv instance).
