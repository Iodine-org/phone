package org.seefin.phone;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class InitalizationTests {

  private static final List<MSISDNScheme> SCHEMES = new ArrayList<MSISDNScheme>() {{
    add(MSISDNScheme.parse("3,2,7;CC=353;NDC=82,83,85,86,87,88,89", "IE"));
    add(MSISDNScheme.parse("2,3,6;CC=44", "UK"));
    add(MSISDNScheme.parse("1,3,7;CC=1", "US"));
  }};

  @Test(expected=IllegalArgumentException.class)
  public void shouldFailIfNoSchemes() {
    MSISDNScheme.fromLong(0L);
  }

  @Test
  public void loadMultipleSchemes() {
    MSISDNScheme.addSchemes(SCHEMES);
    MSISDNScheme ireland = MSISDNScheme.getSchemeForCC(353, 12);
    Assert.assertNotNull(ireland);
    MSISDNScheme uk = MSISDNScheme.getSchemeForCC(44, 11);
    Assert.assertNotNull(uk);
    MSISDNScheme us = MSISDNScheme.getSchemeForCC(1, 11);
    Assert.assertNotNull(us);
  }

  @Test
  public void clearSchemesWillThrowException() {
    MSISDNScheme.addSchemes(SCHEMES);
    MSISDNScheme.clearSchemes();
    MSISDNScheme.getSchemeForCC(353, 12);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailWithNegativePartLength() {
    MSISDNScheme.parse("1,-3,7;CC=1", "US");
  }
}
