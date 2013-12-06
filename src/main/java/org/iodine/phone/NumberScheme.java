package org.iodine.phone;

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
  public enum SchemeType {
    UNDEFINED, FIXED_LINE, MOBILE, TOLL_FREE, PREMIUM_RATE, SHARE_COST, VOIP, PERSONAL
  }
  /** exception message prefixes (public for testability) */
  public static final String INVALID_SCHEMA_PART_LENGTH = "Invalid part length ( <= 0 ) at index ";
  /** per-scheme instance values */
  final Map<PartCode, PartRule> rules;
  final String name;
  final int length;
  final long ccfactor; // factor to decode CC from a long value
  final long ndcfactor; // factor to decode NDC from a long value
  private SchemeType type = SchemeType.UNDEFINED;

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
    return name + "=" + rules.toString() +
        (type != null && type != SchemeType.UNDEFINED ? ", type=" + type : "");
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
    Map<String,String> specMap = getSpecMap(specification);
    assert specMap.size() > 0 : "At minimum, parts lengths must specified";

    SchemeType type = SchemeType.UNDEFINED;
    if ( specMap.containsKey("TYPE")) {
      type = SchemeType.valueOf(specMap.get("TYPE"));
    }
    Map<PartCode, PartRule> rules = getPartRules(specMap);
    if ( rules.size() != 3) {
      throw new IllegalArgumentException("Expected rules for CC, NDC and SN, got[" + specification + "]");
    }
    int schemeLength =
        rules.get(PartCode.CC).getLength() +
        rules.get(PartCode.NDC).getLength() +
        rules.get(PartCode.SN).getLength();
    NumberScheme result = new NumberScheme(schemeName, rules, schemeLength);
    result.setType ( type);
    return result;
  }

  private static Map<String,String> getSpecMap(String specification) {
    String[] parts = specification.replaceAll(" ", "").toUpperCase().split(";");
    Map<String,String> result = new HashMap<>(parts.length);
    for ( String part : parts) {
      String[] spec = part.split("=");
      if ( spec.length != 2) {
        throw new IllegalArgumentException ( "Expected Key=Value, got [" + specification + "]");
      }
      result.put(spec[0], spec[1]);
    }
    return result;
  }

  /**
   * For each part, assign the rule from the specification array supplied
   * (this is the set of allowed values for that part)
   *
   * @param valueSet array of allowed values
   */
  private static Map<PartCode, PartRule> getPartRules(final Map<String, String> valueSet) {
    Map<PartCode, PartRule> result = new HashMap<>();
    for ( PartCode part : new PartCode[] {PartCode.CC, PartCode.NDC, PartCode.SN}) {
      if ( valueSet.containsKey(part.toString())) {
        String[] partSpec = valueSet.get(part.toString()).split(":");
        int length = Integer.parseInt(partSpec[0]);
        if (length <= 0) {
          throw new IllegalArgumentException(INVALID_SCHEMA_PART_LENGTH
              + part + " in " + partSpec);
        }
        PartRule rule;
        if ( part == PartCode.SN) {
          rule = new PatternRule(length);
        } else {
          rule = new SetRule(length);
        }
        if ( partSpec.length > 1) {
          rule.set(partSpec[1]);
        }
        result.put(part, rule);
      }
    }
    return result;
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
    SetRule ccRule = (SetRule)rules.get(PartCode.CC);
    Set<Integer> values = ccRule.values;
    return createKey(values.iterator().next(), this.length);
  }

  public void setType ( SchemeType type) {
    this.type = type;
  }

  public SchemeType getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public SetRule getCCRule() {
    return (SetRule)rules.get(PartCode.CC);
  }

  public SetRule getNDCRule() {
    return (SetRule)rules.get(PartCode.NDC);
  }

  public PatternRule getSNRule() {
    return (PatternRule)rules.get(PartCode.SN);
  }

  public static SchemeBuilder SchemeBuilder() {
    return new SchemeBuilder();
  }

  public static class SchemeBuilder {
    private String label;
    private String cc;
    private String ndc;
    private String sn;
    private SchemeType type;

    public SchemeBuilder label(String label) {
      this.label = label;
      return this;
    }

    public SchemeBuilder cc(String cc) {
      this.cc = cc;
      return this;
    }

    public SchemeBuilder ndc(String ndc) {
      this.ndc = ndc;
      return this;
    }

    public SchemeBuilder sn(String sn) {
      this.sn = sn;
      return this;
    }

    public SchemeBuilder type(SchemeType type) {
      this.type = type;
      return this;
    }

    public NumberScheme build() {
      String spec = "CC=" + cc + ";NDC=" + ndc + ";SN=" + sn;
      NumberScheme result = NumberScheme.create(spec, label);
      if ( type != null) {
        result.setType(type);
      }
      return result;
    }
  }

}