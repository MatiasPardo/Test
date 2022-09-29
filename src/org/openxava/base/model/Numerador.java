package org.openxava.base.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.actions.*;
import org.openxava.calculators.*;

@Entity

@Views({
	@View(name="Transaccion", members="nombre"),
	@View(name="Simple",
		members="nombre, proximoNumero, cantidadDigitos, prefijo;"),
	@View(name="AltaNumerador", 
		members="nombre;" +
				"empresa;" + 
				"ProximoNumero[proximoNumero; modificadoPor, fechaModificacion];" +
				"cantidadDigitos;" + 
				"prefijo;")
})

@Tabs({
	@Tab(properties="nombre, entidad.entidad, proximoNumero, modificadoPor, fechaModificacion, cantidadDigitos, prefijo")
})

public class Numerador extends ObjetoNegocio{
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="entidad")
	@ReadOnly
	private ConfiguracionEntidad entidad;
	
	@Column(length=50) 
	@SearchKey
	private String nombre = "";
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	private Empresa empresa;
	
	@DefaultValueCalculator(value=IntegerCalculator.class,
							properties={@PropertyValue(name="value", value="1")})
	@Required
	@OnChange(OnChangeProximoNumeroEnNumerador.class)
	private Long proximoNumero;
		
	@Column(length=50)
	@ReadOnly
	private String modificadoPor;
	
	@ReadOnly
	@Stereotype("DATETIME")
	private Date fechaModificacion;
	
	@DefaultValueCalculator(value=IntegerCalculator.class,
			properties={@PropertyValue(name="value", value="0")})
	private Integer cantidadDigitos = 0;
	
	private String prefijo = new String("");
	
	@Version
	@Hidden
	int version;

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Long getProximoNumero() {
		return proximoNumero;
	}

	public void setProximoNumero(Long proximoNumero) {
		this.proximoNumero = proximoNumero;
	}
	
	public Integer getCantidadDigitos() {
		return cantidadDigitos == null ? 0 : cantidadDigitos;
	}

	public void setCantidadDigitos(Integer cantidadDigitos) {
		this.cantidadDigitos = cantidadDigitos;
	}

	public String getPrefijo() {
		return prefijo == null ? "" : prefijo;
	}

	public void setPrefijo(String prefijo) {
		this.prefijo = prefijo;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	
	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public ConfiguracionEntidad getEntidad() {
		return entidad;
	}

	public void setEntidad(ConfiguracionEntidad entidad) {
		this.entidad = entidad;
	}

	public String getModificadoPor() {
		return modificadoPor;
	}

	public void setModificadoPor(String modificadoPor) {
		this.modificadoPor = modificadoPor;
	}

	public Date getFechaModificacion() {
		return fechaModificacion;
	}

	public void setFechaModificacion(Date fechaModificacion) {
		this.fechaModificacion = fechaModificacion;
	}

	public void numerarObjetoNegocio(ObjetoNegocio objeto){
		Long numero = this.getProximoNumero();
	    objeto.asignarNumeracion(this.formatearNumero(numero), numero);
		this.setProximoNumero(numero + 1);
	}

	private String formatearNumero(Long numero) {
		String numeroStr = numero.toString();
		
		String numeroFormateado = this.getPrefijo();
		int i = 0;
		while (i < (this.getCantidadDigitos() - numeroStr.length())){
			numeroFormateado += "0";
			i++;
		}
		numeroFormateado += numeroStr; 
		return numeroFormateado;
	}
}
