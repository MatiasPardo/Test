package org.openxava.base.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.model.*;
import org.openxava.model.meta.MetaMember;
import org.openxava.util.*;
import org.openxava.validators.*;

public class CambiarAtributoAction extends ViewBaseAction implements IChainAction{
	
	private String nombreAtributo;
	
	private String descripcionAtributo;
	
	public String getNombreAtributo() {
		if (Is.emptyString(this.nombreAtributo)){
			if (this.getView().getMetaMembers().size() == 1){
				for(Object metaMember: this.getView().getMetaMembers()){
					this.nombreAtributo = ((MetaMember)metaMember).getName();
					break;
				}
			}
		}
		return nombreAtributo;
	}

	public void setNombreAtributo(String nombreAtributo) {
		this.nombreAtributo = nombreAtributo;
	}

	public String getDescripcionAtributo() {
		if (Is.emptyString(this.descripcionAtributo)){
			return Labels.get(this.getNombreAtributo());			
		}
		else{
			return descripcionAtributo;
		}
	}

	public void setDescripcionAtributo(String descripcionAtributo) {
		this.descripcionAtributo = descripcionAtributo;
	}

	@Override
	public void execute() throws Exception {
		String error = null;
		try{			
			ObjetoNegocio objetoNegocio = (ObjetoNegocio)MapFacade.findEntity(this.getView().getModelName(), this.getView().getKeyValues());
			this.modificarAtributo(objetoNegocio);
			this.commit();
		}		
		catch(Exception e){
			this.rollback();
			error = e.getMessage();
			if (error == null){
				error = e.toString();
			}
		}
		finally {
			this.closeDialog();
			if (!Is.emptyString(error)){
				addError(error);
			}
			else{
				addMessage("Operación Finalizada");
			}
		}
	}
	
	@Override
	public String getNextAction() throws Exception {
		return "CRUD.refresh";
	}
	
	private void modificarAtributo(ObjetoNegocio objetoNegocio) throws Exception{
		if (!Is.emptyString(this.getNombreAtributo())){
			Object valorNuevo = this.getView().getValue(this.getNombreAtributo());
			Class<?> tipoParametro = valorNuevo.getClass();
			try{
				tipoParametro = this.getView().getMetaReference(this.getNombreAtributo()).getMetaModelReferenced().getPOJOClass();
				String id = (String)((Map<?, ?>)valorNuevo).get("id");
				if (!Is.emptyString(id)){
					valorNuevo = XPersistence.getManager().find(tipoParametro, id);
				}
				else{
					valorNuevo = null;
				}
			}
			catch(Exception e){
			}
			
			Auditoria auditoria = Auditoria.modificarAtributoConAuditoria(objetoNegocio, this.getNombreAtributo(), this.getDescripcionAtributo(), valorNuevo, tipoParametro);
			if (auditoria != null){
				XPersistence.getManager().persist(auditoria);
			}
			else{
				throw new ValidationException("Mismo valor");
			}
		}
		else{
			throw new ValidationException("Falta asignar en el controlador la propiedad nombreAtributo");
		}
	}
	
	/*private void modificarAtributo(ObjetoNegocio objetoNegocio) throws Exception{
		if (!Is.emptyString(this.getNombreAtributo())){
			Object valorNuevo = this.getView().getValue(this.getNombreAtributo());
			String methodName = this.getNombreAtributo().substring(0, 1).toUpperCase() + this.getNombreAtributo().substring(1);
			Method metodoSet = objetoNegocio.getClass().getMethod("set" + methodName, valorNuevo.getClass());
			Method metodoGet = objetoNegocio.getClass().getMethod("get" + methodName);
			Object valorAnterior = metodoGet.invoke(objetoNegocio);
			if (Is.equal(valorAnterior, valorNuevo)){
				throw new ValidationException("Mismo valor");
			}
			
			try{
				objetoNegocio.inicioCambioAtributo();
				metodoSet.invoke(objetoNegocio, valorNuevo);
				objetoNegocio.finCambioAtributo();
			}
			catch(InvocationTargetException e){
				if ((e.getTargetException() != null) && (e.getTargetException() instanceof ValidationException)){
					throw (ValidationException)e.getTargetException();
				}
				else{
					throw e;
				}
			}
			
			if (getErrors().isEmpty()){
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
				
				Auditoria auditoria = Auditoria.generarRegistroAuditoria(objetoNegocio, this.getDescripcionAtributo(), strAnterior, strNuevo);
				XPersistence.getManager().persist(auditoria);
			}
		}
		else{
			throw new ValidationException("Falta asignar en el controlador la propiedad nombreAtributo");
		}
	}*/
}
