package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;
import javax.persistence.Entity;

import org.hibernate.annotations.*;
import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.compras.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.filter.*;
import org.openxava.negocio.model.*;
import org.openxava.ventas.model.*;

@Entity

@Tabs({
	@Tab(		
		properties="fecha, empresa.nombre, tipoComprobante, numeroComprobante, total1, total2, anulado, cliente.nombre, proveedor.nombre, observaciones",
		filter=SucursalDestinoEmpresaFilter.class,
		rowStyles={
				@RowStyle(style="pendiente-ejecutado", property="anulado", value="true")	
		},
		baseCondition=SucursalDestinoEmpresaFilter.BASECONDITION_EMPRESASUCURSALDESTINO,
		defaultOrder="${fecha} desc")	
})	

public class MovimientoFinanciero implements IGeneradoPor{
	
	public static MovimientoFinanciero crearMovimientoFinanciero(MovimientoValores movValores, ITransaccionValores tr){
		MovimientoFinanciero movFinanciero = new MovimientoFinanciero();
		movFinanciero.setEmpresa(movValores.getEmpresa());
		movFinanciero.setFecha(movValores.getFechaComprobante());
		movFinanciero.setIdTransaccion(movValores.getIdTransaccion());
		movFinanciero.setNumeroComprobante(movValores.getNumeroComprobante());
		movFinanciero.setTipoTransaccion(movValores.getTipoTrDestino());
		movFinanciero.setTipoComprobante(movValores.getTipoComprobante());		
		movFinanciero.setAnulado(Boolean.FALSE);
		movFinanciero.setSucursal(tr.getSucursal());
		if (tr.getSucursalDestino() != null){
			movFinanciero.setSucursalDestino(tr.getSucursalDestino());
		}
		else{
			movFinanciero.setSucursalDestino(tr.getSucursal());
		}
		OperadorComercial operador = tr.operadorFinanciero();
		if (operador != null){
			if (operador instanceof Cliente){
				movFinanciero.setCliente((Cliente)operador);
			}
			else if (operador instanceof Proveedor){
				movFinanciero.setProveedor((Proveedor)operador);
			}
		}		
		return movFinanciero;
	}
	
	public static MovimientoFinanciero buscarMovimientoFinanciero(MovimientoValores movValores){
		MovimientoFinanciero movFinanciero = XPersistence.getManager().find(MovimientoFinanciero.class, movValores.getIdTransaccion());
		return movFinanciero;
	}
		
	@Id
	@Column(length=32)
	@Hidden
	private String idTransaccion;
	
	@Hidden
	@Column(length=100)
	@Required
	private String tipoTransaccion;
	
	@ReadOnly
	private Date fecha;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	@ReadOnly
	private Empresa empresa;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@ReadOnly
	private Sucursal sucursal;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@ReadOnly
	private Sucursal sucursalDestino;
	
	@ReadOnly
	@Column(length=100)
	private String tipoComprobante = "";
	
	@Column(length=20)
	@ReadOnly
	private String numeroComprobante = "";
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private Cliente cliente;
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private Proveedor proveedor;
	
	@Stereotype("MEMO")	
	public String getObservaciones(){
		try{
			Query query = XPersistence.getManager().createQuery("from " + this.generadaPorTipoEntidad() + " where id = :id");
			query.setParameter("id", this.generadaPorId());
			Transaccion transaccion = (Transaccion)query.getSingleResult();
			return transaccion.getObservaciones();
		}
		catch(Exception e){
			return null;
		}
	}
	
	@Formula("(select Sum(o.importeMoneda1) from MovimientoValores o where o.idTransaccion = idTransaccion)")
	private BigDecimal total1;
	
	@Formula("(select Sum(o.importeMoneda2) from MovimientoValores o where o.idTransaccion = idTransaccion)")
	private BigDecimal total2;
	
	@ReadOnly
	private Boolean anulado = Boolean.FALSE;
		
	public String getIdTransaccion() {
		return idTransaccion;
	}

	public void setIdTransaccion(String idTransaccion) {
		this.idTransaccion = idTransaccion;
	}

	public String getTipoTransaccion() {
		return tipoTransaccion;
	}

	public void setTipoTransaccion(String tipoTransaccion) {
		this.tipoTransaccion = tipoTransaccion;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public String getTipoComprobante() {
		return tipoComprobante;
	}

	public void setTipoComprobante(String tipoComprobante) {
		this.tipoComprobante = tipoComprobante;
	}

	public String getNumeroComprobante() {
		return numeroComprobante;
	}

	public void setNumeroComprobante(String numeroComprobante) {
		this.numeroComprobante = numeroComprobante;
	}

	public BigDecimal getTotal1() {
		return total1;
	}

	public BigDecimal getTotal2() {
		return total2;
	}

	public Boolean getAnulado() {
		return anulado;
	}

	public void setAnulado(Boolean anulado) {
		this.anulado = anulado;
	}
	
	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}

	@Override
	public String generadaPorId() {
		return this.getIdTransaccion();
	}

	@Override
	public String generadaPorTipoEntidad() {
		return this.getTipoTransaccion();
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public Sucursal getSucursalDestino() {
		return sucursalDestino;
	}

	public void setSucursalDestino(Sucursal sucursalDestino) {
		this.sucursalDestino = sucursalDestino;
	}
}
