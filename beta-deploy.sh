#!/usr/bin/env bash

mvn compile
mvn package

sudo docker login -u AWS -p $(aws ecr get-login-password --region ap-south-1) 898030347853.dkr.ecr.ap-south-1.amazonaws.com

sudo docker build -t charges-processing .

sudo docker tag charges-processing:latest 898030347853.dkr.ecr.ap-south-1.amazonaws.com/charges-processing:latest

sudo docker push 898030347853.dkr.ecr.ap-south-1.amazonaws.com/charges-processing:latest

scp -i "aws-keypair.pem" beta_command_on_ec2_charge_processing.sh ec2-user@ec2-3-110-77-133.ap-south-1.compute.amazonaws.com:/home/ec2-user
echo "Copied latest 'beta_command_on_ec2_transaction.sh' file from local machine to ec2 instance"

ssh -i "aws-keypair.pem" ec2-user@ec2-3-110-77-133.ap-south-1.compute.amazonaws.com ./beta_command_on_ec2_charge_processing.sh
