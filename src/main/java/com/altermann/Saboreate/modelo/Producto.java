package com.altermann.Saboreate.modelo;

import java.math.*;
import javax.persistence.*;
import javax.validation.constraints.Min;

import org.openxava.annotations.*;
import lombok.*;

@Entity
@Getter @Setter
@Tab(properties="codigo, nombre, descripcion, precio, stock")
public class Producto {

    @Id
    @Column(length=10)
    String codigo;

    @Column(length=50)
    @Required
    String nombre;

    @Column(length=100)
    String descripcion;

    @Money
    @Required
    BigDecimal precio;

    @Min(0)
    @Required
    int stock;

}
