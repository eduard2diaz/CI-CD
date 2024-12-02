# PASOS PARA DEPLEGAR EL EJERCICIO:

## Deployment imperativo
### Crear la imagen
Primeramente debemos creat la imagen de docker
> docker build -t kub-first-app .

### Crear el deployment
Para intentar crear un deployment (el cual automaticamente crea el pod e intenta levantarlo) seria:
> kubectl create deployment first-app --image=kub-first-app

, donde first-app es el nombre del deployment. Sin embargo, si kub-first-ap se trata de una imagen local y no remota , cuando listemos los deployments.

Para ver los deployments que tenemos seria:
> kubectl get deployments

Luego de ejecutar el comando anterior veremos algo como esto: 

    NAME        READY   UP-TO-DATE   AVAILABLE   AGE
    first-app   0/1     1            0           3m57s


indicando que ese deployment fallo, y si listamos los pods:

Para listar los pods que tenemos seria:
> kubectl get pods

obtendremos algo similar a esto:
    
    NAME                         READY   STATUS             RESTARTS   AGE
    first-app-6fc798dbcd-pfn4j   0/1     ImagePullBackOff   0          47s 

el que en el status no esta indicando que fallo el pull de la imagen, y claro que fallo si la imagen es local. Por lo que el comando

> kubectl create deployment first-app --image=kub-first-app
    
**nos sirve especificamente para imagenes presentes en el Image Registry, es decir, imagenes remotas**

Por lo anterior procederemos a eliminar el deployment:
> kubectl delete deployment first-app

    Lo que nos elimina el deployment y el pod

### Subir la imagen a dockerhub:
Ahora procederemos a subir la imagen a docker-hub:

EN CASO DE SER NECESARIO, RENOMBRAMOS LA IMAGEN PARA QUE COINCIDA CON EL NOMBRE DEL REPOSITORIO EN DOCKERHUB
> docker tag kub-first-app eduard2diaz/kub-first-app

SUBIMOS LA IMAGEN
> docker push eduard2diaz/kub-first-app

Y por ultimo volvemos a crear el deployment
> kubectl create deployment first-app --image=eduard2diaz/kub-first-app

### Visualizacion (Opcional)

Usaremos minikube solo para ver lo que creemos en su dashboard.

Para instalar minikube seria:
> brew install minikube

Para saber si minikube esta corriendo:
> minikube status

Para levantar minikube:
> minikube start --driver=docker

Para desinstalar minikube encontre este comando en internet:
> minikube stop; minikube delete &&
docker stop $(docker ps -aq) &&
rm -rf ~/.kube ~/.minikube &&
sudo rm -rf /usr/local/bin/localkube /usr/local/bin/minikube &&
launchctl stop '*kubelet*.mount' &&
launchctl stop localkube.service &&
launchctl disable localkube.service &&
sudo rm -rf /etc/kubernetes/ &&
docker system prune -af --volumes

Finalmente, para acceder al dashboard usaremos el sig. comando:
> minikube dashboard

HASTA AQUI EL EJEMPLO (**ESTA ES LA FORMA IMPERTATIVA DE CREAR DEPLOYMENTS**)

### Nota

En Kubernetes, un Deployment es un objeto de configuración que define cómo se debe implementar y gestionar un conjunto de réplicas de un contenedor en un clúster. Los Deployments son una de las formas más comunes de administrar la ejecución y el escalado de aplicaciones en Kubernetes, ya que proporcionan una forma declarativa de describir el estado deseado de la aplicación y permiten que Kubernetes gestione el ciclo de vida de los contenedores para cumplir con ese estado.

Principales características de un Deployment:
- Control de versiones y actualizaciones: Permite realizar actualizaciones controladas y graduales a una nueva versión de una aplicación sin tiempo de inactividad, usando estrategias de actualización, como Rolling Updates (actualización continua) o Recreate (recrear todos los pods simultáneamente).

- Escalado automático: Puede escalar dinámicamente el número de réplicas de la aplicación (número de pods) en función de la carga de trabajo, permitiendo responder de forma rápida a cambios en la demanda.

- Autoreparación: Si un pod falla, el Deployment reemplaza el pod dañado por uno nuevo para mantener el estado deseado de la aplicación.

- Historial de versiones y reversión: Los Deployments permiten mantener un historial de versiones, lo que facilita revertir cambios a una versión anterior si algo falla en una actualización.

## Service
El Dockerfile de este proyecto dice que el mismo estaria corriendo por el puerto 8080, sin embargo, cuando llamamos a http://localhost:8080, no tenemos nada. Esto se debe a que si bien
el pod esta corriendo, este no esta expuesto. **Para esto nos sirven los servicios**.

