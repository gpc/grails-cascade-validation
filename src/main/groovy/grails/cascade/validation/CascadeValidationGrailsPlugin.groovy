package grails.cascade.validation


import grails.cascade.validation.internal.CascadedConstraintRegistration
import grails.plugins.Plugin

class CascadeValidationGrailsPlugin extends Plugin {

    def grailsVersion = '7.0.0 > *'
    def profiles = ['web']
    def title = 'Cascade Validation Plugin'
    def author = "Russell Morris"
    def authorEmail = "rmorrise@cscinfo.com"
    def description = '''\
Establishes a 'cascade' constraint property for validatable objects. If "cascade:true" is set
 on a nested object, the nested object's validate() method will be invoked and the results will
 be reported as part of the parent object's validation.

Based on a blog post by Eric Kelm:
 https://asoftwareguy.com/2013/07/01/grails-cascade-validation-for-pogos/
Used with permission.
'''
    def documentation = 'https://gpc.github.io/grails-cascade-validation/snapshot/manual/index.html'
    def license = 'APACHE'
    def organization = [name: 'Grails Plugin Collective', url: "https://www.github.com/gpc"]
    def issueManagement = [system: 'GITHUB', url: 'https://github.com/gpc/grails-cascade-validation/issues']
    def scm = [url: 'https://github.com/gpc/grails-cascade-validation']

    def developers = [
            [name: 'Russell Morrisey', github: 'https://github.com/rmorrise'],
            [name: 'Søren Berg Glasius', email: 'soeren@glasius.dk', github: 'https://github.com/sbglasius'],
            [name: 'Thomas Dauer Rasmussen', github: 'https://github.com/dauer'],
            [name: 'Eric Kelm', github: 'https://github.com/asoftwareguy'],
            [name: 'Burt Beckwith', github: 'https://github.com/burtbeckwith'],
            [name: 'Christian Oestreich', github: 'https://github.com/ctoestreich'],
            [name: 'Tucker Pelletier', github: 'https://github.com/virtualdogbert']
    ]


    void doWithApplicationContext() {
        CascadedConstraintRegistration.register(applicationContext)
    }

}
