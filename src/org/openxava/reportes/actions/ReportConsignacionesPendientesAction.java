package org.openxava.reportes.actions;

import java.util.*;

import javax.validation.*;

import org.openxava.actions.*;
import org.openxava.clasificadores.model.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.ventas.model.*;

import net.sf.jasperreports.engine.*;

public class ReportConsignacionesPendientesAction extends ReportBaseMultipleAction{

	private List<Cliente> clientes = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute() throws Exception {
		this.clientes = (List<Cliente>)this.getRequest().getAttribute("clientes");
		this.getRequest().removeAttribute("clientes");
		
		Marca marca = null;
		String id = this.getView().getValueString("marca.id");
		if (!Is.emptyString(id)){
			marca = XPersistence.getManager().find(Marca.class, id);
		}
		Rubro rubro = null;
		id = this.getView().getValueString("rubro.id");
		if (!Is.emptyString(id)){
			rubro = XPersistence.getManager().find(Rubro.class, id);
		}
		
		if (this.clientes.isEmpty()){
			throw new ValidationException("No se selecciono ningún clientes");
		}
		
		for(Cliente cliente: this.clientes){
			Map<String, Object> parametros = this.crearParametros();
			parametros.put("CLIENTE_CODIGO", cliente.getCodigo());
			parametros.put("CLIENTE_NOMBRE", cliente.getNombre());
			parametros.put("CLIENTE_ID", cliente.getId());
			parametros.put("CLIENTE_DIRECCION", cliente.getDomicilio().getDireccion());
			parametros.put("CLIENTE_CIUDAD", cliente.getDomicilio().getCiudad().getCiudad());
			parametros.put("CLIENTE_PROVINCIA", cliente.getDomicilio().getProvincia().getProvincia());
			parametros.put("CLIENTE_CODIGOPOSTAL", cliente.getDomicilio().getCiudad().getCodigoPostal());
			
			if (marca != null){
				parametros.put("MARCA_ID", marca.getId());
				parametros.put("MARCA_CODIGO", marca.getCodigo());
				parametros.put("MARCA_NOMBRE", marca.getNombre());
			}
			else{
				parametros.put("MARCA_ID", "");
				parametros.put("MARCA_CODIGO", "");
				parametros.put("MARCA_NOMBRE", "");
			}
			if (rubro != null){
				parametros.put("RUBRO_ID", rubro.getId());
				parametros.put("RUBRO_CODIGO", rubro.getCodigo());
				parametros.put("RUBRO_NOMBRE", rubro.getNombre());
			}
			else{
				parametros.put("RUBRO_ID", "");
				parametros.put("RUBRO_CODIGO", "");
				parametros.put("RUBRO_NOMBRE", "");
			}
			this.addParameters(parametros);
		}
		
		this.setFormat(JasperMultipleReportBaseAction.PDF);
		super.execute();
		
		this.closeDialog();
		this.addMessage("listado_OK");
	}
	
	@Override
	protected String[] getNombresReportes() {
		String[] reportes = new String[this.clientes.size()];
		for(int i = 0; i < this.clientes.size(); i++ ){
			reportes[i] = "ConsignacionesPendientes.jrxml";
		}
		return reportes;
	}

	@Override
	protected JRDataSource[] getDataSources() throws Exception {
		return null;
	}
	
	@Override
	protected boolean filtraPorEmpresa(){
		return true;
	}

	@Override
	protected boolean filtrarPorSucursal(){
		return true;
	}
	
}
