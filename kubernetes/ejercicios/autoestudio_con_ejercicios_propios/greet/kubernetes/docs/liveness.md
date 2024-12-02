# Liveness Probe

Si vamos a la carpeta `liveness\pod` encontraremos los siguientes ficheros

El fichero `service.yaml` simplemente define el servicio de nuestro pod
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

Pero, el fichero `pod.yaml` define nuestro pod. Vale destacar dentro de los
`spec` la seccion `livenessProbe` donde se define los detalles de la prueba de
vida que le vamos a hacer a nuestro container
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
      livenessProbe:
        httpGet:
          #ruta a donde hacer la solicitud, dicha ruta no debe requerir autenticacion
          #y preferiblemente es responsable de comprobar el correcto funcionamiento de
          #los componentes, por ejemplo, la ruta /health en los actuator de spring boot
          path: /
          #puerto al que hay que conectarse para hacer la prueba, es donde la app esta corriendo
          port: 8080
```

Luego simplemente restaria aplicar los cambios anteriores:

> kubectl apply -f pod.yaml -f service.yaml

Asimismo si queremos definir un delay para que kubernetes comience a hacer las pruebas, bastaria con modificar el pod
para que quede de la siguiente forma

```yaml
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
      livenessProbe:
        httpGet:
          #ruta a donde hacer la solicitud
          path: /
          #puerto al que hay que conectarse para hacer la prueba, es donde la app esta corriendo
          port: 8080
        # Kubernetes will wait 15 seconds before executing the first probe.
        initialDelaySeconds: 15
```