# Service account
En Kubernetes, una **ServiceAccount** (Cuenta de Servicio) es una identidad que utilizan los Pods para **interactuar con la API del clúster de Kubernetes**. Las ServiceAccounts permiten a las aplicaciones dentro de un clúster autenticar solicitudes hacia la API de Kubernetes, y se pueden configurar con permisos específicos utilizando Roles y RoleBindings.

## Propósito de una ServiceAccount
1. Proveer identidad a los Pods: Cuando un Pod necesita interactuar con la API de Kubernetes (por ejemplo, listar pods, obtener secretos, o crear objetos), usa una ServiceAccount para autenticarse.
2. Controlar permisos: Se pueden asociar permisos específicos a la ServiceAccount mediante mecanismos como Roles o ClusterRoles, lo que permite un control granular sobre qué acciones pueden realizar los Pods.

## Características principales
* Asociación automática: Por defecto, Kubernetes asigna una ServiceAccount llamada default a cada Pod que no especifique otra cuenta de servicio.
* Token para autenticación: Kubernetes genera un token asociado a la ServiceAccount, que es montado en el Pod a través de un volumen secreto.
* Scoped permissions: Las acciones que puede realizar una ServiceAccount están definidas por los Roles o ClusterRoles asignados a través de **RoleBindings** o **ClusterRoleBindings**.

> A default ServiceAccount is automatically created for each namespace.

Para listar los service account seria
>kubectl get sa

    NAME      SECRETS   AGE
    default   0         22d

As you can see, the current namespace only contains the default ServiceAccount. Additional ServiceAccounts can be added when required. Each pod is associated with exactly one ServiceAccount, but multiple pods can use the same ServiceAccount.

You can assign a ServiceAccount to a pod by specifying the account’s name in the pod manifest. If you don’t assign it explicitly, the pod will use the default ServiceAccount in the namespace.

By assigning different ServiceAccounts to pods, you can control which resources each pod has access to.

> When a request bearing the authentication token is received by the API server, the server uses the token to authenticate the client sending the request and then determines whether or not the related ServiceAccount is allowed to perform the requested operation. The API server obtains this information from the system-wide authorization plugin configured by the cluster administrator. One of the available authorization plugins is the role-based access control (RBAC) plugin.

Para crear un `service account` desde la terminal bastaria con correr el siguiente comando
```bash
> kubectl create serviceaccount foo
 
> kubectl get sa                   
NAME      SECRETS   AGE
default   0         22d
foo       0         3s

> kubectl describe sa foo

Name:                foo
Namespace:           default
Labels:              <none>
Annotations:         <none>
Image pull secrets:  <none>
Mountable secrets:   <none>
Tokens:              <none>
Events:              <none>
```

Para crear un service account de forma declarativa seria
```yaml
#service-account.yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: my-service-account
  #namespace sobre el que crear el Service Account
  namespace: default
```

luego asignamos el service account a nuestro pod
```yaml
#pod.yaml
apiVersion: v1
kind: Pod
metadata:
  name: greet-pod
  labels:
    app: greet
spec:
  #Indicamos el service account a utilizar el pod
  serviceAccountName: my-service-account
  containers:
    - name: greet
      image: eduard2diaz/greet:storage
```

Luego solo restaria crear el servicio
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

Primeramente aplicamos nuestra service-account
> kubectl apply -f  service-account.yaml

Como resultado de esto al listado los service accounts debemos ser capaces de ver el recien creado
> kubectl get sa                                                   

    NAME                 SECRETS   AGE
    default              0         22d
    my-service-account   0         34s

Luego aplicamos nuestro pod y el servicio
> kubectl apply -f pod.yaml -f service.yaml

## Asignar permisos a la ServiceAccount (Opcional)
Si el Deployment necesita acceder a la API de Kubernetes o a ciertos recursos, asegúrate de asignar los permisos necesarios a la ServiceAccount usando un Role o ClusterRole con RoleBinding o ClusterRoleBinding.


### Gestión de permisos con Roles
1. Role y RoleBinding (nivel de namespace): Permiten definir permisos específicos para una ServiceAccount dentro de un namespace.
2. ClusterRole y ClusterRoleBinding (nivel de clúster): Permiten definir permisos globales en todo el clúster.

Al definir el rol, definimos la(s) operacion(es) que se pueden hacer sobre un determinado recurso; por ejemplo, los pods.
```yaml
#role.yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  namespace: default
  name: pod-reader
rules:
  - apiGroups: [""]
    resources: ["pods"]
    verbs: ["get", "watch", "list"]
```

Luego, tenemos que vincular el role con el service-acoount, para lo cual utilizaremos un role-binding

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: read-pods-binding
  namespace: default
subjects:
  - kind: ServiceAccount
    name: my-service-account
    namespace: default
roleRef:
  kind: Role
  name: pod-reader
  apiGroup: rbac.authorization.k8s.io
```

Entonces aplicamos los ficheros
> kubectl apply -f role.yaml -f role-binding.yaml

Podemos ver el role creado ejecutando el siguiente comando
> kubectl get role
    
    NAME         CREATED AT
    pod-reader   2024-12-01T23:13:30Z

Asimismo, para lista el role binding basta con
> kubectl get rolebinding

    NAME                ROLE              AGE
    read-pods-binding   Role/pod-reader   5m18s


Como resultado de lo anterior
* Un Pod que usa la ServiceAccount `my-service-account` podrá listar y ver `Pods` en el namespace default, pero no podrá realizar otras acciones ni interactuar con recursos en otros namespaces.
* Si se necesita acceso a otros recursos, se pueden añadir más reglas al Role o asociarlo con un ClusterRole.

Para verficar los permisos bastaria con obtener el nombre de nuestro pod

```bash
> kubectl get pods
    
    NAME               READY   STATUS      RESTARTS   AGE
    greet-pod          1/1     Running     0          10m
```

Y finalmente basta con hacer una llamada a la API
> kubectl exec -it greet-pod -- curl -k https://kubernetes.default.svc/api/v1/namespaces/default/pods
