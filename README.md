Overview
========
Flux Capacitor is a Java-based reference app demonstrating the following Netflix Open Source components (in alphabetical order):
* [archaius] (https://github.com/Netflix/archaius)
* [astyanax] (https://github.com/Netflix/astyanax) - coming soon
* [blitz4j] (https://github.com/Netflix/blitz4j)
* [curator] (https://github.com/Netflix/curator) - coming soon
* [eureka] (https://github.com/Netflix/eureka)
* [exhibitor] (https://github.com/Netflix/exhibitor) - coming soon
* [governator] (https://github.com/Netflix/governator)
* [hystrix] (https://github.com/Netflix/hystrix)
* [karyon] (https://github.com/Netflix/karyon)
* [ribbon] (https://github.com/Netflix/ribbon)
* [servo] (https://github.com/Netflix/servo)

In addition to the Netflix OSS components listed above, Flux Capacitor uses the following Open Source components:
* graphite 
* jersey
* jetty
* netty
* tomcat

This project demonstrates many aspects of a complete distributed, scalable application from dynamic configuration to real-time metrics.

This app can be deployed standalone, in a data center, or in an AWS (Amazon Web Services) environment with just a few configuration changes.

Modules
=======
flux-core
-----------
* Shared classes between edge and middletier.

flux-edge
-----------
* Customer-facing edge service.

flux-middletier
-----------------
* Internal middletier service.

Pre-baked AMIs
==============
* Coming soon

Documentation
==============
Please see [wiki] (https://github.com/cfregly/fluxcapacitor/wiki) for detailed documentation.
