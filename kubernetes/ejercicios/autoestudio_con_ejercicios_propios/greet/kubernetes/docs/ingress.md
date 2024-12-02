# Ingress

Un Ingress en Kubernetes es un recurso de red que gestiona el acceso externo a los servicios en un clúster. Ofrece un punto de entrada único para múltiples servicios y permite controlar el tráfico HTTP y HTTPS usando reglas específicas, como nombres de host, rutas de URL, y más.

**Características principales de Ingress**
1. Punto de entrada único:

* Proporciona un único punto de acceso para múltiples servicios dentro del clúster.
* Permite definir reglas para enrutar el tráfico basado en rutas o nombres de host.

2. Balanceo de carga:

* Puede actuar como un balanceador de carga para distribuir solicitudes entre varios Pods o servicios.

3. Soporte para HTTPS:

* Permite manejar certificados SSL para ofrecer tráfico seguro.

4. Configuración avanzada:

* Soporta redirecciones, encabezados personalizados, autenticación, y más, dependiendo del controlador de Ingress usado.

**Componentes de Ingress**
1. Ingress Resource:

* Es el manifiesto YAML que define las reglas de enrutamiento y los servicios asociados.
2. Ingress Controller:

* Es el componente que implementa las reglas definidas en el recurso de Ingress.
Ejemplos: NGINX, Traefik, HAProxy.

De manera general los Ingress funcionan como DNS que permiten redirigir el trafico a partir un dominio personalizado y no 
utilizar una direccion IP y un puerto.

**Pasos:**

1. Verificar que tenemos un Ingress Controller
   * Para ello podemos correr el correr el comando
  >    kubectl get pod --all-namespaces

     kubectl get pod --all-namespaces                         
     NAMESPACE       NAME                                        READY   STATUS      RESTARTS       AGE
     ingress-nginx   ingress-nginx-admission-create-z2r92        0/1     Completed   0              23m
     ingress-nginx   ingress-nginx-admission-patch-mfznz         0/1     Completed   0              23m
     ingress-nginx   ingress-nginx-controller-6568cc55cd-kpdmc   1/1     Running     0              23m
     kube-system     coredns-7db6d8ff4d-nkhjs                    1/1     Running     24 (39m ago)   21d
     kube-system     coredns-7db6d8ff4d-swxkn                    1/1     Running     24 (39m ago)   21d
     kube-system     etcd-docker-desktop                         1/1     Running     24 (10h ago)   21d
     kube-system     kube-apiserver-docker-desktop               1/1     Running     24 (39m ago)   21d
     kube-system     kube-controller-manager-docker-desktop      1/1     Running     24 (10h ago)   21d
     kube-system     kube-proxy-mt59p                            1/1     Running     24 (10h ago)   21d
     kube-system     kube-scheduler-docker-desktop               1/1     Running     24 (39m ago)   21d
     kube-system     storage-provisioner                         1/1     Running     47 (39m ago)   21d
     kube-system     vpnkit-controller                           1/1     Running     24 (10h ago)   21d

Existen varios controladores en kubernetes, por ejemplo los de nginx, pero en este caso nosotros tenemos `ingress-nginx-controller-6568cc55cd-kpdmc`.
Si no queremos ver tanta informacion basta con ejecutar el comando

>  kubectl get pods -n ingress-nginx
    
    NAME                                        READY   STATUS      RESTARTS   AGE
    ingress-nginx-admission-create-z2r92        0/1     Completed   0          25m
    ingress-nginx-admission-patch-mfznz         0/1     Completed   0          25m
    ingress-nginx-controller-6568cc55cd-kpdmc   1/1     Running     0          25m

* Si de casualidad no tenemos ningun ingress basta con ejecutar el comando 

> kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/cloud/deploy.yaml

2. Una vez hecho lo anterior pasamos a definir nuestros recursos.

Primeramente definimos el puerto y el servicio, esto funciona igual a como lo haciamos antes
```yaml
#pod.yaml
apiVersion: v1
kind: Pod
metadata:
  name: greet-pod-default
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
  #nombre del service
  name: greet-internal-service
spec:
  selector:
    app: greet
  ports:
    - protocol: 'TCP'
      #puerto interno del service
      port: 80
      targetPort: 8080
  type: NodePort
```

Luego solo nos queda definir el ingress

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: greet-ingress
spec:
  ingressClassName: nginx
  rules:
    - host: myapp-endpoint.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                #name debe coincidir con el nombre del service
                name: greet-internal-service
                port:
                  #port debe coincidir con el puerto interno del service
                  number: 80
```
En este parte vale destacar la propiedad `ingressClassName`. Los posibles valores que puedas definir dependen
del ingress controller que tengas instalado. Entonces, para saber que nombres de ingress puedes utilizar
basta con ejecutar el comando

>  kubectl get ingressclass

    NAME    CONTROLLER             PARAMETERS   AGE
    nginx   k8s.io/ingress-nginx   <none>       26m

Luego de ejecutar el comando 

> kubectl apply -f pod.yaml -f service.yaml -f ingress.yaml

veras que si listas los ingress, es decir, si corres el comando 

> kubectl get ingress

obtendras algo como. **Es importante que el campo ADDRESS NO ESTE VACIO, SI NO ALGO ESTA FALTANDO**

    NAME            CLASS   HOSTS                ADDRESS     PORTS   AGE
    greet-ingress   nginx   myapp-endpoint.com   localhost   80      17m

Asimismo, si quieres obtener todos los detalles de un ingress basta con ejecutar el comando

> kubectl describe ingress greet-ingress

y obtendras algo como

```yaml
Name:             greet-ingress
Labels:           <none>
Namespace:        default
Address:          localhost
Ingress Class:    nginx
Default backend:  <default>
Rules:
  Host                Path  Backends
  ----                ----  --------
  myapp-endpoint.com  
                      /   greet-internal-service:80 (10.1.1.145:8080)
Annotations:          <none>
Events:
  Type    Reason  Age                From                      Message
  ----    ------  ----               ----                      -------
  Normal  Sync    18m (x2 over 19m)  nginx-ingress-controller  Scheduled for sync
```

No obstante si intentas cargar la url http://myapp-endpoint.com en el navegador veras que obtienes un error 404,
esto se debe a que debes indicarle a tu computadora que host es el responsable de procesar dicha solicitud.

> sudo nano /etc/hosts

Adicional al listado

```bash
127.0.0.1 myapp-endpoint.com
```

Y te deberia estar quedando algo como

```bash
##
# Host Database
#
# localhost is used to configure the loopback interface
# when the system is booting.  Do not change this entry.
##
127.0.0.1       localhost
255.255.255.255 broadcasthost
::1             localhost
127.0.0.1 myapp-endpoint.com
```
Una vez hecho lo anterior, ya la url te debe estar cargando. Si no verifica los logs del ingress usando el comando
> kubectl logs -n ingress-nginx ingress-nginx-controller-6568cc55cd-kpdmc