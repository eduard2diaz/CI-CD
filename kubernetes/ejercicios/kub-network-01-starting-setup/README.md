# Pasos de despliegue

# Users-api

Primeramente vamos al proyecto `users-api`, donde el primer pasos era construir la imagen

> docker build -t eduard2diaz/kub-demo-users .

la cual luego subimos a dockerhub

> docker push eduard2diaz/kub-demo-users

## Deployment del proyecto users-api
Luego en la raiz del proyecto creamos una carpeta nombrada `kubernetes`, donde crearemos un fichero nombrado `users-deployment.yaml` con la siguiente estructura

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: users-deployment
spec:
  replicas: 1
  selector: 
    matchLabels:
      app: users
  template:
    metadata:
      labels:
        app: users
    spec:
      containers:
        - name: users
          image: eduard2diaz/kub-demo-users
```

para luego aplicar dicho deployment

> kubectl apply -f=users-deployment.yaml

Y si listamos nuestros deployments obtendremos:

> kubectl get deployments                   

    NAME               READY   UP-TO-DATE   AVAILABLE   AGE
    users-deployment   1/1     1            1           34s

lo mismo si listamos los pods

> kubectl get pods       

    NAME                                READY   STATUS    RESTARTS   AGE
    users-deployment-567d46f6cf-hbnjb   1/1     Running   0          77s

Luego debemos crear el servicio, para ello definiremos dentro de la carpeta `kubernetes` un fichero llamado `users-service.yaml` con el siguiente contenido:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: users-service
spec:
  selector:
    app: users
  type: LoadBalancer
  ports:
    - protocol: TCP
      port: 8080
      targetPort: 8080
```

luego procedemos a aplicar el servicio

> kubectl apply -f=users-service.yaml

, si listamos nuestrso servicios obtenemos que 

> kubectl get services               

    NAME            TYPE           CLUSTER-IP       EXTERNAL-IP   PORT(S)          AGE
    kubernetes      ClusterIP      10.96.0.1        <none>        443/TCP          3d
    users-service   LoadBalancer   10.108.181.125   localhost     8080:32132/TCP   23s

Ya nuestra aplicacion `users-api` esta lista, ahora procederemos a integrarla con nuestra aplicacion `auth-api`

##  Deployment del proyecto auth-api

primeramente vamos al proyecto `auth-api` y creamos la imagen

> docker build -t eduard2diaz/kub-demo-auth .

para luego subir esta a dockerhub

> docker push eduard2diaz/kub-demo-auth

Sin embargo, como quiero desplegar el proyecto `users-api` junto con el proyecto `auth-api`, en este caso no vamos a crear un nuevo deployment para `auth-api`, sino que en el 
deployment que tenemos para los usuarios, es decir, `users-deployment.yaml` vamos a crear un contenedor nuevo donde correra una instancia de `auth-api`. Por lo que el fichero
`users-deploymemt.yaml` quedaria asi:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: users-deployment
spec:
  replicas: 1
  selector: 
    matchLabels:
      app: users
  template:
    metadata:
      labels:
        app: users
    spec:
      containers:
        - name: users
          image: eduard2diaz/kub-demo-users:latest
        - name: auth
          image: eduard2diaz/kub-demo-auth:latest
```

Ojo, en este caso se puso la la etiqueta `latest` para presionar a kubernetes a buscar en dockerhub la ultima version de la imagen y no solo utilizar la que tenga en la cache.

>
    Cuando en kubernetes tenemos un pod con 2 o mas containers, solo en este caso, kubernetes permite que los contenedores se comuniquen utilizando como IP `localhost` y el puerto por el que esta corriendo cada aplicacion de cada contenedor. Por lo que pudiesemos modificar el fichero `users-deployment.yaml` de la siguiente forma:

```yaml
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: users-deployment
spec:
  replicas: 1
  selector: 
    matchLabels:
      app: users
  template:
    metadata:
      labels:
        app: users
    spec:
      containers:
        - name: users
          image: eduard2diaz/kub-demo-users:latest
          #definimos una variable de entorno para este contenedor que le indique como comunicarse con el contenedor de authenticacion
          env:
            - name: AUTH_ADDRESS
              #fijate que en el valor se puso localhost porque se trata de 2 contenedores de kubernetes corriendo en el mismo pod
              value: localhost
        - name: auth
          image: eduard2diaz/kub-demo-auth:latest
