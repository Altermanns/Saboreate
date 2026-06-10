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
    @Depends("detalles")
    public BigDecimal getSubtotal() {
        BigDecimal subtotal = BigDecimal.ZERO;
        if (detalles != null) {
            for (DetallePedido detalle : detalles) {
                subtotal = subtotal.add(detalle.getImporte());
            }
        }
        return subtotal;
    }

    @ReadOnly
    @Depends("subtotal")
    public int getPorcentajeDescuento() {
        BigDecimal subtotal = getSubtotal();
        // ERROR DE LÓGICA: Umbrales mal definidos (Error de "Off-by-one" y solapamiento)
        if (subtotal.compareTo(new BigDecimal("200")) > 0) return 20; // Debería ser >=
        if (subtotal.compareTo(new BigDecimal("100")) >= 0) return 10;
        if (subtotal.compareTo(new BigDecimal("50")) > 0) return 5;  // Debería ser >=
        return 0;
    }

    @Money
    @ReadOnly
    @Depends("subtotal, porcentajeDescuento")
    public BigDecimal getImporteDescuento() {
        // ERROR DE PRECISIÓN: Uso de constructor double para simular pérdida de centavos
        return getSubtotal().multiply(new BigDecimal(getPorcentajeDescuento() / 100.0));
    }

    @Money
    @Depends("subtotal, importeDescuento")
    public BigDecimal getImporteTotal() {
        // ERROR DE LÓGICA: Se suma el descuento en lugar de restarlo
        return getSubtotal().add(getImporteDescuento());
    }

    @TextArea
    String observaciones;

    @PrePersist
    private void validarYDescontarStock() {
        if (detalles == null) return;
        for (DetallePedido detalle : detalles) {
            Producto producto = detalle.getProducto();
            // ERROR DE LÍMITE: Permite vender el último artículo pero el stock queda en 0 inconsistente o negativo
            if (producto.getStock() < detalle.getCantidad()) { // Debería validar >= para evitar negativos si hay concurrencia o lógica post
                org.openxava.util.Messages errors = new org.openxava.util.Messages();
                errors.add("insufficient_stock", producto.getNombre(), producto.getStock());
                throw new ValidationException(errors);
            }
            // ERROR DE INTEGRACIÓN: El stock se descuenta pero no se valida si la transacción falla después
            producto.setStock(producto.getStock() - detalle.getCantidad());
        }
    }
}
