package fr.mrmicky.fastinv.paginated;

import com.google.common.collect.Lists;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Lightweight and easy-to-use paginated inventory API for Bukkit plugins.
 * The project is on <a href="https://github.com/MrMicky-FR/FastInv">GitHub</a>.
 *
 * @author kubbidev
 * @version 1.0.1
 */
public abstract class PaginatedFastInv<T> extends FastInv {
    private int page = 1;

    // This remains true until after #redraw is called for the first time
    private boolean firstDraw = true;

    public PaginatedFastInv(int size) {
        super(size);
    }

    public PaginatedFastInv(int size, String title) {
        super(size, title);
    }

    public PaginatedFastInv(InventoryType type) {
        super(type);
    }

    public PaginatedFastInv(InventoryType type, String title) {
        super(type, title);
    }

    public PaginatedFastInv(Function<InventoryHolder, Inventory> inventoryFunction) {
        super(inventoryFunction);
    }

    /**
     * Gets a list containing all pages item, different from the actual page items.
     *
     * @return a {@link List} of {@link T} objects.
     */
    public abstract List<T> contents();

    /**
     * Retrieves a list of integers representing the slot indices
     * within the inventory where the content for the current page is displayed.
     *
     * @return a {@link List} of {@link Integer} objects, each representing an
     * inventory slot index used for displaying content on the current page.
     */
    public abstract List<Integer> contentSlots();

    public boolean isFirstDraw() {
        return this.firstDraw;
    }

    protected PaginatedItem getPrevPageItem() {
        return null;
    }

    protected PaginatedItem getNextPageItem() {
        return null;
    }

    @Override
    public void redraw(Player viewer) {
        List<Integer> slots = new ArrayList<>(contentSlots());
        List<List<T>> pages = Lists.partition(contents(), slots.size());

        PaginatedInfo info = new PaginatedInfo(this.page, pages.size());
        normalizePage(info.maxPages());

        if (!isFirstDraw()) {
            slots.forEach(this::removeItem);
        }

        List<T> currentPage = pages.isEmpty() ? new ArrayList<>() : pages.get(this.page - 1);
        for (T item : currentPage) {
            setItem(slots.remove(0), viewer, item);
        }

        drawPageItems(viewer, info);
        this.firstDraw = false;
    }

    public abstract void setItem(int slot, Player viewer, T item);

    private void normalizePage(int maxPages) {
        this.page = Math.max(1, Math.min(this.page, maxPages));
    }

    private void drawPageItems(Player viewer, PaginatedInfo info) {
        PaginatedItem prevPageItem = getPrevPageItem();
        PaginatedItem nextPageItem = getNextPageItem();

        // If one of them is null, the user is considered to be integrating his own mechanism
        if (prevPageItem == null || nextPageItem == null) {
            return;
        }
        removeItem(prevPageItem.slot());
        removeItem(nextPageItem.slot());
        if (this.page > 1) {
            setItem(prevPageItem.slot(), prevPageItem.item().apply(viewer, info), e -> {
                if (this.page > 1) {
                    this.page--;
                    redraw((Player) e.getWhoClicked());
                }
            });
        }
        if (this.page < info.maxPages()) {
            setItem(nextPageItem.slot(), nextPageItem.item().apply(viewer, info), e -> {
                if (this.page < info.maxPages()) {
                    this.page++;
                    redraw((Player) e.getWhoClicked());
                }
            });
        }

    }
}