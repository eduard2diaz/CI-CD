# Volumes
Si utilizamos los ficheros dentro de la carpeta `volume/basic` vemos que hay un controlador `StorageController`
que permite la creacion, visualizacion y eliminacion de ficheros en el servidor. Sin embargo, lo que sucede es
que cuando se elimina el pod, los ficheros se pierden. Por eso, en esta seccion exploraremos el manejo de volumenes
en kubernetes.

## emptyDir
**The simplest volume type is the emptyDir volume**, so let’s look at it in the first example of how to define
a volume in a pod. As the name suggests, **the volume starts out as an empty directory**. The app running inside
the pod can then write any files it needs to it. Because the volume’s lifetime is tied to that of the pod,
the volume’s contents are lost when the pod is deleted.

An emptyDir volume is especially useful for sharing files between containers running in the same pod.
But it can also be used by a single container for when a container needs to write data to disk temporarily,
such as when performing a sort operation on a large dataset, which can’t fit into the available memory.

```yaml
#pod.yaml
apiVersion: v1
kind: Pod
metadata:
  name: storage-pod
  labels:
    app: storage-pod
spec:
  containers:
    - name: storage-container
      image: eduard2diaz/greet:storage
      ports:
        - containerPort: 8080
      env:
        - name: FILE_SAVE_PATH
          value: "/mnt/data" # Ruta donde se montará el volumen
      volumeMounts:
        #Especifica dónde estará disponible el volumen dentro del contenedor.
        - mountPath: "/mnt/data" # Punto de montaje dentro del contenedor
          name: storage-volume
  volumes:
    - name: storage-volume
      #Proporciona almacenamiento temporal que existe mientras el pod está activo.
      emptyDir: {}
```

```yaml
#service.yaml
apiVersion: v1
kind: Service
metadata:
  name: storage-service
spec:
  selector:
    app: storage-pod
  ports:
    - protocol: 'TCP'
      port: 80
      targetPort: 8080
  type: NodePort
```
Luego solo basta aplicar los ficheros
> kubectl apply -f pod.yaml -f service.yaml

> kubectl get services

        NAME              TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)        AGE
        kubernetes        ClusterIP   10.96.0.1        <none>        443/TCP        26h
        storage-service   NodePort    10.107.131.127   <none>        80:30887/TCP   9s

En Kubernetes, los volúmenes no son recursos independientes como los pods o servicios. En su lugar, están definidos dentro del manifiesto del pod y no pueden ser listados directamente como objetos.

Listamos los pods
> kubectl get pods                  

    NAME                       READY   STATUS      RESTARTS   AGE
    storage-pod                1/1     Running     0          9m35s

Obtenemos la informacion del pod, para dentro de la misma obtener la informacion asociada al volumen
> kubectl get pod storage-pod -o yaml

