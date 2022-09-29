package org.openxava.reportes.actions;


import net.sf.jasperreports.engine.*;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

public class ReportTransaccionAction extends JasperConcatReportBaseAction implements IChainAction{
	
	private Transaccion transaccion = null;
	
	protected Transaccion getTransaccion(){
		try{
			if (transaccion == null){
				this.transaccion = (Transaccion)MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
			}
			return this.transaccion;
		}
		catch(Exception e){
			throw new ValidationException("No se pudo encontrar la transacción: " + e.toString());
		}
	}
		
	@Override
	protected String[] getJRXMLs() throws Exception {
		String[] jrxmls = new String[this.getTransaccion().nroCopiasImpresion()];
		//String nombreReporte = tr.getClass().getSimpleName() + "_reporte.jrxml";
		String nombreReporte = this.getTransaccion().nombreReporteImpresion();
		for(Integer nroCopia = 1; nroCopia <= this.getTransaccion().nroCopiasImpresion(); nroCopia ++){			
			jrxmls[nroCopia - 1] =  ConfiguracionERP.fullFileNameReporte(nombreReporte);			
		}
		return jrxmls;
	}
	
	@Override
	public String getNextAction() throws Exception {
		return "Transaccion.editar";
	}
	
	@Override
	protected JRDataSource[] getDataSources() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void execute() throws Exception {
		if (getView().isKeyEditable()){
			throw new ValidationException("No se puede imprimir el comprobante. Falta grabarlo");
		}
		else{
			Messages errors = MapFacade.validate(getView().getModelName(), getView().getValues());
			if (errors.contains()) throw new ValidationException(errors);
		}
		
		for(Integer nroCopia = 1; nroCopia <= this.getTransaccion().nroCopiasImpresion(); nroCopia++){
			addParameters(parametrosReporteTransaccion(nroCopia));
		}
		super.execute();
	}

	private Map<String, Object> parametrosReporteTransaccion(Integer nroCopia){
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("ESQUEMA", ConfiguracionERP.esquemaDB());
		parameters.put("NUMEROCOPIA", nroCopia);
		transaccion.agregarParametrosImpresion(parameters);
		return parameters;
	}
	
	public String archivoJRXMLEnvioMail() {
		// por ahora solo se envia la primer copia
		try{
			return this.getJRXMLs()[0];
		}
		catch(Exception e){
			throw new ValidationException(e.toString());
		}
	}

	public Map<String, Object> parametrosReporteEnvioMail() {
		// por ahora solo se envia la primer copia
		return parametrosReporteTransaccion(1);
	}
}
