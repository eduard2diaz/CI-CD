FROM node:18
WORKDIR /app
COPY . .
RUN npm install
CMD ["node", "app.js"]


#Si queremos evitar que el contenedor tiene acceso a la red seria:
#docker run --network none node-app

#Puedes especificar una red personalizada o utilizar la red host en sistemas Linux para que el contenedor utilice directamente la interfaz de red del host:
#docker run --network host node-app
#Sin embargo, generalmente esto no es necesario para acceder a Internet, ya que la red bridge proporciona esa capacidad.