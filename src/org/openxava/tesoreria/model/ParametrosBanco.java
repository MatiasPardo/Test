package org.openxava.tesoreria.model;

import java.util.Map;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.negocio.model.IParametrosReporte;
import org.openxava.view.View;

public class ParametrosBanco implements IParametrosReporte{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate
	@NoModify
	private CuentaBancaria cuentaOrigen;

	public CuentaBancaria getCuentaOrigen() {
		return cuentaOrigen;
	}

	public void setCuentaOrigen(CuentaBancaria cuentaOrigen) {
		this.cuentaOrigen = cuentaOrigen;
	}

	@Override
	public void asignarValoresIniciales(View view, View previousView, Map<?, ?>[] idsSeleccionados) {
	}	
}
