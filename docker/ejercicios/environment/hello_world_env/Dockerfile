FROM node

WORKDIR /app
COPY . .
RUN npm install

#DEFINIMOS UNA VARIABLE DE ENTORNO (con un valor por defecto)
ENV PORT 3000

#USADO EL SIGNO DE $ HACEMOS REFERENCIA A LA VARIABLE DE ENTORNO
EXPOSE $PORT
CMD [ "node", "app.js"]

#FIJATE, si ejecutamos docker run -p 3000:3000 el va a funcionar bien porque el puerto por defecto por donde corre la app es el 3000, eso lo dejamos claro cuando definimos
#ENV PORT 3000
#Pero si quisieramos que la app corriese por otro puerto bastaria con modificar el valor de esa variable de entorno, por ejemplo:
#docker run -p 3000:80 -e PORT=80 environment, que es lo mismo que
#docker run -p 3000:80 --env PORT=80 environment
#Asi, si tenemos varias variables de entorno seria run -p 3000:80 -e PORT=80 -e VAR2=VALUE_VAR2 -e VAR3=VALUE_VAR3 ...  environment

#En este caso la variable de entorno PORT tomaria valor 80, y la app empezaria a correr por el puerto 80. Esto es posible tambien porque en el fijero app.js definimos que 
#la variable PORT tome su valor a traves de objeto process
#const PORT = process.env.PORT;

#Una ultima forma es leer las variables de entorno a partir de un fichero de configuracion (.env),
#y luego bastaria con correr el sig comando donde le indicamos a la propiedad --env-file la ruta relativa
#del fichero de configuracion 
#hello_world_env % docker run -p 3000:90 --env-file ./.env environment