package fr.mrmicky.fastinv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * A fast API to easely create advanced GUI
 *
 * @author MrMicky
 * @version 1.0
 */
public class FastInv {

	private static Plugin plugin;

	public static void init(Plugin plugin) {
		FastInv.plugin = plugin;
	}

	private Inventory inv;
	private Listener listener;
	private boolean destroy = false;
	private boolean willDestroy = true;
	private Set<Player> viewers = new HashSet<>();
	private Set<FastInvCloseListener> menuListeners = new HashSet<>();
	private Set<FastInvClickListener> clickListeners = new HashSet<>();
	private HashMap<Integer, FastInvClickListener> itemListeners = new HashMap<>();
	private Set<BukkitTask> tasks = new HashSet<>();

	/**
	 * Create a new FastInv with a custom size
	 *
	 * @param size
	 *            The size of the inventory
	 */
	public FastInv(int size) {
		this(Bukkit.createInventory(null, size));
	}

	/**
	 * Create a new FastInv with a custom size and title
	 *
	 * @param size
	 *            The size of the inventory
	 * @param title
	 *            The title of the inventory
	 */
	public FastInv(int size, String title) {
		this(Bukkit.createInventory(null, size, title));
	}

	/**
	 * Create a new FastInv with a custom type
	 *
	 * @param type
	 *            The type of the inventory
	 */
	public FastInv(InventoryType type) {
		this(Bukkit.createInventory(null, type));
	}

	/**
	 * Create a new FastInv with a custom type and title
	 *
	 * @param type
	 *            The type of the inventory
	 * @param title
	 *            The title of the inventory
	 */
	public FastInv(InventoryType type, String title) {
		this(Bukkit.createInventory(null, type, title));
	}

	private FastInv(Inventory inv) {
		checkDestroy();
		if (this.inv != null) {
			throw new IllegalStateException("Inventory is already init");
		}

		this.inv = inv;

		registerListener();
	}

	/**
	 * Add an item to the inventory
	 *
	 * @param item
	 *            The item to add
	 * @return the FastInv build
	 */
	public FastInv addItem(ItemStack item) {
		return addItem(item, null);
	}

	/**
	 * Add an item to the inventory with a {@link FastInvClickListener} to
	 * handle click
	 *
	 * @param item
	 *            The item to add
	 * @param listener
	 *            The {@link FastInvClickListener} for the item
	 * @return the FastInv build
	 */
	public FastInv addItem(ItemStack item, FastInvClickListener listener) {
		int slot = inv.firstEmpty();
		if (slot != -1) {
			return addItem(slot, item, listener);
		}
		return this;
	}

	/**
	 * Add an item to the inventory on a specific slot
	 *
	 * @param slot
	 *            The slot of the item
	 * @param item
	 *            The item to add
	 * @return the FastInv build
	 */
	public FastInv addItem(int slot, ItemStack item) {
		return addItem(slot, item, null);
	}

	/**
	 * Add an item to the inventory on specific slot with a
	 * {@link FastInvClickListener} to handle click
	 *
	 * @param slot
	 *            The slot of the item
	 * @param item
	 *            The item to add
	 * @param listener
	 *            The {@link FastInvClickListener} for the item
	 * @return the FastInv builder
	 */
	public FastInv addItem(int slot, ItemStack item, FastInvClickListener listener) {
		checkDestroy();

		inv.setItem(slot, item);
		if (listener != null) {
			itemListeners.put(slot, listener);
		} else {
			itemListeners.remove(slot);
		}
		return this;
	}

	/**
	 * Add an item to the inventory on a multiples slots
	 *
	 * @param slots
	 *            The slot of the item
	 * @param item
	 *            The item to add
	 * @return the FastInv build
	 */
	public FastInv addItem(int[] slots, ItemStack item) {
		return addItem(slots, item, null);
	}

	/**
	 * Add an item to the inventory on multiples slots with a
	 * {@link FastInvClickListener} to handle click
	 *
	 * @param slots
	 *            The slots of the item
	 * @param item
	 *            The item to add
	 * @param listener
	 *            The {@link FastInvClickListener} for the item
	 * @return the FastInv builder
	 */
	public FastInv addItem(int[] slots, ItemStack item, FastInvClickListener listener) {
		for (int slot : slots) {
			addItem(slot, item, listener);
		}
		return this;
	}

	/**
	 * Add a {@link FastInvCloseListener} to listen on inventory close
	 *
	 * @param listener
	 *            The {@link FastInvCloseListener} to add
	 * @return the FastInv builder
	 */
	public FastInv onClose(FastInvCloseListener listener) {
		checkDestroy();

		menuListeners.add(listener);
		return this;
	}

	/**
	 * Add a {@link FastInvClickListener} to listen on inventory click
	 *
	 * @param listener
	 *            The {@link FastInvClickListener} to add
	 * @return the FastInv builder
	 */
	public FastInv onClick(FastInvClickListener listener) {
		checkDestroy();

		clickListeners.add(listener);
		return this;
	}

	public FastInv onUpdate(long period, Runnable runnable) {
		return onUpdate(period, period, runnable);
	}

