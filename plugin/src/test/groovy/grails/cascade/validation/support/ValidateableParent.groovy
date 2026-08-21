package grails.cascade.validation.support

// tag::code[]
import grails.validation.Validateable

class ValidateableParent implements Validateable {

    ValidateableProperty property
    ValidateableProperty anotherProperty
    
    boolean shouldCascade() { true }
    
    static constraints = {
        property cascaded: true
        anotherProperty cascade: { value, object -> object.shouldCascade() }
    }
}
// end::code[]
