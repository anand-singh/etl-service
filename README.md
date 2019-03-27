# ETL Service
ETL Service (REST API performing extract, transform and load operations) using Scala & http4s

Normally, I uses Play Framework & AkkaHTTP to build REST API with the Scala & Java. But in this code example I used http4s as I needs to complete this design using FP fundamentals. And http4s made it quite easy for me as it supports Typeful and functional design. Composability is quite easy task using this tool because of Cats library ecosystem.

##### This is a classic ETL Service, backed by Scala & http4s. It demonstrates:
* Extensible and maintainable design
* Used FP fundamentals to complete this design.
* Enough Unit tests and API (or integration tests) are available.
* Well logging & monitoring implemented as and when it’s needed.
* Good Error handling implementation.

### Domain Objects
* EtlSequence - This is main object of this application and it contains whole details about sequence operations which we can perform with the tool we have created. More specific will be composing operations one after the other.
* Operation - Contains info about operations like word count, word frequency, caps and replace with the body details.

*Transformation and Aggregation Operation:* - Below objects are used to transform http request into objects and send it to the service layer for further processing. 
* CountRequest
* FrequencyRequest
* AggregationResult
* CapsRequest
* ReplaceRequest
* TransformationResult

### Test Cases
* All the routes and request/response validation level test cases are available in the `package test.scala.io.github.etl.route` like:
  - AggregationRouteSpec - contains all tests related to aggregation operations (word count and word frequency)
  - TransformationRouteSpec - contains all tests related to transformation operations (caps and replace)
  - SequenceRouteSpec - contains all tests related to sequence validation and operations on both aggregation and transformation
* Service layer execution and validation level test cases are available in the `package test.scala.io.github.etl.service` like:
  - AggregationServiceSpec - Unit tests of aggregation operations (word count and word frequency)
  - TransformationServiceSpec - Unit tests of transformation operations (caps and replace)
  - SequenceServiceSpec - Unit tests of sequence validation and operations on both aggregation and transformation

### Logging & Monitoring - Request/Response
* All requests are tracked by a http header called `Request-Id`. This implementation mainly done for the tracking and monitoring purpose. Also a logger enabled for each request and response to track the details.
* System generates an unique `responseId` to track the incoming `requestId` and both are returns with the response header.
* Info and Error logging has been implemented as and when it’s needed.
* A well descriptive response header available for each request which contains information about the process state.
```
{
    "header": {
        "requestId": "f045faf4-845d-4f5b-a7bd-76d6fbcf8f44",
        "responseId": "f5bc3c6a-7d91-4e35-b5db-9b05fe695b90",
        "statusCode": 2000,
        "statusMessage": "SUCCESS"
    },
    "result": {}
}
```

### Exception Handling and Error Codes
* Application throws `EtlException` with appropriate error code for the `NonFatal` and `Validation` exceptions.
Below error codes has been implemented for the same.
```
CODE_2000 = 2000 // Success status code

CODE_5000 = 5000 // Unexpected errors
CODE_5001 = 5001 // System unavailable
CODE_5002 = 5002 // Scheduled downtime

CODE_4000 = 4000 // Invalid request - Malformed Json
CODE_4001 = 4001 // Invalid request - Mandatory data missing
CODE_4002 = 4002 // Invalid request - Pattern Syntax error
CODE_4003 = 4003 // Invalid request - Non logical operations.

CODE_3000 = 3000 // Resource reader errors
```

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
