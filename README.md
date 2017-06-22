# RMap
The RMap Project is an Alfred P. Sloan Foundation-funded initiative undertaken by the Data Conservancy, Portico, and IEEE. The goal of the RMap Project is to make it possible to capture and preserve the many-to-many complex relationships among the distributed components of a scholarly work. 

The RMap software allows users to create and manage these maps of relationships as RDF graphs in a format called RMap DiSCOs (Distributed Scholarly Compound Objects).  DiSCOs are named graphs consisting of a persistent identifier, a list of aggregated works ("ore:aggregates"), an optional list of additional assertions (which must form a connected graph with the aggregated works), and several other optional metadata fields.  The provenance of each DiSCO is captured as RMap Events, and each Event cites the RMap Agent responsible for triggering it.

Further documentation and links about the RMap Project are available on the [RMap website](http://rmap-project.info/) and [technical wiki](https://rmap-project.atlassian.net).  

# Downloading RMap
The current version of RMap and supporting scripts can be [downloaded from the Github](https://github.com/rmap-project/rmap/releases).  The [installation documentation](https://rmap-project.atlassian.net/wiki/display/RMAPPS/Installation) can be found on the RMap technical wiki.

# Configuring and running RMap
Deployers of RMap can choose to place `/rmap.properties` on the classpath, or specify a location to a configuration file.  Any supported RMap configuration property can be specified in this file.

To specify a location for a configuration file, define a system property named `rmap.configFile`, which should be a `file:///` url that references a Java properties file.  If `rmap.configFile` is not defined, the classpath resource `/rmap.properties` is used by default.  

# Developer Documentation
Instructions for building RMap from source, and other development-related detail can be found in [DEVELOPER.md](DEVELOPER.md).