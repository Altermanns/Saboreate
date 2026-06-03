package com.altermann.Saboreate.modelo;

import java.math.*;
import javax.persistence.*;
import javax.validation.constraints.Min;

import org.openxava.annotations.*;
import lombok.*;

@Embeddable
@Getter @Setter
public class DetallePedido {

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @DescriptionsList
    Producto producto;

    @Required
    @Min(1)
    int cantidad;

    @Money
    @Depends("producto.precio, cantidad")
    public BigDecimal getImporte() {
        if (producto == null || producto.getPrecio() == null) return BigDecimal.ZERO;
        return producto.getPrecio().multiply(new BigDecimal(cantidad));
    }

}
