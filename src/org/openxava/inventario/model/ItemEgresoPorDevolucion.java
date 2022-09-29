package org.openxava.inventario.model;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;

import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.OnChange;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.Required;
import org.openxava.annotations.Tab;
import org.openxava.annotations.Tabs;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;
import org.openxava.base.model.ItemTransaccion;
import org.openxava.base.model.Transaccion;
import org.openxava.inventario.actions.OnChangeProductoAsignaAtributosInventarioAction;
import org.openxava.negocio.actions.OnChangeUnidadMedida;
import org.openxava.negocio.model.Cantidad;
import org.openxava.negocio.model.UnidadMedida;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.Producto;

@Entity

@Views({
	@View(members=
		"producto;" +
		"unidadMedida;" +
		"cantidad;" +
		"despacho;"	+ 
		"lote;"
	)
})

@Tabs({
	@Tab(properties="devolucion.fecha, devolucion.numero, devolucion.estado, devolucion.proveedor.nombre, devolucion.origen.nombre, producto.codigo, producto.nombre, cantidad, despacho.codigo")	
})

public class ItemEgresoPorDevolucion extends ItemTransaccion implements IItemMovimientoInventario{

	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private EgresoPorDevolucion devolucion;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	@OnChange(OnChangeProductoAsignaAtributosInventarioAction.class)
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
	private BigDecimal cantidad;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	private DespachoImportacion despacho;

	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	private Lote lote;
	
	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	@Override
	public Producto getProducto() {
		return this.producto;
	}

	public EgresoPorDevolucion getDevolucion() {
		return devolucion;
	}

	public void setDevolucion(EgresoPorDevolucion devolucion) {
		this.devolucion = devolucion;
	}

	public BigDecimal getCantidad() {
		return cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}

	public void setUnidadMedida(UnidadMedida unidadMedida) {
		this.unidadMedida = unidadMedida;
	}

	@Override
	public DespachoImportacion getDespacho() {
		return this.despacho;
	}

	@Override
	public void setDespacho(DespachoImportacion despacho) {
		this.despacho = despacho;
	}

	@Override
	@Hidden
	public Deposito getDeposito() {
		if (this.getDevolucion() != null){
			return this.getDevolucion().getOrigen();
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
			// se mantienen el signo de la cantidad original
			BigDecimal cantidadActualizada = cantidad.getCantidad().abs();			
			this.setCantidad(cantidadActualizada);		
		}
		else{
			throw new ValidationException("Difieren las unidades de medida");
		}				
	}

	@Override
	public UnidadMedida getUnidadMedida() {
		return this.unidadMedida;
	}

	@Override
	public void crearItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem) {
	}

	@Override
	public void posActualizarItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem) {	
	}

	@Override
	public Lote getLote() {
		return this.lote;
	}

	@Override
	public void setLote(Lote lote) {
		this.lote = lote;
	}

	@Override
	public Transaccion transaccion() {
		return this.getDevolucion();
	}
	
	@Override
	public ITipoMovimientoInventario tipoMovimientoInventario(boolean reversion) {
		if(!reversion){
			return new TipoMovInvEgreso();			
		}
		else{
			return new TipoMovInvIngreso();
		}
	}
	
	@Override
	public void recalcular() {
		if (this.unidadMedida == null){
			if (this.getProducto() != null){
				this.setUnidadMedida(this.getProducto().getUnidadMedida());
			}
		}		
	}

}
