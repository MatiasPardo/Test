package org.openxava.contratos.model;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.openxava.annotations.CollectionView;
import org.openxava.annotations.Condition;
import org.openxava.annotations.DefaultValueCalculator;
import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.ListProperties;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.OnChange;
import org.openxava.annotations.PropertyValue;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.Required;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;
import org.openxava.base.filter.EmpresaFilter;
import org.openxava.base.model.Estado;
import org.openxava.base.model.EstadoEntidad;
import org.openxava.base.model.ObjetoNegocio;
import org.openxava.base.model.Transaccion;
import org.openxava.base.model.UtilERP;
import org.openxava.calculators.BigDecimalCalculator;
import org.openxava.calculators.TrueCalculator;
import org.openxava.calculators.ZeroBigDecimalCalculator;
import org.openxava.contratos.actions.OnChangeContratoAction;
import org.openxava.contratos.calculators.FechaFacturacionContratoCalculator;
import org.openxava.jpa.XPersistence;
import org.openxava.negocio.calculators.ObjetoPrincipalCalculator;
import org.openxava.util.Is;
import org.openxava.util.Messages;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.ItemVentaElectronica;
import org.openxava.ventas.model.Producto;

@Entity

@Views({
	@View(name="Simple", 
		members="numero, estado"),
	
	@View(members=
		"Principal{" +
			"Principal[fecha, fechaCreacion, usuario;" +
					"estado, subestado;" + 
			"];" + 
			"contrato;" +
			"Vigencia[ciclo, cantidadCiclos, ciclosPendientes;" +
				"proximaEmisionFactura, proximoVencimientoFactura;" +
				"];" +
			"observaciones;" + 	
			"concepto;" +
			"Facturacion[cantidad, porcentajeDescuento; utilizaListaPrecio, precioFijo, moneda;" +
					"detalle, incluirDetalle;" +
				"];" +
		"}" + 
		"Facturas{facturas}")
})

@Tab(
	filter=EmpresaFilter.class,
	baseCondition=EmpresaFilter.BASECONDITION,
	properties="fecha, subestado.nombre, proximaEmisionFactura, contrato.cliente.codigo, contrato.cliente.nombre, concepto.codigo, concepto.nombre, moneda.nombre, ciclo.nombre, usuario, contrato.cliente.listaPrecio.nombre, contrato.cliente.listaPrecio.codigo",
	defaultOrder="${fechaCreacion} desc")

public class NovedadContrato extends Transaccion{
	
	public static final String ESTADOVIGENTE = "VIGENTE";
	
	public static final String ESTADOFINALIZADO = "FINALIZADO";
	
