package org.openxava.reportes.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.clasificadores.model.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import net.sf.jasperreports.engine.*;

public class ReportProductosDisponiblesAction  extends ReportBaseAction{
	
	private Boolean incluyeCompras = Boolean.FALSE;
	
	private String idMarca = "";
		
	public Boolean getIncluyeCompras() {
		return incluyeCompras;
	}

	public void setIncluyeCompras(Boolean incluyeCompras) {
		if (incluyeCompras != null){
			this.incluyeCompras = incluyeCompras;
		}
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
		
		this.setIncluyeCompras((Boolean)getView().getValue("incluyeCompras"));
		this.setIdMarca((String)getView().getValue("marca.id"));		
		this.setFormat(JasperReportBaseAction.EXCEL);
		super.execute();
		
		this.closeDialog();
		addMessage("Listado Finalizado");
	}
	
	@Override
	protected JRDataSource getDataSource() throws Exception {
		return null;
	}

	@Override
	protected String getNombreReporte(){
		return new String("ProductosDisponiblesVenta.jrxml");		
	}

	
	@Override
	protected void agregarParametros(Map<String, Object> parametros){
		parametros.put("FILTROMARCA", this.getIdMarca());
		parametros.put("MARCA", this.getNombreMarca());
		if (this.getIncluyeCompras()){
			parametros.put("INCLUYECOMPRAS", "S");
		}
		else{
			parametros.put("INCLUYECOMPRAS", "N");
		}		
	}
	
	private String getNombreMarca(){
		if (!Is.emptyString(this.getIdMarca())){
			Marca marca = (Marca)XPersistence.getManager().find(Marca.class, this.getIdMarca());
			return marca.getNombre();
		}
		else{
			return "";
		}
	}
}
