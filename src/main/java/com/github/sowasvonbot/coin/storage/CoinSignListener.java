package com.github.sowasvonbot.coin.storage;

import com.github.sowasvonbot.util.BlockUtility;
import com.github.sowasvonbot.util.ConfigHolder;
import java.util.Optional;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.BlockInventoryHolder;

/**
 * Listens to events related to coin signs.
 */
public class CoinSignListener implements Listener {

  /**
   * Creates a coin sign if no other is present on the supporting block.
   *
   * @param event {@link SignChangeEvent} should be the only event necessary to listen to
   */
  @EventHandler
  public void createsCoinSignIfPossible(SignChangeEvent event) {
    Optional<Sign> potentialSign = BlockUtility.checkIfBlockIsSignWithPrefix(event.getBlock(),
        ConfigHolder.getInstance().getString(ConfigHolder.ConfigField.SIGN_PREFIX,
            ConfigHolder.getMaxCharsPredicate(BlockUtility.SIGN_LINE_LENGTH)),
        event.getLines());
    if (potentialSign.isEmpty()) {
      return;
    }
    Sign sign = potentialSign.get();
    Optional<Block> supporter = BlockUtility.getBlockSupportingSign(sign);
    if (supporter.isEmpty() || !(supporter.get().getState() instanceof BlockInventoryHolder)
        || CoinSign.isCoinStorageBlock(supporter.get())) {
      sign.getBlock().breakNaturally();
      event.setCancelled(true);
      return;
    }
    CoinSign.markSignAsCoinSign(sign, event.getPlayer());
  }
}
