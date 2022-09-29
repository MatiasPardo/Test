package org.openxava.afip.model;

import java.io.*;
import java.math.*;
import java.text.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.base.model.*;
import org.openxava.contabilidad.model.EjercicioContable;
import org.openxava.contabilidad.model.PeriodoContable;
import org.openxava.hibernate.XHibernate;
import org.openxava.jpa.*;
import org.openxava.negocio.model.Pais;
import org.openxava.negocio.model.TipoDocumento;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

import com.allin.interfacesafip.model.*;
import com.csvreader.*;

public class FacturaElectronicaAfip {

	public void SolicitarCAE(VentaElectronica venta, Class<?> tipoVenta)throws Exception{		
		this.autorizarComprobante(venta);
		if (this.erroresAutorizacion().isEmpty()){
			if (venta.tieneAccionesPosCommitAutorizaciones()){
				String id = venta.getId();
				
				XPersistence.commit();
				XHibernate.commit();
				VentaElectronica ventaCAE = (VentaElectronica) XPersistence.getManager().find(tipoVenta, id);
				ventaCAE.ejecutarAccionesPosCommitAutorizaciones();
			}
		}
		else{
			throw new ValidationException(this.erroresAutorizacion());
		}		
	}
	
	private String errores = new String("");
	
	public String erroresAutorizacion() {
		return errores;
	}
	
	private void validarAntesAutorizar(VentaElectronica comprobante){
		if (comprobante.getItems().isEmpty()){
			throw new ValidationException("sin_items");
		}
		
		PeriodoContable periodo = EjercicioContable.buscarPeriodo(comprobante.ContabilidadFecha());
		if (periodo == null){
			throw new ValidationException("Falta definir el periodo contable para " + UtilERP.convertirString(comprobante.ContabilidadFecha()));
		}
		else if (!periodo.permiteAsientos()){
			throw new ValidationException("No se puede grabar asientos en el periodo " + periodo.getNombre());
		}
		
		// NOTA: Si algún cliente solicita que se pueda hacer una factura en $0 (la afip lo permite) se tiene que modificar la generación de la contabilidad:
		// Los asientos no graban movimientos en $0. Para el caso de una factura, debería permitir generar el asiento en $0 y modificar esta validación a "< 0"
		if (comprobante.getTotal().compareTo(BigDecimal.ZERO) < 0){
			throw new ValidationException("El total no puede ser menor cero");
		}
		
		TipoComprobanteArg tipoComprobanteAfip = comprobante.getTipo().tipoComprobanteAfip();
		if (tipoComprobanteAfip.getRegimenFacturaCredito()){
			if (comprobante.CtaCteTipo().equals("DEBITO")){
				throw new ValidationException("Tipo de operación no permitido para FCE");
			}
			else if (comprobante.CtaCteTipo().equals("CREDITO")){
				if (!comprobante.revierteTransaccion()){
					throw new ValidationException("Tipo de operación no permitido para FCE: el crédito debe estar asociado a una factura");
				}
			}
			else if (comprobante.getFechaVencimiento().compareTo(comprobante.getFecha()) <= 0){
				throw new ValidationException("La fecha de vencimiento debe ser posterior a la fecha del comprobante");
			}
		}
	}
	
	// este metodo debe ser privado y usar el metodo SolicitarCAE
	public void autorizarComprobante(VentaElectronica comprobante) throws Exception{
		validarAntesAutorizar(comprobante);
		
		if (comprobante.debeAutorizaAfip()){		
			NumeradorPuntoVenta numerador = null;
			VentaElectronica transaccion = comprobante;
			if (transaccion.getEstado().equals(Estado.Borrador) || transaccion.getEstado().equals(Estado.Abierta)){			
				// se reserva un numero y se marca la transacción como procesando
				numerador = this.buscarNumerador(comprobante);
				if (!numerador.getReservado()){
					numerador.setReservadoPor(comprobante.getId());
					comprobante.setNumeroInterno(numerador.getProximoNumero());
					comprobante.setNumero(numerador.formatearNumero(comprobante.getNumeroInterno()));
					comprobante.cambiarEstadoAProcesando();
					XPersistence.commit();
				}
				else{
					throw new ValidationException("No se puede obtener el CAE. Primero autorice el comprobante de estado PROCESANDO.");
				}			
				// Se vuelve a instanciar los objetos para ser nuevamente modificados.
				transaccion = (VentaElectronica) XPersistence.getManager().find(comprobante.getClass(), comprobante.getId());
				numerador = (NumeradorPuntoVenta)XPersistence.getManager().find(numerador.getClass(), numerador.getId());
			}
			else if (comprobante.getEstado().equals(Estado.Procesando)){
				numerador = this.buscarNumerador(comprobante);
			}
			else{
				throw new ValidationException("No se puede autorizar un comprobante en estado " + comprobante.getEstado().toString());
			}
			
			this.autorizarAfip(transaccion, numerador);
		}
		else{
			// Los comprobantes que no son de afip se confirman directamente
			comprobante.confirmarTransaccion();
		}
		XPersistence.commit();
		XHibernate.commit();
	}
	
