package org.openxava.inventario.model;

import java.math.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.hibernate.annotations.Formula;
import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;
import org.openxava.codigobarras.model.IItemControlCodigoBarras;
import org.openxava.codigobarras.model.TipoControlCodigoBarras;
import org.openxava.compras.model.*;
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
		"noConforme, motivoNoConformidad;" + 
		"producto;" +
		"unidadMedida;" +
		"cantidad, precioUnitario, cantidadExcedeOC, controlado;" +
		"noEntregados, pendienteRecepcion;" + 
		"itemOrdenCompra;" + 
		"lote;"
	),
	@View(name="FacturaCompra",
		members="cantidad, pendienteFacturacion")
})

@EntityValidators({
	@EntityValidator(			
			value=ItemRecepcionMercaderiaValidator.class, 
			properties= {
				@PropertyValue(name="transaccion", from="recepcionMercaderia"), 
				@PropertyValue(name="cantidadPendiente", from="pendienteRecepcion"),
				@PropertyValue(name="cantidadNoEntregada", from="noEntregados"),
				@PropertyValue(name="cantidadRecepcionada", from="cantidad"),
				@PropertyValue(name="producto", from="producto")
			}
	)
})

@Tab(properties="recepcionMercaderia.fecha, recepcionMercaderia.numero, recepcionMercaderia.estado, recepcionMercaderia.proveedor.nombre, recepcionMercaderia.proveedor.codigo, producto.codigo, producto.nombre, cantidad, cantidadExcedeOC, noEntregados")

public class ItemRecepcionMercaderia extends ItemTransaccion implements IItemMovimientoInventario, IDivisionItemTransaccion, IItemControlCodigoBarras{

	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private RecepcionMercaderia recepcionMercaderia;
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView(value="Recepcion")
	private ItemOrdenCompra itemOrdenCompra;
	
	@Hidden
	public BigDecimal getPendienteRecepcion(){
		BigDecimal pendiente = BigDecimal.ZERO;
		if (this.getItemOrdenCompra() != null){
			pendiente = this.getItemOrdenCompra().getPendienteRecepcion();
		}
		return pendiente;
	}
	
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
	private UnidadMedida unidadMedida;
	
	@Min(value=0, message="No puede ser negativo")
	private BigDecimal cantidad;

	@ReadOnly
	private BigDecimal controlado = BigDecimal.ZERO;
	
	@DefaultValueCalculator(value=ZeroBigDecimalCalculator.class)
	@Min(value=0, message="No puede ser negativo")
	private BigDecimal noEntregados = BigDecimal.ZERO;
	
	@ReadOnly
	@DefaultValueCalculator(value=NoConformeCantidadExcedeOCCalculator.class, 
	properties={@PropertyValue(from="cantidad", name="cantidad")})
	private BigDecimal cantidadExcedeOC = BigDecimal.ZERO;
	
	private BigDecimal precioUnitario = BigDecimal.ZERO;
	
	@ReadOnly
	@DefaultValueCalculator(value=NoConformeItemRecepcionCalculator.class, 
			properties={@PropertyValue(from="motivoNoConformidad", name="motivo")})
	private Boolean noConforme = Boolean.FALSE; 
	
	@Column(length=100)
	@ReadOnly
	@DefaultValueCalculator(value=NoConformeMotivoItemRecepcionCalculator.class, 
		properties={@PropertyValue(from="producto.id", name="idProducto"), 
					@PropertyValue(from="cantidad", name="cantidad"),
					@PropertyValue(from="precioUnitario", name="precio")})
	private String motivoNoConformidad;
	
	@ReadOnly
	private BigDecimal pendienteFacturacion = BigDecimal.ZERO;
	
	public void setMotivoNoConformidad(String motivoNoConformidad) {
		this.motivoNoConformidad = motivoNoConformidad;
	}

	public String getMotivoNoConformidad(){
		return this.motivoNoConformidad;
	}
	
	public ItemOrdenCompra getItemOrdenCompra() {
		return itemOrdenCompra;
	}

