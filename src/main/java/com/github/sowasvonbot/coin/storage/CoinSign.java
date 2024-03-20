package com.github.sowasvonbot.coin.storage;

import com.github.sowasvonbot.RealCoinsPlugin;
import com.github.sowasvonbot.util.BlockUtility;
import com.github.sowasvonbot.util.ConfigHolder;
import com.github.sowasvonbot.util.ItemConverter;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Methods for CoinSigns.
 */
public class CoinSign {
  private static final String KEY_VALUE = "coinStorageChest";
  private static final NamespacedKey OWNER =
      NamespacedKey.fromString("coin_sign_owner", RealCoinsPlugin.COINS_PLUGIN);
  private static final NamespacedKey KEY =
      NamespacedKey.fromString("coin_sign", RealCoinsPlugin.COINS_PLUGIN);

  static boolean isCoinStorageBlock(Block block) {
    return BlockUtility.getTouchingWallSignWithKey(block, KEY).isPresent();
  }

  static boolean isCoinStorageSign(Block block) {
    return BlockUtility.isWallSignWithKey(block, KEY);
  }

  protected static void markSignAsCoinSign(Sign sign, Player player) {
    sign.getPersistentDataContainer().set(KEY, PersistentDataType.STRING, KEY_VALUE);
    sign.getPersistentDataContainer()
        .set(OWNER, PersistentDataType.STRING, player.getUniqueId().toString());
    sign.update();
    addCoinChestToPlayer(player, sign.getLocation());
    CoinBuffer.getInstance().clearPlayerBuffer(player);
  }

  private static void addCoinChestToPlayer(Player player, Location location) {
    PersistentDataContainer container = player.getPersistentDataContainer();

    if (!container.getKeys().contains(Constants.PLAYER_STORAGE_KEY)) {
      container.set(Constants.PLAYER_STORAGE_KEY, PersistentDataType.STRING, "");
    }

    String chests = container.get(Constants.PLAYER_STORAGE_KEY, PersistentDataType.STRING);

    Optional<String> newChest = ItemConverter.objectToBase64String(location.serialize());
    if (newChest.isEmpty()) {
      player.sendMessage(ConfigHolder.getInstance()
          .getValue(ConfigHolder.ConfigField.ERROR_CREATE_COIN_CHEST, String.class));
      location.getBlock().breakNaturally();
      return;
    }
    if (!chests.isBlank()) {
      chests += ";";
    }
    chests += newChest.get();
    container.set(Constants.PLAYER_STORAGE_KEY, PersistentDataType.STRING, chests);
  }
}
