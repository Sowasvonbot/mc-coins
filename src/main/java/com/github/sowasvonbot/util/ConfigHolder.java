package com.github.sowasvonbot.util;

import com.github.sowasvonbot.RealCoinsPlugin;
import java.util.Objects;
import java.util.function.Predicate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Hold the whole plugin config.
 */
public class ConfigHolder {

  private final FileConfiguration fileConfiguration;
  private static ConfigHolder instance;

  private ConfigHolder(JavaPlugin javaPlugin) {
    javaPlugin.saveDefaultConfig();
    fileConfiguration = javaPlugin.getConfig();
  }

  /**
   * Return the String behind the given {@link ConfigField}. If the value is not a String, use the
   * default value. For custom checks see {@link #getValue(ConfigField, Predicate)}
   *
   * @param field {@link ConfigField} to search
   * @return the associated String Value
   */
  public <T> T getValue(ConfigField field, Class<T> valueClass) {
    return this.getValue(field, valueClass, (e) -> true);
  }

  /**
   * Return the Value behind the given {@link ConfigField}. If the value is not of the requested
   * type, use the default value. If the custom check fails, also use the default value.
   *
   * @param field      {@link ConfigField} to search
   * @param valueCheck custom Check as {@link Predicate}, should return false if check fails
   * @return the associated String Value
   */
  public <T> T getValue(ConfigField field, Class<T> valueClass, Predicate<T> valueCheck) {
    T possibleValue = fileConfiguration.getObject(field.getPath(), valueClass);
    if (!predCheck(valueCheck, field, possibleValue)) {
      possibleValue = Objects.requireNonNull(fileConfiguration.getDefaults())
          .getObject(field.getPath(), valueClass);
    }
    return possibleValue;
  }

  /**
   * Get the instance of the ConfigHolder.
   *
   * @return Singleton Instance
   */
  public static ConfigHolder getInstance() {
    if (instance == null) {
      instance = new ConfigHolder(RealCoinsPlugin.COINS_PLUGIN);
    }
    return instance;
  }

  /**
   * Predicate used for Strings with a maximum allowed Number of chars. Fails if the chars in the
   * String exceed the given charAmount. Also logs a warning if the check fails.
   *
   * @param charAmount to check the String length against
   * @return a {@link Predicate} to check the length of a String
   */
  public static Predicate<String> getMaxCharsPredicate(int charAmount) {
    // max sign length
    return (String test) -> {
      if (test.length() > charAmount) {
        RealCoinsPlugin.COINS_PLUGIN.getLogger()
            .warning(() -> String.format("Message %s longer than %d chars", test, charAmount));
        return false;
      }
      return true;
    };
  }

  private static <T> boolean predCheck(Predicate<T> predicate, ConfigField field, T value) {
    if (!predicate.test(value)) {
      RealCoinsPlugin.COINS_PLUGIN.getLogger().warning(
          () -> String.format("Found invalid configuration for %s: %s. Using default value", field,
              value));
      return false;
    }
    return true;
  }

  /**
   * All currently present config fields in the default config.
   */
  public enum ConfigField {
    COIN_DISPLAY_NAME, COIN_SMELT_EXP, COIN_SMELT_TIME, SIGN_PREFIX, STORAGE_CHEST_PREFIX,
    ERROR_CREATE_TRADING_SIGN, ERROR_INVALID_TRADE_CHEST, ERROR_NOT_COIN_DURING_PAY,
    COIN_MESSAGE_FAKE, COIN_MESSAGE_REAL, ERROR_CREATE_COIN_CHEST, COMMAND_MESSAGE_COIN,
    COIN_USE_HEAD, COIN_ITEM_MATERIAL, COIN_HEAD_VALUE, COIN_HEAD_PLAYER_UUID,
    COIN_RESOURCE_PACK_URL, COIN_USE_RESOURCE_PACK, COIN_RECIPE_SHAPE,
    COIN_RECIPE_AMOUNT;

    /**
     * Returns the <b>absolute</b> path for this {@link ConfigField} in the config.yml. May be used
     * to get the value without the need to know the section.
     *
     * @return the path to the field as String
     */
    public String getPath() {
      return switch (this) {
        case COIN_SMELT_EXP -> "coin.smelt_exp";
        case COIN_SMELT_TIME -> "coin.smelt_time";
        case COIN_DISPLAY_NAME -> "coin.display_name";
        case COIN_USE_HEAD -> "coin.use_head";
        case COIN_ITEM_MATERIAL -> "coin.item_material";
        case COIN_HEAD_VALUE -> "coin.head_value";
        case COIN_HEAD_PLAYER_UUID -> "coin.head_player_uuid";
        case COIN_USE_RESOURCE_PACK -> "coin.use_resource_pack";
        case COIN_RESOURCE_PACK_URL -> "coin.resource_pack_url";
        case COIN_RECIPE_SHAPE -> "coin.recipe.shape";
        case COIN_RECIPE_AMOUNT -> "coin.recipe.amount";

        case COMMAND_MESSAGE_COIN -> "command_message.coins";

        case SIGN_PREFIX -> "coin_sign.prefix";
        case STORAGE_CHEST_PREFIX -> "storage_chest.prefix";

        case ERROR_CREATE_TRADING_SIGN -> "error_messages.create_trading_sign";
        case ERROR_CREATE_COIN_CHEST -> "error_messages.create_coin_chest";
        case ERROR_INVALID_TRADE_CHEST -> "error_messages.invalid_trade_chest";
        case ERROR_NOT_COIN_DURING_PAY -> "error_messages.not_coin_during_pay";

        case COIN_MESSAGE_FAKE -> "coin_messages.fake_coin";
        case COIN_MESSAGE_REAL -> "coin_messages.real_coin";
      };
    }
  }
}
