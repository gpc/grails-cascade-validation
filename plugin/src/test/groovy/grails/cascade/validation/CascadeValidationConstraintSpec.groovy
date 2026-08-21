package grails.cascade.validation

import grails.cascade.validation.internal.CascadedConstraint
import grails.cascade.validation.support.ValidateableParent
import grails.cascade.validation.support.ValidateableProperty
import grails.validation.ValidationErrors
import org.grails.testing.GrailsUnitTest
import org.springframework.validation.Errors
import org.springframework.validation.FieldError
import spock.lang.Specification

/**
 * @author: rmorrise
 * @author Eric Kelm
 */
class CascadeValidationConstraintSpec extends Specification implements GrailsUnitTest {

    CascadedConstraint constraint
    ValidateableParent parent
    ValidationErrors errors = Mock()

    def setup() {
        parent = Mock(ValidateableParent)
        errors = Mock(ValidationErrors)
        parent.errors >> errors
    }

    void "constraint name should be cascade"() {
        given:
        constraint = new CascadedConstraint(
                ValidateableParent,
                'property',
                true,
                null
        )

        expect:
        constraint.name == 'cascaded'
    }

    void "validateWithVetoing fails when constraint is set on non-validatable type"() {
        given:
        constraint = new CascadedConstraint(
                ValidateableParent,
                'property',
                true,
                null
        )

        def target = "Some value"

        when:
        constraint.validate(parent, target, errors)

        then:
        thrown(NoSuchMethodException)
    }

    void "validateWithVetoing returns valid when constraint is set to validateable type and constraints pass"() {
        given:
        constraint = new CascadedConstraint(
                ValidateableParent,
                'property',
                true,
                null
        )

        def target = Mock(ValidateableProperty)

        when:
        constraint.validate(parent, target, errors)

        then:
        1 * target.validate() >> true
        0 * errors.addError(_)
    }

    void "validateWithVetoing returns invalid when constraint is set to validateable type and constraints fail"() {
        given:
        constraint = new CascadedConstraint(
                ValidateableParent,
                'property',
                true,
                null
        )

        def target = Mock(ValidateableProperty)
        def childErrors = Mock(Errors)
        def rejected = Mock(Object)
        String[] codes = ['A', 'B']
        def defaultMessage = 'default'
        Object[] args = [Mock(Object)]

        def field = 'field'
        def fieldError = new FieldError('obj', field, rejected, true, codes,
                args, defaultMessage)
        def fieldErrors = [fieldError]
        def parentName = 'foo'

        when:
        constraint.validate(parent, target, errors)

        then:
        1 * target.validate() >> false
        1 * target.errors >> childErrors
        1 * childErrors.fieldErrors >> fieldErrors
        1 * errors.objectName >> parentName
        1 * errors.addError({
            it.objectName == parentName &&
                    it.field == "property." + field &&
                    it.bindingFailure == true &&
                    it.codes == codes &&
                    it.arguments == args &&
                    it.defaultMessage == defaultMessage
        })
    }

    void "validateWithVetoing returns invalid when constraint is set to validateable type and constraints fail on list"() {
        given:
        constraint = new CascadedConstraint(
                ValidateableParent,
                'property',
                true,
                null
        )

        def child1 = Mock(ValidateableProperty)
        def child2 = Mock(ValidateableProperty)
        def child1Errors = Mock(Errors)
        def child2Errors = Mock(Errors)
        def target = [child1, child2]
        def rejected = Mock(Object)
        String[] codes = ['A', 'B']
        def defaultMessage = 'default'
        Object[] args = [Mock(Object)]
        def field = 'field'
        def fieldError = new FieldError('obj', field, rejected, true, codes,
                args, defaultMessage)
        def fieldErrors = [fieldError]
        def parentName = 'foo'

        when:
        constraint.validate(parent, target, errors)

        then:
        1 * child1.validate() >> false
        1 * child1.errors >> child1Errors
        1 * child2.validate() >> false
        1 * child2.errors >> child2Errors
        1 * child1Errors.fieldErrors >> fieldErrors
        1 * child2Errors.fieldErrors >> fieldErrors
        target.size() * errors.objectName >> parentName
        2 * errors.addError(_)
    }

    void "constraint only validates if enabled evaluates to true"() {
        given:
        constraint = new CascadedConstraint(
                ValidateableParent,
                'anotherProperty',
                truth,
                null
        )
        parent.shouldCascade() >> shouldCascade

        and:
        def target = Mock(ValidateableProperty)

        when:
        constraint.validate(parent, target, errors)

        then:
        callsToValidate * target.validate() >> true

        where:
        _ | truth                                 | shouldCascade || callsToValidate
        _ | true                                  | _             || 1
        _ | false                                 | _             || 0
        _ | { true }                              | _             || 1
        _ | { false }                             | _             || 0
        _ | { val -> true }                       | _             || 1
        _ | { val -> false }                      | _             || 0
        _ | { value, obj -> obj.shouldCascade() } | true          || 1
        _ | { value, obj -> obj.shouldCascade() } | false         || 0
    }

    void "constraint does not support non-validateable types"() {
        given:
        constraint = new CascadedConstraint(
                ValidateableParent,
                'property',
                true,
                null
        )

        expect:
        !constraint.supports(String)
    }

    void "constraint supports validateable types"() {
        given:
        constraint = new CascadedConstraint(
                ValidateableParent,
                'property',
                true,
                null
        )

        expect:
        constraint.supports(ValidateableProperty)
    }

    void "constraint supports collection types"() {
        given:
        constraint = new CascadedConstraint(
                ValidateableParent,
                'property',
                true,
                null
        )

        expect:
        constraint.supports(List)
    }

    void "constraint can handle constraintParameter when is a closure with one or two params"() {
        when:
        constraint = new CascadedConstraint(
                ValidateableParent,
                'property',
                closure,
                null
        )

        then:
        notThrown IllegalArgumentException

        where:
        closure << [{ return true }, { a -> return true }, { a, b -> return true }]
    }

    void "constraint cannot handle constraintParameter when is a closure more than two parameters"() {
        when:
        constraint = new CascadedConstraint(
                ValidateableParent,
                'property',
                { a, b, c -> true },
                null
        )

        then:
        def e = thrown IllegalArgumentException
        e.message == 'Too many arguments on closure, expects one or two'
    }

    void "constraint cannot handle constraintParameter other than boolean"() {
        when:
        constraint = new CascadedConstraint(
                ValidateableParent,
                'property',
                ['x'],
                null
        )

        then:
        thrown IllegalArgumentException
    }

}
