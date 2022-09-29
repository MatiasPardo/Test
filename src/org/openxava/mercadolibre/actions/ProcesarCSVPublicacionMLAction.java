package org.openxava.mercadolibre.actions;

import java.io.IOException;
import java.util.*;

import org.openxava.base.actions.ProcesarCSVGenericoAction;
import org.openxava.base.model.*;
import org.openxava.mercadolibre.model.ConfiguracionMercadoLibre;
import org.openxava.mercadolibre.model.Ecommerce;
import org.openxava.mercadolibre.model.PublicacionML;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.Producto;

import com.csvreader.CsvReader;

public class ProcesarCSVPublicacionMLAction extends ProcesarCSVGenericoAction{

	
	@Override
	protected void posProcesarCSV() throws Exception {		
	}

	@Override
	protected void procesarLineaCSV(CsvReader csvReader) throws IOException {
		String idMercadoLibre = csvReader.get(0); //columna 1 se usa para el id de mercadolibre
		String codigoProducto = csvReader.get(1); //columna 2 se usa para el codigo de producto de cloud
		String idVarianteML = null; // column 3 - id de producto de publicacionml (en ML representa la variante)

		ConfiguracionMercadoLibre configurador = null;
		
		if (Is.emptyString(idMercadoLibre)){
			throw new ValidationException("Id mercado libre no asignado");
		}
		if (Is.emptyString(codigoProducto)){
			throw new ValidationException("Código producto no asignado");
		}
		
		if (!Is.emptyString(csvReader.get(2))){
			idVarianteML = csvReader.get(2);
		}
		
		List<ConfiguracionMercadoLibre> configuradores = ConfiguracionMercadoLibre.buscarConfiguradores(Ecommerce.MercadoLibre);
		
		if(configuradores.size() > 1){
			if (csvReader.getColumnCount() > 3){
				if (Is.emptyString(csvReader.get(3))){
					throw new ValidationException("Columna 4 debe tener asignado el codigo de configurador");
				}else{
					configurador = (ConfiguracionMercadoLibre)ObjetoEstatico.buscarPorCodigoError(csvReader.get(3), ConfiguracionMercadoLibre.class.getSimpleName());
				}

			}else{
				throw new ValidationException("Columna 4 debe tener asignado el codigo de configurador");
			}

		}
		
		if(configuradores.size() == 1){
			configurador = configuradores.get(0);
		}
		
		
	/*	if (csvReader.getColumnCount() > 3){
			if (Is.emptyString(csvReader.get(3))){
				configuradores = ConfiguracionMercadoLibre.buscarConfiguradores(Ecommerce.MercadoLibre);
				if(configuradores.size() == 1){
					configurador = configuradores.get(0);
				}else{
					throw new ValidationException("Columna 4 debe tener asignado el codigo de configurador");
				}
			}else{
				configurador = (ConfiguracionMercadoLibre)ObjetoEstatico.buscarPorCodigoError(csvReader.get(3), ConfiguracionMercadoLibre.class.getSimpleName());
			}
		}
		else{
			configuradores = ConfiguracionMercadoLibre.buscarConfiguradores(Ecommerce.MercadoLibre);
			if(configuradores.size() == 1){
				configurador = configuradores.get(0);
			}else{
				throw new ValidationException("Columna 4 debe tener asignado el codigo de configurador, porque existen mas de 1");
			}
		}
*/
		Producto producto = (Producto)Producto.buscarPorCodigoError(codigoProducto, Producto.class.getSimpleName());
		PublicacionML.crearPublicacion(idMercadoLibre, producto, configurador, idVarianteML);

		
	}
	
	@Override
	protected int getCantidadColumnasObligatorias(){
		return 2;
	}

	@Override
	protected void preProcesarCSV() throws Exception {
		// TODO Auto-generated method stub
		
	}
}
