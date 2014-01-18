package org.nulleins.phone;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternRule extends PartRule {

  private final ThreadLocal<Matcher> Validator = new ThreadLocal<>();
  private String pattern = "9(" + length + ")";

  PatternRule ( int length) {
    super(length);
  }

  @Override
  void set(String specification) {
    if ( specification.length() != length) {
      throw new IllegalArgumentException("Pattern must be same length as part");
    }
    this.pattern = specification;
    String testPattern = specification.replaceAll("\\*", "[\\\\d]");
    Validator.set( Pattern.compile(testPattern).matcher(""));
  }

  @Override
  boolean isValid(Integer value) {
    return Validator.get() == null ||
        Validator.get().reset(Integer.toString(value)).matches();
  }

  @Override
  public String toString() {
    return "match["+pattern+"]";
  }

  public String getPattern() {
    return Validator.get() != null ? Validator.get().pattern().toString() : "";
  }
}
