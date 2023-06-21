package fr.mrmicky.fastinv;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class FastInvScheme extends FastInv {

    private final LinkedList<String> maskList = new LinkedList<>();
    private final HashMap<Character, Pair<ItemStack, Consumer<InventoryClickEvent>>> schemeItems = new HashMap<>();

    public FastInvScheme(int size) {
        super(size);
    }

    public FastInvScheme(int size, String title) {
        super(size, title);
    }

    public FastInvScheme(InventoryType type) {
        super(type);
    }

    public FastInvScheme(InventoryType type, String title) {
        super(type, title);
    }

    public FastInvScheme(Function<InventoryHolder, Inventory> inventoryFunction) {
        super(inventoryFunction);
    }

    private static final class Pair<A, B> {

        public final A fst;
        public final B snd;

        public Pair(A fst, B snd) {
            this.fst = fst;
            this.snd = snd;
        }

        public String toString() {
            return "Pair[" + fst + "," + snd + "]";
        }
    }

    /**
     * Add a mask to the corresponding FastInv including all sort of characters.
     * For Example: "110101011"
     *
     * @param mask not nullable string of the mask to add.
     */
    public void addMask(String mask) {
        Objects.requireNonNull(mask);
        maskList.add(mask.length() > 9 ? mask.substring(0, 10) : mask);
    }

    /**
     * Add an array of mask to the corresponding FastInv including all sort of characters.
     * For Example: "111111111", "110101011", "111111111"
     *
     * @param masks not nullable array of string of the masks to add.
     */
    public void addMasks(String... masks) {
        Objects.requireNonNull(masks);
        Stream.of(masks).forEach(this::addMask);
    }

    /**
     * Remove the mask corresponding to the parameter mask.
     *
     * @param mask not nullable string of the mask to remove.
     */
    public void removeMask(String mask) {
        Objects.requireNonNull(mask);
        maskList.remove(mask.length() > 9 ? mask.substring(0, 10) : mask);
    }

    /**
     * Remove all the masks in the FastInv.
     *
     */
    public void removeMasks() {
        maskList.clear();
    }

    public void setSchemeItem(Character character, ItemStack item, Consumer<InventoryClickEvent> consumer) {
        Objects.requireNonNull(character);
        Objects.requireNonNull(item);
        schemeItems.put(Character.toLowerCase(character), new Pair<>(item, consumer));
    }

    public void removeSchemeItem(Character character) {
        Objects.requireNonNull(character);
        schemeItems.remove(character);
    }

    @Override
    public Inventory getInventory() {
        int slot = 0;
        for (String nextMask : maskList) {
            for (int i = 0; i <= 8; i++) {

                try {
                    final Character character = nextMask.charAt(i);
                    final Pair<ItemStack, Consumer<InventoryClickEvent>> pair = schemeItems.get(character);
                    if (pair != null) {
                        super.setItem(slot, pair.fst, pair.snd);
                    }
                } catch (IndexOutOfBoundsException ignored) {
                    // mean mask's length is greater than 9 characters. Shouldn't happen, can be ignored.
                }

                slot++;
            }
        }
        return super.getInventory();
    }
}
