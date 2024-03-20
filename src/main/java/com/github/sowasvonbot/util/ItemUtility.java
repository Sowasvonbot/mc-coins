package com.github.sowasvonbot.util;

import com.github.sowasvonbot.CoinsPlugin;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Class for item utility methods.
 */
public abstract class ItemUtility {

  private static final NamespacedKey UNIQUE_KEY =
      Objects.requireNonNull(NamespacedKey.fromString("somestupidname"));

  private static final Enchantment FALLBACK_ENCHANTMENT = Enchantment.BINDING_CURSE;
  private static final int FALLBACK_ENCHANTMENT_LEVEL = 1;

  /**
   * Retrieve a meaningful name for the given {@link ItemStack}.
   *
   * @param itemStack {@link ItemStack} to retrieve the name from
   * @return {@link String} the name of the given item
   */
  public static String getItemName(ItemStack itemStack) {
    if (itemStack.hasItemMeta() && Objects.requireNonNull(itemStack.getItemMeta())
        .hasDisplayName()) {
      return itemStack.getItemMeta().getDisplayName();
    }
    return itemStack.getType().name().replace('_', ' ').toLowerCase();
  }


  /**
   * Marks the given ItemStack as unique.
   *
   * @param itemStack {@link ItemStack} to mark
   * @return a filled {@link Optional} if marking the {@link ItemStack} was successful
   */
  public static Optional<ItemStack> setAsUniqueItem(ItemStack itemStack) {
    if (itemStack.containsEnchantment(FALLBACK_ENCHANTMENT)) {
      return Optional.empty();
    }
    if (!itemStack.hasItemMeta()) {
      if (itemStack.containsEnchantment(FALLBACK_ENCHANTMENT)) {
        return Optional.empty();
      }
      try {
        itemStack.addUnsafeEnchantment(FALLBACK_ENCHANTMENT, FALLBACK_ENCHANTMENT_LEVEL);
      } catch (Exception e) {
        CoinsPlugin.getPluginLogger()
            .warning(String.format("Error creating a unique item: %s", e.getMessage()));
        return Optional.empty();
      }
    }
    ItemMeta meta = itemStack.getItemMeta();
    meta.getPersistentDataContainer().set(UNIQUE_KEY, PersistentDataType.INTEGER, 1);
    if (itemStack.setItemMeta(meta)) {
      return Optional.of(itemStack);
    } else {
      return Optional.empty();
    }
  }

  /**
   * Returns true, if the given {@link ItemStack} was marked as unique with
   * {@link #setAsUniqueItem(ItemStack)}.
   *
   * @param itemStack {@link ItemStack} to check
   * @return true, if unique
   */
  public static boolean isUniqueItem(@Nullable ItemStack itemStack) {
    if (itemStack == null || !itemStack.hasItemMeta()) {
      return false;
    }
    return itemStack.getItemMeta().getPersistentDataContainer()
        .has(UNIQUE_KEY, PersistentDataType.INTEGER);
  }
}
