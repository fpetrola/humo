package com.miempresa.api;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {
  public static void main(String[] args) {
    // 1. URL base de tu API
    String baseUrl = "http://localhost:8080/api";

    // 2. Proveedores JAX-RS (Jackson para JSON)
    JacksonJaxbJsonProvider jacksonProv = new JacksonJaxbJsonProvider();

    Map<String, String> headerMap = new HashMap<>();
    headerMap.put("X-XSRF-TOKEN", "dummy_token");
    headerMap.put("Cookie", "XSRF-TOKEN=dummy_token");
    headerMap.put("Content-Type", "application/x-www-form-urlencoded");
    headerMap.put("Accept", "application/json, text/plain, */*");

    headerMap.put("Connection", "keep-alive");
    headerMap.put("Host", "localhost.fr:8080");
    headerMap.put("Referer", "http://localhost:8080/");

    bean.setHeaders(headerMap);

    // 3. Creas el proxy cliente a partir de la interfaz
    PetsApi client = JAXRSClientFactory.create(
        baseUrl,
        PetsApi.class,
        Arrays.asList(jacksonProv)
    );


    // 4. Llamas al método tipado
    List<Pet> pets = Arrays.asList(client.listPets());
    pets.forEach(p ->
        System.out.printf("Mascota id=%d, nombre=%s%n", p.getId(), p.getName())
    );
  }
}
