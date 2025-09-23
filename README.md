# SpeedInv (Better fork of FastInv)

Lightweight and easy-to-use inventory API for Bukkit plugins.

## Features

* Lightweight (around 500 lines of code with the Javadoc) and no dependencies
* Compatible with all Minecraft versions starting with 1.7.10
* [Adventure components support](#adventure-components-support)
* Supports custom inventories (size, title and type)
* Built-in support for [paginated inventories](#paginated-inventory)
* Easy to use
* Option to prevent a player from closing the inventory
* The Bukkit inventory can still be directly used
* Auto update for all inventories if you need
* Dynamic items in simple and paginated inventories

## Installation

### Maven

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.3.0</version>
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
                        <pattern>com.github.iDimaBR.speedinv</pattern>
                        <!-- Replace 'com.yourpackae' with the package of your plugin! -->
                        <shadedPattern>com.yourpackage.speedinv</shadedPattern>
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
        <groupId>com.github.iDimaBR</groupId>
        <artifactId>speedinv</artifactId>
        <version>-SNAPSHOT</version>
    </dependency>
</dependencies>
```

### Gradle

```groovy
plugins {
    id 'com.gradleup.shadow' version '8.3.0'
}

repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.iDimaBR:speedinv:-SNAPSHOT'
}

shadowJar {
    // Replace 'com.yourpackage' with the package of your plugin 
    relocate 'com.github.iDimaBR.speedinv', 'com.yourpackage.speedinv'
}
```

### Manual

Simply copy `FastInv.java` and `FastInvManager.java` in your plugin.
You can also add `ItemBuilder.java` if you need.

## Usage

### Register FastInv

Before creating inventories, register your plugin by adding `FastInvManager.register(this);` in the `onEnable()` method of your plugin:
```java
@Override
public void onEnable() {
    loadInventories();
}

private void loadInventories(){
    FastInvManager.register(this);
    new ExampleInventory(3 * 9, "Example Inventory");
}
```

### Creating an inventory class

Now you can create an inventory by creating a class that extends `FastInv`, and adding items in the constructor. 
You can also override `onClick`, `onClose` and `onOpen` if you need.

Basic example inventory:

```java
import com.github.iDimaBR.speedinv.FastInv;
import com.github.iDimaBR.speedinv.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

public class ExampleInventory extends FastInv {

    private boolean preventClose = false;

    public ExampleInventory(int size, String title) {
        super(size, title);

        // Add a random item
        setItem(22, new ItemStack(Material.IRON_SWORD), e -> e.getWhoClicked().sendMessage("You clicked on the sword"));

        // Add some blocks to the borders
        setItems(getBorders(), new ItemBuilder(Material.LAPIS_BLOCK).name(" ").build());

        // Add a simple item to prevent closing the inventory
        setItem(34, new ItemBuilder(Material.BARRIER).name(ChatColor.RED + "Prevent close").build(), e -> {
            this.preventClose = !this.preventClose;
        });

        // Allow slots interaction (like 36 slot of inventory)
        setItem(36, new ItemBuilder(Material.DIRT).name(ChatColor.RED + "Pick me").build());
        setAllowedClickSlots(36);

        // Allow interaction in player's inventory with menu open
        setAllowInventoryPlayer(true); // false disable it

        // Define dynamic items for auto update
        setDynamicItem(24, () -> {
            int ticks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
            int totalSeconds = ticks / 20;
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;
            int seconds = totalSeconds % 60;

            String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            return new ItemBuilder(Material.CLOCK)
                    .name("Online Time: " + timeFormatted)
                    .build();
        }, e -> p.sendMessage("You clicked on me!"));

        // Prevent from closing when preventClose is true
        setCloseFilter(p -> this.preventClose);

        // Define autoupdate for dynamic items to true
        setAutoUpdate(20L) // 1 second

        // Define autoupdate for dynamic items to false
        setAutoUpdate(0L); // no interval

        // You can also call this method for update all dynamic items manually
        refreshDynamicItems();
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

The inventory can be opened with the `open(Class<T>, Player)` method:
```java
FastInvManager.open(ExampleInventory.class, player);
```

### Paginated inventory

FastInv also supports paginated inventories, which can be created by using `PaginatedFastInv` instead of `FastInv`.

Content can be added to the inventory using `addContent` or `setContent`, and pagination items can be added with `previousPageItem` and `nextPageItem`.

You can also use `onPageChange` to execute code when the page changes, and the `#currentPage()`, `#lastPage()`, `#isFirstPage()` and `#isLastPage()` methods to get information about the current page.
```java
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ExamplePaginatedInventory extends PaginatedFastInv {

    private static final InventoryScheme SCHEME = new InventoryScheme()
            .mask(" 1111111 ")
            .mask(" 1111111 ")
            .bindPagination('1');

    public ExamplePaginatedInventory() {
        super(27, "Example paginated inventory");

        // Add pagination items to the inventory
        // These items are automatically updated when the page changes and displayed only if needed
        previousPageItem(20, p -> new ItemBuilder(Material.ARROW).name("Page " + p + "/" + lastPage()).build());
        nextPageItem(24, p -> new ItemBuilder(Material.ARROW).name("Page " + p + "/" + lastPage()).build());

        // Add some paginated content to the inventory
        for (int i = 1; i < 64; i++) {
            addContent(new ItemStack(Material.GOLDEN_APPLE, i),
                    e -> e.getWhoClicked().sendMessage("You clicked on paginated item"));
        }

        // The setContent method can also be used to set the index of a specific item in the paginated content
        setContent(42, new ItemStack(Material.APPLE, 42));

        // Non-paginated items can also still be added to the inventory if needed
        setItem(26, new ItemBuilder(Material.BARRIER).name("Close").build(),
                e -> e.getWhoClicked().closeInventory());

        // The pagination layout can also be customized with a mask instead of the default one
        SCHEME.apply(this);
    }

    @Override // Optional method to handle the page change event if needed
    protected void onPageChange(int page) {
        // Called after the page change
        setItem(18, new ItemBuilder(Material.PAPER).name("Current page " + page).build());
    }
}
```

Like a normal inventory, you can open the paginated inventory with `open(Class<T>, Player)`:
```java
FastInvManager.open(ExamplePaginatedInventory.class, player);
```

### Creating a 'compact' inventory

Instead of creating a new class for each inventory, a 'compact' inventory can be created directly:

```java
FastInv inv = new FastInv(InventoryType.DISPENSER, "Example compact inventory");

inv.setItem(4, new ItemStack(Material.NAME_TAG), e -> e.getWhoClicked().sendMessage("You clicked on the name tag"));
inv.addClickHandler(e -> player.sendMessage("You clicked on slot " + e.getSlot()));
inv.addCloseHandler(e -> player.sendMessage(ChatColor.YELLOW + "Inventory closed"));
inv.open(player);
```

In the same way, you can also create a 'compact' paginated inventory.

### Get the FastInv instance
You can get the FastInv instance from a Bukkit inventory with the holder:
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
