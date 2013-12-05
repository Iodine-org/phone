package org.iodine.phone;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Description of an PhoneNumber scheme (e.g., country/network operator) for a mobile subscriber
 * number, specifying the country code, national dialing code and subscriber number
 *
 * @see PhoneNumber
 *
 * @author roy.phillips
 */
public class NumberScheme {

  enum PartCode {
    CC(0), NDC(1), SN(2);
    final int index;
    PartCode(int idx) { index = idx;}
  }
  /** exception message prefixes (public for testability) */
  public static final String INVALID_SCHEMA_PART_LENGTH = "Invalid part length ( <= 0 ) at index ";
  /** per-scheme instance values */
  final Map<PartCode, PartRule> rules;
  final String name;
  final int length;
  final long ccfactor; // factor to decode CC from a long value
  final long ndcfactor; // factor to decode NDC from a long value

  /**
   * Constructor called from the static scheme loader to instantiate a scheme
   * from the parsed specification, as represented by the parameters below
   *
   * @param schemeName unique name of the scheme (key)
   * @param rules      set of rules defining the parts
   * @param length     total length of a number in this scheme
   */
  private NumberScheme(String schemeName, Map<PartCode, PartRule> rules, int length) {
    this.name = schemeName;
    this.length = length;
    this.rules = rules;
    int snlength = rules.get(PartCode.SN).length;
    int ndcLength = rules.get(PartCode.NDC).length;
    this.ccfactor = (long) Math.pow(10, ndcLength + snlength);
    this.ndcfactor = (long) Math.pow(10, snlength);
  }

  /** @return a new PhoneNumber for the supplied number, in this scheme
    * @throws java.lang.IllegalArgumentException if <code>number</code>
    *           is not valid in this scheme */
  public PhoneNumber fromLong(long number) {
    PhoneNumber result = PhoneNumber.fromLong(number, this);
    if ( isValid(result) == false) {
      throw new IllegalArgumentException(number + " not valid in scheme: " + this);
    }
    return result;
  }

  /** @return true is <code>phoneNumber</code> is a valid PhoneNumber as defined by this scheme
    * @param phoneNumber number */
  public boolean isValid(PhoneNumber phoneNumber) {
    return isValid(phoneNumber.getCountryCode(), phoneNumber.getNationalDialingCode(), phoneNumber.getSubscriberNumber());
  }

  /** @return true if all supplied PhoneNumber elements represent a valid PhoneNumber for this scheme
    * @param countryCode value of the country code prefix
    * @param nationalDialingCode value of the national/operator code
    * @param subscriberNumber the subscriber (final) portion of the number */
  boolean isValid(int countryCode, int nationalDialingCode, int subscriberNumber) {
    return rules.get(PartCode.CC).isValid(countryCode)
        && rules.get(PartCode.NDC).isValid(nationalDialingCode)
        && rules.get(PartCode.SN).isValid(subscriberNumber);
  }

  @Override
  public String toString() {
    return name + "=" + rules.toString();
  }

  /**
   * Create a NumberScheme object representing the scheme specification
   * passed in string form
   * <p/>
   * Example specification:
   * <pre>
   *   "2,2,7;CC=20;NDC=10,11,12,14,16,17,18,19"
   * </pre>
   * defines an PhoneNumber scheme where the country code is "20", the national
   * dialing code is in the supplied set of values and the subscriber number
   * is seven digits long, and the complete number is eleven digits long
   *
   * @param specification string describing the scheme
   * @param schemeName    identifying the scheme
   * @return a new scheme initialized from the supplied specification
   */
  public static NumberScheme create(final String specification, String schemeName) {
    final String[] spec = specification.replaceAll(" ","").split(";");
    assert spec.length > 0 : "At minimum, parts lengths must specified";
    Map<PartCode, PartRule> rules = new HashMap<>();

    int schemeLength = getPartLengths(rules, spec[0]);
    assert schemeLength <= 15 : "PhoneNumber in fifteen-digit numbering space";
    setPartRules(rules, Arrays.copyOfRange(spec, 1, spec.length));

    return new NumberScheme(schemeName, rules, schemeLength);
  }

  /**
   * For each part, set the length of that part, as defined by the supplied
   * specification string, in the supplied <code>rules</code> map
   *
   * @param lengthSpec comma-separated part length specification (e.g., "2,2,7")
   * @param rules      map of rule objects, one for each part: CC, NDC and SN
   * @return the total length of the PhoneNumber of the specified scheme
   */
  private static int getPartLengths(Map<PartCode, PartRule> rules, final String lengthSpec) {
    final String[] partsLengths = lengthSpec.split(",");
    assert partsLengths.length == 3 : "Requires lengths of all three parts (got: "+lengthSpec+")";
    final PartCode[] keys = {PartCode.CC, PartCode.NDC, PartCode.SN};
    int result = 0;
    for ( PartCode part : keys) {
      int length = Integer.parseInt(partsLengths[part.index]);
      if (length <= 0) {
        throw new IllegalArgumentException(INVALID_SCHEMA_PART_LENGTH + part + " in " + lengthSpec);
      }
      if ( part == PartCode.SN) {
        rules.put(part, new PatternRule(length));
      } else {
        rules.put(part, new PartRule(length));
      }
      result += length;
    }
    return result;
  }

  /**
   * For each part, assign the rule from the specification array supplied
   * (this is the set of allowed values for that part)
   *
   * @param valueSet array of allowed values
   * @param rules    for the parts of the PhoneNumber
   */
  private static void setPartRules(Map<PartCode, PartRule> rules, final String[] valueSet) {
    for (String partSpec : valueSet) {
      final String[] rule = partSpec.split("=");
      assert rule.length == 2 : "part is " + partSpec;
      rules.get(PartCode.valueOf(rule[0])).set(rule[1]);
    }
  }

  /**
   * @param cc  country code
   * @param ndc national dialing code
   * @param sn  subscriber number
   * @return a PhoneNumber number belonging to this scheme constructed from
   *         the parameters supplied
   */
  long longValue(int cc, int ndc, int sn) {
    return (cc * ccfactor) + (ndc * ndcfactor) + sn;
  }

  /** @return a generated unique key for this scheme<p/>
   * Key to the scheme map is CC left-shifted by four bits plus the PhoneNumber length-1, this
   * allows up to 15 digits lengths to be specified with an arbitrarily long country code
   * E.g., 353 + 11 => 0x161b, or 1 + 14 (max US number):  0x001e */
  private static Integer createKey(int countryCode, int length) {
    return (countryCode << 4) + Math.abs(length - 1);
  }

  /** @return the unique key generated for this scheme */
  int getKey() {
    Set<Integer> ccRule = rules.get(PartCode.CC).values;
    return createKey(ccRule.iterator().next(), this.length);
  }

  public String getName() {
    return name;
  }

  public PartRule getCCRule() {
    return rules.get(PartCode.CC);
  }

  public PartRule getNDCRule() {
    return rules.get(PartCode.NDC);
  }

  public PartRule getSNRule() {
    return rules.get(PartCode.SN);
  }

}