```yaml
apiVersion: v1
kind: Pod
metadata:
annotations:
kubectl.kubernetes.io/last-applied-configuration: |
{"apiVersion":"v1","kind":"Pod","metadata":{"annotations":{},"labels":{"app":"storage-pod"},"name":"storage-pod","namespace":"default"},"spec":{"containers":[{"env":[{"name":"FILE_SAVE_PATH","value":"/mnt/data"}],"image":"eduard2diaz/greet:storage","name":"storage-container","ports":[{"containerPort":8080}],"volumeMounts":[{"mountPath":"/mnt/data","name":"storage-volume"}]}],"volumes":[{"emptyDir":{},"name":"storage-volume"}]}}
creationTimestamp: "2024-11-30T19:54:45Z"
labels:
app: storage-pod
name: storage-pod
namespace: default
resourceVersion: "244991"
uid: 66ce016d-f971-44c1-97fe-226107684f97
spec:
containers:
- env:
    - name: FILE_SAVE_PATH
      value: /mnt/data
      image: eduard2diaz/greet:storage
      imagePullPolicy: IfNotPresent
      name: storage-container
      ports:
    - containerPort: 8080
      protocol: TCP
      resources: {}
      terminationMessagePath: /dev/termination-log
      terminationMessagePolicy: File
      volumeMounts:
    - mountPath: /mnt/data
      name: storage-volume
    - mountPath: /var/run/secrets/kubernetes.io/serviceaccount
      name: kube-api-access-nfrqd
      readOnly: true
      dnsPolicy: ClusterFirst
      enableServiceLinks: true
      nodeName: docker-desktop
      preemptionPolicy: PreemptLowerPriority
      priority: 0
      restartPolicy: Always
      schedulerName: default-scheduler
      securityContext: {}
      serviceAccount: default
      serviceAccountName: default
      terminationGracePeriodSeconds: 30
      tolerations:
- effect: NoExecute
  key: node.kubernetes.io/not-ready
  operator: Exists
  tolerationSeconds: 300
- effect: NoExecute
  key: node.kubernetes.io/unreachable
  operator: Exists
  tolerationSeconds: 300
  volumes:
- emptyDir: {}
  name: storage-volume
- name: kube-api-access-nfrqd
  projected:
  defaultMode: 420
  sources:
    - serviceAccountToken:
      expirationSeconds: 3607
      path: token
    - configMap:
      items:
        - key: ca.crt
          path: ca.crt
          name: kube-root-ca.crt
    - downwardAPI:
      items:
        - fieldRef:
          apiVersion: v1
          fieldPath: metadata.namespace
          path: namespace
          status:
          conditions:
- lastProbeTime: null
  lastTransitionTime: "2024-11-30T19:54:46Z"
  status: "True"
  type: PodReadyToStartContainers
- lastProbeTime: null
  lastTransitionTime: "2024-11-30T19:54:45Z"
  status: "True"
  type: Initialized
- lastProbeTime: null
  lastTransitionTime: "2024-11-30T19:54:46Z"
  status: "True"
  type: Ready
- lastProbeTime: null
  lastTransitionTime: "2024-11-30T19:54:46Z"
  status: "True"
  type: ContainersReady
- lastProbeTime: null
  lastTransitionTime: "2024-11-30T19:54:45Z"
  status: "True"
  type: PodScheduled
  containerStatuses:
- containerID: docker://6c3a3c8ca7b74f465f67004027fbf66c5ec261ed8169ad25b6eb801f5fc818b4
  image: eduard2diaz/greet:storage
  imageID: docker-pullable://eduard2diaz/greet@sha256:495cc8fce3a80b6788ed2f2ee29587f73ac7d04bbfe5e491c7093fc960a1a911
  lastState: {}
  name: storage-container
  ready: true
  restartCount: 0
  started: true
  state:
  running:
  startedAt: "2024-11-30T19:54:45Z"
  hostIP: 192.168.65.3
  hostIPs:
- ip: 192.168.65.3
  phase: Running
  podIP: 10.1.1.175
  podIPs:
- ip: 10.1.1.175
  qosClass: BestEffort
  startTime: "2024-11-30T19:54:45Z"
```

### Comportamiento del volumen emptyDir
* Duración del ciclo de vida: Un volumen emptyDir está vinculado al ciclo de vida del pod. Esto significa que:

  * Cuando el pod se elimina, el volumen también se elimina.
  * Los datos almacenados en un volumen emptyDir no persisten más allá de la vida del pod.
* Uso común: Se usa para almacenamiento temporal dentro del pod, como cachés, datos intermedios, o para compartir datos entre contenedores dentro del mismo pod.

## hostPath
A `hostPath` volume points to a specific file or directory on the node’s filesystem. Pods running on the same node and using the same path in their hostPath volume see the same files.

`hostPath` volumes are the first type of persistent storage we’re introducing, because both the `gitRepo` and `emptyDir` volumes contents get deleted when a pod is torn down, whereas a hostPath volume’s contents don’t. If a pod is deleted and the next pod uses a hostPath volume pointing to the same path on the host, the new pod will see whatever was left behind by the previous pod, but only if it’s scheduled to the same node as the first pod.

En este fitpo de almacenamiento, si bien el servicio no cambia el pod si lo hace, de forma tal que el pod quedaria:

