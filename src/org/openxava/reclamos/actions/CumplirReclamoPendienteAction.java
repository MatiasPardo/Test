package org.openxava.reclamos.actions;

import org.openxava.reclamos.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.model.*;

public class CumplirReclamoPendienteAction extends TabBaseAction implements IChainAction{
	
	private Collection<Reclamo> getSelectedReclamo() throws Exception{
		Collection<Reclamo> result = new ArrayList<Reclamo>();
		for (Map<?, ?> key: getTab().getSelectedKeys()) { 
			Reclamo reclamos = (Reclamo)MapFacade.findEntity("Reclamo", key); 
			result.add(reclamos);
		} 
		return result;
	}
	
	private boolean editarTransaccion = false;
	
	@Override
	public void execute() throws Exception {
		Collection<Reclamo> reclamos = this.getSelectedReclamo();
		int cantidadReclamos = reclamos.size();
		if(!Is.empty(cantidadReclamos)) {
			if(cantidadReclamos == 1) {
				Reclamo miReclamo = reclamos.iterator().next();				
				CumplimientoReclamo cumplimientoReclamo = miReclamo.buscarCumplimientoPendiente();
				if (cumplimientoReclamo == null){
					cumplimientoReclamo = miReclamo.generarCumplimiento();
					this.commit();
				}				
				this.showNewView();
				Map<String, Object> key = new HashMap<String, Object>();
				key.put("id", cumplimientoReclamo.getId());                        
				getView().setModelName(cumplimientoReclamo.getClass().getSimpleName());
				getView().setValues(key);
				getView().findObject();                               
				getView().setKeyEditable(false);
			
				String[] controladores = new String[1];
				controladores[0] = "TransaccionGenerada";                        
				this.setControllers(controladores);
				
				this.editarTransaccion = true;
				this.getTab().deselectAll();
			}
			else throw new ValidationException("selecciono mas de 1 reclamo");
		}
		else throw new ValidationException("Debe seleccionar 1 reclamo");
	}

	@Override
	public String getNextAction() throws Exception {
		if (editarTransaccion){ 
			return "Transaccion.editar";
		}
		else{
			return null;
		}
	}


}

