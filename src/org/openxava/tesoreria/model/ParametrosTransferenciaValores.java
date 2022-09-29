package org.openxava.tesoreria.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.negocio.model.*;

public class ParametrosTransferenciaValores {
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	private Sucursal sucursal;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(
			descriptionProperties="nombre", 
			depends="this.sucursal",
			condition="${sucursal.id} = ?")
	private Tesoreria tesoreria;

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public Tesoreria getTesoreria() {
		return tesoreria;
	}

	public void setTesoreria(Tesoreria tesoreria) {
		this.tesoreria = tesoreria;
	}
}
