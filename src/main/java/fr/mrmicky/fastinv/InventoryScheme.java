package fr.mrmicky.fastinv;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Consumer;

public class InventoryScheme {

    private final LinkedList<String> maskList = new LinkedList<>();
    private final HashMap<Character, Pair<ItemStack, Consumer<InventoryClickEvent>>> schemeItems = new HashMap<>();

    private static final class Pair<A, B> {
        public final A fst;
        public final B snd;

        public Pair(A fst, B snd) {
            this.fst = fst;
            this.snd = snd;
        }
    }

    /**
     * Add a mask to the corresponding FastInv including all sort of characters.
     * For Example: "110101011"
     *
     * @param mask not nullable string of the mask to add.
     */
    public InventoryScheme mask(String mask) {
        Objects.requireNonNull(mask);
        maskList.add(mask.length() > 9 ? mask.substring(0, 10) : mask);
        return this;
    }

    /**
     * Bind character to the corresponding itemstack in the inventory.
     * @param character not nullable character. Example: '4'
     * @param item not nullable itemstack to be binded with the character
     * @param consumer nullable consumer for the item
     */
    public InventoryScheme bindItem(Character character, ItemStack item, Consumer<InventoryClickEvent> consumer) {
        Objects.requireNonNull(character);
        Objects.requireNonNull(item);
        schemeItems.put(character, new Pair<>(item, consumer));
        return this;
    }

    /**
     * Unbind any item from this character.
     * @param character not nullable character
     */
    public InventoryScheme unbindItem(Character character) {
        Objects.requireNonNull(character);
        schemeItems.remove(character);
        return this;
    }

    /**
     * Apply the current inventory scheme to the FastInv object.
     * @param fastInv not nullable fastinv that will be applied this scheme.
     */
    public void apply(FastInv fastInv) {
        Objects.requireNonNull(fastInv);
        int slot = 0;
        for(String mask : maskList) {
            for(int i = 0; i <= 8; i++) {
                try {
                    final Character character = mask.charAt(i);
                    final Pair<ItemStack, Consumer<InventoryClickEvent>> pair = schemeItems.get(character);
                    if(pair != null) {
                        fastInv.setItem(slot, pair.fst, pair.snd);
                    }
                } catch (IndexOutOfBoundsException ignored) {
                    // mean mask's length is greater than 9 characters. Shouldn't happen, can be ignored.
                }

                slot++;
            }
        }
    }
}
