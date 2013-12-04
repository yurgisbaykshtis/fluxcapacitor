FROM cfregly/eureka 
MAINTAINER Chris Fregly "chris@fregly.com"

echo "export JAVA_OPTS=\"-Xms512m -Xmx1024m -Deureka.environment=prod -Daws.accessKeyId=AKIAJRQB6RBNQBTHGQGQ -Daws.secretKey=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\"" >> /usr/share/tomcat7/bin/setenv.sh

CMD service tomcat7 start && tail -f /var/lib/tomcat7/logs/catalina.out
