# Prototype

That is an example for a prototype of what I have in mind:

* Lightweight 40MB  Java Spring application that can be started with a simple java -jar <name>.jar
* You can install it in whatever container you want as long as a JRE is there
* Full support of Spring dependency injection for clean IoC
* Offers an REST API on 80 and 443 through an embedded Jetty (we could simply extend it for the usage of websockets int he future) instead of this (!&&! not documented netty
* It contains an embedded database. While files are nice, SQL is really mighty and reduces the boiler plate code a lot
* Database is automatically intialized from JSON files (no need to initialize every demo from scratch)
* Entities are written in a way that they can be easily written to the DB or serialized / deserialized to JSON, i.e. no need for conversaion during the prototype phase
* The Open API documentation is automatically generated from the Jersey Endpoints, i.e. code as documentation and you can generate what ever client you want for the rest endpoint
* Can be used as templates for all of the services (in a single repository of course, but this will ensure a clean separation)

Coding effort: 2h 
