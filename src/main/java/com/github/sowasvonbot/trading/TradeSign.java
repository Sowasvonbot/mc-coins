package com.github.sowasvonbot.trading;

import static java.util.Objects.requireNonNull;

import com.github.sowasvonbot.RealCoinsPlugin;
import com.github.sowasvonbot.coin.Coin;
import com.github.sowasvonbot.coin.storage.CoinBuffer;
import com.github.sowasvonbot.util.BlockUtility;
import com.github.sowasvonbot.util.ItemConverter;
import com.github.sowasvonbot.util.ItemUtility;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Methods for the trade sign.
 */
public class TradeSign {
  private static final NamespacedKey KEY =
      requireNonNull(NamespacedKey.fromString("trade_sign", RealCoinsPlugin.COINS_PLUGIN));
  private static final NamespacedKey AMOUNT =
      requireNonNull(NamespacedKey.fromString("trade_sign_amount", RealCoinsPlugin.COINS_PLUGIN));
  private static final NamespacedKey MATERIAL =
      requireNonNull(NamespacedKey.fromString("trade_sign_material", RealCoinsPlugin.COINS_PLUGIN));
  private static final NamespacedKey OWNER =
      requireNonNull(NamespacedKey.fromString("trade_sign_owner", RealCoinsPlugin.COINS_PLUGIN));
  private static final NamespacedKey PRICE =
      requireNonNull(NamespacedKey.fromString("trade_sign_price", RealCoinsPlugin.COINS_PLUGIN));
  private static final NamespacedKey PIECES =
      requireNonNull(NamespacedKey.fromString("trade_sign_pieces", RealCoinsPlugin.COINS_PLUGIN));

  /**
   * Converts the given sign to a trading sign, e.g. saves the correct persistent data.
   *
   * @param sign   {@link Sign} which needs to be a trading sign.
   * @param player {@link OfflinePlayer} the future owner of the sign.
   * @param item   {@link ItemStack} the item to sell in the chest.
   * @return true, if the conversion was successful.
   */
  public static boolean makeSignTradeSign(Sign sign, OfflinePlayer player, ItemStack item) {
    if (Coin.isCoin(item)) {
      return false;
    }

    Optional<Inventory> inventory = getInventoryRelatedToSign(sign);
    if (inventory.isEmpty()) {
      return false;
    }
    if (inventory.get().getItem(0) != null) {
      return false;
    }
    Optional<String> serializedItem = ItemConverter.convertToBase64(item);
    if (serializedItem.isEmpty()) {
      return false;
    }

    ItemStack tempItem = item.clone();
    tempItem.setAmount(1);
    Optional<ItemStack> uniqueTempItem = ItemUtility.setAsUniqueItem(tempItem);
    if (uniqueTempItem.isEmpty()) {
      return false;
    }
    inventory.get().setItem(0, uniqueTempItem.get());
    PersistentDataContainer dataContainer = sign.getPersistentDataContainer();
    dataContainer.set(KEY, PersistentDataType.INTEGER, 1);
    dataContainer.set(AMOUNT, PersistentDataType.INTEGER, 0);
    dataContainer.set(MATERIAL, PersistentDataType.STRING, serializedItem.get());
    dataContainer.set(OWNER, PersistentDataType.STRING, player.getUniqueId().toString());
    dataContainer.set(PRICE, PersistentDataType.INTEGER, 0);
    dataContainer.set(PIECES, PersistentDataType.INTEGER, item.getAmount());

    sign.setLine(0,
        String.format("§0§l %s", player.getName() == null ? "ERROR" : player.getName()));
    sign.setLine(1, String.format("%s", ItemUtility.getItemName(item)));
    sign.setGlowingText(true);
    updateSign(sign);

    return true;
  }

  /**
   * Checks if the given block is a valid trading sign.
   *
   * @param block {@link Block} to check
   * @return true, if the block is a valid trading sign
   */
  public static boolean isTradingSign(Block block) {
    return BlockUtility.isWallSignWithKey(block, KEY);
  }

  /**
   * Checks if the given block is a valid trading block (chest).
   *
   * @param block {@link Block} to check
   * @return true, if the block is a valid trading block
   */
  public static boolean isTradingBlock(Block block) {
    if (!BlockUtility.isPossibleInventoryBlock(block)) {
      return false;
    }
    return getTradingSign(block).isPresent();
  }

