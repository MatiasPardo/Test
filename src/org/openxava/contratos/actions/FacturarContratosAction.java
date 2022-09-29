package org.openxava.contratos.actions;

import java.util.LinkedList;
import java.util.List;

import org.openxava.actions.TabBaseAction;
import org.openxava.contratos.model.Contrato;
import org.openxava.jpa.XPersistence;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.VentaElectronica;

public class FacturarContratosAction extends TabBaseAction{

	@Override
	public void execute() throws Exception {
		List<String> ids = new LinkedList<String>();
		Contrato.buscarTodosContratosParaFacturar(ids);
		if (!ids.isEmpty()){
			int facturasGeneradas = 0;
			int contratosError = 0;
			for(String id: ids){
				Contrato contrato = XPersistence.getManager().find(Contrato.class, id);
				try{					
					List<VentaElectronica> facturas = new LinkedList<VentaElectronica>();
					contrato.facturar(facturas);
					if (!facturas.isEmpty()){
						this.commit();
						facturasGeneradas += facturas.size();
					}
				}
				catch(ValidationException e){
					this.addErrors(e.getErrors());
					contratosError++;
					this.rollback();
				}
				catch(Exception e){
					this.addError(contrato.toString() + " error: " + e.toString() );
					contratosError++;
					this.rollback();
				}
			}
			if (facturasGeneradas > 0){
				this.addMessage("Facturas generadas " + Integer.toString(facturasGeneradas));
			}
			if (contratosError > 0){
				this.addError("Contratos con error " + Integer.toString(contratosError));
			}
			
			if (this.getMessages().isEmpty() && this.getErrors().isEmpty()){
				this.addMessage("No hay contratos pendientes");
			}
		}
		else{
			this.addError("No hay contratos para facturar");
		}			
	}

}
