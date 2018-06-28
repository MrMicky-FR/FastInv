package fr.mrmicky.fastinvexample;

import fr.mrmicky.fastinv.FastInv;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * @author MrMicky
 */
public class ExempleFullClassInventory extends FastInv {

    private Random random = new Random();

    public ExempleFullClassInventory() {
        super(27, "Exemple with FastInv");

        addItem(0, 26, new ItemStack(Material.STAINED_GLASS_PANE), e -> e.getItem().setDurability((short) random.nextInt(16)));
    }
}
