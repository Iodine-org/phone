package org.iodine.phone;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import static org.iodine.phone.NumberScheme.PartCode;

/**
 * Creates PhoneNumber numbers based on registered schemes, loaded from a resource named
 * <code>NumberScheme.properties</code>
 * <p/>
 * The file format consists of a unique key (usually the ISO country code, and optionally, MNO name and/or a
 * discriminator), followed by a specification of the the number scheme, for example:
 * <pre>
 *   EG.9=2,2,7;CC=20;NDC=10,11,12,14,16,17,18,19
 * </pre>
 * Describes a PhoneNumber scheme where the country code part is "20", the NDC part is two digits long and in the
 * specified set, and the subscriber number is seven digits long
 * <p/>
 * This is the 'companion class' (public) that Josh Bloch recommends when working with immutable value objects,
 * to perform factory type operations, such as value construction
 *
 * @author roy.phiilips
 */
public class NumberFactory {

  /** loads PhoneNumber specifications from file named by this property */
  public static final String MSISDN_PROPERTIES_DEFAULT = "/NumberScheme.properties";
  /** known PhoneNumber schemes from property file */
  private static final Map<Integer, NumberScheme> schemes = new HashMap<>();
  static {
    loadDefaultScheme();
  }
  /** exception message prefixes (public for testability) */
  public static final String UNRECOGNIZED_SCHEME = "PhoneNumber Unrecognized scheme";

  /**
   * PhoneNumber factory method to match the input string against the known
   * set of PhoneNumber schemes and instantiate an PhoneNumber object
   *
   * @param msisdnString from which to initialize the result
   * @return PhoneNumber create from the input string
   * @throws IllegalArgumentException if the parameter is not a valid and known PhoneNumber
   */
  static PhoneNumber createMSISDN(String msisdnString) {
    if (msisdnString == null || msisdnString.trim().length() == 0) {
      throw new IllegalArgumentException(
          "PhoneNumber candidate string must be non-null and non-empty: " + msisdnString);
    }
    if ( schemes.size() == 0) {
      throw new IllegalStateException ( "No PhoneNumber schemes registered");
    }
    String candidate = normalize(msisdnString);
    PhoneNumber result = null;
    for (int ccSize = 3; ccSize > 0 && result == null; ccSize--) {
      result = lookupByCC(ccSize, candidate);
    }
    if (result == null) {
      throw new IllegalArgumentException(
          UNRECOGNIZED_SCHEME + " for: " + msisdnString
              + " (known schemes=" + schemes.values() + ")");
    }
    return result;
  }

  /**
   * @return a registered scheme that matches the supplied values
   * @param cc country code of the returned scheme
   * @param length of the number
   */
  public static NumberScheme getSchemeForCC(int cc, int length) {
    return schemes.get(createKey(cc, length));
  }

  /**
   * Look-up the scheme for the country code represented by the first <code>ccSize</code>
   * digits of the supplied string, continuing to match the NDC and SN if found, and
   * creating the appropriate PhoneNumber
   *
   * @param ccSize    assumed size of country code prefix
   * @param candidate the normalized PhoneNumber string
   * @return a PhoneNumber version of the candidate string, if valid, else null
   */
  private static PhoneNumber lookupByCC(int ccSize, String candidate) {
    if (candidate.length() < ccSize) {
      return null;
    }
    int tryCC = Integer.valueOf(candidate.substring(0, ccSize));
    NumberScheme scheme = schemes.get(createKey(tryCC, candidate.length()));
    if (scheme == null) {
      return null;
    }
    PartRule ndcRule = scheme.rules.get(PartCode.NDC);
    int tryNDC = Integer.valueOf(candidate.substring(ccSize, ccSize + ndcRule.length));
    if (ndcRule.isValid(tryNDC) == false) {
      return null;
    }
    PartRule snRule = scheme.rules.get(PartCode.SN);
    String snString = candidate.substring(ccSize + ndcRule.length);
    int sn = Integer.valueOf(snString);
    if (snRule.isValid(sn) == false) {
      return null;
    }
    return PhoneNumber.create(tryCC, tryNDC, sn, scheme);
  }

  /**
   * process a external PhoneNumber string into a normalized string, removing
   * non-digits, and country code indicators ('+', '00') if present
   *
   * @param msisdnString possibly non-normalized
   * @return normalized copy of the input string
   */
  private static String normalize(String msisdnString) {
    return msisdnString
        .replaceAll("[^\\d]", "")
        .replaceFirst("^00", "");
  }


