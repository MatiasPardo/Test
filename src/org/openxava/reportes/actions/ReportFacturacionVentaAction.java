package org.openxava.reportes.actions;

import java.text.*;
import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.calculators.*;
import org.openxava.clasificadores.model.Marca;
import org.openxava.model.MapFacade;
import org.openxava.util.*;

import net.sf.jasperreports.engine.*;

public class ReportFacturacionVentaAction extends ReportBaseConcatAction{
	
	private Date mes;
	
	public Date getMes() {
		return mes;
	}

	public void setMes(Date mes) {
		this.mes = mes;
	}
	
	private List<String> idsMarcas = null;
	
	private String nombresMarcas = null;
	
	private List<String> getIdsMarcas(){		
		if (this.idsMarcas == null){
			StringBuffer nombres = new StringBuffer("");
			this.idsMarcas = new LinkedList<String>();
			@SuppressWarnings("rawtypes")
			Map[] keys = this.getView().getSubview("marcas").getCollectionTab().getSelectedKeys();
			try{
				for (int i = 0; i < keys.length; i++) {					
					Marca marca = (Marca)MapFacade.findEntity("Marca", keys[i]);
					this.idsMarcas.add(marca.getId());
					if (nombres.length() > 0) nombres.append(", ");
					nombres.append(marca.getNombre());
				}
			}
			catch(Exception ex){		
			}
			this.nombresMarcas = nombres.toString();
		}		
		return this.idsMarcas;
	}
	
	@Override
	public void execute() throws Exception {
		
		this.setMes((Date)this.getView().getValue("mes"));
		
		this.setFormat(JasperMultipleReportBaseAction.EXCEL);
		super.execute();
		
		this.closeDialog();
		addMessage("Listado Finalizado");
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected Map getParameters() throws Exception {
		Map<String, Object> parameters = this.crearParametros();
		
		FechaInicioMesCalculator calInicio = new FechaInicioMesCalculator();
		calInicio.setFecha(this.getMes());
		Date desde = (Date) calInicio.calculate();
		
		FechaFinMesCalculator calFin = new FechaFinMesCalculator();
		calFin.setFecha(this.getMes());
		Date hasta = (Date) calFin.calculate();
		
		parameters.put("USUARIO", Users.getCurrent());
		parameters.put("DESDE", DateFormat.getDateInstance(DateFormat.LONG).format(desde));
		parameters.put("HASTA", DateFormat.getDateInstance(DateFormat.LONG).format(hasta));
		parameters.put("FECHAUSUARIO_DATE", this.getMes());
		
		SimpleDateFormat formatoFiltro = new SimpleDateFormat("yyyy-MM-dd");
		parameters.put("FILTRODESDE", formatoFiltro.format(desde));
		parameters.put("FILTROHASTA", formatoFiltro.format(hasta));
		parameters.put("FILTROFECHAUSUARIO", formatoFiltro.format(this.getMes()));
		
		parameters.put("MARCAS", this.getIdsMarcas());
		parameters.put("MARCAS_NOMBRES", this.nombresMarcas);
		parameters.put("MARCAS_TODAS", this.getIdsMarcas().isEmpty());
				
		return parameters;
	}
	
	@Override
	protected JRDataSource[] getDataSources() throws Exception {
		return null;
	}

	@Override
	protected String[] getNombresReportes() {
		String nombreReporte1 = "Ventas.jrxml";
				
		String nombreReporte2 = "VentasDetallada.jrxml";
				
		String[] reportes = new String[2];
		reportes[0] = nombreReporte1;
		reportes[1] = nombreReporte2;
		
		return reportes;
	}

}
