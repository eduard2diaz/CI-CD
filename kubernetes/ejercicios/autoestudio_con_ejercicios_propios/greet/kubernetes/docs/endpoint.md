# Endpoint
En Kubernetes, los Endpoints son un recurso que representa las direcciones IP y puertos de los Pods que
están asociados a un Service. Son la forma en que Kubernetes conecta los clientes que usan un Service
con los Pods que implementan ese Service.

## Theory
Por ejemplo si vamos a nuestra carpeta `enpoint/theory`, y aplicamos los ficheros `pod.yaml` y `service.yaml`

```yaml
#pod.yaml
apiVersion: v1
kind: Pod
metadata:
  name: greet-pod
  labels:
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
  name: greet-service
spec:
  #sessionAffinity puede tener 2 valores (None o ClientIP). Pero si usamos ClientIP cada vez que un usuario haga una
  #solicitud, usando la misma ip de cliente, sera redirigido al mismo pod. Es similar a un sticky session
  sessionAffinity: ClientIP
  selector:
    app: greet
  ports:
    - protocol: 'TCP'
      port: 80
      targetPort: 8080
  type: NodePort
```

Luego si aplicamos los ficheros
> kubectl apply -f pod.yaml -f service.yaml

Veremos el servicio recien creado
> kubectl get services
  
    NAME            TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)        AGE
    greet-service   NodePort    10.110.169.54   <none>        80:32748/TCP   13s
    kubernetes      ClusterIP   10.96.0.1       <none>        443/TCP        20d

y su correspondiente pod
> kubectl get pods    
  
    NAME                       READY   STATUS      RESTARTS   AGE
    greet-pod                  1/1     Running     0          17s

Ahora, podemos verificar que nuestro endpoint fue creado
> kubectl get endpoints greet-service
    
    NAME            ENDPOINTS         AGE
    greet-service   10.1.1.109:8080   45s

Asimismo, para obtener mas informacion referente al mismo basta con
> kubectl get endpoints greet-service -o yaml

```yaml
apiVersion: v1
kind: Endpoints
metadata:
annotations:
endpoints.kubernetes.io/last-change-trigger-time: "2024-11-29T16:23:21Z"
creationTimestamp: "2024-11-29T16:23:20Z"
name: greet-service
namespace: default
resourceVersion: "210976"
uid: 8cf1fea0-1a66-4954-a965-2c62b3073c93
subsets:
- addresses:
  - ip: 10.1.1.109
    nodeName: docker-desktop
    targetRef:
    kind: Pod
    name: greet-pod
    namespace: default
    uid: c0976638-90e6-40d5-ad96-f5ae48589887
    ports:
  - port: 8080
    protocol: TCP
    (base) neyvislopez@MacBook-Pro-de-Neyvis theory %
```

Los Endpoints son un componente crítico en Kubernetes que conecta los Services con los Pods asociados. Son dinámicos y se actualizan automáticamente, permitiendo un enrutamiento de tráfico eficiente y confiable en tu clúster.

| **Concepto**         | **Service**                                       | **Endpoints**                                   |
|-----------------------|--------------------------------------------------|------------------------------------------------|
| **Función**           | Proporciona un punto de acceso estable a los Pods. | Lista las IPs y puertos de los Pods asociados. |
| **Creación**          | Se define explícitamente en un manifiesto.       | Es generado automáticamente por el Service.    |
| **Actualización**     | Es estático, excepto si se cambia el manifiesto. | Se actualiza dinámicamente según el estado de los Pods. |


## Configuracion manual de los endpoint de los servicios
You may have probably realized this already, but having the service’s endpoints decoupled from the service allows them to be configured and updated manually.

If you create a service without a pod selector, Kubernetes won’t even create the Endpoints resource (after all, without a selector, it can’t know which pods to include in the service). It’s up to you to create the Endpoints resource to specify the list of endpoints for the service.

To create a service with manually managed endpoints, you need to create both a Service and an Endpoints resource.

