package org.openxava.ventas.actions;

import java.io.*;
import java.math.*;
import java.util.*;

import org.openxava.base.actions.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.model.*;
import org.openxava.negocio.model.UnidadMedida;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

import com.csvreader.*;

public class ProcesarCSVPrecioAction extends ProcesarCSVGenericoAction{
	
	private Collection<ListaPrecio> listasPrecio = new LinkedList<ListaPrecio>();
	
	@SuppressWarnings("rawtypes")
	protected void preProcesarCSV() throws Exception{
		Map [] selectedOnes = this.getSelectedKeys();
		this.listasPrecio.clear();
		if (selectedOnes != null) {
			if (selectedOnes.length > 0){
				addInfo("Listas de Precios seleccionadas: ");
				for (int i = 0; i < selectedOnes.length; i++) {
					Map clave = selectedOnes[i];
					ListaPrecio lista = (ListaPrecio)MapFacade.findEntity(this.getTab().getModelName(), clave);
					this.listasPrecio.add(lista);
					addInfo(lista.getNombre());
				}
			}
		}
		if (this.listasPrecio.isEmpty()){
			throw new ValidationException("Debe seleccionar las listas de precio que desea actualizar");
		}		
	}
	
	@Override
	protected void procesarLineaCSV(CsvReader csvReader) throws IOException {
		String codigo = csvReader.get(0);
		if (!Is.emptyString(codigo)){
			Producto producto = (Producto)ObjetoEstatico.buscarPorCodigo(codigo, Producto.class.getSimpleName());
			if (producto == null){
				throw new ValidationException("No existe el producto " + codigo);
			}			
			BigDecimal importeBase = this.convertirStrDecimal((String)csvReader.get(1));
			if (importeBase.compareTo(BigDecimal.ZERO) == 0){
				throw new ValidationException(codigo + " sin precio");
			}
			BigDecimal porcentaje = this.convertirStrDecimal((String)csvReader.get(2));
			boolean precioPorCantidad = false;
			BigDecimal desde = null;
			BigDecimal hasta = null;
			if (csvReader.getColumnCount() >= 5){
				if (!Is.emptyString((String)csvReader.get(3)) && !Is.emptyString((String)csvReader.get(4))){
					desde = this.convertirStrDecimal((String)csvReader.get(3));
					hasta = this.convertirStrDecimal((String)csvReader.get(4));
					if ((hasta.compareTo(desde) > 0) && (hasta.compareTo(BigDecimal.ZERO) != 0)){
						precioPorCantidad = true;
					}
				}
			}
			UnidadMedida unidadMedida = producto.getUnidadMedida();
			if (csvReader.getColumnCount() >= 6){
				String codigoUM = (String)csvReader.get(5);
				if (!Is.emptyString(codigoUM)){
					unidadMedida = (UnidadMedida)ObjetoEstatico.buscarPorCodigo(codigoUM, UnidadMedida.class.getSimpleName());
					if (unidadMedida == null){
						throw new ValidationException("No existe la unidad de medida " + codigoUM);
					}
					else{
						if (!producto.unidadMedidaPermitida(unidadMedida)){
							Messages errors = new Messages();
							errors.add("unidadMedida_no_permitida", producto.getCodigo(), unidadMedida.getCodigo());
							throw new ValidationException(errors);							
						}
					}
				}
			}
			for(ListaPrecio lista: this.listasPrecio){
				Precio precio = null;
				if (precioPorCantidad){
					precio = lista.actualizarPrecioPorCantidad(producto, unidadMedida, importeBase, porcentaje, desde, hasta);
				}
				else{
					precio = lista.actualizarPrecio(producto, unidadMedida, importeBase, porcentaje);					
				}
				XPersistence.getManager().persist(precio);
			}			
		}
		else{
			throw new ValidationException("Código no asignado");
		}
	}
	
	@Override
	protected void posProcesarCSV() throws Exception {		
	}	
}
