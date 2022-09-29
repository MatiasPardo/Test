package org.openxava.afip.model;

import java.math.*;
import java.text.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.*;

import org.openxava.afip.calculators.*;
import org.openxava.annotations.*;
import org.openxava.base.calculators.FechaFinMesCalculator;
import org.openxava.base.calculators.FechaInicioMesCalculator;
import org.openxava.base.model.*;
import org.openxava.fisco.model.TipoComprobante;
import org.openxava.impuestos.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.calculators.*;
import org.openxava.ventas.model.*;
import org.openxava.view.View;
import org.openxava.web.editors.*;

@org.openxava.annotations.View(members="fecha; empresa;")

public class InterfazRetPercSufridasAfip implements IParametrosReporte{
		
	private static final String TIPOCOMPROBANTESVENTAS = "'FacturaVenta', 'DebitoVenta', 'CreditoVenta'";
	
	private static final String TIPOCOMPROBANTESCOMPRAS = "'FacturaCompra', 'DebitoCompra', 'CreditoCompra'";
	
	public static final String RETENCIONESSUFRIDAS = "RetSufridas"; 
	
	private static final String TIPOOPERACION_RETENCION="1";
	private static final String TIPOOPERACION_PERCEPCION="2";
	
	private Date fecha = null;
	
	private String tipoInterfaz;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo")
	@NoCreate @NoModify
	private Empresa empresa = null;
	
	private Messages errores = null;
	
	private Messages mensajes = null;
	
	public Messages getErrores(){
		if (errores == null){
			errores = new Messages();
		}
		return errores;
	}
	
	public Messages getMensajes(){
		if (mensajes == null){
			mensajes = new Messages();
		}
		return mensajes;
	}
	
	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	
	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public void generarArchivos(List<String> fileIDs){
		if (this.getFecha() == null){
			throw new ValidationException("Fecha no asignada");
		}
		if (this.getEmpresa() == null){
			throw new ValidationException("Empresa no asignada");
		}		
		
		this.errores = new Messages();
		this.mensajes = new Messages();
		if (Is.equalAsString(this.getTipoInterfaz(), "RETENCIONESSUFRIDAS")){
			fileIDs.add(this.generarArchivoPercepcionesSufridas());
			fileIDs.add(this.generarArchivoRetencionesSufridas());
		}
		else if (Is.equalAsString(this.getTipoInterfaz(), "ARCIBA")){
			fileIDs.add(this.generarArchivoPercepcionesRetencionesRealizadasCABA());
		}
		else if (Is.equalAsString(this.getTipoInterfaz(), "PercepcionesARBA")){
			fileIDs.add(this.generarArchivoPercepcionesRealizadasARBA());
			if (this.getErrores().isEmpty() && !this.getMensajes().isEmpty()){
				fileIDs.add(this.grabarArchivoMensajes(this.formatearFileName("PercepcionesARBA_Mensajes"), this.getMensajes()));
			}
		}
		else if (Is.equalAsString(this.getTipoInterfaz(), "RetencionesARBA")){
			fileIDs.add(this.generarArchivoRetencionesRealizadasARBA());
			if (this.getErrores().isEmpty() && !this.getMensajes().isEmpty()){
				fileIDs.add(this.grabarArchivoMensajes(this.formatearFileName("RetencionesARBA_Mensajes"), this.getMensajes()));
			}
		}
		else{
			throw new ValidationException("parámetro tipo interfaz incorrecto");
		}
	}
			
	private String generarArchivoPercepcionesSufridas(){
		SimpleDateFormat formatoDate = new SimpleDateFormat("yyyyMMdd");
		String fileName = "PercepcionesSufridas" + formatoDate.format(this.getFecha());
	
		StringBuilder datos = new StringBuilder();
		
		// Percepciones sufridas en Factura de compra
		Query query = getQueryPercepcionesSufridas();
		List<?> result = query.getResultList();
		for(Object row: result){
			try{
				Object[] array = (Object[])row;
				
				String codigoJuridiccion = ((Integer)array[0]).toString();
				String cuit = this.formatearCuit((String)array[1], 13);
				String fecha = this.formatearFecha((Date)array[2]);
				String numeroComprobante = (String)array[3];
				numeroComprobante = this.validarNumeroComprobante(numeroComprobante);
				String tipoComprobante = obtenerTipoComprobante((String)array[4]);
				String letraComprobante = this.obtenerLetraComprobante((Integer)array[5]);
				String importe = this.formatearImporte((BigDecimal)array[6]);				
				// punto de venta principal 
				String numeroSucursal = String.format("%04d", obtenerNumeroPuntoVentaPropio());
				// En compras el nro de constancia siempre es cero
				String numeroConstancia = String.format("%016d", new Integer(0));
				
				datos.append(codigoJuridiccion);
				datos.append(cuit);
				datos.append(fecha);
				datos.append(numeroSucursal);
				datos.append(numeroConstancia);
				datos.append(tipoComprobante);
				datos.append(letraComprobante);
				datos.append(numeroComprobante);
				datos.append(importe);
				datos.append("\n");
			}
			catch(Exception e){
				if (e.getMessage() != null){
					this.getErrores().add(e.getMessage());
				}
				else{
					this.getErrores().add(e.toString());
				}
			}
		}
		return this.grabarArchivo(fileName, datos);		
	}
	
