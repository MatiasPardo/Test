package org.openxava.ventas.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.ventas.model.*;
import org.openxava.view.*;

public class OnChangeClienteVentaAction extends OnChangePropertyBaseAction{
	
	private Cliente cliente = null;
	
	protected Cliente getCliente(){
		return this.cliente;
	}
	
	@Override
	public void execute() throws Exception {
		this.cliente = null;
		if (!Is.emptyString((String)getNewValue())){
			String idCliente = (String)getNewValue();
			this.cliente = (Cliente)XPersistence.getManager().find(Cliente.class, idCliente);
			Domicilio domicilio = this.cliente.domicilioEntregaPrincipal();
			View viewDomicilio = this.getView().getSubview("domicilioEntrega");
			viewDomicilio.clear();
			if (domicilio != null){
				
				Map<String, Object> values = new HashMap<String, Object>();
				values.put("__MODEL_NAME__", viewDomicilio.getModelName());
				values.put("id", domicilio.getId());
				values.put("direccion", domicilio.getDireccion());
				
				Map<String, Object> valuesCiudad = new HashMap<String, Object>();
				valuesCiudad.put("codigoPostal", domicilio.getCiudad().getCodigoPostal());
				valuesCiudad.put("ciudad", domicilio.getCiudad().getCiudad());
				valuesCiudad.put("codigo", domicilio.getCiudad().getCodigo());
				
				Map<String, Object> valuesProvincia = new HashMap<String, Object>();
				valuesProvincia.put("codigo", domicilio.getCiudad().getProvincia().getCodigo());
				valuesProvincia.put("provincia", domicilio.getCiudad().getProvincia().getProvincia());
				valuesCiudad.put("provincia", valuesProvincia);
				
				values.put("ciudad", valuesCiudad);
				
				viewDomicilio.setValues(values);
			}
			
			getView().setValue("porcentajeDescuento", this.getCliente().getPorcentajeDescuento());
			
			// Puede no estar el porcentaje financiero
			getView().trySetValue("porcentajeFinanciero", this.getCliente().getPorcentajeFinanciero());			
		}
	}

}
