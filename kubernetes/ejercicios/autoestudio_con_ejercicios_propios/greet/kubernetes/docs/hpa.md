# Vertical Pod Autoscaler (VPA)

Un HorizontalPodAutoscaler (HPA), se encarga de escalar horizontalmente (añadir o eliminar réplicas) un conjunto de Pods en función del uso de recursos, como CPU o memoria.

## Comportamiento del HPA
* Escalado hacia arriba (aumentar réplicas):
Si el uso promedio de la CPU supera el 10% en los Pods del Deployment greet, el HPA añadirá más réplicas (hasta el límite de 5).
* Escalado hacia abajo (reducir réplicas):
Si el uso promedio de la CPU cae por debajo del 10%, el HPA reducirá las réplicas (pero no menos de 2).

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

```yaml
#deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: greet
spec:
  replicas: 1
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
          #tengo que indicar los recursos solicitados para cada pod, sino no funcionara el escalado
          resources:
            requests:
              cpu: 100m
            limits:
              cpu: 200m
```

```yaml
#hpa.yaml
apiVersion: autoscaling/v1
kind: HorizontalPodAutoscaler
metadata:
  name: greet-replicaset
  namespace: default
spec:
  maxReplicas: 5
  minReplicas: 2
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: greet
  #HPA escalará los pods solo si el promedio de uso de la CPU de los Pods del Deployment greet supera el 10% de la CPU solicitada.
  targetCPUUtilizationPercentage: 10
```

> kubectl apply -f service.yaml -f deployment.yaml -f hpa.yaml

> kubectl get pods
    
    NAME                     READY   STATUS    RESTARTS   AGE
    greet-8584bf6bdd-tvmff   1/1     Running   0          2s

```bash
> kubectl get pods

NAME                     READY   STATUS              RESTARTS   AGE
greet-574779bd74-65fww   0/1     ContainerCreating   0          1s
greet-574779bd74-nh2fw   1/1     Running             0          5s

> kubectl get pods

NAME                     READY   STATUS    RESTARTS   AGE
greet-574779bd74-65fww   1/1     Running   0          4s
greet-574779bd74-nh2fw   1/1     Running   0          8s
```

Para lista los escaladores horizontales que tenemos seria:

> kubectl get hpa
    
    NAME               REFERENCE          TARGETS              MINPODS   MAXPODS   REPLICAS   AGE
    greet-replicaset   Deployment/greet   cpu: <unknown>/10%   2         5         2          21m