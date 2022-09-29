package org.openxava.ventas.actions;

import org.openxava.calculators.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.ventas.model.*;

@SuppressWarnings("serial")
public class ConsignacionVentaCalculator implements ICalculator{
	
	private String idCliente;

	public String getIdCliente() {
		return idCliente;
	}

	public void setIdCliente(String idCliente) {
		this.idCliente = idCliente;
	}
	
	private Cliente cliente = null;
	
	private Cliente getCliente(){
		if (this.cliente == null){
			if (!Is.emptyString(this.idCliente)){
				this.cliente = (Cliente)XPersistence.getManager().find(Cliente.class, this.idCliente);
			}	
		}
		return this.cliente;
		
	}

	@Override
	public Object calculate() throws Exception {
		Cliente cliente = this.getCliente();
		if (cliente != null){
			return this.getCliente().getConsignacion();
		}
		else{
			return Boolean.FALSE;
		}
	}
	
	
}
