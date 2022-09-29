package org.openxava.inventario.actions;

import java.io.IOException;
import java.util.Date;

import org.openxava.base.actions.ProcesarCSVGenericoAction;
import org.openxava.base.model.ObjetoEstatico;
import org.openxava.inventario.model.Lote;
import org.openxava.jpa.XPersistence;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.Producto;

import com.csvreader.CsvReader;

public class ProcesarCSVLoteAction extends ProcesarCSVGenericoAction{

	@Override
	protected void preProcesarCSV() throws Exception {
	}

	@Override
	protected void posProcesarCSV() throws Exception {	
	}

	@Override
	protected void procesarLineaCSV(CsvReader csvReader) throws IOException {
		String codigo = csvReader.get(0);		
		String codigoProducto = csvReader.get(2);		
		if (Is.emptyString(codigo)){
			throw new ValidationException("Código no asignado");
		}
		else if (Is.emptyString(codigoProducto)){
			throw new ValidationException("Producto no asignado");
		}		
		else{
			String fechaVencimientoStr = csvReader.get(1);
			Date fechaVencimiento = this.convertirStrFecha(fechaVencimientoStr);
			Lote lote = Lote.buscarPorCodigo(codigo, codigoProducto);
			if (lote == null){
				lote = new Lote();
				lote.setCodigo(codigo);
				lote.setProducto((Producto)ObjetoEstatico.buscarPorCodigo(codigoProducto, Producto.class.getSimpleName()));
				if (lote.getProducto() == null){
					throw new ValidationException("No existe el producto " + codigoProducto);
				}
				lote.setFechaVencimiento(fechaVencimiento);
				XPersistence.getManager().persist(lote);
			}
			else{
				throw new ValidationException("Ya existe lote " + codigo + " producto " + codigoProducto);
			}
		}		
	}

}
