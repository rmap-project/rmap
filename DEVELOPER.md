# RMap Developer Documentation

## RMap source overview
The RMap software has 4 main modules that work together plus one small module for test data.

### core
As the name suggests, this contains the core code for the RMap software. It defines the RMap data model, interacts with the underlying triplestore, and provides the tools needed to manage and access RMap data.
### auth
*Core* functions support the creation of an RMap Agent, and each time you write to RMap a valid Agent URI is required.  The *core* module is not otherwise involved in authentication or user management. Instead, the web application and API handle the authorization and authentication of users. To do this they both use the *auth* component. This coordinates between the users that have logged in through the website, the API keys those users create, and the association of an RMap Agent. The *auth* module is supported by a small user database. 
### api
The api module provides a REST API to expose the major functions of RMap.  These include functions to manage DiSCOs and retrieve triples that reference particular resource URIs.  The technical wiki contains a [full description of the API functions](https://rmap-project.atlassian.net/wiki/display/RMAPPS/API+Documentation).  Read functions are available without an API key. Write funtions require the user to login through the website and register for an API key. 
### webapp
The web application allows users to browse RMap Agents, DiSCOs, and Events in a visual and interactive way.  It also supports the configuration of RMap Agents and API keys to be used for write-access to the RMap API. A [live demo site](https://demo.rmap-hub.org/app) is available to try out.
### testdata
The test data module contains RDF files for DiSCOs and Agents. This data is used to generate test data for the JUnit tests in the other modules.

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
The `war` files will be found in the `/target` folder of `webapp` and `api`. These can be installed per the [installation documentation](https://rmap-project.atlassian.net/wiki/display/RMAPPS/Installation).  If, however, you simply need to run a local instance of RMap for development purposes, see below for instructions on the [developer runtime](#developer-runtime)

# [Developer runtime](#developer-runtime)
## Running RMap
Developers can run a local instance of RMap by executing:

- `mvn clean install` from the base RMap directory
- then `cd` into the `integration` directory and run `mvn validate cargo:run`

The developer runtime is appropriate for testing modifications to the source code, API, or web user interface.  It is _not_ appropriate for production.  For example, data will be lost when stopping the developer runtime, because data are only persisted in memory, not on disk. 
 
The RMap API and web interface will start on a random, high-numbered port (e.g. `55349`, actual port is output to the console), available at the `/api` and `/app` contexts, respectively.  For example:
* `http://localhost:55349/app`
* `http://localhost:55349/api`

## Configuration
RMap will automatically be configured with in-memory or embedded implementations of key interfaces:
* In-memory identifier generation service
* In-memory triplestore
* In-memory user service
* Embedded database

The configuration of these implementations takes place in `integration/src/main/resources/rmap.properties`.  To customize any of the supported properties of the developer runtime, edit this file before executing `mvn validate cargo:run`.

## Spring Profiles
Behind the scenes, RMap uses [Spring Profiles](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/beans.html#beans-environment) to activate beans (or graphs of beans) at runtime.  Example supported profiles are:
* `inmemory-triplestore` and its analog `http-triplestore`
* `inmemory-db` and its analog `persistent-db`
* `inmemory-idservice` and its analog `http-idservice`

The use of an in-memory profile is mutually exclusive with its analog.  For example, activating the `inmemory-triplestore` _and_ the `http-triplestore` at the same time is not supported.

For production, the following profiles are active:
* `http-triplestore`
* `persistent-db`
* `http-idservice`

For development, the following profiles are active:
* `inmemory-triplestore`
* `inmemory-db`
* `inmemory-idservice`

Regardless of which profiles are active, the `rmap.properties` file is used for configuration.  That means you can configure the database connectivity and the identifier generation service in the same place, regardless of which profiles are active.

## Logging 
Logging output goes to a file based on the current date.  The name of the logging file used is output to the console upon startup, and is typically named something like: `integration/target/cargo/configurations/jetty9x/logs/YYYY_MM_DD.jetty.log`.  If RMap is not behaving as you expect, often this log file will provide a clue.  

To modify the logging level of RMap, edit `integration/src/main/resoures/logback.xml` before executing `mvn validate cargo:run`.


