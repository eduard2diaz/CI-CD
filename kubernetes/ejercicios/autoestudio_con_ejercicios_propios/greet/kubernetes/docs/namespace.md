## Namespace

We’ve seen how they organize pods and other objects into groups. Because each object can have multiple labels, those groups of objects can overlap. Plus, when working with the cluster (through kubectl for example), if you don’t explicitly specify a label selector, you’ll always see all objects.

For this and other reasons, Kubernetes also groups objects into namespaces. Kubernetes namespaces provide a scope for objects names. Instead of hav- ing all your resources in one single namespace, you can split them into multiple name- spaces, which also allows you to use the same resource names multiple times (across different namespaces).

Using multiple namespaces allows you to split complex systems with numerous com- ponents into smaller distinct groups. They can also be used for separating resources in a multi-tenant environment, splitting up resources into production, development, and QA environments, or in any other way you may need. Resource names only need to be unique within a namespace.

### Truco I

Para listar los namespace seria:
> kubectl get ns

    NAME              STATUS   AGE
    default           Active   19d
    kube-node-lease   Active   19d
    kube-public       Active   19d
    kube-system       Active   19d

**Up to this point, you’ve operated only in the default namespace**. When listing resources with the kubectl get command, you’ve never specified the namespace explicitly, so **kubectl always defaulted to the default namespace, showing you only the objects in that namespace**.

### Truco II

Para listar los pods en otro namespace seria:

> kubectl get pods --namespace kube-system

    NAME                                     READY   STATUS    RESTARTS         AGE
    coredns-7db6d8ff4d-nkhjs                 1/1     Running   21 (3h27m ago)   19d
    coredns-7db6d8ff4d-swxkn                 1/1     Running   21 (3h27m ago)   19d
    etcd-docker-desktop                      1/1     Running   21 (3h27m ago)   19d
    kube-apiserver-docker-desktop            1/1     Running   21 (3h27m ago)   19d
    kube-controller-manager-docker-desktop   1/1     Running   21 (3h27m ago)   19d
    kube-proxy-mt59p                         1/1     Running   21 (3h27m ago)   19d
    kube-scheduler-docker-desktop            1/1     Running   21 (3h27m ago)   19d
    storage-provisioner                      1/1     Running   41 (3h26m ago)   19d
    vpnkit-controller                        1/1     Running   21 (3h27m ago)   19d

Asimismo puedo hacer para cualquier otro tipo de recurso.

### Truco III
Para crear un namespace bastaria por ejemplo como se hizo en la carpeta `namespace`,
crear un fichero, en este caso por ejemplo nombrado `custom-namespace.yaml` y definirle el siguiente contenido

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: custom-namespace
```

Luego bastaria con ejecutar el comando:

> kubectl create -f custom-namespace.yaml

Luego, verificamos su creacion
> kubectl get ns

    NAME               STATUS   AGE
    custom-namespace   Active   71s
    default            Active   19d
    kube-node-lease    Active   19d
    kube-public        Active   19d
    kube-system        Active   19d


Asimismo, si queremos no tenemos que crearlo usando una forma declarativa,
es decir, pudieramos utilizar el siguiente comando para definir un namespace
utilizando una forma imperativa.

> kubectl create namespace custom-namespace

### Truco IV

Para crear un objeto en un determinado namespace, por ejemplo el que creamos
anteriormente `custom-namespace`, seria:

> kubectl create -f ../pod/pod.yaml -n custom-namespace

Luego podemos verificar los pods en el namespace `default`
> kubectl get pods

    No resources found in default namespace.

Y podemos hacer lo mismo en el `custom-namespace`
> kubectl get pods -n custom-namespace

    NAME        READY   STATUS    RESTARTS   AGE
    greet-pod   1/1     Running   0          10s

### Truco V

Asimismo, para listar los pods,con sus labels, en un determiando namespace seria:

> kubectl get pods -n custom-namespace --show-labels

    NAME        READY   STATUS    RESTARTS   AGE     LABELS
    greet-pod   1/1     Running   0          3m19s   app=greet

### Truco VI
Igualmente, para eliminar un recurso que se encuentra en un namespace que no es el `default`

> kubectl delete pod greet-pod -n custom-namespace

Aunque tambien podiamos haber hecho

> kubectl delete -f ../pod/pod.yaml -n custom-namespace

### Truco VI
Igualmente, para eliminar un recurso que se encuentra en un namespace que no es el `default`
a partir de la llave de su label seria

> kubectl delete pod -l app -n custom-namespace

De igual forma si lo queremos eliminar pr su llave-valor seria:

> kubectl delete pod -l app=greet -n custom-namespace

### Truco VII
Para eliminar el namespace basta con:

> kubectl delete ns custom-namespace
