# ReplicationControllers
A ReplicationController is a Kubernetes resource that ensures its pods are always kept running. 
If the pod disappears for any reason, such as in the event of a node disappearing from the cluster
or because the pod was evicted from the node, the ReplicationController notices the missing pod and
creates a replacement pod. Replication-Controllers, in general, are meant to create and manage
multiple copies (replicas) of a pod. That’s where ReplicationControllers got their name from.

The ReplicationController in the figure manages only a single pod, but Replication- Controllers,
in general, are meant to create and manage multiple copies (replicas) of a pod. That’s where
ReplicationControllers got their name from.

El servicio queda como siempre
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

y el replication-controller.yaml quedaria:

```yaml
#replication-controller.yaml
apiVersion: v1
kind: ReplicationController
metadata:
  #nombre del replication controller
  name: greet-rc
spec:
  replicas: 3
  selector:
    #selector del pod
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

Luego basta con aplicar el `servicio.yaml` y el `replication-controller.yaml`
que creamos

> kubectl apply -f replication-controller.yaml -f service.yaml

Si listamos nuestros servicios vemos que fue creado

> kubectl get services                                        

    NAME         TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)        AGE
    greet        NodePort    10.110.94.61   <none>        80:30259/TCP   16s
    kubernetes   ClusterIP   10.96.0.1      <none>        443/TCP        19d

Asimismo como 3 instancias del pod

> kubectl get pods    

    NAME             READY   STATUS    RESTARTS   AGE
    greet-rc-jf4fz   1/1     Running   0          22s
    greet-rc-l6vpt   1/1     Running   0          22s
    greet-rc-q7qcg   1/1     Running   0          22s

Si una de estas instancias sea cae o es eliminada como veremos a continuacion

> kubectl delete pod greet-rc-l6vpt

    pod "greet-rc-l6vpt" deleted

vemos que automaticamente es creada una nueva instancia, que suple la que perdimos
> kubectl get pods                 

    NAME             READY   STATUS    RESTARTS   AGE
    greet-rc-f7ngx   1/1     Running   0          3s
    greet-rc-jf4fz   1/1     Running   0          74s
    greet-rc-q7qcg   1/1     Running   0          74s

Ademas podemos ver el replicacion controller que creamos, si ejecutamos el comando

> kubectl get rc

    NAME       DESIRED   CURRENT   READY   AGE
    greet-rc   3         3         3       8m18s

## Informacion
Para obtener informacion de nuestro replication controller basta con ejecuta el comando

> kubectl describe rc greet-rc

```bash
Name:         greet-rc
Namespace:    default
Selector:     app=greet
Labels:       app=greet
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
Type    Reason            Age    From                    Message
  ----    ------            ----   ----                    -------
Normal  SuccessfulCreate  9m     replication-controller  Created pod: greet-rc-jf4fz
Normal  SuccessfulCreate  9m     replication-controller  Created pod: greet-rc-q7qcg
Normal  SuccessfulCreate  9m     replication-controller  Created pod: greet-rc-l6vpt
Normal  SuccessfulCreate  7m49s  replication-controller  Created pod: greet-rc-f7ngx
```

## Modificacion en caliente
Si queremos cambiar algo de la configuracion de nuestro replicationcontroller que esta corriendo
basta con ejecutar el comando

> kubectl edit rc greet-rc

el cual abrira la definicion del YAML del mismo en nuestro editor por defecto. 
Entonces, modificamos la propiedad y guardamos el cambio. 

Una vez modificamos el numero de replicas a 4, si ejecutamos el sig comando

> kubectl get pods

Vemos que tenemos un nuevo pod

    NAME             READY   STATUS    RESTARTS   AGE   LABELS
    greet-rc-9gbzs   1/1     Running   0          7s    app=greet
    greet-rc-f7ngx   1/1     Running   0          16m   app=greet
    greet-rc-jf4fz   1/1     Running   0          17m   app=greet
    greet-rc-q7qcg   1/1     Running   0          17m   app=greet

## Escalando
Como mismo podemos modificar cualquiera de las propiedades del replicacion controller, como vimos anteriormente.
Tambien podemos si queremos escalar el numero de replicas sin tener que editar la especificacion del mismo manualmente.
Para ello ejecutaremos el siguiente comando

> kubectl scale rc greet-rc --replicas=10

Ahora si listamos los pods obtenemos

> kubectl get pods

    NAME             READY   STATUS    RESTARTS   AGE     LABELS
    greet-rc-2scx5   1/1     Running   0          7s      app=greet
    greet-rc-496pb   1/1     Running   0          7s      app=greet
    greet-rc-6jpzf   1/1     Running   0          7s      app=greet
    greet-rc-9gbzs   1/1     Running   0          4m44s   app=greet
    greet-rc-bcfkn   1/1     Running   0          7s      app=greet
    greet-rc-f7ngx   1/1     Running   0          21m     app=greet
    greet-rc-gxgz7   1/1     Running   0          7s      app=greet
    greet-rc-jf4fz   1/1     Running   0          22m     app=greet
    greet-rc-kgksk   1/1     Running   0          7s      app=greet
    greet-rc-q7qcg   1/1     Running   0          22m     app=greet

Aunque una forma mas sencilla de comprobar si funciono es:

> kubectl get rc

    NAME       DESIRED   CURRENT   READY   AGE
    greet-rc   10        10        10      28m

## Eliminacion
Si queremos eliminar el replication controller justo con sus pods bastaria con

> kubectl delete rc greet-rc

Por otra parte, si no queremos eliminar los pods bastaria con 

> kubectl delete rc greet-rc --cascade=false

El ReplicationController y el ReplicaSet son ambos controladores en Kubernetes diseñados para garantizar que un número deseado de réplicas de un Pod estén en ejecución. Sin embargo, el ReplicaSet es una versión más moderna y flexible del ReplicationController. A continuación, te detallo las diferencias clave:

| **Aspecto**               | **ReplicationController**                               | **ReplicaSet**                                  |
|---------------------------|--------------------------------------------------------|------------------------------------------------|
| **Introducción**          | Es el controlador original para manejar réplicas de Pods. | Es una evolución del ReplicationController, más moderno y flexible. |
| **Selector**              | Solo admite selecciones exactas con etiquetas (`matchLabels`). | Admite **`matchLabels`** y **`matchExpressions`**, lo que permite reglas más complejas para seleccionar Pods. |
| **Flexibilidad**          | Limitado en cuanto a configuraciones avanzadas de selección. | Más potente al permitir condiciones como "OR" o "NOT" en los selectores. |
| **Compatibilidad**        | Es compatible con versiones anteriores de Kubernetes.   | Es parte del estándar actual (`apps/v1`) y se prefiere en implementaciones modernas. |
| **Uso recomendado**       | Está en desuso en la mayoría de los casos.              | Ideal para proyectos nuevos, especialmente si no necesitas las funcionalidades completas de un Deployment. |
| **Actualizaciones**       | No soporta actualizaciones declarativas.                | Usualmente no se usa directamente para actualizaciones (esto lo gestiona el Deployment). |
| **Casos de uso**          | Se recomienda evitarlo en nuevos proyectos.             | Ideal para proyectos nuevos o si necesitas solo réplicas sin las capacidades completas de un Deployment. |
