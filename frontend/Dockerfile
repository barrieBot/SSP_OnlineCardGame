# Stage 1: Build the Angular application
FROM node:20-slim AS build

WORKDIR /src

# Copy package.json and package-lock.json first to leverage Docker cache
COPY package.json package-lock.json ./

# Install Angular CLI globally
RUN npm install -g @angular/cli

# Install project dependencies based on package-lock.json
RUN npm ci

# Copy the rest of your application source code
COPY . .

# Run the Angular production build
# Confirming the copy path for the next step, assuming it's still frontend/browser
RUN ng build --configuration=production --base-href=/

# Stage 2: Serve the application with Nginx
FROM nginx:stable AS final
EXPOSE 80

# Copy your custom Nginx configurations
COPY ./etc/nginx/nginx.conf /etc/nginx/nginx.conf
COPY ./etc/nginx/default.conf /etc/nginx/conf.d/default.conf

# Revert the copy path to include 'browser' - based on the previous error, this is likely correct
COPY --from=build /src/dist/frontend/browser /usr/share/nginx/html

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
