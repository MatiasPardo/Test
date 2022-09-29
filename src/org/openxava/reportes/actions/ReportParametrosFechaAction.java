package org.openxava.reportes.actions;

import java.text.*;
import java.util.*;

import org.openxava.base.model.TipoFormatoImpresion;
import org.openxava.util.*;
import org.openxava.validators.*;

public abstract class ReportParametrosFechaAction extends ReportBaseAction{

	private Date desde;
	
	private Date hasta;
	
	protected void validarParametrosDesdeVista(){
		this.desde = (Date)getView().getValue("desde");
		this.hasta = (Date)getView().getValue("hasta");
			
		
		if (Is.empty(this.desde) || Is.empty(this.hasta)) {
			throw new ValidationException("Fechas no asignadas");
		}
		else if (this.desde.after(this.hasta)){
			throw new ValidationException("Fecha hasta debe ser posterior a " + this.desde.toString());
		}
	}
	
	@Override
	protected TipoFormatoImpresion formatoImpresion(){
		return TipoFormatoImpresion.PDF;
	}
	
	@Override
	public void execute() throws Exception {
		this.validarParametrosDesdeVista();
		super.execute();
		
		this.closeDialog();
		addMessage("Listado Finalizado");
	}
	
	@Override
	protected void agregarParametros(Map<String, Object> parametros){	
		parametros.put("FECHAEJECUCION", DateFormat.getDateInstance(DateFormat.LONG).format(new Date()));		
		parametros.put("DESDE", DateFormat.getDateInstance(DateFormat.LONG).format(this.desde));
		parametros.put("HASTA", DateFormat.getDateInstance(DateFormat.LONG).format(this.hasta));		
		SimpleDateFormat formatoFiltro = new SimpleDateFormat("yyyy-MM-dd");
		parametros.put("FILTRODESDE", formatoFiltro.format(this.desde));
		parametros.put("FILTROHASTA", formatoFiltro.format(this.hasta));
		
		parametros.put("DESDE_DATE", this.desde);
		parametros.put("HASTA_DATE", this.hasta);
	}
}
