package org.openxava.reportes.actions;

import java.util.*;

import org.openxava.base.model.TipoFormatoImpresion;
import org.openxava.util.*;
import org.openxava.validators.*;

import net.sf.jasperreports.engine.*;

public class ReportLibroDiarioAction extends ReportBaseAction{

	@Override
	protected String getNombreReporte() {
		return "LibroDiario.jrxml";
	}

	@Override
	protected boolean filtraPorEmpresa(){
		return true;
	}
	
	@Override
	protected void agregarParametros(Map<String, Object> parametros) {
		if (Is.equalAsStringIgnoreCase(this.getPreviousView().getModelName(), "PeriodoContable")){
			parametros.put("PERIODO_ID", this.getPreviousView().getValueString("id"));
			parametros.put("EJERCICIO_ID", "");
		}
		else if (Is.equalAsStringIgnoreCase(this.getPreviousView().getModelName(), "EjercicioContable")){
			parametros.put("PERIODO_ID", "");
			parametros.put("EJERCICIO_ID", this.getPreviousView().getValueString("id"));
		}
		else{
			throw new ValidationException("Error al agregar parámetros");
		}
		
		parametros.put("CODIGO", this.getPreviousView().getValueString("codigo"));
		parametros.put("NOMBRE", this.getPreviousView().getValueString("nombre"));
	}
	
	@Override
	public void execute() throws Exception {
		if (Is.emptyString(this.getView().getValueString("empresa.id"))){
			throw new ValidationException("Falta asignar la empresa");
		}
		this.setIdFiltroEmpresa(this.getView().getValueString("empresa.id"));
				
		super.execute();
		
		this.closeDialog();
	}

	@Override
	protected JRDataSource getDataSource() throws Exception {
		return null;
	}

	@Override
	protected TipoFormatoImpresion formatoImpresion(){
		TipoFormatoImpresion formato = (TipoFormatoImpresion)this.getView().getValue("formato");
		if (formato == null){
			formato = TipoFormatoImpresion.PDF;
		}
		return formato;
	}
}
