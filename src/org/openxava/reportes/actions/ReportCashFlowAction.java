package org.openxava.reportes.actions;

import java.math.*;
import java.text.*;
import java.util.*;

import javax.persistence.*;
import javax.validation.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;

import net.sf.jasperreports.engine.*;

public class ReportCashFlowAction extends ReportBaseAction{

	@Override
	protected String getNombreReporte() {
		return "CashFlow.jrxml";
	}

	private Date fechaActual = new Date();
	
	@Override
	protected void agregarParametros(Map<String, Object> parametros) {
		this.agregarValoresEnCarteraYSemanas(parametros);
		this.agregarValoresEnCarteraVencidos(parametros);
		this.agregarValoresEfectivo(parametros);
		this.agregarCuentaCorriente(parametros);
		this.agregarCuentaCorrienteVencida(parametros);
		this.agregarPedidosNoFacturados(parametros);
		this.agregarComprasNoFacturados(parametros);		
	}

	@Override
	protected JRDataSource getDataSource() throws Exception {
		return null;
	}

	@Override
	protected boolean filtraPorEmpresa(){
		return true;
	}
	
	private int cantidadSemanas(){
		return 24;
	}
	
	private void agregarValoresEnCarteraYSemanas(Map<String, Object> parametros){
		String sql = queryValoresEnCartera();
		Query query = XPersistence.getManager().createNativeQuery(sql);
		List<?> result = query.getResultList();
		
		Integer semana = 1;
		for(Object res: result){
			parametros.put("SEMANA" + semana.toString(), ((Object[])res)[0]);
			parametros.put("VALORESAPAGAR" + semana.toString(), ((Object[])res)[1]);
			parametros.put("VALORESACOBRAR" + semana.toString(), ((Object[])res)[2]);
			semana++;
			if (semana > this.cantidadSemanas()){
				break;
			}
		}
	}
	
	private String queryValoresEnCartera(){
		StringBuffer sql = new StringBuffer();
		sql.append("select fecha_inicial as semana, ");
		sql.append("coalesce(sum(case when e.moneda1_id = m.id and v.estado = 0 and t.comportamiento in (2) then v.importe * -1 else 0 end), 0) importaPagar, "); 
		sql.append("coalesce(sum(case when e.moneda1_id = m.id and v.estado = 0 and t.comportamiento in (1) then v.importe else 0 end), 0) importeCobrar from "); 
		sql.append(Esquema.concatenarEsquema("semanas_intervalo")).append("(" + this.fechaActualSQL() + ", " + this.cantidadSemanasSQL() + ") as (fecha_inicial date, fecha_final date) left join ");
		sql.append(Esquema.concatenarEsquema("valor")).append(" v on v.fechavencimiento <= fecha_final and v.fechavencimiento >= fecha_inicial left join "); 
		sql.append(Esquema.concatenarEsquema("tipoValorConfiguracion")).append(" t on t.id = v.tipoValor_id left join ");
		sql.append(Esquema.concatenarEsquema("moneda")).append(" m on m.id = v.moneda_id left join "); 
		sql.append(Esquema.concatenarEsquema("empresa")).append(" e on e.id = v.empresa_id ");
		sql.append("group by semana ");
		sql.append("order by semana asc");
		
		return sql.toString();
	}
	
	private void agregarValoresEnCarteraVencidos(Map<String, Object> parametros){
		if (!parametros.containsKey("SEMANA1")){
			throw new ValidationException("Error al calcular saldos de valores vencidos");
		}
		String sql = this.queryValoresEnCarteraVencidos((Date)parametros.get("SEMANA1"));
		Query query = XPersistence.getManager().createNativeQuery(sql);
		List<?> result = query.getResultList();
		if (result.isEmpty()){
			parametros.put("VALORESAPAGARVENCIDOS", BigDecimal.ZERO);
			parametros.put("VALORESACOBRARVENCIDOS", BigDecimal.ZERO);
		}
		else{
			for(Object res: result){
				parametros.put("VALORESAPAGARVENCIDOS", ((Object[])res)[0]);
				parametros.put("VALORESACOBRARVENCIDOS", ((Object[])res)[1]);			
			}
		}
	}
	
