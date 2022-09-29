package org.openxava.base.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.calculators.*;
import org.openxava.jpa.*;
import org.openxava.validators.*;

@Entity

@View(
	members="Principal[" +
				"fechaCreacion;" +
				"origen; destino;" +
				"automatico, confirmaDestino;" +
				"];" + 
			"Cantidades[permiteSuperarCantidad];" 	
)

@Tab(properties="origen.entidad, automatico, destino.entidad")

public class ConfiguracionCircuito extends ObjetoNegocio{
	
	public static void buscarCircuitosAutomaticosPorOrigen(String entidad, Collection<ConfiguracionCircuito> circuitos) {
		Query query = XPersistence.getManager().createQuery("from ConfiguracionCircuito where origen.entidad = :origen and automatico = :automatico");
		query.setParameter("origen", entidad);
		query.setParameter("automatico", Boolean.TRUE);
		List<?> result = query.getResultList();
		for(Object circuito: result){
			circuitos.add((ConfiguracionCircuito)circuito);
		}
	}
	
	public static ConfiguracionCircuito buscarCircuito(String entidadOrigen, String entidadDestino){
		Query query = XPersistence.getManager().createQuery("from ConfiguracionCircuito where origen.entidad = :origen and destino.entidad = :destino");
		query.setParameter("origen", entidadOrigen);
		query.setParameter("destino", entidadDestino);
		query.setMaxResults(1);
		List<?> result = query.getResultList();
		if (!result.isEmpty()){
			return (ConfiguracionCircuito)result.get(0);
		}
		else{
			return null;
		}
	}
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="entidad")
	private ConfiguracionEntidad origen;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="entidad")
	private ConfiguracionEntidad destino;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean automatico = Boolean.FALSE;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean confirmaDestino = Boolean.FALSE;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean permiteSuperarCantidad = Boolean.FALSE;
	
	public ConfiguracionEntidad getOrigen() {
		return origen;
	}

	public void setOrigen(ConfiguracionEntidad origen) {
		this.origen = origen;
	}

	public ConfiguracionEntidad getDestino() {
		return destino;
	}

	public void setDestino(ConfiguracionEntidad destino) {
		this.destino = destino;
	}

	public Boolean getAutomatico() {
		return automatico;
	}

	public void setAutomatico(Boolean automatico) {
		this.automatico = automatico;
	}
	
	public Boolean getConfirmaDestino() {
		return confirmaDestino;
	}

	public void setConfirmaDestino(Boolean confirmaDestino) {
		this.confirmaDestino = confirmaDestino;
	}

	@PreDelete
	public void onPreDelete(){
		super.onPreDelete();
		
		throw new ValidationException("No se puede borrar");
	}
	
	@Override
	public String toString(){
		if ((this.getOrigen() != null) && (this.getDestino() != null)){
			return this.getOrigen().getEntidad() + " - " + this.getDestino().getEntidad();
		}
		else{
			return super.toString();
		}
	}

	public Boolean getPermiteSuperarCantidad() {
		return permiteSuperarCantidad == null ? Boolean.FALSE : this.permiteSuperarCantidad;
	}

	public void setPermiteSuperarCantidad(Boolean permiteSuperarCantidad) {
		if (permiteSuperarCantidad == null){
			this.permiteSuperarCantidad = Boolean.FALSE;
		}
		else{
			this.permiteSuperarCantidad = permiteSuperarCantidad;
		}
	}
}
