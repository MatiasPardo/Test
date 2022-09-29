package org.openxava.ventas.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.util.*;

public class GoAddCuentaCorrienteVentaToTransaccionAction extends GoAddElementsToCollectionAction {
	 	
	public void execute() throws Exception {
        super.execute(); 
        
        String idCliente = this.getPreviousView().getValueString("cliente.id");
        if (Is.emptyString(idCliente)) idCliente = "null";
        
        String idEmpresa = null;
        if (this.getPreviousView().getValue("empresa") != null){
        	idEmpresa = this.getPreviousView().getValueString("empresa.id");
        }
        
        String comprobantesSeleccionados = "";
        if (!this.getPreviousView().isKeyEditable()){
        	ArrayList<?> maps = (ArrayList<?>)getView().getValue("comprobantesPorCobrar");
        	if (maps != null){
        		for(Object values: maps){
        			@SuppressWarnings("unchecked")
					Map<String, Object> map = (Map<String, Object>) values;
        			if (map.containsKey("id")){
        				if (comprobantesSeleccionados != "") comprobantesSeleccionados += ", ";
        				comprobantesSeleccionados += "'" + map.get("id").toString() + "'";
        			}
        		}
        	}        	
        }
        
        String condition = "${pendiente} = 't' and ${cliente.id} = '" + idCliente + 
        		"' and ${tipo} != 'COBRANZA'";
        if (comprobantesSeleccionados != ""){
        	condition += " and ${id} not in (" + comprobantesSeleccionados + ")";
        }
        if (!Is.emptyString(idEmpresa)){
        	condition += " and ${empresa.id} = '" + idEmpresa + "'";
        }
        
        this.getTab().setBaseCondition(condition);
    }

    @Override
	public String getNextController() { 
		return "AgregarComprobantesPorCobrar"; 
	} 
}
