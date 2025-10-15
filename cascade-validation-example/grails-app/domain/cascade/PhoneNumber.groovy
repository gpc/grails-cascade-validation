package cascade

// tag::domain[]
class PhoneNumber {

    String countryCode
    String areaCode
    String number
    String extension
    TelephoneType telephoneType
    boolean isPrimary

    static constraints = {
        countryCode nullable: false
        areaCode nullable: false
        number nullable: false
        extension nullable: true
        telephoneType nullable: false, cascaded: true
    }

}
// end::domain[]
