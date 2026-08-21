package cascade


import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
@Rollback
class PersonDataServiceSpec extends Specification {

    PersonDataService personDataService

    void "person save without errors"() {
        when:
        def result = personDataService.save(new Person(firstName: 'John', lastName: 'Doe'))
        
        then:
        result.id 
        result.firstName == 'John'
        result.lastName == 'Doe'
    }
    
}
