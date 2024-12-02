# Pasos para el despliegue

Los volumenes persistentes a diferencias de todos los volumenes anteriores que hemos visto son capaces de funcionar cuando tengo multiples pods en multiples hosts cosa que no puede hacerse con los  tipos `emptyDir`, ni `hosyPath`. Ademas los volumnes persistentes son recursos adicionales, como son los sertvicios y los deployments, por lo que a diferencia de cuando usamos `emptyDir`, no seran eliminados si el pod se elimina o escala. Sin embargo para que los nodos y por consiguiente los pods dentro de cada nodo, se puedan conectar a un volumen persistente es necesario definir un `Persisten Volume Claim` en cada nodo del cluster.

Por ello primeramente definiremos el volumen persistente en un fichero `host-pv.yaml`
```yaml
apiVersion: v1
#definimos el tipo de recurso
kind: PersistentVolume
metadata:
  #definimos el nombre del volumen persistente
  name: host-pv
spec:
  #definimos la capacidad del volumen
  capacity: 
    #capacidad de almacenamiento del volumen
    storage: 1Gi
  #Existen 2 tipos de modo de volumen, FileSystem y Block  
  volumeMode: Filesystem
  storageClassName: standard
  #Definimos los modos de acceso al volumen, son los posibles tipos de acceso que podran ser solicitados por los claims
  accessModes:
    #- ReadWriteOnce #indica que solo puede ser montado como volumen de lectura y escritura por un unico nodo
    #- ReadOnlyMany  #indica que es un volumen de solo lectura, pero que podra ser utilizado por multiples nodos
    #- ReadWriteMany  #indica que es un volumen de lectura y escritura, pero que podra ser utilizado por multiples nodos
    - ReadWriteOnce
  #definimos el tipo de volumen que vamos a utilizar, en este caso hostPath. En este caso estariamos creando un volumen persistente hostPath que,
  #es independiente de cualquier pod,
  #pero no es independiente de cualquier nodo.
  hostPath:
    path: /data
    type: DirectoryOrCreate
```

Definimos el claim que va a ser utilizado por cada nodo que quiera utilizar este volumen. Para ello utilizaremos el fichero `host-pvc.yaml`

```yaml
apiVersion: v1
#definimos el tipo de recurso que estamos creando
kind: PersistentVolumeClaim
metadata:
  #definimos el nombre del claim
  name: host-pvc
spec:
  #definimos el nombre del volumen al que esta relacionado
  volumeName: host-pv
  #definimos el/los tipo(s) de acceso que estamos solicitando sobre ese volumen
  accessModes:
    - ReadWriteOnce
  #definimos el storage class a utilizar, es una clase que se utiliza para permitir una configuracion de grafo fino, es decir a detalle, al administrador, acerca de como los volumenes
  #son creados y/o configurados
  storageClassName: standard
  resources:
    requests: 
      #es la cantidad de solicitud del volumen que estamos solicitando para el claim, tiene que ser menos o igual que la definida en la capacidad de almacenamiento del volumen
      storage: 1Gi
```
**NO ME PREGUNTES EN EL CLAIM POR QUE TUVE QUE UTILIZAR `standard` COMO storageClassName, PORQUE INTENTE PONERLE `hostpath` QUE ES EL QUE APARECE CUANDO SE LISTAN LOS STORAGE CLASS, PERO SI UTILIZO ESE NO FUNCIONA**

Ahora creamos el deployment y lo configuramos para que utilice el claim
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
          image: academind/kub-data-demo:1
          volumeMounts:
            - mountPath: /app/story
              name: story-volume
      volumes:
        - name: story-volume
          #En vez de definir el tipo de volumenm le indicamos que va a utilizar un claim
          persistentVolumeClaim:
            #especificamos el claim a utilizar
            claimName: host-pvc
```

Primeramente creamos el volumen persistente, para lo que ejecutaremos el siguiente comando:

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
    story-service   NodePort   10.109.7.102   localhost     80:30864/TCP   9s

# Nota

Para listar los storage class utilizamos

> kubectl get sc

    NAME                 PROVISIONER          RECLAIMPOLICY   VOLUMEBINDINGMODE   ALLOWVOLUMEEXPANSION   AGE
    hostpath (default)   docker.io/hostpath   Delete          Immediate           false                  47h

Una vez hecho todo lo anterior basta con visitar la URL http://localhost/story