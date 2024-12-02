# Secrets
All the information you’ve passed to your containers so far is regular, non-sensitive configuration data that doesn’t need to be kept secure. But as we mentioned at the start of the chapter, the config usually also includes sensitive information, such as cre- dentials and private encryption keys, which need to be kept secure.

Secrets are much like ConfigMaps—they’re also maps that hold key-value pairs. They can be used the same way as a ConfigMap. You can
* Pass Secret entries to the container as environment variables
* Expose Secret entries as files in a volume

> Secrets are always stored in memory and never written to physical storage, which would require wiping the disks after deleting the Secrets from them.
>
> Secrets used to be stored in unencrypted form, which meant the master node needs to be secured to keep the sensi- tive data stored in Secrets secure.

Choosing between them is simple:
  * Use a ConfigMap to store non-sensitive, plain configuration data.
  * Use a Secret to store any data that is sensitive in nature and needs to be kept under key. If a config file includes both sensitive and not-sensitive data, you
  should store the file in a Secret.

> Every pod has a secret volume attached to it automatically.
>
> Secret contains three entries—ca.crt, namespace, and token

**TIP** You can use Secrets even for non-sensitive binary data, but be aware that the maximum size of a Secret is limited to 1MB.

## Enfoque imperativo

Para crear un secreto en la terminal directamente indicando sus datos en la forma clave-valor seria

> kubectl create secret generic mi-secret --from-literal=usuario=admin --from-literal=contrasena=12345

Para listar todos los servicios que tenemos seria

```bash
> kubectl get secrets
                                                                                 
NAME        TYPE     DATA   AGE
mi-secret   Opaque   2      17s
```

Para obtener informacion detallada sobre los secretos que tenemos en kubernetes seria:

```bash
> kubectl describe secrets

Name:         mi-secret
Namespace:    default
Labels:       <none>
Annotations:  <none>

Type:  Opaque

Data
====
usuario:     5 bytes
contrasena:  5 bytes
```

Aunque, tambien podemos crear secretos a traves de ficheros externos

```bash
kubectl create secret generic mi-secret --from-file=usuario=./usuario.txt --from-file=contrasena=./contrasena.txt
```

## Enfoque declarativo basico

Para desplegar nuestra aplicacion usando un enfoque declarativo basta con definir primeramente el secreto.
```yaml
#secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: mi-secret
type: Opaque
data:
  usuario: YWRtaW4=        # "admin" codificado en Base64
  contrasena: MTIzNDU=     # "12345" codificado en Base64
```
fijate que los secretos deben estar codificados en `base64`.

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
            - name: APP_NAME
              valueFrom:
                secretKeyRef:
                  #indicamos el secreto a utilizar
                  name: mi-secret
                  #indicamos la llave del secreto a utilizar
                  key: usuario
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

Luego, basta con aplicar dichos ficheros

>  kubectl apply -f deployment.yaml -f service.yaml -f secret.yaml

Una vez listado nuestros servicios, veremos que si llamamos a http://localhost:31966,
la variable de entorno `APP_NAME` toma su valor desde `mi-secret`, especificamente la llave `usuario`.
```bash
kubectl get services
NAME         TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)        AGE
greet        NodePort    10.111.147.118   <none>        80:31966/TCP   18h
kubernetes   ClusterIP   10.96.0.1        <none>        443/TCP        2d
```

si listamos los secrtos encontraremos el recien creado
```bash
> kubectl get secrets                                            

NAME        TYPE     DATA   AGE
mi-secret   Opaque   2      28m
```
**NOTA** El codigo anterior se encuentra disponible dentro de la carpeta `secrets/basic`.

## Usar secreto como volumen
Ademas de lo que vimos anteriormente, tambien podemos usar secreto como volumen, lo que proporciona
una forma adicional para que la aplicación acceda al secreto desde los archivos. Para ello, 
Kubernetes crea un archivo para cada clave del secreto en la ruta especificada.

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
            - name: APP_NAME
              valueFrom:
                secretKeyRef:
                  name: mi-secret
                  key: usuario
          volumeMounts:
            - name: secret-vol
              #se indica donde se montara el volumen
              mountPath: "/etc/secrets" # Ruta donde estará disponible el secreto
              readOnly: true
      volumes:
        - name: secret-vol
          secret:
            #Se agrega una referencia al secreto llamado mi-secret
            secretName: mi-secret
```
Fuera, de esto, el resto de los ficheros permanecen igual.

**NOTA** Podemos usar el siguiente comando para obtener el valor en texto plano de una determinada llave

> kubectl get secret mi-secret -o jsonpath="{.data.usuario}" | base64 --decode