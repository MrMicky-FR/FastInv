package fr.mrmicky.fastinv;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 
 * @author MrMicky
 * A complete ItemBuilder for FastInv, only works on 1.8+
 *
 */
@SuppressWarnings("deprecation")
public class ItemBuilder {

	private ItemStack item;
	private ItemMeta meta;

	/*
	 * Constructors
	 */
	public ItemBuilder(Material material) {
		this(new ItemStack(material));
	}

	public ItemBuilder(Material material, int amount) {
		this(new ItemStack(material, amount));
	}

	public ItemBuilder(Material material, byte data) {
		this(new ItemStack(material, 1, data));
	}

	public ItemBuilder(Material material, int amount, byte data) {
		this(new ItemStack(material, amount, data));
	}

	public ItemBuilder(ItemStack item) {
		this.item = item;
		this.meta = item.getItemMeta();
	}

	/*
	 * Meta
	 */
	public boolean hasMeta() {
		return getMeta() != null;
	}

	public ItemMeta getMeta() {
		return meta;
	}

	public ItemBuilder meta(ItemMeta meta) {
		this.meta = meta;
		return this;
	}

	/*
	 * Name
	 */
	public boolean hasName() {
		return meta.hasDisplayName();
	}

	public String getName() {
		return meta.getDisplayName();
	}

	public ItemBuilder name(String name) {
		meta.setDisplayName(name);
		return this;
	}

	/*
	 * Lore
	 */
	public boolean hasLore() {
		return meta.hasLore();
	}

	public List<String> getLore() {
		return meta.getLore();
	}

	public ItemBuilder lore(String... lore) {
		return lore(Arrays.asList(lore));
	}

	public ItemBuilder lore(List<String> lore) {
		meta.setLore(lore);
		return this;
	}

	/*
	 * Enchants
	 */
	public boolean hasEnchants() {
		return meta.hasEnchants();
	}

	public boolean hasEnchant(Enchantment enchantment) {
		return meta.hasEnchant(enchantment);
	}

	public boolean hasConflictingEnchant(Enchantment enchantment) {
		return meta.hasConflictingEnchant(enchantment);
	}

	public Map<Enchantment, Integer> getEnchants() {
		return meta.getEnchants();
	}

	public ItemBuilder enchant(Enchantment enchantment, int level) {
		meta.addEnchant(enchantment, level, true);
		return this;
	}

	public ItemBuilder removeEnchant(Enchantment enchantment) {
		meta.removeEnchant(enchantment);
		return this;
	}

	/*
	 * Flags
	 */
	public boolean hasFlag(ItemFlag flag) {
		return meta.hasItemFlag(flag);
	}

	public Set<ItemFlag> getFlags() {
		return meta.getItemFlags();
	}

	public ItemBuilder flags(ItemFlag... flags) {
		meta.addItemFlags(flags);
		return this;
	}

	public ItemBuilder flags() {
		return flags(ItemFlag.values());
	}

	public ItemBuilder removeFlags(ItemFlag... flags) {
		meta.removeItemFlags(flags);
		return this;
	}

	/*
	 * Unbreakable
	 */
	public boolean isUnbreakable() {
		return meta.spigot().isUnbreakable();
	}

	public ItemBuilder unbreakable() {
		return unbreakable(true);
	}

	public ItemBuilder unbreakable(boolean unbreakable) {
		meta.spigot().setUnbreakable(unbreakable);
		return this;
	}

	/*
	 * ==========================
	 * 
	 * SPECIFIQ META
	 * 
	 * ==========================
	 */

	/*
	 * Banners
	 */
	public DyeColor getBannerBaseColor() {
		return ((BannerMeta) meta).getBaseColor();
	}

	public List<Pattern> getBannerPatterns() {
		return ((BannerMeta) meta).getPatterns();
	}

	public ItemBuilder bannerBaseColor(DyeColor color) {
		((BannerMeta) meta).setBaseColor(color);
		return this;
	}

	public ItemBuilder bannerPatterns(List<Pattern> patterns) {
		((BannerMeta) meta).setPatterns(patterns);
		return this;
	}

	public ItemBuilder bannerPattern(int i, Pattern pattern) {
		((BannerMeta) meta).setPattern(i, pattern);
		return this;
	}

	public ItemBuilder addBannerPatterns(Pattern pattern) {
		((BannerMeta) meta).addPattern(pattern);
		return this;
	}

	public ItemBuilder removeBannerPattern(int i) {
		((BannerMeta) meta).removePattern(i);
		return this;
	}

	/*
	 * Leather armors
	 */
	public Color getLeatherArmorColor() {
		return ((LeatherArmorMeta) meta).getColor();
	}

	public ItemBuilder leatherArmorColor(Color color) {
		((LeatherArmorMeta) meta).setColor(color);
		return this;
	}

	/*
	 * Skull
	 */
	public boolean hasSkullOwner() {
		return ((SkullMeta) meta).hasOwner();
	}

	public String getSkullOwner() {
		return ((SkullMeta) meta).getOwner();
	}

	public ItemBuilder skullOwner(String owner) {
		((SkullMeta) meta).setOwner(owner);
		return this;
	}

	/*
	 * Potion
	 */
	public boolean hasPotionEffect(PotionEffectType type) {
		return ((PotionMeta) meta).hasCustomEffect(type);
	}

	public boolean hasPotionEffects() {
		return ((PotionMeta) meta).hasCustomEffects();
	}

	public List<PotionEffect> getPotionEffects() {
		return ((PotionMeta) meta).getCustomEffects();
	}

	public ItemBuilder addPotionEffect(PotionEffect effect, boolean overwrite) {
		((PotionMeta) meta).addCustomEffect(effect, overwrite);
		return this;
	}

	/*
	 * Build
	 */
	public ItemStack build() {
		item.setItemMeta(meta);
		return item;
	}
}
