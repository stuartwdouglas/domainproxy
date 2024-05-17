package io.github.stuartwdouglas.domainproxy;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/")
public class UnsharedProxyEndpoint {


    final Client client;

    final String socket;

    public UnsharedProxyEndpoint(@ConfigProperty(name = "client-domain-socket") String socket) {
        client = ClientBuilder.newBuilder()
                .build();
        this.socket = "unix:/" + socket;
    }

    @GET
    @Path("{path:.*}")
    public Response get(@PathParam("path") String path) {
        return client.target(socket + "/" + path).request().get();
    }

}
