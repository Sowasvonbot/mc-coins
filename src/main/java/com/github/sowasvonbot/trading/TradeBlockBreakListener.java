package com.github.sowasvonbot.trading;

import com.github.sowasvonbot.util.BreakListener;
import com.github.sowasvonbot.util.PlayerUtility;
import java.util.Optional;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Prevents destroying of trading blocks.
 */
public class TradeBlockBreakListener extends BreakListener {
  @Override
  protected boolean keepBlock(Block block, Optional<Player> optionalPlayer) {
    if (optionalPlayer.isPresent()) {
      if (optionalPlayer.get().isOp() && optionalPlayer.get().getGameMode()
          .equals(GameMode.CREATIVE)) {
        return TradeSign.isTradingBlock(block);
      }
    }
    if (TradeSign.isTradingBlock(block)) {
      return true;
    }
    if (TradeSign.isTradingSign(block)) {
      return optionalPlayer.map(
              player -> !PlayerUtility.equal(TradeSign.getOwnerOffTradingBlock(block), player))
          .orElse(true);
    }
    return false;

  }

  @Override
  @EventHandler
  public void preventBlockDestroyed(BlockBreakEvent event) {
    super.preventBlockDestroyed(event);
    if (event.isCancelled()) {
      return;
    }
    TradeSign.spawnItemsOnDestroy(event.getBlock());
  }
}
