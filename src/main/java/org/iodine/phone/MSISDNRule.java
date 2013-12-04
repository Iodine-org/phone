package org.iodine.phone;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** Defines the content rule for one part of a MSISDN number, in terms
 *  of its length and, optionally, the domain of values valid for the part */
public final class MSISDNRule {
  final int length;
  final Set<Integer> values = new TreeSet<>();

  /** initialize this rule for the length specified */
  MSISDNRule(int length) {
    this.length = length;
  }

  /**
   * Parse the specification for this number-part rule and initialize
   * the current rule object
   *
   * @param specification comma-separated list of valid integer values
   */
  void setValues(String specification) {
    for (String part : specification.split(",")) {
      if ( part.contains("-")) {
        values.addAll(generateRange(part));
      }
      else {
        values.add(Integer.parseInt(part));
      }
    }
  }

  /** @return a list of integers in the (inclusive) range specified
   *  @param rangeSpec string, e.g., "120-131"
   *  @throws IllegalArgumentException if the <code>rangeSpec</code>
   *            does not specify a valid integer range*/
  private List<Integer> generateRange(String rangeSpec) {
    ArrayList<Integer> result = new ArrayList<>();
    String[] range = rangeSpec.split("-");
    int low = Integer.parseInt(range[0]);
    int high = Integer.parseInt(range[1]);
    if ( low >= high) {
      throw new IllegalArgumentException("Bad range specified: " + range);
    }
    for ( int i = low; i <= high; i++) {
      result.add(i);
    }
    return result;
  }

  /** @return true if values is not empty and it contains the supplied value
   *  @param value of the rule */
  boolean isValid(Integer value) {
    return values.isEmpty() || values.contains(value);
  }

  @Override
  public String toString() {
    return values.toString();
  }

  /** @return the length for the part describes by this rule */
  public int getLength() {
    return length;
  }

  /** @return the valid values for the part describes by this rule */
  public ArrayList<Integer> getValues() {
    return new ArrayList<>(values);
  }
}