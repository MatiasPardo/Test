package org.openxava.tesoreria.actions;

import org.openxava.negocio.actions.*;
import org.openxava.tab.*;
import org.openxava.util.*;

public class VerChequesDepositarMultiseleccionAction extends VerListaParaMultiseleccionAction{
	
	@Override
	protected void armarListadoMultilseleccion(Tab tab) {
		tab.setModelName("Valor");
		tab.setTabName("ChequesParaDepositar");
		
		String empresaId = this.getView().getValueString("empresa.id");
		if (!Is.emptyString(empresaId)){
			tab.setBaseCondition("${empresa.id} = '" + empresaId + "'");
		}		
	}
}
