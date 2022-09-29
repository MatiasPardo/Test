package org.openxava.contabilidad.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.negocio.model.*;
import org.openxava.view.View;

public class ParametrosEnumerarLibroDiario implements IParametrosReporte{

	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private Empresa empresa;
	
	private Integer primerNumero;
	
	@Override
	public void asignarValoresIniciales(View view, View previousView, Map<?, ?>[] idsSeleccionados) {
		Empresa empresa = Empresa.buscarEmpresaPorNro(1);
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("id", empresa.getId());
		view.setValue("empresa", values);		
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public Integer getPrimerNumero() {
		return primerNumero;
	}

	public void setPrimerNumero(Integer primerNumero) {
		this.primerNumero = primerNumero;
	}
}
