package com.github.sowasvonbot.coin;

import com.github.sowasvonbot.CoinsPlugin;
import org.bukkit.NamespacedKey;

/**
 * Constants for the coins.
 */
public abstract class Constants {
  protected static final NamespacedKey COIN_KEY =
      new NamespacedKey(CoinsPlugin.COINS_PLUGIN, "coin");
  protected static final String COIN_KEY_VALUE = "Coin";
}
