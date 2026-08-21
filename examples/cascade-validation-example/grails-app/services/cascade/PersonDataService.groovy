package cascade


import grails.gorm.services.Service

@Service(Person)
interface PersonDataService {

    Person save(Person person)

}
