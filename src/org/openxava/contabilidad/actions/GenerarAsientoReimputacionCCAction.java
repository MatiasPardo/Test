package org.openxava.contabilidad.actions;

import java.util.HashMap;
import java.util.Map;

import org.openxava.actions.IChainAction;
import org.openxava.actions.ViewBaseAction;
import org.openxava.contabilidad.model.Asiento;
import org.openxava.model.MapFacade;

public class GenerarAsientoReimputacionCCAction extends ViewBaseAction implements IChainAction{

	private boolean editarReimputacion = false;
	
	@Override
	public void execute() throws Exception {
		this.editarReimputacion = false;
		if (!this.getView().isKeyEditable()){
			Asiento asiento = (Asiento)MapFacade.findEntity(this.getView().getModelName(), this.getView().getKeyValues());
			Asiento reimputacion = asiento.generarAsientoParaReimputarCentroCostos();
			
			this.commit();
			
			this.showNewView();
			Map<String, Object> key = new HashMap<String, Object>();
			key.put("id", reimputacion.getId());
			getView().setModelName("Asiento");
			getView().setValues(key);
			getView().findObject();                               
            getView().setKeyEditable(false);
   
            String[] controladores = new String[1];
            controladores[0] = "TransaccionGenerada";			            
            this.setControllers(controladores);
            
            this.editarReimputacion = true;
		}
		else{
			this.addError("primero_grabar");
		}
	}

	@Override
	public String getNextAction() throws Exception {
		if (this.editarReimputacion){
			return "Transaccion.editar";
		}
		else{
			return null;
		}
	}

}
