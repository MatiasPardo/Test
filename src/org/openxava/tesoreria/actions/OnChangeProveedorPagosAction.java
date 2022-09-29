package org.openxava.tesoreria.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.compras.model.*;
import org.openxava.jpa.*;
import org.openxava.util.*;

public class OnChangeProveedorPagosAction extends OnChangePropertyBaseAction{
	
	@Override
	public void execute() throws Exception {
		if (!Is.emptyString((String)getNewValue())){
			String idProveedor = (String)getNewValue();
			Proveedor proveedor = (Proveedor)XPersistence.getManager().find(Proveedor.class, idProveedor);
			if (proveedor.getMoneda() != null){
				Map<String, Object> values = new HashMap<String, Object>();
			    values.put("id", proveedor.getMoneda().getId());
			    getView().setValue("moneda", values);
			}
		}
	}
}
