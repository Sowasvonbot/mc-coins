package com.github.sowasvonbot.util;

import javax.annotation.Nullable;
import org.bukkit.OfflinePlayer;

/**
 * Utility methods for {@link org.bukkit.entity.Player} and {@link OfflinePlayer}.
 */
public class PlayerUtility {

  /**
   * Custom equality check for two players. Accepts null.
   *
   * @param player1 some {@link OfflinePlayer}
   * @param player2 some {@link OfflinePlayer}
   * @return true, if the players are equal
   */
  public static boolean equal(@Nullable OfflinePlayer player1, @Nullable OfflinePlayer player2) {
    if (player1 == player2) {
      return true;
    }
    if (player1 == null || player2 == null) {
      return false;
    }
    if (player1.equals(player2)) {
      return true;
    }
    return player1.getUniqueId().equals(player2.getUniqueId());
  }
}
