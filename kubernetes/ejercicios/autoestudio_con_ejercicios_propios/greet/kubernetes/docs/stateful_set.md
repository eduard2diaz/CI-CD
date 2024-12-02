# StatefulSet

Un **StatefulSet** es un objeto de Kubernetes utilizado para gestionar aplicaciones con estado. A diferencia de los **Deployments**, los StatefulSets garantizan el orden y la unicidad de los Pods, lo que los hace ideales para aplicaciones como bases de datos o servicios que requieren identidades de red estables.

**Caracteristicas:**
* Nombres estables para los Pods: Cada Pod obtiene un nombre único y predecible (por ejemplo, **app-0**, **app-1**).
* Almacenamiento persistente: Cada Pod puede tener su propio volumen persistente.
* Despliegue y escalado ordenados: Los Pods se crean y terminan en un orden específico.
* Integración con servicios Headless: Proporciona nombres DNS únicos para cada Pod.

> ReplicaSets create multiple pod replicas from a single pod template. These replicas don’t differ from each other, apart from their name and IP address. If the pod template includes a volume, which refers to a specific PersistentVolumeClaim, all replicas of the ReplicaSet will use the exact same PersistentVolumeClaim and therefore the same PersistentVolume bound by the claim.
 
> Because the reference to the claim is in the pod template, which is used to stamp out multiple pod replicas, you can’t make each replica use its own separate PersistentVolumeClaim. You can’t use a ReplicaSet to run a distributed data store, where each instance needs its own separate storage—at least not by using a single ReplicaSet. To be honest, none of the API objects you’ve seen so far make running such a data store possible. You need something else.

```yaml
#statefulset.yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  #nombre del statefulset
  name: greet
spec:
  #Nombre del servicio a utilizar
  serviceName: "greet"
  replicas: 3
  selector:
    matchLabels:
      app: greet
  template:
    metadata:
      labels:
        app: greet
    spec:
      containers:
        - name: greet-statefulset
          image: eduard2diaz/greet:storage
          ports:
            - containerPort: 8080
              #definimos un nombre simbolico para el puerto
              name: http
```

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

Luego, basta con aplicarlo

> kubectl apply -f=service.yaml -f=statefulset.yaml

y obtendremos

> kubectl get pods

    NAME                                READY   STATUS             RESTARTS   AGE
    greet-0                             1/1     Running            0          29m
    greet-1                             1/1     Running            0          29m
    greet-2                             1/1     Running            0          29m

Finalmente, si listamos los statefulsets y los servicios obtendremos

> kubectl get statefulsets

    NAME                READY   AGE
    greet               3/3     4m43s

> kubectl get services

    NAME            TYPE           CLUSTER-IP       EXTERNAL-IP   PORT(S)          AGE
    greet           NodePort       10.101.15.250    <none>        80:31298/TCP     38s


## Comparacion entre un daemonset y un statefulset

| **Aspecto**                 | **DaemonSet**                                      | **StatefulSet**                                   |
|-----------------------------|---------------------------------------------------|-------------------------------------------------|
| **Propósito**               | Asegurar que haya **exactamente un Pod** en cada nodo del clúster (o en un subconjunto de nodos). | Administrar Pods con estado o identidad única, como bases de datos o aplicaciones que requieren persistencia. |
| **Cantidad de Pods por nodo**| Un Pod por nodo.                                  | Número específico de réplicas, independientemente de los nodos. |
| **Identidad de los Pods**   | Todos los Pods son **idénticos** y no tienen identidad única. | Cada Pod tiene una identidad única (por ejemplo, `app-0`, `app-1`, `app-2`). |
| **Persistencia**            | Generalmente no necesita almacenamiento persistente. | Puede usar **PersistentVolumeClaims** (PVCs) para datos persistentes. |
| **Orden de creación y eliminación** | No garantiza un orden al crear o eliminar Pods. | Los Pods se crean y eliminan en un **orden predefinido**. |
| **Casos de uso**            | - Recolección de logs (Fluentd, Logstash).<br>- Monitoreo de nodos (Node Exporter).<br>- Configuración de red (Calico, Weave). | - Bases de datos (MySQL, Cassandra).<br>- Sistemas distribuidos.<br>- Aplicaciones con replicación y dependencia entre Pods. |
| **Servicio asociado**       | Normalmente utiliza un **Headless Service** (`clusterIP: None`) para direccionar directamente a los Pods. | Puede usar un **Headless Service** o un servicio estándar para administrar Pods con nombres DNS únicos. |
| **Automatización en nuevos nodos** | Crea automáticamente un Pod en cada nuevo nodo del clúster. | No crea Pods automáticamente en nuevos nodos; depende de la configuración del controlador y la disponibilidad de recursos. |


