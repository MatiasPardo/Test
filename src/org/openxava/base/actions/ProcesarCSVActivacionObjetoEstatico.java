package org.openxava.base.actions;

import java.io.*;

import org.openxava.base.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

import com.csvreader.*;

public class ProcesarCSVActivacionObjetoEstatico extends ProcesarCSVGenericoAction{

	@Override
	protected void preProcesarCSV() throws Exception {
	}

	@Override
	protected void posProcesarCSV() throws Exception {
	}

	@Override
	protected void procesarLineaCSV(CsvReader csvReader) throws IOException {
		String codigo = csvReader.get(0);
		if (Is.emptyString(codigo)){
			throw new ValidationException("Código no asignado");
		}
		ObjetoEstatico objeto = ObjetoEstatico.buscarPorCodigo(codigo, this.getTab().getModelName());
		if (objeto != null){
			Boolean estado = this.convertirStrLogico(csvReader.get(1));
			if (estado && objeto.getActivo()){
				throw new ValidationException(codigo + " ya esta activo");
			}
			else if (!estado && !objeto.getActivo()){
				throw new ValidationException(codigo + " ya esta desactivado");
			}
			else{
				objeto.cambiarEstado();
				
			}
		}
		else{
			throw new ValidationException("No se encontró el código " + codigo);
		}
	}
	
	@Override
	protected int getCantidadColumnasObligatorias(){
		return 2;
	}

}
