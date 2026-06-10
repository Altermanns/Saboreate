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

    public void testAlgoritmoDescuento() throws Exception {
        login("admin", "admin");

        // 1. Crear un producto caro para probar rangos
        changeModule("Producto");
        execute("CRUD.new");
        setValue("codigo", "P_CARO");
        setValue("nombre", "Langosta");
        setValue("precio", "250.00");
        setValue("stock", "10");
        execute("CRUD.save");

        // 2. Probar Rango 1: Subtotal >= 200 (20% descuento)
        changeModule("Pedido");
        execute("CRUD.new");
        execute("Collection.new", "viewObject=xava_view_detalles");
        setValue("producto.codigo", "P_CARO");
        setValue("cantidad", "1");
        execute("Collection.save");
        
        assertValue("subtotal", "250,00");
        assertValue("porcentajeDescuento", "20");
        assertValue("importeDescuento", "50,00");
        assertValue("importeTotal", "200,00");

        // 3. Probar Rango 2: 100 <= Subtotal < 200 (10% descuento)
        execute("Collection.edit", "row=0,viewObject=xava_view_detalles");
        setValue("cantidad", "0"); // Reset or delete, better edit
        execute("Collection.save"); // This might fail if min(1). Let's just create a new one.
        
        execute("CRUD.new");
        execute("Collection.new", "viewObject=xava_view_detalles");
        setValue("producto.codigo", "P_CARO");
        setValue("cantidad", "1");
        // Cambiar precio temporalmente no es fácil, usemos otro producto
        
        changeModule("Producto");
        execute("CRUD.new");
        setValue("codigo", "P_MEDIO");
        setValue("nombre", "Vino");
        setValue("precio", "120.00");
        setValue("stock", "10");
        execute("CRUD.save");

        changeModule("Pedido");
        execute("CRUD.new");
        execute("Collection.new", "viewObject=xava_view_detalles");
        setValue("producto.codigo", "P_MEDIO");
        setValue("cantidad", "1");
        execute("Collection.save");

        assertValue("subtotal", "120,00");
        assertValue("porcentajeDescuento", "10");
        assertValue("importeDescuento", "12,00");
        assertValue("importeTotal", "108,00");

        // 4. Probar Rango 3: 50 <= Subtotal < 100 (5% descuento)
        execute("CRUD.new");
        execute("Collection.new", "viewObject=xava_view_detalles");
        setValue("producto.codigo", "P_CARO"); // Usaremos un producto barato
        
        changeModule("Producto");
        execute("CRUD.new");
        setValue("codigo", "P_BARATO");
        setValue("nombre", "Ensalada");
        setValue("precio", "70.00");
        setValue("stock", "10");
        execute("CRUD.save");

        changeModule("Pedido");
        execute("CRUD.new");
        execute("Collection.new", "viewObject=xava_view_detalles");
        setValue("producto.codigo", "P_BARATO");
        setValue("cantidad", "1");
        execute("Collection.save");

        assertValue("subtotal", "70,00");
        assertValue("porcentajeDescuento", "5");
        assertValue("importeDescuento", "3,50");
        assertValue("importeTotal", "66,50");
    }
}
