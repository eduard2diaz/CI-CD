# Environment variables
Ahora supongamos que queremos pasarle la variable de entorno

## Version 1: Variables de entorno en el deployment

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
          #Definimos las variables de entorno
          env:
            #nombre de la variable de entorno
            - name: APP_NAME
              #valor de la variable de entorno
              value: 'adios'
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

Aplicamos los ficheros y seria todo:

> kubectl apply -f=deployment.yaml -f=service.yaml

## Version 2: ConfigMap

```yaml
#environment.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  #nombre del ConfigMap
  name: greet-env
data:
  #llave - valor
  llave_env: cosa
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
          env:
            #nombre de la variable de entorno a configurar
            - name: APP_NAME
              #referencia al config map
              valueFrom: 
                configMapKeyRef:
                  #nombre del config map
                  name: greet-env
                  #nombre de la llave en el config map
                  key: llave_env
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

Luego, solo basta aplicar los ficheros anteriores, **incluyendo el ConfigMap**.

> kubectl apply -f=deployment.yaml -f=service.yaml -f=environment.yaml