#!/bin/bash

if [ -z "$DOCKERHUB_USER" ]; then
  echo "Error: DOCKERHUB_USER environment variable is not set: $DOCKERHUB_USER"
  exit 1
fi
if [ -z "$DOCKERHUB_TOKEN" ]; then
  echo "Error: DOCKERHUB_TOKEN environment variable is not set"
  exit 1
fi
if [ -z "$OPENAI_API_KEY" ]; then
  echo "Error: OPENAI_API_KEY environment variable is not set"
  exit 1
fi
if [ -z "$DB_URL" ]; then
  echo "Error: DB_URL environment variable is not set"
  exit 1
fi
if [ -z "$CDN_DOMAIN" ]; then
  echo "Error: CDN_DOMAIN environment variable is not set"
  exit 1
fi
if [ -z "$S3_ACCESS_KEY" ]; then
  echo "Error: S3_ACCESS_KEY environment variable is not set"
  exit 1
fi
if [ -z "$S3_SECRET_KEY" ]; then
  echo "Error: S3_SECRET_KEY environment variable is not set"
  exit 1
fi
if [ -z "$S3_BUCKET_NAME" ]; then
  echo "Error: S3_BUCKET_NAME environment variable is not set"
  exit 1
fi

IMAGE_NAME="luisghtz/personalwebapss:myaichatapi-springboot"
CONTAINER_NAME="myaichatapi-springboot"
LOCALPORT=3001
DOCKERPORT=8080
# Login to Docker Hub using the access token from the OS environment variable
echo "$DOCKERHUB_TOKEN" | docker login --username "$DOCKERHUB_USER" --password-stdin

# Stop and remove any running container that uses the image
if docker ps -a --filter "name=${CONTAINER_NAME}" | grep -q "${CONTAINER_NAME}"; then
  echo "Stopping container ${CONTAINER_NAME}..."
  docker stop ${CONTAINER_NAME}
  echo "Removing container ${CONTAINER_NAME}..."
  docker rm ${CONTAINER_NAME}
fi

# Remove the existing image if it exists
if docker images | grep -q "$(echo $IMAGE_NAME | cut -d':' -f1)"; then
  echo "Removing image ${IMAGE_NAME}..."
  docker rmi ${IMAGE_NAME}
fi

# Pull the new image from Docker Hub
echo "Pulling image ${IMAGE_NAME}..."
docker pull ${IMAGE_NAME}

# Run a new container with the specified flags
echo "Running new container ${CONTAINER_NAME}..."
docker run -d -p ${LOCALPORT}:${DOCKERPORT} --network dbs -e OPENAI_API_KEY=${OPENAI_API_KEY} -e DB_URL=${DB_URL} -e CDN_DOMAIN=${CDN_DOMAIN} -e S3_ACCESS_KEY=${S3_ACCESS_KEY} -e S3_SECRET_KEY=${S3_SECRET_KEY} -e S3_BUCKET_NAME=${S3_BUCKET_NAME} --name ${CONTAINER_NAME} ${IMAGE_NAME}
