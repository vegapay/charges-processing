#!/usr/bin/env bash

mvn compile
mvn package

#Copy execute_commands_on_ec2.sh file which has commands to be executed on server... Here we are copying this file
# every time to automate this process through 'deploy.sh' so that whenever that file changes, it's taken care of
scp -i "web-server-key.pem" execute_commands_on_ec2.sh ubuntu@ec2-3-110-194-158.ap-south-1.compute.amazonaws.com:/home/ubuntu
echo "Copied latest 'execute_commands_on_ec2.sh' file from local machine to ec2 instance"

scp -i "web-server-key.pem" target/charges-processing-0.0.1-SNAPSHOT.jar ubuntu@ec2-3-110-194-158.ap-south-1.compute.amazonaws.com:/home/ubuntu
echo "Copied jar file from local machine to ec2 instance"

echo "Connecting to ec2 instance and starting server using java -jar command"
ssh -i "web-server-key.pem" ubuntu@ec2-3-110-194-158.ap-south-1.compute.amazonaws.com ./execute_commands_on_ec2.sh