#!/bin/bash

prefix="[createInfrastructure]"
echo "${prefix} Building modules..."
echo

mvn clean install

echo
echo "${prefix} Setting up docker images..."
echo

docker build -t api-gateway:1.0 -f api-gateway/Dockerfile api-gateway
docker build -t user-service:1.0 -f user-service/Dockerfile user-service
docker pull postgres


echo
echo "${prefix} Applying kubernetes files..."
echo

kubectl apply -f infrastructure

echo
echo "${prefix} Finished creating infrastructure!"