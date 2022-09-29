package org.openxava.base.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;

public class BuscarEmpresaSearchAction extends ReferenceSearchAction{

	@Override
	public void execute() throws Exception {
		super.execute();
		List<String> idsEmpresa = new LinkedList<String>();
		Empresa.buscarEmpresasHabilitadas(idsEmpresa);
		
		String condition = "${id} in (";
		if (idsEmpresa.isEmpty()){
			condition += "null";
		}
		else{
			boolean first = true;
			for(String id: idsEmpresa){
				if (first){
					first = false;
				}
				else{
					condition += ","; 
				}
				condition += "'" + id + "'";
			}
		}
		condition += ")";
		this.getTab().setBaseCondition(condition);		
	}
	
}
