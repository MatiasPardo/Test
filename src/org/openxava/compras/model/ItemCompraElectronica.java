package org.openxava.compras.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.base.calculators.*;
import org.openxava.base.model.*;
import org.openxava.compras.actions.*;
import org.openxava.compras.validators.ItemCompraElectronicaValidator;
import org.openxava.contabilidad.model.*;
import org.openxava.impuestos.model.*;
import org.openxava.inventario.model.ItemRecepcionMercaderia;
import org.openxava.jpa.XPersistence;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.ventas.model.*;

@Entity

@Views({
	@View(
		members="producto;" + 
				"unidadMedida;" + 
				"cantidad;" + 
				"precioUnitario, porcentajeDescuento;" +
				"detalle;" +
				"descuento, suma;" + 
				"alicuotaIva, iva;" + 
				"centroCostos;"),
	@View(name="FacturaCompra",
		members="producto;" + 
			"unidadMedida;" + 
			"cantidad, precioUnitario, porcentajeDescuento;" +
			"detalle;" +
			"descuento, suma;" + 
			"alicuotaIva, iva;" + 
			"cantidadExcede, pendienteFacturacion;" +
			"centroCostos;")
})

@EntityValidators({
	@EntityValidator(value=ItemCompraElectronicaValidator.class, 
			properties= {
					@PropertyValue(name="transaccion", from="compra"), 
					@PropertyValue(name="cantidad", from="cantidad"),
					@PropertyValue(name="cantidadExcede", from="cantidadExcede"),
					@PropertyValue(name="pendienteFacturar", from="pendienteFacturacion"),
					@PropertyValue(name="unidadMedida", from="unidadMedida"),
					@PropertyValue(name="producto", from="producto")
				}
	)
})

@Tab(properties="compra.fecha, compra.tipoOperacion, compra.numero, compra.proveedor.nombre, producto.codigo, producto.nombre, cantidad, precioUnitario, descuento, suma, usuario, fechaCreacion", 
	defaultOrder="${fechaCreacion} desc")

