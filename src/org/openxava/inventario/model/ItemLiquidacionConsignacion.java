package org.openxava.inventario.model;

import java.math.*;
import java.text.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.inventario.calculators.*;
import org.openxava.negocio.model.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;


@Entity

@Views({
	@View(members=
		"itemRemito;" +		
		"facturar, devolucion, cantidadTotal;" + 
		"facturado;"
	)
})

public class ItemLiquidacionConsignacion extends ItemTransaccion implements IItemMovimientoInventario{

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	private LiquidacionConsignacion liquidacion;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@ReadOnly
	private Producto producto;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre",
					depends=UnidadMedida.DEPENDSDESCRIPTIONLIST,
					condition=UnidadMedida.CONDITIONDESCRIPTIONLIST)
	@ReadOnly	
	private UnidadMedida unidadMedida;
		
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@ReadOnly
	private DespachoImportacion despacho;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@ReadOnly
	private Lote lote;
	
	private BigDecimal facturar = BigDecimal.ZERO;
		
	private BigDecimal devolucion = BigDecimal.ZERO;
	
	@Required
	@ReadOnly
	@DefaultValueCalculator(value=CantidadTotalItemLiquidacionCalculator.class, 
						properties={
							@PropertyValue(from="facturar", name="cantidadFacturar"),
							@PropertyValue(from="devolucion", name="cantidadDevolucion")
						})	
	private BigDecimal cantidadTotal = BigDecimal.ZERO;
		
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly	
	@ReferenceView("Liquidacion")	
	private ItemRemito itemRemito;
	
	@ReadOnly
	private Boolean facturado = Boolean.FALSE;
	
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

	public BigDecimal getFacturar() {
		return facturar == null ? BigDecimal.ZERO : facturar;
	}

	public void setFacturar(BigDecimal facturar) {
		this.facturar = facturar;
		this.recalcularCantidadTotal();
	}

	public BigDecimal getDevolucion() {
		return devolucion == null ? BigDecimal.ZERO : devolucion;
	}

	public void setDevolucion(BigDecimal devolucion) {
		this.devolucion = devolucion;
		this.recalcularCantidadTotal();
	}

	public ItemRemito getItemRemito() {
		return itemRemito;
	}

	public void setItemRemito(ItemRemito itemRemito) {
		this.itemRemito = itemRemito;
	}

	public LiquidacionConsignacion getLiquidacion() {
		return liquidacion;
	}

	public void setLiquidacion(LiquidacionConsignacion liquidacion) {
		this.liquidacion = liquidacion;
	}

	public Boolean getFacturado() {
		return facturado;
	}

	public void setFacturado(Boolean facturado) {
		this.facturado = facturado;
	}

	@Override
	public Transaccion transaccion() {
		return this.getLiquidacion();
	}

	@Override
	public void recalcular() {
	}

	@Override
	public ITipoMovimientoInventario tipoMovimientoInventario(boolean reversion) {
		// la liquidación es un movimiento de egreso por la cantidad a facturar y la que se devuelve
		if (this.getCantidadTotal().compareTo(BigDecimal.ZERO) > 0){
			if (!reversion){
				return new TipoMovInvEgresoDesreserva();
			}
			else{
				return new TipoMovInvIngresoReserva();
			}
		}
		else{
			throw new ValidationException("error item de liquidación de consignación: no tiene cantidad para facturar y esta intentando egresar inventario");
		}
	}

	@Override
	public Deposito getDeposito() {
		return this.getLiquidacion().getDepositoPorConsignacion();
	}
	
	public Cantidad cantidadPorDevolucion(){
		Cantidad cantidad = new Cantidad();
		cantidad.setUnidadMedida(this.getUnidadMedida());
		cantidad.setCantidad(this.getDevolucion());
		return cantidad;
	}

	public Cantidad cantidadPorFacturar(){
		Cantidad cantidad = new Cantidad();
		cantidad.setUnidadMedida(this.getUnidadMedida());
		cantidad.setCantidad(this.getFacturar());
		return cantidad;
	}
	
	public Cantidad cantidadTotal(){
		Cantidad cantidad = new Cantidad();
		cantidad.setUnidadMedida(this.getUnidadMedida());
		cantidad.setCantidad(this.getFacturar().add(this.getDevolucion()));
		return cantidad;
	}
	
	@Override
	public Cantidad cantidadStock() {
		// el item representa un movimiento de egreso por toda la cantidad. Esta mercadería sale del depósito de consignación.
		return this.cantidadTotal();
	}

	@Override
	public void actualizarCantidadItem(Cantidad cantidad) {		
	}
	
	public BigDecimal getCantidadTotal() {
		return cantidadTotal == null ? BigDecimal.ZERO : cantidadTotal;
	}

	public void setCantidadTotal(BigDecimal cantidadTotal) {
		this.cantidadTotal = cantidadTotal;
	}

	private void recalcularCantidadTotal(){
		try{
			CantidadTotalItemLiquidacionCalculator calculator = new CantidadTotalItemLiquidacionCalculator();
			calculator.setCantidadDevolucion(this.getDevolucion());
			calculator.setCantidadFacturar(this.getFacturar());
			this.setCantidadTotal((BigDecimal)calculator.calculate());
		}
		catch(Exception e){
			
		}
	}
	
	@Override
	protected void onPrePersist(){
		super.onPrePersist();
		this.validarAntesGrabarItemLiquidacion();
	}

	@Override
	protected void onPreUpdate(){
		super.onPreUpdate();
		this.validarAntesGrabarItemLiquidacion();
	}
	
	private void validarAntesGrabarItemLiquidacion(){
		if ((this.getItemRemito() != null) && (this.getLiquidacion() != null)){
			if (!this.getLiquidacion().cerrado()){
				// esta validacion unicamente se activa si la transacción no esta confirmada o anulada. 
				// en estado confirmada, la validación se encarga el pendiente.
				BigDecimal pendiente = this.getItemRemito().getPendienteLiquidacion();
				if (pendiente.compareTo(this.getCantidadTotal()) < 0){
					DecimalFormat format = new DecimalFormat("####.##");
					DecimalFormatSymbols symbols = new DecimalFormatSymbols();
					symbols.setDecimalSeparator('.');
					format.setDecimalFormatSymbols(symbols);
					throw new ValidationException("La cantidad total no puede superar lo pendiente de liquidación: " + format.format(pendiente));
				}
			}
		}
	}
	
	public IItemPendiente itemPendienteFacturaVentaProxy() {
		if (this.getFacturar().compareTo(BigDecimal.ZERO) > 0){
			ItemPendienteFacturaVentaProxy itemPendiente = new ItemPendienteFacturaVentaProxy();
			itemPendiente.setItemRemito(this.getItemRemito());
			itemPendiente.setItemLiquidacion(this);
			return itemPendiente;
		}
		else{
			// este item no genera pendiente
			return null;
		}
	}
	
	@Override
	public void crearItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem){
	}
	
	@Override
	public void posActualizarItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem) {		
	}
}