```

Una vez hecho lo anterior solo restaria aplicar los cambios:

> kubectl apply -f=users-deployment.yaml 

Si listamos los pods veremos que ambos contenedores estan corriendo

> kubectl get pods                            

    NAME                                READY   STATUS    RESTARTS   AGE
    users-deployment-85545c6dc5-9d7bl   2/2     Running   0          33s


### OJO
Fijate que el fichero `docker-compose.yaml` del proyecto `users-api` quedo de la forma. Es decir, a pesar de que los 2 contenedores estan dentro del mismo pod en el contexto
de kubernetes, si vamos a desplegarlos fuera de este contexto, debemos usar el nombre del contenedor, en este caso `auth`, en lugar de localhost.

```yaml
version: "3"
services:
  auth:
    build: ./auth-api
  users:
    build: ./users-api
    #Fijate que la variable de entorno que definimos aqui no esta en el Dockerfile, quiere decir, que puedo tener variables de entorno en uno u otro lugar sin problema
    environment:
      #fijate que el valor de la variable es auth, porque ese es el nombre del contenedor que se asigno en el fichero users-deployment en kubernetes
      AUTH_ADDRESS: auth
    ports: 
      - "8080:8080"
  tasks:
    build: ./tasks-api
    ports: 
      - "8000:8000"
    environment:
      TASKS_FOLDER: tasks
```

Ademas resulta curioso el primer comentario respecto a las variables de entorno, que pueden estar en `docker-compose.yaml` y no en el Dockerfile, sin problema ninguno, y esto lo podemos
verificar pues el fichero `Dockerfile` es el siguiente:

```yaml
FROM node:14-alpine

WORKDIR /app

COPY package.json .

RUN npm install

COPY . .

EXPOSE 8080

CMD [ "node", "users-app.js" ]
```
## Un deploymemt para cada cual
Ahora, que pasaria en casos de que querramos tener un deployment para cada una de nuestras aplicaciones. Entonces, por ejemplo en la carpeta `kubernetes`
tendriamos que crear un fichero `auth-deployment.yaml` y copiar del `user-deployment.yaml` la seccion referente al contenedor de `auth`, quedando el
fichero `auth-deployment.yaml` de la sig forma:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-deployment
spec:
  replicas: 1
  selector: 
    matchLabels:
      app: auth
  template:
    metadata:
      labels:
        app: auth
    spec:
      containers:
        - name: auth
          image: eduard2diaz/kub-demo-auth:latest
```

Mientras que el fichero `users-deployment.yaml` quedaria como

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: users-deployment
spec:
  replicas: 1
  selector: 
    matchLabels:
      app: users
  template:
    metadata:
      labels:
        app: users
    spec:
      containers:
        - name: users
          image: eduard2diaz/kub-demo-users:latest
          env:
            - name: AUTH_ADDRESS
              value: localhost
```

De esta forma los contenedores de `auth` y `users` ya no residen mas en el mismo pod. Ahora, tendriamos que ademas crear un servicio para exponer
la aplicacion de `auth`, para lo que utilizaremos el servicio `auth-service.yaml`

```yaml
apiVersion: v1
kind: Service
metadata:
  name: auth-service
spec:
  selector:
    app: auth
  type: LoadBalancer
  ports:
    - protocol: TCP
      port: 80
      targetPort: 80
```

Fijate que en `users-app.js` vas a tener el enunciado 

```js
const response = await axios.get(
    `http://${process.env.AUTH_SERVICE_SERVICE_HOST}/token/` + hashedPassword + '/' + password
  );
```

En este caso `AUTH_SERVICE_SERVICE_HOST` se refiere a la direccion IP (`SERVICE_HOST`) del servicio `AUTH_SERVICE`. Esto se debe a que kubernetes
nos da a traves de variables de entorno la configuracion de cada uno de los servicios. Vale destacar, que si queremos que esto tambien funcione con docker,
no solo con kubernetes tendriamos que definir `AUTH_SERVICE_SERVICE_HOST` como variable de entorno de l docker compose en el proyecto `users-api`

```yaml
version: "3"
services:
  auth:
    build: ./auth-api
  users:
    build: ./users-api
    environment:
      AUTH_ADDRESS: auth
      AUTH_SERVICE_SERVICE_HOST: auth
    ports: 
      - "8080:8080"
  tasks:
    build: ./tasks-api
    ports: 
      - "8000:8000"
    environment:
      TASKS_FOLDER: tasks
```

## Service discovery

Lo anterior esta bien pero kubernetes cuenta con service discovery, por lo que al igual que como hacemos con docker que para indicarle a donde debe conectarse
basta con indicarle el nombre del contenedor. En kubernetes basta con indicarle el nombre del servicio acompanhado con el signo de puntuacion y el namespace, es decir `nombre_servicio.namespace`, por lo que el `users-deployment.yaml` pudiera ser 

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: users-deployment
spec:
  replicas: 1
  selector: 
    matchLabels:
      app: users
  template:
    metadata:
      labels:
        app: users
    spec:
      containers:
        - name: users
          image: eduard2diaz/kub-demo-users:latest
          env:
            - name: AUTH_ADDRESS
              #value: "10.99.104.252" #direccion ip del servicio
              value: "auth-service.default"
```

Para saber los namespaces que tenemos basta con

> kubectl get namespaces

  NAME              STATUS   AGE
  default           Active   5d1h
  kube-node-lease   Active   5d1h
  kube-public       Active   5d1h
  kube-system       Active   5d1h

**Por defecto todas nuestras aplicaciones estaran en el namespace default a menos que indiquemos otra cosa.**

Para ver este ejemplo en especifico busque el ejemplo **kub-network-04-automatic-domain-names**.