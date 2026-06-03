package com.altermann.Saboreate.pruebas;

import org.openxava.tests.*;

/**
 * Test para validar la lógica de pedidos y stock.
 */
public class PedidoTest extends ModuleTestBase {

    public PedidoTest(String testName) {
        super(testName, "Saboreate", "Pedido");
    }

    public void testValidarStockInsuficiente() throws Exception {
        login("admin", "admin");
        
        // 1. Crear un producto con stock conocido
        changeModule("Producto");
        execute("CRUD.new");
        setValue("codigo", "P1");
        setValue("nombre", "Producto Test");
        setValue("precio", "10.00");
        setValue("stock", "5");
        execute("CRUD.save");
        assertNoErrors();

        // 2. Intentar crear un pedido con cantidad superior al stock
        changeModule("Pedido");
        execute("CRUD.new");
        
        execute("Collection.new", "viewObject=xava_view_detalles");
        setValue("producto.codigo", "P1");
        setValue("cantidad", "10"); // Stock es 5
        execute("Collection.save");
        
        // Debería fallar con el mensaje de error configurado
        assertErrorsCount(1);
        // El mensaje i18n es: Stock insuficiente para {0}. Disponible: {1}
        assertError("Stock insuficiente para Producto Test. Disponible: 5");
    }

    public void testDescontarStockAlCrearPedido() throws Exception {
        login("admin", "admin");
        
        // 1. Crear producto
        changeModule("Producto");
        execute("CRUD.new");
        setValue("codigo", "P2");
        setValue("nombre", "Producto Descuento");
        setValue("precio", "5.00");
        setValue("stock", "100");
        execute("CRUD.save");
        assertNoErrors();

        // 2. Crear pedido
        changeModule("Pedido");
        execute("CRUD.new");
        execute("Collection.new", "viewObject=xava_view_detalles");
        setValue("producto.codigo", "P2");
        setValue("cantidad", "20");
        execute("Collection.save");
        assertNoErrors();
        
        execute("CRUD.save");
        assertNoErrors();

        // 3. Verificar que el stock disminuyó
        changeModule("Producto");
        execute("List.filter");
        execute("List.viewDetail", "row=0"); // Asumiendo que es el primero o filtramos
        assertValue("stock", "80");
    }
}
