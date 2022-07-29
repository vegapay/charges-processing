#!/usr/bin/env bash

sudo docker stop charges-processing

sudo docker rm charges-processing

sudo docker rmi -f 898030347853.dkr.ecr.ap-south-1.amazonaws.com/charges-processing:latest

sudo docker login -u AWS -p $(aws ecr get-login-password --region ap-south-1) 898030347853.dkr.ecr.ap-south-1.amazonaws.com

sudo docker pull 898030347853.dkr.ecr.ap-south-1.amazonaws.com/charges-processing:latest

sudo docker run -p 8086:8086 --name charges-processing 898030347853.dkr.ecr.ap-south-1.amazonaws.com/charges-processing:latest
