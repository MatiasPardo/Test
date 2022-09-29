package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.conciliacionbancaria.model.ExtractoBancario;
import org.openxava.contabilidad.model.*;
import org.openxava.cuentacorriente.model.*;
import org.openxava.inventario.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.filter.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

@Entity

@Views({
	@View(members=
		"Principal[#" + 
				"descripcion;" +
				"empresa, moneda, cotizacion;" +
				"numero, fecha, fechaCreacion;" +
				"estado, subestado, ultimaTransicion;" + 
				"cliente;" +
				"observaciones];" +
		"Total[#total, aCobrar, diferencia];" +
		"items{comprobantesCobrar; items; retenciones} Trazabilidad{trazabilidad} CuentaCorriente{ctacte}" 
	),
	@View(name="Simple", members="numero"),
	@View(name="ReciboContado", 
			members="Cobranza[numero, moneda;" +
				"total, aCobrar, diferencia;" + 	
				"items;" +
				"retenciones;" +
				"]" +
			"Factura[facturaContado]" +
			"observaciones;" 			
	),
	@View(name="ReciboContadoSoloLectura", 
		members="Cobranza[numero, moneda;" +
				"total, aCobrar, diferencia;" + 	
				"items;" +
				"retenciones;" +
				"]" +
				"Factura[facturaContado]" +
				"observaciones;" +
				"trazabilidad;"
	),
	@View(name="ItemReciboCobranza", 
		members="numero, fecha, estado;" +
			"cliente;")
})

@Tabs({
	@Tab(
		properties="empresa.nombre, fecha, numero, estado, cliente.nombre, moneda.nombre, cotizacion, observaciones, total",
		filter=SucursalEmpresaFilter.class,		
		baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL,
		defaultOrder="${fechaCreacion} desc")
})

public class ReciboCobranza extends IngresoValores implements ITransaccionCtaCte, ITransaccionContable, IDestinoEMail{

	public static String convertirStringPendienteCobrar(BigDecimal importe) {
		String msj = "";
		if (importe != null){
			int compare = importe.compareTo(BigDecimal.ZERO);
			if (compare > 0){
				msj = "Sobran " + UtilERP.convertirString(importe);
			}
			else if (compare < 0){
				msj = "Faltan " + UtilERP.convertirString(importe.abs());
			}
			else{
				msj = UtilERP.convertirString(importe);
			}
		}
		return msj;
	}
	
	@Stereotype("MONEY")
	@Min(value=0, message="No puede ser menor a 0")
	@ReadOnly(forViews="ReciboContado")
	private BigDecimal aCobrar;
	
	@Depends(value="total, aCobrar")
	@Hidden
	public BigDecimal getDiferencia(){
		if (this.getaCobrar().compareTo(BigDecimal.ZERO) > 0){
			return this.getTotal().subtract(this.getaCobrar());
		}
		else{
			return null;
		}
	}
	
	@ManyToMany(fetch=FetchType.LAZY)
	@ListProperties("fecha, tipo, numero, importeOriginal, monedaOriginal.nombre, saldo1, saldo2")
	@OrderBy("fecha asc") 
	@CollectionView(value="Simple")
	@NewAction("ComprobantesPorCobrar.add")
	@RemoveAction(value="ComprobantesPorCobrar.remove")
	@RemoveSelectedAction(value="ComprobantesPorCobrar.removeSelected")
	private Collection<CuentaCorrienteVenta> comprobantesCobrar;
	
	@OneToMany(mappedBy="reciboCobranza", cascade=CascadeType.ALL)
	@ListProperties("empresa.nombre, destino.nombre, tipoValor.nombre, importeOriginal, cotizacion, importe, detalle, numero, fechaEmision, fechaVencimiento, banco.nombre")
	@SaveActions({
		@SaveAction(value="ItemTransaccion.save", notForViews="ReciboContado"),
		@SaveAction(value="ReciboFacturaContado.saveAndStay", forViews="ReciboContado")
	})	
	@NewAction(value="ItemTransaccion.new")
	@HideDetailAction(value="ItemTransaccion.hideDetail")
	@EditAction(value="ItemTransaccion.edit", notForViews="ReciboContado")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	@ReadOnly(forViews="ReciboContadoSoloLectura")
	private Collection<ItemReciboCobranza> items;
	
