# hsm-api project

This project uses Quarkus, see https://quarkus.io/ .

Tested with OpenJDK 17 on Ubuntu 22.04 (in WSL2).

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
./mvnw quarkus:dev
```

## Packaging and running the application

The application can be packaged using `./mvnw package`.
It produces the `hsm-api-1.0-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/hsm-api-1.0-SNAPSHOT-runner.jar`.

## Creating a native executable

You can create a native executable using: `./mvnw package -Pnative`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: `./mvnw package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your native executable with: `./target/hsm-api-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image.

## Create Container Image

### JVM Container Image

This chapter shows how to build an image containing the Java VM and the appliacation, i.e. JARs.

To create a container image of the application, use:

```bash
./mvnw clean package -Dquarkus.container-image.build=true -Dquarkus.container-image.image=karl/hsm-api:latest
```

Or alternatively:
```
./mvnw package
docker build -f src/main/docker/Dockerfile.jvm -t karl/hsm-api:latest .
```

Then, you can run database and application using `docker-compose` like this:

```
docker-compose --file src/main/docker/docker-compose.yaml up --detach
```  

And stop application and database when finished:

```  
docker-compose --file src/main/docker/docker-compose.yaml down
```  

### Native Container Image

In this chapter, we create a container image with the native executable. It will not contain a Java VM or ony JARs.

To create a native container image, type:

```
./mvnw clean package -Dquarkus.container-image.build=true -Pnative -Dquarkus.native.container-build=true -Dquarkus.container-image.image=karl/hsm-api-native:latest
```

This uses a GraalVM/Mandrel from a docker image. 

If you have build and tested the native executable before (which takes several minutes) and want this packaged in an image, type:

```
./mvnw package -Dquarkus.container-image.build=true -Pnative -Dquarkus.native.reuse-existing=true -Dquarkus.container-image.image=karl/hsm-api-native:latest
```

Then, you can run database and application using `docker-compose` like this:

```
docker-compose --file src/main/docker/docker-compose-native.yaml up --detach 
```  

And stop application and database when finished:

```  
docker-compose --file src/main/docker/docker-compose-native.yaml down
```  

## Setup DB

The JUnit tests and quarkus dev mode start the required PostgreSQL DB automatically. 

If you start the application in prod mode, you must provide a running DB.
To start PostgreSQL inside a container with a empty database suitable for this project:

```
docker run --name postgres-hsm --ulimit memlock=-1:-1 -it --rm=true --memory-swappiness=0 -e POSTGRES_USER=hsm -e POSTGRES_PASSWORD=hsm -e POSTGRES_DB=hsm_db -p 5432:5432 postgres:14
```

## Push Container Image

To build the native container image and push it to the registry configured in the `applicatoin.properties` use:

```
./mvnw package -Pnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dquarkus.container-image.push=true
```

## Logging

To enable json format application logging, set this config:

```
quarkus.log.console.json=true
```

## Update Dependencies

To check if a new quarkus release is available, use
```
./mvnw quarkus:update
```

See `./mvnw quarkus:help -Ddetail -Dgoal=update`

To update dependencies like quarkus to their latest release versions, use

```
./mvnw versions:use-latest-releases
```

Check for new plugins:

```
./mvnw versions:display-plugin-updates 
```

See documentation of [Maven Versions Plugin](https://www.mojohaus.org/versions-maven-plugin/index.html).

## Keycloak


```
docker run --name keycloak-19 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin -p 8180:8080 quay.io/keycloak/keycloak:19.0 start-dev

wslview http://localhost:8180/

# Admins Console/<Click "Master" at left top and "Create Realm">/<Real name: hsm-api>/<create>
./mvnw clean package

java -jar target/quarkus-app/quarkus-run.jar

export access_token=$(curl --insecure --no-progress-meter --request POST --url http://localhost:8180/realms/hsm-api/protocol/openid-connect/token --user hsm-cli:secret --header 'content-type: application/x-www-form-urlencoded' --data 'username=hsm-user-1&password=hsm-user-1&grant_type=password' | jq --raw-output '.access_token' )



curl -v --request GET --url http://localhost:8080/api/users/me --header "Authorization: Bearer "$access_token
# HTTP 200 OK

curl -v --request GET --url http://localhost:8080/api/admin --header "Authorization: Bearer "$access_token
# HTTP 403 Forbidden

export access_token=$(curl --insecure --no-progress-meter --request POST --url http://localhost:8180/realms/hsm-api/protocol/openid-connect/token --user backend-service:secret --header 'content-type: application/x-www-form-urlencoded' --data 'username=admin&password=admin&grant_type=password' | jq --raw-output '.access_token' )

curl -v --request GET --url http://localhost:8080/api/admin --header "Authorization: Bearer "$access_token
# HTTP 200 OK

```