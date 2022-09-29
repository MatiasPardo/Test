package org.openxava.cuentacorriente.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.cuentacorriente.model.*;
import org.openxava.model.*;



public class ImputarComprobantesSeleccionadasAction extends TabBaseAction{

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void execute() throws Exception {			
		Map [] selectedOnes = getSelectedKeys(); 
		if (selectedOnes != null) {
			List<CuentaCorriente> listadoCtaCte = new LinkedList<CuentaCorriente>();
			for (int i = 0; i < selectedOnes.length; i++) {
				Map clave = selectedOnes[i];
				CuentaCorriente ctacte = (CuentaCorriente)MapFacade.findEntity(this.getTab().getModelName(), clave);
				listadoCtaCte.add(ctacte);
			}
			if (!listadoCtaCte.isEmpty()){
				if (listadoCtaCte.size() > 1){
					List<Imputacion> imputaciones = new LinkedList<Imputacion>();				
					Imputacion.imputarComprobantes(listadoCtaCte, imputaciones);
					this.commit();
					this.addMessage("Operación finalizada");
					
					if (!imputaciones.isEmpty()){						
						for(Imputacion imp: imputaciones){
							this.addMessage(imp.getNumero());
						}
					}
					
					this.getTab().deselectAll();
				}
				else{
					this.addError("Solo ha seleccionado 1 comprobante");
				}				
			}
			else{
				this.addError("No hay comprobantes seleccionados");
			}		
		}
		else{
			this.addError("No hay comprobantes seleccionados");
		}
	}


}
