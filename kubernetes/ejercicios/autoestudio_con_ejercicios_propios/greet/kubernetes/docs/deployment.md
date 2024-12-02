# Deployments
Como vimos anteriormente los objectos `Pod` no pueden ser escalados directamente, para ello suelen ser utiles los `Deployments` y los `Replicaset`; por lo que en este ejemplo aprenderemos a trabajar con deployments. Para ello iremos a la carpeta `deployment`, dentro de la cual tenemos un fichero para el deployment y el servicio, respectivamente nombrados `deployment.yaml` y `service.yaml`

A Deployment is a higher-level resource meant for deploying applications and updating them declaratively, instead of doing it through a ReplicationController or a ReplicaSet, which are both considered lower-level concepts.
When you create a Deployment, a ReplicaSet resource is created underneath (eventually more of them).

```yaml
#deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  # definimos el nombre de la aplicacion
  name: greet
spec:
  #definimos que usaremos por el momento una unica replica
  replicas: 1
  #define cómo Kubernetes seleccionará los Pods gestionados por este Deployment. Esto se hace a través de etiquetas.
  selector:
    matchLabels:
      app: greet

  #define el "molde" que se usará para crear los Pods gestionados por este Deployment.
  template:
    metadata:
      labels:
        #asigna la etiqueta app: greet a los Pods
        app: greet
    spec:
      containers:
        - name: greet
          image: eduard2diaz/greet:storage
```

```yaml
#service.yaml
apiVersion: v1
kind: Service
metadata:
  name: greet
#define las configuraciones del Service, incluyendo los Pods que gestionará, los puertos que expondrá y cómo se accederá a él.
spec:
  #Indica qué Pods estarán gestionados por este Service.
  selector: 
    app: greet
  ports:
    #Especifica el protocolo utilizado para la comunicación.
    - protocol: 'TCP'
      #Es el puerto del Service, el cual estará disponible para los clientes que consuman este servicio.
      port: 80
      #Especifica el puerto del contenedor al que se redirigirán las solicitudes. Este puerto debe coincidir con el puerto que expone el contenedor definido #en el Deployment.
      targetPort: 8080
  type: NodePort
```

Aplicamos los 2 objetos

> kubectl apply -f=deployment.yaml -f=service.yaml

Luego comprobamos si los objetos anteriores fueron creados:

> kubectl get deployments 

    NAME               READY   UP-TO-DATE   AVAILABLE   AGE
    greet              1/1     1            1           5s

> kubectl get services

    NAME            TYPE           CLUSTER-IP       EXTERNAL-IP   PORT(S)          AGE
    greet           NodePort       10.110.24.155    <none>        80:32573/TCP     18s

Una vez realizado lo anterior, basta con llamar a la url http://localhost:32573

## NOTA
    Vale destacar que los `Deployment` crean automáticamente los Pods basándose en la especificación que se define en su
    manifiesto. El Deployment actúa como un controlador que administra el ciclo de vida de los Pods y asegura que siempre
    haya la cantidad deseada de réplicas en ejecución.