package com.github.sowasvonbot.coin.storage;

import com.github.sowasvonbot.RealCoinsPlugin;
import com.github.sowasvonbot.util.ConfigHolder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * All commands relating to coins.
 */
public class CoinCommands implements CommandExecutor {

  private CoinCommands() {
  }

  public static void register() {
    CoinCommands coinCommands = new CoinCommands();
    RealCoinsPlugin.COINS_PLUGIN.getCommand("coins").setExecutor(coinCommands);
  }

  @Override
  public boolean onCommand(CommandSender commandSender, Command command, String label,
      String[] args) {
    if (!(commandSender instanceof Player player)) {
      return false;
    }

    switch (command.getName()) {
      case "coins" -> commandSender.sendMessage(String.format(ConfigHolder.getInstance()
          .getValue(ConfigHolder.ConfigField.COMMAND_MESSAGE_COIN, String.class,
              (message) -> message.contains("%sd")), CoinBuffer.getInstance().getCoins(player)));
      default -> throw new CommandException(String.format("Command %s not found", label));
    }
    return true;
  }
}
