# RMap
The RMap Project is an Alfred P. Sloan Foundation-funded initiative undertaken by the Data Conservancy, Portico, and IEEE. The goal of the RMap Project is to make it possible to capture and preserve the many-to-many complex relationships among the distributed components of a scholarly work. 

The RMap software allows users to create and manage these maps of relationships as RDF graphs in a format called RMap DiSCOs (Distributed Scholarly Compound Objects).  DiSCOs are named graphs consisting of a persistent identifier, a list of aggregated works ("ore:aggregates"), an optional list of additional assertions (which must form a connected graph with the aggregated works), and several other optional metadata fields.  The provenance of each DiSCO is captured as RMap Events, and each Event cites the RMap Agent responsible for triggering it.

Further documentation and links about the RMap Project are available on the [RMap website](http://rmap-project.info/) and [technical wiki](https://rmap-project.atlassian.net).  

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

## Download
The current version of RMap and supporting scripts can be [downloaded from the Github](https://github.com/rmap-project/rmap/releases).  The [installation documentation](https://rmap-project.atlassian.net/wiki/display/RMAPPS/Installation) can be found on the RMap technical wiki.

## Compiling from source
### System Requirements

- Java 8
- Maven 2.4

### Maven install
Each component should be compiled using mvn install. If tests are to be run during compilation, testdata should be compiled first, followed by core and auth, and finally api and webapp.
```
mvn clean install
``` 
.war files will be found in the /target folder of webapp and api. These can be installed per the [installation documentation](https://rmap-project.atlassian.net/wiki/display/RMAPPS/Installation).

### Developer runtime

Developers can run a local instance of RMap by running:

- `mvn clean install`
- then `cd` into the `integration` directory and run `mvn validate cargo:run`
 
The RMap API and web interface will start on a random, high-numbered port (e.g. `55349`, actual port is output to the console), available at the `/api` and `/app` contexts, respectively.
 
A local Apache Derby database will be created under `target/derby` named `testdb`.  In-memory implementations of various services are used, including an in-memory triplestore, id and user service.

To customize supported properties of the developer runtime, edit `integration/src/main/resoures/rmap.properties` before executing `mvn validate cargo:run`.

### Production runtime

Deployers of RMap can choose to place `/rmap.properties` on the classpath, or specify a location to a configuration file.  Any supported RMap configuration property can be specified in this file.

To specify a location for a configuration file, define a system property named `rmap.configFile`, which should be a `file:///` url that references a Java properties file.  If `rmap.configFile` is not defined, the classpath resource `/rmap.properties` is used by default.  

