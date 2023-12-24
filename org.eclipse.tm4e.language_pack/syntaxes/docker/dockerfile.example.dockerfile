#syntax=docker/dockerfile:1.4

# Use an official base image
FROM ubuntu:20.04

LABEL maintainer="Alice & Bob"

SHELL ["/bin/bash", "-euo", "pipefail", "-c"]

# Set environment variables
ENV APP_HOME /app
ENV PORT 80

# Set the working directory
WORKDIR $APP_HOME

# Copy files into the container
COPY . $APP_HOME

# Install dependencies
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    rm -rf /var/lib/apt/lists/*

# Install Python dependencies
RUN pip3 install -r requirements.txt

# Run a multiline command
RUN <<EOF
    echo "Multiline command executed!"
    echo "This is line 1"
    echo "This is line 2"
    echo "This is line 3"
EOF

# Expose a port
EXPOSE $PORT

# Define a command to run on container start
CMD ["python3", "app.py"]
