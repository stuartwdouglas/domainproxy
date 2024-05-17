package io.github.stuartwdouglas.domainproxy;

import io.quarkus.logging.Log;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/")
public class ExternalProxyEndpoint {

    final Client client;
    final Map<String, List<String>> proxyTargets;

    public ExternalProxyEndpoint(Config config,
                                 @ConfigProperty(name = "proxy-paths") List<String> endpoints) {
        client = ClientBuilder.newBuilder()
                .build();
        Map<String, List<String>> targets = new HashMap<>();
        for (var endpoint : endpoints) {
            var proxyTarget = config.getConfigValue("proxy-path." + endpoint + ".targets").getValue().split(",");
            targets.put(endpoint, List.of(proxyTarget));
        }
        this.proxyTargets = targets;
    }

    @GET
    @Path("{root}/{path:.*}")
    public InputStream get(@PathParam("root") String root, @PathParam("path") String path) {
        var targets = proxyTargets.get(root);
        if (targets == null) {
            throw new NotFoundException();
        }
        for (var target : targets) {
            var response = client.target(target + "/" + path).request().get();
            if (response.getStatus() != 200) {
                Log.errorf("Response %s %s", response.getStatus(), response.readEntity(String.class));
                continue;
            }
            return response.readEntity(InputStream.class);
        }
        throw new NotFoundException();
    }


}
