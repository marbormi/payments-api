# Instructions

## Launch locally
```bash
./mvnw spring-boot:run
```

## Create docker image
```bash
./mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=prommpt/paymentapi
```

## Run application with docker
```bash
docker run -p 8088:8088 prommpt/paymentapi
```