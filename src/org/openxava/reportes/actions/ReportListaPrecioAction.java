package org.openxava.reportes.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.clasificadores.model.*;
import org.openxava.jpa.*;
import org.openxava.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

import net.sf.jasperreports.engine.*;

public class ReportListaPrecioAction extends ReportBaseAction{

	private String tipoFormato;
	
	public String getTipoFormato() {
		return tipoFormato;
	}

	public void setTipoFormato(String tipoFormato) {
		this.tipoFormato = tipoFormato;
	}
	
	@Override
	protected String getNombreReporte() {
		return "ListaPrecio.jrxml";
	}

	@Override
	public void execute() throws Exception {
		org.openxava.view.View viewListaPrecio = this.getPreviousView();
		if (viewListaPrecio.isKeyEditable()){
			throw new ValidationException("primero_grabar");
		}
		if (!Is.emptyString(this.getTipoFormato())){
			if (this.getTipoFormato().equals("excel")){
				this.setFormat(JasperReportBaseAction.EXCEL);
			}
			else if (this.getTipoFormato().equals("pdf")){
				this.setFormat(JasperReportBaseAction.PDF);
			}
			else{
				throw new ValidationException("Tipo de formato incorrecto " + this.getTipoFormato());
			}
		}
		else{
			throw new ValidationException("Debe asignar la variable Tipo de formato en la definición de la acción");
		}
		
		super.execute();
		
		this.closeDialog();
	}
	
	@Override
	protected void agregarParametros(Map<String, Object> parametros) {
		try{ 
			ListaPrecio listaPrecio = (ListaPrecio)MapFacade.findEntity(getPreviousView().getModelName(), getPreviousView().getKeyValues());
			parametros.put("ID", listaPrecio.getId());
			parametros.put("CODIGO", listaPrecio.getCodigo());
			parametros.put("NOMBRE", listaPrecio.getNombre());
			parametros.put("MONEDA_NOMBRE", listaPrecio.getMoneda().getNombre());
			
			String id = this.getView().getValueString("marca.id");
			if (!Is.emptyString(id)){
				ObjetoEstatico objetoEstatico = XPersistence.getManager().find(Marca.class, id);
				parametros.put("MARCA_ID", id);
				parametros.put("MARCA_CODIGO", objetoEstatico.getCodigo());
				parametros.put("MARCA_NOMBRE", objetoEstatico.getNombre());
			}
			else{
				parametros.put("MARCA_ID", "");
				parametros.put("MARCA_CODIGO", "");
				parametros.put("MARCA_NOMBRE", "");
			}
			
			id = this.getView().getValueString("rubro.id");
			if (!Is.emptyString(id)){
				ObjetoEstatico objetoEstatico = XPersistence.getManager().find(Rubro.class, id);
				parametros.put("RUBRO_ID", id);
				parametros.put("RUBRO_CODIGO", objetoEstatico.getCodigo());
				parametros.put("RUBRO_NOMBRE", objetoEstatico.getNombre());
			}
			else{
				parametros.put("RUBRO_ID", "");
				parametros.put("RUBRO_CODIGO", "");
				parametros.put("RUBRO_NOMBRE", "");
			}
		}
		catch(Exception e){
			throw new ValidationException("Error al instanciar lista precio");
		}
		 
	}

	@Override
	protected JRDataSource getDataSource() throws Exception {		
		return null;
	}

}