	private String generarArchivoRetencionesSufridas(){
		SimpleDateFormat formatoDate = new SimpleDateFormat("yyyyMMdd");
		String fileName = "RetencionesSufridas" + formatoDate.format(this.getFecha());
	
		StringBuilder datos = new StringBuilder();
				
		// Retenciones (Recibos)
		Query query = getQueryRetencionesSufridas();
		List<?> result = query.getResultList();
		for(Object row: result){
			try{
				Object[] array = (Object[])row;
				
				String codigoJuridiccion = ((Integer)array[0]).toString();
				String cuit = this.formatearCuit(this.getEmpresa().getCuit(), 13);
				String fecha = this.formatearFecha((Date)array[1]);
				String numeroRecibo = (String)array[2];
				String strNroConstancia = (String)array[3];
				Integer numConstancia = 0;
				if (!Is.emptyString(strNroConstancia)){
					try{
						numConstancia = Integer.decode(strNroConstancia);
					}
					catch(Exception e){
						throw new ValidationException("Número de retención incorrecto: " + strNroConstancia + " en recibo " + numeroRecibo);
					}
				}
				String numeroConstancia = String.format("%016d", numConstancia);
				
				String tipoComprobante = "R";
				String importe = this.formatearImporte((BigDecimal)array[4]);
				// punto de venta principal 
				String numeroSucursal = String.format("%04d", obtenerNumeroPuntoVentaPropio());
				// En compras el nro de constancia siempre es cero
				String numeroComprobante = String.format("%020d", new Integer(0));
				
				String letraComprobante = "A";
				String codigoPosicionIVA = (String)array[5];
				TipoComprobanteCalculator tipoComprobanteCalculator = new TipoComprobanteCalculator();
				tipoComprobanteCalculator.setCodigoPosicionIVA(codigoPosicionIVA);
				try{
					TipoComprobante tipoComp = (TipoComprobante)tipoComprobanteCalculator.calculate();
					letraComprobante = tipoComp.tipoComprobanteAfip().getLetra();
				}
				catch(Exception e){
				}
				
				datos.append(codigoJuridiccion);
				datos.append(cuit);
				datos.append(fecha);
				datos.append(numeroSucursal);
				datos.append(numeroConstancia);
				datos.append(tipoComprobante);
				datos.append(letraComprobante);
				datos.append(numeroComprobante);
				datos.append(importe);
				datos.append("\n");
			}
			catch(Exception e){
				if (e.getMessage() != null){
					this.getErrores().add(e.getMessage());
				}
				else{
					this.getErrores().add(e.toString());
				}
			}
		}
		
		return this.grabarArchivo(fileName, datos);		
	}
	
	private Query getQueryRetencionesSufridas(){
		StringBuffer sql = new StringBuffer();
		sql.append("select pcia.codigoJuridiccion codigoJuridiccion, ");  
		sql.append("i.fecha fecha, r.numero recibo, i.numero numeroConstancia, ");  
		sql.append("i.importe importe, cli.posicionIva_codigo ");  
		sql.append("from ItemReciboCobranzaRetencion i "); 
		sql.append("join impuesto imp on imp.id = i.impuesto_id "); 
		sql.append("join ReciboCobranza r on r.id = i.reciboCobranza_id and r.estado = 1 ");  
		sql.append("join Cliente cli on cli.id = r.cliente_id  ");		
		sql.append("join Domicilio d on d.id = cli.domicilio_id   "); 
		sql.append("join Provincia pcia on pcia.codigo = d.provincia_codigo ");
		sql.append("join PosicionAnteImpuesto pi on pi.codigo = cli.posicionIva_codigo ");
		sql.append("where i.importe > 0 and imp.grupo = 2 "); 
		sql.append("and r.empresa_id = :empresa ");   
		sql.append("and to_char(r.fecha, 'YYYYMM') = :fecha ");    
		sql.append("and pi.presentacionesImpositivas = :presentaciones ");
		sql.append("order by i.fecha asc");
		
		Query query = XPersistence.getManager().createNativeQuery(sql.toString());
		query.setParameter("empresa", this.getEmpresa().getId());
		SimpleDateFormat format = new SimpleDateFormat("YYYYMM");
		query.setParameter("fecha", format.format(this.getFecha()));
		query.setParameter("presentaciones", Boolean.TRUE);
		return query;
	}
	
	private Query getQueryPercepcionesSufridas(){
		// Son las percepciones de ingresos brutos que se hacen en las facturas de compras
		StringBuffer sql = new StringBuffer();
		sql.append("select pcia.codigoJuridiccion, prov.numerodocumento, c.fecha, c.numero, ");
		sql.append("c.dtype, c.tipo, p.importe "); 
		sql.append("from CompraElectronica_Impuestos p ");
		sql.append("join CompraElectronica c on c.id = p.compraelectronica_id and c.estado = 1 ");
		sql.append("join Impuesto i on i.id = p.impuesto_id ");
		sql.append("join Proveedor prov on prov.id = c.proveedor_id ");
		sql.append("join Domicilio d on d.id = prov.domicilio_id ");
		sql.append("join Provincia pcia on pcia.codigo = d.provincia_codigo ");
		sql.append("join PosicionAnteImpuesto pi on pi.codigo = prov.posicionIva_codigo ");
		sql.append("where to_char(c.fecha, 'YYYYMM') = :fecha ");
		sql.append("and pi.presentacionesImpositivas = :presentaciones ");
		sql.append("and i.grupo = 1 ");
		sql.append("and c.empresa_id = :empresa ");
		sql.append("and c.dtype in (" + TIPOCOMPROBANTESCOMPRAS + ") ");
		sql.append("order by c.fecha, c.fechacreacion");
		
		Query query = XPersistence.getManager().createNativeQuery(sql.toString());
		query.setParameter("empresa", this.getEmpresa().getId());
		SimpleDateFormat format = new SimpleDateFormat("YYYYMM");
		query.setParameter("fecha", format.format(this.getFecha()));
		query.setParameter("presentaciones", Boolean.TRUE);
		return query;		
	}
	