  /** @return a PhoneNumber created from the supplied number
   *  @param value a numeric representation of an PhoneNumber */
  public static PhoneNumber fromLong(long value) {
    return createMSISDN(value + "");
  }

  /**
   * Iterate over the schemes in the supplied properties map, parsing each
   * definition and storing it against the scheme key in the resulting map
   *
   * @param schemes defined as property key/value pairs
   * @return map of scheme keys (hash of country code + length) to definitions
   */
  private static Map<Integer, NumberScheme> getSchemeMap(Properties schemes) {
    assert schemes.size() > 0;
    Map<Integer, NumberScheme> result = new HashMap<>();
    for (Entry<Object, Object> entry : schemes.entrySet()) {
      NumberScheme scheme = NumberScheme.create((String) entry.getValue());
      scheme.setName((String) entry.getKey());
      SetRule ccRule = (SetRule) scheme.rules.get(PartCode.CC);
      Integer cc = (Integer) ccRule.values.toArray()[0];
      result.put(createKey(cc, scheme.length), scheme);
    }
    assert result.size() == schemes.size();
    return result;
  }

  /** @return key to the scheme map is CC left-shifted by four bits plus the PhoneNumber length-1, this
   *    allows up to 15 digits lengths to be specified with an arbitrarily long country code
   *    E.g., 353 + 11 => 0x161b, or 1 + 14 (max US number):  0x001e */
  private static Integer createKey(int countryCode, int length) {
    return (countryCode << 4) + Math.abs(length - 1);
  }

  /** Clear the currently registered PhoneNumber schemes in this singleton,
   *  and reload the scheme definitions from the resource supplied
   * @param schemeResource location (e.g., filename, URL) of PhoneNumber scheme definitions */
  private static void loadScheme(String schemeResource, Map<Integer, NumberScheme> schemes) throws IOException {
    schemes.clear();
    final Properties properties = new Properties();
    InputStream input = schemes.getClass().getResourceAsStream(schemeResource);
    if ( input == null) {
      throw new IOException("could not open " + schemeResource);
    }
    properties.load(input);
    schemes.putAll(getSchemeMap(properties));
  }

  /** load schemes from the default definition files,
   *  <code>MSISDN_PROPERTIES_DEFAULT</code>, if it exists
   *   on the classpath, otherwise does nothing */
  public static void loadDefaultScheme() {
    loadSchemesFromResource(MSISDN_PROPERTIES_DEFAULT);
  }

  /** load schemes from the specified definition files, <code>schemeResource</code>,
   *  if it exists on the classpath, otherwise does nothing */
  public static void loadSchemesFromResource(String schemeResource) {
    schemes.clear();
    try {
      loadScheme(schemeResource, schemes);
    } catch (IOException e) {
      // scheme not present, not necessarily an error
    }
  }

  /** remove any schemes registered with this factory */
  public static void clearSchemes() {
    schemes.clear();
  }

  /** register the schemes in the supplied list with this factory
   *  @param schemes to be added to the factory */
  public static void addSchemes(List<NumberScheme> schemes) {
    for ( NumberScheme scheme : schemes) {
      addScheme ( scheme);
    }
  }

  /** register the scheme supplied with this factory
   *  @param scheme to be added to the factory */
  public static void addScheme(NumberScheme scheme) {
    schemes.put(scheme.getKey(), scheme);
  }

  /** @return the scheme with <code>name</code> matching the supplied
   *  value, otherwise <code>null</code>
   *  @param label to look-up */
  public static NumberScheme getScheme(String label) {
    for ( NumberScheme scheme : schemes.values()) {
      if ( label.equals(scheme.getName()) == true) {
        return scheme;
      }
    }
    return null;
  }

  public static Set<Integer> getCountryCodes() {
    Set<Integer> result = new HashSet<>(schemes.size());
    for ( NumberScheme scheme : schemes.values()) {
      result.addAll(scheme.getCCRule().values);
    }
    return result;
  }

  public static Set<Integer> getAreaCodes(int countryCode) {
    Set<Integer> result = new HashSet<>(schemes.size());
    for ( NumberScheme scheme : schemes.values()) {
      if ( scheme.getCCRule().values.contains(countryCode)) {
        result.addAll(scheme.getNDCRule().values);
      }
    }
    return result;
  }
}