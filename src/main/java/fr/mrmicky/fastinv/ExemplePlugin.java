package fr.mrmicky.fastinv;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * An exemple plugin to show what FastInv can do
 * 
 * @author MrMicky
 */
public class ExemplePlugin extends JavaPlugin {

	private Random r = new Random();

	@Override
	public void onEnable() {
		FastInv.init(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
 			int[] int1 = new int[] { 0, 2, 4, 6, 8, 45, 47, 49, 51, 53 };
			int[] int2 = new int[] { 1, 3, 5, 7, 46, 48, 50, 52 };
			FastInv inv = new FastInv(54, "Custom Menu");
			
			int id = r.nextInt();
			
			inv.addItem(22, new ItemStack(Material.NAME_TAG), e -> inv.addItem(new ItemStack(Material.OBSIDIAN)))
				.onUpdate(20, () -> inv.addItem(int1, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) r.nextInt(15))))
				.onUpdate(10, () -> inv.addItem(int2, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) r.nextInt(15))))
				.onClick(e -> p.sendMessage("You clicked on slot " + e.getSlot()))
				.onClose(e -> getLogger().warning("Inventory close"))
				.onUpdate(50, () -> getLogger().info("Update Inv: " + id))
				.open(p);
		}
		return true;
	}
}