	private String generarArchivoPercepcionesRetencionesRealizadasCABA(){
		// especificacion: https://www.agip.gob.ar/filemanager/source/Agentes/DocTecnicoImpoOperacionesDise%C3%B1odeRegistro.pdf
		String fileName = this.formatearFileName("ARCIBA"); 
				
		Query query = getQueryPercepcionesRetencionesCABARealizadas();
		List<?> result = query.getResultList();
		StringBuilder datosArchivo = new StringBuilder();
		for(Object row: result){
			try{
				StringBuilder datos = new StringBuilder();
				Object[] array = (Object[])row;
											
				String tipoOperacion = ((Integer)array[0]).toString();
				String fechaImpuesto = this.formatearFecha((Date)array[1]);
				String fechaComprobante = this.formatearFecha((Date)array[2]);
				String numeroComprobante = (String)array[3];								
				numeroComprobante = this.validarNumeroComprobante16Digitos(numeroComprobante);
				String tipoComprobante = obtenerTipoComprobante((String)array[4]);
				String letraComprobante = this.obtenerLetraComprobante((Integer)array[5]);								
				BigDecimal montoComprobante = (BigDecimal)array[6];								
				BigDecimal importePercepcionRet = (BigDecimal)array[7];
				String numeroCertificado = (String)array[8];			
				TipoDocumento tipoDocumentoDelRetenido = TipoDocumento.values()[(Integer)array[9]];
				String numeroDocumentoDelRetenido = (String)array[10];
				String nroInscripcionIBDelRetenido = (String)array[11];
				Integer situacionIBDelRetenido = (Integer)array[12];
				if (Is.emptyString(nroInscripcionIBDelRetenido) || (situacionIBDelRetenido == null)){
					if (tipoOperacion.equals(TIPOOPERACION_PERCEPCION)){
						throw new ValidationException("El cliente " + numeroDocumentoDelRetenido + " debe tener asignado el nro y situacion frente a ingresos brutos");
					}
					else{
						throw new ValidationException("El proveedor " + numeroDocumentoDelRetenido + " debe tener asignado el nro y situacion frente a ingresos brutos");
					}
					
				}
				String posicionIVADelRetenido = (String)array[13];
				String razonSocialDelRetenido = (String)array[14];
				BigDecimal importeOtroConceptos = (BigDecimal)array[15];
				BigDecimal importeIVA = (BigDecimal)array[16];				
				BigDecimal alicuota = (BigDecimal)array[17];
				if (importePercepcionRet.compareTo(BigDecimal.ZERO) != 0){
					// tipo operacion: 
					datos.append(tipoOperacion); 
					// Código de norma: Kiero usa 029, tendría que configurarse
					datos.append("029");
					// fecha retencion/percepcion
					datos.append(fechaImpuesto);
					// tipo comprobante
					if (tipoOperacion.equals(TIPOOPERACION_RETENCION)){
						if (Is.equalAsString(tipoComprobante, "F")){
							datos.append("01"); // factura
						}
						else if (Is.equalAsString(tipoComprobante, "D")){
							datos.append("02");
						}
						else{
							datos.append("09"); // otros comprobantes
						}
					}
					else{
						if (Is.equalAsString(tipoComprobante, "F")){
							datos.append("01"); // factura
						}
						else{
							datos.append("09"); // otros comprobantes
						}
					}
					
					// letra comprobante
					datos.append(letraComprobante);		
					// numero comprobante
					datos.append(numeroComprobante);
					// fecha comprobante
					datos.append(fechaComprobante);
					// monto comprobante
					datos.append(this.formatearImporteDigitos(montoComprobante, 2, 13));
					// nro certificado propio
					if (tipoOperacion.equals(TIPOOPERACION_RETENCION)){
						datos.append(this.concatenarEspaciosBlancoDerecha(numeroCertificado, 16));
					}
					else{
						datos.append(this.concatenarEspaciosBlancoDerecha("", 16));
					}
					// tipo documento del retenido
					if (tipoDocumentoDelRetenido.equals(TipoDocumento.CUIT)){
						datos.append("3");
					}
					else if (tipoDocumentoDelRetenido.equals(TipoDocumento.CUIL)){
						datos.append("2");
					}
					else{
						throw new ValidationException("Tipo documento del retenido inválido " + tipoDocumentoDelRetenido.name());
					}
					// número del documento del retenido y número de inscripción de ingresos brutos
					datos.append(this.formatearCuit(numeroDocumentoDelRetenido, 11));
					// Situacion IB del retenido: 
					// 1:local / 2:convenio multilateral / 4:no inscripto / 5:reg.simplificado
					// Si tipo de documento = 3 -> 1, 2, 4, 5
					// Si tipo de documento = 2 -> 4
					if (tipoDocumentoDelRetenido.equals(TipoDocumento.CUIT)){						
						datos.append(SituacionIngresosBrutos.values()[situacionIBDelRetenido].getCodigoArciba().toString());
						datos.append(this.formatearNroInscIIBB(nroInscripcionIBDelRetenido));
					}
					else{
						datos.append("4");
						datos.append(this.concatenarCeros("0", 11));
					}
					// Posicion de iva del retenido: 1-RI / 3-Exento / 4-Monotributista
					if (posicionIVADelRetenido.equals("RI")){
						datos.append("1");
					}
					else if (posicionIVADelRetenido.equals("M")){
						datos.append("4");
					}
					else if (posicionIVADelRetenido.equals("E")){
						datos.append("3");
					}
					else{
						throw new ValidationException("Error en la situación frente al iva del sujeto retenido: " + posicionIVADelRetenido);
					}
					// Razon social del retenido
					datos.append(this.concatenarEspaciosBlancoDerecha(razonSocialDelRetenido, 30));
					// importes otros conceptos
					datos.append(this.formatearImporteDigitos(importeOtroConceptos, 2, 13));
					// iva
					datos.append(this.formatearImporteDigitos(importeIVA, 2, 13));
					// monto sujeto a retención/percepción
					BigDecimal montoSujetoARetPer = montoComprobante.subtract(importeIVA).subtract(importeOtroConceptos);
					datos.append(this.formatearImporteDigitos(montoSujetoARetPer, 2, 13));
					// alicuota 					
					datos.append(this.formatearImporteDigitos(alicuota, 2, 2));
					if (tipoOperacion.equals(TIPOOPERACION_RETENCION)){
						// Un pago puede imputar a más de una factura, con lo cual hay que calcular la retención para cada una de ellas
						BigDecimal importeRetencionCalculado = montoSujetoARetPer.divide(alicuota, 2, RoundingMode.HALF_EVEN);
						if (importeRetencionCalculado.compareTo(importePercepcionRet) < 0){
							throw new ValidationException("Error en importe de retención: " + importeRetencionCalculado.toString() + " en comprobante " + numeroComprobante);
						}
						importePercepcionRet = importeRetencionCalculado;
					}
					// retención/percepción practicada
					datos.append(this.formatearImporteDigitos(importePercepcionRet, 2, 13));
					// monto total retenido/practicado
					datos.append(this.formatearImporteDigitos(importePercepcionRet, 2, 13));					
					datos.append("\n");
					
					datosArchivo.append(datos);				
				}					
			}
			catch(Exception e){
				if (e.getMessage() != null){
					this.getErrores().add(e.getMessage());
				}
				else{
					this.getErrores().add(e.toString());
				}
			}
		}
		
		return this.grabarArchivo(fileName, datosArchivo);		
	}
	
