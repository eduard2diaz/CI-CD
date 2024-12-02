# Contenido

## Objetivo
El objetivo de este ejercicio creado por mi es demostrar que las variables de entorno que pongas en `Dockerfile` no necesariamente necesitan estar definidas en el `docker-compose.yaml`, o viceversa. La idea es que las que pongas en el `docker-compose.yaml` sirven para configurar la imagen, pero si en el `Dockerfile` existe una variable de entorno con el mismo nombre, solamente la sobreescribira.

## Explicacion
A continuación se describen las siguientes secciones de mi `docker-compose.yaml`

```yaml
  config-server-1:
    build:
      context: .
      dockerfile: Dockerfile_no_env
    ports:
      - "8083:8080"
```

Despliega un contenedor con la imagen que no utiliza variables de entornos, pues `Dockerfile_no_env` es

```Dockerfile
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package

FROM openjdk:17-jdk
WORKDIR /app
COPY --from=build /app/target/greet.jar greet.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/greet.jar"]
```

y el proyecto de spring boot usara el valor que tenga definida la variable `spring.application.name` en el fichero `application.properties`.

> spring.application.name=${APP_NAME:hola}

```yaml
    config-server-2:
    build:
      context: .
      dockerfile: Dockerfile_no_env
    ports:
      - "8084:8080"
    environment:
      - APP_NAME=COMPOSER_2
```

Similar al ejemplo anterior pero ahora se esta inicializando la variable de entorno `APP_NAME` desde el `docker-compose.yaml` y este seria el valor que tomara la variable `spring.application.name`

```yaml
      config-server-3:
    build:
      context: .
      dockerfile: Dockerfile_with_env
    ports:
      - "8085:8080"
```
En este caso en el `docker-compose.yaml` no se esta pasando variable entorno, pero en la imagen si, pues el `Dockerfile_with_env` es

```Dockerfile
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package

FROM openjdk:17-jdk
WORKDIR /app
COPY --from=build /app/target/greet.jar greet.jar
ENV APP_NAME=EDUARDO_GREET
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/greet.jar"]
```

por lo que la variable `spring.application.name` tendra valor `EDUARDO_GREET`.

Por ultimo 

```yaml
config-server-4:
    build:
      context: .
      dockerfile: Dockerfile_with_env
    ports:
      - "8086:8080"
    environment:
      - APP_NAME=COMPOSER_4
```
define la variable de entorno `APP_NAME`, pero como esta fue tambien definida en el `Dockerfile_with_env` entonces sobreescribe el valor que tiene en `Dockerfile_with_env` y utiliza el valor que tiene definido en el `docker-compose.yaml`. Por lo que la variable `spring.application.name` tendra valor `COMPOSER_4`.