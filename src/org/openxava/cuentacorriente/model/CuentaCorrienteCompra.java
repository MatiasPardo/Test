package org.openxava.cuentacorriente.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.compras.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;

@Entity

@Views({
	@View(members=
		"Principal[#" + 
				"empresa, anulado, pendiente, pendienteDiferenciaCambio;" +
				"fecha, tipo, numero;" + 
				"proveedor];"+
		"importeOriginal, saldoOriginal, monedaOriginal, cotizacion;" +
		"importe1, saldo1;" + 
		"importe2, saldo2;" + 
		"imputaciones;"
				),
	@View(name="Transaccion", members=	 
		"pendiente, pendienteDiferenciaCambio, clasificador, fechaProbable;" +
		"importeOriginal, saldoOriginal, monedaOriginal, cotizacion;" +
		"importe1, saldo1;" + 
		"importe2, saldo2;" + 
		"imputaciones;"
		),
	@View(name="Simple", 
		members="empresa;" + 
				"fecha, numero, tipo;" +
				"proveedor;" + 
				"monedaOriginal, saldoOriginal, importeOriginal;" + 
				"saldo1, saldo2, pendiente"),
	@View(name="CambioClasificador", members="clasificador"),
	@View(name="CambioFechaProbable", members="fechaProbable")
})


@Tab(
		properties="empresa.nombre, fecha, numero, tipo, pendiente, anulado, proveedor.nombre, ingreso, egreso, saldo1, importe1, cotizacion2, importe2, saldo2, monedaOriginal.nombre, importeOriginal, saldoOriginal, cotizacion",
		filter=EmpresaFilter.class,
		baseCondition=EmpresaFilter.BASECONDITION,
		defaultOrder="${fechaCreacion} desc")

public class CuentaCorrienteCompra extends CuentaCorriente{

	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@ReadOnly
	@Required
	private Proveedor proveedor;

	@ReadOnly
	@Hidden
	@Column(length=32)
	private String idPagoProveedores;
	
	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}
	
	public String getIdPagoProveedores() {
		return idPagoProveedores;
	}

	public void setIdPagoProveedores(String idPagoProveedores) {
		this.idPagoProveedores = idPagoProveedores;
	}

	@Override
	public OperadorComercial operadorCtaCte(){
		return this.getProveedor();
	}
	
	@Override
	protected void setOperadorCtaCte(OperadorComercial operador){
		this.setProveedor((Proveedor)operador);
	}
	
	@Override
	public Imputacion crearImputacion(CuentaCorriente origen, CuentaCorriente destino){
		ImputacionCompra imputacion = new ImputacionCompra();
		imputacion.setProveedor(((CuentaCorrienteCompra)origen).getProveedor());		
		imputacion.setOrigen((CuentaCorrienteCompra)origen);
		imputacion.setDestino((CuentaCorrienteCompra)destino);
		return imputacion;
	}
	
	@ReadOnly
	@ListProperties("origen.tipo, origen.numero, estado, destino.tipo, destino.numero, importe, moneda.nombre, diferenciaCambio, monedaDifCambio.nombre")
	public Collection<ImputacionCompra> getImputaciones(){
		Collection<ImputacionCompra> items = new ArrayList<ImputacionCompra>();
		if (!Is.emptyString(this.getId())){
			String sql = "from ImputacionCompra i where ";
			if (this.ingresa()){
				sql += "i.origen.id = :id";
			}
			else{
				sql += "i.destino.id = :id";
			}
			Query query = XPersistence.getManager().createQuery(sql);
			query.setParameter("id", this.getId());
			@SuppressWarnings("unchecked")
			List<ImputacionCompra> result = query.getResultList();
			items.addAll(result);
		}
		return items;
	}
}
