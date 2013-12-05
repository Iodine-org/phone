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
  private static final List<NumberScheme> SCHEMES = new ArrayList<>();
  static {
    for ( String[] item : SCHEME_SPEC)  {
      SCHEMES.add(NumberScheme.create(item[0], item[1]));
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
    NumberScheme.create("1,-3,7;CC=1", "US");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailWhenNDCValueDoesNotMatchlength() {
    NumberScheme.create("2,2,6;CC=30;NDC=222", "GR");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailWhenNDCRangeDoesNotMatchlength() {
    NumberScheme.create("2,2,6;CC=300-400;NDC=222", "GR");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailWhenSNPatternDoesNotMatchlength() {
    NumberScheme.create("2,2,6;CC=30;NDC=22;SN=32***", "GR");
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