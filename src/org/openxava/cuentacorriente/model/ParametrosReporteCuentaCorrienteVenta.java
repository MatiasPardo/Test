package org.openxava.cuentacorriente.model;

import java.util.Map;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.calculators.*;
import org.openxava.base.model.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.ventas.model.*;
import org.openxava.view.View;

public class ParametrosReporteCuentaCorrienteVenta implements IParametrosReporte{
	
	
	@ReferenceView("Simple")
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@NoCreate @NoModify
	private Vendedor vendedor;
		
	public Vendedor getVendedor() {
		return vendedor;
	}

	public void setVendedor(Vendedor vendedor) {
		this.vendedor = vendedor;
	}

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

	@Override
	public void asignarValoresIniciales(View view, View previousView, Map<?, ?>[] idsSeleccionados) {		
		try{
			EmpresaDefaultCalculator calculator = new EmpresaDefaultCalculator();
			Empresa emp = (Empresa)calculator.calculate();
			if (emp != null){
				view.setValue("empresa.id", emp.getId());
			}
			
			Vendedor vendedor = Vendedor.buscarVendedorUsuario(Users.getCurrent());
			if (vendedor != null){
				if (!vendedor.getGerencia()){
					view.setValue("vendedor.id", vendedor.getId());
				}
			}
		}
		catch(Exception e){
		}
	}
}
