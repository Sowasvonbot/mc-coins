package com.github.sowasvonbot.coin.storage;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Listens to player events related to the coin buffer.
 */
public class PlayerJoinListener implements Listener {

  @EventHandler
  public void sendCoinsOnJoin(PlayerJoinEvent event) {
    CoinBuffer.getInstance().clearPlayerBuffer(event.getPlayer());
  }
}
