package fr.mrmicky.fastinv.paginated;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiFunction;

public class PaginatedItem {
    private final BiFunction<Player, PaginatedInfo, ItemStack> item;
    private final int slot;

    public PaginatedItem(int slot, BiFunction<Player, PaginatedInfo, ItemStack> item) {
        this.slot = slot;
        this.item = item;
    }

    public BiFunction<Player, PaginatedInfo, ItemStack> item() {
        return this.item;
    }

    public int slot() {
        return this.slot;
    }
}