public class ItemCompraElectronica extends ItemTransaccion implements IGeneradorItemContable{

	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private CompraElectronica compra;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY) 
	@ReferenceView("Simple")
	@NoFrame
	@NoCreate @NoModify
	@OnChange(OnChangeProductoItemCompraElectronica.class)
	@SearchListCondition(value="${compras} = 't'")
	private Producto producto;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@ReadOnly
	private UnidadMedida unidadMedida;
	
	@ReadOnly
	@Hidden
	private BigDecimal equivalencia = new BigDecimal(1);
	
	@Required
	@Min(value=0, message="No puede ser negativo")
	@DefaultValueCalculator(  
			value=SinAsignarCalculator.class,
			properties={@PropertyValue(name="id", from="producto.id")}
		)
	@OnChange(OnChangeCantidadItemCompraElectronica.class)
	private BigDecimal cantidad = BigDecimal.ZERO;
	
	@OnChange(OnChangeCantidadItemCompraElectronica.class)
	private BigDecimal precioUnitario = BigDecimal.ZERO;
	
	@Min(value=0, message="No puede menor a 0")
	@Max(value=100, message="No puede ser mayor a 100")
	@OnChange(OnChangeCantidadItemCompraElectronica.class)
	private BigDecimal porcentajeDescuento = BigDecimal.ZERO;
	
	@ReadOnly
	@Digits(integer=19, fraction=4)	
	private BigDecimal descuento = BigDecimal.ZERO;
	
	@ReadOnly
	@Digits(integer=19, fraction=4)
	private BigDecimal suma = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal suma1 = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal suma2 = BigDecimal.ZERO;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="descripcion")
	@NoCreate @NoModify
	private TasaImpuesto alicuotaIva; 
	
	/*@DefaultValueCalculator(  
			value=TasaIvaCalculator.class,
			properties={@PropertyValue(name="productoID", from="producto.id")}
		)*/	
	private BigDecimal tasaiva = BigDecimal.ZERO;
	
	@Column(length=50)
	private String detalle;
	
	@Min(value=0, message="No puede ser negativo")
	private BigDecimal cantidadExcede = BigDecimal.ZERO;
	
	@OneToMany(mappedBy="itemFactura", cascade=CascadeType.ALL)
	@ReadOnly
	@ListProperties("itemRecepcion.cantidad, itemRecepcion.pendienteFacturacion, cantidad, itemRecepcion.serie.codigo")	
	private Collection<ItemRecepcionFacturaCompra> recepciones;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="codigo, nombre", forTabs="ninguno")
	private CentroCostos centroCostos;
	
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

	public BigDecimal getEquivalencia() {
		return equivalencia  == null ? new BigDecimal(1) : equivalencia;
	}

	public void setEquivalencia(BigDecimal equivalencia) {
		this.equivalencia = equivalencia;
	}

	public BigDecimal getCantidad() {
		return cantidad == null ? BigDecimal.ZERO : cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}

	public BigDecimal getPrecioUnitario() {
		return precioUnitario == null ? BigDecimal.ZERO : precioUnitario;
	}

	public void setPrecioUnitario(BigDecimal precioUnitario) {
		this.precioUnitario = precioUnitario;
	}

	public BigDecimal getSuma() {
		return suma == null ? BigDecimal.ZERO : suma;
	}

	public void setSuma(BigDecimal suma) {
		this.suma = suma;
	}

	@Digits(integer=19, fraction=4)	
	private BigDecimal iva = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal iva1 = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal iva2 = BigDecimal.ZERO;
	
	public BigDecimal getSuma1() {
		return suma1 == null ? BigDecimal.ZERO : suma1;
	}

	public void setSuma1(BigDecimal suma1) {
		this.suma1 = suma1;
	}

	public BigDecimal getSuma2() {
		return suma2 == null ? BigDecimal.ZERO : suma2;
	}

	public void setSuma2(BigDecimal suma2) {
		this.suma2 = suma2;
	}

	public BigDecimal getTasaiva() {
		return tasaiva == null ? BigDecimal.ZERO : tasaiva;
	}

	public void setTasaiva(BigDecimal tasaiva) {
		if (tasaiva == null){
			this.tasaiva = BigDecimal.ZERO;
		}
		else{
			this.tasaiva = tasaiva;
		}
	}
	
	public BigDecimal getIva() {
		return iva == null ? BigDecimal.ZERO : iva;
	}

	public void setIva(BigDecimal iva) {
		this.iva = iva;
	}

	public CompraElectronica getCompra() {
		return compra;
	}

	public void setCompra(CompraElectronica compra) {
		this.compra = compra;
	}
	
	public BigDecimal getCantidadExcede() {
		return cantidadExcede == null ? BigDecimal.ZERO : this.cantidadExcede;
	}

	public void setCantidadExcede(BigDecimal cantidadExcede) {
		if (cantidadExcede != null){
			this.cantidadExcede = cantidadExcede;
		}
		else{
			this.cantidadExcede = BigDecimal.ZERO;
		}
	}

	public Collection<ItemRecepcionFacturaCompra> getRecepciones() {
		if (recepciones == null){
			recepciones = new ArrayList<ItemRecepcionFacturaCompra>();
		}
		return recepciones;
	}

	public void setRecepciones(Collection<ItemRecepcionFacturaCompra> recepciones) {
		this.recepciones = recepciones;
	}
	
	@Override
	public Transaccion transaccion() {
		return this.getCompra();
	}

	@Override
	public void recalcular() {
		if (this.getProducto() != null){
			if (this.getUnidadMedida() == null){
				this.setUnidadMedida(this.getProducto().getUnidadMedida());
			}
		}
		BigDecimal precioPorCantidad = this.getPrecioUnitario().multiply(this.getCantidad());
		this.setDescuento(precioPorCantidad.multiply(this.getPorcentajeDescuento()).divide(new BigDecimal(100)).setScale(4, RoundingMode.HALF_EVEN).negate());
		
		this.setSuma(precioPorCantidad.add(this.getDescuento()).setScale(4, RoundingMode.HALF_EVEN));
		
		boolean calculaImpuestos = true;
		if (this.getCompra() != null){
			calculaImpuestos = this.getCompra().calcularImpuestos();
		}
		
		if (this.getProducto() != null){
			if (this.getAlicuotaIva() == null){
				this.setAlicuotaIva(this.getProducto().getTasaIva());
			}
			this.setTasaiva(this.getAlicuotaIva().getPorcentaje());
		}
		
		BigDecimal ivaCalculado = BigDecimal.ZERO;
		if (calculaImpuestos){			
			ivaCalculado = (this.getSuma().multiply(this.getTasaiva()).divide(new BigDecimal(100)).setScale(4, RoundingMode.HALF_EVEN));
		}
		else{
			this.setTasaiva(BigDecimal.ZERO);
		}
		if (this.getCompra() != null){
			this.setIva(this.getCompra().sincronizarImporteCalculado(this.getIva(), ivaCalculado));
			List<String> atributos = new LinkedList<String>();
			atributosConversionesMonedaItemCompra(atributos);
			this.getCompra().sincronizarMonedas(this, atributos);
		}
		else{
			this.setIva(ivaCalculado);
		}
		
		this.distribuirCantidadesRecepciones();
	}
	
	public boolean fusionarA(ItemCompraElectronica itemCompra) {
		boolean fusion = false;
		if (Is.equal(itemCompra.getProducto(), this.getProducto()) && 
			(Is.equal(itemCompra.getCompra(), this.getCompra()))	){
			if (itemCompra.getPrecioUnitario().compareTo(this.getPrecioUnitario()) == 0){
				if (Is.equal(itemCompra.getUnidadMedida(), this.getUnidadMedida())){
					if (this.fusionaPorCentroCosto(itemCompra)){
						itemCompra.setCantidad(itemCompra.getCantidad().add(this.getCantidad()));
						itemCompra.agregarRecepciones(this.getRecepciones());
						itemCompra.recalcular();
						fusion = true;
					}
				}
			}
		}
		return fusion;
	}

	private boolean fusionaPorCentroCosto(ItemCompraElectronica itemCompra){
		return Is.equal(this.igcCentroCostos(), itemCompra.igcCentroCostos());
	}
		
	@Override
	public CuentaContable igcCuentaContable() {
		return TipoCuentaContable.Compras.CuentaContablePorTipo(this.getProducto());
	}

	@Override
	public BigDecimal igcHaberOriginal() {
		int comparacion = this.getCompra().CtaCteImporte().compareTo(BigDecimal.ZERO);
		if (comparacion < 0){
			return this.getSuma();
		}
		else{
			return BigDecimal.ZERO;
		}
	}

	@Override
	public BigDecimal igcDebeOriginal() {
		int comparacion = this.getCompra().CtaCteImporte().compareTo(BigDecimal.ZERO);
		if (comparacion > 0){
			return this.getSuma();
		}
		else{
			return BigDecimal.ZERO;
		}
	}

	@Override
	public CentroCostos igcCentroCostos() {
		// Primero: el centro de costo que pudo asignar el usuario
		CentroCostos cc = this.getCentroCostos();
		if (cc == null){
			// Segundo se revisa las recepciones de mercadería asociadas al item
			if (this.getRecepciones() != null){
				for(ItemRecepcionFacturaCompra item: this.getRecepciones()){
					if (item.getItemRecepcion() != null){
						if (item.getItemRecepcion().getItemOrdenCompra() != null){
							cc = item.getItemRecepcion().getItemOrdenCompra().getCentroCostos();
						}
					}
					break;
				}
			}
		}
				
		if (cc == null){
			// tercero se evalua el producto.
			cc = this.getProducto().getCentroCostos();
		}
		return cc;
	}

	@Override
	public UnidadNegocio igcUnidadNegocio() {
		return null;
	}

	@Override
	public String igcDetalle() {
		return "";
	}
	
	protected void atributosConversionesMonedaItemCompra(List<String> atributos){
		 atributos.add("Suma");
		 atributos.add("Iva");
	}
	
	public BigDecimal getPorcentajeDescuento() {
		return this.porcentajeDescuento == null? BigDecimal.ZERO: this.porcentajeDescuento;
	}

	public void setPorcentajeDescuento(BigDecimal porcentajeDescuento) {
		if (porcentajeDescuento == null){
			this.porcentajeDescuento = BigDecimal.ZERO;
		}
		else{
			this.porcentajeDescuento = porcentajeDescuento;
		}
	}
	
	public BigDecimal getDescuento() {
		return this.descuento == null? BigDecimal.ZERO: this.descuento;
	}

	public void setDescuento(BigDecimal descuento) {
		this.descuento = descuento;
	}

	public TasaImpuesto getAlicuotaIva() {
		return alicuotaIva;
	}

	public void setAlicuotaIva(TasaImpuesto alicuotaIva) {
		this.alicuotaIva = alicuotaIva;
	}

	public String getDetalle() {
		return detalle;
	}

	public void setDetalle(String detalle) {
		this.detalle = detalle;
	}

	public BigDecimal getIva1() {
		return iva1;
	}

	public void setIva1(BigDecimal iva1) {
		this.iva1 = iva1;
	}

	public BigDecimal getIva2() {
		return iva2;
	}

	public void setIva2(BigDecimal iva2) {
		this.iva2 = iva2;
	}
	
	@Hidden
	public BigDecimal getPendienteFacturacion(){
		BigDecimal cantidadPendiente = BigDecimal.ZERO;
		for(ItemRecepcionFacturaCompra itemRecepcionFactura: this.getRecepciones()){
			ItemRecepcionMercaderia itemRecepcion = itemRecepcionFactura.getItemRecepcion();
			Cantidad pendienteFacturacion = new Cantidad();
			pendienteFacturacion.setCantidad(itemRecepcion.getPendienteFacturacion());
			pendienteFacturacion.setUnidadMedida(itemRecepcion.getUnidadMedida());
			cantidadPendiente = cantidadPendiente.add(pendienteFacturacion.convertir(this.getUnidadMedida()));
		}
		return cantidadPendiente;
	}
	
	public ItemRecepcionFacturaCompra agregarRecepcion(ItemRecepcionMercaderia itemRecepcion){
		ItemRecepcionFacturaCompra item = new ItemRecepcionFacturaCompra();
		item.setItemFactura(this);
		item.setItemRecepcion(itemRecepcion);				
		this.getRecepciones().add(item);
		return item;
	}
	
	public void quitarRecepcion(ItemRecepcionFacturaCompra item){
		if (!item.esNuevo()){
			XPersistence.getManager().remove(item);
		}
	}
	
	private void agregarRecepciones(Collection<ItemRecepcionFacturaCompra> items){
		for(ItemRecepcionFacturaCompra item: items){
			item.setItemFactura(this);
			this.getRecepciones().add(item);
		}
	}
	
	public BigDecimal cantidadTotalACancelar(){
		return getCantidad().subtract(this.getCantidadExcede());
	}
	
	private void distribuirCantidadesRecepciones(){
		// TODO: para ser genérico, el item de factura de compra debería tener una collection de una entidad que tenga
		// ItemCompraElectronica, ItemRecepcionMercaderia y CantidadCancelada.
		// Las unidades deberían ser convertidas de la unidad de recepción de mercadería a la unidad de medida de los la factura
		BigDecimal cantidadTotalACancelar = this.cantidadTotalACancelar();		
		List<ItemRecepcionFacturaCompra> itemsCancelacion = new LinkedList<ItemRecepcionFacturaCompra>();
		itemsCancelacion.addAll(this.getRecepciones());
		for(ItemRecepcionFacturaCompra itemCancelacion: itemsCancelacion){
			if (cantidadTotalACancelar.compareTo(BigDecimal.ZERO) > 0){
				
				BigDecimal cantidadACancelarItem = itemCancelacion.getItemRecepcion().getPendienteFacturacion();
				cantidadACancelarItem = itemCancelacion.getItemRecepcion().getUnidadMedida().convertir(cantidadACancelarItem, this.getUnidadMedida());
				if (cantidadACancelarItem.compareTo(cantidadTotalACancelar) > 0){
					cantidadACancelarItem = cantidadTotalACancelar;
				}
				itemCancelacion.setCancelar(cantidadACancelarItem);				
				cantidadTotalACancelar = cantidadTotalACancelar.subtract(cantidadACancelarItem);
			}
			else{
				itemCancelacion.setCancelar(BigDecimal.ZERO);
			}
		}					
	}

	public CentroCostos getCentroCostos() {
		return centroCostos;
	}

	public void setCentroCostos(CentroCostos centroCostos) {
		this.centroCostos = centroCostos;
	}
}
