package org.openxava.ventas.actions;

import java.math.BigDecimal;
import java.util.*;

import org.openxava.actions.*;
import org.openxava.model.*;
import org.openxava.tesoreria.model.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

public class GenerarReciboCobranzaFacturaContadoAction extends ViewBaseAction implements IChainAction{

	private boolean nuevoRecibo = false;
	
	@Override
	public void execute() throws Exception {
		try{
			FacturaVentaContado factura = (FacturaVentaContado)MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
			nuevoRecibo = false;
			if (factura.getTotalACobrar().compareTo(BigDecimal.ZERO) > 0){
				ReciboCobranza recibo = factura.generarReciboContado();			
				this.commit();
							
				this.showNewView();
				Map<String, Object> key = new HashMap<String, Object>();
				key.put("id", recibo.getId());
				getView().setModelName(recibo.getClass().getSimpleName());
				getView().setViewName("ReciboContado");
				getView().setValues(key);
				getView().findObject();                               
		        getView().setKeyEditable(false);
		        getView().setEditable("moneda", false);
		        
		        String[] controladores = new String[1];
		        controladores[0] = "ReciboFacturaContado";			            
		        this.setControllers(controladores);
	        
		        this.addInfo("Registrar recibo");
		        nuevoRecibo = true;
			}
		}
	    catch(Exception ex){
			this.rollback();
			if (ex instanceof ValidationException){
				addErrors(((ValidationException)ex).getErrors());
			}
			else{
				if (ex.getMessage() != null){
					addError(ex.getMessage());
				}
				else{
					addError(ex.toString());
				}
			}
		}
	}

	@Override
	public String getNextAction() throws Exception {
		if (this.getErrors().isEmpty()){
			if (nuevoRecibo){
				return null;
			}
			else{
				return "Transaccion.editar";
			}
		}
		else{
			return "Transaccion.editar";
		}
	}
}

