# MSISDN Phone Number Class
### Mobile Subscriber Integrated Services Digital Network Number
Immutable value-type representing MSISDN phone numbers (as well as most other phone numbers).

A MSISDN number can only be validated and parsed in the context of a given scheme. This library provides the MSISDN value object that represents the number, plus the ability to define schemes that specify a class of MSISDNs.
By default, schemes are placed in a Java properties file, named MSISDNSchemes.properties located in the classpath.
Schemes may also be added and removed programatically via the MSISDNFactory singleton or by the creation and use of MSISDNScheme instances.

Schemes are defined using the following grammar:
*SchemeName*=*Definition*

Where scheme name is a label for the scheme, conventionally consisting of the 2-letter ISO country code, and a distiquishing label, such as:
``DE.vodafone``
For the Vodafone number scheme in Germany

The *Definition* consists of:
* Specification of the sizes of the country code, area (operator) code and subscriber number parts of the scheme, e.g.,
2,3,10 specifying a 2 digit country code (CC), 3 digit area code (NDC) and 10 digit subscriber number (SN)
* Valid values for the CC and NDC portions, e.g., ``CC=12;NDC=800,840,900-999``

A complete example:
``DE.tmob+vfone=2,3,10;CC=49;NDC=160,162,163,170-179;SN=5*********``

## Usage
JavaDoc: https://github.com/Iodine-org/phone/blob/master/apidocs/index.html
```java
MSISDN number1 = MSISDN.parse("+353-86-3578380");
MSISDN number2 = MSISDN.parse("+44.865.249.864")
MSISDN number3 = MSISDN.valueOf(491711234567890L);
MSISDN number4 = MSISDN.Builder().cc(353).ndc(87).subscriber(3538080).build();

    MSISDN usNumber = MSISDN.parse("1 855 784-9261");
    Assert.assertEquals("+18557849261", usNumber.toString());
    Assert.assertEquals(1, usNumber.getCC());
    Assert.assertEquals(855, usNumber.getNDC());
    Assert.assertEquals(7849261, usNumber.getSN());
```