	public void setItemOrdenCompra(ItemOrdenCompra itemOrdenCompra) {
		this.itemOrdenCompra = itemOrdenCompra;		
	}

	public RecepcionMercaderia getRecepcionMercaderia() {
		return recepcionMercaderia;
	}

	public void setRecepcionMercaderia(RecepcionMercaderia recepcionMercaderia) {
		this.recepcionMercaderia = recepcionMercaderia;
	}

	public BigDecimal getCantidad() {
		return cantidad == null ? BigDecimal.ZERO : cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}

	@Override
	public Producto getProducto() {
		return this.producto;
	}
	
	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	@Override
	public UnidadMedida getUnidadMedida() {
		return this.unidadMedida;
	}
	
	public void setUnidadMedida(UnidadMedida unidadMedida) {
		this.unidadMedida = unidadMedida;
	}

	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoModify
	private Lote lote;
	
	@Override
	@Hidden
	public DespachoImportacion getDespacho() {
		DespachoImportacion despacho = null;
		if (this.getRecepcionMercaderia() != null){
			despacho = this.getRecepcionMercaderia().getDespacho();
		}
		return despacho;
	}

	@Override
	public void setDespacho(DespachoImportacion despacho) {
		if (this.getRecepcionMercaderia() != null){
			this.getRecepcionMercaderia().setDespacho(despacho);
		}
	}
	
	@Override
	public Lote getLote() {
		return lote;
	}

	@Override
	public void setLote(Lote lote) {
		this.lote = lote;
	}

	public BigDecimal getPrecioUnitario() {
		return precioUnitario == null ? BigDecimal.ZERO : this.precioUnitario;
	}

	public void setPrecioUnitario(BigDecimal precioUnitario) {
		if (precioUnitario != null){
			this.precioUnitario = precioUnitario;
		}
	}

	public Boolean getNoConforme() {
		return noConforme == null ? Boolean.FALSE : noConforme;
	}

	public void setNoConforme(Boolean noConforme) {
		this.noConforme = noConforme;
	}
	
	public BigDecimal getCantidadExcedeOC() {
		return cantidadExcedeOC == null ? BigDecimal.ZERO : cantidadExcedeOC;
	}

	public void setCantidadExcedeOC(BigDecimal cantidadExcedeOC) {
		this.cantidadExcedeOC = cantidadExcedeOC;
	}

	public BigDecimal getNoEntregados() {
		return noEntregados == null ? BigDecimal.ZERO : this.noEntregados;
	}

	public void setNoEntregados(BigDecimal noEntregados) {
		if (noEntregados != null){
			this.noEntregados = noEntregados;
		}
		else{
			this.noEntregados = BigDecimal.ZERO;
		}
	}
	
	@Hidden
	public BigDecimal getImporteNoConformidad() {
		BigDecimal importe = BigDecimal.ZERO;
		if (this.getNoConforme()){
			if (this.getItemOrdenCompra() != null){
				// diferente producto, si hay diferencia de precio
				BigDecimal diferenciaPrecio = this.getPrecioUnitario().subtract(this.getItemOrdenCompra().getPrecioUnitario());
				if (diferenciaPrecio.compareTo(BigDecimal.ZERO) > 0){
					// toda la cantidad se considera no conforme, porque es un producto diferente
					importe = (this.getCantidad().subtract(this.getCantidadExcedeOC())).multiply(diferenciaPrecio);
				}
				if (this.getCantidadExcedeOC().compareTo(BigDecimal.ZERO) > 0){
					importe = importe.add(this.getCantidadExcedeOC().multiply(this.getPrecioUnitario()));
				}
			}
			else{
				// todo lo recibido no fue solicitado en la orden de compra
				importe = this.getCantidad().multiply(this.getPrecioUnitario());
			}
		}
		return importe;	
	}
	
	@Override
	public Transaccion transaccion() {
		return this.getRecepcionMercaderia();
	}

	@Override
	public void recalcular() {		
	}

	@Override
	public ITipoMovimientoInventario tipoMovimientoInventario(boolean reversion) {
		if (!reversion){
			return new TipoMovInvIngreso();
		}
		else{
			return new TipoMovInvEgreso();
		}
		
	}