Si bien kubectl tiene comandos para la creacion de servicios, como por ejemplo **kubectl create service**; en este caso dado que en el paso anterior creamos un deployment, resulta mas 
conveniente usar el siguiente comando para exponer dicho deployment, **como resultado de esta accion igualmente sera creado un servicio**:

> kubectl expose deployment first-app --type=LoadBalancer --port=8080

- ClusterIP: es el tipo por defecto. Este indica que el nodo pod solo podra ser visible desde dentro del cluster.
- NodePort: indica que el deployment sera accesible a traves de la direccion IP del nodo sobre el que esta corriendo.
- LoadBalancer: utiliza un balanceador de carga, que debe existir dentro de la infraestructura sobre la que nuestro cluster corre.
Y en este caso es el balanceador de carga el que distribuye la carga entre los nodos existentes.

Luego para verificar que el servicio fue creado basta con ejecutar el siguiente comando:
> kubectl get services

y debemos obtener algo como:

    NAME         TYPE           CLUSTER-IP     EXTERNAL-IP   PORT(S)          AGE
    first-app    LoadBalancer   10.107.87.76   localhost     8080:30924/TCP   32s
    kubernetes   ClusterIP      10.96.0.1      <none>        443/TCP

Sin embargo, oara oider usar LoadBalancer tenemos que garantizar que el provider lo soporta, por ejemplo AWS lo soporta, y minikube tambien. De no ser asi
cuando intentemos acceder al http://localhost:30924, (en este caso ese fue el puerto) seguiriamos sin ver nada. Por lo que usaremos minikube para obtener una direccion Ip
desde donde acceder al servicio

> minikube service first-app

Pero si por casualidad no tenemos minikube o este no es capaz de ver los servicios que creamos con kubectl, deberiamos usar otro tipo de servicio, por ejemplo **NodePort**.
Para ello primero tendriamos que borrar el servicio que creamos para ese deployment, en caso que lo hallamos definido. 

Por ejemplo, si necesitamos borrar el servicio seria:
> kubectl delete service first-app

Y luego creariamos de nuevo el servicio:
> kubectl expose deployment first-app --type=NodePort --port=8080

Ahora al listar los servicios obtendriamos:
> kubectl get services                                         

    NAME         TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
    first-app    NodePort    10.108.199.37   <none>        8080:31549/TCP   13s
    kubernetes   ClusterIP   10.96.0.1       <none>        443/TCP          14h

y ya podriamos acceder a la app a traves de http://localhost:31549

**EN ESTE EJEMPLO LA VENTAJA DEL DEPLOYMENT ES QUE PERMITE QUE CADA VEZ QUE SE DETENGA EL CONTAINER DENTRO POD, ESTE SERA REINICIADO.** Por ejemplo, si llamamos a http://localhost:31549/error y luego listamos los pods veremos que el pod fue reiniciado, esto a causa de que dicha ruta detiene el proceso (**process.exit(1)**)

### Nota

En Kubernetes, los servicios (services) son recursos que proporcionan una forma estable de acceder a los pods, permitiendo una comunicación confiable entre componentes de una aplicación o entre aplicaciones distintas. Dado que los pods son efímeros y pueden ser creados o eliminados constantemente, los servicios ofrecen una capa de abstracción para acceder a los pods de manera consistente, sin preocuparse por su ciclo de vida individual. A continuación, algunos conceptos clave sobre los servicios en Kubernetes:

1. Propósito de los Servicios
- Los servicios permiten exponer un conjunto de pods a través de una dirección IP y un puerto estables.
- Pueden equilibrar la carga entre varios pods, asegurando que las solicitudes se distribuyan uniformemente entre las instancias de la aplicación.
- Ayudan a resolver el problema de acceso a los pods que pueden cambiar de dirección IP al ser reiniciados o reemplazados.
2. Selector y Endpoints
- Los servicios suelen usar un selector (etiqueta o conjunto de etiquetas) para identificar los pods que deben ser accesibles a través de ellos.
- Basándose en el selector, el servicio mantiene una lista de endpoints (direcciones IP de pods) a los que puede redirigir el tráfico.
3. Tipos de Servicios en Kubernetes
- ClusterIP (por defecto): Hace que el servicio sea accesible solo dentro del clúster, asignándole una IP interna. Ideal para comunicación interna entre aplicaciones.
- NodePort: Expone el servicio a través de un puerto específico en cada nodo del clúster. Permite acceder al servicio desde fuera del clúster usando la IP del nodo y el puerto especificado.
- LoadBalancer: Crea un balanceador de carga externo (si se usa en un entorno en la nube) para exponer el servicio al mundo exterior. Distribuye automáticamente el tráfico entre los pods detrás del servicio.
- ExternalName: Permite mapear el servicio a un nombre de dominio externo, redirigiendo el tráfico a una URL externa en lugar de a los pods dentro del clúster.

