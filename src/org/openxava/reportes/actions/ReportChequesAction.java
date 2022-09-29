package org.openxava.reportes.actions;

import java.text.*;
import java.util.*;



import net.sf.jasperreports.engine.*;

public class ReportChequesAction extends ReportBaseAction{

	@Override
	protected JRDataSource getDataSource() throws Exception {
		 return null;
	}

	@Override
	protected String getNombreReporte(){
		String nombreReporte = "Cheques_reporte.jrxml";
		return nombreReporte;
	}

	@Override
	protected void agregarParametros(Map<String, Object> parametros){
		parametros.put("FECHAEJECUCION",  DateFormat.getDateInstance(DateFormat.LONG).format(new Date()));		
	}

}
