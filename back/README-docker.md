# Docker Run Guide

This project includes a multi-stage Docker image based on Alpine + Java 21.

## Build image

```bash
docker build -t fullstackbp-back:latest .
```

## Run container

```bash
docker run --rm -p 8080:8080 --name fullstackbp-back fullstackbp-back:latest
```

The API will be available at `http://localhost:8080`.

