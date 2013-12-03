package org.iodine.phone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestMSISDNSchemes {
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
    MSISDNFactory.loadSchemesFromResource("/TestMsisdn.properties");
  }

  @Test
  public void
  testGuessSchemeIE() {
    MSISDN number = MSISDN.parse("+353-86-3578380");
    Assert.assertEquals(353, number.getCC());
  }

  @Test
  public void
  testGuessScheme() {
    Assert.assertEquals("+201500000001", MSISDN.valueOf(201500000001L).toString());
    Assert.assertEquals("+44865249864", MSISDN.parse("+44.865.249.864").toString());
    MSISDN nb = MSISDN.parse("+234901220887");
    Assert.assertEquals("+234901220887", nb.toString());
  }

  @Test
  public void
  testEGNumbers() {
    for (String number : testNumbersEG) {
      MSISDN msisdn = MSISDN.parse(number);
      Assert.assertEquals(20, msisdn.getCC());
    }
  }

  @Test
  public void
  testEGNumbers2() {
    for (String number : testMsisdns) {
      MSISDN msisdn = MSISDN.parse(number);
      Assert.assertEquals(20, msisdn.getCC());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void
  testCountryOnly() {
    MSISDN.parse("+44");
  }

  @Test(expected = IllegalArgumentException.class)
  public void
  testCountryPlusNDCOnly() {
    MSISDN.parse("+35396");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailIfLongInvalid() {
    MSISDN.valueOf(0L);
  }

  @Test
  public void
  testSortList() {
    List<MSISDN> list = new ArrayList<>();
    for (String number : testNumbersEG) {
      MSISDN msisdn = MSISDN.parse(number);
      list.add(msisdn);
    }
    Collections.sort(list);
    MSISDN[] array = list.toArray(new MSISDN[0]);
    Assert.assertEquals("+20101233576", array[0].toString());
    Assert.assertEquals("+201529135653", array[array.length - 1].toString());
  }

  @Test
  public void
  testGuessSchemeUS() {
    MSISDN usNumber = MSISDN.parse("1 855 784-9261");
    Assert.assertEquals("+18557849261", usNumber.toString());
    Assert.assertEquals(1, usNumber.getCC());
    Assert.assertEquals(855, usNumber.getNDC());
    Assert.assertEquals(7849261, usNumber.getSN());
  }

  @Test
  public void
  testEquality() {
    MSISDN number1 = MSISDN.parse("+353-86-3578380");
    MSISDN number2 = MSISDN.valueOf(353863578380L);
    Assert.assertEquals(number1, number2);
    Assert.assertNotSame(number1, number2);
    Assert.assertTrue(number1.equals(number2) && number2.equals(number1));
    Assert.assertTrue(number1.hashCode() == number2.hashCode());
  }

  @Test
  public void
  testParts() {
    MSISDN number1 = MSISDN.parse("+353-86-3578380");
    Assert.assertEquals(353, number1.getCC());
    Assert.assertEquals(86, number1.getNDC());
    Assert.assertEquals(3578380, number1.getSN());
  }

  @Test
  public void
  test2Integer() {
    MSISDN number1 = MSISDN.parse("+353-86-3578380");
    MSISDN number2 = MSISDN.valueOf(353863578380L);
    Assert.assertEquals(number1, number2);
  }


  @Test
  public void
  testNewVodafoneNDCs() {
    MSISDN number1 = MSISDN.parse("+20 100 123 4567");
    Assert.assertSame(100, number1.getNDC());
    MSISDN number2 = MSISDN.parse("+20 101 123 4567");
    Assert.assertSame(101, number2.getNDC());
    MSISDN number3 = MSISDN.parse("+20 106 123 4567");
    Assert.assertSame(106, number3.getNDC());
    MSISDN number4 = MSISDN.parse("+20 109 123 4567");
    Assert.assertSame(109, number4.getNDC());
  }

  @Test
  public void
  testAntigua() {
    MSISDN antigua = MSISDN.parse("+2687239010");
    Assert.assertEquals("+2687239010", antigua.toString());
  }

  @Test
  public void
  testHierarchy() {
    // test Greece, Holland, Ireland
    MSISDN dutch = MSISDN.parse("31628000000");
    Assert.assertEquals("+31628000000", dutch.toString());

    MSISDN greek = MSISDN.parse("+30-22-323232");
    Assert.assertEquals("+3022323232", greek.toString());

    MSISDN irish = MSISDN.parse("00353863578380");
    Assert.assertEquals("+353863578380", irish.toString());

  }

  @Test(expected = IllegalArgumentException.class)
  public void
  testUnknownScheme() {
    MSISDN number1 = MSISDN.parse("380561234567");
    Assert.assertNull(number1);
  }


}