package com.github.sowasvonbot.util;


import com.github.sowasvonbot.RealCoinsPlugin;
import java.net.MalformedURLException;
import java.net.URL;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Resolves the resource pack for this plugin if necessary.
 */
public class ResourcePackResolver implements Listener {

  /**
   * If resource packs are enabled, request the client to install it.
   *
   * @param playerJoinEvent the event to listen to
   */
  @EventHandler
  public void suggestResourcePack(PlayerJoinEvent playerJoinEvent) {
    if (!ConfigHolder.getInstance()
        .getValue(ConfigHolder.ConfigField.COIN_USE_RESOURCE_PACK, Boolean.class)) {
      return;
    }

    String url = ConfigHolder.getInstance()
        .getValue(ConfigHolder.ConfigField.COIN_RESOURCE_PACK_URL, String.class, (urlToTest) -> {
          try {
            new URL(urlToTest);
          } catch (MalformedURLException e) {
            RealCoinsPlugin.getPluginLogger().warning(
                () -> String.format("Malformed URL %s used for config field %s", urlToTest,
                    ConfigHolder.ConfigField.COIN_RESOURCE_PACK_URL.getPath()));
            return false;
          }
          return true;
        });
    playerJoinEvent.getPlayer().setResourcePack(url);
  }
}
