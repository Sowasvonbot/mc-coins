package com.github.sowasvonbot.coin;

import static java.util.Objects.requireNonNull;

import com.github.sowasvonbot.RealCoinsPlugin;
import com.github.sowasvonbot.util.ConfigHolder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * Provides methods for coin checks and creation.
 */
public class Coin {

  private static Material coinItemMaterial;

  /**
   * Creates an item stack of coins.
   *
   * @param amount amount of coins
   * @return {@link ItemStack} with special coins
   */
  public static ItemStack createItemStack(int amount) {
    ItemStack coin = createCoinItemStack(amount);
    ItemMeta itemMeta = coin.getItemMeta();
    if (itemMeta == null) {
      throw new IllegalStateException("Can not create coin due to missing item meta");
    }
    itemMeta.getPersistentDataContainer()
        .set(Constants.COIN_KEY, PersistentDataType.STRING, Constants.COIN_KEY_VALUE);
    itemMeta.setDisplayName(ConfigHolder.getInstance()
        .getValue(ConfigHolder.ConfigField.COIN_DISPLAY_NAME, String.class));
    coin.setItemMeta(itemMeta);
    return coin;
  }

  public static ItemStack createItemStack() {
    return createItemStack(1);
  }

  /**
   * Checks if the given ItemStack contains only coins.
   *
   * @param itemStack the {@link ItemStack} to check
   * @return true, if the {@link ItemStack} contains only coins
   */
  public static boolean isCoin(ItemStack itemStack) {
    if (Objects.isNull(itemStack) || Objects.isNull(itemStack.getItemMeta())) {
      return false;
    }
    return itemStack.getItemMeta().getPersistentDataContainer()
        .has(Constants.COIN_KEY, PersistentDataType.STRING);
  }

  /**
   * Returns the recipe to craft one coin.
   *
   * @return {@link ShapedRecipe} to register in the server
   */
  public static ShapedRecipe getRecipe() {
    NamespacedKey recipeKey = requireNonNull(NamespacedKey.fromString("coin_easy"));

    ShapedRecipe coinRecipe = new ShapedRecipe(recipeKey, Coin.createItemStack(3));
    coinRecipe.shape("*E*", "ABA", "*A*");
    coinRecipe.setIngredient('A', Material.GOLD_INGOT);
    coinRecipe.setIngredient('B', Material.DIAMOND);
    coinRecipe.setIngredient('E', Material.EMERALD);

    return coinRecipe;
  }

  /**
   * Returns the recipe to craft back a coin to gold.
   *
   * @return {@link Recipe} to register in the server
   */
  public static Recipe getCraftBackRecipe() {
    NamespacedKey recipeKey = requireNonNull(NamespacedKey.fromString("coin_back"));

    RecipeChoice recipeChoice = new CoinRecipeChoice();

    return new FurnaceRecipe(recipeKey, new ItemStack(Material.GOLD_INGOT, 3), recipeChoice,
        ConfigHolder.getInstance().getValue(ConfigHolder.ConfigField.COIN_SMELT_EXP, Integer.class,
            (smeltExp) -> smeltExp >= 0), ConfigHolder.getInstance()
        .getValue(ConfigHolder.ConfigField.COIN_SMELT_TIME, Integer.class,
            (smeltTime) -> smeltTime >= 0));
  }

  private static Material getCoinItemMaterial() {
    if (coinItemMaterial == null) {
      if (ConfigHolder.getInstance()
          .getValue(ConfigHolder.ConfigField.COIN_USE_HEAD, Boolean.class)) {
        coinItemMaterial = Material.PLAYER_HEAD;
      } else {
        String materialString = ConfigHolder.getInstance()
            .getValue(ConfigHolder.ConfigField.COIN_ITEM_MATERIAL, String.class,
                (material) -> Material.matchMaterial(material.toUpperCase(Locale.ROOT)) != null);
        coinItemMaterial = Material.matchMaterial(materialString);
      }
    }
    return coinItemMaterial;
  }

  private static ItemStack createCoinItemStack(int amount) {
    Material material = getCoinItemMaterial();
    if (material != Material.PLAYER_HEAD) {
      return new ItemStack(material, amount);
    }
    ItemStack headItemstack = new ItemStack(Material.PLAYER_HEAD, amount);
    SkullMeta skullMeta = (SkullMeta) headItemstack.getItemMeta();

    GameProfile gameProfile = new GameProfile(UUID.fromString(ConfigHolder.getInstance()
        .getValue(ConfigHolder.ConfigField.COIN_HEAD_PLAYER_UUID, String.class)), null);
    gameProfile.getProperties().put("textures", new Property("textures", ConfigHolder.getInstance()
        .getValue(ConfigHolder.ConfigField.COIN_HEAD_VALUE, String.class)));

    try {
      Field profileField = skullMeta.getClass().getDeclaredField("profile");
      profileField.setAccessible(true);
      profileField.set(skullMeta, gameProfile);
    } catch (NoSuchFieldException
             | IllegalAccessException e) {
      RealCoinsPlugin.getPluginLogger().log(Level.SEVERE, e,
          () -> "Could not load head file, using default head as coin. "
              + "If this problem is not fixable, please set use_head = false");
    }
    return headItemstack;
  }
}
