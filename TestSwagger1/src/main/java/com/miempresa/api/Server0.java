package com.miempresa.api;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;

import java.util.ArrayList;
import java.util.List;

public class Server0 {
    public static void main(String[] args) {
        // 1) Fabrica el servidor JAX-RS
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        
        // 2) Define la URL base donde atenderá
        sf.setAddress("http://localhost:8080/api");
        
        // 3) Registra las clases de implementación
        sf.setResourceClasses(PetsApiImpl.class);
        sf.setResourceProvider(
            PetsApiImpl.class,
            new SingletonResourceProvider(new PetsApiImpl())
        );

        List<Object> providers = new ArrayList<>();
        providers.add(new JacksonJsonProvider());
        sf.setProviders(providers);

////        // 4) Registramos el proveedor JSON (Jackson)
//        sf.setProviders(Arrays.asList(new JacksonJaxbJsonProvider(), new JacksonJsonProvider()));
//

//        BindingFactoryManager manager = sf.getBus().getExtension(BindingFactoryManager.class);
//
//        JAXRSBindingFactory restFactory = new JAXRSBindingFactory();
//        restFactory.setBus(sf.getBus());
//        manager.registerBindingFactory(JAXRSBindingFactory.JAXRS_BINDING_ID, restFactory);

        // 5) Arrancamos el servidor
        sf.create();
        
        System.out.println("Servidor iniciado en http://localhost:8080/api");
    }
}