  private static Optional<Block> getTradingSign(Block block) {
    return BlockUtility.getTouchingWallSignWithKey(block, KEY);
  }

  /**
   * Returns the owner off a trading block (e.g. chest or sign).
   *
   * @param block the block to check for it's owner
   * @return {@link OfflinePlayer}, because the owner might not be online
   */
  public static @Nullable OfflinePlayer getOwnerOffTradingBlock(Block block) {
    Optional<Block> tradingSign = getTradingSign(block);
    if (tradingSign.isEmpty()) {
      // TODO refactor to good value
      return null;
    }
    if (!(tradingSign.get().getState() instanceof Sign sign)) {
      return null;
    }
    return Bukkit.getOfflinePlayer(
        UUID.fromString(sign.getPersistentDataContainer().get(OWNER, PersistentDataType.STRING)));
  }

  /**
   * Handles a trade attempt inside the given block with the specified inventory. The inventory
   * might not be needed.
   *
   * @param block     {@link Block} chest for the trade attempt
   * @param inventory {@link Inventory} to update in the trade
   * @return error message if there was an error during trading, otherwise "success"
   */
  static String makeTrade(Block block, Inventory inventory) {
    Optional<Block> possibleTradingSign = getTradingSign(block);
    if (possibleTradingSign.isEmpty()) {
      return "Not a trading Block";
    }
    if (!(possibleTradingSign.get().getState() instanceof Sign sign)) {
      return "error converting block to sign";
    }
    int price = sign.getPersistentDataContainer().get(PRICE, PersistentDataType.INTEGER);
    int available = sign.getPersistentDataContainer().get(AMOUNT, PersistentDataType.INTEGER);
    int pieces = sign.getPersistentDataContainer().get(PIECES, PersistentDataType.INTEGER);

    OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(
        UUID.fromString(sign.getPersistentDataContainer().get(OWNER, PersistentDataType.STRING)));

    ItemStack target = getSignItem(sign);

    Bukkit.getScheduler().runTask(RealCoinsPlugin.COINS_PLUGIN, () -> {
      int coinAmount = 0;
      for (ItemStack itemStack : inventory.getStorageContents()) {
        coinAmount += Coin.isCoin(itemStack) ? itemStack.getAmount() : 0;
      }
      for (Transaction transaction : Transaction.getMaximumPossibleTransactions(available, price,
          pieces, coinAmount)) {
        removeCoinsFromInventory(inventory, transaction.price(), targetPlayer);
        addBuyedItemsToInventory(inventory, sign, transaction.amount(), target);
        sign.update();
        updateSign(sign);
      }
    });
    return "success";
  }

  private static void addBuyedItemsToInventory(Inventory inventory, Sign sign, int amount,
      ItemStack item) {
    ItemStack clone = item.clone();
    clone.setAmount(amount);
    Map<Integer, ItemStack> remainingItems = inventory.addItem(clone);
    for (ItemStack itemStack : remainingItems.values()) {
      sign.getBlock().getWorld().dropItemNaturally(sign.getBlock().getLocation(), itemStack);
    }
    addToIntegerNamespace(sign, AMOUNT, amount * -1);
  }

  private static void removeCoinsFromInventory(Inventory inventory, int amount,
      OfflinePlayer receiver) {
    for (ItemStack itemStack : inventory.getStorageContents()) {
      if (amount == 0) {
        return;
      }
      if (!Coin.isCoin(itemStack)) {
        continue;
      }
      int reduce = Math.min(itemStack.getAmount(), amount);
      itemStack.setAmount(itemStack.getAmount() - reduce);
      amount -= reduce;
      CoinBuffer.getInstance().sendCoins(receiver, reduce);
    }
  }

  private static void updateSign(Sign sign) {
    int pieces = sign.getPersistentDataContainer().get(PIECES, PersistentDataType.INTEGER);
    int price = sign.getPersistentDataContainer().get(PRICE, PersistentDataType.INTEGER);
    int amount = sign.getPersistentDataContainer().get(AMOUNT, PersistentDataType.INTEGER);
    String pieceColor = amount < pieces || pieces == 0 ? "§4" : "§2";
    String priceColor = price == 0 ? "§4" : "§2";
    sign.setLine(2, String.format("%s§l%d§0 = %s§l%d§0 ¢", pieceColor, pieces, priceColor, price));
    sign.setLine(3, String.format("stock: %d", amount));
    sign.update();
  }

