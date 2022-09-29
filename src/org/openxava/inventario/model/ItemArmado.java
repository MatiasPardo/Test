package org.openxava.inventario.model;

import java.math.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.base.calculators.*;
import org.openxava.base.model.*;
import org.openxava.negocio.actions.*;
import org.openxava.negocio.model.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

@Entity

@Views({
	@View(members=
		"producto;" +
		"unidadMedida;" +
		"cantidad;" +
		"despacho;" + 
		"lote;" + 
		"costoUnitario, costoTotal;"
	)	
})

public class ItemArmado extends ItemTransaccion implements IItemMovimientoInventario{

	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private Armado armado;
	
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
	
	@Required
	@Min(value=0, message="No puede ser negativo")
	@DefaultValueCalculator(  
			value=SinAsignarCalculator.class,
			properties={@PropertyValue(name="id", from="producto.id")}
		)	
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
	private BigDecimal costoUnitario = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal costoTotal = BigDecimal.ZERO;
	
	@ReadOnly
	@Hidden
	private BigDecimal equivalencia = new BigDecimal(1);
	
	public Armado getArmado() {
		return armado;
	}

	public void setArmado(Armado armado) {
		this.armado = armado;
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
		return cantidad;
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
	
	@Override
	public Transaccion transaccion() {
		return this.getArmado();
	}

	@Override
	public void recalcular() {
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
		
		if (this.getArmado() != null){
			ListaPrecio listaCostos = this.getArmado().getListaCostos();
			if ((listaCostos != null) && (this.getProducto() != null)){
				BigDecimal cantidad = this.getCantidad().multiply(this.getEquivalencia());
				BigDecimal costo = listaCostos.buscarPrecio(this.getProducto().getId(), this.getUnidadMedida().getId(), cantidad);
				if(costo == null) costo = BigDecimal.ZERO;
				// La lista de costos se considera en moneda2.			
				BigDecimal cotizacion = this.getArmado().buscarCotizacionTrConRespectoA(this.getArmado().getMoneda2());				
				this.setCostoUnitario(costo.divide(cotizacion, 2, RoundingMode.HALF_EVEN));
				this.setCostoTotal(this.getCostoUnitario().multiply(cantidad));
			}
		}		
	}

	@Override
	public ITipoMovimientoInventario tipoMovimientoInventario(boolean reversion) {
		if (!reversion){
			return new TipoMovInvEgreso();
		}
		else{
			return new TipoMovInvIngreso();
		}
	}

	@Override
	public Deposito getDeposito() {
		if (this.getArmado() != null){
			return this.getArmado().getDeposito();
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

	public BigDecimal getCostoUnitario() {
		return costoUnitario == null ? BigDecimal.ZERO : this.costoUnitario;
	}

	public void setCostoUnitario(BigDecimal costoUnitario) {
		this.costoUnitario = costoUnitario;
	}

	public BigDecimal getCostoTotal() {
		return costoTotal == null ? BigDecimal.ZERO : this.costoTotal;
	}

	public void setCostoTotal(BigDecimal costoTotal) {
		this.costoTotal = costoTotal;
	}

	public BigDecimal getEquivalencia() {
		return equivalencia == null ? new BigDecimal(1) : this.equivalencia;
	}

	public void setEquivalencia(BigDecimal equivalencia) {
		this.equivalencia = equivalencia;
	}
	
	public void crearItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem){
	}
	
	@Override
	public void posActualizarItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem) {		
	}
}
