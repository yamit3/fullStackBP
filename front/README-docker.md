# How to Use

This guide explains how to build and run the Angular Frontend application using Docker.


## Quick Start

Build and run with Docker:

```bash
docker build -t angular-app .
docker run -p 4200:4200 angular-app

```
Or with Docker Compose:

```bash
docker-compose up

```

Build with specific tag:

```bash
docker build -t angular-app:latest .

```
