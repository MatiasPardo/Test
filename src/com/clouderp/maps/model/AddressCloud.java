package com.clouderp.maps.model;

import org.openxava.util.Is;

public class AddressCloud {
	
	private String codigo = "";
	
	private boolean asignado = false;
	
	private String description;
	
	private String label;
	
	private double latitud;
	
	private double longitud;

	private String calle;
	
	private String localidad;
	
	private String provincia;

	private String pais;
	
	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public boolean isAsignado() {
		return asignado;
	}

	public void setAsignado(boolean asignado) {
		this.asignado = asignado;
	}

	public double getLatitud() {
		return latitud;
	}

	public void setLatitud(double latitud) {
		this.latitud = latitud;
	}

	public double getLongitud() {
		return longitud;
	}

	public void setLongitud(double longitud) {
		this.longitud = longitud;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCalle() {
		return calle;
	}

	public void setCalle(String calle) {
		this.calle = calle;
	}

	public String getLocalidad() {
		return localidad;
	}

	public void setLocalidad(String localidad) {
		this.localidad = localidad;
	}

	public String getProvincia() {
		return provincia;
	}

	public void setProvincia(String provincia) {
		this.provincia = provincia;
	}

	public String getPais() {
		return pais;
	}

	public void setPais(String pais) {
		this.pais = pais;
	}
	
	public String completAddress(){
		StringBuffer direccion = new StringBuffer();
		
		if(!Is.emptyString(this.getCalle())) {
			direccion.append(this.getCalle().replace(" ", ",")).append(",");
		}
		if(!Is.emptyString(this.getProvincia())) {
			direccion.append(this.getProvincia().replace(" ", ","));
		}
		if(!Is.emptyString(this.getPais())) {
			direccion.append(",").append(this.getPais().replace(" ", ","));
		}
		if(!Is.emptyString(this.getLocalidad())) {
			direccion.append(",").append(this.getLocalidad().replace(" ", ","));
		}
		return direccion.toString();
	}
	
	public boolean emptyCoordinates(){
		return ! ((this.getLatitud() != 0) && (this.getLongitud() != 0));		
	}
}
