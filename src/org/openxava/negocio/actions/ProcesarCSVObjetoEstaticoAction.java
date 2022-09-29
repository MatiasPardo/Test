package org.openxava.negocio.actions;

import java.io.IOException;

import org.openxava.base.actions.ProcesarCSVGenericoAction;
import org.openxava.base.model.ObjetoEstatico;
import org.openxava.jpa.XPersistence;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;

import com.csvreader.CsvReader;

public class ProcesarCSVObjetoEstaticoAction extends ProcesarCSVGenericoAction{

	@Override
	protected void preProcesarCSV() throws Exception {	
	}

	@Override
	protected void posProcesarCSV() throws Exception {	
	}

	@Override
	protected void procesarLineaCSV(CsvReader csvReader) throws IOException {
		String codigo = csvReader.get(0);
		String nombre = csvReader.get(1);
		
		if (Is.emptyString(codigo)){
			throw new ValidationException("Falta asignar el código");
		}
		if (Is.emptyString(nombre)){
			throw new ValidationException("Falta asignar el nombre");
		}
		
		ObjetoEstatico objeto = ObjetoEstatico.buscarPorCodigo(codigo, this.getTab().getModelName());
		if (objeto == null){
			// se crea el objeto
			try{
				objeto = (ObjetoEstatico)this.getTab().getMetaTab().getMetaModel().getPOJOClass().newInstance();
			}
			catch(Exception e){
				throw new ValidationException("No se pudo instanciar la clase");
			}
			objeto.setCodigo(codigo);
		}
		objeto.setNombre(nombre);
		if (objeto.esNuevo()){
			XPersistence.getManager().persist(objeto);
		}
	}
	
	@Override
	protected int getCantidadColumnasObligatorias(){
		return 2;
	}

}
