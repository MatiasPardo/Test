package org.openxava.reclamos.actions;

import java.util.*;

import org.openxava.reportes.actions.*;

import net.sf.jasperreports.engine.*;

public class ReporteControlNovedadAction extends ReportBaseAction{

	@Override
	protected String getNombreReporte() {
		// TODO Auto-generated method stub
		return "ControlNovedad.jrxml";
	}

	@Override
	protected void agregarParametros(Map<String, Object> parametros) {
		// TODO Auto-generated method stub
	}

	@Override
	protected JRDataSource getDataSource() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
