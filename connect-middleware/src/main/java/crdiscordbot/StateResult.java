package crdiscordbot;

import java.util.List;

/**
 * Represents the result of a state query, containing a list of service results.
 */
public class StateResult {

    private List<ServiceResult> services;

    /**
     * Constructs a new StateResult with the specified list of service results.
     *
     * @param services the list of service results
     */
    public StateResult(List<ServiceResult> services) {
        this.services = services;
    }

    /**
     * Returns the list of service results.
     *
     * @return the list of service results
     */
    public List<ServiceResult> getServices() {
        return services;
    }

    /**
     * Sets the list of service results.
     *
     * @param services the new list of service results
     */
    public void setServices(List<ServiceResult> services) {
        this.services = services;
    }

    /**
     * Represents the result of a single service, containing the service name and state.
     */
    protected static class ServiceResult {
        private String serviceName;
        private String serviceState;

        /**
         * Constructs a new ServiceResult with the specified service name and state.
         *
         * @param serviceName the name of the service
         * @param serviceState the state of the service
         */
        public ServiceResult(String serviceName, String serviceState) {
            this.serviceName = serviceName;
            this.serviceState = serviceState;
        }

        /**
         * Returns the name of the service.
         *
         * @return the name of the service
         */
        public String getServiceName() {
            return serviceName;
        }

        /**
         * Sets the name of the service.
         *
         * @param serviceName the new name of the service
         */
        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        /**
         * Returns the state of the service.
         *
         * @return the state of the service
         */
        public String getServiceState() {
            return serviceState;
        }

        /**
         * Sets the state of the service.
         *
         * @param serviceState the new state of the service
         */
        public void setServiceState(String serviceState) {
            this.serviceState = serviceState;
        }
    }

}