package com.miempresa.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Produces("text/xml")
@Path("/pets")
public interface PetsApi {
    @GET
    Pet listPets();
}
