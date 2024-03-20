package com.github.sowasvonbot.trading;

import java.util.LinkedList;
import java.util.List;

/**
 * Represent an item transaction in a trading block.
 *
 * @param price  int, needed for one transaction
 * @param amount int, received pieces
 */
public record Transaction(int price, int amount) {


  /**
   * Returns a list of possible transactions.
   *
   * @param maxStored      int, maximum available
   * @param price          int, price for one transaction
   * @param piecesPerPrice int, pieces received per transaction
   * @param money          int, available money
   * @return {@link List} of {@link Transaction}s available
   */
  public static List<Transaction> getMaximumPossibleTransactions(int maxStored, int price,
      int piecesPerPrice, int money) {
    List<Transaction> transactions = new LinkedList<>();

    while (maxStored >= piecesPerPrice && money >= price && maxStored > 0 && price > 0) {
      maxStored -= piecesPerPrice;
      money -= price;
      transactions.add(new Transaction(price, piecesPerPrice));
    }
    return transactions;
  }
}
