## ReplicaSets

Similar al replication para definir un replicaset seria muy similar. Primeramente definimos
el servicio 

```yaml
#service.yaml
apiVersion: v1
kind: Service
metadata:
  name: greet
spec:
  selector: 
    app: greet
  ports:
    - protocol: 'TCP'
      port: 80
      targetPort: 8080
  type: NodePort
```

Como vemos el servicio es exactamente igual al que definimos en el replication controller.
Mientras que, para definir el replicaset utilizariamos un archivo, en este caso nombra `replicaset.yaml`
con el siguiente contenido.

```yaml
#replicaset.yaml
apiVersion: apps/v1
kind: ReplicaSet
metadata:
  #nombre del replicaset
  name: greet-rs
spec:
  replicas: 3
  selector:
    #selector del pod
    matchLabels:
      app: greet
  #plantilla del pod
  template:
    metadata:
      labels:
        app: greet
    spec:
      containers:
        - name: greet
          image: eduard2diaz/greet:storage
          ports:
            - containerPort: 8080
```

Una vez creado ambos ficheros solo bastaria aplicarlo utilizando el siguiente comando

```bash
kubectl apply -f replicaset.yaml -f service.yaml
```

Luego podemos verificar los cambios listando los replicasets que tenemos disponibles

```bash
replicaset % kubectl get rs
```

y debemos obtener como salida lo siguiente

    NAME       DESIRED   CURRENT   READY   AGE
    greet-rs   3         3         3       53s

Asimismo, para obtener una descripcion de **TODOS** nuestros replicaset basta con
ejecutar el siguiente comando:

```bash
replicaset % kubectl describe rs
```

de lo que obtendremos algo similar a lo siguiente

    Name:         greet-rs
    Namespace:    default
    Selector:     app=greet
    Labels:       <none>
    Annotations:  <none>
    Replicas:     3 current / 3 desired
    Pods Status:  3 Running / 0 Waiting / 0 Succeeded / 0 Failed
    Pod Template:
    Labels:  app=greet
    Containers:
    greet:
    Image:         eduard2diaz/greet:storage
    Port:          8080/TCP
    Host Port:     0/TCP
    Environment:   <none>
    Mounts:        <none>
    Volumes:         <none>
    Node-Selectors:  <none>
    Tolerations:     <none>
    Events:
    Type    Reason            Age   From                   Message
      ----    ------            ----  ----                   -------
    Normal  SuccessfulCreate  67s   replicaset-controller  Created pod: greet-rs-gtjb7
    Normal  SuccessfulCreate  67s   replicaset-controller  Created pod: greet-rs-8p588
    Normal  SuccessfulCreate  67s   replicaset-controller  Created pod: greet-rs-8bgvl

Igualmente, para eliminar el replicaset podriamos ejecutar el siguiente comando, el
cual ademas elimina los pods creados por dicho replicaset.

```bash
kubectl delete rs greet-rs
```

### NOTA
The main improvements of ReplicaSets over ReplicationControllers are their more expressive label selectors. You intentionally used the simpler matchLabels selector in the first ReplicaSet example to see that ReplicaSets are no different from Replication- Controllers. Now, you’ll rewrite the selector to use the more powerful matchExpressions property, as shown in the following listing.

```yaml
selector:
  matchExpressions:
    - key: app
      # This selector requires the pod to contain a label with the “app” key.
      operator: In
      # The label’s value must be “kubia”.
      values:
        - kubia
```

You can add additional expressions to the selector. As in the example, each expression must contain a key, an operator, and possibly (depending on the operator) a list of values. You’ll see four valid operators:
* In—Label’s value must match one of the specified values.
* NotIn—Label’s value must not match any of the specified values.
* Exists—Pod must include a label with the specified key (the value isn’t important). When using this operator, you shouldn’t specify the values field.
* DoesNotExist—Pod must not include a label with the specified key. The values
property must not be specified.

Both ReplicationControllers and ReplicaSets are used for running a specific number of pods deployed anywhere in the Kubernetes cluster. But certain cases exist when you want a pod to run on each and every node in the cluster (and each node needs to run exactly one instance of the pod.