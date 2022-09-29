package org.openxava.contratos.model;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.openxava.annotations.DefaultValueCalculator;
import org.openxava.annotations.EntityValidator;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.OnChange;
import org.openxava.annotations.PropertyValue;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.Required;
import org.openxava.annotations.Tab;
import org.openxava.annotations.Tabs;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;
import org.openxava.base.model.ConfiguracionEntidad;
import org.openxava.base.model.ObjetoEstatico;
import org.openxava.base.model.UtilERP;
import org.openxava.calculators.FalseCalculator;
import org.openxava.calculators.IntegerCalculator;
import org.openxava.calculators.ZeroIntegerCalculator;
import org.openxava.contratos.actions.OnChangeFechaNovedadCicloFacturacion;
import org.openxava.contratos.calculators.FrecuenciaCicloFacturacionCalculator;
import org.openxava.negocio.validators.PrincipalValidator;
import org.openxava.validators.ValidationException;

@Entity

@Views({
	@View(members="codigo, usuario, fechaCreacion;" + 
			"nombre;" + 
			"activo, principal;" + 
			"frecuencia;" + 
			"diaFechaEmision, primerFacturaEmiteCualquierDia;" +
			"diaFechaVencimiento, tipoVencimiento;" + 
			"restringeFacturacion, diasAntes;" + 
			"Simulacion[fechaNovedad;" + 
				"fechaEmision1, fechaVencimiento1;" +
				"fechaEmision2, fechaVencimiento2];" 
			),
	@View(name="Simple", members="codigo, nombre")
})

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})

@EntityValidator(
		value=PrincipalValidator.class, 
		properties= {
			@PropertyValue(name="idEntidad", from="id"), 
			@PropertyValue(name="modelo", value="CicloFacturacion"),
			@PropertyValue(name="principal")
		}
	)

public class CicloFacturacion extends ObjetoEstatico{
	
	@DefaultValueCalculator(FalseCalculator.class)
	private Boolean principal = false;
	
	@Required
	@DefaultValueCalculator(value=FrecuenciaCicloFacturacionCalculator.class)
	@OnChange(OnChangeFechaNovedadCicloFacturacion.class)
	private FrecuenciaCicloFacturacion frecuencia = FrecuenciaCicloFacturacion.Mensual;
	
	@Min(value=1, message="El día emisión no puede ser menor a cero") @Max(value=31, message="El día emisión no puede ser mayor a 31")
	@DefaultValueCalculator(value=IntegerCalculator.class, 
					properties={@PropertyValue(name="value", value="1")})
	@Required
	@OnChange(OnChangeFechaNovedadCicloFacturacion.class)
	private Integer diaFechaEmision = 1;
	
	@Min(value=0, message="El día vencimiento no puede ser menor a cero") @Max(value=31, message="El día vencimiento no puede ser mayor a 31")
	@DefaultValueCalculator(value=IntegerCalculator.class, 
					properties={@PropertyValue(name="value", value="0")})
	@OnChange(OnChangeFechaNovedadCicloFacturacion.class)
	private Integer diaFechaVencimiento = 0;

	@Required
	private TipoVencimientoCicloFacturacion tipoVencimiento = TipoVencimientoCicloFacturacion.Fijo;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	@OnChange(OnChangeFechaNovedadCicloFacturacion.class)
	private Boolean primerFacturaEmiteCualquierDia = Boolean.FALSE;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean restringeFacturacion = Boolean.FALSE;
	
	@Min(value=0, message="no puede ser menor a cero")
	@DefaultValueCalculator(value=ZeroIntegerCalculator.class)
	private Integer diasAntes = 0;
	
	public FrecuenciaCicloFacturacion getFrecuencia() {
		return frecuencia;
	}

	public void setFrecuencia(FrecuenciaCicloFacturacion frecuencia) {
		this.frecuencia = frecuencia;
	}

	public Integer getDiaFechaEmision() {
		return diaFechaEmision;
	}

	public void setDiaFechaEmision(Integer diaFechaEmision) {
		this.diaFechaEmision = diaFechaEmision;
	}

	public Integer getDiaFechaVencimiento() {
		return diaFechaVencimiento;
	}

	public void setDiaFechaVencimiento(Integer diaFechaVencimiento) {
		if (diaFechaVencimiento != null){
			this.diaFechaVencimiento = diaFechaVencimiento;
		}
	}

