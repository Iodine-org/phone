Immutable value-type representing MSISDN phone numbers

A MSISDN number can only be validated and parsed in the context of a given scheme. This library provides the MSISDN value object that represents the number, plus the ability to define schemes that specify a class of MSISDNs.
By default, schemes are placed in a Java properties file, named MSISDNSchemes.properties located in the classpath.
Schemes may also be added and removed programatically via the MSISDNFactory singleton or by the creation and use of MSISDNScheme instances.

Schemes are defined using the following grammar:
<SchemeName>=<Definition>
Where scheme name is a label for the scheme, conventionally consisting of the 2-letter ISO country code, and a distiquishing label, such as:
DE.vodafone
For the Vodafone number scheme in Germany

The <Definition> consists of:
* Specification of the sizes of the country code, area (operator) code and subscriber number parts of the scheme, e.g.,
2,3,10 specifying a 2 digit country code (CC), 3 digit area code (NDC) and 10 digit subscriber number (SN)
* Valid values for the CC and NDC portions, e.g., CC=12;NDC=800,840,900-999

A complete example:
DE.tmob+vfone=2,3,10;CC=49;NDC=160,162,163,170-179
