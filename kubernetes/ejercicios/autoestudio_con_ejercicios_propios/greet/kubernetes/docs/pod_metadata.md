# Metadata
Ademas de las variables de entorno, podemos acceder a los metadatos de nuestros pods. 

It allows you to pass the following information to your containers:
* The pod’s name
* The pod’s IP address
* The namespace the pod belongs to
* The name of the node the pod is running on
* The name of the service account the pod is running under
* The CPU and memory requests for each container
* The CPU and memory limits for each container
* The pod’s labels
* The pod’s annotations

Por ejemplo supongamos que tenemos la siguiente definicion de nuestro pod

```yaml
#pod.yaml
apiVersion: v1  # Define la versión de la API de Kubernetes utilizada para este recurso.
kind: Pod       # Especifica el tipo de recurso, en este caso, un pod.
metadata:       # Contiene información sobre el objeto, como su nombre y etiquetas.
  name: greet-pod  # Nombre único del pod.
  labels:          # Etiquetas para clasificar y organizar el pod.
    app: greet     # Etiqueta que identifica la aplicación asociada al pod.
spec:            # Define las especificaciones del pod y sus contenedores.
  containers:    # Lista de contenedores que se ejecutarán en este pod.
    - name: greet          # Nombre del contenedor dentro del pod.
      image: eduard2diaz/greet:storage  # Imagen de contenedor que se utilizará.
      env:                 # Lista de variables de entorno para el contenedor.
        - name: POD_NAME          # Variable que contiene el nombre del pod.
          valueFrom:
            fieldRef:
              fieldPath: metadata.name  # Obtiene el valor del nombre del pod desde sus metadatos.
        - name: POD_NAMESPACE     # Variable que contiene el namespace del pod.
          valueFrom:
            fieldRef:
              fieldPath: metadata.namespace  # Obtiene el namespace del pod.
        - name: POD_IP            # Variable que contiene la IP del pod.
          valueFrom:
            fieldRef:
              fieldPath: status.podIP  # Obtiene la IP del pod desde su estado.
        - name: NODE_NAME         # Variable que contiene el nombre del nodo donde se ejecuta el pod.
          valueFrom:
            fieldRef:
              fieldPath: spec.nodeName  # Obtiene el nombre del nodo desde la especificación del pod.
        - name: SERVICE_ACCOUNT   # Variable que contiene el nombre de la cuenta de servicio asociada al pod.
          valueFrom:
            fieldRef:
              fieldPath: spec.serviceAccountName  # Obtiene la cuenta de servicio utilizada por el pod.
        - name: CONTAINER_CPU_REQUEST_MILLICORES  # Variable que indica la CPU solicitada en milicores.
          valueFrom:
            resourceFieldRef:
              resource: requests.cpu  # Obtiene la solicitud de CPU del contenedor.
              divisor: 1m             # Convierte la CPU a milicores.
        - name: CONTAINER_MEMORY_LIMIT_KIBIBYTES  # Variable que indica el límite de memoria en KiB.
          valueFrom:
            resourceFieldRef:
              resource: limits.memory  # Obtiene el límite de memoria del contenedor.
              divisor: 1Ki             # Convierte la memoria a KiB.
```

Aplicamos nuestro pod 
> kubectl apply -f pod.yaml

> **OJO:** en este caso no es necesario definir el servicio, pues el objetivo de este ejercicio no es mostrar
como funciona la aplicacion, si no como ahora surgen variables de entornos que puedo utilizar en la aplicacion
para brindar informacion acerca de nuestro pod.

Ahora, si corremos el siguiente comando veremos las variables de entorno, tanto nuevas, como otras previamente
existentes que podemos utilizar en nuestras aplicaciones.

```bash
> kubectl exec -it greet-pod -- env

PATH=/usr/java/openjdk-17/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
HOSTNAME=greet-pod
TERM=xterm
NODE_NAME=docker-desktop
SERVICE_ACCOUNT=default
CONTAINER_CPU_REQUEST_MILLICORES=0
CONTAINER_MEMORY_LIMIT_KIBIBYTES=7922684
POD_NAME=greet-pod
POD_NAMESPACE=default
POD_IP=10.1.1.228
KUBERNETES_PORT_443_TCP_ADDR=10.96.0.1
KUBERNETES_SERVICE_HOST=10.96.0.1
KUBERNETES_SERVICE_PORT=443
KUBERNETES_SERVICE_PORT_HTTPS=443
KUBERNETES_PORT=tcp://10.96.0.1:443
KUBERNETES_PORT_443_TCP=tcp://10.96.0.1:443
KUBERNETES_PORT_443_TCP_PROTO=tcp
KUBERNETES_PORT_443_TCP_PORT=443
JAVA_HOME=/usr/java/openjdk-17
LANG=C.UTF-8
JAVA_VERSION=17.0.2
HOME=/root
```