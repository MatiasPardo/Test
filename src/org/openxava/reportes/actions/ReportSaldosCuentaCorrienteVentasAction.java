package org.openxava.reportes.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

import net.sf.jasperreports.engine.*;

public class ReportSaldosCuentaCorrienteVentasAction extends ReportBaseAction{

	private String filtroVendedor;
	
	private String formato;
		
	public String getFiltroVendedor() {
		if (Is.emptyString(filtroVendedor)){
			return "";
		}
		else{
			return filtroVendedor;
		}
	}

	public void setFiltroVendedor(String filtroVendedor) {
		this.filtroVendedor = filtroVendedor;
	}
	
	@Override
	public void execute() throws Exception {
		this.setFiltroVendedor(this.getView().getValueString("vendedor.id"));
		this.setIdFiltroEmpresa(this.getView().getValueString("empresa.id"));
		
		String idVendedor = this.getFiltroVendedor();
		Vendedor vendedorUsuario = Vendedor.buscarVendedorUsuario(Users.getCurrent());
		if (vendedorUsuario != null){
			if (!vendedorUsuario.getGerencia()){
				if (Is.emptyString(idVendedor)){
					idVendedor = vendedorUsuario.getId();
				}
				else if (!Is.equalAsString(idVendedor, vendedorUsuario.getId())){
					throw new ValidationException("Vendedor no autorizado");
				}
			}
		}		
		this.setFiltroVendedor(idVendedor);
		
		if (Is.equalAsString(this.getFormato(), "excel")){
			this.setFormat(JasperReportBaseAction.EXCEL);
		}
		else{
			this.setFormat(JasperReportBaseAction.PDF);
		}
		super.execute();
		
		this.closeDialog();
		addMessage("Listado Finalizado");
	}
	
	@Override
	protected String getNombreReporte() {
		return "SaldoCuentaCorrienteVenta.jrxml";
	}

	@Override
	protected void agregarParametros(Map<String, Object> parametros) {
		parametros.put("VENDEDOR", this.getFiltroVendedor());		
	}

	@Override
	protected JRDataSource getDataSource() throws Exception {
		return null;
	}

	@Override
	protected boolean filtraPorEmpresa(){
		return true;
	}

	public String getFormato() {
		return formato;
	}

	public void setFormato(String formato) {
		this.formato = formato;
	}
}
