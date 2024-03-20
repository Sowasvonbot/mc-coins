package com.github.sowasvonbot.coin;

import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

/**
 * Custom recipe choice for the coin.
 */
public class CoinRecipeChoice extends RecipeChoice.MaterialChoice {

  public CoinRecipeChoice() {
    super(Material.POISONOUS_POTATO);
  }

  @Override
  public boolean test(ItemStack t) {
    return Coin.isCoin(t);
  }

  @Override
  public int hashCode() {
    return 37 * 3 + Objects.hashCode(this.getChoices().get(0)) + Objects.hash(
        Coin.createItemStack());
  }
}