### Accediendo desde otro contenedor
```yaml
#pod.yaml
apiVersion: v1
kind: Pod
metadata:
  name: greet-pod
  labels:
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
  name: external-service
spec:
  ports:
    - port: 80        # Puerto expuesto por el Servicio
      targetPort: 8080 # Redirige al puerto 8080 del contenedor
  selector:
    app: greet
```

```yaml
#endpoint.yaml
apiVersion: v1
kind: Endpoints
metadata:
  #el nombre del endpoint debe coincidir con el nombre del servicio
  name: external-service
subsets:
  #direcciones del endpoint al que el servicio debe redirigir las conexiones
  - addresses:
    - ip: 11.11.11.11
    - ip: 22.22.22.22
    #puerto destino en el endpoint
    ports:
      #puerto por donde corre la app
      - port: 8080
```

kubectl apply -f pod.yaml -f service.yaml -f endpoint.yaml
> kubectl get services

    NAME               TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)   AGE
    external-service   ClusterIP   10.105.128.100   <none>        80/TCP    29s
    kubernetes         ClusterIP   10.96.0.1        <none>        443/TCP   37s

creamos un contenedor de prueba
> kubectl run test-pod --image=busybox --restart=Never -- sleep 3600

corremos el contenedor de prueba
```bash
 kubectl exec -it test-pod -- sh

/ # wget -qO- http://external-service
{"name":"mundo","env":"hola"}/ #
/ # wget -qO- http://external-service
{"name":"mundo","env":"hola"}/ #
```

**Flujo:**
1. Solicitud del Cliente:
* El cliente envía una solicitud a http://external-service:80.
2. El Service recibe la solicitud:
* Identifica que está asociado al Endpoint external-service.
* Redirige el tráfico al IP 11.11.11.11 y puerto 8080.
3. El Endpoint conecta al destino:
* El tráfico se envía a la dirección configurada.
4. El destino (Pod o externo) responde:
* El contenedor procesa la solicitud y devuelve la respuesta al cliente.

**Resumen del Flujo**
1. Cliente → Service → Endpoint → Pod/Contenedor.
2. Kubernetes utiliza el Service para proporcionar un punto de acceso estable y balancear la carga entre los Endpoints.
3. Los Endpoints actúan como mapeos entre el Service y los Pods (u otros destinos).
4. La configuración de puertos en el Service, Endpoint y Pod debe ser consistente para que el flujo sea exitoso.

### Accediendo desde el navegador web usando un puerto Fijo

Sin embargo, queremos poder acceder a la aplicacion desde el navegador. Para ello definimos esta variante en la carpeta
`manually-browser`. La unica diferencia radica en al forma en que se define el servicio. Este quedaria como:

```bash
apiVersion: v1
kind: Service
metadata:
  name: external-service
spec:
  type: NodePort
  ports:
    - port: 80        # Puerto expuesto por el Servicio
      targetPort: 8080 # Redirige al puerto 8080 del contenedor
      nodePort: 30080 # Puerto fijo en el nodo (opcional, rango 30000-32767)
  selector:
    app: greet
```
Luego, solo nos restaria llamar a la URL http://localhost:30080

### Accediendo desde el navegador web usando LoadBalancer

Otra alternatica es definir el servicio de tipo **LoadBalancer**, para ello el servicio quedaria

```yaml
apiVersion: v1
kind: Service
metadata:
  name: external-service
spec:
  type: LoadBalancer
  ports:
    - port: 80        # Puerto expuesto por el Servicio
      targetPort: 8080 # Redirige al puerto 8080 del contenedor
  selector:
    app: greet
```

| **Método**     | **Ventajas**                                   | **Uso**                                    |
|-----------------|-----------------------------------------------|-------------------------------------------|
| **NodePort**    | Rápido y sencillo para entornos locales.      | Accede con `http://<node-ip>:<nodePort>`. |
| **LoadBalancer**| Ideal para producción en la nube.             | Proporciona una IP pública o privada.     |
| **Minikube**    | Integrado para clústeres locales.             | Usa `minikube service`.                   |
| **Ingress**     | Flexible y poderoso para dominios personalizados. | Requiere un controlador de Ingress.       |
