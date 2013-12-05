package org.iodine.phone.client;

import org.iodine.phone.NumberFactory;
import org.iodine.phone.PartRule;
import org.iodine.phone.NumberScheme;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;

/** Test availability of Scheme metadata: useful for creating and validating UI inputs */
public class MetaDataTests {

  @Before
  public void loadTestSchemas() {
    NumberFactory.loadSchemesFromResource("/TestMsisdn.properties");
  }

  @Test
  public void canGetSchemeMetaData() {

    NumberScheme scheme = NumberFactory.getScheme("IE");
    PartRule ccRule = scheme.getCCRule();
    Assert.assertEquals(3, ccRule.getLength());
    Assert.assertEquals(asList(353), ccRule.getValues());

    PartRule ndcRule = scheme.getNDCRule();
    Assert.assertEquals(2, ndcRule.getLength());
    Assert.assertEquals(asList(82,83,85,86,87,88,89), ndcRule.getValues());

    PartRule snRule = scheme.getSNRule();
    Assert.assertEquals(7, snRule.getLength());
  }
}
