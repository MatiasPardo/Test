package org.openxava.inventario.validators;

import org.openxava.inventario.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

@SuppressWarnings("serial")
public class DepositoPorConsignacionAsignadoValidator implements IValidator{
	
	private Boolean porConsignacion;
	
	private Deposito depositoPorConsignacion;
	
	public Boolean getPorConsignacion() {
		return porConsignacion == null ? Boolean.FALSE : this.porConsignacion;
	}
	
	public void setPorConsignacion(Boolean porConsignacion) {
		this.porConsignacion = porConsignacion;
	}

	public Deposito getDepositoPorConsignacion() {
		return depositoPorConsignacion;
	}

	public void setDepositoPorConsignacion(Deposito depositoPorConsignacion) {
		this.depositoPorConsignacion = depositoPorConsignacion;
	}

	@Override
	public void validate(Messages errors) throws Exception {
		if (this.getPorConsignacion()){
			if (this.getDepositoPorConsignacion() == null){
				errors.add("Falta asignar el depósito por consignación");
			}
		}		
	}

}
