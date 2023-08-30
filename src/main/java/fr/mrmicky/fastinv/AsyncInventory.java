package fr.mrmicky.fastinv;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AsyncInventory {

    private final FastInv fastInv;
    private final Map<UUID, CompletableFuture<Void>> futureMap = new ConcurrentHashMap<>();

    public AsyncInventory(FastInv fastInv) {
        this.fastInv = fastInv;
    }

    private void registerFuture(CompletableFuture<?> future) {
        final UUID uuid;
        this.futureMap.put((uuid = UUID.randomUUID()), future.thenAccept(ignored -> futureMap.remove(uuid)));
    }

    private CompletableFuture<ItemStack> supplyItemStack(Supplier<ItemStack> supplier) {
        return CompletableFuture.supplyAsync(supplier).exceptionally(throwable -> {
            System.err.println("An exception occurred while supplying an itemstack: " + throwable.getMessage());
            return null;
        });
    }

    public CompletableFuture<Void> setItemAsync(int slot, Supplier<ItemStack> supplier, Consumer<InventoryClickEvent> handler) {
        final CompletableFuture<Void> future = supplyItemStack(supplier).thenAccept(itemStack -> this.fastInv.setItem(slot, itemStack, handler));
        registerFuture(future);
        return future;
    }

    public CompletableFuture<Void> setItemAsync(int slot, Supplier<ItemStack> supplier) {
        final CompletableFuture<Void> future = supplyItemStack(supplier).thenAccept(itemStack -> this.fastInv.setItem(slot, itemStack));
        registerFuture(future);
        return future;
    }

    public CompletableFuture<Void> setItemsAsync(int from, int to, Supplier<ItemStack> supplier, Consumer<InventoryClickEvent> handler) {
        final CompletableFuture<Void> future = supplyItemStack(supplier).thenAccept(itemStack -> fastInv.setItems(from, to, itemStack, handler));
        registerFuture(future);
        return future;
    }

    public void waitThenOpen(Player player) {
        if(futureMap.isEmpty()) {
            fastInv.open(player);
            return;
        }
        CompletableFuture.allOf(futureMap.values().toArray(new CompletableFuture[0])).thenAccept(ignored -> this.fastInv.open(player));
    }
}
