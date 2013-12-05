package org.iodine.phone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestNumberSchemes {
  private static final String[] testNumbersEG =
      {
          "+20 10  123 3576", "+20 100 342 5643", // Vodafone
          "+20 11  456 9425", "+20 111 652 8763", // Etisalat
          "+20 12  789 7842", "+20 122 645 8853", // Mobinil
          "+20 14  167 9633", "+20 114 853 2587", // Etisalat
          "+20 150 352 0353", "+20 120 854 4632", // Mobinil
          "+20 151 864 2114", "+20 101 582 5464", // Vodafone
          "+20 152 913 5653", "+20 112 112 9465", // Etisalat
          "+20 16  102 0053", "+20 106 970 3246", // Vodafone
          "+20 17  458 4524", "+20 127 572 3323", // Mobinil
          "+20 18  943 7685", "+20 128 982 9875", // Mobinil
          "+20 19  636 6377", "+20 109 776 4587"  // Vodafone
      };

  private static final String[] testMsisdns =
      {"20 122 123 7543",
          "20 114 123 7543",
          "20 106 123 7543",
          "20 127 123 7543",
          "20 128 123 7543",
          "20 109 123 7543"};

  @Before
  public void loadTestSchemas() {
    NumberFactory.loadSchemesFromResource("/TestMsisdn.properties");
  }

  @Test
  public void
  testGuessSchemeIE() {
    PhoneNumber number = PhoneNumber.parse("+353-86-3578380");
    Assert.assertEquals(353, number.getCountryCode());
    Assert.assertEquals ( "+353 86 3578380", number.format("+$CC $NDC $SN"));
  }

  @Test
  public void
  testGuessScheme() {
    Assert.assertEquals("+201500000001", PhoneNumber.valueOf(201500000001L).toString());
    Assert.assertEquals("+44865249864", PhoneNumber.parse("+44.865.249.864").toString());
    PhoneNumber nb = PhoneNumber.parse("+234901220887");
    Assert.assertEquals("+234901220887", nb.toString());
  }

  @Test
  public void
  testEGNumbers() {
    for (String number : testNumbersEG) {
      PhoneNumber phoneNumber = PhoneNumber.parse(number);
      Assert.assertEquals(20, phoneNumber.getCountryCode());
    }
  }

  @Test
  public void
  testEGNumbers2() {
    for (String number : testMsisdns) {
      PhoneNumber phoneNumber = PhoneNumber.parse(number);
      Assert.assertEquals(20, phoneNumber.getCountryCode());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void
  testCountryOnly() {
    PhoneNumber.parse("+44");
  }

  @Test(expected = IllegalArgumentException.class)
  public void
  testCountryPlusNDCOnly() {
    PhoneNumber.parse("+35396");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailIfLongInvalid() {
    PhoneNumber.valueOf(0L);
  }

  @Test
  public void
  testSortList() {
    List<PhoneNumber> list = new ArrayList<>();
    for (String number : testNumbersEG) {
      PhoneNumber phoneNumber = PhoneNumber.parse(number);
      list.add(phoneNumber);
    }
    Collections.sort(list);
    PhoneNumber[] array = list.toArray(new PhoneNumber[list.size()]);
    Assert.assertEquals("+20101233576", array[0].toString());
    Assert.assertEquals("+201529135653", array[array.length - 1].toString());
  }

  @Test
  public void
  testGuessSchemeUS() {
    PhoneNumber usNumber = PhoneNumber.parse("1 855 784-9261");
    Assert.assertEquals("+18557849261", usNumber.toString());
    Assert.assertEquals(1, usNumber.getCountryCode());
    Assert.assertEquals(855, usNumber.getNationalDialingCode());
    Assert.assertEquals(7849261, usNumber.getSubscriberNumber());
  }

  @Test
  public void
  testEquality() {
    PhoneNumber number1 = PhoneNumber.parse("+353-86-3578380");
    PhoneNumber number2 = PhoneNumber.valueOf(353863578380L);
    Assert.assertEquals(number1, number2);
    Assert.assertNotSame(number1, number2);
    Assert.assertTrue(number1.equals(number2) && number2.equals(number1));
    Assert.assertTrue(number1.hashCode() == number2.hashCode());
  }

  @Test(expected = NullPointerException.class)
  public void
  compareWithNullFails() {
    PhoneNumber number1 = PhoneNumber.parse("+353-86-3578380");
    number1.compareTo(null);
  }

  @Test
  public void
  testParts() {
    PhoneNumber number1 = PhoneNumber.parse("+353-86-3578380");
    Assert.assertEquals(353, number1.getCountryCode());
    Assert.assertEquals(86, number1.getNationalDialingCode());
    Assert.assertEquals(3578380, number1.getSubscriberNumber());
  }

  @Test
  public void
  test2Integer() {
    PhoneNumber number1 = PhoneNumber.parse("+353-86-3578380");
    PhoneNumber number2 = PhoneNumber.valueOf(353863578380L);
    Assert.assertEquals(number1, number2);
  }


  @Test
  public void
  testNewVodafoneNDCs() {
    PhoneNumber number1 = PhoneNumber.parse("+20 100 123 4567");
    Assert.assertSame(100, number1.getNationalDialingCode());
    PhoneNumber number2 = PhoneNumber.parse("+20 101 123 4567");
    Assert.assertSame(101, number2.getNationalDialingCode());
    PhoneNumber number3 = PhoneNumber.parse("+20 106 123 4567");
    Assert.assertSame(106, number3.getNationalDialingCode());
    PhoneNumber number4 = PhoneNumber.parse("+20 109 123 4567");
    Assert.assertSame(109, number4.getNationalDialingCode());
  }

  @Test
  public void
  testAntigua() {
    PhoneNumber antigua = PhoneNumber.parse("+2687239010");
    Assert.assertEquals("+2687239010", antigua.toString());
  }

  @Test
  public void
  testHierarchy() {
    // test Greece, Holland, Ireland
    PhoneNumber dutch = PhoneNumber.parse("31628000000");
    Assert.assertEquals("+31628000000", dutch.toString());

    PhoneNumber greek = PhoneNumber.parse("+30-22-323232");
    Assert.assertEquals("+3022323232", greek.toString());

    PhoneNumber irish = PhoneNumber.parse("00353863578380");
    Assert.assertEquals("+353863578380", irish.toString());

  }

  @Test
  public void shouldValidateSNByPattern() {
    NumberFactory.clearSchemes();
    try {
      NumberScheme newScheme = NumberScheme.create("2,2,6;CC=99;NDC=22;SN=9****0", "XX");
      NumberFactory.addScheme(newScheme);
      PhoneNumber number = PhoneNumber.valueOf(9922911110L);
      Assert.assertEquals ( 911110, number.getSubscriberNumber());
    } finally {
      NumberFactory.clearSchemes();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailInvalidSNByPattern() {
    NumberFactory.clearSchemes();
    try {
      NumberScheme newScheme = NumberScheme.create("2,2,6;CC=99;NDC=22;SN=9****0", "XX");
      NumberFactory.addScheme(newScheme);
      PhoneNumber.valueOf(9922911111L);
    } finally {
      NumberFactory.clearSchemes();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void
  testUnknownScheme() {
    PhoneNumber number1 = PhoneNumber.parse("380561234567");
    Assert.assertNull(number1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void guessFromLongAndFailBadNDC() {
    NumberFactory.fromLong(353811234567L);
  }

  @Test(expected = IllegalArgumentException.class)
  public void guessFromLongAndFailBadCC() {
    NumberFactory.fromLong(3548112345678L);
  }

  @Test(expected = IllegalArgumentException.class)
  public void guessFromLongAndFailBadSN() {
    NumberFactory.addScheme(NumberScheme.create("XX=3,2,7;CC=404;NDC=88;SN=1234567", "XX"));
    NumberFactory.fromLong(4048812345678L);
  }

}