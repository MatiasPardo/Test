package org.openxava.conciliacionbancaria.model;

import java.util.Collection;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import org.hibernate.validator.constraints.Length;
import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.EntityValidator;
import org.openxava.annotations.EntityValidators;
import org.openxava.annotations.ListProperties;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.PropertyValue;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.RowStyle;
import org.openxava.annotations.Stereotype;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;
import org.openxava.base.model.ObjetoNegocio;
import org.openxava.base.model.UtilERP;
import org.openxava.conciliacionbancaria.validators.ResumenExtractoBancarioValidator;
import org.openxava.tesoreria.model.CuentaBancaria;
import org.openxava.validators.ValidationException;

@Entity

@EntityValidators({
	@EntityValidator(
		value=ResumenExtractoBancarioValidator.class, 
		properties= {
			@PropertyValue(name="idEntidad", from="id"), 
			@PropertyValue(name="desde", from="desde"),
			@PropertyValue(name="hasta", from="hasta"),
			@PropertyValue(name="cuenta", from="cuenta")
		}
	)
})

@Views({
	@View(members="cuenta, desde, hasta;" +
		"usuario, fechaCreacion;" +
		"observaciones;" + 
		"extracto"),
	@View(name="ExtractoBancario", 
		members="cuenta, desde, hasta;" +
			"usuario, fechaCreacion;" + 
			"observaciones;")
})

@Tab(properties="cuenta.codigo, cuenta.nombre, desde, hasta, fechaCreacion, usuario", 
	defaultOrder="${fechaCreacion} desc"
)

public class ResumenExtractoBancario extends ObjetoNegocio{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	@NoModify @NoCreate
	@DescriptionsList(descriptionProperties="nombre")
	private CuentaBancaria cuenta;
	
	@ReadOnly
	private Date desde;
	
	@ReadOnly
	private Date hasta;
	
	@OneToMany(mappedBy="resumen", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@ReadOnly
	@ListProperties(value="fecha, concepto, importe, credito, debito, saldo, observaciones, conciliado")
	@RowStyle(style="pendiente-ejecutado", property="conciliado", value="true")
	@OrderBy("nroFila asc")
	private Collection<ExtractoBancario> extracto;
	
	@Stereotype("MEMO")
	@Length(max=255)
	private String observaciones;
	
	public CuentaBancaria getCuenta() {
		return cuenta;
	}

	public void setCuenta(CuentaBancaria cuenta) {
		this.cuenta = cuenta;
	}

	public Date getDesde() {
		return desde;
	}

	public void setDesde(Date desde) {
		this.desde = desde;
	}

	public Date getHasta() {
		return hasta;
	}

	public void setHasta(Date hasta) {
		this.hasta = hasta;
	}

	public Collection<ExtractoBancario> getExtracto() {
		return extracto;
	}

	public void setExtracto(Collection<ExtractoBancario> extracto) {
		this.extracto = extracto;
	}

	public void asignarRangoFecha(Date fecha) {
		Date fechaActual = UtilERP.trucarDateTime(new Date());
		if (fecha.before(fechaActual)){
			if ((this.getDesde() == null) && (this.getHasta() == null)){
				this.setDesde(fecha);
				this.setHasta(fecha);
			}
			else{
				if (this.getDesde() == null){
					this.setDesde(this.getHasta());
				}
				else if (this.getHasta() == null){
					this.setHasta(this.getDesde());
				}
				
				if (fecha.compareTo(this.getDesde()) <= 0){
					this.setDesde(fecha);			
				}
				else if (fecha.compareTo(this.getHasta()) > 0){
					this.setHasta(fecha);			
				}
			}
		}
		else{
			throw new ValidationException("La fecha del extracto debe ser anterior a hoy");
		}
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}
}
