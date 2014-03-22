package com.mowforth.netty.util.handlers.rest.jersey;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.spi.ContainerProvider;

import javax.ws.rs.ext.Provider;

@Provider
public class NettyContainerProvider implements ContainerProvider {

    @Override
    public <T> T createContainer(Class<T> type, ApplicationHandler applicationHandler) {
        if (type != NettyContainer.class) {
            return null;
        }
        return type.cast(new NettyContainer(applicationHandler));
    }
}