package com.altermann.Saboreate.modelo;

import java.math.*;
import java.time.*;
import java.util.*;
import javax.persistence.*;
import org.openxava.annotations.*;
import org.openxava.validators.*;
import lombok.*;

@Entity
@Getter @Setter
public class Pedido {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Hidden
    int id;

    @DefaultValueCalculator(org.openxava.calculators.CurrentLocalDateCalculator.class)
    @Required
    LocalDate fecha;

    @ManyToOne(fetch=FetchType.LAZY)
    @DescriptionsList
    Cliente cliente;

    @ManyToOne(fetch=FetchType.LAZY)
    @DescriptionsList(condition="${rol} = 1") // 1 es MESERO si el enum empieza en 0 (ADMIN=0, MESERO=1, CAJERO=2)
    Personal mesero;

    @ElementCollection
    @ListProperties("producto.codigo, producto.nombre, cantidad, importe")
    Collection<DetallePedido> detalles;

    @Money
    public BigDecimal getImporteTotal() {
        BigDecimal total = BigDecimal.ZERO;
        if (detalles != null) {
            for (DetallePedido detalle : detalles) {
                total = total.add(detalle.getImporte());
            }
        }
        return total;
    }

    @TextArea
    String observaciones;

    @PrePersist
    private void validarYDescontarStock() {
        if (detalles == null) return;
        for (DetallePedido detalle : detalles) {
            Producto producto = detalle.getProducto();
            if (producto.getStock() < detalle.getCantidad()) {
                org.openxava.util.Messages errors = new org.openxava.util.Messages();
                errors.add("insufficient_stock", producto.getNombre(), producto.getStock());
                throw new ValidationException(errors);
            }
            producto.setStock(producto.getStock() - detalle.getCantidad());
        }
    }
}
