package com.github.sowasvonbot.coin.storage;

import com.github.sowasvonbot.RealCoinsPlugin;
import java.util.Objects;
import org.bukkit.NamespacedKey;

/**
 * Constants for the coin storage.
 */
public abstract class Constants {

  protected static final NamespacedKey PLAYER_STORAGE_KEY = Objects.requireNonNull(
      NamespacedKey.fromString("coin_storage_chests", RealCoinsPlugin.COINS_PLUGIN));
}
