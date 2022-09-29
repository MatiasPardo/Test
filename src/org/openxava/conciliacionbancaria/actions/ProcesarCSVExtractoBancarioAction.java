package org.openxava.conciliacionbancaria.actions;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;

import org.openxava.base.actions.ProcesarCSVGenericoAction;
import org.openxava.conciliacionbancaria.model.ConfiguracionExtractoBancario;
import org.openxava.conciliacionbancaria.model.ExtractoBancario;
import org.openxava.conciliacionbancaria.model.ResumenExtractoBancario;
import org.openxava.jpa.XPersistence;
import org.openxava.model.MapFacade;
import org.openxava.tesoreria.model.CuentaBancaria;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;

import com.csvreader.CsvReader;

public class ProcesarCSVExtractoBancarioAction extends ProcesarCSVGenericoAction{
	
	private ConfiguracionExtractoBancario configurador;
	
	private CuentaBancaria cuenta;
	
	private ResumenExtractoBancario resumen;
	
	private Integer numeroFila = 1;
	
	@Override
	protected void procesarLineaCSV(CsvReader csvReader) throws IOException {
		Date fecha = (Date)this.convertirStrFecha(this.obtenerCampo(csvReader, configurador.getColumnaFecha() - 1, "fecha"));
		this.resumen.asignarRangoFecha(fecha);
		
		ExtractoBancario itemResumen = this.crearExtractoBancario();
		
		itemResumen.setConcepto(csvReader.get(configurador.getColumnaConcepto() - 1));
		itemResumen.setFecha(fecha);
		
		if (configurador.tieneCreditoDebito()){
			String valorCredito = this.obtenerCampo(csvReader, configurador.getColumnaCredito() - 1, "Crédito");
			String valorDebito = this.obtenerCampo(csvReader, configurador.getColumnaDebito() - 1, "Débito");
			if (Is.emptyString(valorCredito) && Is.emptyString(valorDebito)){
				throw new ValidationException("Columnas Débitos y Créditos ambas vacías");
			}
			else if (Is.emptyString(valorCredito)){
				valorCredito = "0";
			}
			else if (Is.emptyString(valorDebito)){
				valorDebito = "0";
			}
			BigDecimal credito = this.convertirStrDecimal(valorCredito);
			BigDecimal debito = this.convertirStrDecimal(valorDebito);
			itemResumen.asignarCreditoDebito(credito, debito);
		}
		else{
			BigDecimal importe = this.convertirStrDecimal(this.obtenerCampo(csvReader, configurador.getColumnaDebito() - 1, "Crédito/Débito"));
			String tipoImporte = null;
			if (configurador.getColumnaTipoImporte() != null){
				tipoImporte = this.obtenerCampo(csvReader, configurador.getColumnaTipoImporte() - 1, "tipo importe");
			}
			importe = configurador.establecerSignoImporte(importe, tipoImporte);
			itemResumen.asignarImporte(importe);
		}		
		if (configurador.getColumnaObservaciones() != null){
			itemResumen.setObservaciones(this.obtenerCampo(csvReader, configurador.getColumnaObservaciones() - 1, "Observaciones"));
		}
		if (configurador.getColumnaSaldo() != null){
			itemResumen.setSaldo(this.convertirStrDecimal(this.obtenerCampo(csvReader, configurador.getColumnaSaldo() - 1, "Saldo")));
		}
		itemResumen.setNroFila(this.numeroFila);
		
		this.numeroFila++;
	}
	
	@Override
	protected Boolean commitParcial(){
		return Boolean.FALSE;
	}
	
	@Override
	protected void preProcesarCSV() throws Exception {
		this.cuenta = (CuentaBancaria)MapFacade.findEntity(this.getPreviousView().getModelName(), this.getPreviousView().getKeyValues());
		this.configurador = ConfiguracionExtractoBancario.buscar(this.cuenta);
		this.resumen = crearResumenExtractoBancario(cuenta);
		this.numeroFila = 1;
		this.setMascaraFecha(configurador.getMascaraFecha());		
	}

	@Override
	protected void posProcesarCSV() throws Exception {
		// Si no hay errores se confirma todo.
		// Con que una linea tenga error, no se graba nada
		if (this.getErrors().isEmpty()){
			XPersistence.getManager().persist(this.resumen);
			for(ExtractoBancario extracto: this.resumen.getExtracto()){
				XPersistence.getManager().persist(extracto);
			}
			this.commit();
		}
		else{
			this.rollback();
			this.addError("No se pudo importar correctamente el extracto bancario");
		}
				
		this.configurador = null;
		this.cuenta = null;
		this.resumen = null;
		this.getPreviousView().refreshCollections();
	}
	
	@Override
	protected int numeroFilaInicial() {
		return configurador.getFilaComienzo();
	}
	
	private ResumenExtractoBancario crearResumenExtractoBancario(CuentaBancaria cuenta){
		ResumenExtractoBancario res = new ResumenExtractoBancario();
		res.setCuenta(cuenta);
		res.setExtracto(new LinkedList<ExtractoBancario>());
		return res;
	}
	
	public ExtractoBancario crearExtractoBancario() {
		ExtractoBancario nuevo = new ExtractoBancario();
		nuevo.setResumen(this.resumen);
		this.resumen.getExtracto().add(nuevo);
		return nuevo;		
	}
}
