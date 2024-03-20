package com.github.sowasvonbot.trading;

import com.github.sowasvonbot.coin.Coin;
import com.github.sowasvonbot.util.BlockUtility;
import com.github.sowasvonbot.util.ConfigHolder;
import com.github.sowasvonbot.util.PlayerUtility;
import java.util.Optional;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.BlockInventoryHolder;

/**
 * Listener for all events related to signs.
 */
public class SignListener implements Listener {

  /**
   * Handles the creation of signs.
   *
   * @param event {@link PlayerInteractEvent}
   */
  @EventHandler
  public void playerInteractWithShield(PlayerInteractEvent event) {
    if (event.getClickedBlock() == null) {
      return;
    }
    if (!(event.getClickedBlock().getBlockData() instanceof WallSign)) {
      return;
    }
    if (event.getItem() == null) {
      return;
    }
    if (!(event.getClickedBlock().getState() instanceof Sign sign)) {
      return;
    }
    String firstLine = sign.getLine(0);
    if (!firstLine.equals(ConfigHolder.getInstance()
        .getValue(ConfigHolder.ConfigField.STORAGE_CHEST_PREFIX, String.class,
            ConfigHolder.getMaxCharsPredicate(BlockUtility.SIGN_LINE_LENGTH)))
        && !TradeSign.isTradingSign(event.getClickedBlock())) {
      return;
    }

    if (!TradeSign.isTradingSign(event.getClickedBlock())) {
      if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
        if (!TradeSign.makeSignTradeSign(sign, event.getPlayer(), event.getItem())) {
          event.getPlayer().sendMessage(ConfigHolder.getInstance()
              .getValue(ConfigHolder.ConfigField.ERROR_CREATE_TRADING_SIGN, String.class));
          sign.getBlock().breakNaturally();
          // Experimental block duplication fix
          event.setCancelled(true);
        }
      }
      return;
    }

    if (!PlayerUtility.equal(event.getPlayer(),
        TradeSign.getOwnerOffTradingBlock(event.getClickedBlock()))) {
      return;
    }

    if (!event.hasItem() || event.getItem() == null) {
      return;
    }

    if (!event.getItem().isSimilar(TradeSign.getTradingBlockItem(sign.getBlock())) && !Coin.isCoin(
        event.getItem())) {
      return;
    }
    // decrease with left click, increase with right click
    int multiplier = switch (event.getAction()) {
      case LEFT_CLICK_BLOCK -> -1;
      case RIGHT_CLICK_BLOCK -> 1;
      default -> 0;
    };
    TradeSign.changePrice(sign, event.getItem(), event.getItem().getAmount() * multiplier);
    event.setCancelled(true);
  }

  /**
   * Prevents the placing of a trading sign, if the container already contains a trading sign.
   *
   * @param event {@link SignChangeEvent} called every time a player names a sign
   */
  @EventHandler
  public void preventCreatingTradingSignIfSignAlreadyPresent(SignChangeEvent event) {
    Optional<Sign> potentialSign = BlockUtility.checkIfBlockIsSignWithPrefix(event.getBlock(),
        ConfigHolder.getInstance()
            .getValue(ConfigHolder.ConfigField.STORAGE_CHEST_PREFIX, String.class,
                ConfigHolder.getMaxCharsPredicate(BlockUtility.SIGN_LINE_LENGTH)),
        event.getLines());
    if (potentialSign.isEmpty()) {
      return;
    }
    Sign sign = potentialSign.get();
    Optional<Block> supporter = BlockUtility.getBlockSupportingSign(sign);
    if (supporter.isEmpty() || !(supporter.get().getState() instanceof BlockInventoryHolder)
        || TradeSign.isTradingBlock(supporter.get())) {
      sign.getBlock().breakNaturally();
      event.setCancelled(true);
    }
  }
}
