package com.github.sowasvonbot.util;

import com.github.sowasvonbot.RealCoinsPlugin;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Utility class to convert {@link ItemStack ItemStacks} to Strings and these Strings back to
 * {@link ItemStack ItemStacks}.
 */
public class ItemConverter {

  public static Optional<String> convertToBase64(ItemStack[] items) {
    return objectToBase64String(serializeItemStack(items));
  }

  public static Optional<String> convertToBase64(ItemStack items) {
    return objectToBase64String(serializeItemStack(items));
  }

  /**
   * Returns a {@link String} representation of the given object. The {@link Optional} is empty, if
   * some error occurred. The representation might be deserialized with
   * {@link ItemConverter#base64StringToObject(String)}.
   *
   * @param object an arbitrary {@link Object}
   * @return a filled {@link Optional} if the serialization was successful
   */
  public static Optional<String> objectToBase64String(Object object) {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(object);
      oos.flush();
      return Optional.of(Base64.getEncoder().encodeToString(bos.toByteArray()));
    } catch (Exception e) {
      RealCoinsPlugin.getPluginLogger().warning(e.getMessage());
    }
    return Optional.empty();
  }

  /**
   * Returns a {@link Object} deserialized from the given {@link String}. The {@link Optional} is
   * empty, if some error occurred. Only {@link String}s obtained with
   * {@link ItemConverter#base64StringToObject(String)} can be deserialized with this method.
   *
   * @param base64String an arbitrary {@link Object} serialized to a base644 {@link String} with
   *                     {@link ItemConverter#base64StringToObject(String)}
   * @return a filled {@link Optional} if the deserialization was successful
   */
  public static Optional<Object> base64StringToObject(String base64String) {
    try {
      ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode(base64String));
      ObjectInputStream ois = new ObjectInputStream(bis);
      return Optional.of(ois.readObject());
    } catch (Exception e) {
      RealCoinsPlugin.getPluginLogger().warning(e.getMessage());
    }
    return Optional.empty();
  }

  /**
   * Converts an encoded {@link ItemStack} array back to an {@link ItemStack} array.
   *
   * @param base64 encoded array as String
   * @return {@link ItemStack} array
   */
  @SuppressWarnings("unchecked")
  public static ItemStack[] convertToItems(String base64) {
    Optional<Object> serializedData = base64StringToObject(base64);
    if (serializedData.isEmpty()) {
      return new ItemStack[] {new ItemStack(Material.AIR)};
    }
    Map<String, Object>[] returnValue = (Map<String, Object>[]) serializedData.get();
    return deserializeItemStack(returnValue);
  }

  /**
   * Converts an encoded {@link ItemStack} back to an {@link ItemStack}.
   *
   * @param base64 encoded array as String
   * @return {@link ItemStack}
   */
  @SuppressWarnings("unchecked")
  public static @Nullable ItemStack convertToItem(String base64) {
    Optional<Object> serializedData = base64StringToObject(base64);
    if (serializedData.isEmpty()) {
      return new ItemStack(Material.AIR);
    }
    Map<String, Object> returnValue = (Map<String, Object>) serializedData.get();
    return deserializeItemStack(returnValue);
  }

  private static Map<String, Object> serializeItemStack(ItemStack items) {
    if (items == null) {
      return new HashMap<>();
    }
    Map<String, Object> result = items.serialize();
    if (items.hasItemMeta()) {
      result.put("meta", items.getItemMeta().serialize());
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object>[] serializeItemStack(ItemStack[] items) {
    Map<String, Object>[] result = new Map[items.length];
    for (int i = 0; i < items.length; i++) {
      ItemStack is = items[i];
      result[i] = serializeItemStack(is);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private static @Nullable ItemStack deserializeItemStack(Map<String, Object> map) {
    if (map.size() == 0) {
      return null;
    }
    try {
      if (map.containsKey("meta")) {
        Map<String, Object> im = new HashMap<>((Map<String, Object>) map.remove("meta"));
        im.put("==", "ItemMeta");
        ItemStack is = ItemStack.deserialize(map);
        is.setItemMeta((ItemMeta) ConfigurationSerialization.deserializeObject(im));
        return is;
      } else {
        return ItemStack.deserialize(map);
      }
    } catch (Exception e) {
      RealCoinsPlugin.getPluginLogger().warning(e.getMessage());
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  private static ItemStack[] deserializeItemStack(Map<String, Object>[] map) {
    ItemStack[] items = new ItemStack[map.length];

    for (int i = 0; i < items.length; i++) {
      items[i] = deserializeItemStack(map[i]);
    }
    return items;
  }
}
