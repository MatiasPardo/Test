package org.openxava.reportes.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.UtilERP;
import org.openxava.jpa.*;
import org.openxava.reclamos.model.*;

import net.sf.jasperreports.engine.*;

public class ReportReclamosAction extends ReportBaseAction{

	@Override
	public void execute() throws Exception {
		this.setFormat(JasperReportBaseAction.PDF);		
		super.execute();
		this.closeDialog();
	}
	
	@Override
	protected String getNombreReporte() {
		return "ReclamosListado.jrxml";
	}

	@Override
	protected void agregarParametros(Map<String, Object> parametros) {
		Date fecha = (Date)this.getView().getValue("fechaServicioReclamo");
		if (fecha != null){
			fecha = UtilERP.trucarDateTime(fecha);
		}else addError("La fecha no puede estar vacia");
		UsuarioReclamo asignadoA = XPersistence.getManager().find(UsuarioReclamo.class, this.getView().getValue("asignadoA.id"));
		parametros.put("ASIGNADO_A_CODIGO", asignadoA.getCodigo());
		parametros.put("FECHA_SERVICIO_RECLAMO", fecha);
	}

	@Override
	protected JRDataSource getDataSource() throws Exception {

		return null;
	}

}
