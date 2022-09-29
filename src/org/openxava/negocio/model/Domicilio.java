package org.openxava.negocio.model;

import javax.persistence.*;
import javax.validation.constraints.Digits;

import org.openxava.annotations.*;
import org.openxava.base.model.*;

import com.clouderp.maps.model.AddressCloud;



@Entity

@Views({
	@View(members="direccion;" +
					"ciudad"),
	@View(name="Simple",
		members="direccion;" +
			"ciudad;"), 
	@View(name="Observaciones",
		members="direccion;" +
			"ciudad; observaciones;" + 
			"Geolocalizacion[latitud, longitud]"), 
	@View(name="CodigoExterno", 
		members="codigoExterno")
})

@Tab(properties="direccion, ciudad.codigoPostal, ciudad.ciudad, provincia.provincia")

public class Domicilio extends ObjetoNegocio{

	public static AddressCloud createAddressFromView(org.openxava.view.View viewDomicilio){
		AddressCloud address = new AddressCloud();
		address.setCalle(viewDomicilio.getValueString("direccion"));
		address.setLocalidad(viewDomicilio.getValueString("ciudad.ciudad"));
		address.setProvincia(viewDomicilio.getValueString("ciudad.provincia.provincia"));
		return address;
	}
	
	@Column(length=100) 
	@Required
	@SearchKey
	private String direccion;
	    
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @NoFrame @NoCreate @NoModify
    @ReferenceView("Simple")
    private Ciudad ciudad;
    
    @ManyToOne(optional=true, fetch=FetchType.LAZY)
    @NoFrame @NoCreate @NoModify
    @ReferenceView("Simple")
    private Provincia provincia;
    
    @Stereotype("MEMO")
    private String observaciones;
    
    @Column(length=50) 
    @Hidden
    @ReadOnly	
    private String codigoExterno;
    
    @Hidden
    @Digits(integer=4, fraction=7)
    @Action(value="MapaDomicilio.buscar")
    private double latitud = 0;
    
    @Action(value="MapaDomicilio.verMapa")
    @Hidden
    @Digits(integer=4, fraction=7)
    private double longitud = 0;
    
    public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		int max = 100;
		if (direccion.length() > max){
			this.direccion = direccion.substring(0, max - 1);
		}
		else{
			this.direccion = direccion;
		}	
	}

	public Ciudad getCiudad() {
		return ciudad;
	}

	public void setCiudad(Ciudad ciudad) {
		this.ciudad = ciudad;
	}

	public String getCodigoExterno() {
		return codigoExterno;
	}

	public void setCodigoExterno(String codigoExterno) {
		this.codigoExterno = codigoExterno;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}

	public Provincia getProvincia() {
		return provincia;
	}

	public void setProvincia(Provincia provincia) {
		this.provincia = provincia;
	}
	
	@Override
	protected void onPrePersist() {
		super.onPrePersist();
		
		asignarProvinicia();
	}
	
	@Override
	protected void onPreUpdate() {
		super.onPreUpdate();
		
		asignarProvinicia();
	}
	
	private void asignarProvinicia(){
		if (this.getCiudad() != null){
			this.setProvincia(this.getCiudad().getProvincia());
		}
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

	public boolean tieneCoordenadasGPS() {
		if ((this.getLatitud() != 0) && (this.getLongitud() != 0)){
			return true;
		}
		else{
			return false;
		}
	}
}
