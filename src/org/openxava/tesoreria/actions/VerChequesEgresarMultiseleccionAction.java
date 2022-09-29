package org.openxava.tesoreria.actions;

import org.openxava.model.*;
import org.openxava.negocio.actions.*;
import org.openxava.tab.*;
import org.openxava.tesoreria.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

public class VerChequesEgresarMultiseleccionAction extends VerListaParaMultiseleccionAction{

	@Override
	protected void armarListadoMultilseleccion(Tab tab) {
		
		EgresoFinanzas egreso = null;
		try{
			egreso = (EgresoFinanzas)MapFacade.findEntity(this.getView().getModelName(), this.getView().getKeyValues());			
		}
		catch(Exception e){
			throw new ValidationException(e.toString());
		}
		if (egreso.getConceptoDefault() == null){
			throw new ValidationException("Debe asignar un concepto default");
		}
		
		tab.setModelName("Valor");
		tab.setTabName("ChequesParaPagar");
		
		String empresaId = this.getView().getValueString("empresa.id");
		if (!Is.emptyString(empresaId)){
			tab.setBaseCondition("${empresa.id} = '" + empresaId + "'");
		}
	}
}

