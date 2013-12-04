FROM cfregly/flux-edge 
MAINTAINER Chris Fregly "chris@fregly.com"

echo "export JAVA_OPTS=\"-Xms512m -Xmx1024m -Dlog4j.rootLogger=DEBUG,stdout,R -Darchaius.deployment.applicationId=edge -Darchaius.deployment.environment=docker -Dcom.netflix.karyon.eureka.datacenter.type=MyOwn -Darchaius.dynamicPropertyFactory.registerConfigWithJMX=true -Daws.accessKeyId=AKIAJRQB6RBNQBTHGQGQ -Daws.secretKey=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\"" >> /usr/share/tomcat7/bin/setenv.sh

CMD service tomcat7 start && tail -f /var/lib/tomcat7/logs/catalina.out