	private NumeradorPuntoVenta buscarNumerador(VentaElectronica comprobante){
		// se busca el numerador con la clausula for update para bloquear
		String sql = "select n.id from " + Esquema.concatenarEsquema("NumeradorPuntoVenta") + " n where " + 
				"tipoComprobante = :tipoComprobante and " +
				"puntoVenta_id = :puntoVenta " +
				"for update";
		Query query = XPersistence.getManager().createNativeQuery(sql);
		query.setParameter("tipoComprobante", comprobante.AfipTipoComprobante());
		query.setParameter("puntoVenta", comprobante.getPuntoVenta().getId());
		try{
			String id = (String)query.getSingleResult();
			return (NumeradorPuntoVenta)XPersistence.getManager().find(NumeradorPuntoVenta.class, id);
		}
		catch(Exception e){
			throw new ValidationException("No se pudo obtener el numerador en el punto de venta para el tipo de comprobante " + comprobante.AfipTipoComprobante().toString() + ": Error " + e.toString());
		}
	}
	
	
	/*private String formatearCUIT(String numeroDocumento){
		return numeroDocumento.replaceAll("-", "");
	}*/
		
	private void autorizarAfip(VentaElectronica comprobante, NumeradorPuntoVenta numerador) throws Exception{
		if (comprobante.getPuntoVenta().getTipo().solicitarCae()){
			this.runServerFE();
			String filename = numerador.getArchivoPendiente();
			if (Is.emptyString(filename)){
				filename = this.escribirCSV(comprobante);
			}							
			this.leerCSV(comprobante, numerador, filename);
		}
		else{
			comprobante.confirmarTransaccion();
		}
		
		
		if (comprobante.getEstado().equals(Estado.Borrador) || comprobante.getEstado().equals(Estado.Abierta)){
			// se libera el numero porque la afip rechazo el comprobante, por algún error de datos en la factura
			numerador.setReservadoPor("");
		}
		else if (comprobante.getEstado().equals(Estado.Confirmada)){
			// Se confirmo la transacción, se pasa al siguiente número 
			numerador.setReservadoPor("");
			numerador.setProximoNumero(numerador.getProximoNumero() + 1);
		}
		else if (comprobante.getEstado().equals(Estado.Procesando)){
			if (this.errores.isEmpty()){
				this.errores = "No se pudo establecer comunicación con la Afip. \n" + 
							 "Intente autorizar el comprobante nuevamente";
			}
		}
	}
	
	public void runServerFE(){
		Runtime aplicacion = Runtime.getRuntime();
		try{
			String[] parametros = new String[2];
			parametros[0] = getPathFacturaElectronica();
			aplicacion.exec(getPathFacturaElectronica().concat("\\FacturaElectronicaAFIP.exe " + parametros[0]));	
		}
		catch(Exception ex){
			throw new ValidationException("Error a ejecutar el servidor de Factura electrónica: " + ex.getMessage());
		}
	}
	
	public String getPathFacturaElectronica(){
		return ConfiguracionERP.pathConfig().concat("FacturaElectronica");
	}
	
