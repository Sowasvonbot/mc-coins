package com.github.sowasvonbot.coin;

import static java.util.Objects.requireNonNull;

import com.github.sowasvonbot.util.ConfigHolder;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * Provides methods for coin checks and creation.
 */
public class Coin {


  /**
   * Creates an item stack of coins.
   *
   * @param amount amount of coins
   * @return {@link ItemStack} with special coins
   */
  public static ItemStack createItemStack(int amount) {
    ItemStack coin = new ItemStack(Material.POISONOUS_POTATO, amount);
    ItemMeta itemMeta = coin.getItemMeta();
    if (itemMeta == null) {
      throw new IllegalStateException("Can not create coin due to missing item meta");
    }
    itemMeta.getPersistentDataContainer()
        .set(Constants.COIN_KEY, PersistentDataType.STRING, Constants.COIN_KEY_VALUE);
    itemMeta.setDisplayName(
        ConfigHolder.getInstance().getString(ConfigHolder.ConfigField.COIN_DISPLAY_NAME));
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
        ConfigHolder.getInstance()
            .getNumber(ConfigHolder.ConfigField.COIN_SMELT_EXP, (smeltExp) -> smeltExp >= 0),
        ConfigHolder.getInstance().getNumber(ConfigHolder.ConfigField.COIN_SMELT_TIME,
            (smeltTime) -> smeltTime >= 0));

  }



}
