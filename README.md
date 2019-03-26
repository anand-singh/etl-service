# ETL Service
ETL Service (REST API performing extract, transform and load operations) using Scala & http4s

##### This is a classic ETL Service, backed by Scala & http4s. It demonstrates:
* Extensible and maintainable design
* Used FP fundamentals to complete this design.
* Enough Unit tests and API (or integration tests) are available.
* Well logging & monitoring implemented as and when it’s needed.
* Good Error handling implementation.

### Domain Objects
* EtlSequence - This is main object of this application and it contains whole details about sequence operations which we can perform with the tool we have created. More specific will be composing operations one after the other.
* Operation - Contanis info about operations like wordcount, wordfrequency, caps and replace with the body details.

*Transformation and Aggregation Operation:* - Below objects are used to transform http request into objects and send it to the service layer for further processing. 
* CountRequest
* FrecuencyRequest
* AggregationResult
* CapsRequest
* ReplaceRequest
* TransformationResult



### Build Instructions
* The Github code for the project is at : https://github.com/anand-singh/etl-service
* Clone the project into local system
* To run this sbt project, you need JDK 8 or later
* Execute `sbt clean compile` to build the product
* Execute `sbt run` to start the etl-service
* Finally etl-service should be now accessible at localhost:8080

### Testing, Code Coverage & Scala Style
* Execute `sbt clean test` to test the product
* Run the tests with enabled coverage to generate the coverage report:
```
sbt clean coverage test
```
* To generate the coverage reports run:
```
sbt coverageReport
```
Coverage reports will be in `target/scoverage-report`. There are HTML and XML reports.

##### Current code coverage report
```
[info] Statement coverage.: 93.54%
[info] Branch coverage....: 100.00%
```

##### Scala Style
* Execute `sbt scalastyle` to check the code quality
```
[info] scalastyle Processed 14 file(s)
[info] scalastyle Found 0 errors
[info] scalastyle Found 0 warnings
[info] scalastyle Found 0 infos
```

### References
* [Scala](https://www.scala-lang.org/) - Scala combines object-oriented and functional programming in one concise, high-level language.
* [http4s](https://http4s.org/) - Http4s is a minimal, idiomatic Scala interface for HTTP services.
* [circe](https://circe.github.io/circe/) - A JSON library for Scala powered by Cats
* [Specs2](http://specs2.org) - Software Specifications for Scala
* [Logback](https://logback.qos.ch/) - Logback is intended as a successor to the popular log4j project, picking up where log4j leaves off.
* [sbt-scoverage](https://github.com/scoverage/sbt-scoverage) - Plugin for SBT that integrates the scoverage code coverage library.
* [Scalastyle](http://www.scalastyle.org/) - Scalastyle examines your Scala code and indicates potential problems with it.
