# PASOS PARA DEPLEGAR EL EJERCICIO:

## Creando un deployment

### Crear fichero de deployment
Primeramente necesitamos crear un fichero donde definiremos la configuracion de nuestro deployment. Realmente el nombre del fichero es irrelevante, pero en este caso lo llamaremos 
deployment.yaml

```yaml
#Al igual que con los docker-compose, el apiVersion define la plantilla sobre la que se definira la configuracion
apiVersion: apps/v1
#Con kind definimos el tipo de objecto de kubernetes que estamos creando
kind: Deployment
metadata:
  #Aqui definiremos el nombre del deployment
  name: second-app-deployment
#En las especificaciones donde definiremos como el deployment es configurado, es decir, aqui se ponen las especificaciones del deployment  
spec:
  #el numero de pods por defecto es 1, por lo que la siguiente linea no es obligatoria. No obstante, si es util si queremos un numero diferente de pods
  #Incluso podemos definri 0 si no queremos inicialmente que ningun pod sea lanzado  
  replicas: 1
  selector: 
    matchLabels:
      #Aqui definiremos las etiquetas de los pods que queremos que macheen con este deployment, es decir, definimos las etiquetas de los pods que este deployment debe controlar
      #esta etiqueta no tiene nada que ver con la etiqueta de la imagen, si no con la etiqueta que le dimos a los pods, por ejemplo en spec.template.metadata.label
      app: second-app
      tier: backend
  #dentro de template definimos cosas importantes como la imagen a utilizar    
  template:
    #Dentro de template no es necesario poner la propiedad kind, pues aqui siempre lo que se esta definiendo es un pod, por lo que la siguiente linea es innecesaria
    #kind: Pod
    metadata: 
      labels:
        #en labels lo que se pone es un objeto clave valor, es decir, como mismo puse app: second-app, podia haber puesto eduardo: nuevo-valor
        app: second-app
        tier: backend
    #dado que dentro de template siempre se esta definiendo un pod, en spec lo que se pone es la configuracion que queremos para el pod, es decir, la especificacion del pod
    spec: 
      containers:
        #dentro de container ponemos la lista de containers que queremos, cada un comenzando un con un -
        #definimos el nombre del contenedor y la imagen que debe usar (la imagen debe estar presente en el registry)
        #OJO: cada contenedor que definamos aqui va esta rpresente en cada uno de los pods que definamos en el campo replica
        - name: second-node
          image: academind/kub-first-app:2
        # - name: ...
        #   image: ...
```

### Ejecucion del fichero de deployment
Luego para aplicar una determinada configuracion basta con ejecuta el comando

> kubectl apply -f=deployment.yaml

donde a la opcion -f le asignamos la ruta relativa del fichero a aplicar. Igualmente si queremos en un mismo comando pudiesemos aplicar varias veces la opcion -f, con multiples
ficheros es decir:

> kubectl apply -f=fichero1.yaml -f=fichero2.yaml,....

Luego de ejecutar el comando anterior, si listamos los pods y los deployments deberiamos obtener algo como esto:

> kubectl get pods

    NAME                                     READY   STATUS    RESTARTS   AGE
    second-app-deployment-655db8d94b-5hd8h   1/1     Running   0          4m47s
    
> kubectl get deployments

    NAME                    READY   UP-TO-DATE   AVAILABLE   AGE
    second-app-deployment   1/1     1            1           4m54s


## Creando un servicio
Primeramente creamos un fichero para la definicio del servicio. Para ello definiremos un fichero yaml con un nombre cualquiera, por ejemplo service.yaml

