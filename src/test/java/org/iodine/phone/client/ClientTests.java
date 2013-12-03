package org.iodine.phone.client;

import org.iodine.phone.MSISDN;
import org.iodine.phone.MSISDNScheme;
import org.junit.Assert;
import org.junit.Test;

/** Test MSISDN purely through it's public interface, with no access
 * to its companion class */
public class ClientTests {

  private static final MSISDNScheme SCHEME = MSISDNScheme.create("2,3,10;CC=49;NDC=160,162,163,170-179", "DE.tmob+vfone");

  @Test
  public void canCreateMsisdn() {
    MSISDN number = SCHEME.fromLong(491620987654321L);
    Assert.assertNotNull(number);
  }
}
