cd ~/fluxcapacitor
sudo service tomcat7 start
sudo /opt/graphite/bin/carbon-cache.py start
sudo nohup /opt/graphite/bin/run-graphite-devel-server.py --port 8181 /opt/graphite > graphite.out &
sudo /usr/share/zookeeper/bin/zkServer.sh start
sudo service cassandra start
nohup java -Darchaius.deployment.applicationId=middletier -Darchaius.deployment.environment=$APP_ENV -jar flux-middletier/build/libs/flux-middletier-0.1.0-SNAPSHOT.jar > logs/flux-middletier.out &
nohup java -Darchaius.deployment.applicationId=edge -Darchaius.deployment.environment=$APP_ENV -jar flux-edge/build/libs/flux-edge-0.1.0-SNAPSHOT.jar > logs/flux-edge.out &