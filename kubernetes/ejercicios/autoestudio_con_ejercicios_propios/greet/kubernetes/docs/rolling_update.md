# Rolling update
Eventually, you’re going to want to update your app. Although this can be achieved using only
ReplicationControllers or ReplicaSets, Kubernetes also provides a Deployment.  

You have two ways of updating all those pods. You can do one of the following:
* Delete all existing pods first and then start the new ones.
* Start new ones and, once they’re up, delete the old ones. You can do this either
by adding all the new pods and then deleting all the old ones at once, or sequentially, by adding new pods and removing old ones gradually.

Both these strategies have their benefits and drawbacks. The first option would lead to a short period of time
when your application is unavailable. The second option requires your app to handle running two versions of the
app at the same time. If your app stores data in a data store, the new version shouldn’t modify the data schema
or the data in such a way that breaks the previous version.

## Deleting old pods and replacing them with new ones
You already know how to get a ReplicationController to replace all its pod instances with pods running a new version. You probably remember the pod template of a ReplicationController can be updated at any time. When the ReplicationController creates new instances, it uses the updated pod template to create them.

If you have a ReplicationController managing a set of v1 pods, you can easily replace them by modifying the pod template so it refers to version v2 of the image and then deleting the old pod instances. The ReplicationController will notice that no pods match its label selector and it will spin up new instances.

**This is the simplest way to update a set of pods, if you can accept the short downtime between the time the old pods are deleted and new ones are started.**

> Consiste en cambiar la version de la imagen en la plantilla del pod en el antiguo ReplicationController, eliminar los pods que utilizaban la version antigua de la image y crear nuevos pods a partir de la version nueva, ya actualizada en la plantilla del pod.

## Spinning up new pods and then deleting the old ones
If you don’t want to see any downtime and your app supports running multiple ver- sions at once, you can turn the process around and first spin up all the new pods and only then delete the old ones. This will require more hardware resources, because you’ll have double the number of pods running at the same time for a short while.

This is a slightly more complex method compared to the previous one, but you should be able to do it by combining what you’ve learned about ReplicationControllers and Services so far.

> Consiste en crear nuevos pods con la version nueva de la imagen y una vez creados, eliminar los pods que utilizan la version antigua.

## SWITCHING FROM THE OLD TO THE NEW VERSION AT ONCE
Pods are usually fronted by a Service. It’s possible to have the Service front only the initial version of the pods while you bring up the pods running the new version. Then, once all the new pods are up, you can change the Service’s label selector and have the Service switch over to the new pods.

This is called a **blue-green deployment**. After switching over, and once you’re sure the new version functions correctly, you’re free to delete the old pods by deleting the old ReplicationController.

> Consiste en definir un nuevo ReplicationController con la version nueva de la imagen en la plantilla del pod. Luego utilizando el nuevo ReplicationController crear los nuevos pods y luego modificar el servicio que utilizaba el ReplicationController antiguo para que apunte a los pods creados por el nuevo ReplicationController.

**NOTE** You can change a Service’s pod selector with the kubectl set selector command.

## PERFORMING A ROLLING UPDATE
Instead of bringing up all the new pods and deleting the old pods at once, you can also perform a rolling update, which replaces pods step by step. You do this by slowly scaling down the previous ReplicationController and scaling up the new one. In this case, you’ll want the Service’s pod selector to include both the old and the new pods, so it directs requests toward both sets of pods.

Doing a rolling update manually is laborious and error-prone. Depending on the number of replicas, you’d need to run a dozen or more commands in the proper order to perform the update process. Luckily, Kubernetes allows you to perform the rolling update with a single command.

> A grandes rasgos lo que se hace es crear un nuevo ReplicationController el cual dispondra en la plantilla del pod de la nueva version
 de la imagen. Mientras que el servicio que utilizaba el antiguo ReplicationController debe ser capaz de atender los pods de ambos ReplicationController.
 Luego, se elimina un pod creado por el ReplicationController antiguo y se crea un pod utilizando el ReplicationController nuevo, y seguimos asi iterativamente,
 hasta que todos los pods creados por el ReplicationController antiguo fueron reemplazados por un pod creado por el ReplicationController nuevo. 
> 
>> Vale destacar que los pods creados por el ReplicationController nuevo se crean iterativamente cada vez que se elimino uno creado por el ReplicationController antiguo,
 ,es decir, no existian previamente. 

## Ejemplo
Supongamos que nuestro pod fue creado a partir de los siguientes archivos

```yaml
#deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: greet-deployment
spec:
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
        - name: greet
          image: eduard2diaz/greet:storage
          ports:
            - containerPort: 8080
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
    - protocol: 'TCP'
      port: 80
      targetPort: 8080
  type: NodePort
```

Una vez aplicados los ficheros anteriores
> kubectl apply -f deployment.yaml -f service.yaml

tendriamos los siguientes servicios
>  kubectl get services

    NAME         TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)        AGE
    greet        NodePort    10.108.111.79   <none>        80:31068/TCP   18s
    kubernetes   ClusterIP   10.96.0.1       <none>        443/TCP        2d3h

Y nuestro deployment quedaria
> kubectl get deployments

    NAME               READY   UP-TO-DATE   AVAILABLE   AGE
    greet-deployment   3/3     3            3           55s

Si queremos actualizar la imagen los pods creados por dicho deployment basta con ejecutar el siguiente comando
> kubectl set image deployment/greet-deployment greet=eduard2diaz/greet:logger

Para ver el status del proceso de actualizacion usamos el comando
> kubectl rollout status deployment/greet-deployment

Ahora, si en caso de que ocurra un error queremos revertir el deployment seria:
> kubectl rollout undo deployment/greet-deployment

Si quiero ver el historial de actualizacion de un deployment seria
```bash
> kubectl rollout history deployment greet-deployment
    
    deployment.apps/greet-deployment
    REVISION  CHANGE-CAUSE
    3         <none>
    4         <none>
```

> Cuando ejecutas un rolling update en un Deployment, Kubernetes comienza a reemplazar gradualmente los pods antiguos con nuevos pods que tienen una nueva versión de la imagen, configuración, o algún otro cambio. Si, por alguna razón, necesitas detener ese proceso (por ejemplo, si necesitas realizar una verificación o corregir algo antes de continuar con la actualización), puedes pausar el rollout.
>> kubectl rollout pause deployment greet-deployment
>
> ¿Cuándo es útil pausar un rollout?
> * Problemas durante la actualización: Si notas que algo no está funcionando bien durante la actualización (por ejemplo, los nuevos pods no están funcionando correctamente), puedes pausar el rollout para investigar y corregir los problemas antes de que Kubernetes siga reemplazando más pods.
> * Pruebas: Si necesitas hacer pruebas entre las versiones del pod durante la actualización, puedes pausar el proceso y luego reanudarlo cuando todo esté listo.
> * Cambio de estrategia: Si decides cambiar algunos parámetros del Deployment o hacer ajustes en la configuración mientras se está realizando la actualización, puedes pausar el rollout, hacer los cambios y luego reanudarlo.
>
> ¿Cómo reanudar el rollout?
>
> Una vez que hayas pausado el proceso de actualización, puedes reanudarlo con el siguiente comando:
>> kubectl rollout resume deployment greet-deployment

**Ver codigo en la carpeta `rolling_update`.**