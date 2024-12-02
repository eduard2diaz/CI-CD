#

Para definir las variables de entorno fuera de nuestro recurso de ConfigMap, por ejemplo el fichero `environment.yaml` de la sig forma:
```yaml
apiVersion: v1
#definimos el tipo de recurso de kubernete, que se usa para crear un mapa de configuraciones
kind: ConfigMap
metadata:
  #definimos su nombre
  name: data-store-env
data:
  #aqui definimos los elementos que queremos guardar en el configmap, de la forma llave: valor
  folder: 'story'
  # key: value..
```
Luego aplicamos el fichero 
> kubectl apply -f=environment.yaml

Y para saber si funciono listamos los mapas de configuracion que tengamos

> kubectl get configmap

  NAME               DATA   AGE
  data-store-env     1      29s
  kube-root-ca.crt   1      2d

Luego modificamos el fichero de deployment para que en vez de definir los valores de las llaves dentro de el mismo, los tome de una fuente externa. Por lo que el fichero `deployment.yaml`
quedaria de la siguiente forma:

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
          image: eduard2diaz/kub-data-demo:2
          env:
            # definimos el nombre de la variable de entorno
            - name: STORY_FOLDER
              #pero en vez de definir el valor, definimos de donde va a tomar los datos
              valueFrom: 
                configMapKeyRef:
                  #nombre del confimap a utilizar
                  name: data-store-env
                  #llave que tiene la llave que queremos en el config map, es decir, le indicamos la llave de la que debe tomar el valor
                  key: folder
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