	private void leerCSV(VentaElectronica comprobante, NumeradorPuntoVenta numerador, String filename) throws Exception{
		String fullFileName = this.getPathFacturaElectronica() + "\\FEResultadosCAE\\" + filename + "CAE.csv"; 
		Thread.sleep(2000);
		int intentos = 1;
		boolean archivoLeido = false;
		while (intentos < 5){
			// se intenta leer la respuesta por 10 segundos
			try{
				FileReader fileReader = new FileReader(fullFileName);
				CsvReader reader = new CsvReader(fileReader, ';');
				try{
					while (reader.readRecord()){
						String estado = reader.get(1);
						if ((estado.equals("0")) || (estado.equals("1"))){
							comprobante.setCae(reader.get(2));
							SimpleDateFormat formatoDate = new SimpleDateFormat("yyyyMMdd");							
							comprobante.setFechaVencimientoCAE(formatoDate.parse(reader.get(3)));
							comprobante.confirmarTransaccion();
						}
						else{
							this.errores = reader.get(4);
							if (estado.equals("3")){
								// hubo un error de conexión
							}
							else{
								// Se rechazó el comprobante. 
								// Se vuelve al estado anterior para que pueda ser modificada
								comprobante.volverEstadoAnteriorProcesando();
								comprobante.setNumero("");
								comprobante.setNumeroInterno(null);
							}	
						}
						break;
					}
					archivoLeido = true;
					break;
				}
				finally{
					reader.close();
				}
			}
			catch(FileNotFoundException ex){
				// se espera 1 segundo, antes de volver a verificar si hay respuesta 
				Thread.sleep(1000);
				intentos++;
			}
			
		}
		
		if (!archivoLeido){
			numerador.setArchivoPendiente(filename);
		}
		else{
			numerador.setArchivoPendiente(null);
		}
	}
	
	private String escribirCSV(VentaElectronica comprobante) throws Exception{
		if (comprobante.getPuntoVenta().getTipo().exportacion()){
			return this.escribirCSV_wsfexpo(comprobante);
		}
		else{
			return this.escribirCSV_wsfe(comprobante);
		}
	}
	
