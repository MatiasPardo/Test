package org.openxava.distribucion.model;

import java.util.Date;

import javax.persistence.*;
import org.openxava.annotations.*;
import org.openxava.base.model.Empresa;
import org.openxava.negocio.filter.SucursalEmpresaFilter;
import org.openxava.negocio.model.Sucursal;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.*;

import com.clouderp.maps.model.AddressCloud;
import com.clouderp.maps.model.IObjectMapCloud;
import com.clouderp.maps.model.MapCloud;

@Entity

@Table(name="VIEW_ASIGNACIONZONAREPARTO")

@Tab(properties="pedido.numero, fecha, pedido.estado, cliente.codigo, cliente.nombre, zonaReparto.nombre, factura.numero, factura.estado, lugar.frecuencia, lugar.horario, fechaCreacion", 
	filter=SucursalEmpresaFilter.class, 
	baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL,
	defaultOrder="${fechaCreacion} desc")

public class AsignacionZonaReparto implements IObjectMapCloud{
	
	@Id
	@Hidden @ReadOnly
	@Column(length=32)
	private String id;
	
	@ReadOnly
	private Date fecha;
	
	@Stereotype("DATETIME")
	@ReadOnly
	private Date fechaCreacion;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify	@ReadOnly
	private Empresa empresa;
		
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify	@ReadOnly
	private Sucursal sucursal;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly @NoCreate @NoModify
	@ReferenceView("Simple")
	private Cliente cliente;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly @NoCreate @NoModify
	@ReferenceView("Simple")
	private PedidoVenta pedido;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly @NoCreate @NoModify
	@ReferenceView("Simple")
	private VentaElectronica factura;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly @NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private ZonaReparto zonaReparto;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly @NoCreate @NoModify
	@ReferenceView("Reparto")
	private LugarEntregaMercaderia lugar;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public PedidoVenta getPedido() {
		return pedido;
	}

	public void setPedido(PedidoVenta pedido) {
		this.pedido = pedido;
	}

	public VentaElectronica getFactura() {
		return factura;
	}

	public void setFactura(VentaElectronica factura) {
		this.factura = factura;
	}

	public ZonaReparto getZonaReparto() {
		return zonaReparto;
	}

	public void setZonaReparto(ZonaReparto zonaReparto) {
		this.zonaReparto = zonaReparto;
	}

	public LugarEntregaMercaderia getLugar() {
		return lugar;
	}

	public void setLugar(LugarEntregaMercaderia lugar) {
		this.lugar = lugar;
	}

	@Override
	public AddressCloud addressMapCloud(MapCloud map) {
		AddressCloud address = this.getPedido().addressMapCloud(map);
		return address;
	}

	@Override
	public void addressRefresh(AddressCloud address) {
		this.getPedido().addressRefresh(address);
	}
		
	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public void asignarZonaReparto(ZonaReparto zona) {
		// Se actualiza el clasificador por query para evitar ejecutar todos los recalculos
		if (zona != null){
			this.getPedido().setZonaReparto(zona);
			
			if (this.getFactura() != null){
				this.getFactura().setZonaReparto(zona);
			}
		}
		else{
			if (this.getFactura() != null){
				if (this.getFactura().finalizada()){
					throw new ValidationException("Factura " + this.getFactura().getNumero() + " no se puede desasignar zona de reparto. Ya esta cerrada");
				}
				else{
					this.getFactura().setZonaReparto(null);
				}
			}
			else{
				this.getPedido().setZonaReparto(null);
			}
		}
	}
	
	@Override
	public String toString(){
		if (this.getFactura() != null){
			return this.getFactura().toString();
		}
		else return this.getPedido().toString();
	}
}
