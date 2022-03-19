# hsm-api project

This project uses Quarkus, see https://quarkus.io/ .

Tested with OpenJDK 17 on Ubuntu 20.04 (in WSL2).

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

**Atttention!** The native image seems to have an issue with the embedded ressource of class KeyResource.

You can create a native executable using: `./mvnw package -Pnative`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: `./mvnw package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your native executable with: `./target/hsm-api-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image.

## Create Container Image

To create a container image of the aplication, use:

```
./mvnw clean package -Dquarkus.container-image.build=true -Dquarkus.container-image.tag=karl/hsm-api:latest
```

Or alternatively:
```
./mvnw package
docker build -f src/main/docker/Dockerfile.jvm -t karl/hsm-api:latest .
```

Then, you can run database and application using `docker-compose` like this:

```
docker-compose --file src/main/docker/docker-compose.yaml up -d
docker run --interactive --tty --rm --publish 8080:8080 --network=host karl/hsm-api:latest
```  

And stop application and database when finished:

```  
docker-compose --file src/main/docker/docker-compose.yaml up -d
```  

## Setup DB

The JUnit tests start the required PostgreSQL DB automatically. 

If you start the application alone, you must provide a running DB.
To start PostgreSQL inside a container with a empty database suitable for this project:

```
docker run --name postgres-hsm --ulimit memlock=-1:-1 -it --rm=true --memory-swappiness=0 -e POSTGRES_USER=hsm -e POSTGRES_PASSWORD=hsm -e POSTGRES_DB=hsm_db -p 5432:5432 postgres:14
```

