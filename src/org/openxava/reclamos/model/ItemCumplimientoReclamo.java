package org.openxava.reclamos.model;

import java.math.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.inventario.model.*;
import org.openxava.negocio.actions.*;
import org.openxava.negocio.model.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

@Entity

@Views({
	@View(members="producto; unidadMedida, cantidad, detalle"),
	@View(name="Simple", members="producto, unidadMedida")
})

public class ItemCumplimientoReclamo extends ItemTransaccion implements IItemMovimientoInventario {
	
	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	@ReferenceView("Simple")
	@OnChange(OnChangeProducto.class)
	@NoCreate @NoModify
	private Producto producto;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	@DescriptionsList(descriptionProperties="codigo, nombre",
		depends = UnidadMedida.DEPENDSDESCRIPTIONLIST,
		condition=UnidadMedida.CONDITIONDESCRIPTIONLIST)
	@NoCreate @NoModify
	private UnidadMedida unidadMedida;
	
	@Required
	@Min(value=0)
	private BigDecimal cantidad;
	
	@Column(length=50)
	@DisplaySize(value = 20)
	private String detalle;
	
	@ReadOnly
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	private CumplimientoReclamo cumplimiento;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify	
	private DespachoImportacion despacho;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify	
	private Lote lote;
	
	public CumplimientoReclamo getCumplimiento() {
		return cumplimiento;
	}

	public void setCumplimiento(CumplimientoReclamo cumplimiento) {
		this.cumplimiento = cumplimiento;
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
		return cantidad == null ? BigDecimal.ZERO : cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}

	public String getDetalle() {
		return detalle;
	}

	public void setDetalle(String detalle) {
		this.detalle = detalle;
	}

	@Override
	public Transaccion transaccion() {
		return cumplimiento;
	}
	
	@Override
	public void recalcular() {
		if (this.getProducto() != null){
			if (this.getUnidadMedida() == null){
				this.setUnidadMedida(this.getProducto().getUnidadMedida());
			}
		}
	}

	@Override
	public DespachoImportacion getDespacho() {
		return despacho;
	}
	
	@Override
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
	public ITipoMovimientoInventario tipoMovimientoInventario(boolean reversion) {
		// Los valores negativos son egresos
		// Los positivos, ingreso 
			if (!reversion){
				return new TipoMovInvEgreso();
			}
			else{
				return new TipoMovInvIngreso();
			}
		
	}


	@Override
	public Cantidad cantidadStock(){
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
	
	@Hidden
	@Override
	public Deposito getDeposito() {
		return this.getCumplimiento().getDeposito();
	}
	
	public void crearItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem){
	}
	
	@Override
	public void posActualizarItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem) {		
	}
}
