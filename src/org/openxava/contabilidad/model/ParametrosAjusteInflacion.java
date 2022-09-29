package org.openxava.contabilidad.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.base.model.Empresa;
import org.openxava.negocio.model.IParametrosReporte;
import org.openxava.negocio.model.Sucursal;
import org.openxava.view.View;

public class ParametrosAjusteInflacion implements IParametrosReporte{

	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private Empresa empresa;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private Sucursal sucursal;
	
	@Override
	public void asignarValoresIniciales(View view, View previousView, Map<?, ?>[] idsSeleccionados) {
		Empresa empresa = Empresa.buscarEmpresaPorNro(1);
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("id", empresa.getId());
		view.setValue("empresa", values);
		
		Sucursal sucursal = Sucursal.sucursalDefault();
		values = new HashMap<String, Object>();
		values.put("id", sucursal.getId());
		view.setValue("sucursal", values);
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}
}
