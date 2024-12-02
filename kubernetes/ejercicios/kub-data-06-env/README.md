#

Para definir las variables de entorno basta con definirlas en nuestro recurso de deployment, por ejemplo el fichero `deployment.yaml` de la sig forma:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: story-deployment
spec: 
  replicas: 2
  selector:
    matchLabels:
      app: story
  template:
    metadata:
      labels:
        app: story
    spec:
      containers:
        - name: story
          image: academind/kub-data-demo:2
          #definicion de las variables de entorno
          env:
            #nombre de la variable de entorno
            - name: STORY_FOLDER
              #valor de la variable de entorno
              value: 'story'
          volumeMounts:
            - mountPath: /app/story
              name: story-volume
      volumes:
        - name: story-volume
          persistentVolumeClaim:
            claimName: host-pvc
```

Luego basta con crear la imagen
Creamos la imagen
> docker build -t eduard2diaz/kub-data-demo:2 .

La subimos a DockerHub
> docker push eduard2diaz/kub-data-demo:2


Creamos el volumen persistente, para lo que ejecutaremos el siguiente comando:

> kubectl apply -f=host-pv.yaml

Para listar los volumenes persistentes seria:
> kubectl get pv

    NAME      CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM   STORAGECLASS   VOLUMEATTRIBUTESCLASS   REASON   AGE
    host-pv   1Gi        RWO            Retain           Available           standard       <unset> 

Luego creamos el claim que utilizaremos para acceder desde los nodos al volumen persistente, para ello ejecutaremos el siguiente comando:

> kubectl apply -f=host-pvc.yaml

Para listar los claim de los volumenes persistentes seria:
> kubectl get pvc

    NAME       STATUS    VOLUME    CAPACITY   ACCESS MODES   STORAGECLASS   VOLUMEATTRIBUTESCLASS   AGE
    host-pvc   Pending   host-pv   0                         hostpath       <unset>                 4s

Luego creamos el deployment
> kubectl apply -f=deployment.yaml

Listamos el deployment
> kubectl get deployments         

    NAME               READY   UP-TO-DATE   AVAILABLE   AGE
    story-deployment   2/2     2            0           7s

Y creamos el servicio
> kubectl apply -f=service.yaml

Luego listamos los servicios
> kubectl get services         

    NAME            TYPE           CLUSTER-IP     EXTERNAL-IP   PORT(S)        AGE
    kubernetes      ClusterIP      10.96.0.1      <none>        443/TCP        47h
    story-service   NodePort       10.109.7.102   localhost     80:31250/TCP   9s

Una vez hecho todo lo anterior basta con visitar la URL http://localhost/story.

**Sin embargo, que pasa si no queremos que nuestras variables de configuracion esten definidas en la especificacion del contenedor???** Vea el ejercicio kub-data-07-finished.

**Fijate que las variables de entorno que definimos en nuestro deployment no tienen por que estar en los ficheros Dockerfile ni docker-compose.yaml**