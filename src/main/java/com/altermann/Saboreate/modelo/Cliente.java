package com.altermann.Saboreate.modelo;

import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.*;

@Entity
@Getter @Setter
@Tab(properties="nombre, telefono, email")
public class Cliente {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Hidden
    int id;

    @Column(length=50)
    @Required
    String nombre;

    @Column(length=20)
    @Required
    String telefono;

    @Column(length=50)
    String email;

    @TextArea
    String observaciones;

}
