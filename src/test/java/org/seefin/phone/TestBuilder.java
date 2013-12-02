package org.seefin.phone;

import org.junit.Assert;
import org.junit.Test;

public class TestBuilder {

  @Test
  public void testBuild() {
    MSISDNScheme.addScheme(MSISDNScheme.parse ( "3,2,7;CC=353;NDC=82,83,85,86,87,88,89", "IE"));

    MSISDN number = MSISDN.Builder().cc(353).ndc(87).subscriber(3538080).build();
    Assert.assertEquals(353873538080L, number.longValue());
    Assert.assertEquals(353, number.getCC());
    Assert.assertEquals(87, number.getNDC());
    Assert.assertEquals(3538080, number.getSN());
  }

  @Test
  public void testSchemeBuilder() {
   // MSISDNScheme.clearSchemes();
    MSISDNScheme newScheme = MSISDNScheme.parse("2,3,10;CC=49;NDC=160,162,163,170-179", "DE.tmob+vfone");
    MSISDNScheme.addScheme(newScheme);
    MSISDN number = MSISDN.valueOf(491711234567890L);
    Assert.assertTrue ( newScheme.isValid(number));
    Assert.assertEquals(49, number.getCC());
    Assert.assertEquals(171, number.getNDC());
    Assert.assertEquals(1234567890, number.getSN());
  }
}
