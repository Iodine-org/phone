package org.iodine.phone.client;

import org.iodine.phone.MSISDN;
import org.iodine.phone.MSISDNFactory;
import org.iodine.phone.MSISDNScheme;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializationTests {

  private static final MSISDNScheme SCHEME = MSISDNScheme.create("2,3,10;CC=49;NDC=160,162,163,170-179", "DE.tmob+vfone");

  @Test
     public void recreatesSchemeTransient() throws Exception {
    // create a MSISDN from an explicit scheme (not registered with the factory singleton)
    MSISDN number = SCHEME.fromLong(491601234567890L);
    Assert.assertNotNull(number);

    byte[] serialized = serialize(number);

    // add the scheme to the factory, so that it is found on re-creation
    MSISDNFactory.addScheme(SCHEME);
    Object object = deserialize(serialized);
    Assert.assertEquals ( MSISDN.class, object.getClass());
    MSISDN readBack = (MSISDN)object;
    Assert.assertEquals ( number, readBack);
    Assert.assertEquals ( SCHEME, readBack.getScheme());
  }

  @Test(expected = IllegalStateException.class)
  public void cannotRecreateFromLongValue() throws Exception {
    MSISDN number = SCHEME.fromLong(491601234567890L);
    Assert.assertNotNull(number);
    MSISDNFactory.clearSchemes();

    long longValue = number.longValue();
    MSISDN.valueOf(longValue);
  }

  private byte[] serialize ( Object data) throws Exception {
    try ( ByteArrayOutputStream baos = new ByteArrayOutputStream();
          ObjectOutputStream out = new ObjectOutputStream(baos)) {
      out.writeObject(data);
      return baos.toByteArray();
    } catch ( Exception e){
      Assert.fail(e.getMessage());
      throw e;
    }
  }

  private Object deserialize ( byte[] data) throws Exception {
    try ( ByteArrayInputStream biis = new ByteArrayInputStream(data);
          ObjectInputStream in = new ObjectInputStream(biis)) {
      return in.readObject();
    } catch ( Exception e){
      Assert.fail(e.getMessage());
      throw e;
    }
  }


}
