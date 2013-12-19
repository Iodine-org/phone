package org.macgyver.phone;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class AlphaTranslationTest {

  @Test
  public void alphaNumbersCorrectlyTranslated() {
    long number1 = alphaToNumber("testnumber");
    Assert.assertEquals(8378686237L, number1);
  }

  private static final Map<Character,Character> TranslationTable = new HashMap<Character,Character>() {{
    put ( 'A', '2'); put ( 'B', '2'); put ( 'C', '2'); put ( 'D', '3'); put ( 'E', '3'); put ( 'F', '3');
    put ( 'G', '4'); put ( 'H', '4'); put ( 'I', '4'); put ( 'J', '5'); put ( 'K', '5'); put ( 'L', '5');
    put ( 'M', '6'); put ( 'N', '6'); put ( 'O', '6'); put ( 'P', '7'); put ( 'Q', '7'); put ( 'R', '7');
    put ( 'S', '7'); put ( 'T', '8'); put ( 'U', '8'); put ( 'V', '8'); put ( 'W', '8'); put ( 'X', '9');
    put ( 'Y', '9'); put ( 'Z', '9'); put ( '+', '0');
  }};

  private static long alphaToNumber(String value) {
    StringBuffer result = new StringBuffer();
    String array = value.trim().toUpperCase();
    int len = array.length();
    for ( int i = 0; i < len; i++) {
      Character c = array.charAt(i);
      if ( TranslationTable.containsKey(c)) {
        result.append ( TranslationTable.get(c));
      } else {
        result.append ( c);
      }
    }
    return Long.parseLong(result.toString());
  }
}
