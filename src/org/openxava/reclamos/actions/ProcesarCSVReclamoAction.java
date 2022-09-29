package org.openxava.reclamos.actions;

import java.io.*;
import java.util.*;

import org.openxava.base.actions.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.calculators.*;
import org.openxava.reclamos.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

import com.csvreader.*;

public class ProcesarCSVReclamoAction extends ProcesarCSVGenericoAction {

	private static final int CANTIDADCOLUMNASOBLIGATORIAS = 9;
	private ObjetoReclamo objetoPorDefecto = null;
	
	@Override
	protected void preProcesarCSV() throws Exception {
	}

	@Override
	protected Boolean commitParcial() {
		return Boolean.FALSE;
	}
	
	@Override
	protected void procesarLineaCSV(CsvReader csvReader) throws IOException {
		
		Messages errores = new Messages();
		Map<String,String> mensaje = new HashMap<String, String>();
		int cantidadColumnas = csvReader.getColumnCount();
		
		if (cantidadColumnas < CANTIDADCOLUMNASOBLIGATORIAS){
			throw new ValidationException("Faltan columnas en el archivo");
		}
	
		String codigoOrigen = csvReader.get(0);
		OrigenReclamo origen = null;
		if(!Is.emptyString(codigoOrigen)){
			try{
				origen = (OrigenReclamo) ObjetoEstatico.buscarPorCodigoError(codigoOrigen, OrigenReclamo.class.getSimpleName());
			}catch (Exception e){
				errores.add(e.getMessage());
			}
		}else {
			errores.add("error_busqueda_codigo","OrigenReclamo");
		}
		
		String codigoObjetoReclamo = csvReader.get(6); //piquete
		ObjetoReclamo objetoReclamo = (ObjetoReclamo) ObjetoEstatico.buscarPorCodigo(codigoObjetoReclamo, ObjetoReclamo.class.getSimpleName()); //(como ya existe, lo busco en mi base de datos)
		if(objetoReclamo == null) { 
			try{
				if(objetoPorDefecto == null){
					ObjetoPrincipalCalculator calculator = new ObjetoPrincipalCalculator();
					calculator.setEntidad(ObjetoReclamo.class.getSimpleName());
					objetoPorDefecto = (ObjetoReclamo) calculator.calculate();
				}
				if(objetoPorDefecto == null){
					errores.add("error_busqueda_codigo","ObjetoReclamo");
				}else{
					objetoReclamo = objetoPorDefecto;
					mensaje.put("usa_principal","ObjetoReclamo");
				}
			}catch (Exception e){
				errores.add(e.toString());
			}
		}
		
		String codigoTipoReclamo = csvReader.get(7); //falla
		TipoReclamo tipoReclamo = null;
		
		if(!Is.emptyString(codigoTipoReclamo)){
			try{
				tipoReclamo = (TipoReclamo) ObjetoEstatico.buscarPorCodigoError(codigoTipoReclamo, TipoReclamo.class.getSimpleName());
			}catch (Exception e){
				errores.add("error_busqueda_codigo","TipoReclamo");
				
			}
		}else {
			errores.add("error_busqueda_codigo","TipoReclamo");
		}
		
		Reclamo reclamo = new Reclamo();
		reclamo.setObjetoReclamo(objetoReclamo);
		reclamo.setTipoReclamo(tipoReclamo);
		reclamo.setOrigen(origen);
		reclamo.setObservaciones(this.armarCampoObservaciones(csvReader));
		if(!Is.empty(reclamo.getObservaciones())){
			Reclamo reclamoDuplicado = Reclamo.buscarReclamoPorObservaciones(reclamo.getObservaciones());
			if(reclamoDuplicado != null ){
				errores.add("Reclamo duplicado");
			}
		}
		
		if(!errores.isEmpty()){
			throw new ValidationException(errores);
		}
		else{
			mensaje.forEach((k,v)->addMessage(k, v));
		}
		
		try{
			ObjetoPrincipalCalculator calculator = new ObjetoPrincipalCalculator();
			calculator.setEntidad(UsuarioReclamo.class.getSimpleName());
			reclamo.setAsignadoA((UsuarioReclamo)calculator.calculate());			
		}catch (Exception e){			
		}

		XPersistence.getManager().persist(reclamo);
					
	}

	private String armarCampoObservaciones(CsvReader csvReader) throws IOException {
		String calle = csvReader.get(1);
		String nro = csvReader.get(2);
		String entreCalles = csvReader.get(3);
		String zona = csvReader.get(4);
		String subZona = csvReader.get(5);
		String codigoTipoReclamo = csvReader.get(7); //falla
		String codigoObjetoReclamo = csvReader.get(6); //piquete
		String observaciones = csvReader.get(8); 
		String separador = new String(" / ");
		StringBuilder observacionConcatenada = new StringBuilder();
		observacionConcatenada.append("CALLE: ")
			.append(calle).append(separador)
			.append("NRO: ")
			.append(nro).append(separador)
			.append("ENTRE CALLE: ")
			.append(entreCalles).append(separador)
			.append("ZONA: ")
			.append(zona).append(separador)
			.append("SUB ZONA: ")
			.append(subZona).append(separador)
			.append("PIQUETE: ")
			.append(codigoObjetoReclamo).append(separador)
			.append("FALLA: ")
			.append(codigoTipoReclamo).append(separador);
		
		observacionConcatenada.append(observaciones);		
		/*
		 * verificar que no haya otro reclamo en estado abierto o borrador con las mismas observaciones 
		 * 
		 */
		if(observacionConcatenada.length() >255){
			observaciones = observacionConcatenada.substring(0, 255);
		}else observaciones = observacionConcatenada.toString();
		
		return observaciones;
	}
	
	@Override
	protected void posProcesarCSV() throws Exception {

	/*	int cantidadErrores = erroresTotales.getStrings().size();
		if(cantidadErrores > 0){
			addError("El archivo contiene " + cantidadErrores + " error/es, corrijalos y vuelva a intentar");
		}
	*/
	}

}
