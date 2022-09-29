package org.openxava.inventario.actions;

import org.openxava.negocio.actions.VerListaParaMultiseleccionAction;
import org.openxava.tab.Tab;

public class VerTransferenciasDepositosMultiseleccionAction extends VerListaParaMultiseleccionAction{

	@Override
	protected void armarListadoMultilseleccion(Tab tab) {
		tab.setModelName("Producto");
		tab.setTabName("Multiseleccion");
		tab.setBaseCondition("${tipo} = 0");
	}

}
