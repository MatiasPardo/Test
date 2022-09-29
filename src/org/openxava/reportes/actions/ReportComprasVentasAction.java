package org.openxava.reportes.actions;

import java.text.*;
import java.util.*;

import org.openxava.actions.*;
import org.openxava.util.*;
import org.openxava.validators.*;

import net.sf.jasperreports.engine.*;

public class ReportComprasVentasAction extends ReportBaseAction{

	private Date hasta;
	
	private String idMarca = "";
		
	public Date getHasta() {
		return hasta;
	}

	public void setHasta(Date hasta) {
		this.hasta = hasta;
	}

	public String getIdMarca() {
		return idMarca;
	}

	public void setIdMarca(String idMarca) {
		if (!Is.emptyString(idMarca)){
			this.idMarca = idMarca;
		}
	}
	
	@Override
	public void execute() throws Exception {
		this.hasta = (Date)getView().getValue("hasta");
		if (Is.empty(this.hasta)){
			throw new ValidationException("No esta asignado el día hasta");
		}
		this.setIdMarca((String)getView().getValue("marca.id"));
		
		this.setFormat(JasperReportBaseAction.EXCEL);
		super.execute();
		
		this.closeDialog();
		addMessage("Listado Finalizado");
	}
	
	private Date primerDiaMes(Date fecha){
		Calendar cal = Calendar.getInstance();
		cal.setTime(fecha);
		cal.set(cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH),
				cal.getActualMinimum(Calendar.DAY_OF_MONTH),
				cal.getMinimum(Calendar.HOUR_OF_DAY),
				cal.getMinimum(Calendar.MINUTE),
				cal.getMinimum(Calendar.SECOND));
		return cal.getTime();
	}
	
	private Date diaAnterior(Date fecha){
		Calendar cal = Calendar.getInstance();
		cal.setTime(fecha);
		cal.add(Calendar.DAY_OF_YEAR, -1);
		return cal.getTime();
	}

	@Override
	protected String getNombreReporte() {
		return "InformeComprasVentas.jrxml";		
	}

	@Override
	protected void agregarParametros(Map<String, Object> parametros) {
		parametros.put("FILTROMARCA", this.getIdMarca());
		
		SimpleDateFormat formatoFiltro = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat formatoEtiquetaMes = new SimpleDateFormat("MM/yyyy");
		int cantidadMeses = 6;
		Date fin = this.getHasta();
		Date inicio = primerDiaMes(fin);
		for(int i = 1; i<= cantidadMeses; i++){
			parametros.put("MES" + Integer.toString(i), formatoEtiquetaMes.format(inicio));
			parametros.put("DESDE" + Integer.toString(i), formatoFiltro.format(inicio));
			parametros.put("HASTA" + Integer.toString(i), formatoFiltro.format(fin));
			// mes anterior
			fin = diaAnterior(inicio);
			inicio = primerDiaMes(fin);
		}				
	}

	@Override
	protected JRDataSource getDataSource() throws Exception {
		return null;
	}

}
