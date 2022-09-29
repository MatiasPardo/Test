package org.openxava.ventas.actions;

import java.util.*;

import org.openxava.util.*;
import org.openxava.ventas.model.*;
import org.openxava.view.*;

public class OnChangeClienteVentaConVendedorAction extends OnChangeClienteVentaAction{
	@Override
	public void execute() throws Exception {
		super.execute();
		
		Cliente cliente = this.getCliente();
		if (cliente != null){
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("id", cliente.getListaPrecio().getId());
			this.getView().setValue("listaPrecio", values);
			
			Vendedor usuarioVendedor = Vendedor.buscarVendedorUsuario(Users.getCurrent());
			if (usuarioVendedor == null){
				// El usuario no es vendedor, al asignar un cliente se pega el vendedor default
				
				View viewVendedor = this.getView().getSubview("vendedor");
				viewVendedor.clear();
				Vendedor vendedor = cliente.getVendedor();
				if (vendedor != null){
					
					
					values = new HashMap<String, Object>();
					values.put("__MODEL_NAME__", viewVendedor.getModelName());
					values.put("id", vendedor.getId());
					values.put("codigo", vendedor.getCodigo());
					values.put("nombre", vendedor.getNombre());
				
					viewVendedor.setValues(values);					
				}
			}
			
			if (cliente.getCondicionVenta() != null){
				values = new HashMap<String, Object>();
				values.put("id", cliente.getCondicionVenta().getId());
				getView().setValue("condicionVenta", values);
			}
		}
	}
}
