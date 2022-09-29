package org.openxava.base.actions;

import org.openxava.actions.*;
import org.openxava.util.*;
import org.openxava.view.*;

public class BuscarTransicionSearchAction extends ReferenceSearchAction{
	
	@Override
	public void execute() throws Exception {
		super.execute();
		
		View viewParent = getViewInfo().getView().getParent();
		String tipoEntidad = (String)viewParent.getValue("tipoEntidad");
		String condition = "${entidad.entidad} = '" + tipoEntidad + "' and ";
		
		String estadoOrigen = (String)viewParent.getValue("estadoOriginal.id");
		if (estadoOrigen == null) estadoOrigen = "";
		
		if (estadoOrigen.isEmpty()){
			condition += "${origen.id} is null";
		}
		else{
			condition += "${origen.id} = '" + estadoOrigen + "'";
		}
		condition += " and exists (select u.id from UsuarioTransicion u where u.transicion.id = ${id} and u.usuarioHabilitado.name = '" + Users.getCurrent() + "')";
		this.getTab().setBaseCondition(condition);
	}
}
