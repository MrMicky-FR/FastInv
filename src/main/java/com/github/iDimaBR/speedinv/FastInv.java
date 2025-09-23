/*
 * This file is part of FastInv, licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 MrMicky
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.iDimaBR.speedinv;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class FastInv implements InventoryHolder {

    private final Map<Integer, Consumer<InventoryClickEvent>> itemHandlers = new HashMap<>();
    private final Map<Integer, Supplier<ItemStack>> dynamicItems = new HashMap<>();
    private final List<Consumer<InventoryOpenEvent>> openHandlers = new ArrayList<>();
    private final List<Consumer<InventoryCloseEvent>> closeHandlers = new ArrayList<>();
    private final List<Consumer<InventoryClickEvent>> clickHandlers = new ArrayList<>();
    private final List<Consumer<InventoryDragEvent>> dragHandlers = new ArrayList<>();
    private final List<Integer> allowedClickSlots = new ArrayList<>();
    private boolean allowInventoryPlayer = false;

    private final Inventory inventory;

    private Predicate<Player> closeFilter;

    private long autoUpdateInterval = 0L;
    private boolean autoUpdateRegistered = false;
    private long lastUpdateTick = 0;

    /**
     * Create a new FastInv with a custom size.
     *
     * @param size a multiple of 9 as the size of the inventory
     * @see Bukkit#createInventory(InventoryHolder, int)
     */
    public FastInv(int size) {
        this(owner -> Bukkit.createInventory(owner, size));
    }

    /**
     * Create a new FastInv with a custom size and title.
     *
     * @param size  a multiple of 9 as the size of the inventory
     * @param title the title (name) of the inventory
     * @see Bukkit#createInventory(InventoryHolder, int, String)
     */
    public FastInv(int size, String title) {
        this(owner -> Bukkit.createInventory(owner, size, title));
    }

    /**
     * Create a new FastInv with a custom type.
     *
     * @param type the type of the inventory
     * @see Bukkit#createInventory(InventoryHolder, InventoryType)
     */
    public FastInv(InventoryType type) {
        this(owner -> Bukkit.createInventory(owner, type));
    }

    /**
     * Create a new FastInv with a custom type and title.
     *
     * @param type  the type of the inventory
     * @param title the title of the inventory
     * @see Bukkit#createInventory(InventoryHolder, InventoryType, String)
     */
    public FastInv(InventoryType type, String title) {
        this(owner -> Bukkit.createInventory(owner, type, title));
    }

    public FastInv(Function<FastInv, Inventory> inventoryFunction) {
        Objects.requireNonNull(inventoryFunction, "inventoryFunction");
        Inventory inv = inventoryFunction.apply(this);

        if (inv.getHolder() != this) {
            throw new IllegalStateException("Inventory holder is not FastInv, found: " + inv.getHolder());
        }

        this.inventory = inv;
        FastInvManager.registerMenu(this);
    }

    /**
     * Called when the inventory is opened.
     *
     * @param event the InventoryOpenEvent that triggered this method
     */
    protected void onOpen(InventoryOpenEvent event) {
    }

    /**
     * Called when the inventory is clicked.
     *
     * @param event the InventoryClickEvent that triggered this method
     */
    protected void onClick(InventoryClickEvent event) {
    }

    /**
     * Called when the player drags an item in their cursor across the inventory.
     *
     * @param event the InventoryDragEvent that triggered this method
     */
    protected void onDrag(InventoryDragEvent event) {
    }

    /**
     * Called when the inventory is closed.
     *
     * @param event the InventoryCloseEvent that triggered this method
     */
    protected void onClose(InventoryCloseEvent event) {
    }

    /**
     * Add an {@link ItemStack} to the inventory on the first empty slot, with no click handler.
     *
     * @param item the item to add
     */
    public void addItem(ItemStack item) {
        addItem(item, null);
    }

    /**
     * Add a dynamic {@link ItemStack} supplier to the inventory on a specific slot,
     * with no click handler.
     *
     * @param slot     the slot where to add the item
     * @param supplier supplier that provides the item dynamically
     */
    public void setDynamicItem(int slot, Supplier<ItemStack> supplier) {
        setDynamicItem(slot, supplier, null);
    }

    /**
     * Add a dynamic {@link ItemStack} supplier to the inventory on a specific slot,
     * with a click handler.
     *
     * @param slot     the slot where to add the item
     * @param supplier supplier that provides the item dynamically
     * @param handler  the click handler associated with this item
     */
    public void setDynamicItem(int slot, Supplier<ItemStack> supplier, Consumer<InventoryClickEvent> handler) {
        Objects.requireNonNull(supplier, "supplier");

        this.dynamicItems.put(slot, supplier);
        this.inventory.setItem(slot, supplier.get());

        if (handler != null) {
            this.itemHandlers.put(slot, handler);
        } else {
            this.itemHandlers.remove(slot);
        }
    }

    /**
     * Refresh only the dynamic items (re-fetch supplier values).
     */
    public void refreshDynamicItems() {
        for (Map.Entry<Integer, Supplier<ItemStack>> e : dynamicItems.entrySet()) {
            this.inventory.setItem(e.getKey(), e.getValue().get());
        }
    }

    /**
     * Remove a dynamic item from the inventory.
     */
    public void removeDynamicItem(int slot) {
        this.dynamicItems.remove(slot);
        this.inventory.clear(slot);
        this.itemHandlers.remove(slot);
    }

    /**
     * Add an {@link ItemStack} to the inventory on the first empty slot with a click handler.
     *
     * @param item    the item to add.
     * @param handler the click handler associated with this item
     */
    public void addItem(ItemStack item, Consumer<InventoryClickEvent> handler) {
        int slot = this.inventory.firstEmpty();
        if (slot >= 0) {
            setItem(slot, item, handler);
        }
    }

    /**
     * Add an {@link ItemStack} to the inventory on a specific slot, with no click handler.
     *
     * @param slot The slot where to add the item.
     * @param item The item to add.
     */
    public void setItem(int slot, ItemStack item) {
        setItem(slot, item, null);
    }

    /**
     * Add an {@link ItemStack} to the inventory on specific slot with a click handler.
     *
     * @param slot    the slot where to add the item
     * @param item    the item to add.
     * @param handler the click handler associated with this item
     */
    public void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> handler) {
        this.inventory.setItem(slot, item);

        if (handler != null) {
            this.itemHandlers.put(slot, handler);
        } else {
            this.itemHandlers.remove(slot);
        }
    }

    /**
     * Add an {@link ItemStack} to the inventory on a range of slots, with no click handler.
     *
     * @param slotFrom starting slot (inclusive) to put the item in
     * @param slotTo   ending slot (exclusive) to put the item in
     * @param item     The item to add.
     */
    public void setItems(int slotFrom, int slotTo, ItemStack item) {
        setItems(slotFrom, slotTo, item, null);
    }

    /**
     * Add an {@link ItemStack} to the inventory on a range of slots with a click handler.
     *
     * @param slotFrom starting slot (inclusive) to put the item in
     * @param slotTo   ending slot (exclusive) to put the item in
     * @param item     the item to add
     * @param handler  the click handler associated with these items
     */
    public void setItems(int slotFrom, int slotTo, ItemStack item, Consumer<InventoryClickEvent> handler) {
        for (int i = slotFrom; i < slotTo; i++) {
            setItem(i, item, handler);
        }
    }

    /**
     * Add an {@link ItemStack} to the inventory on multiple slots, with no click handler.
     *
     * @param slots the slots where to add the item
     * @param item  the item to add
     */
    public void setItems(int[] slots, ItemStack item) {
        setItems(slots, item, null);
    }

    /**
     * Add an {@link ItemStack} to the inventory on multiple slots with a click handler.
     *
     * @param slots   the slots where to add the item
     * @param item    the item to add
     * @param handler the click handler associated with this item
     */
    public void setItems(int[] slots, ItemStack item, Consumer<InventoryClickEvent> handler) {
        for (int slot : slots) {
            setItem(slot, item, handler);
        }
    }

    /**
     * Add an {@link ItemStack} to the inventory on multiple slots, with no click handler.
     *
     * @param slots the list of slots where to add the item
     * @param item  the item to add
     */
    public void setItems(Iterable<Integer> slots, ItemStack item) {
        setItems(slots, item, null);
    }

    /**
     * Add an {@link ItemStack} to the inventory on multiple slots with a click handler.
     *
     * @param slots   the list of slots where to add the item
     * @param item    the item to add
     * @param handler the click handler associated with this item
     */
    public void setItems(Iterable<Integer> slots, ItemStack item, Consumer<InventoryClickEvent> handler) {
        for (Integer slot : slots) {
            setItem(slot, item, handler);
        }
    }

    /**
     * Remove an {@link ItemStack} from the inventory.
     *
     * @param slot the slot from where to remove the item
     */
    public void removeItem(int slot) {
        this.inventory.clear(slot);
        this.itemHandlers.remove(slot);
    }

    /**
     * Remove multiples {@link ItemStack} from the inventory.
     *
     * @param slots the slots from where to remove the items
     */
    public void removeItems(int... slots) {
        for (int slot : slots) {
            removeItem(slot);
        }
    }

    /**
     * Define whether players are allowed to interact with their own inventory while this inventory is open.
     */
    public void setAllowInventoryPlayer(boolean value) {
        this.allowInventoryPlayer = value;
    }

    /**
     * Check whether players are allowed to interact with their own inventory while this inventory is open.
     */
    public boolean isAllowInventoryPlayer() {
        return allowInventoryPlayer;
    }

    /**
     * Fill all empty slots in the inventory with the given item and optional click handler.
     * If there are no empty slots, nothing happens.
     *
     * @param item    The item to put in empty slots
     * @param handler Optional click handler associated with the item
     */
    public void fillEmptySlots(ItemStack item, Consumer<InventoryClickEvent> handler) {
        for (int i = 0; i < this.inventory.getSize(); i++) {
            if (this.inventory.getItem(i) == null) {
                setItem(i, item, handler);
            }
        }
    }

    /**
     * Fill specific slots in the inventory with the given item, regardless of whether they are empty or not.
     */
    public void fillSlots(ItemStack item, int... slots) {
        for (int slot : slots) {
            setItem(slot, item);
        }
    }

    /**
     * Clear all items from the inventory and remove the click handlers.
     */
    public void clearItems() {
        this.inventory.clear();
        this.itemHandlers.clear();
    }

    /**
     * Add a close filter to prevent players from closing the inventory.
     * To prevent a player from closing the inventory the predicate should return {@code true}.
     *
     * @param closeFilter The close filter
     */
    public void setCloseFilter(Predicate<Player> closeFilter) {
        this.closeFilter = closeFilter;
    }

    /**
     * Set slots where clicks are allowed.
     */
    public void setAllowedClickSlots(int... slots) {
        this.allowedClickSlots.clear();
        for (int slot : slots) {
            this.allowedClickSlots.add(slot);
        }
    }

    /**
     * Add slots where clicks are allowed.
     */
    public void addAllowedClickSlots(int... slots) {
        for (int slot : slots) {
            if (!this.allowedClickSlots.contains(slot)) {
                this.allowedClickSlots.add(slot);
            }
        }
    }

    /**
     * Get the list of slots where clicks are allowed.
     *
     * @return the list of allowed click slots
     */
    public List<Integer> getAllowedClickSlots() {
        return Collections.unmodifiableList(this.allowedClickSlots);
    }

    /**
     * Clear the list of slots where clicks are allowed (all slots will be allowed).
     */
    public void clearAllowedClickSlots() {
        this.allowedClickSlots.clear();
    }

    /**
     * Check if a slot is in the list of allowed click slots.
     */
    public boolean isClickAllowed(int slot) {
        return this.allowedClickSlots.isEmpty() || this.allowedClickSlots.contains(slot);
    }

    /**
     * Set the inventory to auto-update every given interval (in ticks).
     * Set to 0 to disable auto-updating.
     * <p>
     * Note: Auto-updating only refreshes dynamic items, it does not call .
     *
     * @param interval the update interval in ticks
     */
    public void setAutoUpdate(long interval) {
        this.autoUpdateInterval = interval;
        if (!autoUpdateRegistered && interval != 0) {
            FastInvManager.enableUpdate(this);
            autoUpdateRegistered = true;
        }else{
            FastInvManager.disableUpdate(this);
            autoUpdateRegistered = false;
        }
    }

    /*
        * Get the last tick when the inventory was updated.
     */
    public long getLastUpdateTick() {
        return lastUpdateTick;
    }

    /*
        * Set the last tick when the inventory was updated.
     */
    public void setLastUpdateTick(long lastUpdateTick) {
        this.lastUpdateTick = lastUpdateTick;
    }

    /**
     * Get the auto-update interval in ticks.
     *
     * @return the auto-update interval
     */
    public long getAutoUpdateInterval() {
        return autoUpdateInterval;
    }

    /*
        * Check if the inventory is registered for auto-updating.
     */
    public boolean isAutoUpdateRegistered() {
        return autoUpdateRegistered;
    }

    /**
     * Add a handler that will be called when the inventory is opened.
     *
     * @param openHandler the handler to add
     */
    public void addOpenHandler(Consumer<InventoryOpenEvent> openHandler) {
        this.openHandlers.add(openHandler);
    }

    /**
     * Add a handler that will be called when the inventory is closed.
     *
     * @param closeHandler the handler to add
     */
    public void addCloseHandler(Consumer<InventoryCloseEvent> closeHandler) {
        this.closeHandlers.add(closeHandler);
    }

    /**
     * Add a handler that will be called when an item is clicked.
     *
     * @param clickHandler the handler to add
     */
    public void addClickHandler(Consumer<InventoryClickEvent> clickHandler) {
        this.clickHandlers.add(clickHandler);
    }

    /**
     * Add a handler that will be called when the player drags an item in their cursor across the inventory.
     *
     * @param dragHandler the handler to add
     */
    public void addDragHandler(Consumer<InventoryDragEvent> dragHandler) {
        this.dragHandlers.add(dragHandler);
    }

    /**
     * Open the inventory to the given player.
     *
     * @param player the player to open the inventory to
     */
    public void open(Player player) {
        Objects.requireNonNull(player, "player").openInventory(this.inventory);
    }

    /**
     * Get the borders of this inventory. If the inventory size is under 27, all slots are returned.
     *
     * @return the inventory borders slots
     */
    public int[] getBorders() {
        int size = this.inventory.getSize();
        return IntStream.range(0, size).filter(i -> size < 27 || i < 9
                || i % 9 == 0 || (i - 8) % 9 == 0 || i > size - 9).toArray();
    }

    /**
     * Get the edges of this inventory. If the inventory size is under 27, all slots are returned.
     */
    public int[] getEdges() {
        int size = this.inventory.getSize();
        return IntStream.range(0, size).filter(i -> size < 27 || i < 9
                || i % 9 == 0 || (i - 8) % 9 == 0 || (i > size - 10 && i < size - 8)).toArray();
    }

    /**
     * Get the center slots of this inventory. If the inventory size is under 27, an empty array is returned.
     */
    public int[] getCenter() {
        int size = this.inventory.getSize();
        return IntStream.range(0, size).filter(i -> i > 8 && i < size - 9
                && i % 9 != 0 && (i - 8) % 9 != 0).toArray();
    }

    /**
     * Get all slots of this inventory.
     */
    public int[] getAllSlots() {
        int size = this.inventory.getSize();
        return IntStream.range(0, size).toArray();
    }

    /**
     * Get the corners of this inventory.
     *
     * @return the inventory corners slots
     */
    public int[] getCorners() {
        int size = this.inventory.getSize();
        return IntStream.range(0, size).filter(i -> i < 2 || (i > 6 && i < 10)
                || i == 17 || i == size - 18
                || (i > size - 11 && i < size - 7) || i > size - 3).toArray();
    }

    /**
     * Get the underlying Bukkit inventory.
     *
     * @return the Bukkit inventory
     */
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    void handleOpen(InventoryOpenEvent e) {
        onOpen(e);

        this.openHandlers.forEach(c -> c.accept(e));
    }

    boolean handleClose(InventoryCloseEvent e) {
        onClose(e);

        this.closeHandlers.forEach(c -> c.accept(e));

        return this.closeFilter != null && this.closeFilter.test((Player) e.getPlayer());
    }

    void handleClick(InventoryClickEvent e) {
        onClick(e);

        this.clickHandlers.forEach(c -> c.accept(e));

        Consumer<InventoryClickEvent> clickConsumer = this.itemHandlers.get(e.getRawSlot());

        if (clickConsumer != null) {
            clickConsumer.accept(e);
        }
    }

    void handleDrag(InventoryDragEvent e) {
        onDrag(e);

        this.dragHandlers.forEach(c -> c.accept(e));
    }
}
