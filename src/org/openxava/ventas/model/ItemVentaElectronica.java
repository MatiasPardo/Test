package org.openxava.ventas.model;

import java.math.*;
import java.text.*;
import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.base.calculators.*;
import org.openxava.base.model.*;
import org.openxava.codigobarras.model.IItemControlCodigoBarras;
import org.openxava.contabilidad.model.*;
import org.openxava.contratos.model.NovedadContrato;
import org.openxava.cuentacorriente.model.*;
import org.openxava.impuestos.model.*;
import org.openxava.inventario.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.actions.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.actions.*;
import org.openxava.ventas.calculators.*;

@Entity

@Views({
	@View(members=
	"producto;" + 
	"unidadMedida;" +
	"cantidad, precioUnitario, porcentajeDescuento;" +
	"detalle;" +
	"descuento, suma;" +
	"descuentoGlobal, descuentoFinanciero;" +
	"subtotal;" +
	"tasaiva, iva, impuestoInterno;" +
	"despacho, lote, centroCostos;" +  
	"venta;"), 
	@View(name="Remito", members=
		"producto;" + 
		"unidadMedida;" +
		"cantidad;" + 
		"despacho;" + 
		"lote;"),
	@View(name="Factura", members="venta;")
})

public class ItemVentaElectronica extends ItemTransaccion implements IGeneradorItemContable, IItemMovimientoInventario, IItemControlCodigoBarras{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView("Simple")
	@OnChange(OnChangeProductoItemVentaElectronica.class)
	@SearchListCondition(value="${ventas} = 't'")
	private Producto producto;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre", 
					depends=UnidadMedida.DEPENDSDESCRIPTIONLIST,
					condition=UnidadMedida.CONDITIONDESCRIPTIONLIST)
	@NoCreate @NoModify
	//@DefaultValueCalculator(value=UnidadMedidaDeProductoCalculator.class, 
	//					properties={@PropertyValue(name="idProducto", from="producto.id")})
	@OnChange(OnChangeUnidadMedida.class)
	private UnidadMedida unidadMedida;
	
	@Min(value=0, message="No puede ser negativo")
	@DefaultValueCalculator(  
				value=SinAsignarCalculator.class,
				properties={@PropertyValue(name="id", from="producto.id")}
			)
	@OnChange(OnChangeCantidadItemVentaElectronica.class)
	@Required
	private BigDecimal cantidad = BigDecimal.ZERO;
	
	private BigDecimal precioUnitario = BigDecimal.ZERO;
	
	@Min(value=0, message="No puede menor a 0")
	@Max(value=100, message="No puede ser mayor a 100")
	@DefaultValueCalculator(  
			value=CantidadCalculator.class,
			properties={@PropertyValue(name="productoID", from="producto.id")}
		)
	private BigDecimal porcentajeDescuento = BigDecimal.ZERO;
	
	@ReadOnly
	@Digits(integer=19, fraction=4)
	@DefaultValueCalculator(
			value=DescuentoItemVentaCalculator.class,
			properties={@PropertyValue(name="cantidad", from="cantidad"),
						@PropertyValue(name="precioUnitario", from="precioUnitario"),
						@PropertyValue(name="porcentajeDescuento", from="porcentajeDescuento")})
	private BigDecimal descuento = BigDecimal.ZERO;
	
	@ReadOnly
	@Digits(integer=19, fraction=4)
	@DefaultValueCalculator(  
			value=SumaItemVentaCalculator.class,
			properties={@PropertyValue(name="cantidad", from="cantidad"),
						@PropertyValue(name="precioUnitario", from="precioUnitario"),
						@PropertyValue(name="porcentajeDescuento", from="porcentajeDescuento")})
	@OnChange(OnChangeSumaItemVenta.class)
	private BigDecimal suma = BigDecimal.ZERO;
	
	@ReadOnly
	@Digits(integer=19, fraction=4)
	private BigDecimal descuentoGlobal = BigDecimal.ZERO;
	
	@ReadOnly
	@Digits(integer=19, fraction=4)
	private BigDecimal descuentoFinanciero = BigDecimal.ZERO;
	
	@ReadOnly
	@Digits(integer=19, fraction=4)
	private BigDecimal subtotal = BigDecimal.ZERO;
	
	@ReadOnly
	@Digits(integer=19, fraction=4)
	private BigDecimal subtotal1 = BigDecimal.ZERO;
	
	@ReadOnly
	@Digits(integer=19, fraction=4)
	private BigDecimal subtotal2 = BigDecimal.ZERO;
	
	@org.hibernate.annotations.Formula("subtotal + iva + impuestoInterno")
	private BigDecimal total;
	
	public BigDecimal getTotal() {
		return total;
	}

	@ReadOnly
	@DefaultValueCalculator(  
			value=TasaIvaCalculator.class,
			properties={@PropertyValue(name="productoID", from="producto.id")}
		)
	private BigDecimal tasaiva = BigDecimal.ZERO;
	
	@org.hibernate.annotations.Formula("(precioUnitario + (precioUnitario * tasaiva / 100))")
	private BigDecimal precioMasIva;
	
	public BigDecimal getPrecioMasIva() {
		return precioMasIva;
	}

	@Digits(integer=19, fraction=4)
	@DefaultValueCalculator(  
			value=IVACalculator.class,
			properties={@PropertyValue(name="tasa", from="tasaiva"),
						@PropertyValue(name="importe", from="subtotal")}
		)
	@ReadOnly
	private BigDecimal iva = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal tasaImpuestoInterno = BigDecimal.ZERO;
	
	@Digits(integer=19, fraction=4)
	@ReadOnly
	private BigDecimal impuestoInterno = BigDecimal.ZERO;
	
	@ReadOnly
	@Digits(integer=19, fraction=4)
	private BigDecimal iva1 = BigDecimal.ZERO;
	
	@ReadOnly
	@Digits(integer=19, fraction=4)
	private BigDecimal iva2 = BigDecimal.ZERO;
	
	@Digits(integer=19, fraction=4)
	@ReadOnly
	private BigDecimal impuestoInterno1 = BigDecimal.ZERO;
	
	@Digits(integer=19, fraction=4)
	@ReadOnly
	private BigDecimal impuestoInterno2 = BigDecimal.ZERO;
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceViews({
		@ReferenceView(value="Simple", notForViews="Factura"),		
	})	
	private VentaElectronica venta;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo")
	@NoCreate @NoModify
	private DespachoImportacion despacho;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, fechaVencimiento", 
				depends="this.producto", 
				condition="${producto.id} = ?", forTabs="Combo")
	@NoCreate @NoModify
	private Lote lote;
	
	@ReadOnly
	@Hidden
	@Column(length=32)
	private String idDiferenciaCambio;
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private ItemRemito itemRemito;
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private ItemLiquidacionConsignacion itemLiquidacion;
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private EstadisticaPedidoVenta itemPedido;
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private NovedadContrato novedadContrato;
	
	@ReadOnly
	private Boolean cumplido = Boolean.FALSE;
	
	@Column(length=50)
	private String detalle;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private CentroCostos centroCostos;
	
	public CentroCostos getCentroCostos() {
		return centroCostos;
	}

	
	public void setCentroCostos(CentroCostos centroCostos) {
		this.centroCostos = centroCostos;
	}

	
	public DiferenciaCambioVenta generadoPorDiferenciaCambio(){
		if (!Is.emptyString(this.getIdDiferenciaCambio())){
			return (DiferenciaCambioVenta)XPersistence.getManager().find(DiferenciaCambioVenta.class, this.getIdDiferenciaCambio());
		}
		else{
			return null;
		}
	}
	
	public String getIdDiferenciaCambio() {
		return idDiferenciaCambio;
	}

	public void setIdDiferenciaCambio(String idDiferenciaCambio) {
		this.idDiferenciaCambio = idDiferenciaCambio;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;		
	}

	public BigDecimal getCantidad() {
		return cantidad == null? BigDecimal.ZERO: this.cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}

	public BigDecimal getPrecioUnitario() {
		return precioUnitario == null? BigDecimal.ZERO: this.precioUnitario;
	}

	public void setPrecioUnitario(BigDecimal precioUnitario) {
		this.precioUnitario = precioUnitario;
	}

	public BigDecimal getPorcentajeDescuento() {
		return this.porcentajeDescuento == null? BigDecimal.ZERO: this.porcentajeDescuento;
	}

	public void setPorcentajeDescuento(BigDecimal porcentajeDescuento) {
		this.porcentajeDescuento = porcentajeDescuento;
	}

	public BigDecimal getDescuento() {
		return this.descuento == null? BigDecimal.ZERO: this.descuento;
	}

	public void setDescuento(BigDecimal descuento) {
		this.descuento = descuento;
	}

	@Hidden
	public BigDecimal getSumaSinDescuento(){
		return this.getPrecioUnitario().multiply(this.getCantidad()).setScale(4, RoundingMode.HALF_EVEN);
	}
	
	public BigDecimal getSuma() {
		return this.suma == null? BigDecimal.ZERO: this.suma;
	}

	public void setSuma(BigDecimal suma) {
		this.suma = suma;
	}

	public BigDecimal getTasaiva() {
		return tasaiva;
	}

	public void setTasaiva(BigDecimal tasaiva) {
		this.tasaiva = tasaiva;
	}

	public BigDecimal getIva() {
		return iva == null ? BigDecimal.ZERO : this.iva;
	}

	public void setIva(BigDecimal iva) {
		this.iva = iva;
	}

	public BigDecimal getIva1() {
		return iva1 == null ? BigDecimal.ZERO : this.iva1;
	}

	public void setIva1(BigDecimal iva1) {
		this.iva1 = iva1;
	}

	public BigDecimal getIva2() {
		return iva2 == null ? BigDecimal.ZERO : this.iva2;
	}

	public void setIva2(BigDecimal iva2) {
		this.iva2 = iva2;
	}
	
	public BigDecimal getTasaImpuestoInterno() {
		return tasaImpuestoInterno == null ? BigDecimal.ZERO : this.tasaImpuestoInterno;
	}

	public void setTasaImpuestoInterno(BigDecimal tasaImpuestoInterno) {
		this.tasaImpuestoInterno = tasaImpuestoInterno;
	}

	public BigDecimal getImpuestoInterno() {
		return impuestoInterno == null ? BigDecimal.ZERO : this.impuestoInterno;
	}

	public void setImpuestoInterno(BigDecimal impuestoInterno) {
		this.impuestoInterno = impuestoInterno;
	}
	
	public BigDecimal getImpuestoInterno1() {
		return impuestoInterno1 == null ? BigDecimal.ZERO : this.impuestoInterno1;
	}

	public void setImpuestoInterno1(BigDecimal impuestoInterno1) {
		this.impuestoInterno1 = impuestoInterno1;
	}

	public BigDecimal getImpuestoInterno2() {
		return impuestoInterno2 == null ? BigDecimal.ZERO : this.impuestoInterno2;
	}

	public void setImpuestoInterno2(BigDecimal impuestoInterno2) {
		this.impuestoInterno2 = impuestoInterno2;
	}

	public BigDecimal getSubtotal() {
		return subtotal == null ? BigDecimal.ZERO : this.subtotal;
	}

	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}

	public BigDecimal getSubtotal1() {
		return subtotal1 == null ? BigDecimal.ZERO : this.subtotal1;
	}

	public void setSubtotal1(BigDecimal subtotal1) {
		this.subtotal1 = subtotal1;
	}

	public BigDecimal getSubtotal2() {
		return subtotal2 == null ? BigDecimal.ZERO : this.subtotal2;
	}

	public void setSubtotal2(BigDecimal subtotal2) {
		this.subtotal2 = subtotal2;
	}

	public VentaElectronica getVenta() {
		return venta;
	}

	public void setVenta(VentaElectronica venta) {
		this.venta = venta;
	}

	public BigDecimal getDescuentoGlobal() {
		return this.descuentoGlobal == null ? BigDecimal.ZERO : this.descuentoGlobal;
	}
	
	public void setDescuentoGlobal(BigDecimal descuentoGlobal){
		this.descuentoGlobal = descuentoGlobal;
	}
	
	public BigDecimal getDescuentoFinanciero() {
		return descuentoFinanciero == null ? BigDecimal.ZERO : this.descuentoFinanciero;
	}

	public void setDescuentoFinanciero(BigDecimal descuentoFinanciero) {
		this.descuentoFinanciero = descuentoFinanciero;
	}

	public UnidadMedida getUnidadMedida() {
		return unidadMedida;
	}

	public void setUnidadMedida(UnidadMedida unidadMedida) {
		this.unidadMedida = unidadMedida;
	}
		
	@Override
	public Boolean soloLectura(){
		Boolean sololectura = super.soloLectura();
		if (!sololectura){
			if (this.getVenta() != null){
				sololectura = this.getVenta().soloLectura();
			}
		}
		return sololectura;
	}

	public DespachoImportacion getDespacho() {
		return this.despacho;
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

	private void calcularImportes(){
		BigDecimal porcentaje = (new BigDecimal(100)).subtract(this.getPorcentajeDescuento());
		BigDecimal precioPorCantidad = this.getSumaSinDescuento();
		this.setDescuento(precioPorCantidad.multiply(this.getPorcentajeDescuento()).divide(new BigDecimal(100)).setScale(4, RoundingMode.HALF_EVEN).negate());
		// La suma es el precio unitario por cantidad menos el descuento de item 
		this.setSuma(precioPorCantidad.multiply(porcentaje).divide(new BigDecimal(100)).setScale(4, RoundingMode.HALF_EVEN));
		if (this.esInteres()){
			this.setDescuentoGlobal(BigDecimal.ZERO);
		}
		else{
			this.setDescuentoGlobal(this.getSuma().multiply(this.getVenta().getPorcentajeDescuento()).divide(new BigDecimal(100)).setScale(4, RoundingMode.HALF_EVEN).negate());
		}
		this.setDescuentoFinanciero(this.getSuma().add(this.getDescuentoGlobal()).multiply(this.getVenta().getPorcentajeFinanciero()).divide(new BigDecimal(100)).setScale(4, RoundingMode.HALF_EVEN).negate());
		this.setSubtotal(this.getSuma().add(this.getDescuentoGlobal()).add(this.getDescuentoFinanciero()));
	}
	
	public void recalcular(){
		if (this.getUnidadMedida() == null){
			if(this.getProducto() != null){
				this.setUnidadMedida(this.getProducto().getUnidadMedida());
			}
		}
		
		this.verificarPrecioUnitario();
		
		this.calcularImportes();
				
		Boolean calculaImpuestos = Boolean.TRUE;
		boolean calculaIVA = true;
		if (this.getVenta() != null){
			calculaImpuestos = this.getVenta().calculaImpuestos();
			calculaIVA = this.getVenta().getCliente().getPosicionIva().calculaIvaVentas();
		}
		
		// Tasa de iva y de imp interno
		if (producto != null){
			
			BigDecimal tasaImpInterno = BigDecimal.ZERO;
			if (producto.getImpuestoInterno() != null){
				tasaImpInterno = producto.getImpuestoInterno().getAlicuotaGeneral();
			}
			
			if (this.getVenta() != null){
				if (!this.getVenta().revierteTransaccion()){
					// solo en este caso se toma en cuenta el iva del producto.
					// Cuando revierte una transacción debe reflejar el iva con el que se grabó
					
					if (calculaIVA){
						this.setTasaiva(this.getVenta().alicuotaIva(producto));
					}
					else{
						this.setTasaiva(BigDecimal.ZERO);
					}
					this.setTasaImpuestoInterno(tasaImpInterno);
				}
				
				this.validarStockDisponible();
			}
			else{
				this.setTasaiva(producto.getTasaIva().getPorcentaje());
				this.setTasaImpuestoInterno(tasaImpInterno);
			}
		}
		
		if (calculaImpuestos){
			// Iva
			if (calculaIVA){
				this.setIva(this.getSubtotal().multiply(this.getTasaiva()).divide(new BigDecimal(100)).setScale(4, RoundingMode.HALF_EVEN));
			}
			else{
				this.setIva(BigDecimal.ZERO);
			}
			// Impuesto Interno
			this.setImpuestoInterno(this.getSubtotal().multiply(this.getTasaImpuestoInterno()).divide(new BigDecimal(100)).setScale(4, RoundingMode.HALF_EVEN));
		}
		else{
			this.setIva(BigDecimal.ZERO);
			
			this.setImpuestoInterno(BigDecimal.ZERO);
		}
		
		this.sincronizarAtributosMoneda();
	}
	
	private void recalcularImportesActualizacionInventario(){
		this.calcularImportes();
		
		if (this.getIva().compareTo(BigDecimal.ZERO) != 0){
			this.setIva(this.getSubtotal().multiply(this.getTasaiva()).divide(new BigDecimal(100)).setScale(4, RoundingMode.HALF_EVEN));
		}
		if (this.getImpuestoInterno().compareTo(BigDecimal.ZERO) != 0){
			this.setImpuestoInterno(this.getSubtotal().multiply(this.getTasaImpuestoInterno()).divide(new BigDecimal(100)).setScale(4, RoundingMode.HALF_EVEN));
		}				
		this.sincronizarAtributosMoneda();
	}
	
	private void sincronizarAtributosMoneda(){
		if (this.getVenta() != null){
			List<String> atributos =new LinkedList<String>();
			atributos.add("Iva");
			atributos.add("ImpuestoInterno");
			atributos.add("Subtotal");
			this.getVenta().sincronizarMonedas(this, atributos);				
		}
	}
	
	public BigDecimal sumarIVA(BigDecimal importe){
		return importe.add(importe.multiply(this.getTasaiva().divide(new BigDecimal(100)))).setScale(4, RoundingMode.HALF_EVEN);		
	}
	
	@Override
	public Transaccion transaccion() {
		return this.getVenta();
	}

	@Override
	public CuentaContable igcCuentaContable() {
		return TipoCuentaContable.Ventas.CuentaContablePorTipo(this.getProducto());
	}

	@Override
	public BigDecimal igcHaberOriginal() {
		int comparacion = this.getVenta().CtaCteImporte().compareTo(BigDecimal.ZERO);
		if (comparacion > 0){
			return this.getSubtotal();
		}
		else if (comparacion < 0){
			return BigDecimal.ZERO;
		}
		else{
			return BigDecimal.ZERO;
		}
	}

	@Override
	public BigDecimal igcDebeOriginal() {
		int comparacion = this.getVenta().CtaCteImporte().compareTo(BigDecimal.ZERO);
		if (comparacion > 0){
			return BigDecimal.ZERO;
		}
		else if (comparacion < 0){
			return this.getSubtotal();
		}
		else{
			return BigDecimal.ZERO;
		}
	}

	@Override
	public CentroCostos igcCentroCostos() {
		CentroCostos cc = this.getCentroCostos();
		EstadisticaPedidoVenta itempv = this.getItemPedido();
		
		if(cc == null && itempv != null && itempv.getCentroCostos() != null){
			cc = itempv.getCentroCostos();
		}
		
		return cc != null ? cc: this.getProducto().getCentroCostos(); 
	}

	@Override
	public UnidadNegocio igcUnidadNegocio() {
		return null;
	}

	@Override
	public String igcDetalle() {
		return "";
	}

	public ItemRemito getItemRemito() {
		return itemRemito;
	}

	public void setItemRemito(ItemRemito itemRemito) {
		this.itemRemito = itemRemito;
	}

	public ItemLiquidacionConsignacion getItemLiquidacion() {
		return itemLiquidacion;
	}

	public void setItemLiquidacion(ItemLiquidacionConsignacion itemLiquidacion) {
		this.itemLiquidacion = itemLiquidacion;
		if (itemLiquidacion != null){
			this.setItemRemito(itemLiquidacion.getItemRemito());
		}
	}
	
	@Override
	public void copiarPropiedades(Object objeto){
		super.copiarPropiedades(objeto);
		this.setIdDiferenciaCambio(null);
		this.setItemLiquidacion(null);
		this.setItemRemito(null);
		this.setNovedadContrato(null);
		this.setAutomatico(Boolean.FALSE);
	}
	
	private void verificarPrecioUnitario(){
		if (!this.esInteres()){
			if (this.getVenta() != null){
				if (this.getVenta().verificarPrecioUnitario(this)){
					this.getVenta().asignarPrecioUnitario(this);
				}
			}
		}
	}
	
	public BigDecimal descontarIva(BigDecimal importe){
		BigDecimal tasaIva = this.getTasaiva();
		if (tasaIva == null){
			if (this.getProducto() != null){
				if (this.getVenta() != null){
					tasaIva = this.getVenta().alicuotaIva(this.getProducto());
				}
				else{
					tasaIva = this.getProducto().getTasaIva().getPorcentaje();
				}
			}
		}
		
		if (tasaIva != null){
			return importe.divide( (new BigDecimal(1)).add(tasaIva.divide(new BigDecimal(100), 16, RoundingMode.HALF_EVEN)), 2, RoundingMode.HALF_EVEN );
		}
		else{
			return importe;
		}
	}

	public EstadisticaPedidoVenta getItemPedido() {
		return itemPedido;
	}

	public void setItemPedido(EstadisticaPedidoVenta itemPedido) {
		this.itemPedido = itemPedido;
	}

	@Override
	public ITipoMovimientoInventario tipoMovimientoInventario(boolean reversion) {
		if (this.getVenta() instanceof IVentaInventario){
			return ((IVentaInventario)this.getVenta()).tipoMovimientoInventario(reversion);
		}
		else{
			return null;
		}
	}

	@Hidden
	@Override
	public Deposito getDeposito() {
		if (this.getVenta() instanceof IVentaInventario){
			return ((IVentaInventario)this.getVenta()).getDeposito();
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
			this.recalcularImportesActualizacionInventario();
		}
		else{
			throw new ValidationException("Difieren las unidades de medida");
		}		
	}
	
	@Override
	public void crearItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem){
		this.getVenta().getItems().add((ItemVentaElectronica)nuevoItem);
	}
	
	@Override
	public void posActualizarItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem) {		
	}
	
	public void calcularPrecioUnitarioSegunImporteTotal(BigDecimal importe){
		Boolean calcularIVA = true;
		BigDecimal porcentajeIva = null;
		if (this.getVenta() != null){
			calcularIVA = this.getVenta().calculaImpuestos();
			if (calcularIVA){
				calcularIVA = this.getVenta().getCliente().getPosicionIva().calculaIvaVentas();
				porcentajeIva = this.getVenta().alicuotaIva(this.getProducto());
			}
			
		}
		else{
			porcentajeIva = this.getProducto().getTasaIva().getPorcentaje();
		}
				
		BigDecimal precio = importe;
		if (calcularIVA){			
			BigDecimal alicuotaIva = porcentajeIva.divide(new BigDecimal(100));
			if (alicuotaIva.compareTo(BigDecimal.ZERO) != 0){
				precio = importe.divide((new BigDecimal(1)).add(alicuotaIva), 8, RoundingMode.HALF_EVEN);
			}
		}
		
		// se utiliza 4 para evitar problema de redondeo
		// TODO: si hay diferencia por redondeo en el total, habría que agregar un item con un concepto de redondeo
		// Ejemplo: cantidad 4, precio unitario con iva incluido 50
		int digitosRedondeo = 4;
		if (this.getCantidad().compareTo(BigDecimal.ZERO) > 0){
			precio = precio.divide(this.getCantidad(), digitosRedondeo, RoundingMode.HALF_EVEN);
		}
		else{
			precio = precio.setScale(digitosRedondeo, RoundingMode.HALF_EVEN);
		}
		this.setPrecioUnitario(precio);
	}

	public void agregarPasesContables(Collection<IGeneradorItemContable> items) {
		items.add(this);
		if (this.getImpuestoInterno1().compareTo(BigDecimal.ZERO) > 0){
			items.add(this.generarPaseContableImpuestoInterno());
		}
	}
	
	private IGeneradorItemContable generarPaseContableImpuestoInterno(){		
		Impuesto imp = this.getProducto().getImpuestoInterno();
		if (imp != null){
			CuentaContable cuenta = TipoCuentaContable.Impuesto.CuentaContablePorTipo(imp);
			GeneradorItemContablePorTr pase = new GeneradorItemContablePorTr(this.getVenta(), cuenta);
			int comparacion = this.getVenta().CtaCteImporte().compareTo(BigDecimal.ZERO);
			if (comparacion > 0){
				pase.setHaber(this.getImpuestoInterno1());
			}
			else{
				pase.setDebe(this.getImpuestoInterno1());
			}
			return pase;
		}
		else{
			throw new ValidationException("El producto " + this.getProducto().toString() + " no tiene asignado el impuesto interno, pero ha sido calculado el impuesto");
		}
		
		
	}

	public Boolean getCumplido() {
		return cumplido;
	}

	public void setCumplido(Boolean cumplido) {
		if (cumplido == null){
			this.cumplido = Boolean.FALSE;
		}
		else{
			this.cumplido = cumplido;
		}
	}

	public IItemPendiente itemPendienteRemitoProxy() {
		ItemPendienteRemitoProxy itemPendiente = null;
		if (this.getProducto().getTipo().equals(TipoProducto.Producto)){
			if (this.getVenta().generaRemito()){				
				itemPendiente = new ItemPendienteRemitoProxy();
				itemPendiente.setItemVentaElectronica(this);
			}
		}
		return itemPendiente;		
	}
	
	public void validarStockDisponible(){
		if ((this.getProducto() != null) && (this.getVenta() != null)){
			if (this.getVenta() instanceof IVentaInventario){
				if (this.getCantidad().compareTo(BigDecimal.ZERO) > 0){
					IVentaInventario ventaInv = (IVentaInventario)this.getVenta(); 
					if (ventaInv.validarStockDisponible() && this.getProducto().getTipo().stock()){
						if (ventaInv.getDeposito() != null){
							if (this.getProducto().usaAtributoInventario() || Esquemas.getEsquemaApp().getStockObligatorio()){
								BigDecimal disponible = ventaInv.getDeposito().stockDisponible(this);
								BigDecimal faltan = this.getCantidad().subtract(disponible);
								if (faltan.compareTo(BigDecimal.ZERO) > 0){
									DecimalFormat format = new DecimalFormat("#,###.##");
									throw new ValidationException(this.getProducto().getCodigo() + " faltan " + format.format(faltan));
								}
							}
						}
						else{
							throw new ValidationException("Depósito no asignado");
						}
					}
				}
			}
		}
	}

	public String getDetalle() {
		return detalle;
	}

	public void setDetalle(String detalle) {
		this.detalle = detalle;
	}

	public NovedadContrato getNovedadContrato() {
		return novedadContrato;
	}

	public void setNovedadContrato(NovedadContrato novedadContrato) {
		this.novedadContrato = novedadContrato;
	}

	private Boolean automatico = Boolean.FALSE;
	
	public Boolean getAutomatico() {
		return automatico == null ? Boolean.FALSE : automatico;
	}

	public void setAutomatico(Boolean automatico) {
		if (automatico != null){
			this.automatico = automatico;
		}		
	}

	public boolean esInteres() {
		if (this.getAutomatico() && this.getProducto() != null && this.getProducto().getTipo().equals(TipoProducto.Interes)){
			return true;
		}
		else{
			return false;
		}
	}

	// no se persiste porque no interesa el control de código de barras, solo para dar de alta
	@Transient
	@Hidden
	private BigDecimal controlado = BigDecimal.ZERO;

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


	@Override
	public boolean crearEntidadesPorControl() {
		return false;
	}

	@Override
	public BigDecimal convertirUnidadesLeidas(BigDecimal cantidadLeida) {
		return cantidadLeida;
	}
}
