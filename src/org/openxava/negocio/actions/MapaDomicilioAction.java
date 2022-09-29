package org.openxava.negocio.actions;

import org.openxava.negocio.model.Domicilio;
import org.openxava.view.View;

import com.clouderp.maps.actions.MapsCloudBaseAction;
import com.clouderp.maps.model.AddressCloud;
import com.clouderp.maps.model.MapCloud;

public class MapaDomicilioAction extends MapsCloudBaseAction{

	@Override
	public void buildMap(MapCloud map) {
		
		View viewDomicilio = null;		
		if (this.getView().getValue("domicilio") != null){
			viewDomicilio = this.getView().getSubview("domicilio");
		}
		else if (this.getView().getValue("domicilioLegal") != null){
			viewDomicilio = this.getView().getSubview("domicilioLegal").getSubview("domicilio");
		}
				
		if (viewDomicilio != null){		
			Double latitud = (Double)viewDomicilio.getValue("latitud");
			Double longitud = (Double)viewDomicilio.getValue("longitud");
			
			if (!this.coordenadasAsignadas(latitud, longitud)){
				AddressCloud address = this.buscarCoordenadas(map, viewDomicilio);
				latitud = address.getLatitud();
				longitud = address.getLongitud();
			}
			if (this.coordenadasAsignadas(latitud, longitud)){
				AddressCloud address = map.addAddress(latitud, longitud);
				address.setLabel(this.getView().getValueString("nombre"));
				address.setDescription(viewDomicilio.getValueString("direccion"));
			}					
		}
	}
	
	private boolean coordenadasAsignadas(Double latitud, Double longitud){
		boolean asignadas = false;
		if ((latitud != null) && (longitud != null)){
			if ((latitud != 0) && (longitud != 0)){
				asignadas = true;
			}
		}
		return asignadas;
	}
	
	private AddressCloud buscarCoordenadas(MapCloud map, View viewDomicilio){
		AddressCloud address = Domicilio.createAddressFromView(viewDomicilio);
		try{			
			map.geocoding(address);			
		}
		catch(Exception e){
			addError("No se pudo encontrar las coordenadas para el domicilio: " + e.getMessage());
		}
		return address;
	}

}
