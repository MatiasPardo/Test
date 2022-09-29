package org.openxava.reportes.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

import net.sf.jasperreports.engine.*;

public class ReportCreditoDebitoTextoAction extends ReportBaseAction implements IChainAction{

	@Override
	public void execute() throws Exception {
		if (getView().isKeyEditable()){
			throw new ValidationException("No se puede imprimir el comprobante. Falta grabarlo");
		}
		else{
			Messages errors = MapFacade.validate(getView().getModelName(), getView().getValues());
			if (errors.contains()) throw new ValidationException(errors);
		}
		super.execute();
	}
	
	@Override
	public String getNextAction() throws Exception {
		return "Transaccion.editar";
	}
	
	@Override
	protected JRDataSource getDataSource() throws Exception {
		return null;
	}

	@Override
	protected String getNombreReporte(){
		String nombreReporte = new String(getView().getModelName()).concat("_texto.jrxml");
		return nombreReporte; 
	}
	
	@Override
	protected void agregarParametros(Map<String, Object> parametros){
		try{
			Transaccion tr = (Transaccion)MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());			
			tr.agregarParametrosImpresion(parametros);
		}
		catch(Exception e){
			throw new ValidationException("Error al buscar transacción: " + e.toString());
		}
	}
}