```yaml
apiVersion: v1
#Indicamos el tipo de recurso como Servicio
kind: Service
metadata:
  #definiremos el nombre del servicio
  name: backend
#definimos las especificaciones del servicio  
spec:
  #Con el selector definiremos que recurso debe ser controlador o manejado, en este caso por este servicio
  selector: 
    # ahora le decimos que el servicio debe manejar los pods que tiene como etiqueta de llave app y como valor de llave second-app
    #No es necesario definir todas las etiquetas de nuestros pods, basta con al menos una, por ejemplo la app, que si vamos al fichero deployments.yaml veremos que fue empleada en el pod
    app: second-app
  ports:
    - protocol: 'TCP'
      #Definimos el puerto externo que usaremos para conectarnos desde el host al contenedor
      port: 80
      #y el puerto interno desde donde esta corriendo la aplicacion
      targetPort: 8080
    # - protocol: 'TCP'
    #   port: 443
    #   targetPort: 443
  #Ademas debemos definir el tipo de servicio, es decir, la estrategia que se utilizara para la asignacion de IP a los pods  
  type: LoadBalancer
```

Luego basta con utilizar el mismo comando para aplicar el deployment, para aplicar el servicio, por ejemplo:

> kubectl apply -f=service.yaml

Y luego verificamos si el servicio fue creado

> kubectl get services

    NAME         TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)        AGE
    backend      NodePort    10.107.173.93   <none>        80:30576/TCP   8s
    kubernetes   ClusterIP   10.96.0.1       <none>        443/TCP        23h

Luego para acceder a la aplicacion una vez desplegada utilizariamos la sig URL http://localhost:30576

## Actualizacion de la infraestructura
Cualquier cambio que querramos hacer en la infraestructura consiste en ir al correspondiente fichero yaml, hacer el cambio, guardar los cambios y aplicar el fichero como hemos
hecho anteriormente. Por ejemplo, si modificamos el fichero deployment.yaml para usar 3 pods, este quedaria de la siguiente forma:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: second-app-deployment
spec:
  replicas: 3
  selector:
    matchLabels:
      app: second-app
      tier: backend
  template:
    metadata: 
      labels:
        app: second-app
        tier: backend
    spec: 
      containers:
        - name: second-node
          image: eduard2diaz/kub-first-app:2
```

luego aplicamos el fichero:
> kubectl apply -f=deployment.yaml

Y si listamos los pods obtendriamos que:

> kubectl get pods                
    NAME                                     READY   STATUS    RESTARTS   AGE
    second-app-deployment-655db8d94b-5hd8h   1/1     Running   0          57m
    second-app-deployment-655db8d94b-j4725   1/1     Running   0          10s
    second-app-deployment-655db8d94b-plplx   1/1     Running   0          10s

y seria el mismo proceso si queremos scale down. Ademas siguiendo los mismos pasos podemos cambiar la imagen, etc.

## Eliminacion de recursos

Para eliminar los recursos podemos usar los mismos comandos que usabamos cuando utilizabamos una estrategia imperative, por ejemplo:

> kubectl delete deployment second-app-deployment
> kubectl delete service backend
> kubectl delete pod second-app-deployment-655db8d94b-5hd8h

Pero, tambien podemos utilizar el fichero de configuracion que usamos para crearlos, para tambien eliminarlos; es decir:

> kubectl delete -f=deployment.yaml

> kubectl delete -f=service.yaml

esto no eliminara el fichero si no los recursos creados a partir del mismo. Incluso podemos eliminar multiples recursos a la vez:

> kubectl delete -f=deployment.yaml -f=service.yaml

## Multiples o singulares ficheros de configuracion

Como mismo podemos tener un fichero de configuracio para cada cosa, tambien podemos tener un unico fichero de configuracion que lo englobe todo. Supon que tengamos el fichero
master-deployment.yaml

```yaml
apiVersion: v1
kind: Service
metadata:
  name: backend
spec:
  selector: 
    app: second-app
  ports:
    - protocol: 'TCP'
      port: 80
      targetPort: 8080
  type: NodePort
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: second-app-deployment
spec:
  replicas: 3
  selector:
    matchLabels:
      app: second-app
      tier: backend
  template:
    metadata: 
      labels:
        app: second-app
        tier: backend
    spec: 
      containers:
        - name: second-node
          image: eduard2diaz/kub-first-app:2
