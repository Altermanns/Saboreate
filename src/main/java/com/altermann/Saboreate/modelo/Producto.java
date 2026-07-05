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

    int stockMinimo;

    int prediccionDemanda;

    @ReadOnly
    @Depends("stock, stockMinimo, prediccionDemanda")
    public String getAnalisisInventario() {
        if (stock < stockMinimo) {
            if (prediccionDemanda > (stockMinimo * 1.5)) {
                return "ABASTECIMIENTO URGENTE: INCREMENTAR ORDEN";
            } else {
                return "ABASTECIMIENTO URGENTE: ORDEN ESTÁNDAR";
            }
        } else {
            if (stock >= prediccionDemanda) {
                return "INVENTARIO ÓPTIMO / EN TRÁNSITO";
            } else {
                return "SUGERIR TRANSFERENCIA DE INSUMOS";
            }
        }
    }

}

