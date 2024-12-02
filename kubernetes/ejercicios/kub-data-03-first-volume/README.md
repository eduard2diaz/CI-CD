# Pasos para el despliegue

## Gestion de la imagen
Creamos la imagen
> docker build -t eduard2diaz/kub-data-demo:1 .

La subimos a DockerHub
> docker push eduard2diaz/kub-data-demo:1

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
    #El el protocolo las comillas son opcionales, bien podia haber sido TCP
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
  replicas: 1
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
          image: eduard2diaz/kub-data-demo:1
          #volumeMounts indica donde el volumen debe ser montado
          volumeMounts:
            - mountPath: /app/story
              name: story-volume
      volumes:
        #Definimos el nombre del volumen
        - name: story-volume
          #definimos el tipo de volumen
          #los {} indica que no hay una config en especifico, sino que use la de por defecto
          emptyDir: {}
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
Sin embargo, el volumen anterior esta atado al pod, si tenemos multiples pods, y creamos la informacion en el volumen de uno de ellos, dicha informacion no va 
a estar presente en el volumen del resto. Para ello una de las alternatovas, la mas sencilla es usar como estrategia de gestion de volumenes, en vez de `emptyDir`, usar `hostPath`. Esta estrategia (hostPath), lo que hace es montar un fichero o directorio que esta en el sistema de archivo del nodo huesped en el pod.