```yaml
#pod.yaml
apiVersion: v1
kind: Pod
metadata:
  name: storage-pod
  labels:
    app: storage-pod
spec:
  containers:
    - name: storage-container
      image: eduard2diaz/greet:storage
      ports:
        - containerPort: 8080
      env:
        - name: FILE_SAVE_PATH
          value: "/mnt/data" # Ruta donde se montará el volumen
      volumeMounts:
        #Especifica dónde estará disponible el volumen dentro del contenedor.
        - mountPath: "/mnt/data" # Punto de montaje dentro del contenedor
          name: storage-volume
  volumes:
    - name: storage-volume
      #Proporciona almacenamiento temporal que existe mientras el pod está activo.
      hostPath:
        path: /data
        type: DirectoryOrCreate
```

Luego aplicamos los ficheros

> kubectl apply -f pod.yaml -f service.yaml

Y si listamos los servicios encontraremos el nuestro

> kubectl get services

    NAME              TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)        AGE
    kubernetes        ClusterIP   10.96.0.1      <none>        443/TCP        27h
    storage-service   NodePort    10.99.186.76   <none>        80:30510/TCP   3s

La diferencia con emptyDir es que luego de que subamos los ficheros, no importa si el contenedor se cae o se elimina,
si levantamos otro contenedor, u otro pod, en el mismo nodo del cluster, encontraremos los datos. Esto se debe a que, al usar `hostPath`
**la informacion se guarda en el nodo del cluster**, no en el pod, y mucho menos en el container.

## Volumen persistentes
To enable apps to request storage in a Kubernetes cluster without having to deal with infrastructure specifics, two new resources were introduced. They are PersistentVolumes and PersistentVolumeClaims.

Instead of the developer adding a technology-specific volume to their pod, it’s the cluster administrator who sets up the underlying storage and then registers it in Kubernetes by creating a PersistentVolume resource through the Kubernetes API server. When creating the PersistentVolume, the admin specifies its size and the access modes it supports.

When a cluster user needs to use persistent storage in one of their pods, they first create a PersistentVolumeClaim manifest, specifying the minimum size and the access mode they require. The user then submits the PersistentVolumeClaim manifest to the Kubernetes API server, and Kubernetes finds the appropriate PersistentVolume and binds the volume to the claim.

The PersistentVolumeClaim can then be used as one of the volumes inside a pod. Other users cannot use the same PersistentVolume until it has been released by deleting the bound PersistentVolumeClaim.

> En otras palabras, la ventaja que ofrece un volumen persistente frente a un hostPath u otra alternativa, es que el volumen persistente es otra forma de definir dicha estrategia de almacenamiento, hostPath, emptirDir, etc. Pero permite gestionar las mismas dinamicamente. 

```yaml
#service.yaml
apiVersion: v1
kind: Service
metadata:
  name: storage-service
spec:
  selector:
    app: storage-pod
  ports:
    - protocol: 'TCP'
      port: 80
      targetPort: 8080
  type: NodePort
```

```yaml
#pod.yaml
apiVersion: v1
kind: Pod
metadata:
  name: storage-pod
  labels:
    app: storage-pod
spec:
  containers:
    - name: storage-container
      image: eduard2diaz/greet:storage
      ports:
        - containerPort: 8080
      env:
        - name: FILE_SAVE_PATH
          value: "/mnt/data" # Ruta donde se montará el volumen
      #Monta el volumen asociado en el contenedor bajo la ruta /mnt/data
      volumeMounts:
        - mountPath: "/mnt/data"
          name: storage-volume
  volumes:
    #Asocia el pod con el PersistentVolumeClaim llamado host-pvc
    - name: storage-volume
      persistentVolumeClaim:
        #indica el volum claim a utilizar
        claimName: host-pvc
```

```yaml
#host-pvc.yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: host-pvc
spec:
  #Especifica que esta solicitud se enlazará directamente al volumen host-pv
  volumeName: host-pv
  accessModes:
    - ReadWriteOnce
  storageClassName: standard
  resources:
    requests:
      #Indica que el pod necesita 1 GiB de almacenamiento, que es exactamente lo que ofrece el host-pv.
      storage: 1Gi
```

