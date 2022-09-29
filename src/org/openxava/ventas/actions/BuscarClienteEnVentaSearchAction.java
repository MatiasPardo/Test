package org.openxava.ventas.actions;

import org.openxava.actions.*;
import org.openxava.util.*;
import org.openxava.ventas.model.*;


public class BuscarClienteEnVentaSearchAction extends ReferenceSearchAction{

	@Override
	public void execute() throws Exception {
		super.execute();
		
		Vendedor usuarioVendedor = Vendedor.buscarVendedorUsuario(Users.getCurrent());
		if (usuarioVendedor != null){
			if (!usuarioVendedor.getGerencia()){
				String condition = "${vendedor.id} = '" + usuarioVendedor.getId() + "'";
				this.getTab().setBaseCondition(condition);
			}
		}		
	}
}
