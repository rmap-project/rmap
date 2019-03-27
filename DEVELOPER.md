# RMap Developer Documentation

## RMap source overview
The RMap software has 4 main modules that work together plus one small module for test data, and two other modules supporting integration testing.

### core
As the name suggests, this contains the core code for the RMap software. It defines the RMap data model, interacts with the underlying triplestore, and provides the tools needed to manage and access RMap data.
### auth
*Core* functions support the creation of an RMap Agent, and each time you write to RMap a valid Agent URI is required.  The *core* module is not otherwise involved in authentication or user management. Instead, the web application and API handle the authorization and authentication of users. To do this they both use the *auth* component. This coordinates between the users that have logged in through the website, the API keys those users create, and the association of an RMap Agent. The *auth* module is supported by a small user database. 
### api
The api module provides a REST API to expose the major functions of RMap.  These include functions to manage DiSCOs and retrieve triples that reference particular resource URIs.  The technical documentation contains a [full description of the API functions](https://github.com/rmap-project/rmap-documentation/api).  Read functions are available without an API key. Write funtions require the user to login through the website and register for an API key. 
### webapp
The web application allows users to browse RMap Agents, DiSCOs, and Events in a visual and interactive way.  It also supports the configuration of RMap Agents and API keys to be used for write-access to the RMap API. A [live demo site](https://test.rmap-hub.org/app) is available to try out.
### testdata
The test data module contains RDF files for DiSCOs and Agents. This data is used to generate test data for the JUnit tests in the other modules.
### integration
This module bootstraps an integration environment and executes integration tests.
### spring-util
This module contains utility classes that manage the persisted data used by integration tests. 

## Compiling from source
### System Requirements

- Java 8
- Maven 2.4

### Maven install
RMap is a Maven-based project.  To build RMap from source, including creating jar libraries and web application WARs, run the following command from the base RMap directory:
```
mvn clean install
``` 
This command will build each module (`core`, `auth`, `api`, etc.) in dependency order, and install the built artifacts (i.e. the resulting JAR and WAR files) into your local Maven repository (normally located at `~/.m2/repository`).
The `war` files will be found in the `/target` folder of `webapp` and `api`. These can be installed per the [installation documentation](https://github.com/rmap-project/rmap-documentation/installation).  If, however, you simply need to run a local instance of RMap for development purposes, see below for instructions on the [developer runtime](#developer-runtime)
Note: the RMap integration tests require Docker. If you don't have Docker, you can skip the integration tests using the following:
```
mvn clean install -DskipITs -Ddocker.skip -Dcargo.maven.skip
``` 

# [Developer runtime](#developer-runtime)
## Running RMap
The developer runtime is appropriate for testing modifications to the source code, API, or web user interface.  It is _not_ appropriate for production.  Developers can run a local instance of RMap by executing:

- `mvn clean install` from the base RMap directory
- then `cd` into the `integration` directory
- run `mvn process-test-resources docker:start cargo:run`
  - the **`process-test-resource`** phase prepares RDF4J web applications for deployment
    - builds and copies test data and wars to `integration/target/test-classes/docker` 
  - **`docker:start`** boots Postgres, Zookeeper, Kafka, RDF4J (both the "workbench" app and the "server" app from `integration/target/test-classes/docker/webapps`), and Solr in Docker
  - **`cargo:run`** starts Tomcat, which runs the RMap API and RMap HTML web applications
> Because the runtime configuration depends on the presence of the `docker.host.address` property, and because that property is only available when the [Docker Maven Plugin](https://dmp.fabric8.io/) is invoked, `docker:start` must be invoked with `cargo:run` on the same command line.
- the runtime can be stopped by typing `CTRL-C` at the console, followed by `mvn docker:stop`
- `mvn clean` will remove any data created by the runtime

The RMap API and web interface will start on a random, high-numbered port (e.g. `55349`, actual port is output to the console), available at the `/api` and `/app` contexts, respectively.  For example:
* `http://localhost:55349/app`
* `http://localhost:55349/api`

Upon startup, a database and triplestore will be created under the `integration/target` directory, and a single RMap Agent will be created in the database.  The created database and triplestore may be preserved across restarts of the developer runtime, provided that a `mvn clean` is _not_ run.  Removing the `integration/target` directory will delete any data in the triplestore or database.

## Configuration
RMap will automatically be configured with in-memory or embedded implementations of key interfaces:
* In-memory identifier generation service
* Persistent (on-disk) triplestore
* In-memory user service
* Persistent (on-disk) database

The configuration of these implementations takes place in `integration/src/main/resources/rmap.properties`.  To customize any of the supported properties of the developer runtime, edit this file before executing `mvn validate cargo:run`.

## Spring Profiles
Behind the scenes, RMap uses [Spring Profiles](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/beans.html#beans-environment) to activate beans (or graphs of beans) at runtime.  Example supported profiles are:
* `inmemory-triplestore`, `integration-triplestore` and `http-triplestore`
* `inmemory-db`, `integration-db`, and `persistent-db`
* `inmemory-idservice` , its analog `http-idservice`, and `ark-idservice` which can be used with [EZID's](https://ezid.cdlib.org/) ARK minting service.
* `embedded-solr` and `http-solr`
* `mock-kafka` and `prod-kafka`

The use of an in-memory profile is mutually exclusive with its analog.  For example, activating the `inmemory-triplestore` _and_ the `http-triplestore` at the same time is not supported.

For production (i.e. the RMap API and HTML UI web applications), the following profiles are active:
* `persistent-db`
* `ark-idservice`
* `http-triplestore`
* `http-solr`
* `prod-kafka`
* `auto-start-indexing`

For development (i.e. when executing unit tests), the following profiles are active:
* `inmemory-triplestore`
* `inmemory-db`
* `inmemory-idservice`
* `embedded-solr`
* `mock-kafka`

For integration tests, the following profiles are active:
* `integration-triplestore`
* `integration-db`
* `inmemory-idservice`
* `http-solr`
* `prod-kafka`

The `rmap.properties` configuration file is used for runtime configuration; most properties specified therein will override the default values.  

By default, `rmap.properties` is discovered as the class path resource `/rmap.properties`.  A different resource can be specified by the system property `rmap.configFile`.  For example, `-Drmap.configFile=file:///path/to/rmap.conf` will configure RMap from a `file:/` URL pointing to `rmap.conf` instead of discovering `/rmap.properties` on the class path.  This can be useful when preserving RMap settings across application upgrades.

## Logging
Logging for the runtime is configured in two places.  

### Tomcat Logging (java.util.logging)
Logging for the Tomcat web application itself is managed in `integration/src/test/resources/logging.properties`.  Tomcat, and some other 3rd-party dependencies of RMap (e.g. OkHttp), use `java.util.logging`.  If you are debugging a Tomcat container startup problem, you may want to modify the logging levels here.

### RMap Logging (slf4j)
To modify the logging level of RMap or the majority of RMap 3rd-party dependencies, edit `integration/src/main/resoures/logback.xml`.  This is the log file you would edit for debugging RMap, Spring, and Hibernate.

# Integration Tests
RMap integration tests use the same developer runtime documented above.  When integration tests are executed, the following Spring profiles are activated:
* `integration-triplestore`
* `integration-db` (Docker)
* `inmemory-idservice`
* `http-solr` (Docker)
* `prod-kafka` (Docker)

Hibernate is used to generate the database schema, and Spring is used to populate the database tables and to create a RDF4J HTTP triplestore.  The purpose of the `integration-*` profiles is to manage the persistent state created by running the  [developer runtime](#developer-runtime) or executing integration tests.  The integration profiles provide a Spring configuration that is used to preserve existing data in the database and triplestore.  This insures that a developer can inspect the state of the database and triplestore after an integration test failure.  Without these mechanisms, it would be difficult to debug a failing integration test, or to support re-starts of the developer runtime.

## Integration Environment
The integration environment attempts to match the production environment as closely as possible.  The environment is configured inside the `integration` module's Maven POM.  The Maven [lifecycle](http://maven.apache.org/ref/3.3.9/maven-core/lifecycles.html#default_Lifecycle) is leveraged to start up the various services to support the IT environment.

The `process-test-resources` phase copies the RDF4J Workbench and Triplestore web applications to `target/`, so they can be exposed to the Docker as a volume.  See the `rdf4j` configuration in `docker-compose.yaml` for more detail.

The next relevant lifecycle phase is the `pre-integration-test` phase.  The Cargo Maven plugin is used to configure and launch an instance of Tomcat.  The Tomcat instance contains  the RMap API and HTML UI web applications under test.  A lot happens inside of this phase, which will be discussed a bit later.

Then the `integration-test` phase is started, and the Maven Failsafe plugin takes over.  The actual integration tests execute in this phase, exercising the HTTP endpoints of the RMap API and HTML UI.

Finally, the `post-integration-test` phase stops Tomcat, and the `verify` phase insures that the integration test results pass.  If the ITs fail, or if an error is encountered in any of the previous phases, the build will be failed.

### Database and Triplestore initialization
When the `pre-integration-test` phase is entered, Tomcat is started with two web applications:
 * RMap API (`api` module)
 * RMap HTML UI (`webapp` module)
 
Docker is started with four applications: 
 * RDF4J HTTP server (provides an HTTP API to _existing_ RDF4J triplestores) and the RDF4J Workbench HTML UI (provides an HTML UI to _create_ and manage RDF4J triplestores)
 * Solr
 * Kafka
 * Zookeeper
 * Postgres

 When the RMap applications start, a few things happen:
 1. Hibernate connects to the database and creates the table schema for RMap if it doesn't already exist.
 2. Spring JDBC initialization populates the database with an RMap Agent, used by the ITs to authenticate to the API and perform tests.  If any data exists in the database, initialization will _not_ occur; existing data is preserved.
 3. Upon construction, the `Rdf4jHttpTriplestore` will attempt to _create_ a RDF4J triplestore using the RDF4J Workbench web application.
   
It is important to remember that these initialization steps only occur in the integration environment.  More specifically, they only occur when the `integration-db` and `integration-triplestore` Spring profiles are active.  When the integration profiles are active, collaborating beans are wired together such that they are compelled to perform initialization.  In a production environment, these profiles are not active, and no initialization of any kind takes place.

By performing this initialization automatically as part of the integration environment, integration test classes do not have to worry about mundane issues like database creation or triplestore availability.


