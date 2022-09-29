package org.openxava.ventas.actions;

import org.openxava.negocio.actions.*;
import org.openxava.tab.*;

public class VerProductosMultiseleccionAction extends VerListaParaMultiseleccionAction{
	
	@Override
	protected void armarListadoMultilseleccion(Tab tab) {
		tab.setModelName("Producto");
		tab.setTabName("Multiseleccion");
	}
}
