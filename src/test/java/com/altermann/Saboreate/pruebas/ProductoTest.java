package com.altermann.Saboreate.pruebas;

import org.openxava.tests.*;

/**
 * Test automatizado para validar el algoritmo de Evaluación de Distribución de Inventario.
 * Cubre los 4 caminos identificados en el análisis de Caja Blanca.
 */
public class ProductoTest extends ModuleTestBase {

    public ProductoTest(String testName) {
        super(testName, "Saboreate", "Producto");
    }

    public void testAlgoritmoDistribucionInventario() throws Exception {
        login("admin", "admin");

        // CAMINO 1: Stock < Mínimo Y Demanda Crítica (> 1.5 * Mínimo)
        // stock=10, min=20, demanda=35 (35 > 20*1.5=30)
        checkDistribucion("10", "20", "35", "ABASTECIMIENTO URGENTE: INCREMENTAR ORDEN");

        // CAMINO 2: Stock < Mínimo Y Demanda Normal
        // stock=10, min=20, demanda=25 (25 < 30)
        checkDistribucion("10", "20", "25", "ABASTECIMIENTO URGENTE: ORDEN ESTÁNDAR");

        // CAMINO 3: Stock >= Mínimo Y Stock >= Demanda (Óptimo)
        // stock=50, min=20, demanda=40
        checkDistribucion("50", "20", "40", "INVENTARIO ÓPTIMO / EN TRÁNSITO");

        // CAMINO 4: Stock >= Mínimo Y Stock < Demanda (Transferencia)
        // stock=30, min=20, demanda=40
        checkDistribucion("30", "20", "40", "SUGERIR TRANSFERENCIA DE INSUMOS");
    }

    private void checkDistribucion(String stock, String min, String demanda, String resultadoEsperado) throws Exception {
        execute("CRUD.new");
        setValue("codigo", "TEST_ALG");
        setValue("nombre", "Producto de Prueba");
        setValue("precio", "10.00");
        setValue("stock", stock);
        setValue("stockMinimo", min);
        setValue("prediccionDemanda", demanda);
        
        // Verificamos el valor calculado por el algoritmo en el campo virtual
        assertValue("analisisInventario", resultadoEsperado);
        
        // Cancelamos para no llenar la base de datos de basura
        execute("CRUD.delete"); 
    }
}
