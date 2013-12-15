# Phone Number Class
Immutable value-type representing phone numbers (including Mobile - MSISDN - numbers).

A phone number can only be validated and parsed in the context of a given scheme. This library provides the number value object that represents the number, plus the ability to define schemes that specify a class of numbers.
By default, schemes are placed in a Java properties file, named NumberSchemes.properties located in the classpath.
Schemes may also be added and removed programatically via the NumberFactory singleton or by the creation and use of NumberScheme instances.

Schemes are defined using the following grammar:
*SchemeName*=*Definition*

Where scheme name is a label for the scheme, conventionally consisting of the 2-letter ISO country code, and a distinguishing label, such as:
``DE.vodafone``
For the Vodafone number scheme in Germany

The *Definition* consists of:
* Specification of the sizes of the country code, area (operator) code and subscriber number parts of the scheme, e.g.,
2,3,10 specifying a 2 digit country code (CC), 3 digit area code (NDC) and 10 digit subscriber number (SN)
* Valid values for the CC and NDC portions, e.g., ``CC=2:12;NDC=3:800,840,900-999;SN=10``

A complete example:
``DE.tmob+vfone=CC=2:49;NDC=3:160,162,163,170-179;SN=10:5*********``
Defines a 15-digit phone number, country dialing code +49, with a restricted set of 3-digit 'area' codes and 10-digit subscriber numbers that must start with '5'

## Usage
JavaDoc: https://github.com/Iodine-org/phone/blob/master/apidocs/index.html
```java
PhoneNumber number1 = PhoneNumber.parse("+353-86-3578080");
PhoneNumber number2 = PhoneNumber.parse("+44.865.249.864")
PhoneNumber number3 = PhoneNumber.valueOf(491711234567890L);
PhoneNumber number4 = PhoneNumber.Builder().cc(353).ndc(87).subscriber(3538080).build();

    PhoneNumber usNumber = PhoneNumber.parse("1 855 784-9261");
    Assert.assertEquals("+18557849261", usNumber.toString());
    Assert.assertEquals(1, usNumber.getCC());
    Assert.assertEquals(855, usNumber.getNDC());
    Assert.assertEquals(7849261, usNumber.getSN());
    
    System.out.println ( "Please call: " + usNumber.format("0$CC-$NDC-$SN"));
    Please call: 01-855-7849261
```