```yaml
#host-pv.yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: host-pv
spec:
  #Define un volumen que se encuentra en el nodo del clúster, con una capacidad de almacenamiento de 1 GiB.
  capacity:
    storage: 1Gi
  volumeMode: Filesystem
  storageClassName: standard
  accessModes:
    - ReadWriteOnce
  hostPath:
    #Especifica que el volumen está basado en un directorio local en el nodo anfitrión, en la ruta /data.
    path: /data
    #Si el directorio no existe, Kubernetes lo creará automáticamente
    type: DirectoryOrCreate
```

Aplicamos los ficheros
> kubectl apply -f pod.yaml -f service.yaml -f host-pv.yaml -f host-pvc.yaml

Listamos los volumenes persistentes

> kubectl get pv
    
    NAME      CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS   CLAIM              STORAGECLASS   VOLUMEATTRIBUTESCLASS   REASON   AGE
    host-pv   1Gi        RWO            Retain           Bound    default/host-pvc   standard       <unset>                          19d

Listamos los claims de los volumenes persistentes

> kubectl get pvc
    
    NAME       STATUS   VOLUME    CAPACITY   ACCESS MODES   STORAGECLASS   VOLUMEATTRIBUTESCLASS   AGE
    host-pvc   Bound    host-pv   1Gi        RWO            standard       <unset>                 19d

Listamos los servicios

> kubectl get services
    
    NAME              TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)        AGE
    kubernetes        ClusterIP   10.96.0.1       <none>        443/TCP        27h
    storage-service   NodePort    10.103.32.130   <none>        80:31816/TCP   5m59s

### Nota
Modos de Acceso (`accessModes`) en Kubernetes

En Kubernetes, **`accessModes`** define cómo un volumen puede ser accedido por los pods. Especifica el tipo de acceso que los pods tienen al volumen persistente.

### Modos de Acceso (Access Modes)

| **Modo**            | **Abreviación** | **Descripción**                                                                                      |
|---------------------|-----------------|------------------------------------------------------------------------------------------------------|
| **ReadWriteOnce**   | **`RWO`**       | El volumen puede ser montado en modo lectura/escritura por **un único pod a la vez**.               |
| **ReadOnlyMany**    | **`ROX`**       | El volumen puede ser montado en modo **solo lectura** por **múltiples pods** simultáneamente.       |
| **ReadWriteMany**   | **`RWX`**       | El volumen puede ser montado en modo lectura/escritura por **múltiples pods** simultáneamente.      |

### Explicación de Cada Modo

#### **1. ReadWriteOnce (RWO)**
- **Significado:**
    - El volumen puede ser accedido en **lectura/escritura** por un solo pod a la vez.
- **Uso común:**
    - Bases de datos, aplicaciones que requieren exclusividad en el acceso al almacenamiento.
- **Ejemplo:**
    - Un pod que utiliza un volumen para almacenar datos privados o sensibles.

#### **2. ReadOnlyMany (ROX)**
- **Significado:**
    - El volumen puede ser montado como **solo lectura** por varios pods al mismo tiempo.
- **Uso común:**
    - Compartir datos entre varios pods donde los datos no deben ser modificados, como configuraciones estáticas o bibliotecas compartidas.
- **Ejemplo:**
    - Una aplicación que monta un directorio con archivos de configuración que no necesitan ser modificados.

#### **3. ReadWriteMany (RWX)**
- **Significado:**
    - El volumen puede ser montado en **lectura/escritura** por múltiples pods simultáneamente.
- **Uso común:**
    - Aplicaciones que necesitan colaborar escribiendo o leyendo los mismos datos, como sistemas de almacenamiento compartido.
- **Ejemplo:**
    - Un sistema distribuido donde múltiples réplicas de un servicio escriben logs en el mismo volumen.

### Relación con Tipos de Volumen

No todos los volúmenes admiten todos los modos de acceso. Aquí hay una tabla de compatibilidad:

| **Tipo de Volumen**          | **Compatibilidad**             |
|-----------------------------|--------------------------------|
| **`hostPath`**              | `RWO`                         |
| **`emptyDir`**              | `RWO`                         |
| **`NFS`**                   | `RWO`, `ROX`, `RWX`           |
| **`AWS EBS`**               | `RWO`                         |
| **`Azure Disks`**           | `RWO`                         |
| **`Google Persistent Disk`**| `RWO`                         |
| **`CephFS`**                | `RWO`, `ROX`, `RWX`           |

