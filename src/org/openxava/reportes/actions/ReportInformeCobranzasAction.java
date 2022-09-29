package org.openxava.reportes.actions;

import java.util.Date;
import java.util.Map;

import javax.validation.ValidationException;

import org.openxava.base.model.TipoFormatoImpresion;
import org.openxava.base.model.UtilERP;

import net.sf.jasperreports.engine.JRDataSource;

public class ReportInformeCobranzasAction extends ReportBaseAction{

	@Override
	protected String getNombreReporte() {
		return "InformeCobranzas.jrxml";
	}

	@Override
	protected void agregarParametros(Map<String, Object> parametros) {
		Date desde = (Date)this.getView().getValue("desde");
		Date hasta = (Date)this.getView().getValue("hasta");
		
		if (desde != null && hasta != null && desde.compareTo(hasta) > 0){
			throw new ValidationException("Fechas: hasta debe ser mayor a desde");
		}
		
		parametros.put("DESDE_DATE", UtilERP.trucarDateTime(desde));
		parametros.put("HASTA_DATE", UtilERP.trucarDateTime(hasta));
	}

	@Override
	protected JRDataSource getDataSource() throws Exception {
		return null;
	}
	
	protected TipoFormatoImpresion formatoImpresion(){
		return TipoFormatoImpresion.Excel;
	}
	
}
