package cascade

// tag::domain[]
class TelephoneType {

    Boolean countryCodeRecommended

    static constraints = {
        countryCodeRecommended nullable: false
    }

}
// end::domain[]