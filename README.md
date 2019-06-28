# FastInv
[![JitPack](https://jitpack.io/v/fr.mrmicky/FastInv.svg)](https://jitpack.io/#fr.mrmicky/FastInv)
[![Discord](https://img.shields.io/discord/390919659874156560.svg?colorB=7289da&label=discord&logo=discord&logoColor=white)](https://discord.gg/q9UwaBT)

Small and easy Bukkit inventory API with 1.7 to 1.14 support !

## Features
* Really small (only 2 class with less than 400 lines with the JavaDoc).
* Works with all Bukkit versions from 1.7.10 to 1.14 !
* Support custom inventories (size, title and type).
* Easy to use.
* Option to prevent a player from closing the inventory
* You can still access the Bukkit inventory without any problem

## How to use

### Add FastInv in your plugin
**Maven**
```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.2.1</version>
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
                            <!-- Replace with the package of your plugin ! -->
                            <shadedPattern>com.yourpackage.fastinv</shadedPattern>
                        </relocation>
                    </relocations>
                </configuration>
            </plugin>
        </plugins>
    </build>
```
```xml
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
```
```xml
    <dependencies>
        <dependency>
            <groupId>fr.mrmicky</groupId>
            <artifactId>FastInv</artifactId>
            <version>3.0</version>
            <scope>compile</scope>
        </dependency>
    </dependencies>
```

**Manual**

Just copy `FastInv.java` and `FastInvManager.java` in your plugin. You can also add `ItemBuilder.java`

### Use FastInv

#### Register FastInv
Before creating inventories, you just need to register your plugin by adding `FastInvManager.register(this);` in your `onEnable` method of your plugin like this:
```java
@Override
public void onEnable() {
    FastInvManager.register(this);
}
```

#### Create an inventory class
Now you can create an inventory by make a class that extends `FastInv`, and add items in the constructor. 
You can also override `onClick`, `onClose` and `onOpen` if you need

Just small example:
```java
public class ExampleInventory extends FastInv {

    private AtomicBoolean preventClose = new AtomicBoolean(false);

    public ExampleInventory() {
        super(45, ChatColor.GOLD + "Example inventory");

        // Just add a random item
        setItem(22, new ItemStack(Material.IRON_SWORD), e -> e.getWhoClicked().sendMessage("You clicked on the sword"));

        // Add some blocks to the borders
        setItems(getBorders(), new ItemBuilder(Material.LAPIS_BLOCK).name(" ").build());

        // Add a simple item to prevent closing the inventory
        setItem(34, new ItemBuilder(Material.BARRIER).name(ChatColor.RED + "Prevent close").build(), e -> preventClose.set(!preventClose.get()));

        // Prevent from closing when preventClose is to true
        setCloseFilter(p -> preventClose.get());
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

And open the inventory
```java
    new ExampleInventory().open(player);
```

#### Create a 'compact' inventory

If you prefer you can create a 'compact' inventory that don't need a full class. But the first method should be use

```java
        FastInv inv = new FastInv(InventoryType.DISPENSER, "Example compact inventory");

        inv.setItem(4, new ItemStack(Material.NAME_TAG), e -> e.getWhoClicked().sendMessage("You clicked on the name tag"));
        inv.addClickHandler(e -> player.sendMessage("You clicked on slot " + e.getSlot()));
        inv.addCloseHandler(e -> player.sendMessage(ChatColor.YELLOW + "Inventory closed"));
        inv.open(player);
```

## Changelog

[Changelog](CHANGELOG.md)
