package org.nulleins.phone.client;

import org.nulleins.phone.NumberFactory;
import org.nulleins.phone.NumberScheme;
import org.nulleins.phone.PhoneNumber;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializationTests {

  private static final NumberScheme SCHEME = NumberScheme.create("CC=2:49;NDC=3:160,162,163,170-179;SN=10");

  @Test
  public void recreatesSchemeTransient() throws Exception {
    // create a PhoneNumber from an explicit scheme (not registered with the factory singleton)
    PhoneNumber number = SCHEME.fromLong(491601234567890L);
    Assert.assertNotNull(number);

    byte[] serialized = serialize(number);

    // add the scheme to the factory, so that it is found on re-creation
    NumberFactory.addScheme(SCHEME);
    Object object = deserialize(serialized);
    Assert.assertEquals ( PhoneNumber.class, object.getClass());
    PhoneNumber readBack = (PhoneNumber)object;
    Assert.assertEquals ( number, readBack);
    Assert.assertEquals ( SCHEME, readBack.getScheme());
  }

  @Test(expected = IllegalStateException.class)
  public void cannotRecreateFromLongValue() throws Exception {
    PhoneNumber number = SCHEME.fromLong(491601234567890L);
    Assert.assertNotNull(number);
    NumberFactory.clearSchemes();

    long longValue = number.longValue();
    PhoneNumber.valueOf(longValue);
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
