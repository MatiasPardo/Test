package org.openxava.ventas.model;

import java.math.*;
import java.util.Date;

import javax.persistence.*;

import org.hibernate.annotations.Formula;
import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;
import org.openxava.negocio.actions.OnChangeProducto;
import org.openxava.negocio.model.UnidadMedida;
import org.openxava.util.Users;
import org.openxava.ventas.calculators.*;
import org.openxava.ventas.validators.*;

@Entity

@Views({
	@View(members=
			"listaPrecio;" + 
			"producto;" + 
			"unidadMedida;" + 		
			"Precio[" +
			"precioBase;" + 
			"porcentaje;" + 
			"importe];"	+	
			"precioPorCantidad [" + 
				"porCantidad, desde, hasta];" + 
			"metricasPrecio;" + 
			"Auditoria["
			+ "fechaUltimaModificacion, precioAnterior, usuarioModificacion]")
})

@EntityValidators({
	@EntityValidator(			
			value=PrecioValidator.class, 
			properties= {
				@PropertyValue(name="id", from="id"), 
				@PropertyValue(name="producto", from="producto"),
				@PropertyValue(name="listaPrecio", from="listaPrecio"),
				@PropertyValue(name="unidadMedida", from="unidadMedida"),
				@PropertyValue(name="porCantidad", from="porCantidad"),
				@PropertyValue(name="desde", from="desde"),
				@PropertyValue(name="hasta", from="hasta")
			}
	)
})


@Tabs({
	@Tab(properties="producto.codigo, producto.nombre, producto.marca.nombre, precioBase, porcentaje, importe, metricasPrecio.importeMasIva, listaPrecio.nombre, porCantidad, desde, hasta", 
		baseCondition="${producto.activo} = 't' and ${listaPrecio.activo} = 't'"),
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		properties="producto.codigo, producto.nombre, producto.marca.nombre, precioBase, porcentaje, importe, metricasPrecio.importeMasIva, listaPrecio.nombre, porCantidad, desde, hasta",
		baseCondition="(${producto.activo} = 'f' or ${listaPrecio.activo} = 'f')"),
	@Tab(name="Ventas", 
		properties="producto.codigo, producto.nombre, producto.marca.nombre, precioBase, porcentaje, importe, metricasPrecio.importeMasIva, listaPrecio.nombre, porCantidad, desde, hasta", 
		baseCondition="${producto.activo} = 't' and ${listaPrecio.activo} = 't' and ${listaPrecio.costo} = 'f'"),
	@Tab(name="Costos", 
		properties="producto.codigo, producto.nombre, producto.marca.nombre, precioBase, porcentaje, importe, metricasPrecio.importeMasIva, listaPrecio.nombre, porCantidad, desde, hasta", 
		baseCondition="${producto.activo} = 't' and ${listaPrecio.activo} = 't' and ${listaPrecio.costo} = 't'"),
})

