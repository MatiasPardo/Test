package org.openxava.inventario.model;


import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.EmpresaExterna;
import org.openxava.negocio.filter.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.ventas.model.*;

@Entity

@Tab(properties="fechaCreacion, fechaComprobante, tipoComprobante, numero, producto.codigo, producto.nombre, cantidad, despacho.codigo, lote.codigo, deposito.nombre, usuario",
	defaultOrder="${fechaCreacion} desc",
	filter=DepositoSucursalFilter.class,
	baseCondition=DepositoSucursalFilter.BASECONDITION)

public class Kardex implements IGeneradoPor{
	
	@Id @Hidden
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	@Column(length=32)
	@Hidden
	@Required
	private String idTransaccion;
	
	@Hidden
	@Column(length=100)
	@Required
	private String tipoTrDestino;
	
	@Stereotype("DATETIME")
	private Date fechaCreacion;
	
	private Date fechaComprobante = new Date();
	
	@Column(length=100)
	private String tipoComprobante = "";
	
	@Column(length=20)  
	private String numero = new String("");
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private Deposito deposito;
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private Producto producto;
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private DespachoImportacion despacho;
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private Lote lote;
	
	@ReadOnly
	private BigDecimal cantidad = BigDecimal.ZERO;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@ReadOnly
	private UnidadMedida unidadMedida;
	
	@ReadOnly
	private BigDecimal cantidadOperacion = BigDecimal.ZERO;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@ReadOnly
	private UnidadMedida unidadMedidaOperacion;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly
	@NoCreate @NoModify 
	@ReferenceView("Simple")
	private EmpresaExterna clienteProveedor;
	
	@Column(length=30)
	@ReadOnly
	private String usuario;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public Date getFechaComprobante() {
		return fechaComprobante;
	}

	public void setFechaComprobante(Date fechaComprobante) {
		this.fechaComprobante = fechaComprobante;
	}

	public String getTipoComprobante() {
		return tipoComprobante;
	}

	public void setTipoComprobante(String tipoComprobante) {
		this.tipoComprobante = tipoComprobante;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public Deposito getDeposito() {
		return deposito;
	}

	public void setDeposito(Deposito deposito) {
		this.deposito = deposito;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
		if (producto != null){
			this.setUnidadMedida(producto.getUnidadMedida());
		}
	}

	public BigDecimal getCantidad() {
		return cantidad == null ? BigDecimal.ZERO : this.cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}

	public String getIdTransaccion() {
		return idTransaccion;
	}

	public void setIdTransaccion(String idTransaccion) {
		this.idTransaccion = idTransaccion;
	}

	public String getTipoTrDestino() {
		return tipoTrDestino;
	}

	public void setTipoTrDestino(String tipoTrDestino) {
		this.tipoTrDestino = tipoTrDestino;
	}

	public DespachoImportacion getDespacho() {
		return despacho;
	}

	public void setDespacho(DespachoImportacion despacho) {
		this.despacho = despacho;
	}

	public Lote getLote() {
		return lote;
	}

	public void setLote(Lote lote) {
		this.lote = lote;
	}

	public UnidadMedida getUnidadMedidaOperacion() {
		return unidadMedidaOperacion;
	}

	public void setUnidadMedidaOperacion(UnidadMedida unidadMedidaOperacion) {
		this.unidadMedidaOperacion = unidadMedidaOperacion;
	}

	public BigDecimal getCantidadOperacion() {
		return cantidadOperacion;
	}

	public void setCantidadOperacion(BigDecimal cantidadOperacion) {
		this.cantidadOperacion = cantidadOperacion;
	}

	public UnidadMedida getUnidadMedida() {
		return unidadMedida;
	}

	public void setUnidadMedida(UnidadMedida unidadMedida) {
		this.unidadMedida = unidadMedida;
	}

	public void actualizarCantidad(Cantidad cantidad){
		BigDecimal cantidadConvertida = cantidad.convertir(this.getUnidadMedida());
		this.setCantidadOperacion(cantidad.getCantidad());
		this.setUnidadMedidaOperacion(cantidad.getUnidadMedida());
		this.setCantidad(cantidadConvertida);
	}
	
	@PrePersist
	private void onPrePersist(){
		this.setFechaCreacion(new Date());
		this.setUsuario(Users.getCurrent());
	}

	@Override
	public String generadaPorId() {		
		return this.getIdTransaccion();
	}

	@Override
	public String generadaPorTipoEntidad() {
		return this.getTipoTrDestino();
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public EmpresaExterna getClienteProveedor() {
		return clienteProveedor;
	}

	public void setClienteProveedor(EmpresaExterna clienteProveedor) {
		this.clienteProveedor = clienteProveedor;
	}
}