## Scaling
Podemos tambien escalar el numero de pods que el deployment debe mantener en ejecucion. Para ello basta con ejecutar el siguiente comando:

> kubectl scale deployment/first-app --replicas=3

Una vez ejecutado el comando anterior, si listamos los pods vemos que tenemos 3 instancias corriendo

> kubectl get pods                               
    NAME                         READY   STATUS    RESTARTS      AGE
    first-app-7bc78b754d-dzzgl   1/1     Running   0             4s
    first-app-7bc78b754d-j6l6m   1/1     Running   3 (20m ago)   13h
    first-app-7bc78b754d-rts5r   1/1     Running   0             4s

Esto ademas se refleja si listamos los deployments, donde vemos que hay 3 pods ejecutandose de un total de 3 disponibles (3/3)
> kubectl get deployments
    NAME        READY   UP-TO-DATE   AVAILABLE   AGE
    first-app   3/3     3            3           13h

No obstante, si queremos podemos volver a tener un solo pod ejecutandose en ese deployment.

> kubectl scale deployment/first-app --replicas=1

Al ejecutar el comando anterior y listar los pods vemos que fueron terminados todos excepto el numero de pods definidos

> kubectl get pods                               
    NAME                         READY   STATUS        RESTARTS      AGE
    first-app-7bc78b754d-dzzgl   1/1     Terminating   0             2m39s
    first-app-7bc78b754d-j6l6m   1/1     Running       3 (23m ago)   13h
    first-app-7bc78b754d-rts5r   1/1     Terminating   0             2m39s

Lo anterior tambien se refleja si listamos los deployments
> kubectl get deployments                        
    NAME        READY   UP-TO-DATE   AVAILABLE   AGE
    first-app   1/1     1            1           13h

## Updating deployment   
Supongamos que queremos hacer algun cambio en nuestra aplicacion; por ejemplo cambiar

```javascript
app.get('/', (req, res) => {
  res.send(`
    <h1>Hello from this NodeJS app!</h1>
    <p>Try sending a request to /error and see what happens</p>
  `);
});
```

por

```javascript
app.get('/', (req, res) => {
  res.send(`
    <h1>Hello from this Eduardo's NodeJS app!</h1>
    <p>Try sending a request to /error and see what happens</p>
  `);
});
```

Luego tendriamos que construir nuevamente la imagen.
**OJOL la imagen debe ser creada con un nuevo tag porque si no kubernetes va a usar la imagen vieja que ya tiene en cache**

> docker build -t eduard2diaz/kub-first-app:2 .

Luego subimos la imagen a nuestro service registy, en este caso dockerhub, pues de ahi es de kubernetes intenetara descargarla:

> docker push eduard2diaz/kub-first-app:2

Debemos ver el siguiente mensaje

    deployment.apps/first-app image updated

Luego debemos verificar que el deployment todavia esta ahi, para ello simplemente listaremos los deployments

> kubectl get deployments

debemos ver algo como esto: 

    NAME        READY   UP-TO-DATE   AVAILABLE   AGE
    first-app   1/1     1            1           14h

Luego debemos actualizar la imagen en el contenedor
> kubectl set image deployment/first-app kub-first-app=eduard2diaz/kub-first-app

, en este caso :
 - deployment/first-app: es donde esta nuestro deployment
 - kub-first-app: es el nombre del contenedor dentro de nuestro pod
 - eduard2diaz/kub-first-app: es la imagen a utilizar

Para listar los contenedores en todos los pods en todos los namespaces

> kubectl get pods --all-namespaces -o jsonpath="{range .items[*]}{.metadata.namespace}{'/'}{.metadata.name}{': '}{range .spec.containers[*]}{.name}{' '}{end}{'\n'}{end}"

y debemos ver algo similar a lo siguiente:

    default/first-app-55d8669d7f-gggfw: kub-first-app 
    kube-system/coredns-7db6d8ff4d-nkhjs: coredns 
    kube-system/coredns-7db6d8ff4d-swxkn: coredns 
    kube-system/etcd-docker-desktop: etcd 
    kube-system/kube-apiserver-docker-desktop: kube-apiserver 
    kube-system/kube-controller-manager-docker-desktop: kube-controller-manager 
    kube-system/kube-proxy-mt59p: kube-proxy 
    kube-system/kube-scheduler-docker-desktop: kube-scheduler 
    kube-system/storage-provisioner: storage-provisioner 
    kube-system/vpnkit-controller: vpnkit-controller

