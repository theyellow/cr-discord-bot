# Deploying in kubernetes

For testing, you can use kubernetes easily. Setup of whole deployment is
like this:

![](http://www.plantuml.com/plantuml/proxy?idx=0&src=https://raw.githubusercontent.com/theyellow/cr-discord-bot/main/kubernetes/kubernetes-setup.puml?)

This is the short version for impatient people, for a detailed description,
see [deployment details](deployment.adoc)

Work in kubernetes-directory with kubectl:
```
cd kubernetes
~/cr-discord-bot/kubernetes #
```
---
## Step 1: Generate namespace
```
kubectl apply -f namespace-cr-discord-bot.yaml
```
---
## Step 2: Edit and set (secret) environment variables
```
cp sample-env.list env.list
nano env.list 
kubectl create secret generic cr-discord-bot-secrets --from-env-file ./env.list --namespace=cr-discord-bot
```
---
## Step 3: Deploy on kubernetes - cluster

### Path 1: Apply everything together
```
kubectl apply -f deployment.yaml
```
---
### Path 2: Apply single deployments step-by-step


#### Start Redis
```
kubectl apply -f redis.yaml
```
---
#### Start RabbitMQ - Broker
```
kubectl apply -f rabbitmq-broker.yaml
```
---
#### Start RSocket - middleware
```
kubectl apply -f connect-middleware.yaml
```
---
#### Start distributed worker and leader
```
kubectl apply -f rabbitmq-distributed-worker-leader.yaml
```
---