public class Precio extends ObjetoNegocio{

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate
	@NoModify
	@Required
	@ReferenceView("Simple")
	private ListaPrecio listaPrecio;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate
	@NoModify
	@Required
	@ReferenceView("Simple")
	@OnChange(value=OnChangeProducto.class)
	private Producto producto;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre",
			depends=UnidadMedida.DEPENDSDESCRIPTIONLIST,
			condition=UnidadMedida.CONDITIONDESCRIPTIONLIST)
	@NoCreate @NoModify
	private UnidadMedida unidadMedida;
	
	@Required
	@Stereotype("MONEY")
	@DefaultValueCalculator(
			value=BigDecimalCalculator.class,
			properties={@PropertyValue(name="value", value="0")}
			)
	private BigDecimal precioBase;
	
	@DefaultValueCalculator(
			value=BigDecimalCalculator.class,
			properties={@PropertyValue(name="value", value="0")}
			)
	private BigDecimal porcentaje;
	
	@Required
	@ReadOnly
	@Stereotype("MONEY")
	@DefaultValueCalculator(
			value=PrecioImporteFinalCalculator.class,
			properties={@PropertyValue(name="precioBase", from="precioBase"), 
						@PropertyValue(name="porcentaje", from="porcentaje")}
			)
	private BigDecimal importe;
	
	private Boolean porCantidad = false;
	
	@DefaultValueCalculator(
			value=BigDecimalCalculator.class,
			properties={@PropertyValue(name="value", value="0")}
			)
	private BigDecimal desde;
	
	@DefaultValueCalculator(
			value=BigDecimalCalculator.class,
			properties={@PropertyValue(name="value", value="1000000")}
			)
	private BigDecimal hasta;
	
	@OneToOne(optional=true, fetch=FetchType.LAZY, targetEntity=MetricasPrecio.class, mappedBy="precio")
	@NoFrame
	@ReadOnly
	private MetricasPrecio metricasPrecio;
	
	@Hidden
	@Formula(value="(case when porCantidad = 't' then desde else null end)")
	private BigDecimal cantidadDesde;
	
	public BigDecimal getCantidadDesde() {
		return cantidadDesde;
	}

	@Hidden
	@Formula(value="(case when porCantidad = 't' then hasta else null end)")
	private BigDecimal cantidadHasta;
	
	public BigDecimal getCantidadHasta() {
		return cantidadHasta;
	}
	
	@ReadOnly
	@Stereotype("DATETIME")
	private Date fechaUltimaModificacion;
	
	@ReadOnly 
	@Column(length=50)
	@DisplaySize(value=25)
	private String usuarioModificacion;
	
	@ReadOnly
	private BigDecimal precioAnterior;
	
	@Transient
	@Hidden
	private BigDecimal importeDuplicado = BigDecimal.ZERO;
	
	private BigDecimal getImporteDuplicado() {
		return importeDuplicado;
	}

	private void setImporteDuplicado(BigDecimal importeDuplicado) {
		this.importeDuplicado = importeDuplicado;
	}
	
	public ListaPrecio getListaPrecio() {
		return listaPrecio;
	}

	public void setListaPrecio(ListaPrecio listaPrecio) {
		this.listaPrecio = listaPrecio;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public BigDecimal getPrecioBase() {
		return precioBase == null? BigDecimal.ZERO:precioBase;
	}

	public void setPrecioBase(BigDecimal precioBase) {
		this.precioBase = precioBase;
	}

	public BigDecimal getPorcentaje() {
		return porcentaje == null? BigDecimal.ZERO:porcentaje;
	}

	public void setPorcentaje(BigDecimal porcentaje) {
		this.porcentaje = porcentaje;
	}

	public BigDecimal getImporte() {
		return importe == null? BigDecimal.ZERO:importe;
	}
	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}

	public Boolean getPorCantidad() {
		return porCantidad;
	}

	public void setPorCantidad(Boolean porCantidad) {
		this.porCantidad = porCantidad;
	}

	public BigDecimal getDesde() {
		return desde;
	}

	public void setDesde(BigDecimal desde) {
		this.desde = desde;
	}

	public BigDecimal getHasta() {
		return hasta;
	}

	public void setHasta(BigDecimal hasta) {
		this.hasta = hasta;
	}
	
	@Hidden
	public BigDecimal getCosto(){
		if (Esquemas.getEsquemaApp().getListaPrecioUnica()){
			return this.getPrecioBase();
		}
		else{
			return this.getImporte();
		}
	}

	public UnidadMedida getUnidadMedida() {
		return unidadMedida;
	}

	public void setUnidadMedida(UnidadMedida unidadMedida) {
		this.unidadMedida = unidadMedida;
	}

	public MetricasPrecio getMetricasPrecio() {
		return metricasPrecio;
	}

	public void setMetricasPrecio(MetricasPrecio metricasPrecio) {
		this.metricasPrecio = metricasPrecio;
	}

	public Date getFechaUltimaModificacion() {
		return fechaUltimaModificacion;
	}

	public void setFechaUltimaModificacion(Date fechaUltimaModificacion) {
		this.fechaUltimaModificacion = fechaUltimaModificacion;
	}

	public String getUsuarioModificacion() {
		return usuarioModificacion;
	}

	public void setUsuarioModificacion(String usuarioModificacion) {
		this.usuarioModificacion = usuarioModificacion;
	}

	public BigDecimal getPrecioAnterior() {
		return precioAnterior;
	}

	public void setPrecioAnterior(BigDecimal precioAnterior) {
		this.precioAnterior = precioAnterior;
	}

	@Override
	public void onPreDelete(){
		super.onPreDelete();
		this.setMetricasPrecio(null);
	}
	
	@Override
	public void onPreUpdate(){
		super.onPreUpdate();
		
		if(this.getImporteDuplicado().compareTo(this.getImporte()) != 0){
			this.setFechaUltimaModificacion(new Date());
			this.setUsuarioModificacion(Users.getCurrent());
			this.setPrecioAnterior(this.getImporteDuplicado());
		}
	}
	
	@PostLoad
	public void postLoad(){
		this.setImporteDuplicado(this.getImporte());
	}
}
