package org.openxava.reportes.actions;

import java.util.Date;
import java.util.Map;

import org.openxava.base.model.TipoFormatoImpresion;
import org.openxava.base.model.UtilERP;
import org.openxava.util.Is;

import net.sf.jasperreports.engine.JRDataSource;

public class ReportReposicionMercaderiaAction extends ReportBaseAction{

	@Override
	protected String getNombreReporte() {
		return "ReposicionMercaderia.jrxml";
	}

	@Override
	protected void agregarParametros(Map<String, Object> parametros) {
		Date fecha = (Date)this.getView().getValue("fecha");
		if (fecha == null){
			fecha = UtilERP.trucarDateTime(new Date());
		}
		parametros.put("FECHA_DATE", fecha);
	}

	@Override
	protected JRDataSource getDataSource() throws Exception {		
		return null;
	}
	
	@Override
	protected TipoFormatoImpresion formatoImpresion(){
		return TipoFormatoImpresion.Excel;
	}
	
	@Override
	protected boolean filtraPorEmpresa(){
		return true;
	}
	
	@Override
	protected boolean filtraPorSucursales(){
		return true;
	}
	
	@Override
	public void execute() throws Exception {
		String id = this.getView().getValueString("sucursal.id");
		if (!Is.emptyString(id)){
			this.setIdFiltroSucursal(id);
		}
		
		super.execute();
		
		this.closeDialog();
	}
}
