# stage1 as builder
FROM node:10-alpine as builder

RUN mkdir app-ui

# # Copy the package.json to install dependencies
# COPY package.json yarn.lock ./app-ui/

WORKDIR /app-ui

# # Install the dependencies and make the folder
# RUN yarn install

COPY /. .

RUN ls
# Build the project and copy the files
RUN yarn run ng build -- --prod

FROM nginx:alpine

#!/bin/sh

COPY /nginx/default.conf /etc/nginx/nginx.conf

## Remove default nginx index page
RUN rm -rf /usr/share/nginx/html/*

# Copy from the stage 1
COPY --from=builder /app-ui/dist/project-fe /usr/share/nginx/html

EXPOSE 80 80

ENTRYPOINT ["nginx", "-g", "daemon off;"]
