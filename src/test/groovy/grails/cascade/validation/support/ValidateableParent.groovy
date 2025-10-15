package grails.cascade.validation.support

// tag::code[]
import grails.validation.Validateable

class ValidateableParent implements Validateable {

    ValidateableProperty property

    static constraints = {
        property cascaded: true
    }
}
// end::code[]
