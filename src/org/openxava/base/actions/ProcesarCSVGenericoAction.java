package org.openxava.base.actions;

import java.io.*;
import java.math.*;
import java.text.*;
import java.util.*;

import javax.persistence.*;

import org.apache.commons.fileupload.*;
import org.openxava.actions.*;
import org.openxava.base.model.UtilERP;
import org.openxava.util.*;
import org.openxava.validators.*;


import com.csvreader.*;

public abstract class ProcesarCSVGenericoAction extends TabBaseAction implements INavigationAction, IProcessLoadedFileAction{
	
	private final int MAXIMOLINEAS = 10000;
	private final int MAXIMOERRORES = 100;
	private List<?> fileItems = null;
	
	private String mascaraFecha = "dd/MM/yyyy";
	
	private String mascaraDecimal = "#,###.##";
	
	public int getMaximoLineas(){
		return this.MAXIMOLINEAS;
	}
	
	public int getMaximoErrores(){
		return this.MAXIMOERRORES;
	}
		
	protected abstract void preProcesarCSV() throws Exception;
	
	protected abstract void posProcesarCSV() throws Exception;
	
	protected abstract void procesarLineaCSV(CsvReader csvReader) throws IOException;
	
	@Override
	public void execute() throws Exception {
		
		if (this.fileItems != null){
			if (!this.fileItems.isEmpty()){
				Iterator<?> it = this.fileItems.iterator();
				while (it.hasNext()){
					FileItem fileItem = (FileItem) it.next();
					if (!fileItem.isFormField()){
						// es el archivo CSV						
						CsvReader csvReader = null;
						try {
							this.preProcesarCSV();
							InputStream data = fileItem.getInputStream();
							Reader reader = new InputStreamReader(data); 
							csvReader = new CsvReader(reader, ';');
							Integer i = new Integer(1);							
							int errores = 0;
							int objetosImportados = 0;
							int numeroFilaComienzo = this.numeroFilaInicial();
							if (numeroFilaComienzo < 1){
								throw new ValidationException("La fila de comienzo no puede ser " + Integer.toString(numeroFilaComienzo));
							}
							while (csvReader.readRecord() && (i <= this.getMaximoLineas())){
								if (i >= numeroFilaComienzo){
									try{
										this.validarCantidadColumnasObligatorias(csvReader);
										procesarLineaCSV(csvReader);
										objetosImportados++;
										
										if (this.commitParcial()){
											this.commit();
										}										
									}
									catch(PersistenceException e){
										errores ++;										
										if (errores <= this.getMaximoErrores()){
											addError("Error fila " + i.toString() + ": " + UtilERP.obtenerErrorPersistException(e));
										}
										
										if (this.commitParcial()){
											this.rollback();
										}
									}
									catch(Exception e){
										errores ++;
										String error = e.getMessage();
										if (Is.emptyString(error)){
											error = e.toString(); 
										}
										if (errores <= this.getMaximoErrores()){
											addError("Error fila " + i.toString() + ": " + error);
										}
										
										if (this.commitParcial()){
											this.rollback();
										}
									}
								}
								i++;
							}
							
							this.posProcesarCSV();
							
							if (i > this.getMaximoLineas()){
								addError("Se procesaron las primeros " + Integer.toString(this.getMaximoLineas()) + ". Los demás debe generar otro csv");
							}
							
							if (this.getErrors().isEmpty()){
								addMessage("Importación Finalizada: Lineas procesadas " + Integer.toString(objetosImportados));
								this.getTab().deselectAll();
							}
							else{
								this.rollback();
								addMessage("Lineas procesadas correctamente " + Integer.toString(objetosImportados));
							}							
						}
						catch(PersistenceException e){
							this.rollback();							
							addError("Error importación CSV: " + UtilERP.obtenerErrorPersistException(e));							
						}
						catch (Exception e){
							this.rollback();
							addError("Error importación CSV: " + e.getMessage());
						}
						finally{
							if (csvReader != null){
								csvReader.close();
							}
						}
						break;
					}
				}
			}
		}
		closeDialog();
	}
	
	@Override
	public String[] getNextControllers() throws Exception {
		return DEFAULT_CONTROLLERS;		
	}

