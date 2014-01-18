package org.nulleins.phone;

import java.util.Set;
import java.util.TreeSet;

/** Defines the content rule for one part of a PhoneNumber number, in terms
 *  of its length and, optionally, the domain of values valid for the part */
public abstract class PartRule {
  final int length;
  final Set<Integer> values = new TreeSet<>();

  /** initialize this rule for the length specified */
  PartRule(int length) {
    this.length = length;
  }

  /**
   * Parse the specification for this number-part rule and initialize
   * the current rule object
   *
   * @param specification comma-separated list of valid integer values
   */
  abstract void set(String specification);


  /** @return true if values is not empty and it contains the supplied value
   *  @param value of the rule */
  abstract boolean isValid(Integer value);

  /** @return the length for the part describes by this rule */
  public int getLength() {
    return length;
  }

}