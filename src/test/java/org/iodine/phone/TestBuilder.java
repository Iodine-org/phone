package org.iodine.phone;

import org.junit.Assert;
import org.junit.Test;

import static org.iodine.phone.NumberScheme.SchemeType;

public class TestBuilder {

  @Test
  public void testBuild() {
    NumberFactory.addScheme(NumberScheme.SchemeBuilder()
        .cc("3:353")
        .ndc("2:82,83,85,86,87,88,89")
        .sn("7")
        .label("IE.Mobile")
        .iso3166("IE")
        .type(SchemeType.FIXED_LINE).build());

    PhoneNumber number = PhoneNumber.Builder().cc(353).ndc(87).subscriber(3538080).build();
    Assert.assertEquals(353873538080L, number.longValue());
    Assert.assertEquals(353, number.getCountryCode());
    Assert.assertEquals(87, number.getNationalDialingCode());
    Assert.assertEquals(3538080, number.getSubscriberNumber());
  }

  @Test
  public void testSchemeBuilder() {
   // NumberScheme.clearSchemes();
    NumberScheme newScheme = NumberScheme.create("CC=2:99;NDC=2:22;SN=6:111111;Type=Mobile");
    Assert.assertEquals(SchemeType.MOBILE, newScheme.getType());
    NumberFactory.addScheme(newScheme);
    PhoneNumber number = PhoneNumber.valueOf(9922111111L);
    Assert.assertTrue(newScheme.isValid(number));
    Assert.assertEquals(99, number.getCountryCode());
    Assert.assertEquals(22, number.getNationalDialingCode());
    Assert.assertEquals(111111, number.getSubscriberNumber());
  }

  @Test(expected = IllegalArgumentException.class)
  public void failsWithBadRange() {
    NumberScheme.SchemeBuilder()
        .cc("2:99")
        .ndc("2:19-11")
        .sn("6:111111")
        .type(SchemeType.VOIP)
        .label("XX").build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void failsWithEmptyString() {
    NumberFactory.createMSISDN("");
  }
}