	private Query getQueryPercepcionesRetencionesCABARealizadas(){
		StringBuffer sql = new StringBuffer();
		
		String campoPercepcion = null;
		String campoAlicuota = null;
		try{
			for(Integer i = 1; i <= Empresa.CANTIDADPERCEPCIONESVENTA; i++){
				DefinicionImpuesto definicionPercepcion = (DefinicionImpuesto)this.getEmpresa().getClass().getMethod("getPercepcion" + i.toString()).invoke(this.getEmpresa());
				if (Is.equal(definicionPercepcion, DefinicionImpuesto.PercepcionCABA)){
					campoPercepcion = "percepcion" + i.toString() + "1";
					campoAlicuota = "alicuota" + i.toString();
					break;
				}
			}
			if (campoPercepcion == null){
				throw new ValidationException("Error en empresa: no esta configurada la percepcion de CABA");
			}
		}
		catch(Exception e){
			throw new ValidationException(e.toString());
		}
		sql.append("select tipoOperacion, fechaImpuesto, fechaComprobante, numero, tipoComprobante, letra, total, impuesto, numeroCertificado, tipoDocumento, ");
		sql.append("cuit, nroInscripcionIIBB, situacionIIBB, posicionIva, razonSocial, otrosConceptos, iva, alicuota ");
		sql.append("from (");
		
		sql.append("select ").append(TIPOOPERACION_PERCEPCION).append(" as tipoOperacion, ");
		sql.append("v.fecha as fechaImpuesto, v.fecha as fechaComprobante, v.numero as numero, v.dtype as tipoComprobante, v.tipo as letra, v.total1 as total, v.").append(campoPercepcion).append(" as impuesto, ");
		sql.append("''\\:\\:character varying as numeroCertificado, v.tipoDocumento as tipoDocumento, v.cuit as cuit, c.numeroIIBB as nroInscripcionIIBB, c.situacionIIBB as situacionIIBB, v.posicioniva_codigo as posicionIva, v.razonsocial as razonsocial, "); 
		sql.append("v.total1 - v.iva1 - v.subtotal1 as otrosConceptos, v.iva1 as iva, v.").append(campoAlicuota).append(" as alicuota "); 
		sql.append("from VentaElectronica v ");
		sql.append("join Cliente c on c.id = v.cliente_id ");
		sql.append("where v.estado = 1 and to_char(v.fecha, 'YYYYMM') = :fecha ");
		sql.append("and v.empresa_id = :empresa ");
		sql.append("and v.").append(campoPercepcion).append(" > 0 ");
		sql.append("and v.dtype in (" + TIPOCOMPROBANTESVENTAS + ") ");
		
		sql.append("union all ");
		
		sql.append("select ").append(TIPOOPERACION_RETENCION).append(" as tipoOperacion, ");
		sql.append("p.fecha as fechaImpuesto, cc.fecha as fechaComprobante, cc.numero as numero, compra.dtype as tipoComprobante, compra.tipo as letra, compra.total1 as total, "); 
		sql.append("ret.retencionactual as impuesto, ret.numero as numeroCertificado, p.tipodocumento as tipoDocumento, compra.cuit as cuit, ");
		sql.append("prov.numeroIIBB as numeroIIBB, prov.situacionIIBB as situacionIIBB, p.posicioniva_codigo as posicionIva, compra.razonsocial as razonSocial, "); 
		sql.append("compra.total1 - compra.iva1 - compra.subtotal1 as otrosConceptos, compra.iva1 as iva, ret.alicuota ");  
		sql.append("from ItemPagoRetencion ret ");
		sql.append("join Impuesto imp on ret.impuesto_id = imp.id ");
		sql.append("join PagoProveedores p on p.id = ret.pago_id and p.estado = 1 ");
		sql.append("join Proveedor prov on prov.id = p.pagoA_id ");
		sql.append("join CuentaCorriente cc on cc.idpagoproveedores = p.id ");
		sql.append("join CompraElectronica compra on compra.id = cc.idTransaccion ");
		sql.append("where ret.retencionactual > 0 and imp.tipo = ").append(Integer.toString(DefinicionImpuesto.RetencionCABA.getIndice()));
		sql.append(" and p.empresa_id = :empresa and to_char(p.fecha, 'YYYYMM') = :fecha "); 
				
		sql.append(") t order by t.fechaImpuesto asc");
		
		Query query = XPersistence.getManager().createNativeQuery(sql.toString());
		query.setParameter("empresa", this.getEmpresa().getId());
		SimpleDateFormat format = new SimpleDateFormat("YYYYMM");
		query.setParameter("fecha", format.format(this.getFecha()));		
		return query;		
	}
	
