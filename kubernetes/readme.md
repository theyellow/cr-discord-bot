# Work in kubernetes - directory with kubectl
```
~/cr-discord-bot # cd kubernetes
~/cr-discord-bot/kubernetes #
```

Generate namespace
```
~/cr-discord-bot/kubernetes # kubectl apply -f namespace-cr-discord-bot.yaml`
```

Edit and set (secret) environment variables
```
~/cr-discord-bot/kubernetes # cp sample-env.list env.list
~/cr-discord-bot/kubernetes # nano env.list 
~/cr-discord-bot/kubernetes # kubectl create secret generic cr-discord-bot-secrets --from-env-file ./env.list --namespace=cr-discord-bot
```

Start Redis
```
~/cr-discord-bot/kubernetes # kubectl apply -f redis.yaml
```

Start RSocket - middleware
```
~/cr-discord-bot/kubernetes # kubectl apply -f rsocket-middleware.yaml
```

Start RabbitMQ - Broker
```
~/cr-discord-bot/kubernetes # kubectl apply -f rabbitmq-broker.yaml
```

Start distributed worker and leader
```
~/cr-discord-bot/kubernetes # kubectl apply -f rabbitmq-distributed-worker-leader.yaml
```

## Alternatively start all together
```
~/cr-discord-bot/kubernetes # kubectl apply -f deployment.yaml
```
