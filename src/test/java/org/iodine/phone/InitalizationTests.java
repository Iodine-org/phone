package org.iodine.phone;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class InitalizationTests {

  private static final List<MSISDNScheme> SCHEMES = new ArrayList<MSISDNScheme>() {{
    add(MSISDNScheme.create("3,2,7;CC=353;NDC=82,83,85,86,87,88,89", "IE"));
    add(MSISDNScheme.create("2,3,6;CC=44", "UK"));
    add(MSISDNScheme.create("1,3,7;CC=1", "US"));
  }};

  @Test(expected=IllegalStateException.class)
  public void shouldFailIfNoSchemes() {
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
  public void shouldSilentlyFailToLoadNonExistiongProperties() {
    MSISDNFactory.loadSchemesFromResource("/no");
  }



  @Test(expected = IllegalArgumentException.class)
  public void shouldFailWithNegativePartLength() {
    MSISDNScheme.create("1,-3,7;CC=1", "US");
  }
}
