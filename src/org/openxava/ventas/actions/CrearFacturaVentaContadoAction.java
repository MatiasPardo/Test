package org.openxava.ventas.actions;

import java.util.*;

import org.openxava.base.actions.*;
import org.openxava.ventas.model.*;

public class CrearFacturaVentaContadoAction extends CrearTransaccionAction{

	@Override
	public void execute() throws Exception {
		super.execute();
				
		this.getAccionesOcultas().add(FacturaVentaContado.ACCIONGENERARCREDITOCONTADO);
		
		this.getAccionesOcultas().add(FacturaVentaContado.ACCIONREGISTRARCOBRANZA);
		
		this.getAccionesOcultas().add(FacturaVentaContado.ACCIONDEVOLUCIONCONTADO);
		
		this.getAccionesVisibles().add(FacturaVentaContado.ACCIONCONFIRMARCONTADO);
		
		this.asignarClienteConsumidorFinal();
	}
	
	@Override
	public Boolean getNumeroEditable() {
		return Boolean.FALSE;
	}
	
	private void asignarClienteConsumidorFinal(){
		Cliente cliente = Cliente.buscarSinIdentificacion();
		if (cliente != null){
			Map<String, Object> values = new HashMap<String, Object>();
		    values.put("__MODEL_NAME__", Cliente.class.getSimpleName());
		    values.put("id", cliente.getId());
		    values.put("codigo", cliente.getCodigo());
		    this.getView().getSubview("cliente").setValuesNotifying(values);
		}
	}
	
	protected String getNombreVistaAlCrear(){
		return "FacturaVentaContado";
	}
}
