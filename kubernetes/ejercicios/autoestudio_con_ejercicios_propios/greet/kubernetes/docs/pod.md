# Pod
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

A continuacion aplicaremos el pod y servicio creado
> kubectl apply -f pod.yaml -f service.yaml
    
Entonces, si listamos los pods obtendremos que
> kubectl get pods

    NAME        READY   STATUS    RESTARTS   AGE
    greet-pod   1/1     Running   0          8s

Y si listamos los servicios obtendremos, algo similar a lo siguiente

> kubectl get services
    
    NAME         TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)        AGE
    greet        NodePort    10.101.160.124   <none>        80:30395/TCP   14s
    kubernetes   ClusterIP   10.96.0.1        <none>        443/TCP        19d

Cuando trabajas directamente con un Pod, no puedes definir el número de réplicas porque un Pod es una entidad única y no está diseñado para ser escalable por sí mismo. Para manejar múltiples réplicas, necesitas un controlador como un ReplicaSet o un Deployment.