	@OneToMany(mappedBy="reciboCobranza", cascade=CascadeType.ALL)
	@ListProperties("impuesto.codigo, impuesto.nombre, importe, fecha, numero")
	@SaveAction(value="ItemTransaccion.save")
	@NewAction(value="ItemTransaccion.new")
	@HideDetailAction(value="ItemTransaccion.hideDetail")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	@ReadOnly(forViews="ReciboContadoSoloLectura")
	private Collection<ItemReciboCobranzaRetencion> retenciones;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@ReadOnly
	private Cobranza cobranza; 
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Transaccion")
	private CuentaCorrienteVenta ctacte;
	
	@ReferenceView("ReciboContado")
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly
	@NoFrame
	private FacturaVentaContado facturaContado;
	
	private int dias = 0;
		
	public Collection<ItemReciboCobranza> getItems() {
		return items;
	}

	public void setItems(Collection<ItemReciboCobranza> items) {
		this.items = items;
	}

	public Collection<ItemReciboCobranzaRetencion> getRetenciones() {
		if (this.retenciones == null){
			this.retenciones = new ArrayList<ItemReciboCobranzaRetencion>();
		}
		return retenciones;
	}

	public void setRetenciones(Collection<ItemReciboCobranzaRetencion> retenciones) {
		this.retenciones = retenciones;
	}

	public FacturaVentaContado getFacturaContado() {
		return facturaContado;
	}

