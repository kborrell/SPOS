package com.udl.tfg.sposapp;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SposAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(SposAppApplication.class, args);
    }

    @Bean
    public TomcatEmbeddedServletContainerFactory containerFactory() {
        return new TomcatEmbeddedServletContainerFactory() {
            protected void customizeConnector(Connector connector) {
                super.customizeConnector(connector);
                if ((connector.getProtocolHandler() instanceof AbstractHttp11Protocol)) {
                    ((AbstractHttp11Protocol) connector.getProtocolHandler()).setMaxSwallowSize(-1);
                }
            }
        };

    }
}
