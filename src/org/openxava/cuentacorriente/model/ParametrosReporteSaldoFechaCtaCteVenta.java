package org.openxava.cuentacorriente.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.calculators.*;
import org.openxava.base.model.*;
import org.openxava.negocio.model.*;
import org.openxava.view.View;

public class ParametrosReporteSaldoFechaCtaCteVenta implements IParametrosReporte{
	
	private Date fecha;
	
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DefaultValueCalculator(value=EmpresaDefaultCalculator.class)
	private Empresa empresa;
	
	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	@Override
	public void asignarValoresIniciales(View view, View previousView, Map<?, ?>[] idsSeleccionados) {		
		try{
			EmpresaDefaultCalculator calculator = new EmpresaDefaultCalculator();
			Empresa emp = (Empresa)calculator.calculate();
			if (emp != null){
				view.setValue("empresa.id", emp.getId());
			}
		}
		catch(Exception e){
		}
	}
}
