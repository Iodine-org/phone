package org.nulleins.phone.client;

import org.nulleins.phone.PhoneNumber;
import org.nulleins.phone.NumberScheme;
import org.junit.Assert;
import org.junit.Test;

/** Test PhoneNumber purely through it's public interface, with no access
 *  to its companion class */
public class ClientTests {

  private static final NumberScheme SCHEME = NumberScheme.create("CC=2:49;NDC=3:160,162,163,170-179;SN=10");

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
