package grails.cascade.validation.internal

import grails.util.Holders
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.validation.constraints.AbstractConstraint
import org.springframework.context.MessageSource
import org.springframework.validation.Errors
import org.springframework.validation.FieldError

/**
 * Establishes a 'cascade' constraint property for validateable objects. If "cascaded:true"
 * is set on a nested object, the nested object's validate() method will be invoked and the
 * results will be reported as part of the parent object's validation.
 *
 * Based on a blog article by Eric Kelm, with modifications by Russell Morrisey.
 *
 * @see: http://asoftwareguy.com/2013/07/01/grails-cascade-validation-for-pogos/
 * @author Eric Kelm
 * @author Russell Morrisey
 */
@CompileStatic
class CascadedConstraint extends AbstractConstraint {

    static final String CASCADE_CONSTRAINT = 'cascaded'
    private final Closure<Boolean> enabledEvaluator

    CascadedConstraint(Class<?> constraintOwningClass, String constraintPropertyName, Object constraintParameter, MessageSource messageSource) {
        super(constraintOwningClass, constraintPropertyName, constraintParameter, messageSource)
        this.enabledEvaluator = constraintParameter instanceof Closure ? (constraintParameter as Closure<Boolean>) : { (constraintParameter as boolean) }
        if (this.enabledEvaluator.maximumNumberOfParameters > 2) {
            throw new IllegalArgumentException('Too many arguments on closure, expects one or two')
        }
    }

    @Override
    protected Object validateParameter(Object constraintParameter) {
        if (!(constraintParameter instanceof Boolean || constraintParameter instanceof Closure)) {
            throw new IllegalArgumentException("Parameter for constraint [$CASCADE_CONSTRAINT] of property [$constraintPropertyName] of class [$constraintOwningClass] must be a boolean or a closure returning a boolean")
        }
        return constraintParameter
    }

    boolean supports(Class type) {
        Collection.isAssignableFrom(type) || type.metaClass.respondsTo(type, 'validate')
    }

    String getName() {
        return CASCADE_CONSTRAINT
    }

    protected void processValidate(Object target, Object propertyValue, Errors errors) {
        if (!isEnabled(target, propertyValue)) {
            return
        }

        if (propertyValue instanceof Collection) {
            propertyValue.eachWithIndex { item, pvIdx ->
                validateValue(target, item, errors, pvIdx)
            }
        } else {
            validateValue(target, propertyValue, errors)
        }
    }

    /**
     * Processes the validation of the propertyValue, against the checks patterns set, and setting and calling rejectValue
     * if the propertyValue matches any of the patterns in the checks list.
     *
     * @param target The target field to verify.
     * @param propertyValue the property value of the field.
     * @param errors Errors to be sent by rejectValues,.
     */
    @CompileDynamic
    private void validateValue(target, value, errors, index = null) {
        if (!value.respondsTo('validate')) {
            throw new NoSuchMethodException("Error validating field [${constraintPropertyName}]. Unable to apply 'cascade' constraint on [${value.class}] because the object does not have a validate() method. If the object is a command object, you may need to implement grails.validation.Validateable on the class definition.")
        }

        if (value.validate()) {
            return
        }

        String objectName = target.errors.objectName
        Errors childErrors = value.errors
        List<FieldError> childFieldErrors = childErrors.fieldErrors

        childFieldErrors.each { FieldError childFieldError ->
            String field

            if (index != null) {
                field = useLegacyNaming ? "${propertyName}.${index}.${childFieldError.field}" : "${propertyName}[${index}].${childFieldError.field}"
            } else {
                field = "${propertyName}.${childFieldError.field}"
            }

            FieldError fieldError = new FieldError(objectName, field, childFieldError.rejectedValue, childFieldError.bindingFailure, childFieldError.codes, childFieldError.arguments, childFieldError.defaultMessage)
            errors.addError(fieldError)
        }
    }
    
    private static boolean getUseLegacyNaming() {
        Holders.config?.getProperty('constraints.cascaded.legacy', Boolean, Boolean.FALSE)
    }

    private boolean isEnabled(Object target, Object propertyValue) {
        switch (enabledEvaluator.maximumNumberOfParameters) {
            case 1: return enabledEvaluator.call(propertyValue)
            case 2: return enabledEvaluator.call(propertyValue, target)
            default: throw new IllegalArgumentException('Too many arguments on closure, expects one or two')
        }
    }

}
