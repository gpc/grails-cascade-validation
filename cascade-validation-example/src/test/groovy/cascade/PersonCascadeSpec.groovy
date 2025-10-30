package cascade


import grails.cascade.validation.internal.CascadedConstraintRegistration
import grails.testing.gorm.DataTest
import grails.util.Holders
import spock.lang.Specification

class PersonCascadeSpec extends Specification implements DataTest {

    void setup() {
        mockDomains(Person, PhoneNumber, TelephoneType)
        // Important in unit-tests: Register the CascadeConstraints
        // tag::initialize[]
        CascadedConstraintRegistration.register(applicationContext)
        // end::initialize[]
        // Default
        Holders.config.setAt('constraints.cascaded.legacy', false)
    }

    void "cascade validation on Person with empty phoneNumbers"() {
        given:
        Person person = new Person(firstName: 'John', lastName: 'Doe')
        
        expect:
        person.validate()
    }
    void "cascade validation on Person with non-valid phoneNumber in phoneNumbers"() {
        given:
        Person person = new Person(firstName: 'John', lastName: 'Doe').tap {
            addToPhoneNumbers(countryCode: '+1', areaCode: '800', number: '555-2345')
        }
        
        expect: 'that the validation fails due to errors in the phone-number'
        !person.validate()
        
        and: 'there is one error in the phone-number'
        person.errors.errorCount == 1
        
        and:  'that error is on the first phone-number telephone-type'
        person.errors.getFieldError('phoneNumbers[0].telephoneType').code == 'nullable'
    }    
    
    void "cascade validation on Person with non-valid phoneNumber in phoneNumbers (legacy)"() {
        given:
        Holders.config.setAt('constraints.cascaded.legacy', true)

        and: 
        Person person = new Person(firstName: 'John', lastName: 'Doe').tap {
            addToPhoneNumbers(countryCode: '+1', areaCode: '800', number: '555-2345')
        }
        
        expect: 'that the validation fails due to errors in the phone-number'
        !person.validate()
        
        and: 'there is one error in the phone-number'
        person.errors.errorCount == 2 // Also has the Gorm cascade validation errors
        
        and:  'that error is on the first phone-number telephone-type'
        person.errors.getFieldError('phoneNumbers.0.telephoneType').code == 'nullable'
    }    
    
    void "cascade validation on Person with multiple non-valid phoneNumbers in phoneNumbers"() {
        given:
        Person person = new Person(firstName: 'John', lastName: 'Doe').tap {
            addToPhoneNumbers(countryCode: '+1', areaCode: '800', number: '555-2345')
            addToPhoneNumbers(countryCode: '+1', areaCode: '800', number: '555-1234', telephoneType: new TelephoneType())
        }
        
        expect: 'that the validation fails due to errors in the phone-number'
        !person.validate()
        
        and: 'there are two error in the list of phone numbers'
        person.errors.errorCount == 3 // Due to GORM cascade validations
        
        and:  'that error is on the first phone number telephone type'
        person.errors.getFieldError('phoneNumbers[0].telephoneType').code == 'nullable'

        and: 'the error on the second phone number is cascading to telephoneType'
        person.errors.getFieldError('phoneNumbers[1].telephoneType.countryCodeRecommended').code == 'nullable'
    }  
    
    void "cascade validation on Person with multiple non-valid phoneNumbers in phoneNumbers (legacy)"() {
        given:
        Holders.config.setAt('constraints.cascaded.legacy', true)
        and:
        Person person = new Person(firstName: 'John', lastName: 'Doe').tap {
            addToPhoneNumbers(countryCode: '+1', areaCode: '800', number: '555-2345')
            addToPhoneNumbers(countryCode: '+1', areaCode: '800', number: '555-1234', telephoneType: new TelephoneType())
        }
        
        expect: 'that the validation fails due to errors in the phone-number'
        !person.validate()
        
        and: 'there are two error in the list of phone numbers'
        person.errors.errorCount == 5 // Due to GORM cascade validations
        
        and:  'that error is on the first phone number telephone type'
        person.errors.getFieldError('phoneNumbers.0.telephoneType').code == 'nullable'

        and: 'the error on the second phone number is cascading to telephoneType'
        person.errors.getFieldError('phoneNumbers.1.telephoneType.countryCodeRecommended').code == 'nullable'
    }
}
