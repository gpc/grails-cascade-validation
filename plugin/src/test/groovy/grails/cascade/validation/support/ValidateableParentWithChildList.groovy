package grails.cascade.validation.support

// tag::code[]
import grails.validation.Validateable

class ValidateableParentWithChildList implements Validateable {

    List<ValidateableProperty> children

    static constraints = {
        children cascaded: true
    }
}
// end::code[]
