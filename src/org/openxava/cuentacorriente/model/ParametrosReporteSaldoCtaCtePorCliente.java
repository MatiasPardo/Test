package org.openxava.cuentacorriente.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.negocio.model.*;
import org.openxava.view.View;

public class ParametrosReporteSaldoCtaCtePorCliente implements IParametrosReporte{
	
	private Date desde = null;
	
	private Date hasta = null;
	
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private Empresa empresa;
	
	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public Date getDesde() {
		return desde;
	}

	public void setDesde(Date desde) {
		this.desde = desde;
	}

	public Date getHasta() {
		return hasta;
	}

	public void setHasta(Date hasta) {
		this.hasta = hasta;
	}

	@Override
	public void asignarValoresIniciales(View view, View previousView, Map<?, ?>[] idsSeleccionados) {
		view.setValue("desde", null);
		view.setValue("hasta", null);
	}	
}
