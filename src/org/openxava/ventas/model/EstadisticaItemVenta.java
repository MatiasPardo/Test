package org.openxava.ventas.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.base.calculators.*;
import org.openxava.base.model.*;
import org.openxava.negocio.model.*;
import org.openxava.validators.*;
import org.openxava.ventas.actions.*;
import org.openxava.ventas.calculators.*;

@MappedSuperclass

public class EstadisticaItemVenta extends ItemTransaccion{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY) 
	@ReferenceView("Simple")
	@NoFrame
	@NoCreate @NoModify
	@OnChange(OnChangeProductoItemVenta.class)
	@SearchListCondition(value="${ventas} = 't'")
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
	@OnChange(OnChangeCantidadItemVenta.class)
	private BigDecimal cantidad = BigDecimal.ZERO;
	
	@OnChange(OnChangeCantidadItemVenta.class)
	private BigDecimal precioUnitario = BigDecimal.ZERO;
	
	@Min(value=0, message="No puede ser menor a 0")
	@Max(value=100, message="No puede ser mayor a 100")
	@DefaultValueCalculator(  
			value=CantidadCalculator.class,
			properties={@PropertyValue(name="productoID", from="producto.id")}
		)
	@OnChange(OnChangeCantidadItemVenta.class)
	private BigDecimal porcentajeDescuento = BigDecimal.ZERO;
	
	@ReadOnly
	@DefaultValueCalculator(
			value=DescuentoItemVentaCalculator.class,
			properties={@PropertyValue(name="cantidad", from="cantidad"),
						@PropertyValue(name="precioUnitario", from="precioUnitario"),
						@PropertyValue(name="porcentajeDescuento", from="porcentajeDescuento")})
	private BigDecimal descuento = BigDecimal.ZERO;
	
	@ReadOnly
	@OnChange(OnChangeSumaItemVenta.class)
	private BigDecimal suma = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal descuentoGlobal = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal subtotal = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal subtotal1 = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal subtotal2 = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal suma1 = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal suma2 = BigDecimal.ZERO;
	
	@ReadOnly
	@DefaultValueCalculator(  
			value=TasaIvaCalculator.class,
			properties={@PropertyValue(name="productoID", from="producto.id")}
		)
	private BigDecimal tasaiva = BigDecimal.ZERO;

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

	public BigDecimal getPrecioUnitario() {
		return precioUnitario == null ? BigDecimal.ZERO : this.precioUnitario;
	}

	public void setPrecioUnitario(BigDecimal precioUnitario) {
		this.precioUnitario = precioUnitario;
	}

	public BigDecimal getPorcentajeDescuento() {
		return porcentajeDescuento == null ? BigDecimal.ZERO : this.porcentajeDescuento;
	}

	public void setPorcentajeDescuento(BigDecimal porcentajeDescuento) {
		if (porcentajeDescuento != null){
			this.porcentajeDescuento = porcentajeDescuento;
		}
		else{
			this.porcentajeDescuento = BigDecimal.ZERO;
		}
	}

	public BigDecimal getDescuento() {
		return descuento;
	}

	public void setDescuento(BigDecimal descuento) {
		this.descuento = descuento;
	}

	@Hidden
	public BigDecimal getImporteSinDescuento(){
		return this.getPrecioUnitario().multiply(this.getCantidad());
	}
	
	public BigDecimal getSuma() {
		return suma == null ? BigDecimal.ZERO : this.suma;
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

	public BigDecimal getEquivalencia() {
		return equivalencia;
	}

	public void setEquivalencia(BigDecimal equivalencia) {
		this.equivalencia = equivalencia;
	}

	@Override
	public Boolean soloLectura(){
		return Boolean.TRUE;
	}
	
	@Override
	public void onPreCreate(){
		super.onPreCreate();		
		if ((this.getUnidadMedida() != null) && (this.getProducto() != null)){
			if (!this.getUnidadMedida().equals(this.getProducto().getUnidadMedida())){
				this.setEquivalencia(this.getUnidadMedida().convertir(new BigDecimal(1), this.getProducto().getUnidadMedida()));
			}
		}
	}

	@Override
	public Transaccion transaccion() {
		throw new ValidationException("Implementar método transaccion");
	}

	@Override
	public void recalcular() {
		if ((this.getProducto() != null) && (this.transaccion() != null)){
			if (this.getUnidadMedida() == null){
				this.setUnidadMedida(this.getProducto().getUnidadMedida());			
			}		
			this.setTasaiva(this.getProducto().getTasaIva().getPorcentaje());
			Venta venta = (Venta)this.transaccion();
			if ((venta.getCliente()!= null) && (this.getCantidad().compareTo(BigDecimal.ZERO) != 0)){
				BigDecimal precio = venta.getCliente().calcularPrecio(venta.getListaPrecio(), this.getProducto(), this.getUnidadMedida(), this.getCantidad(), venta);
				// Solo si hay precio en la lista se asigna, si no hay, prevalece el precio asignado por el usuario
				if (precio != null){
					this.setPrecioUnitario(precio);
				}
				
				BigDecimal sumaSinDescuento = this.getImporteSinDescuento();
				this.setDescuento(sumaSinDescuento.multiply(this.getPorcentajeDescuento().divide(new BigDecimal(100))).negate());
				this.setSuma(sumaSinDescuento.add(this.getDescuento()));				
				this.setDescuentoGlobal(this.getSuma().multiply(venta.getPorcentajeDescuento().divide(new BigDecimal(100))).negate());				
				this.setSubtotal(this.getSuma().add(this.getDescuentoGlobal()));
			}
			
			
			List<String> atributos =new LinkedList<String>();
			atributos.add("Suma");
			atributos.add("Subtotal");
			this.transaccion().sincronizarMonedas(this, atributos);
		}		
	}

	public BigDecimal iva() {
		return this.getSubtotal().multiply(this.getTasaiva());
	}

	public BigDecimal getDescuentoGlobal() {
		return descuentoGlobal == null ? BigDecimal.ZERO : descuentoGlobal;
	}

	public void setDescuentoGlobal(BigDecimal descuentoGlobal) {
		this.descuentoGlobal = descuentoGlobal;
	}

	public BigDecimal getSubtotal() {
		return subtotal == null ? BigDecimal.ZERO : subtotal;
	}

	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}

	public BigDecimal getSubtotal1() {
		return subtotal1 == null ? BigDecimal.ZERO : subtotal1;
	}

	public void setSubtotal1(BigDecimal subtotal1) {
		this.subtotal1 = subtotal1;
	}

	public BigDecimal getSubtotal2() {
		return subtotal2 == null ? BigDecimal.ZERO : subtotal2;
	}

	public void setSubtotal2(BigDecimal subtotal2) {
		this.subtotal2 = subtotal2;
	}
	
	public void propiedadesSoloLecturaAlEditar(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		super.propiedadesSoloLecturaAlEditar(propiedadesSoloLectura, propiedadesEditables, configuracion);
		
		boolean tienePrecio = false;
		if ((this.getProducto() != null) && (this.transaccion() != null)){
			Venta venta = (Venta)this.transaccion();
			BigDecimal precio = venta.getCliente().calcularPrecio(venta.getListaPrecio(), this.getProducto(), this.getUnidadMedida(), this.getCantidad(), venta);
			if (precio != null){
				tienePrecio = true;
			}
		}
		
		if (tienePrecio){
			propiedadesSoloLectura.add("precioUnitario");
		}
		else{
			propiedadesEditables.add("precioUnitario");
		}
	}
	
}
