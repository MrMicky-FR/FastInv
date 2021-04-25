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
package fr.mrmicky.fastinv;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manager for FastInv listeners.
 *
 * @author MrMicky
 */
public final class FastInvManager {

    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);

    private FastInvManager() {
        throw new UnsupportedOperationException();
    }

    /**
     * Register listeners for FastInv.
     *
     * @param plugin plugin to register
     * @throws NullPointerException if plugin is null
     * @throws IllegalStateException if FastInv is already registered
     */
    public static void register(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");

        if (REGISTERED.getAndSet(true)) {
            throw new IllegalStateException("FastInv is already registered");
        }

        Bukkit.getPluginManager().registerEvents(new InventoryListener(plugin), plugin);
    }

    /**
     * Close all open FastInv inventories.
     */
    public static void closeAll() {
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getOpenInventory().getTopInventory().getHolder() instanceof FastInv)
                .forEach(Player::closeInventory);
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

                boolean wasCancelled = e.isCancelled();
                e.setCancelled(true);

                inv.handleClick(e);

                // This prevents un-canceling the event if another plugin canceled it before
                if (!wasCancelled && !e.isCancelled()) {
                    e.setCancelled(false);
                }
            }
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
                closeAll();

                REGISTERED.set(false);
            }
        }
    }
}
