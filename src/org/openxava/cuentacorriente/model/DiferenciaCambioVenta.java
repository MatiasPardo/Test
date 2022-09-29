package org.openxava.cuentacorriente.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

@Entity

@Views({
	@View(members=
			"empresa;" + 
			"cliente;" + 
			"imputacion;" + 
			"DiferenciaCambio[importe, moneda]" )
})

@Tab(
		filter=EmpresaFilter.class,
		properties="fecha, empresa.nombre, cliente.codigo, cliente.nombre, importe, moneda.nombre, imputacion.origen.fecha, imputacion.origen.tipo, imputacion.origen.numero, imputacion.destino.fecha, imputacion.destino.tipo, imputacion.destino.numero, imputacion.numero",
		defaultOrder="${fecha} desc", 
		baseCondition=EmpresaFilter.BASECONDITION + " and " + Pendiente.BASECONDITION)

public class DiferenciaCambioVenta extends Pendiente{

	public static boolean estaPendiente(ImputacionVenta imputacion) {
		Query query = XPersistence.getManager().createQuery("from DiferenciaCambioVenta where imputacion.id = :id and cumplido = :cumplido and anulado = :anulado");
		query.setParameter("cumplido", Boolean.FALSE);
		query.setParameter("anulado", Boolean.FALSE);
		query.setParameter("id", imputacion.getId());
		query.setMaxResults(1);
		query.setFlushMode(FlushModeType.COMMIT);
		return !query.getResultList().isEmpty();		
	}
	
	public DiferenciaCambioVenta(){
		
	}
	
	public DiferenciaCambioVenta(Transaccion transaccion){		
		super(transaccion);
		this.setImputacion((ImputacionVenta)transaccion);		
	}
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	@ReferenceView("Simple")
	private ImputacionVenta imputacion;
		
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@ReadOnly
	private Cliente cliente;
	
	@ReadOnly
	private BigDecimal importe;
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	private Moneda moneda;
	
	public ImputacionVenta getImputacion() {
		return imputacion;
	}

	public void setImputacion(ImputacionVenta imputacion) {
		this.imputacion = imputacion;
		if (imputacion != null){
			this.setEmpresa(imputacion.getEmpresa());
			this.setCliente(imputacion.getCliente());
			this.setImporte(imputacion.getDiferenciaCambio());
			this.setMoneda(imputacion.getMonedaDifCambio());
		}
	}

	public BigDecimal getImporte() {
		return importe == null ? BigDecimal.ZERO : importe;
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
		if (importe != null){
			this.setTipoTrDestino(this.tipoEntidadDestino(this.getImputacion()));
		}
	}

	public Moneda getMoneda() {
		return moneda;
	}

	public void setMoneda(Moneda moneda) {
		this.moneda = moneda;
	}

	@Override
	public Transaccion origen() {
		return this.getImputacion();
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	@Override
	public boolean permiteProcesarJunto(Pendiente pendiente) {
		boolean permite = false;
		DiferenciaCambioVenta difCambio = (DiferenciaCambioVenta)pendiente;
		if (this.getClass().equals(pendiente.getClass())){
			if (difCambio.getImputacion().getCliente().equals(this.getImputacion().getCliente()) && (difCambio.getImputacion().getEmpresa().equals(this.getImputacion().getEmpresa()))){
				if (difCambio.getMoneda().equals(this.getMoneda())){
					if (difCambio.getImporte().signum() == this.getImporte().signum()){
						permite = true;
					}
				}	
			}
		}
		return permite;
	}
	
	@Override
	public String tipoEntidadDestino(Transaccion origen) {
		throw new ValidationException("Falta implementar tipoEntidadDestino");
	}
	
	@Override
	public Transaccion crearTransaccionDestino() {
		throw new ValidationException("Falta implementar crearTransaccionDestino");
	}
	
	public ImputacionVenta generarImputacionDiferenciaCambio(CuentaCorriente ctaCteGeneradaPorDifCambio){
		ImputacionVenta imputacion = new ImputacionVenta();
		imputacion.setFecha(new Date());
		imputacion.setEmpresa(this.getEmpresa());
		imputacion.setCliente(this.getCliente());
		
		if (ctaCteGeneradaPorDifCambio.ingresa()){
			imputacion.setOrigen((CuentaCorrienteVenta)ctaCteGeneradaPorDifCambio);
			imputacion.setDestino(this.getImputacion().getDestino());
		}
		else{
			imputacion.setOrigen(this.getImputacion().getOrigen());
			imputacion.setDestino((CuentaCorrienteVenta)ctaCteGeneradaPorDifCambio);
		}		
		return imputacion;
	}
}
