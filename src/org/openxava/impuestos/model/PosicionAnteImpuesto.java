package org.openxava.impuestos.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.jpa.*;

import com.allin.interfacesafip.model.AfipPosicionIVA;


@Entity

public class PosicionAnteImpuesto {
	
	public final static String CONSUMIDORFINAL = "CF"; 
	
	public final static String EXTERIOR = "EXTERIOR";
	
	public final static String MONOTRIBUTISTA = "M";
		
	public final static PosicionAnteImpuesto buscarPorCodigo(String codigo){
		PosicionAnteImpuesto posicion = (PosicionAnteImpuesto)XPersistence.getManager().find(PosicionAnteImpuesto.class, codigo);		
		return posicion;
	}
	
	// Códigos posibles (Ver clase AfipPosicionIVA): RI, CF, M, E, NC
	@Id @Hidden
	@Column(length=10)
	private String codigo;
	
	@ReadOnly
	@Column(length=50)
	private String descripcion;
	
	private Boolean presentacionesImpositivas = Boolean.TRUE;  
	
	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public boolean esResponsableInscripto(){		
		return this.getCodigo().equals(AfipPosicionIVA.RESPONSABLEINSCRIPTO.getCodigoPosicionIVA());	
	}
	
	public boolean esMonotributista(){
		return this.getCodigo().equals(AfipPosicionIVA.MONOTRIBUTO.getCodigoPosicionIVA());	
	}
	
	@PreRemove 
	private void validateOnRemove() {
		// no se pueden borrar
		throw new IllegalStateException("");
	}
	
	public boolean calculaIvaCompras(){
		if (this.getPresentacionesImpositivas()){
			return ((!this.getCodigo().equals("E")) && (!this.getCodigo().equals("NC")));
		}
		else{
			return false;
		}
	}
	
	@Override
	public String toString(){
		return this.getDescripcion();
	}
	
	public boolean calculaIvaVentas(){
		boolean calcula = this.getPresentacionesImpositivas();
		return calcula;
	}

	public Boolean getPresentacionesImpositivas() {
		return presentacionesImpositivas == null ? Boolean.TRUE : this.presentacionesImpositivas;
	}

	public void setPresentacionesImpositivas(Boolean presentacionesImpositivas) {
		this.presentacionesImpositivas = presentacionesImpositivas;
	}
	
	public PosicionAnteRetencion posicion(){
		if (this.esResponsableInscripto()){
			return PosicionAnteRetencion.Inscripto;
		}
		else{
			return PosicionAnteRetencion.NoInscripto;
		}
	}
}
