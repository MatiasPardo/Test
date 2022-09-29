package org.openxava.mercadolibre.model;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;

import org.openxava.annotations.AsEmbedded;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.Required;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;
import org.openxava.base.model.ItemTransaccion;
import org.openxava.base.model.Transaccion;
import org.openxava.inventario.model.Deposito;
import org.openxava.inventario.model.DespachoImportacion;
import org.openxava.inventario.model.IItemMovimientoInventario;
import org.openxava.inventario.model.ITipoMovimientoInventario;
import org.openxava.inventario.model.Lote;
import org.openxava.inventario.model.TipoMovInvDesreserva;
import org.openxava.inventario.model.TipoMovInvReserva;
import org.openxava.negocio.model.Cantidad;
import org.openxava.negocio.model.UnidadMedida;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.Producto;

@Entity

@Tab(properties="pedido.fecha, pedido.estado, pedido.factura.subestado.nombre, pedido.numero, publicacion.idMercadoLibre, producto.codigo, precioUnitario, cantidad, total, producto.nombre, numeroPedidoCarrito, pedido.fechaHora, pedido.idComprador, pedido.nombreComprador, pedido.factura.numero",
	defaultOrder="${pedido.fecha} desc, ${pedido.fechaCreacion} desc"
)

@Views({
	@View(members="pedido;" +
		"Principal[" +
			"numeroPedidoCarrito;" +
			"publicacion;" +
			"producto;" +
			"cantidad, precioUnitario, porcentajeDescuento;" +
			"total, precioOriginal;" + 
		"];" +
		"despacho;" + 
		"lote;"
	),		
	@View(name="Simple", members="publicacion")
})


public class ItemPedidoML extends ItemTransaccion implements IItemMovimientoInventario{

	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("ItemPedidoML")	
	private PedidoML pedido;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	@AsEmbedded
	@ReadOnly
	private PublicacionML publicacion;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	@AsEmbedded
	@ReadOnly
	private Producto producto;
	
	@Required
	@Min(value=0, message="No puede ser negativo")
	@ReadOnly
	private BigDecimal cantidad;
	
	@Required
	@Min(value=0, message="No puede ser negativo")
	@ReadOnly
	private BigDecimal precioUnitario = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal total = BigDecimal.ZERO;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@ReadOnly
	private DespachoImportacion despacho;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@ReadOnly
	private Lote lote;

	@ReadOnly
	private BigDecimal precioOriginal;
	
	@ReadOnly
	private BigDecimal porcentajeDescuento = BigDecimal.ZERO;
	
	@ReadOnly
	private Long numeroPedidoCarrito;

	public BigDecimal getPorcentajeDescuento() {
		return porcentajeDescuento;
	}

	public void setPorcentajeDescuento(BigDecimal porcentajeDescuento) {
		this.porcentajeDescuento = porcentajeDescuento;
	}

	public BigDecimal getPrecioOriginal() {
		return precioOriginal;
	}

	public void setPrecioOriginal(BigDecimal precioOriginal) {
		this.precioOriginal = precioOriginal;
	}
	
	public PedidoML getPedido() {
		return pedido;
	}

	public void setPedido(PedidoML pedido) {
		this.pedido = pedido;
	}

	public BigDecimal getCantidad() {
		return cantidad == null ? BigDecimal.ZERO : this.cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		if (cantidad == null){
			this.cantidad = BigDecimal.ZERO;
		}
		else{
			this.cantidad = cantidad;
		}
	}

	public BigDecimal getPrecioUnitario() {
		return precioUnitario == null ? BigDecimal.ZERO : this.precioUnitario;
	}

	public void setPrecioUnitario(BigDecimal precioUnitario) {
		if (precioUnitario == null){
			this.precioUnitario = BigDecimal.ZERO;
		}
		else{
			this.precioUnitario = precioUnitario;
		}
	}

	public BigDecimal getTotal() {
		return total == null ? BigDecimal.ZERO : this.total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public PublicacionML getPublicacion() {
		return publicacion;
	}

	public void setPublicacion(PublicacionML publicacion) {
		this.publicacion = publicacion;
	}

	@Override
	public String toString(){
		if (this.getPublicacion() != null){
			return this.getPublicacion().toString();
		}
		else{
			return super.toString();
		}
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	@Override
	public Transaccion transaccion() {
		return this.getPedido();
	}

	@Override
	public void recalcular() {
		if (this.getPublicacion() != null){
			this.setProducto(this.getPublicacion().getProducto());
		}
	}
	
	@Override
	protected void onPrePersist() {
		super.onPrePersist();
	
		this.setTotal(this.getCantidad().multiply(this.getPrecioUnitario()));
	}
	
	@Override
	protected void onPreUpdate() {
		super.onPreUpdate();
		
		this.setTotal(this.getCantidad().multiply(this.getPrecioUnitario()));
	}

	public DespachoImportacion getDespacho() {
		return despacho;
	}

	public void setDespacho(DespachoImportacion despacho) {
		this.despacho = despacho;
	}

	@Override
	public ITipoMovimientoInventario tipoMovimientoInventario(boolean reversion) {
		if (!reversion){
			return new TipoMovInvReserva();
		}
		else{
			return new TipoMovInvDesreserva();
		}
	}

	@Override
	@Hidden
	public Deposito getDeposito() {
		return this.getPedido().getDeposito();
	}

	@Override
	public Cantidad cantidadStock() {
		Cantidad cantidad = new Cantidad();
		cantidad.setCantidad(this.getCantidad());
		cantidad.setUnidadMedida(this.getUnidadMedida());
		return cantidad;
	}

	@Override
	public void actualizarCantidadItem(Cantidad cantidad) {
		if (cantidad.getUnidadMedida().equals(this.getUnidadMedida())){
			this.setCantidad(cantidad.getCantidad());
		}
		else{
			throw new ValidationException("Difieren las unidades de medida");
		}		
	}

	@Override
	@Hidden
	public UnidadMedida getUnidadMedida() {
		return this.getProducto().getUnidadMedida();
	}
	
	public Lote getLote() {
		return lote;
	}

	public void setLote(Lote lote) {
		this.lote = lote;
	}

	@Override
	public void crearItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem) {
	}

	@Override
	public void posActualizarItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem) {
	}

	public Long getNumeroPedidoCarrito() {
		return numeroPedidoCarrito;
	}

	public void setNumeroPedidoCarrito(Long numeroPedidoCarrito) {
		this.numeroPedidoCarrito = numeroPedidoCarrito;
	}	
}
