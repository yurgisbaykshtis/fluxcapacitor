Overview
========

Flux Capacitor is a Java-based referencea app demonstrating the following Netflix Open Source components (in alphabetical order):
- archaius
- asgard (coming soon)
- astyanax
- blitz4j
- curator (coming soon)
- eureka
- exhibitor (coming soon)
- governator
- hystrix
- ribbon
- servo

In addition to the Netflix OSS components listed above, Flux Capacitor uses the following Open Source components:
- jersey
- graphite 
- netty io
- tomcat
- jetty
- twitter bootstrap

Flux Capacitor covers everything from dynamic configuration to scalable deployment to real-time metrics.

Flux Capacitor can be deployed standalone, in a data center, or in an AWS (Amazon Web Services) environment.

Modules
=======

flux-core
-----------
Shared classes between edge and middletier.

flux-edge
-----------
Customer-facing edge service.

flux-middletier
-----------------
Internal middletier service.

Feature Highlights
==================
- Coming soon

Pre-baked AMIs
==============
- Coming soon

How to Build
============
Flux Capacitor uses Maven as its build mechanism.  After cloning the repo, run the following command from the ${FLUX_HOME} parent directory:

	mvn clean install

How to Run
==========

Build and Deploy Eureka Server
------------------------------
- Update the following files in ${EUREKA_HOME}/eureka-server/conf/

	eureka-client.properties
	eureka-client-test.properties
	eureka-client-prod.properties
	(and any other eureka-client-env.properties that are appropriate)

to use the following urls with port 8080 versus port 80:

	eureka.serviceUrl.defaultZone=http://localhost:8080/eureka/v2/
	eureka.serviceUrl.default.defaultZone=http://localhost:8080/eureka/v2/
	
- Build the new eureka-server-x.x.x.war per the following: https://github.com/Netflix/eureka/wiki/Building-Eureka-Client-and-Server
- Copy eureka/eureka-server/build/libs/eureka-server-x.x.x.war to the tomcat/webapps/eureka.war

Deploy Hystrix Dashboard
------------------------
- Build the latest hystrix-dashboard-x.x.x.war per the following: https://github.com/Netflix/Hystrix/wiki/Dashboard
- Copy Hystrix/hystrix-dashboard/build/libs/hystrix-dashboard-x.x.x.war to the tomcat/webapps/hystrix-dashboard.war

Install Python and Graphite 0.9.10 (Optional)
---------------------------------------------
- http://graphite.wikidot.com/installation

Start Graphite (Optional)
-------------------------

	${GRAPHITE_HOME}/bin/carbon-cache.py start

Start Tomcat (Admin)
--------------------
- From the ${TOMCAT_HOME} directory, run the following script:	
	
	./startup.sh

Start MiddleTier
----------------
- Make sure the APP_ENV environment variable is set per the following:
	
	export APP_ENV=dev
 
- From the ${FLUX_HOME} directory, run the middletier service on port 9191 by running the following script:

	bin/start-middletier
	
Start Edge
----------
- Make sure the APP_ENV environment variable is set per the following:
	
	export APP_ENV=dev

- From the ${FLUX_HOME} directory, run the edge service on port 9090 by running the following script:
	
	bin/start-edge
	
Note:  you must run the start-edge script from the bin/ directory or the jsp's won't be found.  I'm fixing this.
	
Test It!
--------
Eureka
- http://localhost:8080/eureka 

Flux Capacitor
- Once tomcat/eureka starts up and the middletier service has registered, navigate to the following in your favorite browser:
	
	http://localhost:9090/fraggle.jsp  
	
Note: The middletier service is configured to randomly sleep anywhere between 0 and 2s.  The edge service is configured to timeout and fallback ("Fraggle Fallback!" response) if the middletier sleeps >1s.  
Therefore, it may take a few refreshes to see a successful response of "Fraggle Rock!".

Hystrix
- http://localhost:8080/hystrix-dashboard/monitor/monitor.html?stream=http%3A%2F%2Flocalhost%3A9090%2Fhystrix.stream
or
- curl http://localhost:9090/hystrix.stream

Note:  The Hystrix dashboard will show "Loading..." until there is data to show.  Hit refresh on the Flux Capacitor jsp above a few times and the dashboard will light up. 

Graphite
- Coming soon