	private String queryValoresEnCarteraVencidos(Date primerDiaSemana){
		StringBuffer sql = new StringBuffer();
		sql.append("select coalesce(sum(case when t.comportamiento in (2) then v.importe * -1 else 0 end), 0) importePagar, "); 
		sql.append("coalesce(sum(case when t.comportamiento in (1) then v.importe else 0 end), 0) importeCobrar from "); 
		sql.append(Esquema.concatenarEsquema("valor")).append(" v join "); 
		sql.append(Esquema.concatenarEsquema("tipoValorConfiguracion")).append(" t on t.id = v.tipoValor_id left join ");
		sql.append(Esquema.concatenarEsquema("moneda")).append(" m on m.id = v.moneda_id left join "); 
		sql.append(Esquema.concatenarEsquema("empresa")).append(" e on e.id = v.empresa_id ");
		sql.append("where t.comportamiento in (1, 2) and v.estado = 0 and e.moneda1_id = m.id and to_char(v.fechaVencimiento, 'yyyy-MM-dd') < ").append(this.fechaSQL(primerDiaSemana)); 
		return sql.toString();
	}
	
	private void agregarCuentaCorriente(Map<String, Object> parametros){
		String sql = queryCuentaCorrientePendiente();
		Query query = XPersistence.getManager().createNativeQuery(sql);
		List<?> result = query.getResultList();
		
		Integer semana = 1;
		for(Object res: result){
			parametros.put("PAGOS" + semana.toString(), ((Object[])res)[1]);
			parametros.put("COBRANZAS" + semana.toString(), ((Object[])res)[2]);
			semana++;
			if (semana > this.cantidadSemanas()){
				break;
			}
		}
	}
	
	private String queryCuentaCorrientePendiente(){
		StringBuffer sql = new StringBuffer();
		sql.append("select fecha_inicial as semana, "); 
		sql.append("coalesce(sum(case when cc.dtype = 'CuentaCorrienteCompra' then cc.importe1 else 0 end) * -1, 0) importaPagar, "); 
		sql.append("coalesce(sum(case when cc.dtype = 'CuentaCorrienteVenta' then cc.importe1 else 0 end), 0) importeCobrar from ");
		sql.append(Esquema.concatenarEsquema("semanas_intervalo")).append("(" + this.fechaActualSQL() + ", " + this.cantidadSemanasSQL() + ") as (fecha_inicial date, fecha_final date) left join ");
		sql.append(Esquema.concatenarEsquema("cuentacorriente")).append(" cc on cc.fechavencimiento <= fecha_final and cc.fechavencimiento >= fecha_inicial ");
		sql.append("group by semana ");
		sql.append("order by semana asc");
		return sql.toString();
	}
	
	private void agregarCuentaCorrienteVencida(Map<String, Object> parametros){
		if (!parametros.containsKey("SEMANA1")){
			throw new ValidationException("Error al calcular saldos de cta cte vencidos");
		}
		String sql = queryCuentaCorrienteVencidaPendiente((Date)parametros.get("SEMANA1"));
		Query query = XPersistence.getManager().createNativeQuery(sql);
		List<?> result = query.getResultList();
		
		if (result.isEmpty()){
			parametros.put("PAGOSVENCIDOS", BigDecimal.ZERO);
			parametros.put("COBRANZASVENCIDOS", BigDecimal.ZERO);
		}
		else{
			for(Object res: result){
				parametros.put("PAGOSVENCIDOS", ((Object[])res)[0]);
				parametros.put("COBRANZASVENCIDOS", ((Object[])res)[1]);			
			}
		}
	}
	
	private String queryCuentaCorrienteVencidaPendiente(Date primerDiaSemana){
		StringBuffer sql = new StringBuffer();
		sql.append("select coalesce(sum(case when cc.dtype = 'CuentaCorrienteCompra' then cc.importe1 else 0 end) * -1, 0) importaPagar, "); 
		sql.append("coalesce(sum(case when cc.dtype = 'CuentaCorrienteVenta' then cc.importe1 else 0 end), 0) importeCobrar from ");	
		sql.append(Esquema.concatenarEsquema("cuentacorriente")).append(" cc ");
		sql.append("where to_char(cc.fechavencimiento, 'yyyy-MM-dd') < ").append(this.fechaSQL(primerDiaSemana));		
		return sql.toString();
	}
	
	private void agregarPedidosNoFacturados(Map<String, Object> parametros){
		String sql = queryPedidosPendientes();
		Query query = XPersistence.getManager().createNativeQuery(sql);
		List<?> result = query.getResultList();
		
		Integer semana = 1;
		for(Object res: result){
			parametros.put("PEDIDOS" + semana.toString(), ((Object[])res)[1]);
			semana++;
			if (semana > this.cantidadSemanas()){
				break;
			}
		}
	}
	
