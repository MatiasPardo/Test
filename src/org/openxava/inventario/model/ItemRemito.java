package org.openxava.inventario.model;

import java.math.*;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.actions.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

@Entity

@Views({
	@View(members=
		"producto;" +
		"unidadMedida;" +
		"cantidad;" +
		"despacho;" + 
		"lote;"
	),
	@View(name="Liquidacion", members= 
		"producto;" + 
		"unidadMedida;" + 
		"despacho;" +
		"lote;" + 
		"cantidad, pendienteLiquidacion;"),
	@View(name="ControlMercaderia", 
		members="cantidad, pendienteLiquidacion")
})

@Tabs({
	@Tab(properties="remito.fecha, remito.numero, remito.estado, remito.cliente.nombre, remito.cliente.codigo, producto.codigo, producto.nombre, cantidad, remito.porConsignacion, pendienteLiquidacion, facturado"),
	@Tab(name="ItemPendienteLiquidacionConsignacion",
		properties="remito.fecha, remito.numero, remito.cliente.nombre, remito.cliente.codigo, producto.codigo, producto.nombre, cantidad, pendienteLiquidacion, despacho.codigo, lote.codigo",
		baseCondition="${pendienteLiquidacion} > 0 and ${remito.porConsignacion} = 't' and ${remito.estado} = 1",
		defaultOrder="${fechaCreacion} desc")
})

public class ItemRemito extends ItemTransaccion implements IItemMovimientoInventario{
	
