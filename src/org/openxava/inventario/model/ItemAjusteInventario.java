package org.openxava.inventario.model;

import java.math.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.inventario.actions.*;
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
		"lote;"
	)
})

public class ItemAjusteInventario extends ItemTransaccion implements IItemMovimientoInventario{
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private AjusteInventario ajusteInventario;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Despacho")
	@NoCreate @NoModify
	@OnChange(OnChangeProductoItemAjusteInv.class)
	private Producto producto;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre",
					depends=UnidadMedida.DEPENDSDESCRIPTIONLIST,
					condition=UnidadMedida.CONDITIONDESCRIPTIONLIST)
	@NoCreate @NoModify
	@OnChange(OnChangeUnidadMedida.class)
	private UnidadMedida unidadMedida;
	
	@Required
	private BigDecimal cantidad;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify	
	private DespachoImportacion despacho;

	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify	
	private Lote lote;
	
	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public BigDecimal getCantidad() {
		return cantidad == null ? BigDecimal.ZERO: this.cantidad;
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
	public void actualizarCantidadItem(Cantidad cantidad){
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
	
	@Override
	public Cantidad cantidadStock(){
		Cantidad cantidad = new Cantidad();
		cantidad.setUnidadMedida(this.getUnidadMedida());
		cantidad.setCantidad(this.getCantidad().abs());
		return cantidad;
	}
		
	@Override
	public ITipoMovimientoInventario tipoMovimientoInventario(boolean reversion) {
		// Los valores negativos son egresos
		// Los positivos, ingreso 
		if (this.getCantidad().compareTo(BigDecimal.ZERO) < 0){
			if (!reversion){
				return new TipoMovInvEgreso();
			}
			else{
				return new TipoMovInvIngreso();
			}
		}
		else{
			if (!reversion){
				return new TipoMovInvIngreso();
			}
			else{
				return new TipoMovInvEgreso();
			}
		}
	}

	@Override
	public Deposito getDeposito() {
		return this.getAjusteInventario().getDepositoOrigen();
	}

	public AjusteInventario getAjusteInventario() {
		return ajusteInventario;
	}

	public void setAjusteInventario(AjusteInventario ajusteInventario) {
		this.ajusteInventario = ajusteInventario;
	}
	
	@Override
	protected void onPrePersist(){
		super.onPrePersist();
		Inventario.validarAtributosInventarioCompleto(this);
	}
	
	@Override
	protected void onPreUpdate(){
		super.onPreUpdate();
		Inventario.validarAtributosInventarioCompleto(this);
	}

	public UnidadMedida getUnidadMedida() {
		return unidadMedida;
	}

	public void setUnidadMedida(UnidadMedida unidadMedida) {
		this.unidadMedida = unidadMedida;
	}
	
	@Override
	public void copiarPropiedades(Object objeto){
		super.copiarPropiedades(objeto);		
	}

	@Override
	public Transaccion transaccion() {
		return this.getAjusteInventario();
	}

	@Override
	public void recalcular() {
		if (this.getProducto() != null){
			if (this.getUnidadMedida() == null){
				this.setUnidadMedida(this.getProducto().getUnidadMedida());
			}
		}
	}
	
	public void crearItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem){
	}
	
	@Override
	public void posActualizarItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem) {		
	}
}
