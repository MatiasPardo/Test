package org.openxava.base.model;

import org.openxava.view.*;

public class ItemTransaccionView {
	
	public void copiarValoresItemsPrevioGrabar(View item){		
	}
	
	public void copiarValoresCabecera(View cabecera, View items, ObjetoNegocio cabeceraPosCommit) {
		if ((cabecera.getValue("empresa") != null) && (items.getValue("empresa") != null)){
			items.setValue("empresa.id", cabecera.getValue("empresa.id"));
		}
	}
}