	private String queryPedidosPendientes(){
		StringBuffer sql = new StringBuffer();
		sql.append("select fecha_inicial as semana, coalesce(sum(t.importe), 0) from ");
		sql.append(Esquema.concatenarEsquema("semanas_intervalo")).append("(" + this.fechaActualSQL() + ", " + this.cantidadSemanasSQL() + ") as (fecha_inicial date, fecha_final date) left join ");
		sql.append("(select fecha_inicial as semana, "); 
		sql.append("round(coalesce(sum(i.subtotal1 * i.pendientepreparacion / i.cantidad), 0), 2) importe from ");
		sql.append(Esquema.concatenarEsquema("semanas_intervalo")).append("(" + this.fechaActualSQL() + ", " + this.cantidadSemanasSQL() + ") as (fecha_inicial date, fecha_final date) join ");
		sql.append(Esquema.concatenarEsquema("pedidoventa")).append(" p on p.fechavencimiento <= fecha_final and p.fechavencimiento >= fecha_inicial and p.estado = 1 join ");
		sql.append(Esquema.concatenarEsquema("estadisticapedidoventa")).append(" i on i.venta_id = p.id ");
		sql.append("where (i.pendientePreparacion > 0 and i.cantidad > 0) group by semana ");
		sql.append("union all ");
		sql.append("select fecha_inicial as semana, "); 
		sql.append("round(coalesce(sum((iv.subtotal1 * i.cantidad / iv.cantidad)), 0), 2) importe from "); 
		sql.append(Esquema.concatenarEsquema("semanas_intervalo")).append("(" + this.fechaActualSQL() + ", " + this.cantidadSemanasSQL() + ") as (fecha_inicial date, fecha_final date) join ");
		sql.append(Esquema.concatenarEsquema("pedidoventa")).append(" p on p.fechavencimiento <= fecha_final and p.fechavencimiento >= fecha_inicial and p.estado = 1 join "); 
		sql.append(Esquema.concatenarEsquema("estadisticapedidoventa")).append(" iv on iv.venta_id = p.id join "); 
		sql.append(Esquema.concatenarEsquema("itemordenpreparacion")).append(" i on iv.id = i.itempedidoventa_id join "); 
		sql.append(Esquema.concatenarEsquema("ordenpreparacion")).append(" o on o.id = i.ordenpreparacion_id and o.estado = 1 ");
		sql.append("where i.remitido = false and i.cantidad > 0 and iv.cantidad > 0 group by semana ");  
		sql.append("union all ");
		sql.append("select fecha_inicial as semana, "); 
		sql.append("round(coalesce(sum((iv.subtotal1 * ir.cantidad / iv.cantidad)), 0), 2) importe from "); 
		sql.append(Esquema.concatenarEsquema("semanas_intervalo")).append("(" + this.fechaActualSQL() + ", " + this.cantidadSemanasSQL() + ") as (fecha_inicial date, fecha_final date) join "); 
		sql.append(Esquema.concatenarEsquema("pedidoventa")).append(" p on p.fechavencimiento <= fecha_final and p.fechavencimiento >= fecha_inicial and p.estado = 1 join ");
		sql.append(Esquema.concatenarEsquema("estadisticapedidoventa")).append(" iv on iv.venta_id = p.id join "); 
		sql.append(Esquema.concatenarEsquema("itemordenpreparacion")).append(" io on iv.id = io.itempedidoventa_id join "); 
		sql.append(Esquema.concatenarEsquema("itemremito")).append(" ir on ir.itemordenpreparacion_id = io.id join "); 
		sql.append(Esquema.concatenarEsquema("remito")).append(" r on r.id = ir.remito_id and r.estado = 1 ");
		sql.append("where ir.facturado = false ");
		sql.append("group by semana ");
		sql.append(") t on t.semana = fecha_inicial ");
		sql.append("group by fecha_inicial order by fecha_inicial ");
		
		return sql.toString();
	}
	
	private void agregarComprasNoFacturados(Map<String, Object> parametros){
		String sql = queryComprasPendientes();
		Query query = XPersistence.getManager().createNativeQuery(sql);
		List<?> result = query.getResultList();
		
		Integer semana = 1;
		for(Object res: result){
			parametros.put("COMPRAS" + semana.toString(), ((Object[])res)[1]);
			semana++;
			if (semana > this.cantidadSemanas()){
				break;
			}
		}
	}
	
