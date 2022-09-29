package org.openxava.inventario.model;

import java.math.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
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
		"despacho;"	+
		"lote;"
	)
})

@Tabs({
	@Tab(properties="transferencia.fecha, transferencia.numero, transferencia.estado, transferencia.origen.nombre, transferencia.destino.nombre, producto.codigo, producto.nombre, cantidad, despacho.codigo, lote.codigo")	
})

public class ItemTransferenciaDepositos extends ItemTransaccion implements IItemMovimientoInventario{

	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private TransferenciaDepositos transferencia;
	
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
	private BigDecimal cantidad;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	private DespachoImportacion despacho;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	private Lote lote;
	
	public TransferenciaDepositos getTransferencia() {
		return transferencia;
	}

	public void setTransferencia(TransferenciaDepositos transferencia) {
		this.transferencia = transferencia;
	}

	public BigDecimal getCantidad() {
		return cantidad == null ? BigDecimal.ZERO : this.cantidad;
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

	public Lote getLote() {
		return lote;
	}

	public void setLote(Lote lote) {
		this.lote = lote;
	}

	@Override
	public ITipoMovimientoInventario tipoMovimientoInventario(boolean reversion) {
		// El item representa el egreso del deposito origen
		if (!reversion){
			return new TipoMovInvEgreso();
		}
		else{
			return new TipoMovInvIngreso();
		}		
	}

	@Override
	public Deposito getDeposito() {
		if (this.getTransferencia() != null){
			return this.getTransferencia().getOrigen();
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
	public Transaccion transaccion() {
		return this.getTransferencia();
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
	public void crearItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem){
	}
	
	@Override
	public void posActualizarItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem) {		
	}
}
