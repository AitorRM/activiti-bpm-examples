# Microservice Java EE 7 basic with EJB 3.2

This is an example of Java EE 7 microservice with the following characteristics:

- Multi-Module Maven 3 proyect with two submodules:
  - EJB 3.2 microservice
    - Boundary-Control-Entity pattern
    - Stateless and Singleton session beans samples
    - Arquillian EJB testing for two profiles:
      - Glassfish 4.1 embedded
      - Wildfly 8.2.1.Final managed
  - Remote client
    - RMI remote access for EJB (lookup)
    - Log4j traces

- Tested with remote client in Wildfly 9.0.1.Final server