	public Boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	}
	
	public Boolean getRestringeFacturacion() {
		return restringeFacturacion;
	}

	public void setRestringeFacturacion(Boolean restringeFacturacion) {
		if (restringeFacturacion != null){
			this.restringeFacturacion = restringeFacturacion;
		}
	}

	public Integer getDiasAntes() {
		return diasAntes;
	}

	public void setDiasAntes(Integer diasAntes) {
		if (diasAntes != null){
			this.diasAntes = diasAntes;
		}
	}
	
	// SIMULACION
	
	@Transient @Hidden
	@OnChange(OnChangeFechaNovedadCicloFacturacion.class)
	private Date fechaNovedad;
	
	@Transient
	@ReadOnly @Hidden	
	private Date fechaEmision1;
	
	@Transient
	@ReadOnly @Hidden
	private Date fechaVencimiento1;

	@Transient
	@ReadOnly @Hidden	
	private Date fechaEmision2;
	
	@Transient
	@ReadOnly @Hidden
	private Date fechaVencimiento2;
	
	public Date getFechaNovedad() {
		return fechaNovedad;
	}

	public void setFechaNovedad(Date fechaNovedad) {
		this.fechaNovedad = fechaNovedad;
	}

	public Date getFechaEmision1() {
		return fechaEmision1;
	}

	public void setFechaEmision1(Date fechaEmision1) {
		if (fechaEmision1 != null){
			this.fechaEmision1 = UtilERP.trucarDateTime(fechaEmision1);
		}
		else{
			this.fechaEmision1 = null;
		}
	}

	public Date getFechaVencimiento1() {
		return fechaVencimiento1;
	}

	public void setFechaVencimiento1(Date fechaVencimiento1) {
		if (fechaVencimiento1 != null){
			this.fechaVencimiento1 = UtilERP.trucarDateTime(fechaVencimiento1);
		}
		else{
			this.fechaVencimiento1 = null;
		}
	}

	public Date getFechaEmision2() {
		return fechaEmision2;
	}

	public void setFechaEmision2(Date fechaEmision2) {
		if (fechaEmision2 != null){
			this.fechaEmision2 = UtilERP.trucarDateTime(fechaEmision2);
		}
		else{
			this.fechaEmision2 = null;
		}
	}

	public Date getFechaVencimiento2() {
		return fechaVencimiento2;
	}

	public void setFechaVencimiento2(Date fechaVencimiento2) {
		if (fechaVencimiento2 != null){
			this.fechaVencimiento2 = UtilERP.trucarDateTime(fechaVencimiento2);
		}
		else{
			this.fechaVencimiento2 = null;
		}
	}

	public void simularFechasFacturacion(Date fechaNovedad, boolean primerFactura, boolean calcularSiguienteFecha) {
		this.setFechaNovedad(fechaNovedad);
		this.setFechaEmision1(null);
		this.setFechaVencimiento1(null);
		this.setFechaEmision2(null);
		this.setFechaVencimiento2(null);
		
		if (this.getFechaNovedad() != null && this.getFrecuencia() != null &&
			this.getDiaFechaEmision() != null && this.getDiaFechaVencimiento() != null){
			
			Calendar calendar = Calendar.getInstance();
			if (primerFactura && this.getPrimerFacturaEmiteCualquierDia()){
				this.setFechaEmision1(fechaNovedad);
			}
			else{
				calendar.setTime(this.getFechaNovedad());
				int dia = calendar.get(Calendar.DAY_OF_MONTH);
						
				if (dia > this.getDiaFechaEmision()){
					// se pasa al próximo periodo (mes, bimestre, etc)
					calendar.add(Calendar.MONTH, this.getFrecuencia().getMeses());
					calendar.set(Calendar.DAY_OF_MONTH, this.getDiaFechaEmision());
					this.setFechaEmision1(this.verificarDiaEnMes(calendar.getTime(), this.getDiaFechaEmision()));
				}
				else if (dia == this.getDiaFechaEmision()){
					this.setFechaEmision1(this.getFechaNovedad());
					
				}
				else{
					calendar.set(Calendar.DAY_OF_MONTH, this.getDiaFechaEmision());
					this.setFechaEmision1(this.verificarDiaEnMes(calendar.getTime(), this.getDiaFechaEmision()));				
				}
			}
							
			this.setFechaVencimiento1(this.calcularfechaVencimiento(this.getFechaEmision1()));
			
			if (calcularSiguienteFecha){
				// sirve para que el usuario veo las fechas de dos periodos, solo cuando se define el ciclo			
				calendar.setTime(this.getFechaEmision1());
				calendar.set(Calendar.DAY_OF_MONTH, this.getDiaFechaEmision());
				calendar.add(Calendar.MONTH, this.getFrecuencia().getMeses());
				this.setFechaEmision2(this.verificarDiaEnMes(calendar.getTime(), this.getDiaFechaEmision()));
				
				this.setFechaVencimiento2(this.calcularfechaVencimiento(this.getFechaEmision2()));				
			}
		}		
	}

	private Date calcularfechaVencimiento(Date fechaEmision){
		if (this.getDiaFechaVencimiento() > 0){
			if (this.getTipoVencimiento().equals(TipoVencimientoCicloFacturacion.diasEmision)){
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(fechaEmision);
				calendar.add(Calendar.DAY_OF_YEAR, this.getDiaFechaVencimiento());
				return calendar.getTime();
			}
			else if (this.getTipoVencimiento().equals(TipoVencimientoCicloFacturacion.Fijo)){
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(fechaEmision);
				int diaEmision = calendar.get(Calendar.DAY_OF_MONTH); 
				if (this.getDiaFechaVencimiento() <= diaEmision){					
					calendar.add(Calendar.MONTH, 1);
					calendar.set(Calendar.DAY_OF_MONTH, this.getDiaFechaVencimiento());
					return this.verificarDiaEnMes(calendar.getTime(), this.getDiaFechaVencimiento());
				}
				else{					
					calendar.set(Calendar.DAY_OF_MONTH, this.getDiaFechaVencimiento());
					return this.verificarDiaEnMes(calendar.getTime(), this.getDiaFechaVencimiento());
				}
			}
			else{
				throw new ValidationException("Error calculo fecha vencimiento");
			}
		}
		else{
			return fechaEmision;
		}
	}
	
	private Date verificarDiaEnMes(Date fecha, Integer dia) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fecha);
		if (calendar.get(Calendar.DAY_OF_MONTH) == dia){
			return fecha;
		}
		else{
			// si el día no coincide, se lo lleva al última día del mes anterior
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			calendar.add(Calendar.DAY_OF_MONTH, -1);
			return calendar.getTime();
		}
	}
	
	@Override
	public void propiedadesSoloLecturaAlEditar(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		super.propiedadesSoloLecturaAlEditar(propiedadesSoloLectura, propiedadesEditables, configuracion);
		
		propiedadesSoloLectura.add("frecuencia");
		propiedadesSoloLectura.add("diaFechaEmision");
	}
	
	@Override
	public void propiedadesSoloLecturaAlCrear(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion){
		super.propiedadesSoloLecturaAlCrear(propiedadesSoloLectura, propiedadesEditables, configuracion);
		
		propiedadesEditables.add("frecuencia");
		propiedadesEditables.add("diaFechaEmision");
	}

	public boolean puedeFacturar(Date momentoFacturacion, Date emisionFactura, boolean primerFactura) {
		boolean permiteFacturar = false;
		if (!this.getRestringeFacturacion()){
			this.simularFechasFacturacion(momentoFacturacion, primerFactura, false);
			if (this.getFechaEmision1().compareTo(emisionFactura) >= 0){
				permiteFacturar = true;
			}
		}
		else{
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(emisionFactura);
			calendar.add(Calendar.DAY_OF_YEAR, this.getDiasAntes() * -1);			
			if (momentoFacturacion.compareTo(calendar.getTime()) >= 0){
				permiteFacturar = true;
			}
		}		
		return permiteFacturar;
	}

	public Boolean getPrimerFacturaEmiteCualquierDia() {
		return primerFacturaEmiteCualquierDia;
	}

	public void setPrimerFacturaEmiteCualquierDia(Boolean primerFacturaEmiteCualquierDia) {
		if (primerFacturaEmiteCualquierDia != null){
			this.primerFacturaEmiteCualquierDia = primerFacturaEmiteCualquierDia;
		}
	}

	public TipoVencimientoCicloFacturacion getTipoVencimiento() {
		return tipoVencimiento == null ? TipoVencimientoCicloFacturacion.Fijo : this.tipoVencimiento;
	}

	public void setTipoVencimiento(TipoVencimientoCicloFacturacion tipoVencimiento) {
		if (tipoVencimiento != null){
			this.tipoVencimiento = tipoVencimiento;
		}
	}
}