	@Override
	@Hidden
	public Deposito getDeposito() {
		if (this.getRecepcionMercaderia() != null){
			return this.getRecepcionMercaderia().getDeposito();
		}
		else{
			return null;
		}
	}

	@Override
	public Cantidad cantidadStock() {
		Cantidad cantidad = new Cantidad();
		cantidad.setUnidadMedida(this.getUnidadMedida());
		cantidad.setCantidad(this.getCantidad());
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
	
	public void crearItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem){
	}
	
	@Override
	public void posActualizarItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem) {		
	}
	
	public BigDecimal getPendienteFacturacion() {
		return pendienteFacturacion == null ? BigDecimal.ZERO : this.pendienteFacturacion;
	}
	
	public void setPendienteFacturacion(BigDecimal pendienteFacturacion) {
		if (pendienteFacturacion != null){
			this.pendienteFacturacion = pendienteFacturacion;
		}
		else{
			this.pendienteFacturacion = BigDecimal.ZERO;
		}
	}
	
	public ItemPendienteFacturaCompraProxy itemPendienteFacturaCompraProxy(){
		if (this.getCantidad().compareTo(BigDecimal.ZERO) > 0){
			ItemPendienteFacturaCompraProxy item = new ItemPendienteFacturaCompraProxy();		
			item.setItemRecepcion(this);
			return item;
		}
		else{
			return null;
		}
	}
	
	public BigDecimal cantidadSaldarPendiente() {
		return this.getCantidad().subtract(this.getCantidadExcedeOC()).add(this.getNoEntregados());
	}
	
	@Override
	public ObjetoNegocio generarCopia() {
		ItemRecepcionMercaderia copia = new ItemRecepcionMercaderia();
		copia.setRecepcionMercaderia(this.getRecepcionMercaderia());
		copia.setItemOrdenCompra(this.getItemOrdenCompra());
		copia.copiarPropiedades(this);

		XPersistence.getManager().persist(copia);
		
		return copia;
	}
	
	@Override
	public ItemTransaccion dividirConNuevoItem(){
		
		ItemRecepcionMercaderia nuevoItem = (ItemRecepcionMercaderia) this.generarCopia();
	
		nuevoItem.setCantidad(BigDecimal.ZERO);
		nuevoItem.setCantidad(this.getPendienteRecepcion().subtract(this.getCantidad()));
		
		return nuevoItem;
	}
	
	@Override
	public void validacionesPreDividir(){
		if(this.getPendienteRecepcion() == null ){
			throw new ValidationException("El item no tiene pendiente.");
		}
		if(this.getItemOrdenCompra() == null ){
			throw new ValidationException("El item no tiene orden de compra asociada.");
		}
	}

	@Override
	public BigDecimal getControlado() {
		return controlado == null ? BigDecimal.ZERO : controlado;
	}

	@Override
	public void setControlado(BigDecimal controlado) {
		if (controlado != null){
			this.controlado = controlado;
		}
		else{
			this.controlado = BigDecimal.ZERO;
		}		
	}

	@Formula("cantidad - controlado")
	private BigDecimal pendienteControl;
	
	public BigDecimal getPendienteControl(){
		return this.getCantidad().subtract(this.getControlado());
	}
	
	@Formula("(case when cantidad = 0 then 2 "
			+ "when (cantidad - controlado) = 0 then 1 "
			+ "else 0 end)"
			)  
	private TipoControlCodigoBarras control;
	
	public TipoControlCodigoBarras getControl(){
		return control;
	}
	
	@Override
	public boolean crearEntidadesPorControl() {
		return true;
	}

	@Override
	public BigDecimal convertirUnidadesLeidas(BigDecimal cantidadLeida) {
		return cantidadLeida;
	}

	@Override
	public void copiarPropiedades(Object objeto){
		super.copiarPropiedades(objeto);
		
		this.setControlado(BigDecimal.ZERO);
		this.setNoEntregados(BigDecimal.ZERO);
	}
}