	@Override
	public String getCustomView() throws Exception {
		return DEFAULT_VIEW;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setFileItems(List fileItems) {
		this.fileItems = fileItems;		
	}
	
	public Object convertirStrEnum(String valor, Object[] values, String error){
		String[] split = valor.trim().split(" ");
		StringBuffer str = new StringBuffer();		
		for(String string: split){
			str.append(string);		
		}
		
		for(Object obj: values){
			if (Is.equalAsStringIgnoreCase(obj.toString(), str.toString())){
				return obj;
			}
		}		
		throw new ValidationException(error + " " + valor);
	}
	
	public BigDecimal convertirStrDecimal(String decimal){
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator(',');
		symbols.setGroupingSeparator('.');
		DecimalFormat decimalFormat = new DecimalFormat(this.getMascaraDecimal(), symbols);		
		try {
			return new BigDecimal(decimalFormat.parse(decimal).doubleValue());			
		}
		catch(Exception e){
			throw new ValidationException("Error al procesar el decimal " + decimal);
		}
	}
	
	public BigDecimal convertirStrDecimal(String decimal, int scale){
		BigDecimal importe = convertirStrDecimal(decimal);
		if (importe != null){
			importe = importe.setScale(scale, RoundingMode.HALF_EVEN);
		}
		return importe;
	}
	
	public Double convertirStrDouble(String decimal){
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator(',');
		symbols.setGroupingSeparator('.');
		DecimalFormat decimalFormat = new DecimalFormat("#,###.######", symbols);		
		try {
			return decimalFormat.parse(decimal).doubleValue();			
		}
		catch(Exception e){
			throw new ValidationException("Error al procesar el decimal " + decimal);
		}
	}
	
	public Date convertirStrFecha(String fecha){
		if (fecha != null){
			DateFormat format = new SimpleDateFormat(this.getMascaraFecha());
			Date fechaFormat; 
			try {
				fechaFormat = format.parse(fecha);				
			}
			catch(Exception e){
				throw new ValidationException("Error al procesar la fecha " + fecha);
			}
			UtilERP.validarRangoFecha(fechaFormat);
			return fechaFormat;
		}
		else{
			throw new ValidationException("Fecha no asignada");
		}
	}
	
	public Boolean convertirStrLogico(String valor){
		if (!Is.emptyString(valor)){
			if (Is.equalAsStringIgnoreCase(valor, "f") || Is.equalAsStringIgnoreCase(valor, "NO")){
				return Boolean.FALSE;
			}
			else if (Is.equalAsStringIgnoreCase(valor, "t") || Is.equalAsStringIgnoreCase(valor, "SI") || Is.equalAsStringIgnoreCase(valor, "Sí")){
				return Boolean.TRUE;
			}
			else{
				throw new ValidationException("El valor esperado es si o no, pero recibió " + valor);
			}
		}
		else{
			throw new ValidationException("Valor vacío: falta asignar Si o No");
		}
	}
	
	protected Boolean commitParcial(){
		return Boolean.TRUE;
	}

	private void validarCantidadColumnasObligatorias(CsvReader csvReader){
		if (this.getCantidadColumnasObligatorias() > 0){
			if (csvReader.getColumnCount() < this.getCantidadColumnasObligatorias()){
				throw new ValidationException("Faltan columnas en el archivo. Deben ser " + Integer.toString(this.getCantidadColumnasObligatorias()));
			}
		}
	}
	
	protected int getCantidadColumnasObligatorias(){
		return 0;
	}
	
	protected int numeroFilaInicial() {
		// La primer fila son los titulos, por defecto comienza en la segunda fila
		return 2;
	}
	
	protected String obtenerCampo(CsvReader csv, int columna, String descripcionCampo){
		try{
			return csv.get(columna);
		}
		catch(Exception e){
			throw new ValidationException("Error al obtener columna " + Integer.toString(columna) + " del campo " + descripcionCampo + ": " + e.getMessage() );
		}
	}

	public String getMascaraFecha() {
		return mascaraFecha;
	}

	public void setMascaraFecha(String mascaraFecha) {
		if (mascaraFecha != null){
			this.mascaraFecha = mascaraFecha;
		}
	}

	public String getMascaraDecimal() {
		return mascaraDecimal;
	}

	public void setMascaraDecimal(String mascaraDecimal) {
		if (mascaraDecimal != null){
			this.mascaraDecimal = mascaraDecimal;
		}
	}
}
