<img src="https://raw.github.com/cfregly/fluxcapacitor/master/docs/images/fluxcapacitor-logo.png">

Overview
========
Flux Capacitor is a Java-based reference app demonstrating the following Netflix Open Source components (in alphabetical order):
* [archaius] (https://github.com/Netflix/archaius)
* [astyanax] (https://github.com/Netflix/astyanax)
* [blitz4j] (https://github.com/Netflix/blitz4j)
* [curator] (https://github.com/Netflix/curator)
* [eureka] (https://github.com/Netflix/eureka)
* [exhibitor] (https://github.com/Netflix/exhibitor)
* [governator] (https://github.com/Netflix/governator)
* [hystrix] (https://github.com/Netflix/hystrix)
* [karyon] (https://github.com/Netflix/karyon)
* [ribbon] (https://github.com/Netflix/ribbon)
* [servo] (https://github.com/Netflix/servo)

In addition to the Netflix OSS components listed above, Flux Capacitor uses the following Open Source components:
* graphite (http://graphite.wikidot.com)
* jersey (http://jersey.java.net)
* jetty (http://jetty.codehaus.org/jetty)
* netty io (http://netty.io)
* tomcat (http://tomcat.apache.org/)

This project demonstrates many aspects of a complete distributed, scalable application from dynamic configuration to real-time metrics.

This app can be deployed standalone, in a data center, or in an AWS (Amazon Web Services) environment with just a few configuration changes.

Architecture Overview
=====================
<img src="https://raw.github.com/cfregly/fluxcapacitor/master/docs/images/fluxcapacitor-netflixoss-overview.jpg">

Module Overview
===============
flux-edge
-----------
* Customer-facing edge service.

flux-middletier
-----------------
* Internal middletier service.

flux-core
-----------
* Shared classes between edge and middletier.

Pre-baked AMIs
==============
* Coming soon

Documentation
==============
Please see [wiki] (https://github.com/cfregly/fluxcapacitor/wiki) for detailed documentation.
