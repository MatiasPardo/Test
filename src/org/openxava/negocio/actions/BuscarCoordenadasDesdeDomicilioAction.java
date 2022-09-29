package org.openxava.negocio.actions;

import org.openxava.actions.ViewBaseAction;
import org.openxava.base.model.Esquema;
import org.openxava.negocio.model.Domicilio;
import org.openxava.view.View;

import com.clouderp.maps.model.*;

public class BuscarCoordenadasDesdeDomicilioAction extends ViewBaseAction{

	@Override
	public void execute() throws Exception {
		View viewDomicilio = null;		
		if (this.getView().getValue("domicilio") != null){
			viewDomicilio = this.getView().getSubview("domicilio");
		}
		else if (this.getView().getValue("domicilioLegal") != null){
			viewDomicilio = this.getView().getSubview("domicilioLegal").getSubview("domicilio");
		}
		
		MapCloud map = Esquema.getEsquemaApp().crearMapCloud();
		try{
			AddressCloud address = Domicilio.createAddressFromView(viewDomicilio);
			map.geocoding(address);
			if (!address.emptyCoordinates()){
				viewDomicilio.setValue("latitud", address.getLatitud());
				viewDomicilio.setValue("longitud", address.getLongitud());
				addMessage("Se encontraron las coordenadas. Recuerde grabar");
			}
			else{
				addError("No se pudo encontrar las coordenadas para el domicilio"); 
			}
		}
		catch(Exception e){
			addError("No se pudo encontrar las coordenadas para el domicilio: " + e.getMessage());
		}
		
	}
	
	
	
}
