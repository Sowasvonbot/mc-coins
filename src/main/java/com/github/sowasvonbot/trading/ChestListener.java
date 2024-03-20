package com.github.sowasvonbot.trading;

import com.github.sowasvonbot.coin.Coin;
import com.github.sowasvonbot.util.BlockUtility;
import com.github.sowasvonbot.util.ConfigHolder;
import com.github.sowasvonbot.util.ItemUtility;
import com.github.sowasvonbot.util.PlayerUtility;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for all events related to chests.
 */
public class ChestListener implements Listener {

  /**
   * Handles the buy attempt of a player.
   *
   * @param event {@link InventoryClickEvent}, when a player interacts with an inventory
   */
  @EventHandler
  public void buyAttempt(InventoryClickEvent event) {
    if (event.getClickedInventory() == null) {
      return;
    }
    if (!(event.getWhoClicked() instanceof Player player)) {
      return;
    }
    Location location = event.getView().getTopInventory().getLocation();
    if (location == null) {
      return;
    }
    Block block = location.getBlock();
    if (!TradeSign.isTradingBlock(block)) {
      return;
    }

    if (ItemUtility.isUniqueItem(event.getCurrentItem()) || ItemUtility.isUniqueItem(
        event.getCursor())) {
      event.setCancelled(true);
      return;
    }
    boolean topChestClicked =
        event.getClickedInventory().equals(event.getView().getTopInventory());
    ItemStack tradingItem = TradeSign.getTradingBlockItem(block);

    if (!(topChestClicked || event.isShiftClick())) {
      return;
    }

    if (tradingItem == null) {
      event.setCancelled(true);
      event.getWhoClicked().sendMessage(ConfigHolder.getInstance()
          .getString(ConfigHolder.ConfigField.ERROR_INVALID_TRADE_CHEST));
      return;
    }


    if (PlayerUtility.equal(player, TradeSign.getOwnerOffTradingBlock(block))) {
      handleOwnerBlockUpdate(event.getView().getTopInventory(), block);
    } else {
      if (topChestClicked && tradingItem.isSimilar(event.getCurrentItem())) {
        return;
      }
      ItemStack primaryItem = event.getCurrentItem();
      if (primaryItem == null || primaryItem.getType() == Material.AIR) {
        primaryItem = event.getCursor();
      }
      event.setCancelled(
          !handlePlayerBlockUpdate(block, player, event.getView().getTopInventory(),
              primaryItem));
    }


  }

  /**
   * Prevents drag gestures in trading blocks.
   *
   * @param event {@link InventoryDragEvent}, when a player interacts with an inventory
   */
  @EventHandler
  public void preventDragGestures(InventoryDragEvent event) {
    Location location = event.getView().getTopInventory().getLocation();
    if (location == null) {
      return;
    }
    if (!(event.getWhoClicked() instanceof Player player)) {
      return;
    }
    Block block = location.getBlock();
    if (!TradeSign.isTradingBlock(block)) {
      return;
    }
    if (ItemUtility.isUniqueItem(event.getCursor())) {
      event.setCancelled(true);
      return;
    }
    if (event.getInventory().equals(event.getView().getBottomInventory())) {
      return;
    }
    // event only takes place in the bottom inventory, there we don't care
    if (event.getRawSlots().stream().map(event.getView()::getInventory)
        .allMatch(event.getView().getBottomInventory()::equals)) {
      return;
    }
    if (PlayerUtility.equal(player, TradeSign.getOwnerOffTradingBlock(block))) {
      handleOwnerBlockUpdate(event.getView().getTopInventory(), block);
    } else {
      event.setCancelled(
          !handlePlayerBlockUpdate(block, player, event.getView().getTopInventory(),
              event.getCursor()));
    }
  }

  /**
   * Prevents item moves to trading blocks.
   *
   * @param event {@link InventoryMoveItemEvent} called, when one block moves the item to
   *              another block
   */
  @EventHandler
  public void preventAllInteractions(InventoryMoveItemEvent event) {
    if (event.getSource().getLocation() == null
        && event.getDestination().getLocation() == null) {
      return;
    }
    if (event.getSource().getLocation() != null) {
      Block source = event.getSource().getLocation().getBlock();
      if (cancelEvent(source)) {
        event.setCancelled(true);
        return;
      }
    }
    if (event.getDestination().getLocation() != null) {
      Block destination = event.getDestination().getLocation().getBlock();
      event.setCancelled(cancelEvent(destination));
    }
  }

  private static boolean cancelEvent(Block block) {
    return BlockUtility.isPossibleInventoryBlock(block) && TradeSign.isTradingBlock(block);
  }

  private void handleOwnerBlockUpdate(Inventory inventory, Block block) {
    TradeSign.restock(block, inventory);
  }

  private boolean handlePlayerBlockUpdate(Block block, Player player, Inventory inventory,
      ItemStack itemStack) {
    if (!Coin.isCoin(itemStack)) {
      player.sendMessage(ConfigHolder.getInstance()
          .getString(ConfigHolder.ConfigField.ERROR_NOT_COIN_DURING_PAY));
      return false;
    }
    player.sendMessage(TradeSign.makeTrade(block, inventory));
    return true;
  }

  /**
   * Prevents double chests from connecting to trading blocks.
   *
   * @param event {@link BlockPlaceEvent} fired every time a block is placed
   */
  @EventHandler
  public void preventDoubleChestCreation(BlockPlaceEvent event) {
    if (event.getBlock().getType() != Material.CHEST
        && event.getBlock().getType() != Material.TRAPPED_CHEST) {
      return;
    }
    event.setCancelled(BlockUtility.SIGN_DIRECTIONS.stream().map(event.getBlock()::getRelative)
        .filter(block -> event.getBlock().getType() == block.getType())
        .anyMatch(TradeSign::isTradingBlock));
  }

}
