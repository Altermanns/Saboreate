package com.altermann.Saboreate.modelo;

import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.*;

@Entity
@Getter @Setter
public class Personal {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Hidden
    int id;

    @Column(length=50)
    @Required
    String nombre;

    public enum Rol { ADMIN, MESERO, CAJERO }

    @Required
    Rol rol;

}
