# How to Use

This guide explains how to build and run the Angular Frontend application using Docker.


## Quick Start

Build and run with Docker:

```bash
docker build -t angular-app:latest .
docker run --rm -p 4000:4000 --name fullstackbp-front angular-app:latest

```
Or with Docker Compose:

```bash
docker-compose up

```

Build with specific tag:

```bash
docker build -t angular-app:latest .

```