	private String generarArchivoRetencionesRealizadasARBA(){
		// especificacion: http://www.arba.gov.ar/Archivos/Publicaciones/dise%C3%B1o_registro_ar_web.pdf
		// Usamos la 1.7 Retenciones (Excepto actividad 26, 6 de bancos y 17 de bancos y no Bancos)
		
		//String fileName = this.formatearFileName("RetencionesARBA"); 
		
		SimpleDateFormat formatDay = new SimpleDateFormat("yyyyMM");
		String fileName = "AR" + "-" + this.formatearCuit(this.getEmpresa().getCuit(), 11) + "-" + formatDay.format(this.getFecha()) + 
				this.numeroQuincena(this.getFecha()).toString() + "-" + "P6-" + "RetARBA"; 
				
		Query query = getQueryRetencionesARBARealizadas();
		List<?> result = query.getResultList();
		StringBuilder datosArchivo = new StringBuilder();
		Integer cantidadLineas = 0;
		BigDecimal total = BigDecimal.ZERO;
		for(Object row: result){
			try{
				StringBuilder datos = new StringBuilder();
				Object[] array = (Object[])row;
				
				String cuit = this.formatearCuitGuiones((String)array[0]);
				datos.append(cuit);
				
				String fechaRetencion = this.formatearFecha((Date)array[1]);
				datos.append(fechaRetencion);
				
				// Numero de sucursal: siempre 1 en Kiero
				datos.append("0001");
				
				// Numero de emision
				String numeroRetencion = (String)array[2];
				try{
					datos.append(String.format("%08d", Integer.parseInt(numeroRetencion)));
				}
				catch(Exception e){
					throw new ValidationException("Numero de retención inválido " + numeroRetencion);
				}
								
				BigDecimal importeRetencion = (BigDecimal)array[3];
				total = total.add(importeRetencion);
				datos.append(this.formatearImporteDigitos(importeRetencion, 2, 8));
				
				// tipo operacion A:Alta / B:Baja/ M:Modificacion
				datos.append("A");
				
				datos.append("\n");
				datosArchivo.append(datos);
				cantidadLineas++;
			}
			catch(Exception e){
				if (e.getMessage() != null){
					this.getErrores().add(e.getMessage());
				}
				else{
					this.getErrores().add(e.toString());
				}
			}			
		}
		if (cantidadLineas > 0){
			this.getMensajes().add("Retenciones: Total " + UtilERP.convertirString(total) + " / Cantidad Lineas " + cantidadLineas.toString() );
		}
			
		return this.grabarArchivo(fileName, datosArchivo);	
	}	
	
