package com.miempresa.api;

import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.jaxrs.JAXRSBindingFactory;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;

public class Server1 {
    public static org.apache.cxf.endpoint.Server create() {
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(PetsApi.class);
        sf.setResourceProvider(PetsApi.class, new SingletonResourceProvider(new PetsApiImpl()));

        BindingFactoryManager manager = sf.getBus().getExtension(BindingFactoryManager.class);
        JAXRSBindingFactory factory = new JAXRSBindingFactory();
        factory.setBus(sf.getBus());
        manager.registerBindingFactory(JAXRSBindingFactory.JAXRS_BINDING_ID, factory);
//        List<Object> providers = new ArrayList<>();
//        providers.add(new JacksonJaxbJsonProvider());
//        sf.setProviders(providers);
        sf.setAddress("http://localhost:8080/api");
        return sf.create();
    }
    
    public static void main(String[] args) throws Exception {
        var server = create();
        server.start();
    }
}