Para saber como va el proceso de actualizacion de la imagen dentro del deployment podriamos ejecutar el comando:

> kubectl rollout status deployment/first-app

El cual nos debe devolver una salida similar a la siguiente

    deployment "first-app" successfully rolled out

Una vez hecho todo lo anterior, si visitamos la URL http://localhost:31549 debemos ver nuestros cambios reflejados.

### Nota
En Kubernetes, los namespaces son una forma de organizar y dividir los recursos dentro de un clúster. Funcionan como espacios de nombres lógicos que permiten agrupar y aislar recursos (como pods, servicios, y deployments), lo cual es útil en escenarios donde se requiere separación entre diferentes entornos, equipos o aplicaciones dentro de un mismo clúster.

#### ¿Para Qué Sirven los Namespaces?
- Aislamiento de Recursos: Facilitan la separación de recursos entre diferentes aplicaciones, equipos o entornos, como desarrollo, pruebas y producción.

- Gestión de Acceso y Políticas: Ayudan a definir permisos de acceso. Puedes restringir qué usuarios o roles tienen acceso a ciertos namespaces, proporcionando mayor seguridad y control sobre los recursos.

- Limitación de Recursos: Permiten establecer límites de recursos (como CPU y memoria) a través de ResourceQuotas, asegurando que los equipos o aplicaciones no excedan su presupuesto de recursos.

- Facilita la Administración: Al segmentar los recursos, facilita la administración y organización en clústeres grandes.

#### Namespaces en Kubernetes
Existen algunos namespaces predeterminados en Kubernetes:

- default: Es el namespace por defecto para todos los recursos que no especifican uno. Si no asignas un namespace explícito a un recurso, este se creará en default.
- kube-system: Contiene los recursos internos de Kubernetes, como los componentes del clúster (e.g., kube-dns).
- kube-public: Está disponible para todos los usuarios del clúster, incluyendo usuarios no autenticados. Se usa para datos accesibles públicamente en el clúster.
- kube-node-lease: Contiene objetos de lease que ayudan al control del estado de los nodos en el clúster.

#### Rollback de un deployment

Si de casualidad al ejecutar 
> kubectl rollout status deployment/first-app

obtenemos una salida diferente a

    deployment "first-app" successfully rolled out

, por ejemplo
>
    Waiting for deployment "first-app" rollout to finish: 1 old replicas are pending termination...

podemos ejecutar el siguiente comando para hacer rollback de un deployment problematico:

> kubectl rollout undo deployment/first-app

luego debemos listar los pods y ver si el que nos interesa esta corriendo

> kubectl get pods

y si todo esta bien, volveriamos a ejecutar el comando: 

> kubectl rollout status deployment/first-app

o cualquier otro comando que hallamos intentado ejecutar


#### Historial de un deployment

Para ver el historial de un deployment podemos ejecutar el siguiente comando:

> kubectl rollout history deployment/first-app

y debemos obtener algo como

    deployment.apps/first-app 
    REVISION  CHANGE-CAUSE
    1         <none>
    2         <none>


Asimismo para obtener mas detalles de una determinada revision bastaria con:

> kubectl rollout history deployment/first-app --revision 2
deployment.apps/first-app with revision #2
Pod Template:
  Labels:       app=first-app
        pod-template-hash=65df6cfc48
  Containers:
   kub-first-app:
    Image:      eduard2diaz/kub-first-app:2
    Port:       <none>
    Host Port:  <none>
    Environment:        <none>
    Mounts:     <none>
  Volumes:      <none>
  Node-Selectors:       <none>
  Tolerations:  <none>

Supongamos que queremos hacer rollback y utilizar en nuestro contenedor el codigo de una determinada revision ya hecha, bastaria con ejecutar el comando

> kubectl rollout undo deployment/first-app --to-revision=1

, donde 1 es el numero de la revision a la que queremos ir, y obtendriamos una salida como la siguiente:
    
    deployment.apps/first-app rolled back

Luego, si visitamos http://localhost:31549 veriamos a la aplicacion funcionar como era originalmente, es decir, en la primera revision

### Eliminar servicios y deployment

Hasta aqui el ejemplo de como usar kubernetes con un enfoque imperativo. Entonces a continuacion analizaremos como hacer todo lo anterior a partir
de un enfoque declararativo. Para ello borraremos lo que ya hemos hecho en este ejemplo

Eliminamos el servicio

> kubectl delete service first-app

Eliminamos el deployment
> kubectl delete deployment first-app

Ver ejemplo **kub-action-02-declarative-approach-basics**