### Resumen general de volumenes

#### Diferencia entre PersistentVolume y hostPath, emptyDir, etc.

Un **`PersistentVolume (PV)`** es una **abstracción** en Kubernetes que te permite gestionar el almacenamiento de manera **dinámica** y **agregada**, independientemente de los detalles de implementación subyacentes, como el tipo de almacenamiento o la infraestructura. **`hostPath`**, **`emptyDir`** y otros tipos de almacenamiento pueden ser utilizados como diferentes **tipos de volúmenes dentro de un PV** o en un pod, pero el **`PersistentVolume`** te proporciona una forma estandarizada y flexible para gestionar estos volúmenes.

##### ¿Por qué un `PersistentVolume` es más ventajoso que `hostPath`, `emptyDir`, etc.?

1. **Abstracción del almacenamiento**
- **`PersistentVolume` (PV)** te permite definir una capa de abstracción sobre el almacenamiento físico, ya sea local (`hostPath`), en red (como NFS), en la nube (como EBS, Google Persistent Disk), etc.
- Al usar un **`PV`**, Kubernetes maneja la **asignación y el ciclo de vida** del almacenamiento, lo que facilita la administración y la portabilidad.
- **`hostPath`**, **`emptyDir`**, y otros volúmenes son tipos específicos de almacenamiento que no ofrecen esa flexibilidad por sí solos.

2. **Portabilidad y escalabilidad**
- **`hostPath`** está **ligado a un nodo físico específico**. Si el pod se mueve a otro nodo, no podrá acceder al mismo volumen a menos que se reconfigure.
- En cambio, **un `PersistentVolume`** puede estar asociado a un almacenamiento en la nube, un sistema de archivos en red o incluso un disco de clúster, lo que **permite la portabilidad y la escalabilidad**.
- **`emptyDir`** es **temporal** y desaparece con el pod. Los **`PersistentVolumes`** permiten que los datos persistan incluso si los pods se reinician o se reprograman.

3. **Gestión dinámica**
- **`PersistentVolume` (PV)** y **`PersistentVolumeClaim` (PVC)** ofrecen una forma **dinámica de gestionar el almacenamiento**:
    - Los **PVs** pueden ser provisionados de manera estática (por un administrador del clúster) o dinámica (Kubernetes puede crear y asignar un volumen automáticamente a un PVC).
    - Los **PVCs** permiten a los pods solicitar almacenamiento sin preocuparse de los detalles del volumen subyacente.
- Mientras tanto, tipos como **`hostPath`** o **`emptyDir`** son más estáticos y **no gestionan dinámicamente la asignación de almacenamiento**.

4. **Flexibilidad en la infraestructura**
- **`PersistentVolume`** permite integrar múltiples tipos de almacenamiento (como **`hostPath`**, **`NFS`**, **`AWS EBS`**, etc.) a través de un único mecanismo de gestión.
- Esto significa que puedes cambiar o ajustar el tipo de almacenamiento sin afectar a los pods que consumen ese almacenamiento. **Con `hostPath` o `emptyDir`**, los cambios en el tipo de almacenamiento implican cambios manuales en los pods o en los recursos de Kubernetes.

##### En resumen:
- **`PersistentVolume`** es una forma más flexible y gestionada de definir y gestionar el almacenamiento, permitiendo utilizar diferentes tipos de almacenamiento (como **`hostPath`** o **`emptyDir`**) de manera más dinámica, escalable y portable.
- **`hostPath`**, **`emptyDir`**, y otros volúmenes son tipos específicos de almacenamiento, pero no ofrecen las mismas capacidades de gestión dinámica, portabilidad o escalabilidad que un **`PersistentVolume`**.

##### Ventaja clave:
**`PersistentVolume`** es una abstracción poderosa que permite gestionar el almacenamiento de manera más eficiente, dinámica y flexible, permitiendo elegir y cambiar el tipo de almacenamiento subyacente sin afectar las aplicaciones que lo utilizan.