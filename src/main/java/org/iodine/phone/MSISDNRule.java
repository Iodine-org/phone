package org.iodine.phone;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class MSISDNRule {
    final int length;
    final Set<Integer> values = new HashSet<>();

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

    private List<Integer> generateRange(String part) {
      ArrayList<Integer> result = new ArrayList<>();
      String[] range = part.split("-");
      int low = Integer.parseInt(range[0]);
      int high = Integer.parseInt(range[1]);
      if ( low >= high) {
        throw new IllegalArgumentException("Bad range specified: " + part);
      }
      for ( int i = low; i < high; i++) {
        result.add(i);
      }
      return result;
    }

    /**
     * If the rule contains a restriction of values for this part,
     * is the supplied value in that allowed set?
     *
     * @param value of the rule
     * @return true if values is not empty and it contains the supplied value
     */
    boolean isValid(Integer value) {
      return values.isEmpty() ? true : values.contains(value);
    }

    @Override
    public String toString() {
      return values.toString();
    }
  }