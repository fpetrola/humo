package com.miempresa.api;

import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
public class Server {
   public static void main(String[] args) throws Exception {
      JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
      factory.setResourceClasses(Pet.class);
      factory.setResourceClasses(PetsApi.class);
      factory.setResourceProvider(PetsApi.class,
         new SingletonResourceProvider(new PetsApiImpl()));
      factory.setAddress("http://localhost:8080/api");
      factory.create();
      
      System.out.println("Server ready...");
      Thread.sleep(5 * 60 * 1000);
      
      System.out.println("Server exiting ...");
      System.exit(0);
   }
}