	public FastInv onUpdate(long delay, long period, Runnable runnable) {
		checkDestroy();

		tasks.add(Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, period));
		return this;
	}

	/**
	 * Open the menu to players
	 *
	 * @param players
	 *            The players to open the menu
	 * @return the FastInv builder
	 */
	public FastInv open(Player... players) {
		checkDestroy();

		for (Player p : players) {
			viewers.add(p);
			p.openInventory(inv);
		}
		return this;
	}

	public Inventory getInventory() {
		return inv;
	}

	public boolean isDestroy() {
		return this.destroy;
	}

	public Set<Player> getViewers() {
		return this.viewers;
	}

	private void checkDestroy() {
		if (destroy) {
			throw new IllegalStateException("This inv is destroyed");
		}
	}

	/**
	 * Destory this FastInv (cancel the tasks and unregister the listners)
	 */
	public void destroy() {
		if (destroy) {
			return;
		}
		new ArrayList<>(viewers).forEach(Player::closeInventory);
		HandlerList.unregisterAll(listener);
		tasks.forEach(BukkitTask::cancel);
		destroy = true;
	}

	/**
	 * Set if the menu will be destory when all viewers close it
	 *
	 * @param destroy
	 *            Set if the menu will destory
	 * @return the FastInv builder
	 */
	public FastInv setWillDestroy(boolean destroy) {
		this.willDestroy = destroy;
		return this;
	}

	private void registerListener() {
		listener = new Listener() {

			@EventHandler
			public void onOpen(InventoryOpenEvent e) {
				if (e.getInventory().equals(inv) && e.getPlayer() instanceof Player) {
					Player p = (Player) e.getPlayer();
					checkDestroy();
					if (!viewers.contains(p)) {
						viewers.add(p);
					}
				}
			}

			@EventHandler
			public void onClick(InventoryClickEvent e) {
				if (e.getInventory().equals(inv) && e.getCurrentItem() != null && e.getWhoClicked() instanceof Player) {
					int slot = e.getRawSlot();
					FastInvClickEvent ev = new FastInvClickEvent((Player) e.getWhoClicked(), FastInv.this, slot,
							e.getCurrentItem(), true, e.getAction(), e.getClick());
					if (itemListeners.containsKey(slot)) {
						itemListeners.get(slot).onClick(ev);
					}
					clickListeners.forEach(l -> l.onClick(ev));
					e.setCancelled(ev.isCancelled());
				}
			}

			@EventHandler
			public void onClose(InventoryCloseEvent e) {
				if (e.getInventory().equals(inv) && e.getPlayer() instanceof Player) {
					Player p = (Player) e.getPlayer();

					FastInvCloseEvent ev = new FastInvCloseEvent(p, FastInv.this, false);
					menuListeners.forEach(l -> l.onClose(ev));

					if (ev.isCancelled()) {
						// delay to prevent big errors
						Bukkit.getScheduler().runTask(plugin, () -> p.openInventory(inv));
					} else {
						viewers.remove(p);
						if (viewers.isEmpty() && willDestroy) {
							destroy();
						}
					}
				}
			}

			@EventHandler
			public void onQuit(PlayerQuitEvent e) {
				Player p = e.getPlayer();
				if (viewers.remove(p)) {
					menuListeners.forEach(l -> l.onClose(new FastInvCloseEvent(p, FastInv.this, false)));
					if (viewers.isEmpty() && willDestroy) {
						destroy();
					}
				}
			}

			@EventHandler
			public void onDisable(PluginDisableEvent e) {
				if (e.getPlugin().equals(plugin)) {
					destroy();
				}
			}
		};

		Bukkit.getPluginManager().registerEvents(listener, plugin);
	}

	public class FastInvClickEvent {

		private Player player;
		private FastInv inventory;
		private int slot;
		private ItemStack item;
		private boolean cancelled;
		private InventoryAction action;
		private ClickType clickType;

		FastInvClickEvent(Player player, FastInv inventory, int slot, ItemStack item, boolean cancelled,
				InventoryAction action, ClickType clickType) {
			this.player = player;
			this.inventory = inventory;
			this.slot = slot;
			this.item = item;
			this.cancelled = cancelled;
			this.action = action;
			this.clickType = clickType;
		}

		public Player getPlayer() {
			return this.player;
		}

		public FastInv getInventory() {
			return this.inventory;
		}

		public int getSlot() {
			return this.slot;
		}

		public ItemStack getItem() {
			return this.item;
		}

		public boolean isCancelled() {
			return this.cancelled;
		}

		public void setCancelled(boolean cancelled) {
			this.cancelled = cancelled;
		}

		public InventoryAction getAction() {
			return this.action;
		}

		public ClickType getClickType() {
			return this.clickType;
		}
	}

	public class FastInvCloseEvent {

		private Player player;
		private FastInv inventory;
		private boolean cancelled;

		FastInvCloseEvent(Player player, FastInv inventory, boolean cancelled) {
			this.player = player;
			this.inventory = inventory;
			this.cancelled = cancelled;
		}

		public Player getPlayer() {
			return this.player;
		}

		public FastInv getInventory() {
			return this.inventory;
		}

		public boolean isCancelled() {
			return this.cancelled;
		}

		public void setCancelled(boolean cancelled) {
			this.cancelled = cancelled;
		}
	}

	public interface FastInvClickListener {
		public void onClick(FastInvClickEvent event);
	}

	public interface FastInvCloseListener {
		public void onClose(FastInvCloseEvent event);
	}
}
