package org.openxava.compras.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.util.*;

public class GoAddCuentaCorrienteCompraToTransaccionAction extends GoAddElementsToCollectionAction{
	
    public void execute() throws Exception {
        super.execute(); 
        
        String idProveedor = this.getPreviousView().getValueString("proveedor.id");
        String idEmpresa = this.getPreviousView().getValueString("empresa.id");
        if (Is.emptyString(idProveedor)) idProveedor = "null";
        if (Is.emptyString(idEmpresa)) idEmpresa = "null";
        
        String comprobantesSeleccionados = "";
        if (!this.getPreviousView().isKeyEditable()){
        	ArrayList<?> maps = (ArrayList<?>)getView().getValue("comprobantesPorPagar");
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
        
        String condition = "${pendiente} = 't' and ${proveedor.id} = '" + idProveedor + "' and ${empresa.id} = '" + idEmpresa + "' and ${tipo} != 'PAGO'";
        if (comprobantesSeleccionados != ""){
        	condition += " and ${id} not in (" + comprobantesSeleccionados + ")";
        }
        this.getTab().setBaseCondition(condition);
    }

    @Override
	public String getNextController() { 
		return "AgregarComprobantesPorPagar"; 
	} 
}
