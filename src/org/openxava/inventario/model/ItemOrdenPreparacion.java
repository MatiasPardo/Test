package org.openxava.inventario.model;

import java.math.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.base.calculators.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;
import org.openxava.inventario.actions.*;
import org.openxava.inventario.calculators.*;
import org.openxava.inventario.validators.*;
import org.openxava.jpa.*;
import org.openxava.negocio.actions.*;
import org.openxava.negocio.model.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

@Entity

@Views({
	@View(members=
	"producto; unidadMedida;" +
	"Cantidad[cantidad, despacho, stock;" + 			
			"noPreparar;" +
			"excedePedido, acepta;" +
			"pendientePreparar;" + 
			"]"	+ 
	"lote;"		
	)
})

@Tab(properties="ordenPreparacion.numero, ordenPreparacion.fecha, producto.codigo, producto.nombre, cantidad, despacho.codigo, lote.codigo",
	defaultOrder="${fechaCreacion} desc")

@EntityValidators({
	@EntityValidator(			
			value=ItemOrdenPreparacionValidator.class, 
			properties= {
				@PropertyValue(name="transaccion", from="ordenPreparacion"), 
				@PropertyValue(name="cantidadSolicitada", from="pendientePreparar"),
				@PropertyValue(name="cantidadNoPreparar", from="noPreparar"),
				@PropertyValue(name="cantidadPreparar", from="cantidad"),
				@PropertyValue(name="aceptaCantidadExcede", from="acepta"),
				@PropertyValue(name="producto", from="producto")
			}
	)
})

