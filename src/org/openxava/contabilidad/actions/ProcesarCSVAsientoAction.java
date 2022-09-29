package org.openxava.contabilidad.actions;

import java.io.*;
import java.math.*;
import java.util.*;

import javax.persistence.*;
import javax.validation.*;

import org.apache.commons.lang3.exception.*;
import org.openxava.base.actions.*;
import org.openxava.base.model.*;
import org.openxava.contabilidad.model.*;
import org.openxava.jpa.*;
import org.openxava.util.*;

import com.csvreader.*;

public class ProcesarCSVAsientoAction extends ProcesarCSVGenericoAction{

	private Asiento ultimoAsiento = null;
		
	@Override
	protected void preProcesarCSV() throws Exception {
		this.ultimoAsiento = null;
	}

	@Override
	protected void procesarLineaCSV(CsvReader csvReader) throws IOException {
		String numeroAsiento = csvReader.get(1);
		if (Is.emptyString(numeroAsiento)){
			throw new ValidationException("Falta asignar número de asiento");
		}
		
		if (this.nuevoAsiento(numeroAsiento)){
			this.confirmarUltimoAsiento();
			this.crearAsiento(numeroAsiento, csvReader);
		}
		// agregar item
		ItemAsiento itemAsiento = new ItemAsiento();
		itemAsiento.setAsiento(this.ultimoAsiento);
		if (Is.emptyString(csvReader.get(5))){
			throw new ValidationException("Código de cuenta no asignada");
		}
		itemAsiento.setCuenta((CuentaContable)CuentaContable.buscarPorCodigo(csvReader.get(5), CuentaContable.class.getSimpleName()));
		if (itemAsiento.getCuenta() == null){
			throw new ValidationException("No existe la cuenta de código: " + csvReader.get(5));
		}
		if (!Is.emptyString(csvReader.get(6))){
			BigDecimal importe = this.convertirStrDecimal(csvReader.get(6), 2);
			if (importe.compareTo(BigDecimal.ZERO) < 0){
				throw new ValidationException("Importe en negativo " + csvReader.get(6));
			}
			itemAsiento.setDebe(importe);
		}
		if (!Is.emptyString(csvReader.get(7))){
			BigDecimal importe = this.convertirStrDecimal(csvReader.get(7), 2);
			if (importe.compareTo(BigDecimal.ZERO) < 0){
				throw new ValidationException("Importe en negativo " + csvReader.get(7));
			}
			itemAsiento.setHaber(importe);
		}
		itemAsiento.setDetalle(csvReader.get(8));
		if (csvReader.getColumnCount() >= 10){
			String codigoCentroCostos = csvReader.get(9);
			if (!Is.emptyString(codigoCentroCostos)){				
				itemAsiento.setCentroCostos((CentroCostos)CentroCostos.buscarPorCodigo(codigoCentroCostos, CentroCostos.class.getSimpleName()));
				if (itemAsiento.getCentroCostos() == null){
					throw new ValidationException("No existe el centro de costos de código " + itemAsiento.getCentroCostos().getCodigo());
				}
			}
		}
		if (csvReader.getColumnCount() >= 11){
			String codigoUnidadNegocio = csvReader.get(10);
			if (!Is.emptyString(codigoUnidadNegocio)){				
				itemAsiento.setUnidadNegocio((UnidadNegocio)UnidadNegocio.buscarPorCodigo(codigoUnidadNegocio, UnidadNegocio.class.getSimpleName()));				
			}
		}
		itemAsiento.recalcular();		
		XPersistence.getManager().persist(itemAsiento);
		this.ultimoAsiento.getItems().add(itemAsiento);
	}

	@Override
	protected void posProcesarCSV() throws Exception {
		this.confirmarUltimoAsiento();
	}

	@Override
	protected Boolean commitParcial(){
		return Boolean.FALSE;
	}
	
	private boolean nuevoAsiento(String numero){
		if (this.ultimoAsiento == null){
			return true;
		}
		else{
			return !Is.equalAsString(numero, this.ultimoAsiento.getNumero());
		}		
	}
	
	private void confirmarUltimoAsiento(){
		if (this.ultimoAsiento != null){
			try{
				if (!this.ultimoAsiento.getItems().isEmpty()){
					this.ultimoAsiento.grabarTransaccion();
					this.commit();
					 
					this.ultimoAsiento = XPersistence.getManager().find(Asiento.class, this.ultimoAsiento.getId());
					this.ultimoAsiento.confirmarTransaccion();
					this.commit();
					this.addMessage("Asiento confirmado " + this.ultimoAsiento.getNumero());
				}
				else{
					this.commit();
					this.addMessage("Asiento no confirmado " + this.ultimoAsiento.getNumero());
				}
			}
			catch(PersistenceException e){
				this.rollback();							
				
				String error = ExceptionUtils.getRootCauseMessage(e);
				if (Is.emptyString(error)){
					error = e.getMessage();
				}
				addError("No se pudo confirmar el asiento " + this.ultimoAsiento.getNumero() + ": " + error);				
			}
			catch(Exception e){
				this.rollback();
				
				this.addError("No se pudo confirmar el asiento " + this.ultimoAsiento.getNumero() + ": " + e.getMessage());				
			}
			finally {
				this.ultimoAsiento = null;
			}
		}
	}
	
	private void crearAsiento(String numero, CsvReader csvReader) throws IOException{
		Asiento asiento = Asiento.existeAsiento(numero);
		boolean crear = false;
		if (asiento == null){
			crear = true;
		}
		else if (asiento.getEstado().equals(Estado.Anulada) || asiento.getEstado().equals(Estado.Cancelada)){
			crear = true;
		}
		else if (asiento.getEstado().equals(Estado.Confirmada) || asiento.getEstado().equals(Estado.Procesando)){
			throw new ValidationException("Ya esta registrado el número de asiento " + numero);
		}
		else if (!asiento.getItems().isEmpty()){
			throw new ValidationException("Ya esta registrado el número de asiento " + numero);
		}
		
		if (crear){
			this.ultimoAsiento = new Asiento();
			this.ultimoAsiento.setNumero(numero);
			this.ultimoAsiento.setItems(new ArrayList<ItemAsiento>());
		}
		else{
			this.ultimoAsiento = asiento;
		}
		this.ultimoAsiento.setFecha(this.convertirStrFecha(csvReader.get(0)));
		this.ultimoAsiento.setDetalle(csvReader.get(2));
		this.ultimoAsiento.setObservaciones(csvReader.get(3));
		this.ultimoAsiento.setTipoAsiento((TipoAsiento)TipoAsiento.buscarPorCodigo(csvReader.get(4), TipoAsiento.class.getSimpleName()));				
		if (crear){
			XPersistence.getManager().persist(ultimoAsiento);
		}		
	}
	
	@Override
	protected int getCantidadColumnasObligatorias(){
		return 9;
	}
}