```
fijate que la definicion de cada recurso debe estar separada por ---, ojo, no deben ser 1 o 2, sino 3 dashes (---). Como los recursos son creados de arriba hacia abajo, siempre
es recomendado poner la defincion de los servicios arriba y la de los deployments debajo.

Finalmente para aplicarlo bastaria con:
> kubectl apply -f=master-deployment.yaml

## Match expressions
En el caso de los deployments, podemos utilizar match expressions para definir reglas mas rebuscadas para identificar los pods que este deployment va a manejar. Por ejemplo podemos usar

```yaml
matchExpressions:
  - {key: app, values: [second-app, first-app]}
```
Para definir que atienda a los pods donde la etiqueta tenga como llave `app` y como valor `second-app` o `first-app`. Ademas esto se puede combinar utilziando los operadores `In`, `NotIn`,
`Exists` y `DoesNotExist`, por ejemplo

```yaml
matchExpressions:
  - {key: app, operator: NotIn, values: [second-app, first-app]}
```
Para definir que atienda a los pods donde la etiqueta tenga como llave `app` y no tiene como valor `second-app` ni `first-app`.

Un ejemplo general de lo anterior seria el siguiente:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: second-app-deployment
spec:
  replicas: 3
  selector:
    #matchLabels:
    #  app: second-app
    #  tier: backend
    matchExpressions:
      - {key: app, operator: NotIn, values: [second-app, first-app]}
  template:
    metadata: 
      labels:
        app: second-app
        tier: backend
    spec: 
      containers:
        - name: second-node
          image: eduard2diaz/kub-first-app:2
```

## Eliminando recursos en base a su etiqueta
Como vimos podemos elimicar recursos utilizando el nombre del fichero que lo creo, o el comando

> kubectl delete tipo_recurso nombre_recurso

Sin embargo, tambien podemos eliminar los recursos en funcion de su(s) etiqueta(s); por ejemplo, supongamos que tenemos el siguiente fichero de deployment `deployment-tag.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: second-app-deployment
  labels:
    group: example
spec:
  replicas: 1
  selector:
    matchLabels:
      app: second-app
      tier: backend
  template:
    metadata: 
      labels:
        app: second-app
        tier: backend
    spec: 
      containers:
        - name: second-node
          image: academind/kub-first-app:2
```

Si este fichero fue aplicado 

> kubectl apply -f=master-deployment.yaml

y despues queremos eliminar el/los recursos con etiqueta group=example, bastaria con

> kubectl delete -l group=example

Ahora, si solo queremos eliminar un tipo de recurso en especifico, por ejemplo los deployments, bastaria con:

> kubectl delete deployments -l group=example

O si queremos eliminar 2 o mas grupos de recursos en especifico a la vez que tengan una determinada etiqueta bastaria con:

> kubectl delete deployments, services -l group=example

## Prueba de vida

Podemos definirle a nuestras imagenes una prueba de vida, que no es mas que definir un periodo cada cuanto kubernetes va a hacer una solicitud al contenedor o contenedores
donde se encuentra la imagen, especificamente, donde se encuentra la aplicacion, y a va a verificar una url de la aplicacion. Si obtiene respuesta exitosa espera cierto periodo de tiempo 
y vuelve a hacer la solicitud, y asi sucesivamente. **Pero si la respuesta no es exitosa reinicia el contenedor**.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: second-app-deployment
  labels:
    group: example
spec:
  replicas: 1
  selector:
    matchLabels:
      app: second-app
      tier: backend
  template:
    metadata: 
      labels:
        app: second-app
        tier: backend
    spec: 
      containers:
        - name: second-node
          image: academind/kub-first-app:2
          #Definicion de la prueba de vida
          livenessProbe:
            httpGet:
              #ruta a chequear para verificar si el contenedor esta corriendo
              path: /
              #Puerto por el que la app esta corriendo
              port: 8080
            #Numero de segundos cada cuanto se va a hacer la prueba de que el contenedor esta vivo
            period: 10
            #Cuando tiempo debe esperar para realizar la primera prueba
            initialDelaySeconds: 5
```