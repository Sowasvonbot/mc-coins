package com.github.sowasvonbot.util;

import com.github.sowasvonbot.CoinsPlugin;
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
   * Return the String behind the given {@link ConfigField}. If the value is not a String, use
   * the default value. For custom checks see {@link #getString(ConfigField, Predicate)}
   *
   * @param field {@link ConfigField} to search
   * @return the associated String Value
   */
  public String getString(ConfigField field) {
    return this.getString(field, (e) -> true);
  }

  /**
   * Return the String behind the given {@link ConfigField}. If the value is not a String, use
   * the default value. If the custom check fails, also use the default value.
   *
   * @param field      {@link ConfigField} to search
   * @param valueCheck custom Check as {@link Predicate}, should return false if check fails
   * @return the associated String Value
   */
  public String getString(ConfigField field, Predicate<String> valueCheck) {
    String possibleValue = fileConfiguration.getString(field.getPath());
    if (!predCheck(valueCheck, field, possibleValue)) {
      possibleValue =
          Objects.requireNonNull(fileConfiguration.getDefaults()).getString(field.getPath());
    }
    return possibleValue;
  }

  /**
   * Return the Integer behind the given {@link ConfigField}. If the value is not a Integer, use
   * the default value. For custom checks see {@link #getNumber(ConfigField, Predicate)}
   *
   * @param field {@link ConfigField} to search
   * @return the associated Integer Value
   */
  public int getNumber(ConfigField field) {
    return this.getNumber(field, (e) -> true);
  }



  /**
   * Return the Integer behind the given {@link ConfigField}. If the value is not a Integer, use
   * the default value. If the custom check fails, also use the default value.
   *
   * @param field      {@link ConfigField} to search
   * @param valueCheck custom Check as {@link Predicate}, should return false if check fails
   * @return the associated Integer Value
   */
  public int getNumber(ConfigField field, Predicate<Integer> valueCheck) {
    int possibleValue = fileConfiguration.getInt(field.getPath());
    if (!predCheck(valueCheck, field, possibleValue)) {
      possibleValue =
          Objects.requireNonNull(fileConfiguration.getDefaults()).getInt(field.getPath());
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
      instance = new ConfigHolder(CoinsPlugin.COINS_PLUGIN);
    }
    return instance;
  }

  /**
   * Predicate used for Strings with a maximum allowed Number of chars. Fails if the chars in
   * the String exceed the given charAmount. Also logs a warning if the check fails.
   *
   * @param charAmount to check the String length against
   * @return a {@link Predicate} to check the length of a String
   */
  public static Predicate<String> getMaxCharsPredicate(int charAmount) {
    // max sign length
    return (String test) -> {
      if (test.length() > charAmount) {
        CoinsPlugin.COINS_PLUGIN.getLogger()
            .warning(() -> String.format("Message %s longer than %d chars", test, charAmount));
        return false;
      }
      return true;
    };
  }

  private static <T> boolean predCheck(Predicate<T> predicate, ConfigField field, T value) {
    if (!predicate.test(value)) {
      CoinsPlugin.COINS_PLUGIN.getLogger().warning(
          () -> String.format("Found invalid configuration for %s: %s. Using default value",
              field, value));
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
    COIN_MESSAGE_FAKE, COIN_MESSAGE_REAL, ERROR_CREATE_COIN_CHEST, COMMAND_MESSAGE_COIN;

    /**
     * Returns the <b>absolute</b> path for this {@link ConfigField} in the config.yml. May be
     * used to get the value without the need to know the section.
     *
     * @return the path to the field as String
     */
    public String getPath() {
      return switch (this) {
        case COIN_SMELT_EXP -> "coin.smelt_exp";
        case COIN_SMELT_TIME -> "coin.smelt_time";
        case COIN_DISPLAY_NAME -> "coin.display_name";

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