	public void setFacturaContado(FacturaVentaContado facturaContado) {
		this.facturaContado = facturaContado;
	}
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Cobranza";
	}

	@Override
	protected Collection<ItemIngresoValores> items() {
		Collection<ItemIngresoValores> itemsIngVal = new LinkedList<ItemIngresoValores>();
		if (this.getItems() != null){
			itemsIngVal.addAll(this.getItems());
		}
		return itemsIngVal;
	}
	
	public Cobranza getCobranza() {
		return cobranza;
	}

	public void setCobranza(Cobranza cobranza) {
		this.cobranza = cobranza;
	}
		
	@Override
	public Date CtaCteFecha() {
		return this.getFecha();
	}

	@Override
	public BigDecimal CtaCteImporte() {
		return this.getTotal().negate();
	}

	@Override
	public BigDecimal CtaCteNeto() {
		return this.getTotal().negate();
	}
	
	@Override
	public String CtaCteTipo() {
		return "COBRANZA";
	}

	@Override
	public OperadorComercial CtaCteOperadorComercial() {
		return this.getCliente();
	}

	@Override
	public IResponsableCuentaCorriente CtaCteResponsable() {
		if (getCliente() != null){
			return this.getCliente().getVendedor();
		}
		else{
			return null;
		}
	}

	
	@Override
	public Date CtaCteFechaVencimiento() {
		return this.getFecha();
	}

	@Override
	public String CtaCteNumero() {
		return this.getNumero();
	}

	@Override
	public Transaccion CtaCteTransaccion() {
		return this;
	}

	@Override
	public CuentaCorriente CtaCteNuevaCuentaCorriente() {
		return new CuentaCorrienteVenta();
	}

	@Override
	public boolean generadaPorDiferenciaCambio(){
		return false;
	}
	
	@Override
	public void detalleDiferenciaCambio(Collection<DiferenciaCambioVenta> detalleDifCambio){		
	}
	
	@Override
	public boolean actualizarFinanzasAlConfirmar(){
		boolean actualizar = false;		
		if (this.reciboContado()){
			actualizar = true;
		}
		else if (this.getCobranza() == null){
			actualizar = true;
		}
		return actualizar;
	}

	@Override
	public boolean generaContabilidad(){
		return true;
	}
	
	@Override
	public void generadorPasesContable(Collection<IGeneradorItemContable> items) {
		// Haber
		CuentaContable cuenta = TipoCuentaContable.Ventas.CuentaContablePorTipo(this.getCliente());
		GeneradorItemContablePorTr paseCliente = new GeneradorItemContablePorTr(this, cuenta);
		paseCliente.setHaber(this.getTotal());
		items.add(paseCliente);
						
		// Debe	
		for(ItemIngresoValores itemRecibo: this.getItems()){
			Tesoreria tesoreria = itemRecibo.getDestino();
			Query query = XPersistence.getManager().createQuery("from CuentaBancaria where id = :id");
			query.setParameter("id", tesoreria.getId());
			query.setMaxResults(1);
			List<?> result = query.getResultList();
			if (!result.isEmpty()){
				CuentaBancaria cuentaBancaria = (CuentaBancaria)result.get(0);
				cuenta = TipoCuentaContable.Finanzas.CuentaContablePorTipo(cuentaBancaria);
			}
			else{
				cuenta = TipoCuentaContable.Finanzas.CuentaContablePorTipo(itemRecibo.getTipoValor());
			}
			GeneradorItemContablePorTr paseContable = new GeneradorItemContablePorTr(this, cuenta);
			paseContable.setDebe(itemRecibo.getImporte());
			items.add(paseContable);			
		}		
		
		for(ItemReciboCobranzaRetencion itemRetencion: this.getRetenciones()){
			cuenta = TipoCuentaContable.Impuesto.CuentaContablePorTipo(itemRetencion.getImpuesto());
			GeneradorItemContablePorTr paseContable = new GeneradorItemContablePorTr(this, cuenta);
			paseContable.setDebe(itemRetencion.getImporte());
			items.add(paseContable);
		}
	}

	@Override
	public void antesPersistirMovimientoFinanciero(MovimientoValores item) {	
	}
	
	@Hidden
	@ReadOnly
	private Long generadoPorExtracto = null;
	
	public Long getGeneradoPorExtracto() {
		return generadoPorExtracto;
	}

	public void setGeneradoPorExtracto(Long generadoPorExtracto) {
		this.generadoPorExtracto = generadoPorExtracto;
	}

	@Override
	public void despuesPersistirMovimientoFinanciero(MovimientoValores item, boolean revierte){
		if (!revierte && this.getGeneradoPorExtracto() != null){
			try{
				Collection<MovimientoValores> movimientosAConciliar = new LinkedList<MovimientoValores>();
				movimientosAConciliar.add(item);
				ExtractoBancario extracto = XPersistence.getManager().find(ExtractoBancario.class, this.generadoPorExtracto);
				Collection<ExtractoBancario> extractoAConciliar = new LinkedList<ExtractoBancario>();
				extractoAConciliar.add(extracto);			
				ExtractoBancario.conciliar(movimientosAConciliar, extractoAConciliar);
			}
			catch(Exception e){
				throw new ValidationException("No se puede conciliar, debera borrar y registrar nuevamente la cobranza: " + e.toString());
			}
		}
	}
	
	@Override
	public void recalcularTotales(){
		super.recalcularTotales();
		
		BigDecimal totalRetenciones = BigDecimal.ZERO;
		for(ItemReciboCobranzaRetencion retencion: this.getRetenciones()){
			totalRetenciones = totalRetenciones.add(retencion.getImporte());
		}
		
		this.setTotal(this.getTotal().add(totalRetenciones));
	}

	@Override
	public void imputacionesGeneradas(Collection<Imputacion> imputacionesGeneradas) {
		Query query = XPersistence.getManager().createQuery("from ImputacionVenta where generadaPor = :id and tipoEntidad = :tipoEntidad");
		query.setParameter("id", this.getId());
		query.setParameter("tipoEntidad", this.getClass().getSimpleName());
		List<?> results = query.getResultList();
		for(Object result: results){
			imputacionesGeneradas.add((Imputacion) result);
		}
	}
	
	@Override
	public void copiarPropiedades(Object objeto){
		super.copiarPropiedades(objeto);
		this.setItems(null);
		this.setRetenciones(null);
	}
	
	public CuentaCorrienteVenta getCtacte() {
		return ctacte;
	}

	public void setCtacte(CuentaCorrienteVenta ctacte) {
		this.ctacte = ctacte;
	}

	@Override
	public void CtaCteReferenciarCuentaCorriente(CuentaCorriente ctacte) {
		this.setCtacte((CuentaCorrienteVenta)ctacte);		
	}

	@SuppressWarnings("unchecked")
	public Collection<CuentaCorrienteVenta> getComprobantesCobrar() {
		return comprobantesCobrar == null ? Collections.EMPTY_LIST : comprobantesCobrar;
	}

	public void setComprobantesCobrar(Collection<CuentaCorrienteVenta> comprobantesCobrar) {
		this.comprobantesCobrar = comprobantesCobrar;
	}
	
	@Override
	protected void posConfirmarTransaccion(){
		super.posConfirmarTransaccion();
		
		this.calcularDiasCobranza();
		
		// se imputan los comprobantes con la cobranza
		if (!this.getComprobantesCobrar().isEmpty()){
			List<CuentaCorriente> comprobantesCuentaCorriente = new LinkedList<CuentaCorriente>();
			List<Imputacion> imputaciones = new LinkedList<Imputacion>();
			comprobantesCuentaCorriente.addAll(this.getComprobantesCobrar());
			comprobantesCuentaCorriente.add(this.comprobanteCuentaCorriente());
			Imputacion.imputarComprobantes(comprobantesCuentaCorriente, imputaciones);
						
			for(Imputacion imputacion: imputaciones){
				imputacion.asignarGeneradaPor(this);
			}
		}
		
		if (this.getFacturaContado() != null){
			this.getFacturaContado().asignarSubEstadoCobrado();
			Trazabilidad.crearTrazabilidad(this.getFacturaContado(), FacturaVentaContado.class.getSimpleName(), this, this.getClass().getSimpleName());
		}
	}
	
	@Override
	public Integer CtaCteCoeficiente() {
		return -1;
	}
	
	@Override
	public boolean generaCtaCte(){
		boolean genera = true;
		if (this.reciboContado()){
			genera = false;
		}
		return genera;
	}

	public boolean reciboContado(){
		if (getFacturaContado() != null){
			return true;
		}
		else{
			return false;
		}
	}
	
	@Override
	public String viewName(){
		if (reciboContado()){
			if (this.soloLectura()){
				return "ReciboContadoSoloLectura";
			}
			else{
				return "ReciboContado";
			}
		}
		else{	
			return super.viewName();
		}			
	}

	@Override
	protected void validacionesPreConfirmarTransaccion(Messages errores){
		super.validacionesPreConfirmarTransaccion(errores);
		
		if (this.reciboContado()){
			BigDecimal recibo = this.getTotal().setScale(2, RoundingMode.HALF_EVEN);
			BigDecimal factura =  this.getFacturaContado().getTotalACobrar().setScale(2, RoundingMode.HALF_EVEN);
			if (recibo.compareTo(factura) != 0){
				errores.add("No coincide el total del recibo " + recibo.toString() + " con la factura " + factura.toString());
			}
		}
	}
	
	@Override
	protected void preConfirmarTransaccion(){
		super.preConfirmarTransaccion();
		
		if (this.getaCobrar().compareTo(BigDecimal.ZERO) > 0){
			BigDecimal totalRedondeado = this.getTotal().setScale(2, RoundingMode.HALF_EVEN);
			int compare = totalRedondeado.compareTo(this.getaCobrar());
			if (compare > 0){
				throw new ValidationException("A cobrar " + this.getaCobrar().toString() + ": Sobran " + totalRedondeado.subtract(this.getaCobrar()));
			}
			else if (compare < 0){
				throw new ValidationException("A cobrar " + this.getaCobrar().toString() + ": Faltan " + this.getaCobrar().subtract(totalRedondeado));
			}
			
		}
	}
	
	public int getDias() {
		return dias;
	}

	public void setDias(int dias) {
		this.dias = dias;
	}
	
	private void calcularDiasCobranza(){
		// Cálculo de días de pago
		BigDecimal dias = BigDecimal.ZERO;
		BigDecimal total = BigDecimal.ZERO;
		Date fechaActual = this.getFecha();
		for(ItemReciboCobranza item: this.getItems()){
			long difDias = 0;
			if (item.getFechaVencimiento() != null){
				difDias = UtilERP.diferenciaDias(fechaActual, item.getFechaVencimiento());
			}			 
			if (difDias <= 0){
				difDias = 0;
			}			
			dias = dias.add(item.getImporte().multiply(new BigDecimal(difDias)));
			total = total.add(item.getImporte());
		}
		
		if (total.compareTo(BigDecimal.ZERO) != 0){
			this.setDias(dias.divide(total, 0, RoundingMode.HALF_EVEN).intValue());
		}
		else{
			this.setDias(0);
		}
	}
	
	@Override
	public void tipoTrsDestino(Collection<Class<?>> tipoTrsDestino){
		super.tipoTrsDestino(tipoTrsDestino);
		
		tipoTrsDestino.add(OrdenPreparacion.class);
		tipoTrsDestino.add(Remito.class);
	}
	
	@Override
	protected boolean cumpleCondicionGeneracionPendiente(Class<?> tipoTrDestino){
		boolean cumple = false;
		if (OrdenPreparacion.class.equals(tipoTrDestino)){
			if (this.reciboContado()){
				if (this.getFacturaContado().getEntrega().getComportamiento().equals(ComportamientoTipoEntrega.Preparar)){
					cumple = !this.getFacturaContado().facturaSoloConceptos();
				}
			}
		}
		else if (Remito.class.equals(tipoTrDestino)){
			if (this.reciboContado()){
				if (this.getFacturaContado().getEntrega().getComportamiento().equals(ComportamientoTipoEntrega.Remitir)){
					cumple = !this.getFacturaContado().facturaSoloConceptos();
				}
			}
		}
		else{
			cumple = super.cumpleCondicionGeneracionPendiente(tipoTrDestino);
		}
		return cumple;
	}
	
	@Override
	protected void pasajeAtributosWorkFlowPrePersist(Transaccion destino, List<IItemPendiente> items){
		super.pasajeAtributosWorkFlowPrePersist(destino, items);
		
		if (destino.getClass().equals(Remito.class)){
			Remito remito = (Remito)destino;			
			remito.setDeposito(this.getFacturaContado().getDeposito());
			remito.setDomicilioEntrega(this.getFacturaContado().getDomicilioEntrega());
			remito.setNoFacturar(Boolean.TRUE);
		}
		else if (destino.getClass().equals(OrdenPreparacion.class)){
			throw new ValidationException("No habilitado para generar orden de preparación desde Factura Contado");
		}
	}
		
	@Override
	protected void pasajeAtributosWorkFlowPosPersist(Transaccion destino, List<IItemPendiente> items){
		super.pasajeAtributosWorkFlowPosPersist(destino, items);
		if (destino.getClass().equals(Remito.class)){
			Remito remito = (Remito)destino;
			remito.setItems(new LinkedList<ItemRemito>());
			
			for(IItemPendiente itemPendiente: items){
				ItemVentaElectronica itemorigen = (ItemVentaElectronica)itemPendiente.getItem();
				ItemRemito itemdestino = new ItemRemito();
				itemdestino.copiarPropiedades(itemorigen);
				itemdestino.setIdCreadaPor(itemorigen.getId());
				itemdestino.setTipoEntidadCreadaPor(ItemVentaElectronica.class.getSimpleName());
				itemdestino.setFacturado(Boolean.TRUE);
				itemdestino.setRemito(remito);
				itemdestino.recalcular();
				remito.getItems().add(itemdestino);
				XPersistence.getManager().persist(itemdestino);	
			}
		}
	}
	
	@Override
	public void getTransaccionesGeneradas(Collection<Transaccion> trs){
		if (this.reciboContado()){
			Collection<Remito> remitos = remitosGenerados();
			if (!remitos.isEmpty()){
				trs.addAll(remitos);
			}
		}		
	}
	
	private Collection<Remito> remitosGenerados(){
		if (this.getEstado().equals(Estado.Confirmada)){
			if (this.reciboContado()){
				final Collection<Remito> remitos = new LinkedList<Remito>();
			
				String sql = "select ir.remito_id " +
					"from " + Esquema.concatenarEsquema("ItemVentaElectronica") + " iv " + 
					"join " + Esquema.concatenarEsquema("ItemRemito") + " ir on ir.idCreadaPor = iv.id " +  
					"where iv.venta_id = :factura group by ir.remito_id"; 
				Query query = XPersistence.getManager().createNativeQuery(sql);
				query.setParameter("factura", this.getFacturaContado().getId());
				List<?> result = query.getResultList();
				for(Object id: result){
					Remito remito = XPersistence.getManager().find(Remito.class, id);
					remitos.add(remito);
				}
				return remitos;
			}
			else{
				return Collections.emptyList();
			}
		}
		else{
			return Collections.emptyList();
		}
	}

	public BigDecimal getaCobrar() {
		return aCobrar == null ? BigDecimal.ZERO : this.aCobrar;
	}

	public void setaCobrar(BigDecimal aCobrar) {
		this.aCobrar = aCobrar;
	}
	
	public void recalcularSaldoACobrar() {
		BigDecimal aCobrar = this.getSaldoComprobantes();
		this.setaCobrar(aCobrar);
	}
	
	@Hidden
	public BigDecimal getSaldoComprobantes(){
		BigDecimal importe = BigDecimal.ZERO;
		
		if ((this.getComprobantesCobrar() != null) && (this.getMoneda() != null)){
			for(CuentaCorriente comprobante: this.getComprobantesCobrar()){
				if (comprobante.getMoneda1().equals(this.getMoneda())){
					importe = importe.add(comprobante.getSaldo1());
				}
				else if (comprobante.getMoneda2().equals(this.getMoneda())){
					importe = importe.add(comprobante.getSaldo2());
				}
				else if (comprobante.getMonedaOriginal().equals(this.getMoneda())){
					importe = importe.add(comprobante.getSaldoOriginal());
				}
			}
		}		
		return importe;
	}
	
	@Override
	public EmpresaExterna ContabilidadOriginante() {
		EmpresaExterna empresaExterna = null;
		if (this.getCliente() != null){
			empresaExterna = XPersistence.getManager().find(EmpresaExterna.class, this.getCliente().getId());
		}
		return empresaExterna;		
	}
	
	@Override
	public BigDecimal ContabilidadTotal() {
		return this.calcularImporte1(this.getTotal()); 
	}
	
	@Override
	protected void posAnularTransaccion(){
		super.posAnularTransaccion();
		
		if (this.getFacturaContado() != null){
			this.getFacturaContado().asignarSubEstadoFacturado();
		}
	}

	@Override
	public String emailPara() {
		return this.getCliente().getMail1();
	}

	@Override
	public String emailCC() {
		return this.getCliente().getMail2();
	}
}
