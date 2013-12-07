package org.iodine.phone.client;

import org.iodine.phone.InitalizationTests;
import org.iodine.phone.NumberFactory;
import org.iodine.phone.NumberScheme;
import org.iodine.phone.PatternRule;
import org.iodine.phone.SetRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.iodine.phone.InitalizationTests.createTempSchemeFile;

/** Test availability of Scheme metadata: useful for creating and validating UI inputs */
public class MetaDataTests {

  public static final String CLASSPATH_PROPERTY_FILE
      = "out/test/iodine-phone/NumberScheme.properties";

  @Before
  public void loadTestSchemas() {
    NumberFactory.loadSchemesFromResource("/TestMsisdn.properties");
  }

  @Test
  public void canGetSchemeMetaData() {

    NumberScheme scheme = NumberFactory.getScheme("IE");
    SetRule ccRule = scheme.getCCRule();
    Assert.assertEquals(3, ccRule.getLength());
    Assert.assertEquals(asList(353), ccRule.getValues());

    SetRule ndcRule = scheme.getNDCRule();
    Assert.assertEquals(2, ndcRule.getLength());
    Assert.assertEquals(asList(82,83,85,86,87,88,89), ndcRule.getValues());

    PatternRule snRule = scheme.getSNRule();
    Assert.assertEquals(7, snRule.getLength());
  }

  @Test
  public void metaDataWithPatternWorks() {

    NumberScheme scheme = NumberFactory.getScheme("GR");
    SetRule ccRule = scheme.getCCRule();
    Assert.assertEquals(2, ccRule.getLength());
    Assert.assertEquals(asList(30), ccRule.getValues());

    SetRule ndcRule = scheme.getNDCRule();
    Assert.assertEquals(2, ndcRule.getLength());
    Assert.assertEquals(asList(22), ndcRule.getValues());

    PatternRule snRule = scheme.getSNRule();
    Assert.assertEquals(6, snRule.getLength());
    Assert.assertEquals("32[\\d][\\d][\\d][\\d]", snRule.getPattern());
  }

  @Test
  public void canListRegisteredCCandNDCs() throws IOException {
    final Set<Integer> expectCCs
        = new HashSet<>(asList(new Integer[]{353, 44, 1}));
    final Set<Integer> expectNDCs
        = new HashSet<>(asList(new Integer[]{83, 82, 85, 89, 87, 88, 86}));
    try {
      createTempSchemeFile ( InitalizationTests.SCHEME_SPEC, CLASSPATH_PROPERTY_FILE);
      NumberFactory.loadDefaultScheme();

      Assert.assertEquals(expectCCs, NumberFactory.getCountryCodes());
      Assert.assertEquals(expectNDCs, NumberFactory.getAreaCodes(353));
      Assert.assertEquals ( 1, NumberFactory.getSchemesForLocale(Locale.US).size());
    } finally {
      NumberFactory.clearSchemes();
    }
  }
  
  
}
