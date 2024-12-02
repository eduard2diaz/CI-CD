# Pasos para el despliegue

## Gestion de la imagen
Creamos la imagen
> docker build -t eduard2diaz/kub-data-demo:2 .

La subimos a DockerHub
> docker push eduard2diaz/kub-data-demo:2

## Definicion de la arquitectura
Definimos el fichero de servicio en un fichero `service.yaml`

```yaml
apiVersion: v1
kind: Service
metadata:
  name: story-service
spec:
  selector: 
    app: story
  type: NodePort
  ports:
    - protocol: "TCP"
      port: 80
      targetPort: 3000
```


Definimos el fichero de deployment en un fichero `deployment.yaml`


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
          hostPath:
            #ruta dentro de la maquina host donde los datos seran guardados (NO ES EN EL CONTENEDOR, ES EN LA MAQUINA HOST).
            #Esto viene siendo comoun binging de docker
            path: /data
            #indica como este volumen debe ser manejado. Por ejemplo DirectoryOrCreate se encarga de crear la carpeta si no existe
            type: DirectoryOrCreate
```

## Despliegue
Aplicamos el servicio y el deployment
> kubectl apply -f=service.yaml -f=deployment.yaml

Verificamos que todo funciona bien
> kubectl get deployments                         

    NAME               READY   UP-TO-DATE   AVAILABLE   AGE
    story-deployment   1/1     1            1           3s

>kubectl get services   

    NAME            TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)        AGE
    kubernetes      ClusterIP   10.96.0.1       <none>        443/TCP        45h
    story-service   NodePort    10.111.177.15   <none>        80:30656/TCP   10s

## Nota
A grandes rasgos `hostPath` lo que hace es montar un fichero o directorio que esta en el sistema de archivo del nodo huesped en el pod. Sin embargo hostPath falla cuando trabajamos con 
multiples replicas que estan en multiples nodos, ya que la informacion a la que tiene acceso una determinada replica de un nodo no es accesible desde otra replica que se encuentra en otro nodo y estos 2 volumenes no se estan sincronizando. En este sentido es de utilidad trabajar con el tipo de volumen `Container Storage Interface` (CSI).