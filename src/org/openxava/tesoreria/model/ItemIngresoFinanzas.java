package org.openxava.tesoreria.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.negocio.model.*;
import org.openxava.validators.*;

@Entity

@Views({
	@View(members=
			"empresa;" + 
			"concepto;" + 
			"destino;" + 
			"tipoValor;" +
			"importeOriginal, cotizacion, importe;" + 
			"detalle;" +
			"Datos[" +
				"numero;" + 
				"fechaEmision, fechaVencimiento;" + 
				"banco;" + 
			"];" + 
			"Firmante[" +
				"firmante;" + 
				"cuitFirmante, nroCuentaFirmante" +
			"]")
})

@Tabs({
	@Tab(
		properties="empresa.nombre, ingresoFinanzas.fecha, ingresoFinanzas.numero, ingresoFinanzas.estado, concepto.nombre, tipoValor.nombre, importeOriginal, importe, detalle, numero",
		filter=EmpresaFilter.class,		
		baseCondition=EmpresaFilter.BASECONDITION,
		defaultOrder="${ingresoFinanzas.fecha} desc")
})


public class ItemIngresoFinanzas extends ItemIngresoValores{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	private ConceptoTesoreria concepto;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	private IngresoFinanzas ingresoFinanzas;
		
	public ConceptoTesoreria getConcepto() {
		return concepto;
	}

	public void setConcepto(ConceptoTesoreria concepto) {
		this.concepto = concepto;
	}

	public IngresoFinanzas getIngresoFinanzas() {
		return ingresoFinanzas;
	}

	public void setIngresoFinanzas(IngresoFinanzas ingresoFinanzas) {
		this.ingresoFinanzas = ingresoFinanzas;
	}

	@Override
	public Transaccion transaccion() {
		return ingresoFinanzas;
	}

	@Override
	public void asignarOperadorComercial(Valor valor, Transaccion transaccion) {		
	}
	
	public void recalcular() {
		super.recalcular();
		
		if ((this.getEmpresa() != null) && (this.getIngresoFinanzas() != null)){
			if (!this.getEmpresa().equals(this.getIngresoFinanzas().getEmpresa())){
				throw new ValidationException("No coinciden la empresa del comprobante con los items");
			}
		}
	}
	
	@Override
	public ConceptoTesoreria conceptoTesoreria() {
		return this.getConcepto();
	}
	
	@Override
	public OperadorComercial operadorComercialValores(Transaccion transaccion) {
		return null;
	}
}
