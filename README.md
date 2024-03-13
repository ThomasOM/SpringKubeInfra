# SpringKubeInfra
Barebones Spring Java infrastructure for a simple user system and authentication using Json Web Tokens in Spring Cloud Gateway.

# How to run
Before you start, make sure you have docker and a kubernetes distribution installed on your machine.
You will need these for the scripts to work.

To build all resources and set up the infrastructure in kubernetes, run the script: `createInfrastructure.sh`
To tear down the kubernetes objects after you're done, run the script: `deleteInfrastructure.sh`
