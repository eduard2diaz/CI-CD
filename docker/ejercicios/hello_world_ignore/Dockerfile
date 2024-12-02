FROM node

WORKDIR /app
COPY . .
RUN npm install
EXPOSE 3000
CMD [ "node", "app.js"]

#FIJATE que si definimos un fichero .dockerignore, parecido a como pasa con git, todos los focheros y carpetas definidos dentro van a ser ignorados cuando se use en el Dockerfile
#el comando COPY. Ojo, esto no funciona si se hace un binding de todo un proyecto en vez de un COPY