package com.github.sowasvonbot.util;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.inventory.BlockInventoryHolder;

/**
 * Utility methods for blocks.
 */
public class BlockUtility {

  public static final List<BlockFace> SIGN_DIRECTIONS =
      List.of(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);

  public static final int SIGN_LINE_LENGTH = 15;

  /**
   * Returns the block which supports a wall sign.
   *
   * @param sign {@link Sign} which should be a wall sign
   * @return {@link Optional} with the supporting {@link Block}, empty if no block was found
   */
  public static Optional<Block> getBlockSupportingSign(Sign sign) {
    if (!(sign.getBlockData() instanceof WallSign wallSign)) {
      return Optional.empty();
    }
    Block supporter = sign.getBlock().getRelative(wallSign.getFacing().getOppositeFace());
    assert supporter.getType().isBlock();
    return Optional.of(supporter);
  }

  /**
   * Returns the next Block of AIR above the given block.
   *
   * @param block the block to check for the next air block
   * @return {@link Block} with type {@link Material} AIR
   */
  public static Block getNextAirBlock(Block block) {
    while (block.getType() != Material.AIR) {
      block = block.getRelative(0, 1, 0);
    }
    return block;
  }

  /**
   * Returns true if the given block is a {@link WallSign} with a specific {@link NamespacedKey}.
   *
   * @param block         an arbitrary {@link Block}
   * @param namespacedKey {@link NamespacedKey}
   * @return true, if the block is a wall sign with the given key
   */
  public static boolean isWallSignWithKey(Block block, NamespacedKey namespacedKey) {
    if (!(block.getBlockData() instanceof WallSign)) {
      return false;
    }
    if (!(block.getState() instanceof Sign sign)) {
      return false;
    }
    return sign.getPersistentDataContainer().getKeys().contains(namespacedKey);
  }

  /**
   * Returns a touching wall sign block with the given key.
   *
   * @param block         an arbitrary block
   * @param namespacedKey {@link NamespacedKey}
   * @return block is present, if a wall sign with the given key was found
   */
  public static Optional<Block> getTouchingWallSignWithKey(Block block,
      NamespacedKey namespacedKey) {
    if (isWallSignWithKey(block, namespacedKey)) {
      return Optional.of(block);
    }

    for (BlockFace face : SIGN_DIRECTIONS) {
      Block possibleTradingSign = block.getRelative(face);
      if (isWallSignWithKey(possibleTradingSign, namespacedKey)) {
        Optional<Block> supportedBlock =
            getBlockSupportingSign((Sign) possibleTradingSign.getState());
        if (supportedBlock.isEmpty()) {
          continue;
        }
        if (supportedBlock.get().equals(block)) {
          return Optional.of(possibleTradingSign);
        }
      }
    }
    return Optional.empty();
  }


  /**
   * Checks the given block if it is a sign and contains the given prefix.
   *
   * @param block    an arbitrary {@link Block}
   * @param prefix   {@link String}, an arbitrary prefix
   * @param newLines the new lines for this block
   * @return an filled {@link Optional} if the block is a valid sign, otherwise it will be empty
   */
  public static Optional<Sign> checkIfBlockIsSignWithPrefix(Block block, String prefix,
      String[] newLines) {
    if (!(block.getState() instanceof Sign sign)) {
      return Optional.empty();
    }
    return Arrays.stream(newLines).anyMatch(line -> line.contains(prefix)) ? Optional.of(sign) :
        Optional.empty();
  }

  /**
   * Checks if the given block is a valid block for trading or coin storage.
   *
   * @param block {@link Block} to check
   * @return true, if the given {@link Block} is a valid trading block
   */

  public static boolean isPossibleInventoryBlock(Block block) {
    if (!(block.getState() instanceof BlockInventoryHolder)) {
      return false;
    }
    return block.getType() != Material.HOPPER && block.getType() != Material.DROPPER
        && block.getType() != Material.DISPENSER;
  }
}
