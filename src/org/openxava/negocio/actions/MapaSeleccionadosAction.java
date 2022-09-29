package org.openxava.negocio.actions;

import java.util.Map;

import org.openxava.model.MapFacade;

import com.clouderp.maps.actions.MapsCloudBaseAction;
import com.clouderp.maps.model.AddressCloud;
import com.clouderp.maps.model.IObjectMapCloud;
import com.clouderp.maps.model.MapCloud;

public class MapaSeleccionadosAction extends MapsCloudBaseAction{

	@Override
	public void buildMap(MapCloud map) {
		if (this.getSelectedKeys().length > 0){
			for(@SuppressWarnings("rawtypes") Map key: this.getSelectedKeys()){
				try{
					IObjectMapCloud objetoMapCloud = (IObjectMapCloud)MapFacade.findEntity(this.getTab().getModelName(), key);
					AddressCloud address = objetoMapCloud.addressMapCloud(map);
					if (address == null){
						addError("Comprobante sin coordenadas: " + objetoMapCloud.toString());
					}
				}
				catch(Exception e){					
				}
			}
			this.getTab().deselectAll();
		}
		else{
			this.addError("sin_seleccionar_items");
		}				
	}

}
