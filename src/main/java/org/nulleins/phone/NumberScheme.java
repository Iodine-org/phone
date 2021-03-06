package org.nulleins.phone;

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
  String name;
  String iso3166;
  final int length;
  final long ccfactor; // factor to decode CC from a long value
  final long ndcfactor; // factor to decode NDC from a long value
  private SchemeType type = SchemeType.UNDEFINED;

  /**
   * Constructor called from the static scheme loader to instantiate a scheme
   * from the parsed specification, as represented by the parameters below
   *
   * @param rules      set of rules defining the parts
   * @param length     total length of a number in this scheme
   */
  private NumberScheme(Map<PartCode, PartRule> rules, int length) {
    this.length = length;
    this.rules = rules;
    int snlength = rules.get(PartCode.SN).length;
    int ndcLength = rules.get(PartCode.NDC).length;
    this.ccfactor = (long) Math.pow(10, ndcLength + snlength);
    this.ndcfactor = (long) Math.pow(10, snlength);
  }

  public void setName(String name) {
    this.name = name;
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
   * @return a new scheme initialized from the supplied specification
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
   */
  public static NumberScheme create(final String specification) {
    Map<String,String> specMap = getSpecMap(specification);
    assert specMap.size() > 0 : "At minimum, parts lengths must specified";

    Map<PartCode, PartRule> rules = getPartRules(specMap);
    if ( rules.size() != 3) {
      throw new IllegalArgumentException("Expected rules for CC, NDC and SN, got[" + specification + "]");
    }
    int schemeLength =
        rules.get(PartCode.CC).getLength() +
        rules.get(PartCode.NDC).getLength() +
        rules.get(PartCode.SN).getLength();
    NumberScheme result = new NumberScheme(rules, schemeLength);
    SchemeType type = SchemeType.UNDEFINED;
    if ( specMap.containsKey("TYPE")) {
      type = SchemeType.valueOf(specMap.get("TYPE"));
    }
    result.setType ( type);
    if ( specMap.containsKey("ISO3166")) {
      result.iso3166 = specMap.get("ISO3166");
    }
    return result;
  }

  /** @return a mapping of the specification as key/value pairs */
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

  /** @return a mapping of the scheme part codes to the part rules defined
   *  For each part, assign the rule from the specification array supplied
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

  /** @return a PhoneNumber number belonging to this scheme constructed from
   *            the parameters supplied
   * @param cc  country code
   * @param ndc national dialing code
   * @param sn  subscriber number
   */
  long longValue(int cc, int ndc, int sn) {
    return (cc * ccfactor) + (ndc * ndcfactor) + sn;
  }

  /** @return a generated unique key for this scheme<p/>
   * Key to the scheme map is CC left-shifted by four bits plus the PhoneNumber length-1, this
   * allows up to 15 digits lengths to be specified with an arbitrarily long country code
   * E.g., 353 + 11 => 0x161b, or 1 + 14 (max US number):  0x001e */
  static Integer createKey(int countryCode, int length) {
    return (countryCode << 4) + Math.abs(length - 1);
  }

  /** @return the unique key generated for this scheme */
  int getKey() {
    SetRule ccRule = (SetRule)rules.get(PartCode.CC);
    Set<Integer> values = ccRule.values;
    return createKey(values.iterator().next(), this.length);
  }

  /** set the type of this number scheme to <code>type</code> */
  public void setType ( SchemeType type) {
    this.type = type;
  }

  /** @return type (category) of the numbers represented by this scheme */
  public SchemeType getType() {
    return type;
  }
  /** @return the ISO 3166-1-alpha-2 country code, if set, else null */
  public String getIso3166() {
    return iso3166;
  }
  /** @return the name this scheme has been tagged with, else null */
  public String getName() {
    return name;
  }

  /** @return the rule describing the country dialing code part */
  public SetRule getCCRule() {
    return (SetRule)rules.get(PartCode.CC);
  }

  /** @return the rule describing the national dialing code part */
  public SetRule getNDCRule() {
    return (SetRule)rules.get(PartCode.NDC);
  }

  /** @return the rule describing the subscriber number part */
  public PatternRule getSNRule() {
    return (PatternRule)rules.get(PartCode.SN);
  }

  /** @return a new builder that can be used to construct a NumberScheme */
  public static SchemeBuilder Builder() {
    return new SchemeBuilder();
  }

  /** Builder for constructing NumberScheme objects */
  public static class SchemeBuilder {
    private String label;
    private String cc;
    private String ndc;
    private String sn;
    private SchemeType type;
    private String iso3166;

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

    public SchemeBuilder iso3166(String iso3166) {
      this.iso3166 = iso3166;
      return this;
    }

    public NumberScheme build() {
      String spec = "CC=" + cc + ";NDC=" + ndc + ";SN=" + sn;
      NumberScheme result = NumberScheme.create(spec);
      if ( label != null) {
        result.setName(label);
      }
      if ( type != null) {
        result.setType(type);
      }
      if ( iso3166 != null) {
        result.iso3166 = iso3166;
      }
      return result;
    }
  }

}