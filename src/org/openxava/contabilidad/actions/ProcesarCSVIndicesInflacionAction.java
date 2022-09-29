package org.openxava.contabilidad.actions;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.openxava.base.actions.ProcesarCSVGenericoAction;
import org.openxava.contabilidad.model.EjercicioContable;
import org.openxava.contabilidad.model.PeriodoContable;
import org.openxava.model.MapFacade;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;

import com.csvreader.CsvReader;

public class ProcesarCSVIndicesInflacionAction extends ProcesarCSVGenericoAction{

	private EjercicioContable ejercicio;
	
	@Override
	protected void preProcesarCSV() throws Exception {
		this.ejercicio = (EjercicioContable)MapFacade.findEntity(this.getPreviousView().getModelName(), this.getPreviousView().getKeyValues());
		this.setMascaraDecimal("#,###.####");
	}

	@Override
	protected void posProcesarCSV() throws Exception {
		this.ejercicio = null;
		this.getPreviousView().refreshCollections();
	}

	@Override
	protected void procesarLineaCSV(CsvReader csvReader) throws IOException {
		String codigo = csvReader.get(0);
		if (!Is.emptyString(codigo)){
			PeriodoContable periodo = this.ejercicio.buscarPeriodo(codigo);
			String indice = csvReader.get(2);
			try{
				BigDecimal indiceInflacion = this.convertirStrDecimal(indice);
				indiceInflacion = indiceInflacion.setScale(4, RoundingMode.HALF_EVEN);
				periodo.setIndiceInflacion(indiceInflacion);
			}
			catch(ValidationException e){
				throw new ValidationException("Indice Inflación incorrecto: " + e.getErrors());
			}			
		}
		else{
			throw new ValidationException("Código no asignado");
		}		
	}
	
	protected int getCantidadColumnasObligatorias(){
		return 3;
	}
}
