# Kubernetes TIPS

## Labels

Labels are a simple, yet incredibly powerful, Kubernetes feature for organizing not only pods, but all other Kubernetes resources. A label is an arbitrary key-value pair you attach to a resource, which is then utilized when selecting resources using label selectors (resources are filtered based on whether they include the label specified in the selec- tor). A resource can have more than one label, as long as the keys of those labels are unique within that resource. You usually attach labels to resources when you create them, but you can also add additional labels or even modify the values of existing labels later without having to recreate the resource.

### TRUCO I
Para listar los pods y que aparezca sus labels seria:
> kubectl get pods --show-labels

    NAME        READY   STATUS    RESTARTS   AGE     LABELS
    greet-pod   1/1     Running   0          5m29s   app=greet

### TRUCO II
Para cambiar el label seria:
> kubectl label pod greet-pod creation_method=manual

    > kubectl get pods --show-labels                    

    NAME        READY   STATUS    RESTARTS   AGE    LABELS
    greet-pod   1/1     Running   0          9m3s   app=greet,creation_method=manual

### TRUCO III
Para cambiar el valor de un label seria:
> kubectl label pod greet-pod app=prueba --overwrite


    > kubectl get pods --show-labels

    NAME        READY   STATUS    RESTARTS   AGE   LABELS
    greet-pod   1/1     Running   0          11m   app=prueba,creation_method=manual

### TRUCO IV
Para listar los pods con una determinada llave de label seria:
> kubectl get pods -l app                           

    NAME        READY   STATUS    RESTARTS   AGE
    greet-pod   1/1     Running   0          15m

### TRUCO V
Asimismo, para listar los pods con una terminada llave-valor seria:
> kubectl get pods -l app=prueba
    
    NAME        READY   STATUS    RESTARTS   AGE
    greet-pod   1/1     Running   0          15m

## Recortes de trucos en el CLI

Para escalar un deployment seria
> kubectl scale deploy nombre_deployment --replicas=3

Crear autoescaling cuando el cpu alcanza el 50 porciento
> kubectl scale deploy nombre_deployment --min=2 ,max=5 --cpu-percent=50

Listar los escalados horizontales automaticos
> kubectl get hpa

Para verificar el estado de un despliegue seria
> kubectl rollout status deployments/hello-kubernetes

Para crear un ConfigMap en Kubernetes a partir de un literal seria
> kubectl create ConfigMap my-config --from-literal-MESSAGE="Hello from config file!"