	public static final String ESTADOANULADO = "ANULADO";
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Novedad Contrato";
	}
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView(value="NovedadContrato")
	@OnChange(value=OnChangeContratoAction.class)
	private Contrato contrato;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="codigo, nombre", forTabs="Combo")
	@DefaultValueCalculator(value=ObjetoPrincipalCalculator.class, 
		properties={@PropertyValue(name="entidad", value="CicloFacturacion")})
	private CicloFacturacion ciclo;
	
	@Min(value=1, message="Valor mínimo 1")
	private Integer cantidadCiclos;
	
	@ReadOnly
	private Integer ciclosPendientes;
	
	@ReadOnly
	@DefaultValueCalculator(value=FechaFacturacionContratoCalculator.class, 
			properties={@PropertyValue(name="fecha"), 
						@PropertyValue(name="idCiclo", from="ciclo.id"),
						@PropertyValue(name="tipoFecha", value="proximaEmisionFactura")})
	private Date proximaEmisionFactura;
	
	@ReadOnly
	@DefaultValueCalculator(value=FechaFacturacionContratoCalculator.class, 
			properties={@PropertyValue(name="fecha"), 
				@PropertyValue(name="idCiclo", from="ciclo.id"), 
				@PropertyValue(name="tipoFecha", value="proximoVencimientoFactura")})
	private Date proximoVencimientoFactura;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView(value="Simple")
	private Producto concepto;
	
	@Required
	@DefaultValueCalculator(value=BigDecimalCalculator.class, 
				properties={@PropertyValue(name="value", value="1")})
	private BigDecimal cantidad = new BigDecimal(1);
	
	@Min(value=0, message="No puede menor a 0")
	@Max(value=100, message="No puede ser mayor a 100")
	@DefaultValueCalculator(value=ZeroBigDecimalCalculator.class)
	private BigDecimal porcentajeDescuento = BigDecimal.ZERO;
	
	@DefaultValueCalculator(value=TrueCalculator.class)
	private Boolean utilizaListaPrecio = Boolean.TRUE;
	
	@DefaultValueCalculator(value=ZeroBigDecimalCalculator.class)
	@Min(value=0, message="No puede ser negativo")
	private BigDecimal precioFijo = BigDecimal.ZERO;

	@Column(length=50)
	private String detalle;
	
	private TipoDetalleContrato incluirDetalle;
	
	@Condition("${novedadContrato.id} = ${this.id}")
	@ReadOnly
	@ListProperties(value="venta.fecha, venta.numero, venta.estado, venta.total, fechaCreacion, usuario")
	@OrderBy("fechaCreacion desc")
	@CollectionView(value="Factura")
	public Collection<ItemVentaElectronica> getFacturas(){
		return null;
	}
	
	public Contrato getContrato() {
		return contrato;
	}

	public void setContrato(Contrato contrato) {
		this.contrato = contrato;
	}

	public CicloFacturacion getCiclo() {
		return ciclo;
	}

	public void setCiclo(CicloFacturacion ciclo) {
		this.ciclo = ciclo;
	}

	public Integer getCantidadCiclos() {
		return cantidadCiclos;
	}

	public void setCantidadCiclos(Integer cantidadCiclos) {
		this.cantidadCiclos = cantidadCiclos;
	}
	
	public Integer getCiclosPendientes() {
		return ciclosPendientes == null ? 0 : ciclosPendientes;
	}

	public void setCiclosPendientes(Integer ciclosPendientes) {
		this.ciclosPendientes = ciclosPendientes;
	}

	public Producto getConcepto() {
		return concepto;
	}

	public void setConcepto(Producto concepto) {
		this.concepto = concepto;
	}

	public BigDecimal getCantidad() {
		return cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}

	public BigDecimal getPorcentajeDescuento() {
		return porcentajeDescuento == null ? BigDecimal.ZERO : this.porcentajeDescuento;
	}

	public void setPorcentajeDescuento(BigDecimal porcentajeDescuento) {		
		this.porcentajeDescuento = porcentajeDescuento;
	}

	public Boolean getUtilizaListaPrecio() {
		return utilizaListaPrecio;
	}

	public void setUtilizaListaPrecio(Boolean utilizaListaPrecio) {
		if (utilizaListaPrecio != null){
			this.utilizaListaPrecio = utilizaListaPrecio;
		}
	}

	public BigDecimal getPrecioFijo() {
		return precioFijo;
	}

	public void setPrecioFijo(BigDecimal precioFijo) {
		if (precioFijo != null){
			this.precioFijo = precioFijo;
		}
	}
	
	@Override
	protected void validacionesPreGrabarTransaccion(Messages errores){
		super.validacionesPreGrabarTransaccion(errores);
		
		if (this.getContrato() != null){
			if (!this.getContrato().getEstado().equals(Estado.Confirmada)){
				errores.add("El contrato debe estar confirmado");
			}
		}
		
		if (this.getUtilizaListaPrecio()){
			if (this.getPrecioFijo().compareTo(BigDecimal.ZERO) > 0){
				errores.add("El precio fijo debe ser cero si utiliza lista de precios");
			}
		}
		else if (this.getPrecioFijo().compareTo(BigDecimal.ZERO) == 0){
			errores.add("Si no utiliza lista de precio debe asignar un precio fijo");
		}
		
		String detalleParaFacturacion = this.detalleFacturacion(this.getFecha());
		if (!Is.emptyString(detalleParaFacturacion)){
			try{
				final Column anotacionColumn = ItemVentaElectronica.class.getDeclaredField("detalle").getAnnotation(Column.class);
				if (anotacionColumn != null){
					if (detalleParaFacturacion.length() > anotacionColumn.length()){
						errores.add("El detalle tiene una longitud de " + detalleParaFacturacion.length() + " y no puede superar los " + anotacionColumn.length());
					}
				}				
			}
			catch(Exception e){			
			}
		}
	}
	
	@Override
	public void recalcularTotales(){
		super.recalcularTotales();
		
		if (this.getContrato() != null){
			this.setEmpresa(this.getContrato().getEmpresa());
			if (this.getMoneda() == null){
				this.setMoneda(this.getContrato().getMoneda());
			}
		}
		
		if (this.getCiclo() != null && this.getFecha() != null){
			this.getCiclo().simularFechasFacturacion(this.getFecha(), true, false);
			this.setProximaEmisionFactura(this.getCiclo().getFechaEmision1());
			this.setProximoVencimientoFactura(this.getCiclo().getFechaVencimiento1());
		}
		
		this.setCiclosPendientes(this.getCantidadCiclos());
	}

	public Date getProximaEmisionFactura() {
		return proximaEmisionFactura;
	}

	public void setProximaEmisionFactura(Date proximaEmisionFactura) {
		if (proximaEmisionFactura != null){
			this.proximaEmisionFactura = UtilERP.trucarDateTime(proximaEmisionFactura);
		}
		else{
			this.proximaEmisionFactura = null;
		}
	}

	public Date getProximoVencimientoFactura() {
		return proximoVencimientoFactura;
	}

	public void setProximoVencimientoFactura(Date proximoVencimientoFactura) {
		if (proximoVencimientoFactura != null){
			this.proximoVencimientoFactura = UtilERP.trucarDateTime(proximoVencimientoFactura);
		}
		else{
			this.proximoVencimientoFactura = null;
		}
	}

	public void avanzarProximoCicloFacturacion() {
		if (this.getCiclosPendientes() > 0){
			boolean primerFactura =  this.getCiclosPendientes() == this.getCantidadCiclos();
			this.setCiclosPendientes(this.getCiclosPendientes() - 1);
			
			if (this.getCiclosPendientes() > 0){
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(this.getProximaEmisionFactura());				
				this.getCiclo().simularFechasFacturacion(calendar.getTime(), primerFactura, true);
				
				this.setProximaEmisionFactura(this.getCiclo().getFechaEmision2());
				this.setProximoVencimientoFactura(this.getCiclo().getFechaVencimiento2());
			}			
		}
		else{
			throw new ValidationException(this.toString() + " no le quedan ciclos de facturación pendientes");
		}		
	}
	
	public boolean facturacionFinalizada(){
		if (this.getCiclosPendientes() <= 0){
			return true;
		}
		else{
			return false;
		}
	}
		
	@Override
	public ObjetoNegocio generarCopia() {
		NovedadContrato copia = new NovedadContrato();
		copia.copiarPropiedades(this);
		XPersistence.getManager().persist(copia);
		return copia;
	}
	
	@Override
	public void copiarPropiedades(Object objeto){
		super.copiarPropiedades(objeto);
				
		this.setCiclosPendientes(this.getCantidadCiclos());
	}

	public String getDetalle() {
		return detalle;
	}

	public void setDetalle(String detalle) {
		this.detalle = detalle;
	}
	
	public TipoDetalleContrato getIncluirDetalle() {
		return incluirDetalle;
	}

	public void setIncluirDetalle(TipoDetalleContrato incluirDetalle) {
		this.incluirDetalle = incluirDetalle;
	}

	@Override
	protected void validarFechaComprobante(Messages errores){
		if ((this.getFecha() != null) && (this.getCiclo() != null)){
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.MONTH, this.getCiclo().getFrecuencia().getMeses());
			Date fechaMax = UtilERP.trucarDateTime(calendar.getTime());
			
			if (this.getFecha().compareTo(fechaMax) > 0){
				errores.add("La fecha no puede ser mayor a " + UtilERP.convertirString(fechaMax));
			}
		}		
	}

	public boolean haFacturado() {
		return !(this.getCiclosPendientes() == this.getCantidadCiclos());
	}
	
	public String detalleFacturacion(Date fechaEmision){
		StringBuilder det = new StringBuilder();
		if (!Is.emptyString(this.getDetalle())){
			det.append(this.getDetalle());
		}
		if (this.getIncluirDetalle() != null){
			if (det.length() > 0) det.append(" ");
			
			if (this.getIncluirDetalle().equals(TipoDetalleContrato.MesActual) 
					|| this.getIncluirDetalle().equals(TipoDetalleContrato.MesAnterior)){
				Date fecha = fechaEmision;
				if (this.getIncluirDetalle().equals(TipoDetalleContrato.MesAnterior)){
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(fecha);
					calendar.add(Calendar.MONTH, -1);					
					fecha = calendar.getTime();
				}
				SimpleDateFormat format = new SimpleDateFormat("MM/yyyy");
				det.append(format.format(fecha));
			}
			else{
				Integer numeroCiclo = this.getCantidadCiclos() - this.getCiclosPendientes() + 1;
				det.append(String.format("%" + this.getCantidadCiclos().toString().length() + "d", numeroCiclo));				
				det.append("/");
				det.append(this.getCantidadCiclos().toString());
			}
		}
		return det.toString();
	}
	
	@Override
	protected void posAnularTransaccion(){
		super.posAnularTransaccion();
		
		if (this.getSubestado() != null && this.getSubestado().getCodigo().equals(NovedadContrato.ESTADOVIGENTE)){
			// Si se anula y sigue teniendo el subestado de Vigente, se lo pasa al estado anulado
			EstadoEntidad novedadAnulada = EstadoEntidad.buscarPorCodigo(NovedadContrato.ESTADOANULADO, NovedadContrato.class.getSimpleName());
			this.setSubestado(novedadAnulada);
		}
	}
}
