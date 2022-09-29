package org.openxava.afip.actions;

import org.openxava.actions.*;
import org.openxava.afip.model.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.view.*;

import com.allin.interfacesafip.model.*;

public class ValidarPadronPersonasAfipAction extends ViewBaseAction {

	@Override
	public void execute() throws Exception {
		TipoDocumento tipoDoc = (TipoDocumento)getView().getValue("tipoDocumento");
		if (tipoDoc == null){
			addError("Debe ingresar el tipo de documento");
			return;
		}
		String nroDoc = (String)getView().getValue("numeroDocumento");
		if (nroDoc != null){
			if (!nroDoc.isEmpty()){	 
				FacturaElectronicaAfip fe = new FacturaElectronicaAfip();
				fe.runServerFE();				 
				AfipPadronPersonas afip = new AfipPadronPersonas();
				afip.setPathAplicacionFE(fe.getPathFacturaElectronica());
				 
				if (tipoDoc.equals(TipoDocumento.DNI)){
					TipoPersonaAfip tipo = (TipoPersonaAfip)getView().getValue("tipo");
					if (tipo == null){
						addError("Para validar un DNI debe asignar el tipo de persona");
					}
					else if (!tipo.esPersonaFisica()){
						addError("Para validar un DNI debe asignar Masculino o Femenino");
					}	
					else{						
						afip.validarDNI(nroDoc, tipo.getTipoCuit());
					}						
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
					 getView().setValue("nombre", nombre);
					 try{
						 getView().setValue("nombreFantasia", nombre);
					 }
					 catch(Exception e){
					 }
					 if (afip.getDomicilio().getProvincia() != null){
						 Ciudad ciudad = Ciudad.buscarPor(afip.getDomicilio().getProvincia().getCodigo(), afip.getDomicilio().getCiudadAfip(), afip.getDomicilio().getCodigoPostal().toString());
						  
						 View subViewDomicilio = null;
						 if (getView().getValue("domicilio") != null){
							 subViewDomicilio = getView().getSubview("domicilio");
						 }
						 else if (getView().getValue("domicilioLegal") != null){
							 subViewDomicilio = getView().getSubview("domicilioLegal").getSubview("domicilio");
							 getView().getSubview("domicilioLegal").setValue("codigo", "1");
							 getView().getSubview("domicilioLegal").setValue("nombre", "Legal");
							 getView().getSubview("domicilioLegal").setValue("principal", Boolean.TRUE);
						 }
						 else{
							 throw new ValidationException("No se encontró subview domicilio");
						 }
						 if (ciudad != null){
							 String codigoPostal = (String)subViewDomicilio.getSubview("ciudad").getValue("codigoPostal");
							 if (codigoPostal != null){
								 if (codigoPostal.equals(ciudad.getCodigoPostal())){
									ciudad = null;
								 } 
							 }
							 if (ciudad != null){
								 subViewDomicilio.getSubview("ciudad").setValueNotifying("codigo", ciudad.getCodigo());
							 }	 
						 }
						 subViewDomicilio.setValue("direccion", afip.getDomicilio().getDireccion());
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