public class ItemOrdenPreparacion extends ItemTransaccion implements IItemMovimientoInventario, IDivisionItemTransaccion{
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private OrdenPreparacion ordenPreparacion;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY) 
	@ReferenceView("Simple")
	@NoCreate @NoModify
	@OnChange(OnChangeItemMovimientoInventario.class)
	private Producto producto;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre",
					depends=UnidadMedida.DEPENDSDESCRIPTIONLIST,
					condition=UnidadMedida.CONDITIONDESCRIPTIONLIST)
	@NoCreate @NoModify
	@OnChange(OnChangeUnidadMedida.class)
	private UnidadMedida unidadMedida;
	
	@ReadOnly
	private Boolean remitido = Boolean.FALSE;
	
	@Min(value=0, message="No puede ser negativo")
	@DefaultValueCalculator(  
				value=SinAsignarCalculator.class,
				properties={@PropertyValue(name="id", from="producto.id")}
			)
	private BigDecimal cantidad = BigDecimal.ZERO;
	
	@Hidden
	public BigDecimal getPendientePreparar() {
		if (this.getItemPedidoVenta() != null){
			BigDecimal pendientePreparar = this.getItemPedidoVenta().getPendientePreparacion();
			return pendientePreparar;
		}
		else if (this.getItemSolicitud() != null){
			BigDecimal pendientePreparar = this.getItemSolicitud().getPendientePreparacion();
			return pendientePreparar;
		}
		else{
			return BigDecimal.ZERO;
		}
	}
	
	@Min(value=0, message="No puede ser negativo")
	private BigDecimal noPreparar = BigDecimal.ZERO;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean acepta = Boolean.FALSE;
	
	@ReadOnly
	@DefaultValueCalculator(value=CantidadExcedePedidoCalculator.class, 
		properties={@PropertyValue(from="cantidad", name="cantidadPreparar"), 
					@PropertyValue(from="pendientePreparar", name="cantidadPendientePreparar")})	
	private BigDecimal excedePedido = BigDecimal.ZERO;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo")
	@NoCreate @NoModify
	@OnChange(OnChangeItemMovimientoInventario.class)
	private DespachoImportacion despacho;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	@OnChange(OnChangeItemMovimientoInventario.class)
	private Lote lote;
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private PedidoVenta pedido;
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private EstadisticaPedidoVenta itemPedidoVenta;
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private ItemSolicitudMercaderia itemSolicitud;
	
	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
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
	
	public PedidoVenta getPedido() {
		return pedido;
	}

	public void setPedido(PedidoVenta pedido) {
		this.pedido = pedido;
	}

	public EstadisticaPedidoVenta getItemPedidoVenta() {
		return itemPedidoVenta;
	}

	public void setItemPedidoVenta(EstadisticaPedidoVenta itemPedidoVenta) {
		this.itemPedidoVenta = itemPedidoVenta;
	}

	public OrdenPreparacion getOrdenPreparacion() {
		return ordenPreparacion;
	}

	public void setOrdenPreparacion(OrdenPreparacion ordenPreparacion) {
		this.ordenPreparacion = ordenPreparacion;
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

	public UnidadMedida getUnidadMedida() {
		return unidadMedida;
	}

	public void setUnidadMedida(UnidadMedida unidadMedida) {
		this.unidadMedida = unidadMedida;
	}

	@Override
	public Cantidad cantidadStock(){
		Cantidad cantidad = new Cantidad();
		cantidad.setCantidad(this.getCantidad());
		cantidad.setUnidadMedida(this.getUnidadMedida());
		return cantidad;
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
		if (this.getOrdenPreparacion() != null){
			return this.getOrdenPreparacion().getDeposito();
		}
		else{
			return null;
		}
	}

	@Override
	public void actualizarCantidadItem(Cantidad cantidad){
		if (cantidad.getUnidadMedida().equals(this.getUnidadMedida())){
			this.setCantidad(cantidad.getCantidad());
		}
		else{
			throw new ValidationException("Difieren las unidades de medida");
		}
	}
	
	
	public BigDecimal getStock(){
		return Inventario.buscarStock(this);
	}

	public Boolean getRemitido() {
		return remitido;
	}

	public void setRemitido(Boolean remitido) {
		this.remitido = remitido;
	}

	public BigDecimal getNoPreparar() {
		return noPreparar == null ? BigDecimal.ZERO : this.noPreparar;
	}

	public void setNoPreparar(BigDecimal noPreparar) {
		if (noPreparar == null){
			this.noPreparar = BigDecimal.ZERO;
		}
		else{
			this.noPreparar = noPreparar;
		}
	}
	
	public Boolean getAcepta() {
		return acepta == null ? Boolean.FALSE : this.acepta;
	}

	public void setAcepta(Boolean acepta) {
		if (acepta == null){
			this.acepta = Boolean.FALSE;
		}
		else{
			this.acepta = acepta;
		}
	}

	public BigDecimal getExcedePedido() {
		return excedePedido == null ? BigDecimal.ZERO : this.excedePedido;
	}

	public void setExcedePedido(BigDecimal excedePedido) {
		if (excedePedido == null){
			this.excedePedido = BigDecimal.ZERO;
		}
		else{
			this.excedePedido = excedePedido;
		}
	}

	public IItemPendiente itemPendienteRemitoProxy() {
		if (this.afectaStock()){
			ItemPendienteRemitoProxy itemPendiente = new ItemPendienteRemitoProxy();
			itemPendiente.setItemOrdenPreparacion(this);
			return itemPendiente;
		}
		else{
			return null;
		}
	}
	
	@Override
	public void crearItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem) {
		// Los items original es el que guarda la cantidad a no preparar
		((ItemOrdenPreparacion)nuevoItem).setNoPreparar(BigDecimal.ZERO);
		((ItemOrdenPreparacion)nuevoItem).setExcedePedido(BigDecimal.ZERO);
	}

	@Override
	public void posActualizarItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem) {
		if (this.getCantidad().compareTo(BigDecimal.ZERO) > 0){
			// Se redistribuye la cantidad que excede
			if (this.getExcedePedido().compareTo(this.getCantidad()) > 0){
				BigDecimal diferencia = this.getExcedePedido().subtract(this.getCantidad());
				this.setExcedePedido(this.getCantidad());
				((ItemOrdenPreparacion)nuevoItem).setExcedePedido(diferencia);
			}
		}
	}

	@Override
	public Transaccion transaccion() {
		return this.getOrdenPreparacion();
	}

	@Override
	public void recalcular() {
		this.recalcularCantidadExcedePedido();
	}
	
	public void recalcularCantidadExcedePedido(){
		BigDecimal excede = BigDecimal.ZERO;
		if (this.getAcepta()){
			try{
				CantidadExcedePedidoCalculator calculator = new CantidadExcedePedidoCalculator();
				calculator.setCantidadPendientePreparar(this.getPendientePreparar());
				calculator.setCantidadPreparar(this.getCantidad());
				excede = (BigDecimal)calculator.calculate();
			}
			catch(Exception e){				
			}
		}
		this.setExcedePedido(excede);		
	}
	
	public boolean afectaStock(){
		return this.cantidadStock().getCantidad().compareTo(BigDecimal.ZERO) != 0;
	}
	
	public ItemSolicitudMercaderia getItemSolicitud() {
		return itemSolicitud;
	}

	public void setItemSolicitud(ItemSolicitudMercaderia itemSolicitud) {
		this.itemSolicitud = itemSolicitud;
	}
	
	@Override
	public ObjetoNegocio generarCopia() {
		ItemOrdenPreparacion copia = new ItemOrdenPreparacion();
		copia.copiarPropiedades(this);
		
		XPersistence.getManager().persist(copia);

		return copia;
	}

	@Override
	public ItemTransaccion dividirConNuevoItem() {

		ItemOrdenPreparacion nuevoItem = (ItemOrdenPreparacion) this.generarCopia();
		
		nuevoItem.setCantidad(BigDecimal.ZERO);
		nuevoItem.setCantidad(this.getPendientePreparar().subtract(this.getCantidad()));
		return nuevoItem;
	}

	@Override
	public void validacionesPreDividir() {
		if(this.getPendientePreparar() == null ){
			throw new ValidationException("El item no tiene pendiente.");
		}
		/*if(this.getPedido() == null && this.getItemSolicitud() == null){
			throw new ValidationException("El item no tiene solicitud ni pedido asociado.");
		}	
		*/	
	}
}
