package org.openxava.base.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.validators.*;

import net.sf.jasperreports.engine.*;

public class MultipleReportTransaccionAction extends JasperMultipleReportBaseAction{
	
	private Collection<Transaccion> lista;
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute() throws Exception {
		this.lista = (Collection<Transaccion>)this.getRequest().getAttribute("transacciones");
		if (this.lista == null){
			throw new ValidationException("Error al imprimir: no hay lista de comprobantes");
		}
		else{ 
			this.getRequest().removeAttribute("transacciones");
			if (this.lista.isEmpty()){
				throw new ValidationException("Error al imprimir: la lista de comprobantes esta vacía");
			}
		}
		
		for(Transaccion tr: this.lista){
			// se instancia por si el objeto perdió la sesión 
			Transaccion transaccion = XPersistence.getManager().find(tr.getClass(), tr.getId());
			for(Integer nroCopia = 1; nroCopia <= transaccion.nroCopiasImpresion(); nroCopia++ ){
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("ESQUEMA", ConfiguracionERP.esquemaDB());
				parameters.put("NUMEROCOPIA", nroCopia);
				transaccion.agregarParametrosImpresion(parameters);
				addParameters(parameters);
			}		
		}		
		super.execute();		
	}
	
	@Override
	protected JRDataSource[] getDataSources() throws Exception {
		return null;		
	}

	@Override
	protected String[] getJRXMLs() throws Exception {
		List<String> jrxmls = new LinkedList<String>();
		for(Transaccion tr: lista){
			String nombreReporte = tr.nombreReporteImpresion();
			for(Integer nroCopia = 1; nroCopia <= tr.nroCopiasImpresion(); nroCopia++ ){
				jrxmls.add(ConfiguracionERP.fullFileNameReporte(nombreReporte));
			}
		}
		String[] array = new String[jrxmls.size()];
		return jrxmls.toArray(array);
	}
}