  /**
   * Restocks the given trading block by deleting items from its inventory.
   *
   * @param block     the trading block to restock
   * @param inventory the {@link Inventory} of the block
   */
  public static void restock(Block block, Inventory inventory) {
    Optional<Block> possibleTradingSign = getTradingSign(block);
    if (possibleTradingSign.isEmpty()) {
      return;
    }
    if (!(possibleTradingSign.get().getState() instanceof Sign sign)) {
      return;
    }
    Bukkit.getScheduler().runTask(RealCoinsPlugin.COINS_PLUGIN, () -> {
      if (!actualRestock(sign, inventory)) {
        makeTrade(block, inventory);
      }
    });
  }

  private static boolean actualRestock(Sign sign, Inventory inventory) {
    boolean restockSuccessful = false;
    ItemStack target = getSignItem(sign);
    for (ItemStack itemStack : inventory.getStorageContents()) {
      if (itemStack == null || ItemUtility.isUniqueItem(itemStack) || !itemStack.isSimilar(
          target)) {
        continue;
      }
      addToIntegerNamespace(sign, AMOUNT, itemStack.getAmount());
      itemStack.setAmount(0);
      restockSuccessful = true;
    }
    if (restockSuccessful) {
      updateSign(sign);
    }
    return restockSuccessful;
  }

  protected static void spawnItemsOnDestroy(Block block) {
    if (!isTradingSign(block)) {
      return;
    }
    Sign sign = (Sign) block.getState();
    int amount = sign.getPersistentDataContainer().get(AMOUNT, PersistentDataType.INTEGER);
    ItemStack item = ItemConverter.convertToItem(
        sign.getPersistentDataContainer().get(MATERIAL, PersistentDataType.STRING));
    if (item == null) {
      return;
    }
    while (amount > 0) {
      int spawnAmount = Math.min(item.getMaxStackSize(), amount);
      item.setAmount(spawnAmount);
      block.getWorld().dropItemNaturally(BlockUtility.getNextAirBlock(block).getLocation(), item);
      amount -= spawnAmount;
    }
    Optional<Inventory> inventory = getInventoryRelatedToSign(sign);
    if (inventory.isEmpty()) {
      return;
    }
    inventory.get().setItem(0, null);
  }

  protected static void changePrice(Sign sign, ItemStack itemStack, int amount) {
    NamespacedKey toUpdate;
    if (Coin.isCoin(itemStack)) {
      toUpdate = PRICE;
    } else if (getSignItem(sign).isSimilar(itemStack)) {
      toUpdate = PIECES;
    } else {
      return;
    }
    addToIntegerNamespace(sign, toUpdate, amount);
    updateSign(sign);
  }

  static @Nullable ItemStack getTradingBlockItem(Block block) {
    Optional<Block> tempBlock = getTradingSign(block);
    if (tempBlock.isEmpty()) {
      return null;
    }
    return getSignItem((Sign) tempBlock.get().getState());
  }

  private static @Nullable ItemStack getSignItem(Sign sign) {
    return ItemConverter.convertToItem(
        sign.getPersistentDataContainer().get(MATERIAL, PersistentDataType.STRING));
  }

  private static void addToIntegerNamespace(Sign sign, NamespacedKey namespacedKey, int amount) {
    int tempNewValue =
        sign.getPersistentDataContainer().get(namespacedKey, PersistentDataType.INTEGER) + amount;
    // Min number stored is zero
    tempNewValue = Math.max(0, tempNewValue);
    sign.getPersistentDataContainer().set(namespacedKey, PersistentDataType.INTEGER, tempNewValue);
    sign.update();
  }

  private static Optional<Inventory> getInventoryRelatedToSign(Sign sign) {
    Optional<Block> supporter = BlockUtility.getBlockSupportingSign(sign);
    if (supporter.isEmpty()) {
      return Optional.empty();
    }
    if (!BlockUtility.isPossibleInventoryBlock(supporter.get())) {
      return Optional.empty();
    }

    return Optional.of(((BlockInventoryHolder) supporter.get().getState()).getInventory());
  }
}
