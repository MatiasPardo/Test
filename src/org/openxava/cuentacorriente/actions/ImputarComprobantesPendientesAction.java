package org.openxava.cuentacorriente.actions;

import java.util.*;

import javax.persistence.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.cuentacorriente.model.*;
import org.openxava.jpa.*;
import org.openxava.model.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

public class ImputarComprobantesPendientesAction extends TabBaseAction{

	private String ordenarPor;
	
	public String getOrdenarPor() {
		return ordenarPor;
	}

	public void setOrdenarPor(String ordenarPor) {
		this.ordenarPor = ordenarPor;
	}



	@Override
	public void execute() throws Exception {
		if (!Is.equalAsStringIgnoreCase(Users.getCurrent(), "admin") ){
			throw new ValidationException("Debe ser usuario admin");
		}
		
		if (!Is.emptyString(this.getOrdenarPor())){
			String sql = "select id from {h-schema}CuentaCorriente where pendiente = :pendiente and anulado = :anulado and dtype = :tipo " + 
					"order by " + this.getOrdenarPor() + ", empresa_id";
			Query query = XPersistence.getManager().createNativeQuery(sql);
			query.setParameter("pendiente", true);
			query.setParameter("anulado", false);
			query.setParameter("tipo", this.getTab().getModelName());
			List<?> results = query.getResultList();
			if (!results.isEmpty()){				
				OperadorComercial operadorCtaCte = null;
				Empresa empresa = null;
				List<CuentaCorriente> listadoCtaCte = new LinkedList<CuentaCorriente>();				
				for(Object id: results){
					Map<String, Object> key = new HashMap<String, Object>();
					key.put("id", id);					
					CuentaCorriente ctacte = (CuentaCorriente)MapFacade.findEntity(this.getTab().getModelName(), key);
					if (operadorCtaCte == null){
						operadorCtaCte = ctacte.operadorCtaCte();
						empresa = ctacte.getEmpresa();
					}
					else if ((!ctacte.operadorCtaCte().equals(operadorCtaCte)) || (!ctacte.getEmpresa().equals(empresa))){						
						try{
							Imputacion.imputarComprobantes(listadoCtaCte, new LinkedList<Imputacion>());
							this.commit();
						}
						catch(Exception e){
							CuentaCorriente ctacteNoImputada = listadoCtaCte.get(0);
							addError(ctacteNoImputada.operadorCtaCte().getCodigo() + " " + ctacteNoImputada.operadorCtaCte().getNombre() + " - " + ctacteNoImputada.getEmpresa().getCodigo() + ": " + e.getMessage() );
							this.rollback();
						}
						finally{
							listadoCtaCte.clear();
							// se vuelve a instancier la cuenta corriente, después de un commit/rollback
							ctacte = (CuentaCorriente)MapFacade.findEntity(this.getTab().getModelName(), key);
							operadorCtaCte = ctacte.operadorCtaCte();
							empresa = ctacte.getEmpresa();
						}
					}
					listadoCtaCte.add(ctacte);
				}
				if (!listadoCtaCte.isEmpty()){
					try{
						Imputacion.imputarComprobantes(listadoCtaCte, new LinkedList<Imputacion>());
						this.commit();
					}
					catch(Exception e){
						CuentaCorriente ctacteNoImputada = listadoCtaCte.get(0);
						addError(ctacteNoImputada.operadorCtaCte().getCodigo() + " " + ctacteNoImputada.operadorCtaCte().getNombre() + " - " + ctacteNoImputada.getEmpresa().getCodigo() + ": " + e.getMessage());
						this.rollback();
					}
				}
				addMessage("ejecucion_OK");
			}
			else{
				addError("No hay comprobantes pendientes");
			}
		}
		else{
			throw new ValidationException("Falta asignar el parámetro ordenadoPor");
		}
	}

}
