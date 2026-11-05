package com.promou.dominio.descuento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @Column(length = 30)
    private String id;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false)
    private int orden;

    protected Categoria() {
    }

    public Categoria(String id, String nombre, int orden) {
        this.id = id;
        this.nombre = nombre;
        this.orden = orden;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
