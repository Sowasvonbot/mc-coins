package com.github.sowasvonbot.updater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class VersionTest {

  @Test
  public void testStringToVersionComparison() {
    Map<Version, String> versionsToTest =
        Map.of(new Version(1, 1, 1), "v1.1.1", new Version(0, 0, 1), "v0.0.1",
            new Version(100, 10, 2), "meineOmafaehrtimHuehnerstallMotoradv100.10.2gell?");

    for (Map.Entry<Version, String> entry : versionsToTest.entrySet()) {
      Optional<Version> converted = Version.fromString(entry.getValue());
      assertFalse(converted.isEmpty());
      assertEquals(entry.getKey(), converted.get());
    }

  }

  @Test
  public void testNewestVersion() {
    List<Version> someVersions =
        List.of(new Version(1, 1, 1), new Version(2, 1000000, 0), new Version(100, 2, 10000000),
            new Version(999, 3, 1));

    assertEquals(new Version(999, 3, 1), Version.getMaxVersion(someVersions));
  }

}
