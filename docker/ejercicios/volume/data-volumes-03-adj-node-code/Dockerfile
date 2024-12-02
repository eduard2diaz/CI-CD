FROM node:14

WORKDIR /app

COPY package.json .

RUN npm install

COPY . .

EXPOSE 80

#Si ponemos esto, seria un volumen anonimo. (Esto crea la carpeta dentro del contenedor si no existe)
#VOLUME [ "/app/feedback" ]
#Los volumenes anonimos son manejados por docker
#Los volumenes anonimos son eliminados cuando el contenedor es eliminado solo si ese contenedor fue levantado (start/run) usando --rm
#Lo que pasa es que si no usamos --rm para levantar (start/run) el contenedor, despues eliminamos el contenedor, y lo volvemos a levantar,
#un nuevo volumen va a ser creado, reemplazando al anterior,
#PERO ESO NO QUIERE DECIR QUE CUANDO ELIMINAMOS EL CONTENEDOR EL VOLUMEN SIEMPRE VA A ELIMINARSE AUNQUE NO USEMOS --rm

#En los Dockerfile solo se pueden crear volumenes anonimos, no volumenes nombrados
#Los volumenes anonimos no se listan si ejecitamos docker volume ls, y no aparecen en ningun listado de volumenes pues estan atados al contenedor

#Para crear volumenes nombrados tenemos que usar la opcion -v del comando docker run
#Ej: docker run -p 80:80 -v nombre_volumen_en_host:ruta_volumen_en_container imagen
#    docker run -p 80:80 -v feedback:/app/feedback volumen_image:latest

#El comando anterior va a crear un carpeta en el host que apunta a una ruta en el container, donde esta el volumen
#De esta forma creamos un volumen nombrado, y por consiguiente, cuando se elimine el contenedor el volumen no desaparecera
#Los docker nombrados si aparecen cuando llamamos a docker volume ls, en este caso veremos feedback
#Ademas los volumenes nombrados pueden ser utilizados por otros contenedores

#Para eliminar un volumen especifico basta con docker volume rm volume_name
#Para eliminar los volumenes que no estan siendo usados basta con docker volume prune,
#Como mismo hacemos para eliminar imagenes que no estan siendo usadas docker image prune -a


#Por otra parte los bind son volumen donde una ruta del host apunta a una ruta del contenedor, 
#tiene todas las ventajas de los volumenes nombrados, pero ademas permiten ver/modificar/eliminar
#desde una ruta de tu host el contenido presente en una ruta del contenedor
#Ej: docker run -p 80:80 -v ruta_host:ruta_contenedor image_name
#Ej: docker run -p 80:80 -v /Users/neyvislopez/Desktop/bind_docker:/app/feedback volumen_image:latest

#OJO: en mi mismo contenedor puedo utilizar tanto volumenes anotados como binds
#docker run -p 80:80 -v feedback:/app/feedback -v /Users/neyvislopez/Desktop/bind_docker:/app/feedback volumen_image:latest

#Tambien desde la terminal podemos definir volumenes anonimos, que es lo mismo que definir un volumen dentro del Dockerfile
#docker run -p 80:80 -v ruta_contenedor image_name
CMD [ "node", "server.js" ]