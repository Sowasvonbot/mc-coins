package com.github.sowasvonbot.coin.storage;

import static java.util.Objects.requireNonNull;

import com.github.sowasvonbot.CoinsPlugin;
import com.github.sowasvonbot.coin.Coin;
import com.github.sowasvonbot.util.BlockUtility;
import com.github.sowasvonbot.util.ItemConverter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.ScoreboardManager;

/**
 * Coin Buffer, which saves Coins of players, if they can't receive it atm.
 */
public class CoinBuffer {

  private static final String KEY = "CoinBuffer";
  private static CoinBuffer instance;

  private final Objective coinData;


  private CoinBuffer() {
    ScoreboardManager scoreboardManager = requireNonNull(Bukkit.getScoreboardManager());
    if (scoreboardManager.getMainScoreboard().getObjective(KEY) == null) {
      Bukkit.getScoreboardManager().getMainScoreboard()
          .registerNewObjective(KEY, Criteria.create(KEY), KEY);
    }
    coinData = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(KEY);
  }

  /**
   * Singleton for the current CoinBuffer.
   *
   * @return {@link CoinBuffer} which represents all buffered coins of a player
   */
  @SuppressFBWarnings("MS_EXPOSE_REP")
  public static CoinBuffer getInstance() {
    if (instance == null) {
      instance = new CoinBuffer();
    }
    return instance;
  }

  /**
   * Sends coins to the specified player. Every coin should be sent with this method.
   *
   * @param target {@link OfflinePlayer} because the player who receives coins might not be online
   * @param amount int, the amount of coins to send to the player
   */
  public void sendCoins(OfflinePlayer target, int amount) {
    Score targetBuffer = coinData.getScore(target.getUniqueId().toString());
    if (target.isOnline()) {
      amount = sendCoinsToPlayerChests(target.getPlayer(), amount);
    }
    targetBuffer.setScore(targetBuffer.getScore() + amount);
  }

  @SuppressWarnings("unchecked")
  private int sendCoinsToPlayerChests(Player player, int amount) {
    String chestString = player.getPersistentDataContainer()
        .get(Constants.PLAYER_STORAGE_KEY, PersistentDataType.STRING);
    if (chestString == null || chestString.isBlank()) {
      return amount;
    }
    List<String> chests = Arrays.asList(chestString.split(";"));

    if (chests.size() == 0) {
      return amount;
    }

    List<BlockInventoryHolder> inventoryHolders = new LinkedList<>();
    List<String> verifiedChests = new LinkedList<>();

    for (String possibleChestString : chests) {
      try {
        if (possibleChestString.isBlank()) {
          continue;
        }
        Optional<Object> optionalObject = ItemConverter.base64StringToObject(possibleChestString);
        if (optionalObject.isEmpty()) {
          continue;
        }
        if (!(optionalObject.get() instanceof Map map)) {
          continue;
        }

        // Check if the location points to a coin sign
        Location signLocation = Location.deserialize(map);
        if (!CoinSign.isCoinStorageSign(signLocation.getBlock()) || !(signLocation.getBlock()
            .getState() instanceof Sign sign)) {
          continue;
        }

        // Check if the block supporting the sign is an inventory block
        Optional<Block> possibleStorageBlock = BlockUtility.getBlockSupportingSign(sign);
        if (possibleStorageBlock.isEmpty()) {
          continue;
        }
        Block storageBlock = possibleStorageBlock.get();
        if (BlockUtility.isPossibleInventoryBlock(storageBlock)) {
          inventoryHolders.add((BlockInventoryHolder) storageBlock.getState());
          verifiedChests.add(possibleChestString);
        }

      } catch (Exception e) {
        CoinsPlugin.getPluginLogger().warning("Error deserializing Location object");
      }
    }

    player.getPersistentDataContainer()
        .set(Constants.PLAYER_STORAGE_KEY, PersistentDataType.STRING,
            String.join(";", verifiedChests));

    for (BlockInventoryHolder holder : inventoryHolders) {
      amount = fillInventoryWithCoins(holder.getInventory(), amount);
      if (amount == 0) {
        // exiting loop here
        return amount;
      }
    }
    return amount;
  }

  private static int fillInventoryWithCoins(Inventory inventory, int amount) {
    while (amount > 0) {
      ItemStack coins = Coin.createItemStack();
      coins.setAmount(Math.min(amount, coins.getMaxStackSize()));
      Map<Integer, ItemStack> remaining = inventory.addItem(coins);

      if (!remaining.isEmpty()) {
        return remaining.values().stream().mapToInt(ItemStack::getAmount).sum();
      }
      amount -= coins.getAmount();
    }
    return amount;
  }

  /**
   * Returns the buffered coins of one specific player.
   *
   * @param target the player to check for buffered coins
   * @return the amount of coins
   */
  public int getCoins(OfflinePlayer target) {
    return coinData.getScore(target.getUniqueId().toString()).getScore();
  }

  protected void clearPlayerBuffer(Player player) {
    int amount = getCoins(player);
    coinData.getScore(player.getPlayer().getUniqueId().toString()).setScore(0);
    sendCoins(player, amount);
  }

}
