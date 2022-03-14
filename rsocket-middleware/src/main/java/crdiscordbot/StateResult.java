package crdiscordbot;

import java.util.List;

public class StateResult {

    private List<ServiceResult> services;

    public StateResult(List<ServiceResult> services) {
        this.services = services;
    }

    public List<ServiceResult> getServices() {
        return services;
    }

    public void setServices(List<ServiceResult> services) {
        this.services = services;
    }

    protected static class ServiceResult {
        private String serviceName;
        private String serviceState;

        public ServiceResult(String serviceName, String serviceState) {
            this.serviceName = serviceName;
            this.serviceState = serviceState;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getServiceState() {
            return serviceState;
        }

        public void setServiceState(String serviceState) {
            this.serviceState = serviceState;
        }
    }

}
