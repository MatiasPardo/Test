package org.openxava.ventas.actions;

import org.openxava.base.actions.*;
import org.openxava.model.*;
import org.openxava.ventas.model.*;

public class GrabarPuntoVentaAction extends GrabarObjetoNegocioAction{
	
	@Override
	public void execute() throws Exception {
		super.execute();
		
		if(this.getErrors().isEmpty()){
			PuntoVenta puntoVta = (PuntoVenta)MapFacade.findEntity(this.getModelName(), this.getView().getKeyValues());
			puntoVta.generarNumeradores();
		}
	}	

}
