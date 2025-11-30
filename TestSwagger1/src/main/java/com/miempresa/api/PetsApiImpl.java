package com.miempresa.api;

import java.util.ArrayList;
import java.util.List;

public class PetsApiImpl implements PetsApi {
    
    // Un repositorio simulado en memoria
    private static final List<Pet> STORE = new ArrayList<>();
    
    static {
        Pet p = new Pet();
        p.setId(1);
        p.setName("Fido");
        STORE.add(p);
    }

    @Override
    public Pet listPets() {
        return STORE.get(0);
    }
    
    // Si tu spec tuviera otros métodos, los implementas igual aquí.
}
