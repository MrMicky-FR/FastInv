package fr.mrmicky.fastinvexample;

import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

/**
 * An exemple plugin to show what FastInv can do
 *
 * @author MrMicky
 */
public class ExamplePlugin extends JavaPlugin {

    private final Random random = new Random();

    @Override
    public void onEnable() {
        FastInv.init(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            int[] color1 = new int[]{0, 2, 4, 6, 8, 45, 47, 49, 51, 53};
            int[] color2 = new int[]{1, 3, 5, 7, 46, 48, 50, 52};
            FastInv inv = new FastInv(54, "Custom Menu");

            int id = random.nextInt();

            inv.addItem(22, new ItemStack(Material.NAME_TAG), itemClickEvent -> inv.addItem(new ItemBuilder(Material.OBSIDIAN).name("Hello!").build()))
                    .onUpdate(20, () -> inv.addItem(color1, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) random.nextInt(15))))
                    .onUpdate(10, () -> inv.addItem(color2, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) random.nextInt(15))))
                    .onClick(invClickEvent -> {
                        player.sendMessage("You clicked on slot " + invClickEvent.getSlot());
                        invClickEvent.setCancelled(true);
                    })
                    .onClose(e -> getLogger().warning("Inventory close"))
                    .onUpdate(50, () -> getLogger().info("Update Inv: " + id))
                    .open(player);
        }
        return true;
    }
}
