package ua.com.lavi.komock.registrar.consul

import com.ecwid.consul.v1.ConsulClient
import com.ecwid.consul.v1.agent.model.NewService
import org.slf4j.LoggerFactory
import ua.com.lavi.komock.model.config.consul.ConsulAgentProperties
import ua.com.lavi.komock.registrar.Registrar

/**
 * Created by Oleksandr Loushkin
 */

class ConsulRegistrar: Registrar<ConsulAgentProperties> {

    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun register(properties: ConsulAgentProperties) {
        val clientRegistrar = ConsulClient(properties.consulHost, properties.consulPort)
        log.debug("Found: ${properties.services.size} consul services")
        for (consulService in properties.services) {
            if (consulService.enabled) {
                val newService = NewService()
                newService.id = consulService.serviceId
                newService.name = consulService.serviceName
                newService.port = consulService.servicePort
                newService.address = consulService.serviceAddress
                val check = NewService.Check()
                check.interval = consulService.checkInterval
                check.timeout = consulService.checkTimeout
                check.tcp = consulService.tcp
                check.http = consulService.http
                check.script = consulService.script
                newService.check = check
                clientRegistrar.agentServiceRegister(newService)
                log.info("Registered consul service: ${consulService.serviceId} - ${consulService.serviceAddress}:${consulService.servicePort}")
            }
        }
        if (properties.daemon) {
            try {
                log.info("Consul registration is running in daemon mode")
                Thread.currentThread().join()
            } catch (e: InterruptedException) {
                log.warn("Error: {}", e)
            }
        }
    }
}