	private String escribirCSV_wsfe(VentaElectronica comprobante) throws Exception{
		// Se escribe el archivo CSV para que el servidor lo envie a al afip
		Date date = new Date();
		String fechaActual = new SimpleDateFormat("yyyyMMddHHmmss").format(date); 
		String filename = comprobante.getId() + "_" + fechaActual;
		String fullFileName = this.getPathFacturaElectronica() + "\\FEPendientes\\" + filename + ".csv";
		
		FileWriter fileWriter = new FileWriter(fullFileName, false);
		CsvWriter writer = new CsvWriter(fileWriter, ';');		
		AfipPadronPersonas padronPersonas = new AfipPadronPersonas();
		TipoComprobanteArg tipoComprobanteAfip = comprobante.getTipo().tipoComprobanteAfip();
		try{
			
			DecimalFormat formatoDecimal = new DecimalFormat("#.##");
			SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyyMMdd");
			// CABECERA
			writer.write(comprobante.getId());
			writer.write(comprobante.getTipoDocumento().getCodigoAfip().toString());
			writer.write(padronPersonas.formatearCuit((comprobante.getCuit())));
			writer.write(comprobante.AfipTipoComprobante().toString());
			writer.write(comprobante.getPuntoVenta().getNumero().toString());
			writer.write(comprobante.getNumeroInterno().toString());
			// producto 1, servicios 2, productos y servicios 3
			writer.write("1");
			// Importe total: Importe neto no grabado + importe neto grabado + importe exento + importe IVA + importe tributos
			
			writer.write(formatoDecimal.format(comprobante.getTotal1().setScale(2, RoundingMode.HALF_EVEN)));
			// Importe neto no grabado 
			writer.write("0");
			// importe neto grabado
			writer.write(formatoDecimal.format(comprobante.getSubtotal1().setScale(2, RoundingMode.HALF_EVEN)));
			// importe exento
			writer.write("0");
			// IVA
			writer.write(formatoDecimal.format(comprobante.getIva1().setScale(2, RoundingMode.HALF_EVEN)));
			// Otros impuestos
			BigDecimal otrosImpuestos = BigDecimal.ZERO;
			Collection<ImpuestoAfip> percepciones = new LinkedList<ImpuestoAfip>();
			for(int i=1; i <= Empresa.CANTIDADPERCEPCIONESVENTA; i++){
				try {
					BigDecimal importePercepcion = (BigDecimal)comprobante.getClass().getMethod("getPercepcion" + Integer.toString(i) + "1").invoke(comprobante);
					otrosImpuestos = otrosImpuestos.add(importePercepcion);
					ImpuestoAfip percepcion = new ImpuestoAfip();
					percepcion.setImporte(importePercepcion);
					percepcion.setAlicuota((BigDecimal)comprobante.getClass().getMethod("getAlicuota" + Integer.toString(i)).invoke(comprobante));
					percepcion.setBaseImponible(comprobante.getSubtotal1());
					percepciones.add(percepcion);
				}
				catch (Exception e) {
					throw new ValidationException("Error al obtener percepciones: " + e.toString());
				}
			}
			// se le suman los impuestos internos
			otrosImpuestos = otrosImpuestos.add(comprobante.getImpuestosInternos1());
			writer.write(formatoDecimal.format(otrosImpuestos.setScale(2, RoundingMode.HALF_EVEN)));
			
			// moneda y cotizacion
			writer.write("PES");
			writer.write("1");
			String fechaEmision = formatoFecha.format(comprobante.getFecha()); 
			writer.write(fechaEmision);
			// Obligatorio si el concepto es 2 o 3: 
			//		Fecha Servicio desde 
			//		Fecha Servicio hasta
			writer.write("");
			writer.write("");
			//	Obligatorios si el concepto es 2 o 3 o si es factura credito
			//		Fecha vencimiento
			String fechaVencimientoStr = "";
			if (tipoComprobanteAfip.getRegimenFacturaCredito()){
				if (!comprobante.revierteTransaccion()){
					// Cuando es un crédito no se informa la fecha de vencimiento
					fechaVencimientoStr = formatoFecha.format(comprobante.getFechaVencimiento());
				}
			}
			writer.write(fechaVencimientoStr);
			writer.endRecord();
					
			// DETALLE de IVA y de IMPUESTOS INTERNOS: SOLO COMPROBANTES DIFERENTES a Consumidor final
			if (!tipoComprobanteAfip.getConsumidorFinal()){
				Map<BigDecimal, ImpuestoAfip> mapImpuestosInternos = new HashMap<BigDecimal, ImpuestoAfip>();
				if (comprobante.getTotal1().compareTo(BigDecimal.ZERO) != 0){
					// Si el total es cero, no se detalle el IVA.
					
					writer.write("IVA"); writer.endRecord();
					Map<BigDecimal, ImpuestoAfip> mapIva = new HashMap<BigDecimal, ImpuestoAfip>();				
					for (ItemVentaElectronica itemVenta: comprobante.getItems()){
						BigDecimal importeIva = itemVenta.getIva1();
						if (mapIva.containsKey(itemVenta.getTasaiva())){
							ImpuestoAfip iva = (ImpuestoAfip)mapIva.get(itemVenta.getTasaiva());
							iva.setImporte(iva.getImporte().add(importeIva));
							iva.setBaseImponible(iva.getBaseImponible().add(itemVenta.getSubtotal1()));
						}
						else{
							ImpuestoAfip iva = new ImpuestoAfip();
							iva.setImporte(importeIva);
							iva.setBaseImponible(itemVenta.getSubtotal1());
							iva.setAlicuota(itemVenta.getTasaiva());
							mapIva.put(itemVenta.getTasaiva(), iva);
						}
						
						if (itemVenta.getImpuestoInterno1().compareTo(BigDecimal.ZERO) != 0){
							if (mapImpuestosInternos.containsKey(itemVenta.getTasaImpuestoInterno())){
								ImpuestoAfip impInterno = (ImpuestoAfip)mapImpuestosInternos.get(itemVenta.getTasaImpuestoInterno());
								impInterno.setImporte(impInterno.getImporte().add(itemVenta.getImpuestoInterno1()));
								impInterno.setBaseImponible(impInterno.getBaseImponible().add(itemVenta.getSubtotal1()));
							}
							else{
								ImpuestoAfip impInterno = new ImpuestoAfip();
								impInterno.setImporte(itemVenta.getImpuestoInterno1());
								impInterno.setBaseImponible(itemVenta.getSubtotal1());
								impInterno.setAlicuota(itemVenta.getTasaImpuestoInterno());
								mapImpuestosInternos.put(itemVenta.getTasaImpuestoInterno(), impInterno);
							}
						}
					}
					Iterator<Map.Entry<BigDecimal, ImpuestoAfip>> it = mapIva.entrySet().iterator();
					while(it.hasNext()){
						Map.Entry<BigDecimal, ImpuestoAfip> entry = it.next();
						ImpuestoAfip iva = entry.getValue();
						String codigoAfip = "";
						if (iva.getAlicuota().compareTo(new BigDecimal(10.5)) == 0){
							codigoAfip = "4";
						}
						else if (iva.getAlicuota().compareTo(new BigDecimal(21)) == 0){
							codigoAfip = "5";
						}
						else if (iva.getAlicuota().compareTo(new BigDecimal(27)) == 0){
							codigoAfip = "6";
						}
						else if (iva.getAlicuota().compareTo(BigDecimal.ZERO) == 0){
							codigoAfip = "3";
						}
						else{
							throw new ValidationException("Error al obtener código afip para el IVA");
						}
						writer.write(codigoAfip);
						writer.write(formatoDecimal.format(iva.getBaseImponible()));
						writer.write(formatoDecimal.format(iva.getImporte().setScale(2, RoundingMode.HALF_EVEN)));
						writer.endRecord();
					}
					writer.write("FIN IVA"); writer.endRecord();
				}	
			
			
				// DETALLE DE OTROS TRIBUTOS
				writer.write("TRIBUTOS"); writer.endRecord();
				// Percepciones
				for(ImpuestoAfip percepcion: percepciones){
					// las percepciones son impuestos provinciales
					writer.write("2");
					writer.write("Percepción");
					writer.write(formatoDecimal.format(percepcion.getBaseImponible()));
					writer.write(formatoDecimal.format(percepcion.getAlicuota()));
					writer.write(formatoDecimal.format(percepcion.getImporte().setScale(2, RoundingMode.HALF_EVEN)));
					writer.endRecord();
				}			
				// Impuestos internos
				for(ImpuestoAfip internos: mapImpuestosInternos.values()){
					writer.write("4");
					writer.write("Impuesto Interno");
					writer.write(formatoDecimal.format(internos.getBaseImponible()));
					writer.write(formatoDecimal.format(internos.getAlicuota()));
					writer.write(formatoDecimal.format(internos.getImporte().setScale(2, RoundingMode.HALF_EVEN)));
					writer.endRecord();
				}
				writer.write("FIN TRIBUTOS"); writer.endRecord();
			}
			
			if (tipoComprobanteAfip.getRegimenFacturaCredito()){
				// Si es Factura CREDITO se deben agregar datos opciones
				ConfiguracionAfipEmpresa configAfip = ConfiguracionAfip.getConfigurador().empresaHabilitadaFCE(comprobante.getEmpresa());
				if (!comprobante.revierteTransaccion()){
					// Es una factura/debito: en este caso se tiene que informar:
					// - CBU Emisor
					// - ALIAS EMISOR					
					if (configAfip == null){
						throw new ValidationException("La empresa " + comprobante.getEmpresa().toString() + " ");
					}
					String cbuEmisor = configAfip.getCbu();
					String aliasEmisor = configAfip.getAlias();
					writer.write("OPCIONALES"); writer.endRecord();
					
					writer.write("2101");
					writer.write(cbuEmisor);
					writer.endRecord();
					
					writer.write("2102");
					writer.write(aliasEmisor);
					writer.endRecord();
					
					// Tipo de gestión de los valores
					// ADC = "Agente de deposito Colectivo"
					// SCA = "Transferencia sistema circulación abierta"
					writer.write("27");
					writer.write(configAfip.getTipoSistemaFCE().getCodigoAfip());
					writer.endRecord();
					
					writer.write("FIN OPCIONALES"); writer.endRecord();
				}
				else{
					// Es un crédito de una factura: en este caso se tiene que informar:
					
					// Opcional: "N" si es anulada antes de ser enviada al cliente o  "S" (esAnulacion) si el cliente la rechazó.
					String rechazadaPorCliente = "S";
					if (configAfip != null){
						rechazadaPorCliente = configAfip.getCreditoFCE().getTipoAfip();
					}
					
					writer.write("OPCIONALES"); writer.endRecord();
					
					writer.write("22");
					writer.write(rechazadaPorCliente);
					writer.endRecord();
										
					writer.write("FIN OPCIONALES"); writer.endRecord();
					
					// Comprobante asociado
					writer.write("COMPROBANTES ASOCIADOS"); writer.endRecord();
					VentaElectronica facturaAnulada = comprobante.getRevierte();					
					writer.write(facturaAnulada.AfipTipoComprobante().toString());
					writer.write(facturaAnulada.getPuntoVenta().getNumero().toString());
					writer.write(facturaAnulada.getNumeroInterno().toString());
					writer.write(padronPersonas.formatearCuit(facturaAnulada.getEmpresa().getCuit()));
					writer.write(formatoFecha.format(facturaAnulada.getFecha()));
					writer.endRecord();				
					writer.write("FIN COMPROBANTES ASOCIADOS"); writer.endRecord();
				}
			}
			else{
				if (comprobante.revierteTransaccion() && !comprobante.getTipoDocumento().equals(TipoDocumento.DNI)){
					// informamos que se revierte a una factura (solo si el tipo de comprobante es cuit/cuil)
					// Comprobante asociado
					writer.write("COMPROBANTES ASOCIADOS"); writer.endRecord();
					VentaElectronica facturaAnulada = comprobante.getRevierte();					
					writer.write(facturaAnulada.AfipTipoComprobante().toString());
					writer.write(facturaAnulada.getPuntoVenta().getNumero().toString());
					writer.write(facturaAnulada.getNumeroInterno().toString());
					writer.write(padronPersonas.formatearCuit(facturaAnulada.getEmpresa().getCuit()));
					writer.write(formatoFecha.format(facturaAnulada.getFecha()));
					writer.endRecord();				
					writer.write("FIN COMPROBANTES ASOCIADOS"); writer.endRecord();
				}
				else if (!comprobante.esFactura()){
					// Los creditos y debitos, no 
					writer.write("PERIODOS ASOCIADOS"); writer.endRecord();
					writer.write(fechaEmision);
					writer.write(fechaEmision);
					writer.endRecord();
					writer.write("FIN PERIODOS ASOCIADOS"); writer.endRecord();
				}
			}
			return filename;
		}
		finally{
			writer.flush();
			writer.close();
		}
	}
	
