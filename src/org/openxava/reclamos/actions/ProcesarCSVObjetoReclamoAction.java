package org.openxava.reclamos.actions;

import java.io.*;
import java.math.*;

import javax.validation.*;

import org.openxava.base.actions.*;
import org.openxava.base.model.*;
import org.openxava.clasificadores.model.*;
import org.openxava.jpa.*;
import org.openxava.reclamos.model.*;
import org.openxava.util.*;
import org.openxava.ventas.model.*;

import com.csvreader.*;

public class ProcesarCSVObjetoReclamoAction extends ProcesarCSVGenericoAction{

	private final int CANTIDADCOLUMNASOBLIGATORIAS = 9;
	
	@Override
	protected void preProcesarCSV() throws Exception {
	}

	@Override
	protected void procesarLineaCSV(CsvReader csvReader) throws IOException {
		int cantidadColumnas = csvReader.getColumnCount();
		
		if (cantidadColumnas < CANTIDADCOLUMNASOBLIGATORIAS){
			throw new ValidationException("Faltan columnas en el archivo");
		}
		
		String codigo = csvReader.get(0);
		if(!Is.emptyString(codigo)) {//compruebo que el codigo venga cargado para luego actualizarlo
			ObjetoReclamo objetoReclamo = (ObjetoReclamo)ObjetoEstatico.buscarPorCodigo(codigo, ObjetoReclamo.class.getSimpleName()); //(como ya existe, lo busco en mi base de datos)
			if(objetoReclamo == null) { //compruebo que no este creado (si esta vacio el objeto no existe)
				objetoReclamo = new ObjetoReclamo();
				objetoReclamo.setCodigo(codigo);
			}
			objetoReclamo.setNombre(csvReader.get(1));
			objetoReclamo.setCalle(csvReader.get(2));
			if (!Is.emptyString(csvReader.get(3))){
				objetoReclamo.setAltura(Integer.parseInt(csvReader.get(3)));
			}
			objetoReclamo.setCalle1(csvReader.get(4));
			objetoReclamo.setCalle2(csvReader.get(5));
		
			String codigoZona = csvReader.get(6);
			
			Zona zona = null;
			if(!Is.emptyString(codigoZona)) {
				zona = (Zona)ObjetoEstatico.buscarPorCodigoError(codigoZona, Zona.class.getSimpleName());
				if(zona != null) {
					objetoReclamo.setZona(zona);				
				}				
			}
			else throw new ValidationException("codigo_zona_no_asignado");
		
			String codigoSubzona = csvReader.get(7);
			
			if(!Is.emptyString(codigoSubzona)) {
				Subzona subzona = (Subzona)ObjetoEstatico.buscarPorCodigoError(codigoSubzona, Subzona.class.getSimpleName());
				if(subzona != null) {
					if (subzona.getZona().equals(zona)){
						objetoReclamo.setSubzona(subzona);
					}
					else{
						throw new ValidationException("La subzona " + subzona.getNombre() + " debe coincidir con la zona " + zona.getNombre());
					}
				}				
			}
			else throw new ValidationException("codigo_subzona_no_asignado");
			
			String codigoGrupoUsuario = csvReader.get(8);
			if (!Is.emptyString(codigoGrupoUsuario)){
				GrupoUsuarioObjetoReclamo grupo = (GrupoUsuarioObjetoReclamo)ObjetoEstatico.buscarPorCodigoError(codigoGrupoUsuario, GrupoUsuarioObjetoReclamo.class.getSimpleName());
				if (grupo != null){
					objetoReclamo.setSeguridad(grupo);
				}
			}
			else throw new ValidationException("Código seguridad no asignado");
			
			int i = 9;
			if (cantidadColumnas > i){
				String potenciaString = csvReader.get(i);
				if (!Is.emptyString(potenciaString)){
					BigDecimal potenciaCasteada = this.convertirStrDecimal(potenciaString);
					objetoReclamo.setPotencia(potenciaCasteada);
				}
				i++;
				
				if (cantidadColumnas > i){
					objetoReclamo.setNumeroPiquete(csvReader.get(i));					
				}
				i++;
				
				if (cantidadColumnas > i){
					objetoReclamo.setPlano(csvReader.get(i));					
				}
				i++;
				
				if (cantidadColumnas > i){
					if (!Is.emptyString(csvReader.get(i))){
						objetoReclamo.setObjetoReclamoClasificador1(Clasificador.buscar(csvReader.get(i), ObjetoReclamo.class.getSimpleName(), 1));
					}
				}
				i++;
				
				if (cantidadColumnas > i){
					if (!Is.emptyString(csvReader.get(i))){
						objetoReclamo.setObjetoReclamoClasificador2(Clasificador.buscar(csvReader.get(i), ObjetoReclamo.class.getSimpleName(), 2));
					}
				}
				i++;
				
				if (cantidadColumnas > i){
					if (!Is.emptyString(csvReader.get(i))){
						objetoReclamo.setObjetoReclamoClasificador3(Clasificador.buscar(csvReader.get(i), ObjetoReclamo.class.getSimpleName(), 3));
					}
				}
				i++;
				
				if (cantidadColumnas > i){
					if (!Is.emptyString(csvReader.get(i))){
						objetoReclamo.setObjetoReclamoClasificador4(Clasificador.buscar(csvReader.get(i), ObjetoReclamo.class.getSimpleName(), 4));
					}
				}
				i++;
				
				if (cantidadColumnas > i){
					if (!Is.emptyString(csvReader.get(i))){
						objetoReclamo.setObjetoReclamoClasificador5(Clasificador.buscar(csvReader.get(i), ObjetoReclamo.class.getSimpleName(), 5));
					}
				}
				i++;
			}		
			XPersistence.getManager().persist(objetoReclamo);
		
		}else throw new ValidationException("codigo_no_asignado");
					
	}

	@Override
	protected void posProcesarCSV() throws Exception {		
	}	

}
