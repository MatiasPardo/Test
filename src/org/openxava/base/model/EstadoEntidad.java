package org.openxava.base.model;



import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.jpa.XPersistence;
import org.openxava.validators.ValidationException;



@Entity

@Views({
	@View(name="Simple",
		members="nombre")	
})

public class EstadoEntidad extends ObjetoNegocio{
	
	public static EstadoEntidad buscarPorCodigo(String codigo, String tipoEntidad){
		Query query = XPersistence.getManager().createQuery("from EstadoEntidad where codigo = :codigo and " +
								"entidad.entidad = :tipoEntidad");
		query.setMaxResults(1);
		query.setParameter("codigo", codigo);
		query.setParameter("tipoEntidad", tipoEntidad);
		query.setFlushMode(FlushModeType.COMMIT);
		List<?> result = query.getResultList();
		if (result.isEmpty()){
			throw new ValidationException("No existe el estado " + codigo + " para la entidad " + tipoEntidad);
		}
		else{
			return (EstadoEntidad)result.get(0);
		}
	}
	
	@SearchKey
	@Column(length=50) @Required
	private String nombre;
	
	@Column(length=20) 
	private String codigo;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	@ReferenceView("Simple")
	private ConfiguracionEntidad entidad;
	
	@Required
	private Estado estadoTransaccional;

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public ConfiguracionEntidad getEntidad() {
		return entidad;
	}

	public void setEntidad(ConfiguracionEntidad entidad) {
		this.entidad = entidad;
	}

	public Estado getEstadoTransaccional() {
		return estadoTransaccional;
	}

	public void setEstadoTransaccional(Estado estadoTransaccional) {
		this.estadoTransaccional = estadoTransaccional;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
	
	@Override
	public void propiedadesSoloLecturaAlEditar(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		super.propiedadesSoloLecturaAlEditar(propiedadesSoloLectura, propiedadesEditables, configuracion);
		propiedadesSoloLectura.add("codigo");
	}

}
