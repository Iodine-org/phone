package org.iodine.phone.client;

import org.iodine.phone.MSISDNFactory;
import org.iodine.phone.MSISDNRule;
import org.iodine.phone.MSISDNScheme;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;

/** Test availability of Scheme metadata: useful for creating and validating UI inputs */
public class MetaDataTests {

  @Before
  public void loadTestSchemas() {
    MSISDNFactory.loadSchemesFromResource("/TestMsisdn.properties");
  }

  @Test
  public void canGetSchemeMetaData() {

    MSISDNScheme scheme = MSISDNFactory.getScheme("IE");
    MSISDNRule ccRule = scheme.getCCRule();
    Assert.assertEquals(3, ccRule.getLength());
    Assert.assertEquals(asList(353), ccRule.getValues());

    MSISDNRule ndcRule = scheme.getNDCRule();
    Assert.assertEquals(2, ndcRule.getLength());
    Assert.assertEquals(asList(82,83,85,86,87,88,89), ndcRule.getValues());

    MSISDNRule snRule = scheme.getSNRule();
    Assert.assertEquals(7, snRule.getLength());
  }
}
