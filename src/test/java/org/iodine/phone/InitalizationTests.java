package org.iodine.phone;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class InitalizationTests {

  private static final List<String[]> SCHEME_SPEC = new ArrayList<String[]>() {{
    add(new String[] { "3,2,7;CC=353;NDC=82,83,85,86,87,88,89", "IE"});
    add(new String[] { "2,3,6;CC=44", "UK"});
    add(new String[] { "1,3,7;CC=1", "US"});
  }};
  private static final List<MSISDNScheme> SCHEMES = new ArrayList<>();
  static {
    for ( String[] item : SCHEME_SPEC)  {
      SCHEMES.add(MSISDNScheme.create(item[0], item[1]));
    }
  }

  @Test(expected = IllegalStateException.class)
  public void shouldFailIfNoSchemes() {
    MSISDNFactory.clearSchemes();
    MSISDNFactory.fromLong(0L);
  }

  @Test
  public void loadMultipleSchemes() {
    MSISDNFactory.addSchemes(SCHEMES);
    MSISDNScheme ireland = MSISDNFactory.getSchemeForCC(353, 12);
    Assert.assertNotNull(ireland);
    MSISDNScheme uk = MSISDNFactory.getSchemeForCC(44, 11);
    Assert.assertNotNull(uk);
    MSISDNScheme us = MSISDNFactory.getSchemeForCC(1, 11);
    Assert.assertNotNull(us);
  }

  @Test
  public void clearSchemesWillThrowException() {
    MSISDNFactory.addSchemes(SCHEMES);
    MSISDNFactory.clearSchemes();
    MSISDNFactory.getSchemeForCC(353, 12);
  }

  @Test
  public void shouldSilentlyFailToLoadNoDefaultProperties() {
    MSISDNFactory.loadDefaultScheme();
  }

  @Test
  public void shouldSilentlyFailToLoadNonExistingProperties() {
    MSISDNFactory.loadSchemesFromResource("/no");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailWithNegativePartLength() {
    MSISDNScheme.create("1,-3,7;CC=1", "US");
  }

  @Test
  public void returnsNullForNonExistantScheme() {
    Assert.assertNull(MSISDNFactory.getScheme("XYZ"));
  }

  @Test
  public void loadsDefaultSchemes() throws IOException {
    MSISDNFactory.clearSchemes();

    try {
      createTempSchemeFile(SCHEME_SPEC,"out/test/iodine-phone/MSISDNScheme.properties");

      MSISDNFactory.loadDefaultScheme();
      Assert.assertNotNull(MSISDNFactory.getScheme("IE"));
    } finally {
      MSISDNFactory.clearSchemes();
    }
  }

  private void createTempSchemeFile(List<String[]> schemeSpec, String path) throws IOException {
    File propFile = new File(path);
    propFile.deleteOnExit();
    PrintWriter printer = new PrintWriter(new FileOutputStream(propFile),true);
    for (String[] scheme : schemeSpec) {
      printer.printf("%s=%s\n", scheme[1], scheme[0]);
    }
    printer.close();
    Assert.assertTrue(propFile.canRead());
  }
}