	private String escribirCSV_wsfexpo(VentaElectronica comprobante) throws Exception{
		Pais pais = comprobante.getCiudad().getProvincia().getPais();
		if (pais == null){
			throw new ValidationException("País no asignado en el domicilio");
		}
		else if (pais.getCodigoAfip() <= 0){
			throw new ValidationException("País sin código de afip " + pais.toString());
		}
		if (Is.emptyString(comprobante.getMoneda().getCodigoAfip())){
			throw new ValidationException("Falta asignar el código de afip en la moneda " + comprobante.getMoneda().getNombre());
		}
		ClienteFacturaExportacion datosFacturaExportacion = ClienteFacturaExportacion.buscar(comprobante.getCliente());
		if (datosFacturaExportacion == null){
			throw new ValidationException("Falta asignar los datos de facturas de exportación para el cliente " + comprobante.getCliente().toString());
		}
		
		// Se escribe el archivo CSV para que el servidor lo envie a al afip
		Date date = new Date();
		String fechaActual = new SimpleDateFormat("yyyyMMddHHmmss").format(date); 
		String filename = "exportacion_" + comprobante.getId() + "_" + fechaActual;
		String fullFileName = this.getPathFacturaElectronica() + "\\FEPendientes\\" + filename + ".csv";
				
		StringWriter stringWriter = new StringWriter();
		CsvWriter writer = new CsvWriter(stringWriter, ';');		
		AfipPadronPersonas padronPersonas = new AfipPadronPersonas();
				
		boolean multimoneda = true;		
		try{
			
			DecimalFormat formatoDecimal = new DecimalFormat("#.##");
			SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyyMMdd");
			// CABECERA
			
			// ID de solitud: usamos la fecha y hora, porque cada solicitud debe ser única
			writer.write(fechaActual);
			writer.write(comprobante.AfipTipoComprobante().toString());
			writer.write(comprobante.getPuntoVenta().getNumero().toString());
			writer.write(comprobante.getNumeroInterno().toString());
			writer.write(formatoFecha.format(comprobante.getFecha()));
			// Tipo de exportacion: 1 (productos), 2 (servicios), 4 (otros)
			Integer tipoExportacion = 4;
			writer.write(tipoExportacion.toString());
			// Permisos existentes: S o N
			if (comprobante.AfipTipoComprobante() == 19){
				// Para los tipos de exportacion 2 y 4 siempre va vacío.
				// Como siempre mandamos 4, entonces va vacío.
				writer.write("");
			}
			else{
				// para creditos y débitos vacío
				writer.write("");
			}
			// pais destino
			writer.write(Integer.toString(pais.getCodigoAfip()));
			// nombre de cliente
			writer.write(comprobante.getRazonSocial());
			
			String cuitPais = "";
			String idImpositivo = "";
			if (pais.getCodigoAfip() == 200){
				// argentina
				idImpositivo = padronPersonas.formatearCuit((comprobante.getCuit())); 
			}
			else{
				cuitPais = padronPersonas.formatearCuit((comprobante.getCuit()));
			}
			writer.write(cuitPais);
			writer.write(comprobante.getDireccion());
			writer.write(idImpositivo);
			
			if (multimoneda && (!comprobante.getMoneda().equals(comprobante.getMoneda1()))){
				writer.write(comprobante.getMoneda().getCodigoAfip());
				writer.write(formatoDecimal.format(comprobante.getCotizacion()));
			}
			else{
				writer.write("PES");
				writer.write("1");
			}
			
			// Observaciones comerciales
			writer.write("");
			// Total
			BigDecimal totalComprobante = comprobante.getTotal1();
			if (multimoneda) totalComprobante = comprobante.getTotal();
			
			totalComprobante = totalComprobante.setScale(2, RoundingMode.HALF_EVEN);
			writer.write(formatoDecimal.format(totalComprobante));
			
			// forma de pago
			writer.write("");
			// Obs
			writer.write("");
			
			// Incoterms
			writer.write(datosFacturaExportacion.getIncoterms().name());
			// Incoterms descripcion
			writer.write("");
			writer.write(Integer.toString(datosFacturaExportacion.getIdioma().getCodigo()));
			// fecha de pago
			if (comprobante.AfipTipoComprobante() == 19 &&
					(tipoExportacion == 2 || tipoExportacion == 4)){
				// Para las facturas, si el tipo es 2 (servicio) o 4 (otros) se debe informar la fecha de pago
				writer.write(formatoFecha.format(comprobante.getFechaVencimiento()));
			}
			else{
				writer.write("");
			}
			writer.endRecord();
					
			// DETALLE  
			writer.write("ITEMS"); writer.endRecord();
			
			BigDecimal sumaItems = BigDecimal.ZERO;
			for(ItemVentaElectronica item: comprobante.getItems()){
				writer.write(item.getProducto().getCodigo());
				writer.write(item.getProducto().getNombre());
				// cantidad
				writer.write("");
				// unidad medida
				writer.write("");
				// precio
				writer.write("");
				// bonificacion
				writer.write("");
				// precio total
				BigDecimal subtotalItem = item.getSubtotal1();
				if (multimoneda) subtotalItem = item.getSubtotal();
				
				// se redondea para abajo, así el ajuste por redondeo es siempre positivo
				subtotalItem = subtotalItem.setScale(2, RoundingMode.DOWN);
				writer.write(formatoDecimal.format(subtotalItem));
				writer.endRecord();
				
				sumaItems = sumaItems.add(subtotalItem);
			}
			
			if (sumaItems.compareTo(totalComprobante) < 0){
				// Ajuste por redondeo
				BigDecimal difRedondeo = totalComprobante.subtract(sumaItems);
				
				writer.write("REDONDEO");
				writer.write("AJUSTE POR REDONDEO");
				// cantidad
				writer.write("");
				// unidad medida
				writer.write("");
				// precio
				writer.write("");
				// bonificacion
				writer.write("");
				// precio total
				writer.write(formatoDecimal.format(difRedondeo));
				writer.endRecord();
			}
			
			writer.write("FIN ITEMS"); writer.endRecord();
			
			FileWriter fileWriter = new FileWriter(fullFileName, false);
			fileWriter.write(stringWriter.toString());
			fileWriter.flush();
			fileWriter.close();
			return filename;
		}
		finally{
			writer.flush();
			writer.close();
		}
	}
}
