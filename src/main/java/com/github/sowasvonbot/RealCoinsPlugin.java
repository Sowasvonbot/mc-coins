package com.github.sowasvonbot;

import com.github.sowasvonbot.coin.Coin;
import com.github.sowasvonbot.coin.CoinListener;
import com.github.sowasvonbot.coin.storage.CoinCommands;
import com.github.sowasvonbot.coin.storage.CoinSignListener;
import com.github.sowasvonbot.coin.storage.PlayerJoinListener;
import com.github.sowasvonbot.trading.ChestListener;
import com.github.sowasvonbot.trading.SignListener;
import com.github.sowasvonbot.trading.TradeBlockBreakListener;
import com.github.sowasvonbot.util.ResourcePackResolver;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class.
 */
public class RealCoinsPlugin extends JavaPlugin {

  @SuppressFBWarnings("MS_CANNOT_BE_FINAL") public static RealCoinsPlugin COINS_PLUGIN;

  public static Logger getPluginLogger() {
    return COINS_PLUGIN.getLogger();
  }

  @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
  @Override
  public void onEnable() {
    COINS_PLUGIN = this;
    getPluginLogger().info("Coin started!");

    getServer().addRecipe(Coin.getRecipe());
    getServer().addRecipe(Coin.getCraftBackRecipe());
    getServer().getPluginManager().registerEvents(new CoinListener(), this);

    getServer().getPluginManager().registerEvents(new CoinSignListener(), this);
    getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

    getServer().getPluginManager().registerEvents(new ChestListener(), this);
    getServer().getPluginManager().registerEvents(new SignListener(), this);
    getServer().getPluginManager().registerEvents(new TradeBlockBreakListener(), this);

    getServer().getPluginManager().registerEvents(new ResourcePackResolver(), this);

    CoinCommands.register();
  }

  @Override
  public void onDisable() {
    getPluginLogger().info("Coin stopped");
    getServer().removeRecipe(Coin.getRecipe().getKey());
  }
}