Cada vez que creamos un deployment, se crea en segundo plano un replicaset. Los replicaset se utilizan para el escalado de las aplicaciones

Para listar los replicaset seria
> kubectl get replicaset

> kubectl get rs

# Diferencias entre `StatefulSet` y `Deployment`

Un **`StatefulSet`** está diseñado para aplicaciones con **estado**, mientras que un **`Deployment`** es adecuado para aplicaciones **sin estado**. Las principales diferencias se centran en la **identidad de los pods**, el **almacenamiento persistente**, el **orden de despliegue** y la **gestión dinámica**.

# ¿Qué puede hacer un `StatefulSet` que no puede un `Deployment`?

## 1. **Identidad estable y persistente para los pods**
- **`StatefulSet`**: Cada pod tiene un **nombre único y estable** que se mantiene a lo largo del ciclo de vida del pod. Esto es útil para aplicaciones que requieren una identidad persistente, como bases de datos distribuidas.
    - Ejemplo: Los pods en un `StatefulSet` pueden tener nombres como `myapp-0`, `myapp-1`, etc.
- **`Deployment`**: Los pods son asignados **nombres aleatorios** y no tienen una identidad persistente. Cuando un pod se reemplaza, su nombre cambia.

## 2. **Almacenamiento persistente y exclusivo por pod**
- **`StatefulSet`**: Cada pod tiene acceso a su propio volumen persistente (a través de un `PersistentVolumeClaim`), lo que garantiza que los datos se mantengan incluso si el pod se reinicia o se reemplaza.
    - Ejemplo: En un `StatefulSet` con 3 réplicas, cada pod tendría su propio volumen persistente (`myapp-0-data`, `myapp-1-data`, etc.).
- **`Deployment`**: Los pods no tienen volúmenes persistentes individuales. Si se usa almacenamiento persistente, los volúmenes son compartidos y no están vinculados de manera exclusiva a un pod específico.

## 3. **Despliegue ordenado (orden de inicio y eliminación)**
- **`StatefulSet`**: Los pods se **despliegan y eliminan en un orden secuencial**. Esto es útil cuando el orden de inicio o eliminación es crucial para la aplicación.
    - Ejemplo: En una base de datos distribuida, el pod `myapp-0` debe ser creado primero y `myapp-1` después.
- **`Deployment`**: Los pods son creados y eliminados **de manera paralela**, lo que puede no ser adecuado para aplicaciones que dependen del orden.

## 4. **Escalado controlado**
- **`StatefulSet`**: El escalado de pods se realiza de manera **ordenada**, uno por uno, en un orden específico.
- **`Deployment`**: El escalado de pods se hace **de manera paralela**, creando o eliminando múltiples pods al mismo tiempo.

## 5. **Actualizaciones y Rollbacks ordenados**
- **`StatefulSet`**: Las actualizaciones de pods se realizan de manera **secuencial**, lo que garantiza que los pods se actualicen en el orden correcto, manteniendo la consistencia.
- **`Deployment`**: Aunque las actualizaciones son progresivas, los pods se actualizan **de manera paralela** sin un orden específico, lo que puede afectar a aplicaciones que requieren consistencia.

# Resumen
- **`StatefulSet`** es ideal para aplicaciones que necesitan:
    - **Identidad persistente** de los pods.
    - **Almacenamiento exclusivo por pod**.
    - **Despliegue y eliminación ordenados**.
    - **Escalado y actualizaciones controladas**.

- **`Deployment`** es más adecuado para aplicaciones **sin estado** que no requieren estas características.

---