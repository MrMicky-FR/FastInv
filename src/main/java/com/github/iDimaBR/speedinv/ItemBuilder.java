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

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Simple {@link ItemStack} builder.
 *
 * @author MrMicky
 */
public class ItemBuilder {

    private final ItemStack item;

    public static ItemBuilder copyOf(ItemStack item) {
        return new ItemBuilder(item.clone());
    }

    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    public ItemBuilder(ItemStack item) {
        this.item = Objects.requireNonNull(item, "item");
    }

    public ItemBuilder edit(Consumer<ItemStack> function) {
        function.accept(this.item);
        return this;
    }

    public ItemBuilder meta(Consumer<ItemMeta> metaConsumer) {
        return edit(item -> {
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                metaConsumer.accept(meta);
                item.setItemMeta(meta);
            }
        });
    }

    public <T extends ItemMeta> ItemBuilder meta(Class<T> metaClass, Consumer<T> metaConsumer) {
        return meta(meta -> {
            if (metaClass.isInstance(meta)) {
                metaConsumer.accept(metaClass.cast(meta));
            }
        });
    }

    public ItemBuilder type(Material material) {
        return edit(item -> item.setType(material));
    }

    public ItemBuilder data(int data) {
        return durability((short) data);
    }

    @SuppressWarnings("deprecation")
    public ItemBuilder durability(short durability) {
        return edit(item -> item.setDurability(durability));
    }

    public ItemBuilder amount(int amount) {
        return edit(item -> item.setAmount(amount));
    }

    public ItemBuilder enchant(Enchantment enchantment) {
        return enchant(enchantment, 1);
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        return meta(meta -> meta.addEnchant(enchantment, level, true));
    }

    public ItemBuilder removeEnchant(Enchantment enchantment) {
        return meta(meta -> meta.removeEnchant(enchantment));
    }

    public ItemBuilder removeEnchants() {
        return meta(m -> m.getEnchants().keySet().forEach(m::removeEnchant));
    }

    public ItemBuilder name(String name) {
        return meta(meta -> meta.setDisplayName(name));
    }

    public ItemBuilder lore(String lore) {
        return lore(Collections.singletonList(lore));
    }

    public ItemBuilder lore(String... lore) {
        return lore(Arrays.asList(lore));
    }

    public ItemBuilder lore(List<String> lore) {
        return meta(meta -> meta.setLore(lore));
    }

    public ItemBuilder addLore(String line) {
        return meta(meta -> {
            List<String> lore = meta.getLore();

            if (lore == null) {
                meta.setLore(Collections.singletonList(line));
                return;
            }

            lore.add(line);
            meta.setLore(lore);
        });
    }

    public ItemBuilder addLore(String... lines) {
        return addLore(Arrays.asList(lines));
    }

    public ItemBuilder addLore(List<String> lines) {
        return meta(meta -> {
            List<String> lore = meta.getLore();

            if (lore == null) {
                meta.setLore(lines);
                return;
            }

            lore.addAll(lines);
            meta.setLore(lore);
        });
    }

    public ItemBuilder flags(ItemFlag... flags) {
        return meta(meta -> meta.addItemFlags(flags));
    }

    public ItemBuilder flags() {
        return flags(ItemFlag.values());
    }

    public ItemBuilder removeFlags(ItemFlag... flags) {
        return meta(meta -> meta.removeItemFlags(flags));
    }

    public ItemBuilder removeFlags() {
        return removeFlags(ItemFlag.values());
    }

    public ItemBuilder armorColor(Color color) {
        return meta(LeatherArmorMeta.class, meta -> meta.setColor(color));
    }

    public ItemStack build() {
        return this.item;
    }
}