	private Query getQueryRetencionesARBARealizadas(){
		StringBuffer sql = new StringBuffer();
						
		sql.append("select prov.numeroDocumento as cuit, p.fecha as fechaRetencion, ret.numero as numeroEmision, ret.retencionactual as retencion ");
		sql.append("from ").append(Esquema.concatenarEsquema("ItemPagoRetencion ret "));
		sql.append("join ").append(Esquema.concatenarEsquema("Impuesto imp on ret.impuesto_id = imp.id "));
		sql.append("join ").append(Esquema.concatenarEsquema("PagoProveedores p on p.id = ret.pago_id and p.estado = 1 "));
		sql.append("join ").append(Esquema.concatenarEsquema("Proveedor prov on p.proveedor_id = prov.id "));
		sql.append("join ").append(Esquema.concatenarEsquema("PosicionAnteImpuesto pos on pos.codigo = prov.posicionIva_codigo "));
		sql.append("where ret.retencionactual > 0 and imp.tipo = ").append(Integer.toString(DefinicionImpuesto.RetencionBsAs.getIndice()));
		sql.append(" and p.empresa_id = :empresa and ");
		sql.append("to_char(p.fecha, 'YYYYMMDD') >= :desde and  to_char(p.fecha, 'YYYYMMDD') <= :hasta and ");
		sql.append("pos.presentacionesImpositivas = :presentaciones ");
		sql.append("order by p.fecha asc");
		
		Query query = XPersistence.getManager().createNativeQuery(sql.toString());
		query.setParameter("empresa", this.getEmpresa().getId());
		query.setParameter("presentaciones", true);
		
		// Las retenciones se informan por quincena. 
		// Depende el día que cae la quincena 
		Date desde;
		Date hasta;
		try{
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(this.getFecha());
			int dia = calendar.get(Calendar.DAY_OF_MONTH);						
			calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),	15);
			Date diaQuince = calendar.getTime();			
			if (dia <= 15){
				FechaInicioMesCalculator calculator = new FechaInicioMesCalculator();
				calculator.setFecha(this.getFecha());
				desde = (Date)calculator.calculate();
				hasta = diaQuince;
			}
			else{
				FechaFinMesCalculator calculator = new FechaFinMesCalculator();
				calculator.setFecha(this.getFecha());
				calendar.setTime(diaQuince);
				calendar.add(Calendar.DAY_OF_YEAR, 1);
				desde = calendar.getTime();
				hasta = (Date)calculator.calculate();
			}
		}
		catch(Exception e){
			throw new ValidationException(e.toString());
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		query.setParameter("desde", format.format(desde));
		query.setParameter("hasta", format.format(hasta));
				
		return query;		
	}
	
	private String generarArchivoPercepcionesRealizadasARBA(){
		// especificacion: http://www.arba.gov.ar/Archivos/Publicaciones/dise%C3%B1o_registro_ar_web.pdf
		// Usamos la 1.1 Percepciones (Excepto actividad 29, 7 quincenal y 17 de bancos)
		
		//String fileName = this.formatearFileName("PercepcionesARBA"); 
		SimpleDateFormat formatDay = new SimpleDateFormat("yyyyMM");
		String fileName = "AR" + "-" + this.formatearCuit(this.getEmpresa().getCuit(), 11) + "-" + formatDay.format(this.getFecha()) + 
				"0" + "-" + "P7-" + "PercARBA"; 
		
		
		Query query = getQueryPercepcionesARBARealizadas();
		List<?> result = query.getResultList();
		StringBuilder datosArchivo = new StringBuilder();
		Integer cantidadLineas = 0;
		BigDecimal total = BigDecimal.ZERO;
		for(Object row: result){
			try{
				StringBuilder datos = new StringBuilder();
				Object[] array = (Object[])row;
				
				String cuit = this.formatearCuitGuiones((String)array[0]);
				datos.append(cuit);
				
				String fechaPercepcion = this.formatearFecha((Date)array[1]);
				datos.append(fechaPercepcion);
				
				String tipoComprobante = this.obtenerTipoComprobante((String)array[2]);
				datos.append(tipoComprobante);
				
				String letraComprobante = this.obtenerLetraComprobante((Integer)array[3]);
				datos.append(letraComprobante);
				
				String numeroComprobante = (String)array[4];
				Integer[] puntoVentaYEmision = this.obtenerNumeroPuntoVentaYEmision(numeroComprobante);
				String numeroSucursal = String.format("%04d", puntoVentaYEmision[0]);
				String numeroEmision = String.format("%08d", puntoVentaYEmision[1]);
				datos.append(numeroSucursal);
				datos.append(numeroEmision);
				
				BigDecimal montoImponible = (BigDecimal)array[5];
				datos.append(this.formatearImporteDigitos(montoImponible, 2, 9));
				BigDecimal importePercepcion = (BigDecimal)array[6];
				total = total.add(importePercepcion);
				datos.append(this.formatearImporteDigitos(importePercepcion, 2, 8));
				// tipo operacion A:Alta / B:Baja/ M:Modificacion
				datos.append("A");
				
				datos.append("\n");
				datosArchivo.append(datos);
				cantidadLineas++;
			}
			catch(ValidationException e){
				this.getErrores().add(e.getErrors());
			}
			catch(Exception e){
				if (e.getMessage() != null){
					this.getErrores().add(e.getMessage());
				}
				else{
					this.getErrores().add(e.toString());
				}
			}			
		}
		if (cantidadLineas > 0){
			this.getMensajes().add("Percepciones: Total " + UtilERP.convertirString(total) + " / Cantidad Lineas " + cantidadLineas.toString());
		}
			
		return this.grabarArchivo(fileName, datosArchivo);
	}
	
	private Query getQueryPercepcionesARBARealizadas(){
		StringBuffer sql = new StringBuffer();
		
		String campoPercepcion = null;		
		try{
			for(Integer i = 1; i <= Empresa.CANTIDADPERCEPCIONESVENTA; i++){
				DefinicionImpuesto definicionPercepcion = (DefinicionImpuesto)this.getEmpresa().getClass().getMethod("getPercepcion" + i.toString()).invoke(this.getEmpresa());
				if (Is.equal(definicionPercepcion, DefinicionImpuesto.PercepcionBsAs)){
					campoPercepcion = "percepcion" + i.toString() + "1";					
					break;
				}
			}
			if (campoPercepcion == null){
				throw new ValidationException("Error en empresa: no esta configurada la percepcion de ARBA");
			}
		}
		catch(Exception e){
			throw new ValidationException(e.toString());
		}
			
		sql.append("select v.cuit, v.fecha as fechaImpuesto, v.dtype as tipoComprobante, v.tipo as letra, v.numero as numero, ");
		sql.append("v.subtotal1 * v.coeficiente as montoImponible, v.").append(campoPercepcion).append(" * v.coeficiente as percepcion ");
		sql.append("from ").append(Esquema.concatenarEsquema("VentaElectronica v "));
		sql.append("join ").append(Esquema.concatenarEsquema("Cliente c on c.id = v.cliente_id "));
		sql.append("join ").append(Esquema.concatenarEsquema("PosicionAnteImpuesto pos ")).append("on pos.codigo = v.posicionIva_codigo ");
		sql.append("where v.estado = 1 and to_char(v.fecha, 'YYYYMM') = :fecha ");
		sql.append("and v.empresa_id = :empresa ");
		sql.append("and v.").append(campoPercepcion).append(" > 0 ");
		sql.append("and v.dtype in (" + TIPOCOMPROBANTESVENTAS + ") ");
		sql.append("and pos.presentacionesImpositivas = :presentaciones " );
		sql.append("order by v.fecha asc ");
		
		Query query = XPersistence.getManager().createNativeQuery(sql.toString());
		query.setParameter("empresa", this.getEmpresa().getId());
		SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
		query.setParameter("fecha", format.format(this.getFecha()));
		query.setParameter("presentaciones", true);
		return query;		
	}
	
	private String grabarArchivo(String fileName, StringBuilder datos){
		IFilePersistor filePersistor = FilePersistorFactory.getInstance();
		AttachedFile file = new AttachedFile();
		file.setName(fileName);
		file.setLibraryId(Esquema.getEsquemaApp().getNombre());
		file.setData(datos.toString().getBytes());
		filePersistor.save(file);
		
		String separador = "_OX_";
		return file.getId() + separador + file.getName() + separador + file.getLibraryId();
	}
	
	private String grabarArchivoMensajes(String fileName, Messages mens) {
		StringBuilder datos = new StringBuilder();
		for(Object m: mens.getStrings()){
			datos.append(m.toString());
			datos.append("\n");
		}
		return this.grabarArchivo(fileName, datos);
	}
	
	private String formatearCuit(String cuit, int cantidadDigitos){
		String s = cuit;
		if (s.matches("[0-9]{2}-[0-9]{8}-[0-9]{1}")){
			s = s.replaceAll("-","");
		}
		else if (!s.matches("[0-9]{1,13}")){
			throw new ValidationException("El cuit " + cuit + " es inválido");
		}
		
		return concatenarCeros(s, cantidadDigitos);		
	}
	
	private String formatearCuitGuiones(String cuit){
		String s = cuit;
		if (!s.matches("[0-9]{2}-[0-9]{8}-[0-9]{1}")){
			if (s.matches("[0-9]{11}")){
				s = s.substring(0, 2) + "-"	+ s.substring(2, 10) + "-" + s.substring(10, 11);
			}
			else{
				throw new ValidationException("El cuit " + cuit + " es inválido");
			}
		}		
		return s;		
	}
	
	private String formatearNroInscIIBB(String nroInscIIBB){
		String numero = nroInscIIBB.replace("-", "");
		if (numero.length() > 11){
			numero = numero.substring(0, 11);
		}
		else{
			numero = this.concatenarCeros(numero, 11);
		}
		return numero;
	}
	
	private String formatearFecha(Date fecha){
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		return format.format(fecha);
	}
	
	private String validarNumeroComprobante(String numero){
		if (Is.emptyString(numero)){
			throw new ValidationException("Número no asignada");
		}
		else{
			if (!numero.matches("[0-9]+-[0-9]")){
				return concatenarCeros(numero.replaceAll("-", ""), 20);
			}
			else{
				throw new ValidationException("Error en el comprobante: " + numero + ": el formato del nro de comprobante debe ser XXXX-XXXXXXXX" );
			}
		}
	}
	
	private String validarNumeroComprobante16Digitos(String numero){
		if (Is.emptyString(numero)){
			throw new ValidationException("Número no asignada");
		}
		else{
			if (!numero.matches("[0-9]+-[0-9]")){
				String[] partes = numero.split("-");
				return this.concatenarCeros(partes[0], 4) + this.concatenarCeros(partes[1], 12);
			}
			else{
				throw new ValidationException("Error en el comprobante: " + numero + ": el formato del nro de comprobante debe ser XXXX-XXXXXXXX" );
			}
		}
	}
	
	private String obtenerTipoComprobante(String tipoComprobante){
		// F: factura
		// D: Debito
		// C: credito
		return tipoComprobante.substring(0, 1).toUpperCase();
	}
	
	private String obtenerLetraComprobante(Integer letraComprobante){
		TipoComprobante tipoComprobante = TipoComprobante.buscarPorId(letraComprobante);
		if (tipoComprobante != null){
			return tipoComprobante.tipoComprobanteAfip().getLetra();
		}
		else{
			throw new ValidationException("No se pudo determinar la letra del comprobante: " + letraComprobante);
		}		
	}
	
	private String formatearImporte(BigDecimal importe){
		DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
		simbolos.setDecimalSeparator(',');
		DecimalFormat format = new DecimalFormat("00000000.00", simbolos);
		return format.format(importe);
	}
		
	private String formatearImporteDigitos(BigDecimal importe, int cantidadDecimales, int cantidadEnteros){
		DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
		simbolos.setDecimalSeparator(',');
		StringBuffer mascara = new StringBuffer();
		int digitosEntero = cantidadEnteros;
		if (importe.compareTo(BigDecimal.ZERO) < 0) digitosEntero--;
		
		for(int i = 1; i <= digitosEntero; i++){
			mascara.append("0");
		}
		mascara.append(".");
		for(int i = 1; i <= cantidadDecimales; i++){
			mascara.append("0");
		}
		DecimalFormat format = new DecimalFormat(mascara.toString(), simbolos);
		return format.format(importe);
	}
	
	@Override
	public void asignarValoresIniciales(View view, View previousView, Map<?, ?>[] idsSeleccionados){
		Date fechaActual = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fechaActual);
		if ((calendar.get(Calendar.DAY_OF_MONTH)) > 15){
			// se vuelve al día 1, que sería la primera quincena
			calendar.set(Calendar.DAY_OF_MONTH, 1);
		}
		else{
			// Se vuelve a la segunda quincena, que sería el día 16 del mes anterior
			calendar.add(Calendar.MONTH, -1);
			calendar.set(Calendar.DAY_OF_MONTH, 16);
		}
		view.setValue("fecha", calendar.getTime());
		
		Empresa empresa = Empresa.buscarEmpresaPorNro(1);
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("id", empresa.getId());
		view.setValue("empresa", values);
	}
	
	private Map<String, Integer> numPtoVentaPorSucursal = null;
	
	private Map<String, Integer> getNumPtoVentaPorSucursal(){
		if (this.numPtoVentaPorSucursal == null){
			this.numPtoVentaPorSucursal = new HashMap<String, Integer>();
		}
		return this.numPtoVentaPorSucursal;
	}
	
	private Integer obtenerNumeroPuntoVentaPropio(){
		// no hay sucursales propias en kiero
		String id = "";
		if (this.getNumPtoVentaPorSucursal().containsKey("")){
			return this.getNumPtoVentaPorSucursal().get("");
		}
		else{			
			PuntoVentaDefaultCalculator calculator = new PuntoVentaDefaultCalculator();			
			try{
				PuntoVenta puntoVenta = (PuntoVenta)calculator.calculate();
				this.getNumPtoVentaPorSucursal().put(id, puntoVenta.getNumero());
				return puntoVenta.getNumero();
			}
			catch(Exception e){
				throw new ValidationException("No se encontró el punto de venta principal ");
			}
						
		}		
	}
		
	private Integer[] obtenerNumeroPuntoVentaYEmision(String numeroComprobante){
		Pattern patron = Pattern.compile("([0-9]+)-([0-9]+)");
		Matcher match = patron.matcher(numeroComprobante);
		if (match.matches()){
			Integer[] numeros = new Integer[2];
			String numeroPuntoVenta = match.group(1);
			numeros[0] = Integer.parseInt(numeroPuntoVenta);
			
			String numeroEmision = match.group(2);
			numeros[1] = Integer.parseInt(numeroEmision);
			
			return numeros;
		}
		else{
			throw new ValidationException("Error en el comprobante: " + numeroComprobante + ": el formato del nro de comprobante debe ser XXXX-XXXXXXXX" );
		}
	}
	
	private String concatenarCeros(String s, int cantidad){
		int ceros = cantidad - s.length();
		if (ceros > 0){
			StringBuffer sb = new StringBuffer();
			for(int i = 1; i <= ceros; i++){
				sb.append("0");
			}
			s = sb.toString() + s;
		}
		return s;
	}

	private String concatenarEspaciosBlancoDerecha(String s, int cantidad){
		int ceros = cantidad - s.length();
		String str = s;
		if (ceros > 0){
			StringBuffer sb = new StringBuffer();
			for(int i = 1; i <= ceros; i++){
				sb.append(" ");
			}
			str = s + sb.toString();
		}
		else if (ceros < 0){
			str = s.substring(0, cantidad);
		}
		return str;
	}
	
	public String getTipoInterfaz() {
		return tipoInterfaz;
	}

	public void setTipoInterfaz(String tipoInterfaz) {
		this.tipoInterfaz = tipoInterfaz;
	}
	
	private String formatearFileName(String fileName){
		SimpleDateFormat formatDay = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat formatHour = new SimpleDateFormat("_hh_mm_a");
		return fileName.concat(formatDay.format(this.getFecha())).concat(formatHour.format(new Date()));
	}
	
	private Integer numeroQuincena(Date fecha){
		Calendar c = Calendar.getInstance();
		c.setTime(fecha);
		Integer dia = c.get(Calendar.DAY_OF_MONTH);
		if (dia <= 15){
			return 1;
		}
		else{
			return 2;
		}
	}
}
