package grails.cascade.validation.support

// tag::code[]
import grails.validation.Validateable

class ValidateableProperty implements Validateable {

    String field

    static constraints = {
        field blank: false
    }
}
// end::code[]
