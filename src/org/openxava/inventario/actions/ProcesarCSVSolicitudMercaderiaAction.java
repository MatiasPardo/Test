package org.openxava.inventario.actions;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedList;

import javax.persistence.PersistenceException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openxava.base.actions.ProcesarCSVGenericoAction;
import org.openxava.base.model.Empresa;
import org.openxava.base.model.ObjetoEstatico;
import org.openxava.inventario.model.Deposito;
import org.openxava.inventario.model.ItemSolicitudMercaderia;
import org.openxava.inventario.model.SolicitudMercaderia;
import org.openxava.jpa.XPersistence;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.Producto;

import com.csvreader.CsvReader;

public class ProcesarCSVSolicitudMercaderiaAction extends ProcesarCSVGenericoAction{

	private SolicitudMercaderia ultimaSolicitud = null;
	
	@Override
	protected void preProcesarCSV() throws Exception {
		this.ultimaSolicitud = null;		
	}

	@Override
	protected void posProcesarCSV() throws Exception {
		this.grabarUltimaSolicitud();		
	}

	@Override
	protected void procesarLineaCSV(CsvReader csvReader) throws IOException {
		String empresa = csvReader.get(0);
		String depositoOrigen = csvReader.get(1);
		String depositoDestino = csvReader.get(3);		
		if (Is.emptyString(empresa)) throw new ValidationException("Empresa no asignada");
		if (Is.emptyString(depositoOrigen)) throw new ValidationException("Depósito origen no asignado");
		if (Is.emptyString(depositoDestino)) throw new ValidationException("Depósito destino no asignado");
		
		if (this.nuevaSolicitud(empresa, depositoOrigen, depositoDestino)){
			this.grabarUltimaSolicitud();
			// observaciones
			this.crearSolicitud(empresa, depositoOrigen, depositoDestino);
			this.ultimaSolicitud.setObservaciones(csvReader.get(5));
		}
	
		// codigo producto, nombre producto, cantidad
		String producto = csvReader.get(6);
		if (Is.emptyString(producto)) throw new ValidationException("Producto no asignado");
		if (Is.emptyString(csvReader.get(8))) throw new ValidationException("Cantidad no asignada");
		BigDecimal cantidad = this.convertirStrDecimal(csvReader.get(8)); 
				
		ItemSolicitudMercaderia item = new ItemSolicitudMercaderia();
		item.setSolicitud(this.ultimaSolicitud);
		item.setProducto((Producto)ObjetoEstatico.buscarPorCodigoError(producto, Producto.class.getSimpleName()));
		item.setCantidad(cantidad);
		
		item.recalcular();		
		XPersistence.getManager().persist(item);
		this.ultimaSolicitud.getItems().add(item);	
	}
	
	private void crearSolicitud(String empresa, String depositoOrigen, String depositoDestino) {
		this.ultimaSolicitud = new SolicitudMercaderia();
		this.ultimaSolicitud.setEmpresa((Empresa)ObjetoEstatico.buscarPorCodigoError(empresa, Empresa.class.getSimpleName()));
		this.ultimaSolicitud.setOrigen((Deposito)ObjetoEstatico.buscarPorCodigoError(depositoOrigen, Deposito.class.getSimpleName()));
		this.ultimaSolicitud.setSolicitaA((Deposito)ObjetoEstatico.buscarPorCodigoError(depositoDestino, Deposito.class.getSimpleName()));
		this.ultimaSolicitud.setItems(new LinkedList<ItemSolicitudMercaderia>());
		XPersistence.getManager().persist(this.ultimaSolicitud);
	}

	private void grabarUltimaSolicitud() {
		if (this.ultimaSolicitud != null){
			try{
				this.ultimaSolicitud.grabarTransaccion();
				this.commit();
			}
			catch(PersistenceException e){
				this.rollback();							
				
				String error = ExceptionUtils.getRootCauseMessage(e);
				if (Is.emptyString(error)){
					error = e.getMessage();
				}
				addError("No se pudo grabar la solicitud de origen " + this.ultimaSolicitud.getOrigen().toString()  + ": " + error);				
			}
			catch(Exception e){
				this.rollback();
				
				addError("No se pudo grabar la solicitud de origen " + this.ultimaSolicitud.getOrigen().toString()  + ": " + e.getMessage());				
			}
			finally{
				this.ultimaSolicitud = null;
			}
		}
	}

	private boolean nuevaSolicitud(String empresa, String depositoOrigen, String depositoDestino) {
		if (this.ultimaSolicitud == null){
			return true;
		}
		else if (this.ultimaSolicitud.getEmpresa().getCodigo().equals(empresa) &&
				this.ultimaSolicitud.getOrigen().getCodigo().equals(depositoOrigen) &&
				this.ultimaSolicitud.getSolicitaA().getCodigo().equals(depositoDestino)){
			return false;
		}
		else{
			return true;
		}
	}

	@Override
	protected Boolean commitParcial(){
		return Boolean.FALSE;
	}
	
	@Override
	protected int getCantidadColumnasObligatorias(){
		return 9;
	}

}
