package org.iodine.phone;

import org.junit.Assert;
import org.junit.Test;

public class TestBuilder {

  @Test
  public void testBuild() {
    NumberFactory.addScheme(NumberScheme.create("3,2,7;CC=353;NDC=82,83,85,86,87,88,89", "IE"));

    PhoneNumber number = PhoneNumber.Builder().cc(353).ndc(87).subscriber(3538080).build();
    Assert.assertEquals(353873538080L, number.longValue());
    Assert.assertEquals(353, number.getCountryCode());
    Assert.assertEquals(87, number.getNationalDialingCode());
    Assert.assertEquals(3538080, number.getSubscriberNumber());
  }

  @Test
  public void testSchemeBuilder() {
   // NumberScheme.clearSchemes();
    NumberScheme newScheme = NumberScheme.create("2,2,6;CC=99;NDC=22;SN=111111", "XX");
    NumberFactory.addScheme(newScheme);
    PhoneNumber number = PhoneNumber.valueOf(9922111111L);
    Assert.assertTrue ( newScheme.isValid(number));
    Assert.assertEquals(99, number.getCountryCode());
    Assert.assertEquals(22, number.getNationalDialingCode());
    Assert.assertEquals(111111, number.getSubscriberNumber());
  }

  @Test(expected = IllegalArgumentException.class)
  public void failsWithBadRange() {
    NumberScheme.create("2,2,6;CC=99;NDC=19-11;SN=111111", "XX");
  }

  @Test(expected = IllegalArgumentException.class)
  public void failsWithEmptyString() {
    NumberFactory.createMSISDN("");
  }
}
