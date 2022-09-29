package org.openxava.base.model;


import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.calculators.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.validators.*;

import com.openxava.naviox.model.*;

@MappedSuperclass

public class ObjetoEstatico extends ObjetoNegocio{
	
	public static final String TABNAME_INACTIVOS = "Inactivos";
	public static final String CONDITION_INACTIVOS = "${activo} = 'f'";
	public static final String CONDITION_ACTIVOS = "${activo} = 't'";

	public static final String ROL_CAMBIAR_ESTADO = "Desactivar";
	
	public static ObjetoEstatico buscarPorCodigo(String codigo, String nombreClase){
		Query query = XPersistence.getManager().createQuery("from " + nombreClase + " where codigo = :codigo");
		query.setParameter("codigo", codigo);		
		query.setMaxResults(1);
		try{
			return (ObjetoEstatico)query.getSingleResult();
		}
		catch(NoResultException e){
			return null;
		}
	}
	
	public static ObjetoEstatico buscarPorCodigoError(String codigo, String nombreClase){
		ObjetoEstatico objeto = ObjetoEstatico.buscarPorCodigo(codigo, nombreClase);
		if (objeto != null){
			return objeto;
		}
		else{
			throw new ValidationException(nombreClase + ": no se encontró el código " + codigo );
		}
	}
	
	@Column(length=50, unique=true) @Required
	@SearchKey
    private String codigo;
	
	@Column(length=100) @Required
    private String nombre;
	
	@DefaultValueCalculator(value=TrueCalculator.class)
	@ReadOnly
	@Action(value="ObjetoEstaticoEstadoActivo.Cambiar", alwaysEnabled=true)
	private Boolean activo = true;
	
	public String getCodigo() {
		if (this.codigo == null) return "";
		else return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	public Boolean getActivo() {
		return this.activo == null ? Boolean.TRUE: this.activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}

	@Override
	public void asignarNumeracion(String numeracion, Long numero){
		super.asignarNumeracion(numeracion, numero);
		this.setCodigo(numeracion);
	}
	
	@Override
	public void onPreCreate(){
		super.onPreCreate();
		
		ConfiguracionEntidad entidad = ConfiguracionEntidad.buscarConfigurador(this.getClass().getSimpleName());
		if (entidad != null){
			if (entidad.getNumerador() != null){
				this.setCodigo("");
				entidad.getNumerador().numerarObjetoNegocio(this);
			}
		}
	}
		
	@Override 
	public Boolean soloLectura(){
		Boolean soloLectura = super.soloLectura();
		if (!soloLectura){
			soloLectura = ! this.getActivo();
		}
		return soloLectura;
	}
	
	/*private void validarCodigoRepetido(){
		if (!Is.emptyString(this.getCodigo())){
			String sqltext = "select codigo from " + Esquema.concatenarEsquema(this.getClass().getSimpleName()) + " where codigo = :codigo";
			if (!Is.emptyString(this.getId())){
				sqltext += " and id != '" + this.getId() + "'"; 
			}
			Query query = XPersistence.getManager().createNativeQuery(sqltext);
			query.setParameter("codigo", this.getCodigo());
			query.setMaxResults(1);
			try{
				String codigo = (String)query.getSingleResult();
				if (codigo != null){
					throw new ValidationException("Código repetido"); 
				}
			}
			catch(Exception e){
			}
		}
	}*/

	public void cambiarEstado() {
		User currentUser = User.find(Users.getCurrent());
		boolean usuarioAutorizado = false;
		
		Role adminRole = Role.find("admin");		
		if (adminRole != null){
			if (currentUser.getRoles().contains(adminRole)){
				usuarioAutorizado = true;
			}
			
		}
		else{
			throw new ValidationException("No se pudo encontrar el rol admin");
		}
		
		if (!usuarioAutorizado){
			Role cambiarEstadoRole = Role.find(ObjetoEstatico.ROL_CAMBIAR_ESTADO );
			if (cambiarEstadoRole != null){
				if (currentUser.getRoles().contains(cambiarEstadoRole)){
					usuarioAutorizado = true;
				}
			}
		}
		
		if (!usuarioAutorizado){
			throw new ValidationException("El usuario debe tener rol admin o " + ROL_CAMBIAR_ESTADO);
		}
		this.setActivo(! this.getActivo());
	}
	
	@Override
	public String toString(){
		String str = this.getCodigo();
		if (!Is.emptyString(str)){
			return str;
		}
		else{
			return super.toString();
		}
	}
	
	@Override
	public void propiedadesSoloLecturaAlEditar(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		super.propiedadesSoloLecturaAlEditar(propiedadesSoloLectura, propiedadesEditables, configuracion);
		propiedadesSoloLectura.add("codigo");
	}	
}
