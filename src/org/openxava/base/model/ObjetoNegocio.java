package org.openxava.base.model;

import java.lang.reflect.*;
import java.util.*;

import javax.persistence.*;

import org.apache.commons.beanutils.*;
import org.hibernate.annotations.*;
import org.openxava.annotations.*;
import org.openxava.util.*;
import org.openxava.validators.*;

@MappedSuperclass
public class ObjetoNegocio {
	
	@Id @GeneratedValue(generator="system-uuid") @Hidden 
	@GenericGenerator(name="system-uuid", strategy = "uuid")
	@Column(length=32)
	private String id = "";

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	
	@Column(length=50)
	@ReadOnly
	@DisplaySize(value=25)
	private String usuario = new String("");
	
	public String getUsuario() {
		return usuario;
	}
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	
	@ReadOnly
	@Stereotype("DATETIME")	
	private Date fechaCreacion = new java.util.Date();
	
	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		if (fechaCreacion != null){
			this.fechaCreacion = fechaCreacion;
		}
	}

	@Override
	public boolean equals (Object obj){
		if (obj == null){
			return false;
		}
		else if (obj instanceof ObjetoNegocio){
			if (!this.getId().equals(new String(""))){
				return (((ObjetoNegocio)obj).getId().equals(this.getId()));
			}
			else{
				return super.equals(obj);
			}
		}
		else{
			return super.equals(obj);
		}
	}
	
	@PrePersist
	protected void onPrePersist() {
		this.setUsuario(Users.getCurrent());
		this.setFechaCreacion(new java.util.Date());
	}
	
	@PreUpdate
	protected void onPreUpdate() {
	}
	
	@PostUpdate
	protected void onPostUpdate(){
	}
	
	@PostPersist
	protected void onPostPersist(){
	}
	
	@PostRemove
	protected void onPostRemove(){
	}
	
	@PreCreate
	public void onPreCreate(){
	}
	
	@PreDelete
	public void onPreDelete(){
	
	}
	
	public Boolean soloLectura(){
		return false;
	}
	
	public void asignarNumeracion(String numeracion, Long numero){
	}
	
	public void copiarPropiedades(Object objeto){
		try {			
			BeanUtils.copyProperties(this, objeto);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new ValidationException("Error al copiar atributos"); 
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new ValidationException("Error al copiar atributos");
		}
		this.setId(null);
	}
	
	@Override
	public String toString(){
		return this.getClass().getSimpleName();
	}

	public void propiedadesSoloLecturaAlEditar(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
	}
	
	public void propiedadesSoloLecturaAlCrear(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion){
	}
	
	public void propiedadesOcultas(List<String> ocultar, List<String> visualizar){		
	}
	
	public String viewName(){
		return null;
	}

	public ObjetoNegocio generarCopia() {
		throw new ValidationException("No puede copiarse");
	}
	
	public boolean esNuevo(){
		return Is.emptyString(this.getId());
	}

	@Transient
	private boolean cambiandoAtributo = false;

	public boolean estaCambiandoAtributo() {
		return cambiandoAtributo;
	}

	public void inicioCambioAtributo() {
		this.cambiandoAtributo = true;
	}
	
	public void finCambioAtributo(){
		this.cambiandoAtributo = false;
	}
	
	public void permiteCambiarAtributo(String nombreVista) {
	}
}
