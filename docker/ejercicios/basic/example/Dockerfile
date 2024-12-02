FROM python

WORKDIR /app
COPY . .
CMD [ "python", "rng.py"]

#FIJATE QUE PARA APLICACIONES QUE CORREN DESDE LA TERMINAL Y QUE SE VA A INTERACTUAR CON ELLAS DESDE LA TERMINAL,
#NO HACE FALTA EXPONER EL PUERTO, NI EN EL Dockerfile, ni en el docker run

#Por primera vez seria: docker run -it nombre_imagen
#Si esta deteniedo seria: docker start -i nombre_contenedor     .
#EN ESTE CASO NO SE USA EL -t porque ya eso esta guardado en la cache de que debe ser usado, como resultado del primer comando