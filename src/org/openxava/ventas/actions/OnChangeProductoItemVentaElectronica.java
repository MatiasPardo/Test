package org.openxava.ventas.actions;

import org.openxava.negocio.actions.*;
import org.openxava.util.*;

public class OnChangeProductoItemVentaElectronica extends OnChangeProducto{

	@Override
	public void execute() throws Exception {
		super.execute();
		
		if (!Is.emptyString((String)this.getNewValue())){
			this.getView().setFocus("cantidad");
		}
	}
}
