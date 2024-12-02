# DaemonSet

Un **DaemonSet** en Kubernetes es un tipo de controlador que garantiza que un tipo específico de Pod esté presente en todos (o un subconjunto de) los nodos del clúster. Es útil para implementar aplicaciones o servicios que necesitan ejecutarse en cada nodo, como herramientas de monitoreo, recolección de logs, o agentes de red.

To run a pod on all cluster nodes, you create a DaemonSet object, which is much like a ReplicationController or a ReplicaSet, except that pods created by a Daemon- Set already have a target node specified and skip the Kubernetes Scheduler. They aren’t scattered around the cluster randomly.

A DaemonSet makes sure it creates as many pods as there are nodes and deploys each one on its own node.
Whereas a ReplicaSet (or ReplicationController) makes sure that a desired num- ber of pod replicas exist in the cluster, a DaemonSet doesn’t have any notion of a desired replica count. It doesn’t need it because its job is to ensure that a pod match- ing its pod selector is running on each node.

>    If a node goes down, the DaemonSet doesn’t cause the pod to be created else- where. But when a new node is added to the cluster, the DaemonSet immediately deploys a new pod instance to it. It also does the same if someone inadvertently deletes one of the pods, leaving the node without the DaemonSet’s pod. Like a Replica- Set, a DaemonSet creates the pod from the pod template configured in it.

A DaemonSet deploys pods to all nodes in the cluster, unless you specify that the pods should only run on a subset of all the nodes. This is done by specifying the node- Selector property in the pod template, which is part of the DaemonSet definition (similar to the pod template in a ReplicaSet or ReplicationController).

**Características de un DaemonSet**
* Un Pod por nodo: Un DaemonSet asegura que haya exactamente un Pod ejecutándose en cada nodo calificado.
* Automático en nuevos nodos: Cuando se agrega un nuevo nodo al clúster, el DaemonSet automáticamente programa un Pod en ese nodo.
* Compatible con nodos específicos: Puedes limitar los nodos donde se ejecuta el DaemonSet usando **node selectors**, **taints** o **tolerations**.

Primeramente debemos crear el servicio; como notaras es exactamente el mismo que en los ejemplos anteriores

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
    - protocol: TCP
      port: 80
      targetPort: 8080
  type: NodePort
```

Luego debemos definir el daemonset

```yaml
#daemonset.yaml
apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: greet
spec:
  selector:
    matchLabels:
      name: greet
  template:
    metadata:
      labels:
        name: greet
    spec:
      containers:
        - name: greet
          image: eduard2diaz/greet:storage
          ports:
            - containerPort: 8080
```

Finalmente solo nos resta aplicar dichos ficheros

> kubectl apply -f daemonset.yaml -f service.yaml

Luego podemos listar los daemonsets, ya sea ejecutando el comando

> kubectl get daemonsets

como el comando

> kubectl get ds

    NAME    DESIRED   CURRENT   READY   UP-TO-DATE   AVAILABLE   NODE SELECTOR   AGE
    greet   1         1         1       1            1           <none>          104s

* NAME: The name of the DaemonSet, which is "my-daemonset" in this case.
* DESIRED: The desired number of DaemonSet pods. In your case, it's set to 7.
* CURRENT: The current number of DaemonSet pods running. It shows 6 pods are currently running.
* READY: The number of DaemonSet pods that are ready and available for use. All 6 running pods are ready.
* UP-TO-DATE: The number of DaemonSet pods that are up-to-date with the latest configuration.
* AVAILABLE: The number of DaemonSet pods that are available for use.
* NODE SELECTOR: Specifies which nodes in the cluster the DaemonSet should run on.
  In this case, it's set to **none**, meaning the DaemonSet is not restricted to
  specific nodes.
* AGE: The age of the DaemonSet, indicating how long it has been running.

Listamos los pods, como ves es un unico pod por cada nodo de nuestra arquitectura.
En este caso un unico pod

> kubectl get pods

    NAME                                READY   STATUS             RESTARTS   AGE
    greet-c4v52                         1/1     Running            0          14s

Puedes comprobar el numero de nodos utilizando el siguiente comando

> kubectl get nodes
    
    NAME             STATUS   ROLES           AGE   VERSION
    docker-desktop   Ready    control-plane   20d   v1.30.2

Por otra parte si listamos los servicios debemos obtener algo similar a lo siguiente 

> kubectl get services

    NAME            TYPE           CLUSTER-IP       EXTERNAL-IP   PORT(S)          AGE
    greet           NodePort       10.105.186.30    <none>        80:30566/TCP     44s

Ahora si queremos etiquetar un nodo bastaria con hacer lo siguiente

> kubectl label node docker-desktop disk=ssd
 
> kubectl get nodes --show-labels
    
    NAME             STATUS   ROLES           AGE   VERSION   LABELS
    docker-desktop   Ready    control-plane   20d   v1.30.2   beta.kubernetes.io/arch=arm64,beta.kubernetes.io/os=linux,disk=ssd,kubernetes.io/arch=arm64,kubernetes.io/hostname=docker-desktop,kubernetes.io/os=linux,node-role.kubernetes.io/control-plane=,node.kubernetes.io/exclude-from-external-load-balancers=

Asimismo, podemos modificar la etiqueta del nodo

> kubectl label node docker-desktop disk=hdd --overwrite

Y si comprobamos la etiqueta del nodo obtendremos

> kubectl get nodes --show-labels
    
    NAME             STATUS   ROLES           AGE   VERSION   LABELS
    docker-desktop   Ready    control-plane   20d   v1.30.2   beta.kubernetes.io/arch=arm64,beta.kubernetes.io/os=linux,disk=hdd,kubernetes.io/arch=arm64,kubernetes.io/hostname=docker-desktop,kubernetes.io/os=linux,node-role.kubernetes.io/control-plane=,node.kubernetes.io/exclude-from-external-load-balancers=

Up to now, we’ve only talked about pods than need to run continuously. You’ll have cases where you only want to run a task that terminates after completing its work. ReplicationControllers, ReplicaSets, and DaemonSets run continuous tasks that are never considered completed. Processes in such pods are restarted when they exit. But in a completable task, after its process terminates, it should not be restarted again.