## Jobs

Jobs allows you to run a pod whose container isn’t restarted when the process running inside finishes successfully. Once it does, the pod is considered complete.

Jobs are useful for ad hoc tasks, where it’s crucial that the task finishes properly. You could run the task in an unmanaged pod and wait for it to finish, but in the event of a node failing or the pod being evicted from the node while it is performing its task, you’d need to manually recreate it. Doing this manually doesn’t make sense—especially if the job takes hours to complete.

Primeramente definimos el servicio
```yaml
apiVersion: v1
kind: Service
metadata:
  name: greet
spec:
  selector:
    app: greet
  ports:
    - protocol: TCP
      port: 80
      targetPort: 8080
  type: NodePort
```

luego definimos el job

```yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: hello-job
spec:
  template:
    metadata:
      name: hello-job
    spec:
      containers:
        - name: hello-container
          image: eduard2diaz/greet:storage
          ports:
            - containerPort: 8080
          #command no es requerido, es valido solo en casos de que ademas de levantar el contenedor, se ejecute un determinado comando
          command: ["echo", "Hello, World!"]
      restartPolicy: Never
  backoffLimit: 4
```

Luego aplicamos los ficheros

> kubectl apply -f job.yaml -f service.yaml

Listamos los jobs y debemos ver el nuestro en dicha lista
> kubectl get jobs
    
    NAME        STATUS     COMPLETIONS   DURATION   AGE
    hello-job   Complete   1/1           3s         2m40s

Vemos que el job se completo satisfactoriamente. Y al listar los pods, vemos que
el pod asociado ya se detuvo

> kubectl get pods                          

    NAME              READY   STATUS      RESTARTS   AGE
    hello-job-bchw6   0/1     Completed   0          7s

Y si revisamos los logs del pod vemos que si se imprimio el comando adicional que indicamos 
> kubectl logs hello-job-bchw6              
    
    Hello, World!

### Corriendo Job Pods secuencialmente
If you need a Job to run more than once, you set completions to how many times you want the Job’s pod to run. The following listing shows an example.

```yaml
#job-sequentially.yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: hello-job
spec:
  completions: 5
  template:
    metadata:
      name: hello-job
    spec:
      containers:
        - name: hello-container
          image: eduard2diaz/greet:storage
          ports:
            - containerPort: 8080
          command: ["echo", "Hello, World!"]
      restartPolicy: Never
  backoffLimit: 4
```
Luego aplicamos nuestros fichero

> kubectl apply -f job-sequentially.yaml -f service.yaml

Y cuando listamos nuestros jobs, vemos que hay un total de 5 pods que deben ejecutarse solo para
este trabajo

> kubectl get jobs

    NAME        STATUS    COMPLETIONS   DURATION   AGE
    hello-job   Running   2/5           6s         6s

Entonces no es que el mismo pod se ejecute 5 veces, sino que seran ejecutados
5 pods, los cuales se ejecutaran secuencialmente, y cada uno ejecutara la misma tarea

> kubectl get jobs
    
    NAME        STATUS    COMPLETIONS   DURATION   AGE
    hello-job   Running   4/5           13s        13s


> kubectl get jobs
    
    NAME        STATUS    COMPLETIONS   DURATION   AGE
    hello-job   Running   4/5           15s        15s

> kubectl get jobs
    
    NAME        STATUS     COMPLETIONS   DURATION   AGE
    hello-job   Complete   5/5           15s        17s

Si listamos los pods veremos los 5 pods que fueron creados
> kubectl get pods
    
    NAME              READY   STATUS      RESTARTS   AGE
    hello-job-4cbxw   0/1     Completed   0          23s
    hello-job-5xvg9   0/1     Completed   0          14s
    hello-job-7sngr   0/1     Completed   0          17s
    hello-job-d2jkn   0/1     Completed   0          20s
    hello-job-nwrdt   0/1     Completed   0          11s

Y si revisamos los logs de cada uno de los pods veremos el comando en terminal que queriamos se ejecutara

> kubectl logs hello-job-4cbxw

    Hello, World!
> kubectl logs hello-job-5xvg9
    
    Hello, World!
> kubectl logs hello-job-7sngr
    
    Hello, World!
> kubectl logs hello-job-d2jkn

    Hello, World!
> kubectl logs hello-job-nwrdt
    
    Hello, World!

### Parallel
Incluso si queremos podemos indicar el numero de pods que se pueden ejecutar en paralelo a la vez

```yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: hello-job
spec:
  completions: 5
  parallelism: 2
  template:
    metadata:
      name: hello-job
    spec:
      containers:
        - name: hello-container
          image: eduard2diaz/greet:storage
          ports:
            - containerPort: 8080
          command: ["echo", "Hello, World!"]
      restartPolicy: Never
  backoffLimit: 4
```

Or, another way you can even change a Job’s parallelism property while the Job is running. This is similar to scaling a ReplicaSet or ReplicationController, and can be done with the kubectl scale command:

> kubectl scale job multi-completion-batch-job --replicas 3

### Cron Resource
Tambien podemos definir jobs cronometrados, por ejemplo
```yaml
#job-cron.yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: hello-job
spec:
  schedule: "0,15,30,45 * * * *" # Corre cada 15 minutos
  jobTemplate:
    metadata:
      labels:
        app: greet
    spec:
      template:
        metadata:
          labels:
            app: greet
        spec:
          containers:
            - name: hello-container
              image: eduard2diaz/greet:storage
              ports:
                - containerPort: 8080
              command: ["echo", "Hello, World!"]
          restartPolicy: Never
```
En este caso dicho cron lo que hace es crear un job cada 15 min de cada hora(primer *),
de cada dia del mes (segundo *), de cada mes (tercer *) y de cada dia de la semana (cuato *).

Primeramente listamos los cronjob, pues si listasemos los job antes de que el primer job sea creado (luego de los primeros 15 min)
, no veremos nada al ejecutar **kubectl get jobs**.

> kubectl get cronjobs
    
    NAME        SCHEDULE             TIMEZONE   SUSPEND   ACTIVE   LAST SCHEDULE   AGE
    hello-job   0,15,30,45 * * * *   <none>     False     0        <none>          101s

Antes de los primeros 15 min al ejecutar los siguentes comandos obtenemos

> kubectl get pods

    No resources found in default namespace.

> kubectl get jobs

    No resources found in default namespace.

> kubectl get pods

    No resources found in default namespace.

Una vez transcurridos los primeros 15 min

> kubectl get pods

    NAME                       READY   STATUS      RESTARTS   AGE
    hello-job-28881000-swxkk   0/1     Completed   0          4m34s

> kubectl logs hello-job-28881000-swxkk

    Hello, World!

> kubectl get jobs                     

    NAME                 STATUS     COMPLETIONS   DURATION   AGE
    hello-job-28881000   Complete   1/1           3s         5m1s