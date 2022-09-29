package org.openxava.compras.model;

import java.math.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.base.calculators.*;
import org.openxava.base.model.*;
import org.openxava.compras.actions.*;
import org.openxava.impuestos.model.TasaImpuesto;
import org.openxava.negocio.model.*;
import org.openxava.ventas.model.*;

@MappedSuperclass

public abstract class ItemCompra extends ItemTransaccion{

	@ManyToOne(optional=false, fetch=FetchType.LAZY) 
	@ReferenceView("Simple")
	@NoFrame
	@NoCreate @NoModify
	@OnChange(OnChangeProductoItemCompra.class)
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
	@OnChange(OnChangeCantidadItemCompra.class)
	private BigDecimal cantidad = BigDecimal.ZERO;
	
	@OnChange(OnChangeCantidadItemCompra.class)
	private BigDecimal precioUnitario = null;
	
	@Min(value=0, message="No puede menor a 0")
	@Max(value=100, message="No puede ser mayor a 100")
	@OnChange(OnChangeCantidadItemCompra.class)
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
	
	@ReadOnly
	/*@DefaultValueCalculator(  
			value=TasaIvaCalculator.class,
			properties={@PropertyValue(name="productoID", from="producto.id")}
		)*/
	private BigDecimal tasaiva = BigDecimal.ZERO;
	
	@Column(length=50)
	private String detalle;
	
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

	public TasaImpuesto getAlicuotaIva() {
		return alicuotaIva;
	}

	public void setAlicuotaIva(TasaImpuesto alicuotaIva) {
		this.alicuotaIva = alicuotaIva;
	}

	@Digits(integer=19, fraction=4)
	/*@DefaultValueCalculator(  
			value=IVACalculator.class,
			properties={@PropertyValue(name="tasa", from="tasaiva"),
						@PropertyValue(name="importe", from="subtotal")}
		)*/
	@ReadOnly
	private BigDecimal iva = BigDecimal.ZERO;
	
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

	@Override
	public abstract Transaccion transaccion();

	private ListaPrecio buscarCostoUnitarioEnLista(){
		// Si la transacción tiene lista de precio se calcula siempre el costo unitario
		ListaPrecio listaCostos = null;
		if (this.transaccion() != null){
			listaCostos = ((Compra)this.transaccion()).getListaPrecio();
		}		
		if (listaCostos == null){
			// no hay lista de costos, se busca en la principal únicamente si el precioUnitario no esta asignado.
			// Porque si el precioUnitario esta asignado no se recalcula el costo
			if (this.precioUnitario == null){
				listaCostos = ListaPrecio.buscarListaPrecioPrincipal(Boolean.TRUE);
			}
		}
		return listaCostos;					
	}
	
	@Override
	public void recalcular() {
		if (this.getProducto() != null){
			if (this.getUnidadMedida() == null){
				this.setUnidadMedida(this.getProducto().getUnidadMedida());
			}
			if (this.getAlicuotaIva() == null){
				this.setAlicuotaIva(this.getProducto().getTasaIva());
			}
			this.setTasaiva(this.getAlicuotaIva().getPorcentaje());
						
			ListaPrecio listaCosto = buscarCostoUnitarioEnLista();
			if (listaCosto != null){
				BigDecimal costoUnitario = BigDecimal.ZERO;
				Precio precio = listaCosto.buscarObjetoPrecio(this.getProducto().getId(), this.getUnidadMedida().getId(), cantidad);
				if (precio != null){
					costoUnitario = this.transaccion().convertirImporteEnMonedaTr(listaCosto.getMoneda(), precio.getCosto());
				}
				this.setPrecioUnitario(costoUnitario);
			}
		}
		BigDecimal precioPorCantidad = this.getPrecioUnitario().multiply(this.getCantidad());
		this.setDescuento(precioPorCantidad.multiply(this.getPorcentajeDescuento()).divide(new BigDecimal(100)).setScale(4, RoundingMode.HALF_EVEN).negate());
		this.setSuma(precioPorCantidad.add(this.getDescuento()).setScale(4, RoundingMode.HALF_EVEN));
		
		boolean calculaImpuestos = true;
		if (this.transaccion() != null){
			calculaImpuestos = ((Compra)this.transaccion()).calcularImpuestos();
		}
		if (calculaImpuestos){
			if (this.getProducto() != null){
				this.setTasaiva(this.getProducto().getTasaIva().getPorcentaje());
			}
			this.setIva(this.getSuma().multiply(this.getTasaiva()).divide(new BigDecimal(100)).setScale(4, RoundingMode.HALF_EVEN));
		}
		else{
			this.setIva(BigDecimal.ZERO);
		}		
	}
		
	@Override
	public void onPreCreate(){
		super.onPreCreate();
		if (this.getUnidadMedida() == null){
			if (this.getProducto() != null){
				this.setUnidadMedida(this.getProducto().getUnidadMedida());
			}
		}
		if ((this.getUnidadMedida() != null) && (this.getProducto() != null)){
			if (!this.getUnidadMedida().equals(this.getProducto().getUnidadMedida())){
				this.setEquivalencia(this.getUnidadMedida().convertir(new BigDecimal(1), this.getProducto().getUnidadMedida()));
			}
		}
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

	public String getDetalle() {
		return detalle;
	}

	public void setDetalle(String detalle) {
		this.detalle = detalle;
	}
}
