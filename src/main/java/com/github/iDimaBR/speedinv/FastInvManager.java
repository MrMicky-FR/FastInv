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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manager for FastInv listeners.
 *
 * @author MrMicky
 */
public final class FastInvManager {

    private static final Map<Class<? extends FastInv>, FastInv> INVENTORIES = new HashMap<>();
    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);
    private static final Set<FastInv> UPDATABLE_INVENTORIES = Collections.newSetFromMap(new WeakHashMap<>());

    private FastInvManager() {
        throw new UnsupportedOperationException();
    }

    /**
     * Register a FastInv menu to be opened later using {@link #open(Class, Player)}.
     * If the menu has an auto-update interval greater than 0, it will be automatically
     * added to the update list.
     *
     * @param menu menu to register
     * @param <T>  type of the menu
     */
    public static <T extends FastInv> void registerMenu(T menu) {
        INVENTORIES.put(menu.getClass(), menu);
        if (menu.getAutoUpdateInterval() > 0) {
            FastInvManager.enableUpdate(menu);
        }
    }

    /**
     * Open a previously registered FastInv menu for a player.
     *
     * @param clazz  class of the menu
     * @param player player to open the menu for
     * @param <T>    type of the menu
     */
    public static <T extends FastInv> void open(Class<T> clazz, Player player) {
        FastInv menu = INVENTORIES.get(clazz);
        if (menu != null) {
            menu.open(player);
        }
    }

    /**
     * Register listeners for FastInv.
     *
     * @param plugin plugin to register
     * @throws NullPointerException  if plugin is null
     * @throws IllegalStateException if FastInv is already registered
     */
    public static void register(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");

        if (REGISTERED.getAndSet(true)) {
            throw new IllegalStateException("FastInv is already registered");
        }

        Bukkit.getPluginManager().registerEvents(new InventoryListener(plugin), plugin);
        startUpdater(plugin);
    }

    private static void startUpdater(Plugin plugin) {
        final BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskTimerAsynchronously(plugin, new Runnable() {
            private long currentTick = 0;
            @Override
            public void run() {
                currentTick++;
                for (FastInv inv : UPDATABLE_INVENTORIES) {
                    long interval = inv.getAutoUpdateInterval();
                    if (interval > 0 && currentTick - inv.getLastUpdateTick() >= interval) {
                        if (!inv.getInventory().getViewers().isEmpty()) {
                            scheduler.runTask(plugin, () -> {
                                inv.refreshDynamicItems();
                                inv.setLastUpdateTick(currentTick);
                            });
                        }
                    }
                }
            }
        }, 1L, 1L);
    }

    public static void enableUpdate(FastInv inv) {
        UPDATABLE_INVENTORIES.add(inv);
    }

    public static void disableUpdate(FastInv inv) {
        UPDATABLE_INVENTORIES.remove(inv);
    }

    public static final class InventoryListener implements Listener {

        private final Plugin plugin;

        public InventoryListener(Plugin plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if (e.getInventory().getHolder() instanceof FastInv && e.getClickedInventory() != null) {
                FastInv inv = (FastInv) e.getInventory().getHolder();

                if (e.getClickedInventory() == e.getInventory()) {
                    int slot = e.getRawSlot();
                    boolean allowed = inv.isClickAllowed(slot) || e.isCancelled();
                    if (!allowed) {
                        e.setCancelled(true);
                    }
                    inv.handleClick(e);
                }

                if(!inv.isAllowInventoryPlayer()){
                    if (e.getClickedInventory() == e.getWhoClicked().getInventory()) {
                        e.setCancelled(true);
                    }
                }
            }
        }

        @EventHandler
        public void onInventoryDrag(InventoryDragEvent e) {
            if (!(e.getInventory().getHolder() instanceof FastInv)) return;

            FastInv inv = (FastInv) e.getInventory().getHolder();

            boolean cancel = false;
            for (int slot : e.getRawSlots()) {
                if (slot < e.getInventory().getSize()) {
                    if (!inv.isClickAllowed(slot)) cancel = true;
                } else if (!inv.isAllowInventoryPlayer()) {
                    cancel = true;
                }
            }

            if (cancel) e.setCancelled(true);
        }

        @EventHandler
        public void onInventoryOpen(InventoryOpenEvent e) {
            if (e.getInventory().getHolder() instanceof FastInv) {
                FastInv inv = (FastInv) e.getInventory().getHolder();

                inv.handleOpen(e);
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent e) {
            if (e.getInventory().getHolder() instanceof FastInv) {
                FastInv inv = (FastInv) e.getInventory().getHolder();

                if (inv.handleClose(e)) {
                    Bukkit.getScheduler().runTask(this.plugin, () -> inv.open((Player) e.getPlayer()));
                }
            }
        }

        @EventHandler
        public void onPluginDisable(PluginDisableEvent e) {
            if (e.getPlugin() == this.plugin) {
                REGISTERED.set(false);
            }
        }
    }
}
