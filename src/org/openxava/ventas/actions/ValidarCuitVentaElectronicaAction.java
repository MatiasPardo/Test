package org.openxava.ventas.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.afip.model.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;


import com.allin.interfacesafip.model.*;

public class ValidarCuitVentaElectronicaAction extends ViewBaseAction{

	@Override
	public void execute() throws Exception {
		if (this.getView().isEditable()){
			TipoDocumento tipoDoc = (TipoDocumento)getView().getValue("tipoDocumento");
			if (tipoDoc == null){
				addError("Debe ingresar el tipo de documento");
				return;
			}
			String nroDoc = (String)getView().getValue("cuit");
			if (nroDoc != null){
				if (!nroDoc.isEmpty()){	 
					FacturaElectronicaAfip fe = new FacturaElectronicaAfip();
					fe.runServerFE();				 
					AfipPadronPersonas afip = new AfipPadronPersonas();
					afip.setPathAplicacionFE(fe.getPathFacturaElectronica());
					 
					if (tipoDoc.equals(TipoDocumento.DNI)){
						addError("El tipo de documento debe ser CUIT/CUIL");
					}
					else{
						afip.validarCUIT(nroDoc);
					}
					if ((this.getErrors().isEmpty()) && (afip.getErrores().isEmpty())){
						if (afip.getActivo()){
							for(String msg: afip.getDetalleCompleto()){							 
								addMessage(msg);							 
							}						 
						}
						else{
							 for(String msg: afip.getDetalleCompleto()){
								 addWarning(msg);
							 }
						}
						for(String msg: afip.getAdvertencias()){
							 addWarning(msg);
						}
						 
						 String nombre = afip.getRazonSocial();
						 if (nombre.length() > 100) nombre = nombre.substring(0, 99); 
						 getView().setValue("razonSocial", nombre);
						 if (afip.getDomicilio().getProvincia() != null){
							 Ciudad ciudad = Ciudad.buscarPor(afip.getDomicilio().getProvincia().getCodigo(), afip.getDomicilio().getCiudadAfip(), afip.getDomicilio().getCodigoPostal().toString());											
							 if (ciudad != null){
								 String codigoPostal = (String)this.getView().getSubview("ciudad").getValue("codigoPostal");
								 if (codigoPostal != null){
									 if (codigoPostal.equals(ciudad.getCodigoPostal())){
										ciudad = null;
									 } 
								 }
								 if (ciudad != null){
									 
									 Map<String, Object> valuesCiudad = new HashMap<String, Object>();
									 valuesCiudad.put("codigoPostal", ciudad.getCodigoPostal());
									 valuesCiudad.put("ciudad", ciudad.getCiudad());
									 valuesCiudad.put("codigo", ciudad.getCodigo());
									
									 Map<String, Object> valuesProvincia = new HashMap<String, Object>();
									 valuesProvincia.put("codigo", ciudad.getProvincia().getCodigo());
									 valuesProvincia.put("provincia", ciudad.getProvincia().getProvincia());
									 valuesCiudad.put("provincia", valuesProvincia);
									
									 this.getView().getSubview("ciudad").setValues(valuesCiudad);
									 
									 this.getView().getSubview("ciudad").setValue("codigo", ciudad.getCodigo());
								 }	 
							 }
							 this.getView().setValue("direccion", afip.getDomicilio().getDireccion());
						 }
						 else{
							 addError("Sin provincia");
						 }
						 
						 if (!AfipPosicionIVA.SINASIGNAR.equals(afip.getIva())){
							 
							try{ 
								getView().getSubview("posicionIva").setValueNotifying("codigo", afip.getIva().getCodigoPosicionIVA());	
							}
							catch(ElementNotFoundException e){							
							}
						 }
					 }
					 else{
						 for(String msg: afip.getErrores()){
							 addError(msg);
						 }
					 }
				 }
				 else{
					 addError("Número de documento no asignado");
				 }	
			 }
			 else{
				 addError("Número de documento no asignado");
			 }
		}	
	}
}
