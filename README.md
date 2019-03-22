# ETL Service
ETL Service (REST API performing extract, transform and load operations) using Scala & http4s

##### This is a classic ETL Service, backed by Scala & http4s. It demonstrates:
* Extensible and maintainable design
* Used FP fundamentals to complete this design.
* Enough Unit tests and API (or integration tests) are available.
* Well logging & monitoring implemented as and when it’s needed.
* Good Error handling implementation.

### Play Instructions
* The Github code for the project is at : https://github.com/anand-singh/etl-service
* Clone the project into local system
* To run this sbt project, you need JDK 8 or later
* Execute `sbt clean compile` to build the product
* Execute `sbt run` to start the etl-service
* Finaly etl-service should be now accessible at localhost:8080

### References
* [Scala](https://www.scala-lang.org/) - Scala combines object-oriented and functional programming in one concise, high-level language.
* [http4s](https://http4s.org/) - Http4s is a minimal, idiomatic Scala interface for HTTP services.
* [circe](https://circe.github.io/circe/) - A JSON library for Scala powered by Cats
* [Specs2](http://specs2.org) - Software Specifications for Scala
* [Logback](https://logback.qos.ch/) - Logback is intended as a successor to the popular log4j project, picking up where log4j leaves off.