	public static List<ItemRemito> buscarItemRemitoPendienteLiquicacion(String codigoCliente, String codigoProducto, String codigoEmpresa, String codigoSucursal) {
		StringBuffer sql = new StringBuffer("from ItemRemito i where ");
		sql.append("i.remito.cliente.codigo = :cliente and i.producto.codigo = :producto and i.pendienteLiquidacion > 0 ");
		sql.append("and i.remito.porConsignacion = :porConsignacion and i.remito.estado = :confirmado ");
		sql.append("and i.remito.empresa.codigo = :empresa and i.remito.sucursal.codigo = :sucursal ");
		sql.append("order by i.remito.fecha asc, i.remito.fechaCreacion asc");
				
		Query query = XPersistence.getManager().createQuery(sql.toString());
		query.setParameter("cliente", codigoCliente);
		query.setParameter("producto", codigoProducto);
		query.setParameter("porConsignacion", Boolean.TRUE);
		query.setParameter("confirmado", Estado.Confirmada);
		query.setParameter("empresa", codigoEmpresa);
		query.setParameter("sucursal", codigoSucursal);
				
		List<?> result = query.getResultList();
		List<ItemRemito> items = new LinkedList<ItemRemito>();
		for(Object o: result){
			items.add((ItemRemito)o);
		}				
		return items; 					
	}
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private Remito remito;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	@OnChange(OnChangeProducto.class)
	private Producto producto;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre",
					depends=UnidadMedida.DEPENDSDESCRIPTIONLIST,
					condition=UnidadMedida.CONDITIONDESCRIPTIONLIST)
	@NoCreate @NoModify
	@OnChange(OnChangeUnidadMedida.class)
	@ReadOnly
	private UnidadMedida unidadMedida;
	
	@Required
	@ReadOnly
	private BigDecimal cantidad;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	private DespachoImportacion despacho;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	private Lote lote;
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private ItemOrdenPreparacion itemOrdenPreparacion;
	
	@ReadOnly
	private Boolean facturado = Boolean.FALSE;
	
	@ReadOnly
	private BigDecimal pendienteLiquidacion = BigDecimal.ZERO;
	
	@Column(length=32)
	@ReadOnly
	@Hidden
	private String idCreadaPor;
	
	@ReadOnly
	@Hidden
	@Column(length=30)
	private String tipoEntidadCreadaPor;
	
	public Remito getRemito() {
		return remito;
	}

	public void setRemito(Remito remito) {
		this.remito = remito;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public UnidadMedida getUnidadMedida() {
		return unidadMedida;
	}

	public void setUnidadMedida(UnidadMedida unidadMedida) {
		this.unidadMedida = unidadMedida;
	}

	public BigDecimal getCantidad() {
		return cantidad == null ? BigDecimal.ZERO : this.cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
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

	public ItemOrdenPreparacion getItemOrdenPreparacion() {
		return itemOrdenPreparacion;
	}

	public void setItemOrdenPreparacion(ItemOrdenPreparacion itemOrdenPreparacion) {
		this.itemOrdenPreparacion = itemOrdenPreparacion;
		if (itemOrdenPreparacion != null){
			this.setIdCreadaPor(itemOrdenPreparacion.getId());
			this.setTipoEntidadCreadaPor(ItemOrdenPreparacion.class.getSimpleName());
		}
		else{
			this.setIdCreadaPor(null);
			this.setTipoEntidadCreadaPor(null);
		}
	}

	public Boolean getFacturado() {
		return facturado;
	}

	public void setFacturado(Boolean facturado) {
		this.facturado = facturado;
	}

	public BigDecimal getPendienteLiquidacion() {
		return pendienteLiquidacion == null ? BigDecimal.ZERO : pendienteLiquidacion;
	}

	public void setPendienteLiquidacion(BigDecimal pendienteLiquidacion) {
		this.pendienteLiquidacion = pendienteLiquidacion;
	}

	@Override
	public Transaccion transaccion() {
		return this.getRemito();		
	}

	@Override
	public void recalcular() {	
	}

	@Override
	public ITipoMovimientoInventario tipoMovimientoInventario(boolean reversion) {
		if (!reversion){
			return new TipoMovInvEgresoDesreserva();
		}
		else{
			return new TipoMovInvIngresoReserva();
		}
	}

	@Override
	public Deposito getDeposito() {
		if (this.getRemito() != null){
			return this.getRemito().getDeposito();
		}
		else{
			return null;
		}
	}

	@Override
	public Cantidad cantidadStock() {
		Cantidad cantidad = new Cantidad();
		cantidad.setUnidadMedida(this.getUnidadMedida());
		cantidad.setCantidad(this.getCantidad().abs());
		return cantidad;
	}

	@Override
	public void actualizarCantidadItem(Cantidad cantidad) {
		if (cantidad.getUnidadMedida().equals(this.getUnidadMedida())){
			// se mantienen el signo de la cantidad original
			BigDecimal cantidadActualizada = cantidad.getCantidad().abs();
			if (this.getCantidad().compareTo(BigDecimal.ZERO) < 0){
				cantidadActualizada = cantidadActualizada.negate();
			}
			this.setCantidad(cantidadActualizada);		
		}
		else{
			throw new ValidationException("Difieren las unidades de medida");
		}		
	}
	
	public IItemPendiente itemPendienteFacturaVentaProxy() {
		ItemPendienteFacturaVentaProxy itemPendiente = new ItemPendienteFacturaVentaProxy();
		itemPendiente.setItemRemito(this);
		return itemPendiente;
	}
	
	public ItemPendienteLiquidacionConsignacionProxy itemPendienteLiquidacionProxy(){
		ItemPendienteLiquidacionConsignacionProxy item = new ItemPendienteLiquidacionConsignacionProxy();
		item.setItemRemito(this);		
		return item;
	}
	
	public void crearItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem){
	}
	
	@Override
	public void posActualizarItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem) {		
	}

	public String getIdCreadaPor() {
		return idCreadaPor;
	}

	public void setIdCreadaPor(String idCreadaPor) {
		this.idCreadaPor = idCreadaPor;
	}

	public String getTipoEntidadCreadaPor() {
		return tipoEntidadCreadaPor;
	}

	public void setTipoEntidadCreadaPor(String tipoEntidadCreadaPor) {
		this.tipoEntidadCreadaPor = tipoEntidadCreadaPor;
	}

	public ObjetoNegocio creadoPor() {
		if (!Is.emptyString(this.getIdCreadaPor())){
			Query query = XPersistence.getManager().createQuery("from " + this.getTipoEntidadCreadaPor() + " where id = :id");
			query.setParameter("id", this.getIdCreadaPor());
			query.setMaxResults(1);
			return (ObjetoNegocio)query.getSingleResult();
		}
		else{
			return null;
		}
	}
	
	public ItemPendienteControlMercaderiaProxy itemPendienteControlMercaderiaProxy(){
		ItemPendienteControlMercaderiaProxy item = new ItemPendienteControlMercaderiaProxy();
		item.setItemRemito(this);		
		return item;
	}
}
