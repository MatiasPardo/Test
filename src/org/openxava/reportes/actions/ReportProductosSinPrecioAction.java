package org.openxava.reportes.actions;

import java.util.*;

import org.openxava.actions.*;

import net.sf.jasperreports.engine.*;

public class ReportProductosSinPrecioAction extends ReportBaseAction{

	@Override
	protected JRDataSource getDataSource() throws Exception {
		return null;
	}

	@Override
	protected String getNombreReporte(){
		return "ProductosSinPrecios.jrxml";		
	}

	protected void agregarParametros(Map<String, Object> parametros){		
	}

	@Override
	public void execute() throws Exception {
		this.setFormat(JasperReportBaseAction.EXCEL);
		super.execute();
	}
}
