package fr.mrmicky.fastinv;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * A fast API to easely create advanced GUI
 *
 * @author MrMicky
 * @version 2.0.1
 * 
 */
public class FastInv implements InventoryHolder {

	private static Plugin plugin = null;

	public static void init(Plugin plugin) {
		if (FastInv.plugin == null) {
			FastInv.plugin = plugin;
			Bukkit.getPluginManager().registerEvents(getListener(), plugin);
		}
	}

	private Inventory inv;
	private boolean cancelTasksOnClose = true;
	private Set<FastInvCloseListener> closeListeners = new HashSet<>();
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
		this(size, "FastInv");
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
		this(size, InventoryType.CHEST, title);
	}

	/**
	 * Create a new FastInv with a custom type
	 *
	 * @param type
	 *            The type of the inventory
	 */
	public FastInv(InventoryType type) {
		this(type, "FastInv");
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
		this(0, type, title);
	}

	private FastInv(int size, InventoryType type, String title) {
		if (type == InventoryType.CHEST && size > 0) {
			inv = Bukkit.createInventory(this, size, title);
		} else {
			inv = Bukkit.createInventory(this, type, title);
		}
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
		if (slot > 0) {
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
		closeListeners.add(listener);
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
		clickListeners.add(listener);
		return this;
	}

	public FastInv onUpdate(long period, Runnable runnable) {
		return onUpdate(period, period, runnable);
	}

	public FastInv onUpdate(long delay, long period, Runnable runnable) {
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
	public void open(Player... players) {
		Bukkit.getScheduler().runTask(plugin, () -> {
			for (Player p : players) {
				p.openInventory(inv);
			}
		});
	}

	/**
	 * Cancel all tasks
	 */
	public void cancelTasks() {
		tasks.forEach(BukkitTask::cancel);
		tasks.clear();
	}

	/**
	 * Set if the tasks will be cancel on inventory close
	 *
	 * @param cancelTasksOnClose
	 *            Set if the menu will destory
	 * @return the FastInv builder
	 */
	public FastInv setCancelTasksOnClose(boolean cancelTasksOnClose) {
		this.cancelTasksOnClose = cancelTasksOnClose;
		return this;
	}

	@Override
	public Inventory getInventory() {
		return inv;
	}

	private static Listener getListener() {
		return new Listener() {

			@EventHandler
			public void onClick(InventoryClickEvent e) {
				if (e.getInventory().getHolder() instanceof FastInv && e.getWhoClicked() instanceof Player) {
					int slot = e.getRawSlot();
					FastInv inv = (FastInv) e.getInventory().getHolder();
					FastInvClickEvent ev = new FastInvClickEvent((Player) e.getWhoClicked(), inv, slot,
							e.getCurrentItem(), true, e.getAction(), e.getClick());

					if (inv.itemListeners.containsKey(slot)) {
						inv.itemListeners.get(slot).onClick(ev);
					}

					inv.clickListeners.forEach(l -> l.onClick(ev));

					if (ev.isCancelled()) {
						e.setCancelled(true);
					}
				}
			}

			@EventHandler
			public void onClose(InventoryCloseEvent e) {
				if (e.getInventory().getHolder() instanceof FastInv && e.getPlayer() instanceof Player) {
					Player p = (Player) e.getPlayer();
					FastInv inv = (FastInv) e.getInventory().getHolder();

					FastInvCloseEvent ev = new FastInvCloseEvent(p, inv, false);
					inv.closeListeners.forEach(l -> l.onClose(ev));

					Bukkit.getScheduler().runTask(plugin, () -> {
						// delay to prevent errors
						if (ev.isCancelled() && p.isOnline()) {
							p.openInventory(inv.getInventory());
						} else if (e.getInventory().getViewers().isEmpty() && inv.cancelTasksOnClose) {
							inv.cancelTasks();
						}
					});
				}
			}

			@EventHandler
			public void onDisable(PluginDisableEvent e) {
				if (e.getPlugin().equals(plugin)) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getOpenInventory().getTopInventory().getHolder() instanceof  FastInv) {
                            p.closeInventory();
                        }
                    }
                }
			}
		};
	}

	public static class FastInvClickEvent {

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

	public static class FastInvCloseEvent {

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
