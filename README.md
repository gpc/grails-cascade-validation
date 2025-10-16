[![Build](https://github.com/gpc/grails-cascade-validation/actions/workflows/build.yml/badge.svg)](https://github.com/gpc/grails-cascade-validation/actions/workflows/build.yml)

grails-cascade-validation
=========================

This plugin establishes a `cascade` constraint property for validateable objects, that being domain objects, and objects implementing `grails.validation.Validateable`. If `cascade:true` is set on a nested object, the nested object's `validate()`  method will be invoked and the results will be reported as part of the parent object's validation.

This plugin is for Grails 7.0.0

### **BREAKING CHANGE**

The `cascade` constraint in `constraints` was renamed to `cascaded` because of a name clash with Hibernate cascading.

To use this plugin, add the plugin to `build.gradle`:

```groovy 
dependencies {
     implementation "io.github.gpc:cascade-validation:7.0.0"
}
```

Here is an example of a command object that uses the plugin:

```groovy
 class PhoneNumber implements Validateable {
     long id
     String countryCode
     String areaCode
     String number
     String extension
     TelephoneType telephoneType
     boolean isPrimary

     static constraints = {
         areaCode(nullable: false)
         number(nullable: false)
         telephoneType(cascaded: true)
     }

     static class TelephoneType implements Validateable {
         String id
         boolean countryCodeRecommended

         static constraints = {
             id(nullable: false)
             countryCodeRecommended(nullable: false)
         }
     }
 }
```

When the `cascade:` constraint is added on the `telephoneType`  property, this enables nested validation. When the `phoneNumber.validate()` method is called, the `telephoneType.validate()` method will also be invoked. Field errors that are added to the `telephoneType` will also be added to the parent `phoneNumber` object.

This plugin was originally based on a blog post by Eric Kelm and is used here with Eric's permission.

*NOTE:*

When running a unit test, the cascade constraint isn't registered with Grails. To work around this issue, the test class must implement
`org.grails.testing.GrailsUnitTest` and the following code must be added to the `setup()` method of the test:

```groovy
import grails.cascade.validation.internal.CascadeConstraintRegistration
import org.grails.datastore.gorm.validation.constraints.eval.DefaultConstraintEvaluator
import org.grails.testing.GrailsUnitTest
import spock.lang.Specification

class ParentSpec extends Specification implements GrailsUnitTest {

    Closure doWithSpring() {
        return {
            constraintEvaluator(DefaultConstraintEvaluator)
        }
    }

    void setup() {
        // Important for Unit-tests as the registrations only happens when Grails context is started.
        CascadeConstraintRegistration.register(applicationContext)
    }

    void 'validate cascade'() {
        given:
        def phone = new PhoneNumber(telephoneType: new PhoneNumber.TelephoneType())

        when:
        phone.validate(['telephoneType'])

        then:
        phone.hasErrors()

        parent.errors.getFieldError('telephoneType.id').code == 'nullable'
        parent.errors.getFieldError('telephoneType.countryCodeRecommended').code == 'nullable'
    }
}

```
This will register the `CascadeConstraint` the same way as the plugin does at runtime.

See the full documentation here: https://gpc.github.io/grails-cascade-validation/snapshot/
