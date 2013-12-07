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

  public static final List<String[]> SCHEME_SPEC = new ArrayList<String[]>() {{
    add(new String[] { "CC=3:353;NDC=2:82,83,85,86,87,88,89;SN=7", "IE"});
    add(new String[] { "CC=2:44;NDC=3;SN=6", "UK"});
    add(new String[] { "CC=1:1;NDC=3;SN=7", "US"});
  }};
  private static final List<NumberScheme> SCHEMES = new ArrayList<>();
  static {
    for ( String[] item : SCHEME_SPEC)  {
      NumberScheme scheme = NumberScheme.create(item[0]);
      scheme.setName(item[1]);
      SCHEMES.add(scheme);
    }
  }

  @Test(expected = IllegalStateException.class)
  public void shouldFailIfNoSchemes() {
    NumberFactory.clearSchemes();
    NumberFactory.fromLong(0L);
  }

  @Test
  public void loadMultipleSchemes() {
    NumberFactory.addSchemes(SCHEMES);
    NumberScheme ireland = NumberFactory.getSchemeForCC(353, 12);
    Assert.assertNotNull(ireland);
    NumberScheme uk = NumberFactory.getSchemeForCC(44, 11);
    Assert.assertNotNull(uk);
    NumberScheme us = NumberFactory.getSchemeForCC(1, 11);
    Assert.assertNotNull(us);
  }

  @Test
  public void clearSchemesWillThrowException() {
    NumberFactory.addSchemes(SCHEMES);
    NumberFactory.clearSchemes();
    NumberFactory.getSchemeForCC(353, 12);
  }

  @Test
  public void shouldSilentlyFailToLoadNoDefaultProperties() {
    NumberFactory.loadDefaultScheme();
  }

  @Test
  public void shouldSilentlyFailToLoadNonExistingProperties() {
    NumberFactory.loadSchemesFromResource("/no");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailWithNegativePartLength() {
    NumberScheme.create("CC=1:1;NDC=-1;SN=1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailWhenNDCValueDoesNotMatchlength() {
    NumberScheme.create("CC=2:30;NDC=2:222;SN=6");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailWhenNDCRangeDoesNotMatchlength() {
    NumberScheme.create("CC=2:22;NDC=2:300-400;SN=6");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailWhenSNPatternDoesNotMatchlength() {
    NumberScheme.create("CC=2:30;NDC=2:22;SN=6:32***");
  }

  @Test
  public void returnsNullForNonExistantScheme() {
    Assert.assertNull(NumberFactory.getScheme("XYZ"));
  }

  @Test
  public void loadsDefaultSchemes() throws IOException {
    NumberFactory.clearSchemes();

    try {
      createTempSchemeFile(SCHEME_SPEC,"out/test/iodine-phone/NumberScheme.properties");

      NumberFactory.loadDefaultScheme();
      Assert.assertNotNull(NumberFactory.getScheme("IE"));
    } finally {
      NumberFactory.clearSchemes();
    }
  }

  public static void createTempSchemeFile(List<String[]> schemeSpec, String path) throws IOException {
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