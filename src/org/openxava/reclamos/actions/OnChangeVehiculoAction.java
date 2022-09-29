package org.openxava.reclamos.actions;

import org.openxava.actions.*;
import org.openxava.jpa.*;
import org.openxava.reclamos.model.*;
import org.openxava.util.*;

public class OnChangeVehiculoAction extends OnChangePropertyBaseAction{

	@Override
	public void execute() throws Exception {
		if (getNewValue() != null){
			String id = (String)getNewValue();
			if (!Is.emptyString(id)){
				Vehiculo v = (Vehiculo)XPersistence.getManager().find(Vehiculo.class, id);
				this.getView().setValue("kilometraje", v.ultimoKilometrajeRegistrado());
			}
		}
	}
}
