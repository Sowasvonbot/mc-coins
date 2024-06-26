package com.github.sowasvonbot.coin;

import com.github.sowasvonbot.trading.TradeSign;
import com.github.sowasvonbot.util.ConfigHolder;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

/**
 * Listener for coins. Mainly used to prevent placing coins in the world.
 */
public class CoinListener implements Listener {

  /**
   * Prevents the player to eat a coin.
   *
   * @param event {@link PlayerItemConsumeEvent} fired, when a player tries to eat something
   */
  @EventHandler
  public void preventCoinFromGettingEaten(PlayerItemConsumeEvent event) {
    // I think an enum check is faster than reading data from the persistent data container
    if (event.getItem().getType() != Material.POISONOUS_POTATO) {
      return;
    }
    if (!(event.getItem().hasItemMeta() && event.getItem().getItemMeta().hasDisplayName())) {
      return;
    }
    event.setCancelled(true);
    if (!Coin.isCoin(event.getItem())) {
      if (event.getItem().getItemMeta().getDisplayName().toLowerCase().contains("coin")) {
        event.getPlayer().sendMessage(ConfigHolder.getInstance()
            .getValue(ConfigHolder.ConfigField.COIN_MESSAGE_FAKE, String.class));
      }
      return;
    }
    event.getPlayer().sendMessage(ConfigHolder.getInstance()
        .getValue(ConfigHolder.ConfigField.COIN_MESSAGE_REAL, String.class));
  }

  /**
   * Prevents the smelting of all poisonous potatoes except coins.
   *
   * @param event {@link FurnaceSmeltEvent}, was the only event I can cancel
   */
  @EventHandler
  public void preventFalseCoinGettingSmelted(FurnaceSmeltEvent event) {
    if (event.getSource().getType() != Material.POISONOUS_POTATO) {
      return;
    }
    event.setCancelled(!Coin.isCoin(event.getSource()));
  }

  /**
   * Prevents the lighting of all poisonous potatoes except coins.
   *
   * @param event {@link FurnaceBurnEvent}, to be cancelled if the players tries to smelt a potato,
   *              which is not a coin
   */
  @EventHandler
  public void preventFalseCoinFurnaceLit(FurnaceBurnEvent event) {
    if (!(event.getBlock() instanceof Furnace furnace)) {
      return;
    }
    if (furnace.getInventory().getSmelting() == null) {
      return;
    }
    if (furnace.getInventory().getSmelting().getType() != Material.POISONOUS_POTATO) {
      return;
    }
    event.setCancelled(!Coin.isCoin(furnace.getInventory().getSmelting()));
  }

  /**
   * Prevents a head coin from being placed in the world.
   *
   * @param event {@link PlayerInteractEvent}, to be cancelled if the item is a head coin.
   */
  @EventHandler
  public void preventHeadCoinFromBeingPlaced(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return;
    }
    if (!event.hasItem() || !event.hasBlock()) {
      return;
    }
    if (Objects.requireNonNull(event.getItem()).getType() != Material.PLAYER_HEAD) {
      return;
    }
    if (TradeSign.isTradingSign(event.getClickedBlock())) {
      return;
    }
    event.setCancelled(Coin.isCoin(event.getItem()));
  }
}
