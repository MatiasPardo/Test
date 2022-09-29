package com.clouderp.maps.actions.polygon;

import java.util.Collection;

import javax.servlet.http.HttpSession;

import org.openxava.distribucion.model.AsignacionZonaReparto;
import org.openxava.distribucion.model.ZonaReparto;
import org.openxava.jpa.XPersistence;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;

import com.clouderp.maps.model.AddressCloud;
import com.clouderp.maps.model.IMapPolygonAction;
import com.clouderp.maps.model.IObjectMapCloud;
import com.clouderp.maps.model.MapCloud;

public class AsignarZonaRepartoPolygonAction implements IMapPolygonAction{
	
	public final static String PARAMETROZONA = "mapcloud.polygon.idzonareparto"; 
	
	@Override
	public String doPolygonAction(Collection<AddressCloud> selected, MapCloud map, HttpSession session) {
		String idZona = (String)session.getAttribute(AsignarZonaRepartoPolygonAction.PARAMETROZONA);
		if (!Is.emptyString(idZona)){
			ZonaReparto zona = XPersistence.getManager().find(ZonaReparto.class, idZona);
			for(AddressCloud address: selected){
				AsignacionZonaReparto asignacion = XPersistence.getManager().find(AsignacionZonaReparto.class, address.getCodigo());
				asignacion.asignarZonaReparto(zona);
				((IObjectMapCloud)asignacion).addressRefresh(address);  
			}
			return "Zona asignada " + zona.getNombre();
		}
		else{
			throw new ValidationException("Zona no asignada");
		}
	}
}
