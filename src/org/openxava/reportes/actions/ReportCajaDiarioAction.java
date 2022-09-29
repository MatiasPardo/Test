package org.openxava.reportes.actions;

import java.util.*;

import org.openxava.base.model.TipoFormatoImpresion;
import org.openxava.jpa.XPersistence;
import org.openxava.tesoreria.model.Caja;
import org.openxava.util.*;

import net.sf.jasperreports.engine.*;

public class ReportCajaDiarioAction extends ReportParametrosFechaAction{

	private String idCaja = null;
	
	private String idEmpresa = null;
	
	@Override
	protected JRDataSource getDataSource() throws Exception {
		return null;
	}

	@Override
	protected String getNombreReporte(){
		TipoFormatoImpresion tipo = this.formatoImpresion();
		String nombreReporte = "CajaDiariaPdf.jrxml";
		if (tipo.equals(TipoFormatoImpresion.Excel)){
			nombreReporte = "CajaDiaria.jrxml";
		}		
		return nombreReporte;
	}

	@Override
	protected void validarParametrosDesdeVista(){
		super.validarParametrosDesdeVista();
		this.idCaja = (String)getView().getValue("caja.id");
		this.idEmpresa = (String)getView().getValue("empresa.id");
	}
	
	@Override
	protected void agregarParametros(Map<String, Object> parametros){
		super.agregarParametros(parametros);
		
		if (!Is.emptyString(this.idCaja)){
			parametros.put("FILTROCAJA", idCaja);
			Caja caja = (Caja)XPersistence.getManager().find(Caja.class, idCaja);
			parametros.put("NOMBRECAJA", caja.getNombre());
			parametros.put("CODIGOCAJA", caja.getCodigo());
		}
		else{
			parametros.put("FILTROCAJA", "");
			parametros.put("NOMBRECAJA", "");
			parametros.put("CODIGOCAJA", "");
		}
		if (!Is.emptyString(this.idEmpresa)){
			parametros.put("FILTROEMPRESA", idEmpresa);
		}
		else{
			parametros.put("FILTROEMPRESA", "");
		}
		
	}
	
	@Override
	protected boolean filtraPorSucursales(){
		return true;
	}
	
	@Override
	protected TipoFormatoImpresion formatoImpresion(){
		TipoFormatoImpresion tipo = (TipoFormatoImpresion)getView().getValue("formato");
		if (tipo != null){
			return tipo;
		}
		else{
			return super.formatoImpresion();
		}
	}
}
