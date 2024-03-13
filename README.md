# SpringKubeInfra
Barebones Spring Boot infrastructure for a simple user system and authentication using Json Web Tokens in Spring Cloud Gateway.

# How to run
Before you start, make sure you have docker and a kubernetes distribution installed on your machine.
You will need these for the scripts to work.

To build all resources and set up the infrastructure in kubernetes, run the script: `createInfrastructure.sh`
To tear down the kubernetes objects after you're done, run the script: `deleteInfrastructure.sh`

# Testing
I recommend using `Postman` to test the endpoints.
The gateway is exposed on port `30000`

Endpoint | Method | Request | Response | Authorization Header | Description
------------- | ------------- | ------------- | ------------- | ------------- | -------------
`/users` | GET | `{}` |`[{"username": "thomas", "password": "12345"}]`| JWT returned from `/users/login` | Lists all users that have registered
`/users/id` | GET | `{"id": 1}` | `{"username": "thomas", "password": "12345"}` | JWT returned from `/users/login` | Gets user data by their ID
`/users/login` | POST | `{"username": "thomas" "password": "12345"}` | `"accessToken": JWT` | None | Logs in a user and returns a JWT access token
`/users/register` | POST | `{"username": "thomas" "password": "12345"}` | `{}` | None | Register a user with username and password
