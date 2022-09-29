package org.openxava.reportes.actions;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.validators.*;

import net.sf.jasperreports.engine.*;


public class ReportGenerarArchivo{
	
	private String modelName;
	
	private String JRXML;

	private Map<String, Object> parametros;
	
	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public Map<String, Object> getParametros() {
		return parametros;
	}

	public void setParametros(Map<String, Object> parametros) {
		this.parametros = parametros;
	}

	public String getJRXML() {
		return JRXML;
	}

	public void setJRXML(String jRXML) {
		JRXML = jRXML;
	}
	
	public void generar(String fullFileName) throws Exception{
		
		if (Is.emptyString(this.getJRXML())){
			throw new ValidationException("No esta asignado jrxml");
		}
		
		if (this.getParametros() == null){
			throw new ValidationException("No hay parámetros");
		}
		else if (this.getParametros().isEmpty()){
			throw new ValidationException("Debe haber por lo menos un parámetro");
		}
		if (Is.emptyString(this.getModelName())){
			throw new ValidationException("Falta asignar el modelName");
		}
		
		InputStream xmlDesign = null;		
		String jrxml = getJRXML();
		
		
		xmlDesign = new FileInputStream(jrxml);		
		JasperReport report = JasperCompileManager.compileReport(xmlDesign);
		Connection con = null;
		OutputStream output = null;
		try {
			con = DataSourceConnectionProvider.getByComponent(this.getModelName()).getConnection();
			con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED); 
			if (!Is.emptyString(XPersistence.getDefaultSchema())) {
				con.setCatalog(XPersistence.getDefaultSchema());
			}			
			JasperPrint jprint = JasperFillManager.fillReport(report, parametros, con);
			output = new FileOutputStream(fullFileName);
			JasperExportManager.exportReportToPdfStream(jprint, output);			
		} finally {
			con.close();
			if (output != null){
				output.close();
			}
		}		
	}
	
}
