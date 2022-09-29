package org.openxava.inventario.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;

import org.hibernate.annotations.Formula;
import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.EntityValidator;
import org.openxava.annotations.EntityValidators;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.PropertyValue;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.Required;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;
import org.openxava.base.model.ItemTransaccion;
import org.openxava.base.model.Transaccion;
import org.openxava.codigobarras.model.IItemControlCodigoBarras;
import org.openxava.codigobarras.model.TipoControlCodigoBarras;
import org.openxava.inventario.validators.ItemControlMercaderiaValidator;
import org.openxava.negocio.model.Cantidad;
import org.openxava.negocio.model.UnidadMedida;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.Producto;

@Entity

@Views({
	@View(members=
		"producto;"+
		"unidadMedida;" +
		"cantidad, controlado;" + 		
		"itemRemito;" + 		
		"despacho;" +
		"lote;" + 
		"detalle;"  		
	)
})

@EntityValidators({
	@EntityValidator(
		value=ItemControlMercaderiaValidator.class, 
		properties= {
			@PropertyValue(name="transaccion", from="controlMercaderia"),
			@PropertyValue(name="cantidad"), 			
			@PropertyValue(name="itemRemito", from="itemRemito")
		}
	)
})

public class ItemControlMercaderia extends ItemTransaccion implements IItemMovimientoInventario, IItemControlCodigoBarras{

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	@ReferenceView("Simple")
	private ControlMercaderia controlMercaderia;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@ReadOnly
	private Producto producto;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
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
	
	@Column(length=50)
	private String detalle;
	
	@Min(value=0, message="No puede ser negativo")
	@Required
	private BigDecimal cantidad = BigDecimal.ZERO;
			

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly	
	@ReferenceView("ControlMercaderia")	
	private ItemRemito itemRemito;
	
	@Override
	public Transaccion transaccion() {
		return this.getControlMercaderia();
	}
		
	@Override
	public void recalcular() {		
	}

	public ControlMercaderia getControlMercaderia() {
		return controlMercaderia;
	}

	public void setControlMercaderia(ControlMercaderia controlMercaderia) {
		this.controlMercaderia = controlMercaderia;
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

	public DespachoImportacion getDespacho() {
		return despacho;
	}

	public void setDespacho(DespachoImportacion despacho) {
		this.despacho = despacho;
	}
	
	public String getDetalle() {
		return detalle;
	}

	public void setDetalle(String detalle) {
		this.detalle = detalle;
	}

	public ItemRemito getItemRemito() {
		return itemRemito;
	}

	public void setItemRemito(ItemRemito itemRemito) {
		this.itemRemito = itemRemito;
	}

	@Override
	public ITipoMovimientoInventario tipoMovimientoInventario(boolean reversion) {
		ITipoMovimientoInventario tipoMov = null;
		if (this.getControlMercaderia().getResultado().equals(ResultadoControlMercaderia.MercaderiaRecibida)){
			if (!reversion){
				// hace la desreserva para que este disponible la mercadería en el depósito que solicitó la mercadería
				tipoMov = new TipoMovInvDesreserva();
			}
			else{
				tipoMov = new TipoMovInvReserva();
			}
		}
		else if (this.getControlMercaderia().getResultado().equals(ResultadoControlMercaderia.MercaderiaNoRecibida)){
			if (!reversion){
				// hace quita el stock reservado del depósito que solicitó la mercadería
				tipoMov = new TipoMovInvEgresoDesreserva();
			}
			else{
				tipoMov = new TipoMovInvIngresoReserva();
			}
		}
		return tipoMov;
	}

	@Override
	@Hidden
	public Deposito getDeposito() {
		if (this.getControlMercaderia() != null){
			return this.getControlMercaderia().getRecepcion();
		}
		else{
			return null;
		}
	}

	@Override
	public Cantidad cantidadStock() {
		Cantidad cantidad = new Cantidad();
		cantidad.setUnidadMedida(this.getUnidadMedida());
		// se egresa únicamente lo recibido
		cantidad.setCantidad(this.getCantidad());
		return cantidad;
	}

	@Override
	public void actualizarCantidadItem(Cantidad cantidad) {
		throw new ValidationException("Tipo de movimiento de Inventario no debe actualizar la cantidad");
	}

	public BigDecimal getCantidad() {
		return cantidad == null ? BigDecimal.ZERO : this.cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
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

	@ReadOnly
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

	@Formula("cantidad - controlado")
	private BigDecimal pendienteControl;
	
	public BigDecimal getPendienteControl(){
		return this.getCantidad().subtract(this.getControlado());
	}
	
	@Formula("(case when cantidad = 0 then 2 "
			+ "when (cantidad - controlado) = 0 then 1 "
			+ "else 0 end)"
			)  
	private TipoControlCodigoBarras control;
	
	public TipoControlCodigoBarras getControl(){
		return control;
	}
	
	@Override
	public boolean crearEntidadesPorControl() {
		return false;
	}

	@Override
	public BigDecimal convertirUnidadesLeidas(BigDecimal cantidadLeida) {
		return cantidadLeida;
	}
	
	@Override
	public void copiarPropiedades(Object objeto){
		super.copiarPropiedades(objeto);
		
		this.setControlado(BigDecimal.ZERO);
	}
}


