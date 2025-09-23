package com.github.iDimaBR.speedinv;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class InventoryScheme {

    private final List<String> masks = new ArrayList<>();
    private final Map<Character, ItemStack> items = new HashMap<>();
    private final Map<Character, Consumer<InventoryClickEvent>> handlers = new HashMap<>();
    private char paginationChar;

    /**
     * Add a mask to this scheme including all sorts of characters.
     * For example, "110101011"
     *
     * @param mask a 9-character mask
     * @return this scheme instance
     */
    public InventoryScheme mask(String mask) {
        Objects.requireNonNull(mask);
        this.masks.add(mask.length() > 9 ? mask.substring(0, 10) : mask);

        return this;
    }

    /**
     * Add multiple masks to this scheme, including all sorts of characters.
     * For example, "111111111", "110101011", "111111111"
     *
     * @param masks multiple 9-character masks
     * @return this scheme instance
     */
    public InventoryScheme masks(String... masks) {
        for (String mask : Objects.requireNonNull(masks)) {
            mask(mask);
        }
        return this;
    }

    /**
     * Bind character to the corresponding item in the inventory.
     *
     * @param character the associated character in the mask
     * @param item      the item to use for this character
     * @param handler   consumer for the item
     * @return this scheme instance
     */
    public InventoryScheme bindItem(char character, ItemStack item, Consumer<InventoryClickEvent> handler) {
        this.items.put(character, Objects.requireNonNull(item));

        if (handler != null) {
            this.handlers.put(character, handler);
        }
        return this;
    }

    /**
     * Bind character to the corresponding item in the inventory.
     *
     * @param character the associated character in the mask
     * @param item      the item to use for this character
     * @return this scheme instance
     */
    public InventoryScheme bindItem(char character, ItemStack item) {
        return this.bindItem(character, item, null);
    }

    /**
     * Bind character for pagination content.
     *
     * @param character the associated character in the mask
     * @return this scheme instance
     */
    public InventoryScheme bindPagination(char character) {
        this.paginationChar = character;
        return this;
    }

    /**
     * Unbind any item from this character.
     *
     * @param character the character to unbind
     * @return this scheme instance
     */
    public InventoryScheme unbindItem(char character) {
        this.items.remove(character);
        this.handlers.remove(character);
        return this;
    }

    /**
     * Apply the current inventory scheme to the FastInv instance.
     *
     * @param inv the FastInv instance to apply this scheme to
     */
    public void apply(FastInv inv) {
        List<Integer> paginationSlots = new ArrayList<>();

        for (int line = 0; line < this.masks.size(); line++) {
            String mask = this.masks.get(line);

            for (int slot = 0; slot < mask.length(); slot++) {
                char c = mask.charAt(slot);

                if (c == this.paginationChar) {
                    paginationSlots.add(9 * line + slot);
                    continue;
                }

                ItemStack item = this.items.get(c);
                Consumer<InventoryClickEvent> handler = this.handlers.get(c);

                if (item != null) {
                    inv.setItem(9 * line + slot, item, handler);
                }
            }
        }

        if (inv instanceof PaginatedFastInv && !paginationSlots.isEmpty()) {
            ((PaginatedFastInv) inv).setContentSlots(paginationSlots);
        }
    }
}
