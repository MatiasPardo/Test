package org.openxava.inventario.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.openxava.annotations.Hidden;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.RowStyle;
import org.openxava.annotations.Tab;
import org.openxava.base.model.Empresa;
import org.openxava.base.model.IItemPendiente;
import org.openxava.base.model.ObjetoNegocio;
import org.openxava.base.model.Pendiente;
import org.openxava.jpa.XPersistence;
import org.openxava.negocio.filter.SucursalEmpresaFilter;
import org.openxava.negocio.model.IGeneradoPor;
import org.openxava.negocio.model.Sucursal;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.Cliente;
import org.openxava.ventas.model.EstadisticaPedidoVenta;
import org.openxava.ventas.model.Producto;

@Entity

@Table(name="VIEW_PENDIENTEORDENPREPARACIONITEMS")

@Tab(properties="fecha, tipoComprobante, numero, producto.codigo, producto.nombre, cantidadPendiente, cantidadOriginal, cliente.codigo, ejecutado", 
	defaultOrder="${fechaCreacion} desc", 
	filter=SucursalEmpresaFilter.class,
	baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL,
	rowStyles={
		@RowStyle(style="pendiente-ejecutado", property="ejecutado", value="true")	
	})

public class PendienteOrdenPreparacionItems implements IGeneradoPor, IItemPendiente{
	
	@Id
	@Hidden
	@Column(length=32)
	private String id;
	
	@Hidden
	@Column(length=100)
	private String tipoEntidad;
	
	@Column(length=32)
	@Hidden
	private String idTr;
	
	@Column(length=100)
	@Hidden
	private String tipoTr;
	
	@Column(length=50)
	@ReadOnly
	private String tipoComprobante;
	
	@Column(length=20)
	@ReadOnly
	private String numero;
	
	@ReadOnly
	private Date fecha;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView(value="Simple")
	@NoCreate @NoModify @ReadOnly
	private Producto producto;
	
	@ReadOnly
	private BigDecimal cantidadPendiente;
	
	@ReadOnly
	private BigDecimal cantidadOriginal;
	
	@ReadOnly
	private Date fechaCreacion;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView(value="Simple")
	@NoCreate @NoModify @ReadOnly
	private Empresa empresa;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView(value="Simple")
	@NoCreate @NoModify @ReadOnly
	private Sucursal sucursal;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView(value="Simple")
	@NoCreate @NoModify @ReadOnly
	private Cliente cliente;
	
	@ReadOnly
	private Boolean ejecutado;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public BigDecimal getCantidadPendiente() {
		return cantidadPendiente;
	}

	public void setCantidadPendiente(BigDecimal cantidadPendiente) {
		this.cantidadPendiente = cantidadPendiente;
	}

	public BigDecimal getCantidadOriginal() {
		return cantidadOriginal;
	}

	public void setCantidadOriginal(BigDecimal cantidadOriginal) {
		this.cantidadOriginal = cantidadOriginal;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
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

	public String getIdTr() {
		return idTr;
	}

	public void setIdTr(String idTr) {
		this.idTr = idTr;
	}

	public String getTipoTr() {
		return tipoTr;
	}

	public void setTipoTr(String tipoTr) {
		this.tipoTr = tipoTr;
	}

	public String getTipoEntidad() {
		return tipoEntidad;
	}

	public void setTipoEntidad(String tipoEntidad) {
		this.tipoEntidad = tipoEntidad;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
	
	@Override
	public String generadaPorId() {
		return this.getIdTr();
	}

	@Override
	public String generadaPorTipoEntidad() {
		return this.getTipoTr();
	}

	@Transient
	private IItemPendiente itemPendienteAsociado = null;
	
	private IItemPendiente getItemPendienteAsociado(){
		if (this.itemPendienteAsociado == null){
			Object object = this.getItem();
			
			if (object instanceof EstadisticaPedidoVenta){
				this.itemPendienteAsociado = ((EstadisticaPedidoVenta)object).itemPendienteOrdenPreparacionProxy();
			}
			else if (object instanceof ItemSolicitudMercaderia){
				this.itemPendienteAsociado = ((ItemSolicitudMercaderia)object).itemPendienteOrdenPreparacionProxy();
			}
			else{
				throw new ValidationException("Falta implementar el método ItemPedidoAsociado para el tipo de clase " + object.getClass().getSimpleName());
			}
		}
		return this.itemPendienteAsociado;
	}

	@Override
	public Boolean cumplido() {
		return this.getItemPendienteAsociado().cumplido();
	}

	@Override
	public void cancelarPendiente() throws ValidationException {
		this.getItemPendienteAsociado().cancelarPendiente();		
	}

	@Override
	public void liberar() {
		this.getItemPendienteAsociado().liberar();		
	}

	public Boolean getEjecutado() {
		return ejecutado;
	}

	public void setEjecutado(Boolean ejecutado) {
		this.ejecutado = ejecutado;
	}

	@Override
	public Pendiente getPendiente() {
		return this.getItemPendienteAsociado().getPendiente();		
	}

	@Transient
	private ObjetoNegocio obj = null;
	
	@Override
	public ObjetoNegocio getItem() {
		if (obj == null){
			Query query = XPersistence.getManager().createQuery("from " + this.getTipoEntidad() + " where id = :id");
			query.setParameter("id", this.getId());
			query.setMaxResults(1);
			this.obj = (ObjetoNegocio)query.getSingleResult();
		}
		return this.obj;
	}
}
