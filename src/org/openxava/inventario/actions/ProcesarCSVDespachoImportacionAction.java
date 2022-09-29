package org.openxava.inventario.actions;

import java.io.*;

import org.openxava.base.actions.*;
import org.openxava.base.model.*;
import org.openxava.inventario.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

import com.csvreader.*;

public class ProcesarCSVDespachoImportacionAction extends ProcesarCSVGenericoAction{

	@Override
	protected void preProcesarCSV() throws Exception {	
	}

	@Override
	protected void posProcesarCSV() throws Exception {		
	}

	@Override
	protected void procesarLineaCSV(CsvReader csvReader) throws IOException {
		String codigo = csvReader.get(0);
		if (!Is.emptyString(codigo)){
			DespachoImportacion despacho = DespachoImportacion.buscar(codigo);
			if (despacho == null){
				despacho = new DespachoImportacion();
				despacho.setCodigo(codigo);
			}
			if (csvReader.getColumnCount() > 1){
				String codigoInterno = csvReader.get(1);
				despacho.setCodigoInterno(codigoInterno);
			}
			if (csvReader.getColumnCount() > 2){
				String codigoAduana = csvReader.get(2);
				if (!Is.emptyString(codigoAduana)){
					Aduana aduana = (Aduana)ObjetoEstatico.buscarPorCodigo(codigoAduana, Aduana.class.getSimpleName());
					if (aduana != null){
						despacho.setAduana(aduana);
					}
					else{
						throw new ValidationException("Código de aduana no existe: " + codigoAduana);
					}
				}
			}
			if (csvReader.getColumnCount() > 3){
				String codigoPais = csvReader.get(3);
				if (!Is.emptyString(codigoPais)){
					Pais pais = (Pais)ObjetoEstatico.buscarPorCodigo(codigoPais, Pais.class.getSimpleName());
					if (pais != null){
						despacho.setOrigen(pais);
					}
					else{
						throw new ValidationException("Código de país no existe: " + codigoPais);
					}
				}
			}
			XPersistence.getManager().persist(despacho);
		}
		else{
			throw new ValidationException("Código no asignado");			
		}
		
	}

	protected int getCantidadColumnasObligatorias(){
		return 1;
	}
}