	private String queryComprasPendientes(){
		StringBuffer sql = new StringBuffer();
		sql.append("select fecha_inicial as semana, coalesce(sum(t.importe), 0) from ");
		sql.append(Esquema.concatenarEsquema("semanas_intervalo")).append("(" + this.fechaActualSQL() + ", " + this.cantidadSemanasSQL() + ") as (fecha_inicial date, fecha_final date) left join ");
		sql.append("(select fecha_inicial as semana, "); 
		sql.append("round(coalesce(sum(i.suma1 * i.pendienterecepcion / i.cantidad), 0), 2) importe from "); 
		sql.append(Esquema.concatenarEsquema("semanas_intervalo")).append("(" + this.fechaActualSQL() + ", " + this.cantidadSemanasSQL() + ") as (fecha_inicial date, fecha_final date) join "); 
		sql.append(Esquema.concatenarEsquema("itemordencompra")).append(" i on i.fecharecepcion <= fecha_final and i.fecharecepcion >= fecha_inicial join ");
		sql.append(Esquema.concatenarEsquema("ordencompra")).append(" o on i.ordencompra_id = o.id and o.estado = 1 "); 
		sql.append("where (i.pendienteRecepcion > 0 and i.cantidad > 0) group by semana ");
		sql.append("union all ");
		sql.append("select fecha_inicial as semana, "); 
		sql.append("round(coalesce(sum(i.suma1 * ir.cantidad / i.cantidad), 0), 2) importe from "); 
		sql.append(Esquema.concatenarEsquema("semanas_intervalo")).append("(" + this.fechaActualSQL() + ", " + this.cantidadSemanasSQL() + ") as (fecha_inicial date, fecha_final date) join "); 
		sql.append(Esquema.concatenarEsquema("itemordencompra")).append(" i on i.fecharecepcion <= fecha_final and i.fecharecepcion >= fecha_inicial join "); 
		sql.append(Esquema.concatenarEsquema("ordencompra")).append(" o on i.ordencompra_id = o.id and o.estado = 1 join "); 
		sql.append(Esquema.concatenarEsquema("itemrecepcionmercaderia")).append(" ir on ir.itemordencompra_id = i.id join "); 
		sql.append(Esquema.concatenarEsquema("pendientefacturacompra")).append(" p on p.idtrorigen = ir.recepcionMercaderia_id and p.cumplido = false and p.anulado = false "); 
		sql.append("where (i.pendienteRecepcion > 0 and i.cantidad > 0) group by semana ");
		sql.append(") t on t.semana = fecha_inicial ");
		sql.append("group by fecha_inicial order by fecha_inicial ");
		return sql.toString();
	}
	
	private void agregarValoresEfectivo(Map<String, Object> parametros){
		String sql = this.queryValoresEfectivo();
		Query query = XPersistence.getManager().createNativeQuery(sql);
		List<?> result = query.getResultList();		
		BigDecimal efectivo = BigDecimal.ZERO;
		for(Object res: result){
			BigDecimal importe = (BigDecimal)((Object[])res)[0];
			String idMonedaValor = (String)((Object[])res)[1];
			String idMoneda1 = (String)((Object[])res)[2];
			
			if (idMonedaValor.equals(idMoneda1)){
				efectivo = efectivo.add(importe);
			}
			else{
				Moneda monedaOrigen = XPersistence.getManager().find(Moneda.class, idMonedaValor);
				Moneda monedaDestino = XPersistence.getManager().find(Moneda.class, idMoneda1);
				BigDecimal cotizacion = Cotizacion.buscarCotizacion(monedaOrigen, monedaDestino, new Date());
				efectivo = efectivo.add(importe.multiply(cotizacion));
			}
		}
		parametros.put("DISPONIBLE", efectivo);		
	}
	
	private String queryValoresEfectivo(){
		StringBuffer sql = new StringBuffer();
		sql.append("select sum(v.importe), v.moneda_id, e.moneda1_id from ");
		sql.append(Esquema.concatenarEsquema("valor")).append(" v join "); 
		sql.append(Esquema.concatenarEsquema("tipovalorconfiguracion")).append(" t on t.id = v.tipovalor_id join ");
		sql.append(Esquema.concatenarEsquema("empresa")).append(" e on e.id = v.empresa_id ");
		sql.append("where t.comportamiento in (0) and v.estado = 0 group by v.moneda_id, e.moneda1_id");
		return sql.toString();
	}
		
	private String cantidadSemanasSQL(){
		// se agregan más semanas porque la función de sql a veces trunca en menos semanas. -- Revisar
		return Integer.toString(this.cantidadSemanas() + 10);
	}
	
	private String fechaActualSQL(){
		return this.fechaSQL(this.fechaActual);
	}
		
	private String fechaSQL(Date fecha){
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return "'" + format.format(fecha) + "'";
	}
	
	@Override
	public void execute() throws Exception {
		this.setFormat(JasperReportBaseAction.EXCEL);
		super.execute();
	}
}
