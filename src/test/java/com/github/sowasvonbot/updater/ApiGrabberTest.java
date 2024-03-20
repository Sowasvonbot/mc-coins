package com.github.sowasvonbot.updater;

import com.google.gson.JsonArray;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ApiGrabberTest {

  @Test
  public void testGitlabGrab() throws ApiException {
    JsonArray jsonArray = ApiGrabber.queryResult(
        ApiGrabber.GITLAB_BASE + "/" + ApiGrabber.PROJECT_SUB_URL + "/"
            + ApiGrabber.RELEASE_STRING);

    List<Version> versions =
        ApiGrabber.getVersionsFromJsonArray(jsonArray, ApiGrabber.GITLAB_VERSION_KEY);
    Assertions.assertEquals(jsonArray.size(), versions.size());
  }

  @Test
  public void testRetrieveDownloadURLFromGitlab() {
    Optional<URL> optional = ApiGrabber.getGitlabDownloadUrlForVersion(new Version(0, 0, 1));
    Assertions.assertTrue(optional.isPresent());
    Assertions.assertTrue(optional.get().getFile().endsWith("CoinPlugin.jar"));
  }

}
