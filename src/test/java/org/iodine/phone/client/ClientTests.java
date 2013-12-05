package org.iodine.phone.client;

import org.iodine.phone.PhoneNumber;
import org.iodine.phone.NumberScheme;
import org.junit.Assert;
import org.junit.Test;

/** Test PhoneNumber purely through it's public interface, with no access
 *  to its companion class */
public class ClientTests {

  private static final NumberScheme SCHEME = NumberScheme.create("2,3,10;CC=49;NDC=160,162,163,170-179", "DE.tmob+vfone");

  @Test
  public void canCreateMsisdn() {
    PhoneNumber number = SCHEME.fromLong(491620987654321L);
    Assert.assertNotNull(number);
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidLongFails() {
    SCHEME.fromLong(0L);
  }
}
