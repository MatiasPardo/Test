package org.openxava.contabilidad.model;

import java.math.*;
import java.text.*;
import java.util.*;

import javax.persistence.*;

import org.apache.commons.lang3.StringUtils;
import org.openxava.afip.model.*;
import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.view.View;
import org.openxava.web.editors.*;

import com.allin.interfacesafip.model.*;

@org.openxava.annotations.View(members="fecha;empresa;secuencia")

public class RegimenInformacionCompraVenta implements IParametrosReporte{
	
	private static final String TIPOCOMPROBANTESVENTAS = "'FacturaVenta', 'DebitoVenta', 'CreditoVenta', 'FacturaVentaContado'";
	
	private static final String TIPOCOMPROBANTESCOMPRAS = "'FacturaCompra', 'DebitoCompra', 'CreditoCompra'";
	
	private static final String ALICUOTA_IVA_CREDITOFISCAL = "21";
	
	private Date fecha;

	@ManyToOne(fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo")
	@NoCreate @NoModify
	private Empresa empresa;

	private Integer secuencia = 0;

	private boolean excluirPtoVentaManualesVentas = false;
	
	@Hidden
	private Date fechaEjecucion = new Date();
	
	@Hidden
	private Boolean hayMovimientosVentas = false;
	
	@Hidden
	private Boolean hayMovimientosCompras = false;
	
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

	public Integer getSecuencia() {
		return secuencia;
	}

	public void setSecuencia(Integer secuencia) {
		this.secuencia = secuencia;
	}

	public Date getFechaEjecucion() {
		return fechaEjecucion;
	}

	public void setFechaEjecucion(Date fechaEjecucion) {
		this.fechaEjecucion = fechaEjecucion;
	}
	
	@Override
	public void asignarValoresIniciales(View view, View previousView, Map<?, ?>[] idsSeleccionados) {
		
		view.setValue("fecha", new Date());
		
		Empresa empresa = Empresa.buscarEmpresaPorNro(1);
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("id", empresa.getId());
		view.setValue("empresa", values);
		
		view.setValue("secuencia", 0);
	}
	
	public void generarArchivos(List<String> fileIDs){
		if (this.getFecha() == null){
			throw new ValidationException("Fecha no asignada");
		}
		if (this.getEmpresa() == null){
			throw new ValidationException("Empresa no asignada");
		}
		if (this.getSecuencia() == null){
			throw new ValidationException("Secuencia no asignada");
		}
		this.hayMovimientosVentas = false;
		this.hayMovimientosCompras = false;
		ConfiguracionAfip configurador = ConfiguracionAfip.getConfigurador();
		this.excluirPtoVentaManualesVentas = configurador.getExcluirPuntoVentaManuales();
		this.setFechaEjecucion(new Date());
		
		fileIDs.add(this.generarVentasComprobantes());
		fileIDs.add(this.generarVentasAlicuotas());
		fileIDs.add(this.generarComprasComprobantes());
		fileIDs.add(this.generarComprasAlicuotas());
		fileIDs.add(this.generarComprasAlicuotasDespachos());

		fileIDs.add(this.generarRegistroCabecera());
	}
	
	private String generarVentasComprobantes() {
		String fileName = "REGINFO_CV_VENTAS_CBTE";
		fileName = this.formatearFileName(fileName);
	
		StringBuilder datos = new StringBuilder();
		Query query = getQueryComprobantesVentas();
		List<?> rows = query.getResultList();
		SimpleDateFormat formatoDate = new SimpleDateFormat("yyyyMMdd");
		AfipPadronPersonas afip = new AfipPadronPersonas();
		for (Object row: rows){
			this.hayMovimientosVentas = true;
			
			Object[] r =(Object[]) row;
			// fecha de comprobante
			Date fechaComprobante = (Date)r[1];
			datos.append(formatoDate.format(fechaComprobante));
			// tipo de comprobante
			Integer tipoComprobante = (Integer)r[0];
			String tipoTransaccion = (String)r[15];
			
			Integer tipoComprobanteAfip = TipoComprobanteArg.codigoAfipPorIndice(tipoComprobante, tipoTransaccion);
		    datos.append(String.format("%03d", tipoComprobanteAfip));
			// punto de venta
		    datos.append(String.format("%05d", (Integer)r[7]));
		    // nro de comprobante desde y hasta
		    datos.append(String.format("%020d", (BigInteger)r[3]));
		    datos.append(String.format("%020d", (BigInteger)r[3]));
		    // tipo de documento
		    datos.append(TipoDocumento.codigoAfipPorIndice((Integer)r[6]));
			// numero de documento
		    datos.append(String.format("%020d", new BigInteger(afip.formatearCuit((String)r[5]))));
		    // razon social
		    String razonSocial = (String)r[4];
		    int len = razonSocial.length();
		    if (len > 30){
		    	razonSocial = razonSocial.substring(0, 30);
		    }
		    else{
		    	for (int i=1; i <= (30 - len); i++){
		    		razonSocial += " ";
		    	}
		    	 
		    }
		    datos.append(razonSocial);
		    // total
		    BigDecimal total = (BigDecimal)r[11];
		    datos.append(formatearImporte(total));
		    // Importe total de conceptos que no integran el precio neto gravado		    
		    datos.append(formatearImporte(BigDecimal.ZERO));
		    // Percepcion no categorizados
		    datos.append(formatearImporte(BigDecimal.ZERO));
		    // Importe exento
		    datos.append(formatearImporte(BigDecimal.ZERO));
		    // percepciones de impuestos nacionales
		    datos.append(formatearImporte(BigDecimal.ZERO));
		    // percepciones de ingresos brutos
		    datos.append(formatearImporte((BigDecimal)r[8]));
		    // percepciones de impuestos municipales
		    datos.append(formatearImporte(BigDecimal.ZERO));
		    // impuestos internos
		    datos.append(formatearImporte(BigDecimal.ZERO));
		    // Moneda
		    datos.append("PES");
		    datos.append("0001000000");
		    // cantidad de alicuotas de IVA
		    datos.append(r[12].toString());
		    // codigo de operacion: 0:ninguno / N: no grabado
		    BigDecimal importeIva0 = (BigDecimal)r[14];
		    if (importeIva0.compareTo(BigDecimal.ZERO) != 0){
		    	datos.append("N");
		    }
		    else if (total.compareTo(BigDecimal.ZERO) == 0){
		    	// cuando es cero la factura, la alicuota de iva es cero, entonces debe informar un tipo de operación
		    	datos.append("N");
		    }
		    else{
		    	datos.append("0");
		    }
		    // otros tributos
		    datos.append(formatearImporte(BigDecimal.ZERO));
		    // Fecha de vencimiento de pago
		    datos.append("00000000");
		    
		    datos.append(this.finLinea());
		}
		return grabarArchivo(fileName, datos);
	}
	
	private Query getQueryComprobantesVentas(){
		String sql = "select v.tipo tipo, v.fecha fecha, v.numero numeroStr, v.numerointerno numero, " +  
				"v.razonsocial nombreCliente, v.cuit documentoCliente, v.tipoDocumento tipoDocCliente, p.numero puntoVenta, " +  
				"(v.percepcion11 + v.percepcion21) as percepciones, v.iva1 iva, v.subtotal1 subtotal, v.total1 total, " +
				"coalesce(alicuotas.cantidadAlicuotas, 0) as cantidadAlicuotas, " +
				"coalesce(importes.grabado, 0) as importeGrabado, coalesce(importes.exento, 0) as importeExento, " +			
				"v.dtype as tipoTransaccion " + 
				"from " + Esquema.concatenarEsquema("VentaElectronica") + " v " +
				"left outer join " + Esquema.concatenarEsquema("cliente") + " c on c.id = v.cliente_id " + 
				"left outer join " + Esquema.concatenarEsquema("puntoventa") + " p on p.id = v.puntoventa_id " +
				"left join " +
				"( " +
				"  select count(tasasIva.venta_id) cantidadAlicuotas, tasasIva.venta_id as venta_id from " +
				"  (select i.tasaiva, i.venta_id as venta_id " +
				"  from " + Esquema.concatenarEsquema("ItemVentaElectronica") + " i " + 
				"  join " + Esquema.concatenarEsquema("VentaElectronica") + " v on v.id = i.venta_id and v.estado = 1 " +
				"  group by i.tasaiva, i.venta_id " +
				"  ) tasasIva " + 
				"  group by tasasiva.venta_id " +
				") alicuotas on alicuotas.venta_id = v.id " +
				"left join " +
				"( " +
				"  select coalesce(sum( case when i.tasaiva <> 0 then i.subtotal1 else 0 end ), 0) as grabado, " +
				"         coalesce(sum( case when i.tasaiva = 0 then i.subtotal1 else 0 end ), 0) as exento, i.venta_id as venta_id " +      
				"  from " + Esquema.concatenarEsquema("itemventaelectronica") + " i " +
				"  join " + Esquema.concatenarEsquema("ventaelectronica") + " v on v.id = i.venta_id and v.estado = 1 " +
				"  group by venta_id " +
	 			") importes on importes.venta_id = v.id " + 	
				
				"where v.estado = 1 " + 
				"and to_char(v.fecha, 'YYYYMM') = :fecha " + 
				"and v.empresa_id = :empresa " +
				"and v.dtype in (" + TIPOCOMPROBANTESVENTAS + ") ";
		
		if (this.excluirPtoVentaManualesVentas){
			sql += " and p.tipo != 1 ";
		}
		sql += "order by v.fecha, v.numero asc";
		
		Query query = XPersistence.getManager().createNativeQuery(sql);
		query.setParameter("empresa", this.getEmpresa().getId());
		SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
		query.setParameter("fecha", format.format(this.getFecha()));
		
		return query;
	}
	
	private String generarVentasAlicuotas() {
		String fileName = "REGINFO_CV_VENTAS_ALICUOTAS";
		fileName = this.formatearFileName(fileName);
		StringBuilder datos = new StringBuilder();

		Query query = getQueryAlicuotasComprobantesVentas();
		List<?> rows = query.getResultList();
		for (Object row: rows){
			Object[] r =(Object[]) row;
			
			// tipo de comprobante
			Integer tipoComprobante = (Integer)r[0];
			String tipoTransaccion = (String)r[7];
			Integer tipoComprobanteAfip = TipoComprobanteArg.codigoAfipPorIndice(tipoComprobante, tipoTransaccion);
			datos.append(String.format("%03d", tipoComprobanteAfip));
			// punto de venta
		    datos.append(String.format("%05d", (Integer)r[2]));
		    // nro de comprobante 
		    datos.append(String.format("%020d", (BigInteger)r[1]));
		    // importe neto grabado
		    BigDecimal importeNetoGrabado = (BigDecimal)r[6];
		    datos.append(formatearImporte(importeNetoGrabado));
		    // alicuota según tabla de afip
		    BigDecimal tasaIva = (BigDecimal)r[5];
		    try {
				datos.append(String.format("%04d", AfipAlicuotaIVA.codigoAfipPorTasa(tasaIva)));
			} catch (Exception e) {
				throw new ValidationException(e.toString());
			}	    
		    // impuesto liquidado
		    BigDecimal iva = BigDecimal.ZERO;
		    if (tasaIva.compareTo(BigDecimal.ZERO) != 0){
		    	//iva = tasaIva.multiply(importeNetoGrabado, new MathContext(2, RoundingMode.HALF_UP));
		    	iva = tasaIva.multiply(importeNetoGrabado);
		    	iva = iva.divide(new BigDecimal(100));
		    	iva = iva.setScale(2, RoundingMode.HALF_UP);
		    }
		    datos.append(formatearImporte(iva));
		    
		    datos.append(this.finLinea());
		}
		
		return grabarArchivo(fileName, datos);
	}
	
	private Query getQueryAlicuotasComprobantesVentas(){
		String sql = "select v.tipo tipo, v.numerointerno numero, p.numero puntoVenta, v.numero numerostr, v.fecha, " +  
				"case when i.iva = 0 then 0 else i.tasaiva end as alicuota, sum(i.subtotal1) importenetogravado, v.dtype as tipoTransaccion " +  
				"from " + Esquema.concatenarEsquema("ItemVentaElectronica") + " i " +  
				"join " + Esquema.concatenarEsquema("VentaElectronica") + " v on v.id = i.venta_id " +
				"left outer join " + Esquema.concatenarEsquema("PuntoVenta") + " p on p.id = v.puntoventa_id " +
				"where v.estado = 1 " + 
				"and to_char(v.fecha, 'YYYYMM') = :fecha " + 
				"and v.empresa_id = :empresa " +
				"and v.dtype in (" + TIPOCOMPROBANTESVENTAS + ") ";
		
		if (this.excluirPtoVentaManualesVentas){
			sql += " and p.tipo != 1 ";
		}
		sql += "group by v.tipo, v.numerointerno, p.numero, v.numero, v.fecha, v.dtype, " +
						"case when i.iva = 0 then 0 else i.tasaiva end " +  
				"order by v.fecha, v.numero asc";
		
		Query query = XPersistence.getManager().createNativeQuery(sql);
		query.setParameter("empresa", this.getEmpresa().getId());
		SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
		query.setParameter("fecha", format.format(this.getFecha()));
		return query;
	}
	
	private String generarComprasComprobantes() {
		String fileName = "REGINFO_CV_COMPRAS_CBTE";
		fileName = this.formatearFileName(fileName);
		StringBuilder datos = new StringBuilder();
		
		Query query = getQueryComprobantesCompras();
		List<?> rows = query.getResultList();
		SimpleDateFormat formatoDate = new SimpleDateFormat("yyyyMMdd");
		AfipPadronPersonas afip = new AfipPadronPersonas();
		for (Object row: rows){
			this.hayMovimientosCompras = true;		
				
			Object[] r =(Object[]) row;
			
			String despacho = (String)r[16];
			boolean comprobanteDespachoImportacion = false;
			if (!Is.emptyString(despacho)){
				comprobanteDespachoImportacion = true;
			}
			
			// fecha de comprobante
			Date fechaComprobante = (Date)r[0];
			datos.append(formatoDate.format(fechaComprobante));
			
			Integer tipoComprobante = (Integer)r[1];
			String tipoTransaccion = (String)r[2];
			// tipo de comprobante
			if (!comprobanteDespachoImportacion){				
				Integer tipoComprobanteAfip = TipoComprobanteArg.codigoAfipPorIndice(tipoComprobante, tipoTransaccion);
			    datos.append(String.format("%03d", tipoComprobanteAfip));
			    
			    // punto de venta
			    String numeroCompleto = (String)r[3];
			    Integer puntoVenta = obtenerPuntoVenta(numeroCompleto);
			    if (puntoVenta >= 0){
			    	datos.append(String.format("%05d", puntoVenta));
			    }
			    else{
			    	datos.append("PPPPP");
			    }
			    
			    // nro de comprobante
			    Integer numeroComprobante = obtenerNumeroComprobante(numeroCompleto);
			    if (numeroComprobante >= 0){
			    	datos.append(String.format("%020d", numeroComprobante));
			    }
			    else{
			    	datos.append("XXXXXXXXXXXXXXXXXXXX");
			    }
			}
			else{
				// tipo de comprobante de despacho de importacion
				datos.append("066");
				
				// punto venta y numero de comprobante: 0
				datos.append(StringUtils.repeat("0", 5));
				datos.append(StringUtils.repeat("0", 20));
			}
			
		    
		    
		    // despacho de importacion: 
		    if (despacho == null) despacho = "";
		    datos.append(despacho);
		    for(int i = 1; i <= (16 - despacho.length()); i++){
		    	datos.append(" ");
		    }
		    
		    // tipo de documento
		    datos.append(TipoDocumento.codigoAfipPorIndice((Integer)r[6]));
			// numero de documento
		    datos.append(String.format("%020d", new BigInteger(afip.formatearCuit((String)r[5]))));
		    // razon social
		    String razonSocial = (String)r[4];
		    int len = razonSocial.length();
		    if (len > 30){
		    	razonSocial = razonSocial.substring(0, 30);
		    }
		    else{
		    	for (int i=1; i <= (30 - len); i++){
		    		razonSocial += " ";
		    	}
		    	 
		    }
		    datos.append(razonSocial);
		    
		    // total
		    BigDecimal total = (BigDecimal)r[9];
		    datos.append(formatearImporte(total));
		    
		    BigDecimal creditoFiscal = BigDecimal.ZERO;
		    
		    BigDecimal iva = (BigDecimal)r[7];
		    creditoFiscal = creditoFiscal.add(iva);
		    
		    BigDecimal importeGravado = (BigDecimal)r[11];
		    BigDecimal importeNoGravado = (BigDecimal)r[12];
		    BigDecimal iibb = (BigDecimal)r[13];
		    BigDecimal percepcionesIVA = (BigDecimal)r[17];
		    BigDecimal impuestosMunicipales = (BigDecimal)r[18];
		    BigDecimal impuestosNacionales = (BigDecimal)r[19];
		    BigDecimal impuestosInternos = BigDecimal.ZERO;
		    
		    BigDecimal importeTotalConceptosNoGravado = BigDecimal.ZERO; 
		    if (comprobanteDespachoImportacion){
		    	// siempre en cero, salvo para los despacho de importacion, que se calcula según el total y los demás importes.
		    	// En general da negativo
		    	creditoFiscal = creditoFiscal.add((BigDecimal)r[20]);
		    	BigDecimal importeGravadoCreditoFiscal = ((BigDecimal)r[21]).setScale(2, RoundingMode.HALF_EVEN);
		    	
		    	importeTotalConceptosNoGravado = total.subtract(creditoFiscal).subtract(importeNoGravado).subtract(importeGravado).
		    			subtract(iibb).subtract(percepcionesIVA).
		    			subtract(impuestosNacionales).subtract(impuestosMunicipales).subtract(impuestosInternos).
		    			subtract(importeGravadoCreditoFiscal);
		    }
		    // Importe total de conceptos que no integran el precio neto gravado
	  		datos.append(formatearImporte(importeTotalConceptosNoGravado));		    	
	    	// Importe exento		    
	    	datos.append(formatearImporte(BigDecimal.ZERO));    	
		    		    
		    // Importe de percepciones o pagos a cuenta del impuesto de valor agregado		    
		    datos.append(formatearImporte(percepcionesIVA));		    
		    // Importe de percepciones o pagos a cuenta de otros impuestos nacionales		    
		    datos.append(formatearImporte(impuestosNacionales));
		    		    
		    // percepciones de ingresos brutos
		    datos.append(formatearImporte(iibb));
		    		    
		    // percepciones de impuestos municipales		    
		    datos.append(formatearImporte(impuestosMunicipales));
		    
		    // impuestos internos
		    datos.append(formatearImporte(impuestosInternos));
		    
		    // Moneda
		    String moneda = (String)r[15];
		    datos.append(moneda);
		    if (Is.emptyString(moneda)){
		    	throw new ValidationException("Hay una moneda que no tiene definido el código de afip");
		    }
		    else{
			    if (moneda.equals("PES")){
			    	datos.append("0001000000");
			    }
			    else{
			    	BigDecimal cotizacion = (BigDecimal)r[14];
			    	datos.append(formatearCotizacion(cotizacion));
			    }
		    }
		    
		    String cantidadAlicuotasIva = r[10].toString();
		    if (TipoComprobanteArg.A.ordinal() != tipoComprobante){		    
		    	// Los comprobantes B o C no llevan alicuota de IVA
		    	cantidadAlicuotasIva = "0"; 		    	
		    }
		    // cantidad de alicuotas de IVA		    
		    datos.append(cantidadAlicuotasIva);
		    // codigo de operacion: 0 - ninguno / N - no gravado /		    
		    if (importeNoGravado.compareTo(BigDecimal.ZERO) != 0){
		    	datos.append("N"); // no gravado
		    }
		    else{
		    	datos.append("0");
		    }
		    // credito fiscal computable		    
		    datos.append(formatearImporte(creditoFiscal));
		    // otros tributos
		    datos.append(formatearImporte(BigDecimal.ZERO));
		    
		    // cuit emisor/corredor
		    datos.append("00000000000");
		    // Denominación del emisor/corredor
		    for(int i=1; i <= 30; i++){
		    	datos.append(" ");
		    }
		    // iva comision
		    datos.append(formatearImporte(BigDecimal.ZERO));
		    
		    datos.append(this.finLinea());		    
		}
		return grabarArchivo(fileName, datos);		
	}

	private Query getQueryComprobantesCompras(){
		StringBuffer sql = new StringBuffer();
		sql.append("select c.fecha, c.tipo as letraComprobante, c.dtype as tipoTransaccion, c.numero, "); 
		sql.append("p.nombre, p.numeroDocumento, p.tipodocumento, c.iva1, c.subtotal1, c.total1, "); 
		sql.append("coalesce(alicuotas.cantidadAlicuotas, 0) as cantidadAlicuotas, ");
		sql.append("coalesce(tipoImportes.importeGravado, 0) as importeGravado, "); 
		sql.append("coalesce(tipoImportes.importeNoGravado, 0) as importeNoGravado, "); 
		sql.append("coalesce(impuestosCompras.percepcionesIIBB, 0) as percepcionesIIBB, "); 
		sql.append("c.cotizacion, m.codigoAfip, case when provDespacho.proveedoresDespacho_id is null then '' else c.numero end as despacho, "); 
		sql.append("coalesce(impuestosCompras.percepcionesIVA, 0) as percepcionesIVA, "); 
		sql.append("coalesce(impuestosCompras.impuestosMunicipales, 0) as impuestosMunicipales , "); 
		sql.append("coalesce(impuestosCompras.impuestosNacionales, 0) as impuestosNacionales, "); 
		sql.append("coalesce(impuestosCompras.creditoFiscal, 0) as creditoFiscal, ");
		sql.append("coalesce(impuestosCompras.importeGravadoCreditoFiscal, 0) as importeGravadoCreditoFiscal ");
		sql.append("from ").append(Esquema.concatenarEsquema("CompraElectronica c ")); 
		sql.append("join ").append(Esquema.concatenarEsquema("Proveedor p on p.id = c.proveedor_id ")); 
		sql.append("join ").append(Esquema.concatenarEsquema("PosicionAnteImpuesto pi on pi.codigo = p.posicionIva_codigo ")); 
		sql.append("join ").append(Esquema.concatenarEsquema("Moneda m on m.id = c.moneda_id ")); 
		sql.append("left join ").append(Esquema.concatenarEsquema("ConfiguracionAfip_Proveedor provDespacho on p.id = provDespacho.proveedoresDespacho_id ")); 
		sql.append("left join "); 
			sql.append("( ");
			sql.append("select i.compra_id as compra_id, "); 
			sql.append("coalesce(sum(case when i.tasaiva <> 0 then i.suma1 else 0 end), 0) as importeGravado, ");
			sql.append("coalesce(sum(case when i.tasaiva = 0 then i.suma1 else 0 end), 0) as importeNoGravado "); 
			sql.append("from ").append(Esquema.concatenarEsquema("ItemCompraElectronica i "));
			sql.append("group by i.compra_id ");
			sql.append(") tipoImportes on tipoImportes.compra_id = c.id ");
		sql.append("left join ");
			sql.append("( ");  
			sql.append("select i.compraelectronica_id as compra_id, "); 
			sql.append("   coalesce(sum(case when imp.grupo = 1 then i.importe1 else 0 end), 0) as percepcionesIIBB, "); 
			sql.append("   coalesce(sum(case when imp.grupo = 3 then i.importe1 else 0 end), 0) as percepcionesIVA, ");  
			sql.append("		   coalesce(sum(case when imp.grupo = 4 then i.importe1 else 0 end), 0) as impuestosMunicipales, "); 
			sql.append(" coalesce(sum(case when imp.grupo = 5 then i.importe1 else 0 end), 0) as impuestosNacionales, "); 
			sql.append(" coalesce(sum(case when impDesp.impuestosDespacho_id is not null then i.importe1 else 0 end), 0) as creditoFiscal, "); 
			sql.append(" coalesce(sum(case when impDesp.impuestosDespacho_id is not null then i.importe1 * 100 / (case when i.alicuota is null or i.alicuota = 0 then " + ALICUOTA_IVA_CREDITOFISCAL + " else alicuota end) else 0 end), 0) as importeGravadoCreditoFiscal ");
			sql.append("from ").append(Esquema.concatenarEsquema("CompraElectronica_Impuestos i "));
			sql.append("join ").append(Esquema.concatenarEsquema("impuesto imp on imp.id = i.impuesto_id "));
			sql.append("left join ").append(Esquema.concatenarEsquema("ConfiguracionAfip_Impuesto impDesp on impDesp.impuestosDespacho_id = i.impuesto_id ")); 
			sql.append("group by i.compraelectronica_id ");
			sql.append(")  impuestosCompras on impuestosCompras.compra_id = c.id "); 
		sql.append("left join ");
			sql.append("( ");
			sql.append("select compra_id, count(compra_id) as cantidadAlicuotas from "); 
				sql.append("( select i.compra_id as compra_id, i.tasaiva as tasaiva ");
				sql.append("from ").append(Esquema.concatenarEsquema("itemcompraelectronica i "));
				sql.append("group by i.tasaiva, i.compra_id ");
				sql.append("union "); 
				sql.append("select i.compraelectronica_id as compra_id, "); 
				sql.append("case when i.alicuota is null or i.alicuota = 0 then ").append(ALICUOTA_IVA_CREDITOFISCAL).append(" "); 
				sql.append("else i.alicuota end as tasaiva ");
				sql.append("from ").append(Esquema.concatenarEsquema("CompraElectronica_Impuestos i "));
				sql.append("join ").append(Esquema.concatenarEsquema("ConfiguracionAfip_Impuesto impDesp on impDesp.impuestosDespacho_id = i.impuesto_id "));
				sql.append("group by i.alicuota, i.compraelectronica_id) ali "); 
			sql.append("group by compra_id ");       
			sql.append(") alicuotas on alicuotas.compra_id = c.id ");
		sql.append("where c.estado = 1 ");
		sql.append("and pi.presentacionesImpositivas = :presentaciones ");
		sql.append("and to_char(c.fecha, 'YYYYMM') = :fecha "); 
		sql.append("and c.empresa_id = :empresa ");
		sql.append("and c.dtype in (").append(TIPOCOMPROBANTESCOMPRAS).append(") ");
		sql.append("order by c.fecha, c.numero asc");
		
		/*String sql = "select c.fecha, c.tipo as letraComprobante, c.dtype as tipoTransaccion, c.numero, " + 
			"p.nombre, p.numeroDocumento, p.tipodocumento, c.iva1, c.subtotal1, c.total1, " +
			"(select count(*) cantidadAlicuotas from " + 
			"  (select tasaiva from " + Esquema.concatenarEsquema("ItemCompraElectronica") + " i " +
			"  where i.compra_id = c.id " + //and i.tasaiva <> 0 " + // no se considera como alicuota la tasa de iva 0%  
			"  group by tasaiva) Item) cantidadAlicuotas, " +
			"(select coalesce(sum(i.suma1), 0) " + 
			"  from " + Esquema.concatenarEsquema("ItemCompraElectronica") + " i " +
			"  where i.tasaiva <> 0 and i.compra_id = c.id) importeNoGrabado, " + 
			"(select coalesce(sum(i.suma1), 0) " + 
			"  from " + Esquema.concatenarEsquema("ItemCompraElectronica") + " i " +
			"  where i.tasaiva = 0 and i.compra_id = c.id) importeExento, " +
			"(select coalesce(sum(i.importe1), 0) " +
			"  from " + Esquema.concatenarEsquema("CompraElectronica_Impuestos") + " i " +
			"  join " + Esquema.concatenarEsquema("impuesto") + " imp on imp.id = i.impuesto_id " +
			"  where i.compraelectronica_id = c.id and imp.grupo = 1) percepcionesIIBB, " + 
			
			"c.cotizacion, m.codigoAfip, case when provDespacho.proveedoresDespacho_id is null then '' else c.numero end as despacho, " +
			
			"(select coalesce(sum(i.importe1), 0) " +
			"  from " + Esquema.concatenarEsquema("CompraElectronica_Impuestos") + " i " +
			"  join " + Esquema.concatenarEsquema("impuesto") + " imp on imp.id = i.impuesto_id " +
			"  where i.compraelectronica_id = c.id and imp.grupo = 3) percepcionesIVA, " +
			"(select coalesce(sum(i.importe1), 0) " +
			"  from " + Esquema.concatenarEsquema("CompraElectronica_Impuestos") + " i " +
			"  join " + Esquema.concatenarEsquema("impuesto") + " imp on imp.id = i.impuesto_id " +
			"  where i.compraelectronica_id = c.id and imp.grupo = 4) impuestosMunicipales, " +
			"(select coalesce(sum(i.importe1), 0) " +
			"  from " + Esquema.concatenarEsquema("CompraElectronica_Impuestos") + " i " +
			"  join " + Esquema.concatenarEsquema("impuesto") + " imp on imp.id = i.impuesto_id " +
			"  where i.compraelectronica_id = c.id and imp.grupo = 5) impuestosNacionales " +			
			"from " + Esquema.concatenarEsquema("CompraElectronica") + " c " +
			"join " + Esquema.concatenarEsquema("Proveedor") + " p on p.id = c.proveedor_id " +
			"join " + Esquema.concatenarEsquema("PosicionAnteImpuesto") + " pi on pi.codigo = p.posicionIva_codigo " +
			"join " + Esquema.concatenarEsquema("Moneda") + " m on m.id = c.moneda_id " +
			"left outer join " + Esquema.concatenarEsquema("ConfiguracionAfip_Proveedor provDespacho") + " on p.id = provDespacho.proveedoresDespacho_id " +
			"where c.estado = 1 " +
			"and pi.presentacionesImpositivas = :presentaciones " +
			"and to_char(c.fecha, 'YYYYMM') = :fecha " + 
			"and c.empresa_id = :empresa " +
			"and c.dtype in (" + TIPOCOMPROBANTESCOMPRAS + ") " +
			"order by c.fecha, c.numero asc";*/
		
		Query query = XPersistence.getManager().createNativeQuery(sql.toString());
		query.setParameter("empresa", this.getEmpresa().getId());
		SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
		query.setParameter("fecha", format.format(this.getFecha()));
		query.setParameter("presentaciones", Boolean.TRUE);
		return query;
			
	}

	private String generarComprasAlicuotas() {
		String fileName = "REGINFO_CV_COMPRAS_ALICUOTAS";
		fileName = this.formatearFileName(fileName);
		StringBuilder datos = new StringBuilder();
		
		Query query;
		List<?> rows;
		
		/* NO SE ENVIA LAS ALICUOTAS DE LOS DESPACHOS EN ESTE ARCHIVO
		query = this.getQueryAlicuotasProveedorDespachoImportacion();
		rows = query.getResultList();
		for (Object row: rows){
			Object[] r =(Object[]) row;
			
			// tipo de comprobante de despacho de importacion
			datos.append("066");			
			// punto venta y numero de comprobante: 0
			datos.append(StringUtils.repeat("0", 5));
			datos.append(StringUtils.repeat("0", 20));
									
			// tipo documento proveedor
			datos.append(TipoDocumento.codigoAfipPorIndice((Integer)r[0]));
			    
			// cuit del proveedor
			AfipPadronPersonas afip = new AfipPadronPersonas();
			datos.append(String.format("%020d", new BigInteger(afip.formatearCuit((String)r[1]))));
			
			BigDecimal tasaIva = (BigDecimal)r[2];
			BigDecimal importeIva = (BigDecimal)r[3];
			BigDecimal importeNetoGrabado = BigDecimal.ZERO;
			if (tasaIva.compareTo(BigDecimal.ZERO) != 0){
				importeNetoGrabado = importeIva.multiply(new BigDecimal(100)).divide(tasaIva, 2, RoundingMode.HALF_EVEN);
			}			
			datos.append(formatearImporte(importeNetoGrabado));
			try {
				datos.append(String.format("%04d", AfipAlicuotaIVA.codigoAfipPorTasa(tasaIva)));
			} catch (Exception e) {
				throw new ValidationException(e.toString());
			}	    		    
			// Impuesto liquidado
			datos.append(formatearImporte(importeIva));
			datos.append(this.finLinea());			
		}*/
		
		query = this.getQueryAlicuotasCompra();
		rows = query.getResultList();
		for (Object row: rows){
			Object[] r =(Object[]) row;
			
			// tipo de comprobante
			Integer tipoComprobante = (Integer)r[0];
		    
			// Solo se informa alicuotas cuando el comprobante es A
			if (TipoComprobanteArg.A.ordinal() == tipoComprobante){
				
				String tipoTransaccion = (String)r[1];
				Integer tipoComprobanteAfip = TipoComprobanteArg.codigoAfipPorIndice(tipoComprobante, tipoTransaccion);
				datos.append(String.format("%03d", tipoComprobanteAfip));
								
				// punto de venta y nro de comprobante
				String numeroCompleto = (String)r[2];
				Integer puntoVenta = this.obtenerPuntoVenta(numeroCompleto);
				Integer numeroComprobante = this.obtenerNumeroComprobante(numeroCompleto);			
				if (puntoVenta >= 0){
			    	datos.append(String.format("%05d", puntoVenta));
			    }
			    else{
			    	datos.append("PPPPP");
			    }
			    if (numeroComprobante >= 0){
			    	datos.append(String.format("%020d", numeroComprobante));
			    }
			    else{
			    	datos.append("XXXXXXXXXXXXXXXXXXXX");
			    }
								
			    // tipo documento proveedor
			    datos.append(TipoDocumento.codigoAfipPorIndice((Integer)r[7]));
			    
			    // cuit del proveedor
			    AfipPadronPersonas afip = new AfipPadronPersonas();
			    datos.append(String.format("%020d", new BigInteger(afip.formatearCuit((String)r[8]))));
			    
			    // Importe neto grabado
			    datos.append(formatearImporte((BigDecimal)r[5]));
			    
			    // Alicuota IVA
			    BigDecimal tasaIva = (BigDecimal)r[4];
			    try {
					datos.append(String.format("%04d", AfipAlicuotaIVA.codigoAfipPorTasa(tasaIva)));
				} catch (Exception e) {
					throw new ValidationException(e.toString());
				}	    		    
			    		    
			    // Impuesto liquidado
			    datos.append(formatearImporte((BigDecimal)r[6]));
			    
			    datos.append(this.finLinea());
			}
		}
				
		return grabarArchivo(fileName, datos);
	}
	
	private Query getQueryAlicuotasCompra(){
		String sql = "select c.tipo, c.dtype, c.numero, c.fecha, " + 
				"i.tasaiva alicuota, coalesce(sum(i.suma1), 0) importenetogravado, coalesce(sum(i.iva1), 0) iva, " +
				"p.tipoDocumento, p.numeroDocumento,  provDespacho.proveedoresDespacho_id " + 
				"from " + Esquema.concatenarEsquema("ItemCompraElectronica") + " i " +
				"join " + Esquema.concatenarEsquema("CompraElectronica") + " c on c.id = i.compra_id " +
				"join " + Esquema.concatenarEsquema("Proveedor") + " p on p.id = c.proveedor_id " +
				"join " + Esquema.concatenarEsquema("PosicionAnteImpuesto") + " pi on pi.codigo = p.posicionIva_codigo " +
				"left outer join " + Esquema.concatenarEsquema("ConfiguracionAfip_Proveedor provDespacho") + " on p.id = provDespacho.proveedoresDespacho_id " +
				"where c.estado = 1 and provDespacho.proveedoresDespacho_id is null " +
				//"and i.tasaiva <> 0 " +
				"and pi.presentacionesImpositivas = :presentaciones " +
				"and to_char(c.fecha, 'YYYYMM') = :fecha " +
				"and c.empresa_id = :empresa " +
				"and c.dtype in (" + TIPOCOMPROBANTESCOMPRAS + ") " +
				"group by c.tipo, c.numero, c.fecha, c.dtype, i.tasaiva, p.tipoDocumento, p.numeroDocumento, provDespacho.proveedoresDespacho_id " +
				"order by c.fecha, c.numero asc";
		
		Query query = XPersistence.getManager().createNativeQuery(sql);
		query.setParameter("empresa", this.getEmpresa().getId());
		SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
		query.setParameter("fecha", format.format(this.getFecha()));
		query.setParameter("presentaciones", Boolean.TRUE);
		return query;
	}
	
	/*private Query getQueryAlicuotasProveedorDespachoImportacion(){
		StringBuffer sql = new StringBuffer();
		sql.append("select tipoDocumento, numeroDocumento, sum(alicuota) alicuota, sum(iva) iva from ( ");
			sql.append("select i.tasaiva alicuota, coalesce(sum(i.iva1), 0) iva, p.tipoDocumento, p.numeroDocumento ");
			sql.append("from ").append(Esquema.concatenarEsquema("ItemCompraElectronica i "));
			sql.append("join ").append(Esquema.concatenarEsquema("CompraElectronica c on c.id = i.compra_id ")); 
			sql.append("join ").append(Esquema.concatenarEsquema("Proveedor p on p.id = c.proveedor_id "));
			sql.append("join ").append(Esquema.concatenarEsquema("PosicionAnteImpuesto pi on pi.codigo = p.posicionIva_codigo ")); 
			sql.append("join ").append(Esquema.concatenarEsquema("ConfiguracionAfip_Proveedor provDespacho on p.id = provDespacho.proveedoresDespacho_id ")); 
			sql.append("where c.estado = 1 "); 
			sql.append("and pi.presentacionesImpositivas = :presentaciones ");
			sql.append("and to_char(c.fecha, 'YYYYMM') = :fecha ");
			sql.append("and c.empresa_id = :empresa ");
			sql.append("and c.dtype in (" + TIPOCOMPROBANTESCOMPRAS + ") "); 
			sql.append("group by i.tasaiva, p.tipoDocumento, p.numeroDocumento "); 
		sql.append("union all ");
			sql.append("select case when i.alicuota is null or i.alicuota = 0 then " + ALICUOTA_IVA_CREDITOFISCAL + " else i.alicuota end as alicuota, coalesce(sum(i.importe1), 0) impuesto, p.tipoDocumento, p.numeroDocumento ");
			sql.append("from ").append(Esquema.concatenarEsquema("compraelectronica_impuestos i ")); 
			sql.append("join ").append(Esquema.concatenarEsquema("ConfiguracionAfip_Impuesto impDesp on impDesp.impuestosDespacho_id = i.impuesto_id ")); 
			sql.append("join ").append(Esquema.concatenarEsquema("CompraElectronica c on c.id = i.compraelectronica_id ")); 
			sql.append("join ").append(Esquema.concatenarEsquema("Proveedor p on p.id = c.proveedor_id ")); 
			sql.append("join ").append(Esquema.concatenarEsquema("PosicionAnteImpuesto pi on pi.codigo = p.posicionIva_codigo ")); 
			sql.append("join ").append(Esquema.concatenarEsquema("ConfiguracionAfip_Proveedor provDespacho on p.id = provDespacho.proveedoresDespacho_id ")); 
			sql.append("where c.estado = 1 "); 
			sql.append("and pi.presentacionesImpositivas = :presentaciones ");
			sql.append("and to_char(c.fecha, 'YYYYMM') = :fecha ");
			sql.append("and c.empresa_id = :empresa ");
			sql.append("and c.dtype in (" + TIPOCOMPROBANTESCOMPRAS + ") "); 
			sql.append("group by i.alicuota, p.tipoDocumento, p.numeroDocumento	) ");
		sql.append(" t group by alicuota, tipoDocumento, numeroDocumento");
		
		Query query = XPersistence.getManager().createNativeQuery(sql.toString());
		query.setParameter("empresa", this.getEmpresa().getId());
		SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
		query.setParameter("fecha", format.format(this.getFecha()));
		query.setParameter("presentaciones", Boolean.TRUE);
		return query;
	}*/
	
	private String generarComprasAlicuotasDespachos(){
		String fileName = "REGINFO_CV_COMPRAS_DESPACHOS";
		fileName = this.formatearFileName(fileName);
		StringBuilder datos = new StringBuilder();
		
		Query query = this.getQueryAlicuotasDespachosCompra();
		List<?> rows = query.getResultList();
		for (Object row: rows){
			Object[] r =(Object[]) row;
			
			datos.append(String.format("%16s", r[0]));
			BigDecimal importeNetoGravado = (BigDecimal)r[1];
			BigDecimal tasaIva = (BigDecimal)r[2];
			BigDecimal importeIva = (BigDecimal)r[3];
			// despacho importacion 
			if (tasaIva.compareTo(BigDecimal.ZERO) != 0){
				importeNetoGravado = importeIva.multiply(new BigDecimal(100)).divide(tasaIva, 2, RoundingMode.HALF_EVEN); 
			}
			// importe neto gravado
		    datos.append(formatearImporte(importeNetoGravado));		    
		    // Alicuota IVA		    
		    try {
				datos.append(String.format("%04d", AfipAlicuotaIVA.codigoAfipPorTasa(tasaIva)));
			} catch (Exception e) {
				throw new ValidationException(e.toString());
			}	    		    
		    // impuesto liquidado
		    datos.append(formatearImporte(importeIva));	    
		    datos.append(this.finLinea());
		}
		return grabarArchivo(fileName, datos);
	}
	
	private Query getQueryAlicuotasDespachosCompra(){
		
		StringBuffer sql = new StringBuffer();
		sql.append("select numero, sum(importenetogravado) netogravado, alicuota, sum(impuesto) impuesto, fecha ");
		sql.append("from( ");
			sql.append("select c.numero as numero, coalesce(sum(i.suma1), 0) importenetogravado, ");
			sql.append("i.tasaiva alicuota, coalesce(sum(i.iva1), 0) impuesto, c.fecha fecha ");
			sql.append("from ").append(Esquema.concatenarEsquema("ItemCompraElectronica i "));
			sql.append("join ").append(Esquema.concatenarEsquema("CompraElectronica c ")).append("on c.id = i.compra_id "); 
			sql.append("join ").append(Esquema.concatenarEsquema("Proveedor p ")).append("on p.id = c.proveedor_id ");
			sql.append("join ").append(Esquema.concatenarEsquema("ConfiguracionAfip_Proveedor provDespacho ")).append("on p.id = provDespacho.proveedoresDespacho_id ");
			sql.append("join ").append(Esquema.concatenarEsquema("PosicionAnteImpuesto pi ")).append("on pi.codigo = p.posicionIva_codigo ");
			sql.append("where c.estado = 1 ");
			sql.append("and pi.presentacionesImpositivas = :presentaciones "); 
			sql.append("and to_char(c.fecha, 'YYYYMM') = :fecha ");
			sql.append("and c.empresa_id = :empresa ");
			sql.append("and c.dtype in (" + TIPOCOMPROBANTESCOMPRAS + ") ");
			sql.append("group by c.numero, c.fecha, i.tasaiva ");
		sql.append("union all ");

			sql.append("select c.numero as numero, 0 as importenetogravado, ");
			sql.append("case when i.alicuota is null or i.alicuota = 0 then " + ALICUOTA_IVA_CREDITOFISCAL + " else i.alicuota end as alicuota, ");
			sql.append("coalesce(sum(i.importe1), 0) impuesto, c.fecha ");
			sql.append("from ").append(Esquema.concatenarEsquema("compraelectronica_impuestos i ")); 
			sql.append("join ").append(Esquema.concatenarEsquema("ConfiguracionAfip_Impuesto impDesp ")).append("on impDesp.impuestosDespacho_id = i.impuesto_id ");
			sql.append("join ").append(Esquema.concatenarEsquema("CompraElectronica c ")).append("on c.id = i.compraelectronica_id "); 
			sql.append("join ").append(Esquema.concatenarEsquema("Proveedor p ")).append("on p.id = c.proveedor_id ");
			sql.append("join ").append(Esquema.concatenarEsquema("ConfiguracionAfip_Proveedor provDespacho ")).append("on p.id = provDespacho.proveedoresDespacho_id ");
			sql.append("join ").append(Esquema.concatenarEsquema("PosicionAnteImpuesto pi ")).append("on pi.codigo = p.posicionIva_codigo "); 
			sql.append("where c.estado = 1 "); 
			sql.append("and pi.presentacionesImpositivas = :presentaciones ");
			sql.append("and to_char(c.fecha, 'YYYYMM') = :fecha ");
			sql.append("and c.empresa_id = :empresa ");
			sql.append("and c.dtype in (" + TIPOCOMPROBANTESCOMPRAS + ") ");
			sql.append("group by c.numero, c.fecha, i.alicuota ");
		sql.append(") t ");
		sql.append("group by numero, fecha, alicuota");
		
		Query query = XPersistence.getManager().createNativeQuery(sql.toString());
		query.setParameter("empresa", this.getEmpresa().getId());
		SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
		query.setParameter("fecha", format.format(this.getFecha()));
		query.setParameter("presentaciones", Boolean.TRUE);
		return query;
	}
	
	private String generarRegistroCabecera() {
		String fileName = "REGINFO_CV_CABECERA";
		fileName = this.formatearFileName(fileName);
		
		Empresa empresa = getEmpresa();
		AfipPadronPersonas afip = new AfipPadronPersonas();
		
		StringBuilder datos = new StringBuilder();
		// CUIT
		datos.append(afip.formatearCuit(empresa.getCuit()));
		// Periodo YYYYMM
		SimpleDateFormat formatoDate = new SimpleDateFormat("yyyyMM");
		datos.append(formatoDate.format(this.getFecha()));
		// Secuencia
		String secuencia = this.getSecuencia().toString();
		if (secuencia.length() == 1) secuencia = "0" + secuencia;
		datos.append(secuencia);
		// Sin movimientos
		if (! this.hayMovimientos()) {
			datos.append("S");
		}
		else{
			datos.append("N");
		}
		// prorratear creditos fiscal (por el momento todo que no)
		datos.append("N");
		// creditos fiscal computable global o por comprobante
		datos.append(" ");
		// importe credito fiscal global
		datos.append(formatearImporte(BigDecimal.ZERO));
		// importe credito fiscal con asignacion directa
		datos.append(formatearImporte(BigDecimal.ZERO));
		// importe credito fiscal determinado por prorrateo
		datos.append(formatearImporte(BigDecimal.ZERO));
		// importe credito fiscal no computable global
		datos.append(formatearImporte(BigDecimal.ZERO));
		// credito fiscal contri. seg soc
		datos.append(formatearImporte(BigDecimal.ZERO));
		datos.append(formatearImporte(BigDecimal.ZERO));
		
		return grabarArchivo(fileName, datos);
	}
	
	private String formatearFileName(String fileName){
		SimpleDateFormat format = new SimpleDateFormat(" yyyyMMdd_hh_mm_a");
		return fileName.concat(format.format(this.getFechaEjecucion()));
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
	
	private String formatearImporte(BigDecimal importe){
		// 13 digitos para la parte entera
		// 2 digitos para la parte decimal
		DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
		simbolos.setDecimalSeparator('.');
		DecimalFormat formatoDecimal = new DecimalFormat("0000000000000.00", simbolos);
		if (importe.compareTo(BigDecimal.ZERO) < 0){
			formatoDecimal = new DecimalFormat("000000000000.00", simbolos);
		}	
		String str = formatoDecimal.format(importe);
		String[] strs = str.split("\\.");
		
		return strs[0] + strs[1];
	}
	
	private String formatearCotizacion(BigDecimal cotizacion){
		// cotización 1 equivale a: 0001000000
		// 4 digitos para la parte entera
		// 6 digitos para la parte decimal
		DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
		simbolos.setDecimalSeparator('.');
		DecimalFormat formatoDecimal = new DecimalFormat("0000.000000", simbolos);
		String str = formatoDecimal.format(cotizacion);
		String[] strs = str.split("\\.");
		
		return strs[0] + strs[1];
	}
	
	private Integer obtenerPuntoVenta(String comprobante){
		Integer ptoVenta = -1;
		int pos = comprobante.indexOf("-");
		if (pos > 0){
			try{
				ptoVenta = new Integer(comprobante.substring(0, pos));
			}
			catch(Exception e){
				
			}
		}
		return ptoVenta;
	}
	
	private Integer obtenerNumeroComprobante(String comprobante){
		Integer numero = -1;
		int pos = comprobante.indexOf("-");
		if (pos > 0){
			try{
				numero = new Integer(comprobante.substring(pos + 1));
			}
			catch(Exception e){	
			}
		}
		return numero;
	}
	
	private boolean hayMovimientos(){
		return this.hayMovimientosCompras || this.hayMovimientosVentas;
	}
	
	private String finLinea(){
		return System.getProperty("line.separator");
	}
}
