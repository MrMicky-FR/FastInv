# FastInv
[![JitPack](https://jitpack.io/v/fr.mrmicky/FastInv.svg)](https://jitpack.io/#fr.mrmicky/FastInv)
[![Discord](https://img.shields.io/discord/390919659874156560.svg?colorB=5865f2&label=Discord&logo=discord&logoColor=white)](https://discord.gg/q9UwaBT)

Lightweight and easy-to-use inventory API for Bukkit plugins.

## Features
* Very small (less than 400 lines of code with the JavaDoc) and no dependencies
* Works with all Bukkit/Spigot/Paper versions from 1.7.10 to 1.20
* Simple [Adventure components support](#adventure-components-support)
* Supports custom inventories (size, title and type)
* Easy to use
* Option to prevent a player from closing the inventory
* The Bukkit inventory can still be directly used
* Paginated inventory for easy display of lists
* Keep your inventory animated at a predefined rate

## Installation

### Maven
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.3.0<</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <relocations>
                    <relocation>
                        <pattern>fr.mrmicky.fastinv</pattern>
                        <!-- Replace 'com.yourpackae' with the package of your plugin ! -->
                        <shadedPattern>com.yourpackage.fastinv</shadedPattern>
                    </relocation>
                </relocations>
            </configuration>
        </plugin>
    </plugins>
</build>

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>fr.mrmicky</groupId>
        <artifactId>FastInv</artifactId>
        <version>3.0.4</version>
    </dependency>
</dependencies>
```

### Gradle
```groovy
plugins {
    id 'com.github.johnrengelman.shadow' version '7.1.2'
}

repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'fr.mrmicky:FastInv:3.0.4'
}

shadowJar {
    // Replace 'com.yourpackage' with the package of your plugin 
    relocate 'fr.mrmicky.fastinv', 'com.yourpackage.fastinv'
}
```

### Manual

Simply copy `FastInv.java` and `FastInvManager.java` in your plugin.
You can also add `ItemBuilder.java` if you need.

## Usage

### Register FastInv
Before creating inventories, you just need to register your plugin by adding `FastInvManager.register(this);` in the `onEnable()` method of your plugin:
```java
@Override
public void onEnable() {
    FastInvManager.register(this);
}
```

### Creating an inventory class

Now you can create an inventory by make a class that extends `FastInv`, and add items in the constructor. 
You can also override `onClick`, `onClose` and `onOpen` if you need.

*If you want to create an animated inventory, you need to add elements inside the `redraw` override method and not in the constructor, so that they are updated.

Small example inventory:

```java
package fr.mrmicky.fastinv.test;

import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

public class ExampleInventory extends FastInv {

    private boolean preventClose = false;

    public ExampleInventory() {
        super(45, ChatColor.GOLD + "Example inventory");

        // Just add a random item
        setItem(22, new ItemStack(Material.IRON_SWORD), e -> e.getWhoClicked().sendMessage("You clicked on the sword"));

        // Add some blocks to the borders
        setItems(getBorders(), new ItemBuilder(Material.LAPIS_BLOCK).name(" ").build());

        // Add a simple item to prevent closing the inventory
        setItem(34, new ItemBuilder(Material.BARRIER).name(ChatColor.RED + "Prevent close").build(), e -> {
            this.preventClose = !this.preventClose;
        });

        // Prevent from closing when preventClose is to true
        setCloseFilter(p -> this.preventClose);
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        event.getPlayer().sendMessage(ChatColor.GOLD + "You opened the inventory");
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        event.getPlayer().sendMessage(ChatColor.GOLD + "You closed the inventory");
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        // do something
    }
}
```

Now you can open the inventory:
```java
new ExampleInventory().open(player);
```

### Creating a 'compact' inventory
If you prefer, you can create a 'compact' inventory that doesn't require an entire class, but this is not recommended.

```java
FastInv inv = new FastInv(InventoryType.DISPENSER, "Example compact inventory");

inv.setItem(4, new ItemStack(Material.NAME_TAG), e -> e.getWhoClicked().sendMessage("You clicked on the name tag"));
inv.addClickHandler(e -> player.sendMessage("You clicked on slot " + e.getSlot()));
inv.addCloseHandler(e -> player.sendMessage(ChatColor.YELLOW + "Inventory closed"));
inv.open(player);
```

### Creating a 'paginated' inventory
If you wish to display a list of content across multiple pages, you can create a paginated inventory. Here's how:

```java
public class PaginatedExample<T> extends PaginatedFastInv<T> {
    private static final Integer[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public PaginatedExample(Supplier<List<T>> contents) {
        super(contents /* <-- your contents supplier */, 9 * 6, "Example paginated inventory");
    }

    @Override
    public List<Integer> contentSlots() {
        return Arrays.asList(SLOTS);
    }

    @Override
    public PaginatedItem getPrevPageItem() {
        return new PaginatedItem(48, (player, page) -> new ItemBuilder(Material.ARROW)
                .name("<--")
                .addLore("Switch to the previous page.")
                .addLore("Currently viewing page " + page.current() + "/" + page.maxPages())
                .build());
    }

    @Override
    public PaginatedItem getNextPageItem() {
        return new PaginatedItem(50, (player, page) -> new ItemBuilder(Material.ARROW)
                .name("-->")
                .addLore("Switch to the following page.")
                .addLore("Currently viewing page " + page.current() + "/" + page.maxPages())
                .build());
    }

    @Override
    public void setItem(int slot, Player viewer, T entity) {
        setItem(slot, new ItemBuilder(Material.STONE).name(entity.toString()).build(), e -> {
            // action executed when the player clicks on the element
        });
    }
}
```

### Animate your inventory
Here's how you can schedule the animation at a fixed rate of 20 ticks (1 second) between each draw:

```java
FastInv inv = new FastInv(InventoryType.DISPENSER, "Example compact inventory");
        
inv.animate(plugin, 20L, 20L);
inv.open(player);
```

### Get the FastInv instance
You can easily get the FastInv instance from a Bukkit inventory with the holder:
```java
if (inventory.getHolder() instanceof FastInv) {
    FastInv fastInv = (FastInv) inventory.getHolder();
}
```

### Adventure components support

FastInv supports [Adventure components](https://github.com/KyoriPowered/adventure) for inventory titles on [PaperMC](https://papermc.io/) servers:
```java
Component title = Component.text("Hello World");

FastInv inv = new FastInv(owner -> Bukkit.createInventory(owner, 27, title));
```
