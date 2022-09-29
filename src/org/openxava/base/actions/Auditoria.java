package org.openxava.base.actions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.validators.ValidationException;

@Entity

public class Auditoria extends ObjetoNegocio{
	
	public static Auditoria modificarAtributoConAuditoria(ObjetoNegocio objeto, String nombreAtributo, String descripcionAtributo, Object valorNuevo, Class<?> tipoValorNuevo){
		if (!Is.emptyString(nombreAtributo)){
			String methodName = nombreAtributo.substring(0, 1).toUpperCase() + nombreAtributo.substring(1);
			
			Class<?> tipoParametro = tipoValorNuevo;
			
			try{
				Method metodoSet = objeto.getClass().getMethod("set" + methodName, tipoParametro);
				Method metodoGet = objeto.getClass().getMethod("get" + methodName);
				Object valorAnterior = metodoGet.invoke(objeto);
				if (Is.equal(valorAnterior, valorNuevo)){
					return null;
				}
				
				objeto.inicioCambioAtributo();
				metodoSet.invoke(objeto, valorNuevo);
				objeto.finCambioAtributo();
				
				String strAnterior = "";
				if (valorAnterior != null) strAnterior = valorAnterior.toString();
				String strNuevo = "";
				if (valorNuevo != null) strNuevo = valorNuevo.toString();
				
				if (valorAnterior instanceof Boolean){
					if ((Boolean)valorAnterior){
						strAnterior = "Si";
						strNuevo = "No";
					}
					else{
						strAnterior = "No";
						strNuevo = "Si";
					}
				}
				else if (valorAnterior instanceof Date){
					SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
					strAnterior = format.format((Date)valorAnterior);
					strNuevo = format.format((Date)valorNuevo);
				}
				
				Auditoria auditoria = Auditoria.generarRegistroAuditoria(objeto, descripcionAtributo, strAnterior, strNuevo);
				return auditoria;
			}
			catch(InvocationTargetException e){
				if ((e.getTargetException() != null) && (e.getTargetException() instanceof ValidationException)){
					throw (ValidationException)e.getTargetException();
				}
				else{
					throw new ValidationException("Error al modificar atributo: " + e.toString());
				}
			}
			catch(Exception e){
				throw new ValidationException("Error al modificar atributo: " + e.toString());
			}
		}
		else{
			throw new ValidationException("Falta asignar nombreAtributo");
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Collection<Auditoria> registrosAuditoria(ObjetoNegocio objeto){
		if (!Is.emptyString(objeto.getId())){
			Query query = XPersistence.getManager().createQuery("from Auditoria where entidad = :id order by fechaCreacion desc");
			query.setParameter("id", objeto.getId());
			return query.getResultList();
		}
		else{
			return new ArrayList<Auditoria>();
		}		
	}
	
	public static Auditoria generarRegistroAuditoria(ObjetoNegocio objeto, String atributo, String valorAnterior, String valorNuevo){
		Auditoria aud = new Auditoria();
		aud.setEntidad(objeto.getId());
		aud.setTipoEntidad(objeto.getClass().getSimpleName());
		aud.setModificacion(atributo);
		aud.setValorAnterior(valorAnterior);
		aud.setValorNuevo(valorNuevo);
		return aud;
	}
	
	@Hidden
	@ReadOnly
	@Required
	@Column(length=32)
	private String entidad;
	
	@ReadOnly
	@Required
	@Column(length=100)
	private String tipoEntidad;
	
	@Required
	@Column(length=50)
	private String modificacion;
	
	@Column(length=50)
	private String valorAnterior;
		
	@Column(length=50)
	private String valorNuevo;

	public String getEntidad() {
		return entidad;
	}

	public void setEntidad(String entidad) {
		this.entidad = entidad;
	}

	public String getTipoEntidad() {
		return tipoEntidad;
	}

	public void setTipoEntidad(String tipoEntidad) {
		this.tipoEntidad = tipoEntidad;
	}

	public String getModificacion() {
		return modificacion;
	}

	public void setModificacion(String modificacion) {
		this.modificacion = modificacion;
	}

	public String getValorAnterior() {
		return valorAnterior;
	}

	public void setValorAnterior(String valorAnterior) {
		this.valorAnterior = valorAnterior;
	}

	public String getValorNuevo() {
		return valorNuevo;
	}

	public void setValorNuevo(String valorNuevo) {
		this.valorNuevo = valorNuevo;
	}
}
