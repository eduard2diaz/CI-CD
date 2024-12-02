FROM node

WORKDIR /app

COPY package.json .

RUN npm install

COPY . .

CMD ["node", "app.js"]

#PRIMERAMENTE CREAMOS LA RED:       docker network create favorites-net
#CREANDO EL CONTENEDOR DE MONGO:    docker run -d --name mongodb --network favorites-net mongo
#Creamos la imagen del proyecto:    docker build -t favorites .
#Corremos el contenedor:            docker run --name favorites --network favorites-net -p 3000:3000 favorites