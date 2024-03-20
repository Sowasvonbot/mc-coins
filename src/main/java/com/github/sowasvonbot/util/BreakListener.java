package com.github.sowasvonbot.util;

import java.util.Optional;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

/**
 * Prevents destroying of blocks. The result of the keepBlock() function determines if a block will
 * be destroyed or not.
 */
public abstract class BreakListener implements Listener {

  /**
   * Prevents the trading block being destroyed by another user except the owner.
   *
   * @param event {@link BlockBreakEvent}
   */
  @EventHandler
  public void preventBlockDestroyed(BlockBreakEvent event) {
    event.setCancelled(keepBlock(event.getBlock(), Optional.of(event.getPlayer())));
  }

  @EventHandler
  public void preventBlockDestroyed(BlockBurnEvent event) {
    event.setCancelled(keepBlock(event.getBlock()));
  }

  @EventHandler
  public void preventBlockDestroyed(BlockDamageEvent event) {
    event.setCancelled(keepBlock(event.getBlock(), Optional.of(event.getPlayer())));
  }

  @EventHandler
  public void preventBlockDestroyed(EntityExplodeEvent event) {
    event.blockList().removeIf(this::keepBlock);
  }

  @EventHandler
  public void preventBlockDestroyed(BlockIgniteEvent event) {
    event.setCancelled(keepBlock(event.getBlock()));
  }

  @EventHandler
  public void preventBlockMoved(BlockPistonRetractEvent event) {
    event.setCancelled(event.getBlocks().stream().anyMatch(this::keepBlock));
  }

  @EventHandler
  public void preventBlockMoved(BlockPistonExtendEvent event) {
    event.setCancelled(event.getBlocks().stream().anyMatch(this::keepBlock));
  }

  private boolean keepBlock(Block block) {
    return keepBlock(block, Optional.empty());
  }

  protected abstract boolean keepBlock(Block block, Optional<Player> optionalPlayer);
}
