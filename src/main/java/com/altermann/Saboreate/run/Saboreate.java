package com.altermann.Saboreate.run;

import org.openxava.util.*;

/**
 * Ejecuta esta clase para arrancar la aplicación.
 */

public class Saboreate {

	public static void main(String[] args) throws Exception {
		DBServer.start("Saboreate-db"); // Para usar tu propia base de datos comenta esta línea y configura src/main/webapp/META-INF/context.xml
		AppServer.run("Saboreate"); // Usa AppServer.run("") para funcionar en el contexto raíz
	}

}
