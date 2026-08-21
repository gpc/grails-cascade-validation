package cascade

// tag::domain[]
class Person {

    String firstName
    String lastName

    List<PhoneNumber> phoneNumbers = []

    static hasMany = [phoneNumbers: PhoneNumber]

    static constraints = {
        phoneNumbers cascaded: true
    }

}
// end::domain[]
