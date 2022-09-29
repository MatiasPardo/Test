package org.openxava.cuentacorriente.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.compras.model.*;
import org.openxava.negocio.model.*;


@Entity

@Views({
	@View(members=
		"Principal[#" + 
				"fechaCreacion, usuario, estado;" + 
				"fecha, numero;" +
				"empresa;" + 
				"proveedor;" +
				"observaciones;" +		
				"origen;" + 
				"destino];" + 
		"Imputacion[#" +
				"importe, monedaImputacion;" +
				"diferenciaCambio, monedaDifCambio];"),
		@View(name="Simple", 
				members="origen; destino; Imputacion[importe, moneda]")
})

@Tabs({
	@Tab(filter=EmpresaFilter.class,
		baseCondition=EmpresaFilter.BASECONDITION,
		properties="empresa.nombre, proveedor.codigo, proveedor.nombre, fecha, numero, origen.tipo, origen.numero, destino.tipo, destino.numero, importe, monedaImputacion.nombre",
		defaultOrder="${fechaCreacion} desc")
})


public class ImputacionCompra extends Imputacion{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView(value="Simple")
	private Proveedor proveedor;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate
	@ReferenceView("Simple")
	private CuentaCorrienteCompra origen;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate
	@ReferenceView("Simple")
	private CuentaCorrienteCompra destino;
	
	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}

	public CuentaCorrienteCompra getOrigen() {
		return origen;
	}

	public void setOrigen(CuentaCorrienteCompra origen) {
		this.origen = origen;
	}

	public CuentaCorrienteCompra getDestino() {
		return destino;
	}

	public void setDestino(CuentaCorrienteCompra destino) {
		this.destino = destino;
	}
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Imputación Compra";
	}

	@Override
	public CuentaCorriente comprobanteOrigen() {
		return this.getOrigen();
	}

	@Override
	public CuentaCorriente comprobanteDestino() {
		return this.getDestino();
	}
	
	@Override
	public OperadorComercial operadorCtaCte(){
		return this.getProveedor();
	}
}
