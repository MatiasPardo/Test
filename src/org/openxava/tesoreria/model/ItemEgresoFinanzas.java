package org.openxava.tesoreria.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.contabilidad.model.CentroCostos;
import org.openxava.negocio.model.*;
import org.openxava.validators.*;

@Entity

@Views({
	@View(members=
			"empresa;" + 
			"concepto, centroCostos;" + 
			"origen;" +
			"tipoValor;" +
			"ChequeTercero[referencia];" + 
			"importeOriginal, cotizacion, importe;" + 
			"detalle;" +
			"Datos[" +
				"numero;" + 
				"fechaEmision, fechaVencimiento;" + 
				"banco;" + 
			"];" + 
			"Firmante[" +
				"firmante;" + 
				"cuitFirmante, nroCuentaFirmante]" + 
			"chequera;")
})


@Tabs({
	@Tab(
		properties="empresa.nombre, egresoFinanzas.fecha, egresoFinanzas.numero, egresoFinanzas.estado, concepto.nombre, tipoValor.nombre, importeOriginal, importe, detalle, numero",
		filter=EmpresaFilter.class,		
		baseCondition=EmpresaFilter.BASECONDITION,
		defaultOrder="${egresoFinanzas.fecha} desc")
})


public class ItemEgresoFinanzas extends ItemEgresoValores{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	private EgresoFinanzas egresoFinanzas;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	private ConceptoTesoreria concepto;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="codigo, nombre", forTabs="ninguno")
	private CentroCostos centroCostos;
	
	public EgresoFinanzas getEgresoFinanzas() {
		return egresoFinanzas;
	}

	public void setEgresoFinanzas(EgresoFinanzas egresoFinanzas) {
		this.egresoFinanzas = egresoFinanzas;
	}

	public ConceptoTesoreria getConcepto() {
		return concepto;
	}

	public void setConcepto(ConceptoTesoreria concepto) {
		this.concepto = concepto;
	}

	@Override
	public void asignarOperadorComercial(Valor valor, Transaccion transaccion) {
	}

	@Override
	public Transaccion transaccion() {
		return egresoFinanzas;
	}
	
	public void recalcular() {
		super.recalcular();
		
		if ((this.getEmpresa() != null) && (this.getEgresoFinanzas() != null)){
			if (!this.getEmpresa().equals(this.getEgresoFinanzas().getEmpresa())){
				throw new ValidationException("No coinciden la empresa del comprobante con los items");
			}
		}
	}
	
	@Override
	public boolean noGenerarDetalle() {
		return false;
	}


	@Override
	public ConceptoTesoreria conceptoTesoreria() {
		return this.getConcepto();
	}
	
	@Override
	public OperadorComercial operadorComercialValores(Transaccion transaccion) {
		return null;
	}

	public CentroCostos getCentroCostos() {
		return centroCostos;
	}

	public void setCentroCostos(CentroCostos centroCostos) {
		this.centroCostos = centroCostos;
	}
}
