# ---- build stage ----
FROM node:20-alpine AS build
WORKDIR /app

# Vite inlines import.meta.env.* at build time, so the API base is a build arg.
ARG VITE_API_BASE=http://localhost:8080
ARG VITE_DEMO_USER=admin
ARG VITE_DEMO_DISPLAY=Master User
ARG VITE_DEMO_VDMS=VDMS760
ARG VITE_DEMO_DOCKER=right_wing
ENV VITE_API_BASE=$VITE_API_BASE \
    VITE_DEMO_USER=$VITE_DEMO_USER \
    VITE_DEMO_DISPLAY=$VITE_DEMO_DISPLAY \
    VITE_DEMO_VDMS=$VITE_DEMO_VDMS \
    VITE_DEMO_DOCKER=$VITE_DEMO_DOCKER

COPY package.json ./
RUN npm install
COPY . .
RUN npm run build

# ---- serve stage ----
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
