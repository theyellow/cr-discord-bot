## Kubernetes Deployment Overview

#### Directory Structure
The `kubernetes` directory contains the necessary files for deploying the CR Discord Bot on a Kubernetes cluster. The key components include:

1. **Deployment Files**: YAML files that define the deployment configurations for each service.
2. **Service Files**: YAML files that define the services to expose the deployments.
3. **ConfigMaps and Secrets**: YAML files for configuration data and sensitive information.

### Key Components

#### Deployments
Each Maven subproject (`common`, `connect`, `connect-middleware`, `leader`, `worker`) has its own deployment configuration. These deployments specify the Docker images to use, the number of replicas, and other deployment-specific settings.

- **common**: Shared utilities and configurations.
  - [common-deployment.yaml](common-deployment.yaml)
- **connect**: Handles the connection to the Discord API.
  - [connect-deployment.yaml](connect-deployment.yaml)
- **connect-middleware**: Coordinates communication between `leader` and `worker`.
  - [connect-middleware-deployment.yaml](connect-middleware-deployment.yaml)
- **leader**: Manages and coordinates the `worker`.
  - [leader-deployment.yaml](leader-deployment.yaml)
- **worker**: Executes tasks delegated by the `leader`.
  - [worker-deployment.yaml](worker-deployment.yaml)

#### Services
Services are defined to expose the deployments and enable communication between them. Each service corresponds to a deployment and specifies how to access the pods.

- **connect-service**: Exposes the `connect` deployment.
  - [connect-service.yaml](connect-service.yaml)
- **middleware-service**: Exposes the `connect-middleware` deployment.
  - [middleware-service.yaml](middleware-service.yaml)
- **leader-service**: Exposes the `leader` deployment.
  - [leader-service.yaml](leader-service.yaml)
- **worker-service**: Exposes the `worker` deployment.
  - [worker-service.yaml](worker-service.yam)

#### ConfigMaps and Secrets
ConfigMaps and Secrets are used to manage configuration data and sensitive information, such as API keys and environment variables.

- **ConfigMaps**: Store non-sensitive configuration data.
  - [configmap.yaml](configmap.yaml)
- **Secrets**: Store sensitive information securely.
  - [secrets.yaml](secrets.yaml)

### Deployment Steps
1. **Build Docker Images**: Use GitHub Actions or another CI/CD pipeline to build Docker images for each subproject and push them to a Docker registry.
2. **Apply Kubernetes Configurations**: Use `kubectl apply -f` to apply the YAML files in the `kubernetes` directory to your Kubernetes cluster.
3. **Monitor Deployments**: Use `kubectl get pods` and `kubectl get services` to monitor the status of your deployments and services.

### Summary
The Kubernetes deployment for the CR Discord Bot involves defining deployments, services, ConfigMaps, and Secrets for each subproject. The deployment files specify how to build and deploy the bot's components, ensuring they can communicate effectively within the Kubernetes cluster. This setup allows for scalable and manageable deployment of the bot.
