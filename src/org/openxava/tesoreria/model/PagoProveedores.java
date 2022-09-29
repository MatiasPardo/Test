package org.openxava.tesoreria.model;

import java.math.*;
import java.text.*;
import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.base.calculators.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.compras.model.*;
import org.openxava.conciliacionbancaria.model.ExtractoBancario;
import org.openxava.contabilidad.model.*;
import org.openxava.cuentacorriente.model.*;
import org.openxava.impuestos.model.*;
import org.openxava.jpa.*;
import org.openxava.model.*;
import org.openxava.negocio.model.*;
import org.openxava.tesoreria.actions.*;
import org.openxava.util.*;
import org.openxava.validators.*;

import com.allin.interfacesafip.util.NumberToLetterConverter;

@Entity

@Views({
	@View(members=
		"Principal[#" +
				"descripcion;" +
				"empresa, moneda, cotizacion;" +
				"numero, fecha, fechaCreacion;" +
				"estado, subestado, ultimaTransicion;" +				 
				"proveedor;" +
				"observaciones];" +		
		"items{ comprobantesPorPagar;" +
				"Totales[#" + 
					"saldoComprobantes, aPagar, falta;" +
					"valoresTotal, retencionesTotal, total;]" +
				"valores;" +				 
				"retenciones; " +				
		"} historico{historicoEstados} trazabilidad{trazabilidad} CuentaCorriente{ctacte}" 	
	),
	@View(name=Transaccion.VISTACAMBIOATRIBUTOSESPECIALES, members=
			"Principal[#" +
					"descripcion;" +
					"empresa, moneda, cotizacion;" +
					"numero, fecha, fechaCreacion;" +
					"estado, subestado, ultimaTransicion;" +				 
					"proveedor;" +
					"observaciones];" +		
			"items{ comprobantesPorPagar;" +
					"Totales[#" + 
						"saldoComprobantes, aPagar, falta;" +
						"valoresTotal, retencionesTotal, total;]" +
					"valores;" +				 
					"retenciones; " +				
			"} historico{historicoEstados}"),
	@View(name=Transaccion.VISTACAMBIOFECHA, members="fecha"),
	@View(name="Simple",
		members="numero, fecha")
})

@Tabs({
	@Tab(
		properties="fecha, numero, estado, proveedor.nombre, moneda.nombre, cotizacion, observaciones, total",
		filter=EmpresaFilter.class,		
		baseCondition=EmpresaFilter.BASECONDITION,
		defaultOrder="${fechaCreacion} desc")
})

public class PagoProveedores extends Transaccion implements ITransaccionValores, ITransaccionCtaCte, ITransaccionContable{

	public static final String PENDIENTE_ENVIO= "PENDENVIO";
	
	public static final String ENVIADO = "ENVIADO";
	
	public static PagoProveedores ultimoPagoMesProveedor(Date fecha, Proveedor proveedor) throws Exception{
		FechaInicioMesCalculator inicioCalculator = new FechaInicioMesCalculator();
		inicioCalculator.setFecha(fecha);
		Date primerDiaMes = (Date)inicioCalculator.calculate();
		FechaFinMesCalculator finCalculator = new FechaFinMesCalculator();
		finCalculator.setFecha(fecha);
		Date ultimoDiaMes = (Date)finCalculator.calculate();
		Calendar cal = Calendar.getInstance();
		cal.setTime(ultimoDiaMes);
		cal.add(Calendar.DAY_OF_YEAR, 1);
		ultimoDiaMes = cal.getTime();
		
		String sql = "from " + PagoProveedores.class.getSimpleName() + " where estado = :estado and fecha >= :desde and fecha < :hasta " +
					"and proveedor.id = :proveedor " +
					"order by fechaConfirmacion desc";
		Query query = XPersistence.getManager().createQuery(sql);
		query.setMaxResults(1);
		query.setParameter("estado", Estado.Confirmada);
		query.setParameter("desde", primerDiaMes);
		query.setParameter("hasta", ultimoDiaMes);
		query.setParameter("proveedor", proveedor.getId());
		query.setMaxResults(1);		
		List<?> list = query.getResultList();
		PagoProveedores pago = null;
		if (!list.isEmpty()){
			pago = ((PagoProveedores)list.get(0));
		}
		return pago;
	}
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView("Simple")
	@OnChange(value=OnChangeProveedorPagosAction.class)
	private Proveedor proveedor;
	
	@Stereotype("MONEY")
	@Min(value=0, message="No puede menor a 0")
	private BigDecimal aPagar;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal valoresTotal;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal retencionesTotal;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal total;
	
	@ManyToMany(fetch=FetchType.LAZY)
	@NewAction("ComprobantesPorPagar.add")
	@RemoveAction(value="ComprobantesPorPagar.remove")
	@RemoveSelectedAction(value="ComprobantesPorPagar.removeSelected")
	@ListProperties("fecha, tipo, numero, importeOriginal, monedaOriginal.nombre, saldo1, saldo2")
	@OrderBy("fecha asc") 
	@CollectionView(value="Simple")	
	private Collection<CuentaCorrienteCompra> comprobantesPorPagar;
	
	@OneToMany(mappedBy="pago", cascade=CascadeType.ALL)
	@ListProperties("empresa.nombre, origen.nombre, tipoValor.nombre, referencia.numero, importeOriginal, cotizacion, importe, detalle, numero, fechaEmision, fechaVencimiento, banco.nombre")
	@SaveAction(value="ItemTransaccion.save")
	@NewAction(value="ItemTransaccion.new")
	@HideDetailAction(value="ItemTransaccion.hideDetail")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	@ListAction("ColeccionItemsPagoProveedores.Cheques")
	private Collection<ItemPagoProveedores> valores;
	
	@OneToMany(mappedBy="pago", cascade=CascadeType.ALL)
	@ListProperties("impuesto.codigo, impuesto.nombre, calculoManual, retencionActual, numero, netoAcumulado, montoNoSujetoRetencion, netoGrabado, alicuota, retencionTotal, retencionesAnteriores")
	@SaveAction(value="ItemTransaccion.save")
	@NewAction(value="ItemTransaccion.new")
	@HideDetailAction(value="ItemTransaccion.hideDetail")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	@EditOnly	
	@ListAction(value="PagoProveedores.RetencionesPDF")
	private Collection<ItemPagoRetencion> retenciones;
	
	public Collection<CuentaCorrienteCompra> getComprobantesPorPagar() {
		return comprobantesPorPagar;
	}

	public void setComprobantesPorPagar(Collection<CuentaCorrienteCompra> comprobantesPorPagar) {
		this.comprobantesPorPagar = comprobantesPorPagar;
	}

	@Hidden
	public BigDecimal getSaldoComprobantes(){
		BigDecimal importe = BigDecimal.ZERO;
		
		if ((this.getComprobantesPorPagar() != null) && (this.getMoneda() != null)){
			for(CuentaCorrienteCompra comprobante: this.getComprobantesPorPagar()){
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
	
	public Collection<ItemPagoProveedores> getValores() {
		if (this.valores == null){
			return Collections.emptyList();
		}
		else{
			return valores;
		}
	}

	public void setValores(Collection<ItemPagoProveedores> valores) {
		this.valores = valores;
	}

	public Collection<ItemPagoRetencion> getRetenciones() {
		return retenciones;
	}

	public void setRetenciones(Collection<ItemPagoRetencion> retenciones) {
		this.retenciones = retenciones;
	}

	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}
		
	public BigDecimal getaPagar() {
		return aPagar == null ? BigDecimal.ZERO : this.aPagar;
	}

	public void setaPagar(BigDecimal aPagar) {
		this.aPagar = aPagar;
	}

	@Hidden
	@Stereotype("MONEY")
	public BigDecimal getFalta() {
		if (this.getaPagar().compareTo(BigDecimal.ZERO) == 0){
			return BigDecimal.ZERO;
		}
		else{
			return this.getaPagar().subtract(this.getTotal());
		}
	}

	public BigDecimal getValoresTotal() {
		return valoresTotal == null ? BigDecimal.ZERO : valoresTotal;
	}

	public void setValoresTotal(BigDecimal valoresTotal) {
		this.valoresTotal = valoresTotal;
	}

	public BigDecimal getRetencionesTotal() {
		return retencionesTotal == null ? BigDecimal.ZERO : retencionesTotal;
	}

	public void setRetencionesTotal(BigDecimal retencionesTotal) {
		this.retencionesTotal = retencionesTotal;
	}

	public BigDecimal getTotal(){
		return this.total == null ? BigDecimal.ZERO : this.total;
	}
	
	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	@Override
	public void recalcularTotales(){
		super.recalcularTotales();
		inicializarRetenciones();
		
		BigDecimal importeTotal = BigDecimal.ZERO;
		BigDecimal totalRetenciones = BigDecimal.ZERO;
		for(ItemPagoProveedores item: this.getValores()){
			item.recalcular();
			importeTotal = importeTotal.add(item.getImporte());
		}
		if (this.getRetenciones() != null){
			for(ItemPagoRetencion item: this.getRetenciones()){
				item.recalcular();
				totalRetenciones = totalRetenciones.add(item.getRetencionActual());
			}
			if (totalRetenciones.compareTo(BigDecimal.ZERO) != 0){
				// Las retenciones siempre se calculan en moneda1 (Pesos).
				// Si el pago esta en otra moneda debe ser convertidos
				totalRetenciones = convertirMonedaRetencion(totalRetenciones);				
			}
		}
		this.setValoresTotal(importeTotal);
		this.setRetencionesTotal(totalRetenciones);
		this.setTotal(totalRetenciones.add(importeTotal));
	}
	
	@Override
	public void movimientosValores(List<IItemMovimientoValores> lista) {
		lista.addAll(this.getValores());		
	}

	@Override
	public boolean revierteFinanzasAlAnular() {
		return true;
	}

	@Override
	public void antesPersistirMovimientoFinanciero(MovimientoValores item) {	
	}

	@Override
	public void despuesPersistirMovimientoFinanciero(MovimientoValores item, boolean revierte) {
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
	public boolean actualizarFinanzasAlConfirmar() {
		return true;
	}
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Pago";
	}
	
	@Override
	protected void validacionesPreGrabarTransaccion(Messages errores){
		super.validacionesPreGrabarTransaccion(errores);
		
		if (this.validaFechaUltimoPagoProveedor()){
			Date fechaUltimoPago = this.fechaUltimoPago();
			if (fechaUltimoPago != null){
				if (this.getFecha().compareTo(fechaUltimoPago) < 0){
					SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
					errores.add("La fecha del último pago registrado es " + format.format(fechaUltimoPago));				
				}
			}
		}
		
		
		BigDecimal saldoAPagar = this.getSaldoComprobantes();
		if (saldoAPagar.compareTo(BigDecimal.ZERO) > 0){
			if (this.getaPagar().compareTo(saldoAPagar) > 0){
				throw new ValidationException("El importe a pagar " + this.getaPagar().toString() + " no puede superar el saldo de los comprobantes a pagar " + saldoAPagar.toString());
			}
		}
	}
	
	private boolean validaFechaUltimoPagoProveedor(){
		boolean valida = false;
		// Solo se valida si tiene alguna retención que calcular
		if (this.getProveedor().getRetenciones() != null){
			for(EntidadRetencionProveedor entidad: this.getProveedor().getRetenciones()){
				valida = entidad.getCalcula();
				if (valida){
					break;
				}
			}
		}
		return valida;
	}
	
	@Override
	protected void validacionesPreConfirmarTransaccion(Messages errores){
		super.validacionesPreConfirmarTransaccion(errores);
		
		if (this.getValores().isEmpty() && (this.getRetenciones().isEmpty())){
			errores.add("sin_items");
		}
		
		for(ItemPagoProveedores item: this.getValores()){
			if (!item.getEmpresa().equals(this.getEmpresa())){
				errores.add("No puede diferir la empresa en los valores");
				break;
			}
		}
		
		int comparacion = this.getFalta().compareTo(BigDecimal.ZERO);		
		DecimalFormat format = new DecimalFormat("#,###.##");
		if (comparacion > 0){
			throw new ValidationException("Falta pagar " + format.format(this.getFalta().floatValue()));
		}
		else if (comparacion < 0){
			throw new ValidationException("Pago excedido " + format.format(this.getFalta().negate().floatValue()));
		}
	}

	@Override
	public Date CtaCteFecha() {
		return this.getFecha();
	}

	@Override
	public String CtaCteTipo() {
		return "PAGO";
	}

	@Override
	public OperadorComercial CtaCteOperadorComercial() {
		return this.getProveedor();
	}

	@Override
	public IResponsableCuentaCorriente CtaCteResponsable() {
		return null;
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
		return new CuentaCorrienteCompra();
	}

	@Override
	public BigDecimal CtaCteImporte() {
		return this.getTotal().negate();
	}
	
	@Override
	public BigDecimal CtaCteNeto() {
		return this.getValoresTotal().negate();
	}
	
	@Override
	public boolean generadaPorDiferenciaCambio(){
		return false;
	}
	
	@Override
	public void detalleDiferenciaCambio(Collection<DiferenciaCambioVenta> detalleDifCambio){		
	}

	@Override
	public void generadorPasesContable(Collection<IGeneradorItemContable> items) {
		// Debe
		CuentaContable cuenta = TipoCuentaContable.Compras.CuentaContablePorTipo(this.getProveedor());
		GeneradorItemContablePorTr paseProveedor = new GeneradorItemContablePorTr(this, cuenta);
		paseProveedor.setDebe(this.getTotal());
		items.add(paseProveedor);
								
		// Haber	
		
		// Valores
		for(ItemPagoProveedores itemPago: this.getValores()){
			Tesoreria tesoreria = itemPago.getOrigen();
			Query query = XPersistence.getManager().createQuery("from CuentaBancaria where id = :id");
			query.setParameter("id", tesoreria.getId());
			query.setMaxResults(1);
			List<?> result = query.getResultList();
			if (!result.isEmpty()){
				CuentaBancaria cuentaBancaria = (CuentaBancaria)result.get(0);
				cuenta = TipoCuentaContable.Finanzas.CuentaContablePorTipo(cuentaBancaria);
			}
			else{
				cuenta = TipoCuentaContable.Finanzas.CuentaContablePorTipo(itemPago.getTipoValor());
			}
			GeneradorItemContablePorTr paseContable = new GeneradorItemContablePorTr(this, cuenta);
			paseContable.setHaber(itemPago.getImporte());
			items.add(paseContable);			
		}
		// Retenciones
		for(ItemPagoRetencion itemRetencion: this.getRetenciones()){
			cuenta = TipoCuentaContable.Impuesto.CuentaContablePorTipo(itemRetencion.getImpuesto());
			GeneradorItemContablePorTr paseContable = new GeneradorItemContablePorTr(this, cuenta);
			paseContable.setHaber(this.convertirMonedaRetencion(itemRetencion.getRetencionActual()));
			items.add(paseContable);
		}
	}
	
	private void inicializarRetenciones(){		
		if ((this.getRetenciones() != null) && (!Is.emptyString(this.getId()))){
			if ((this.getEmpresa() != null) && (this.getRetenciones().isEmpty())){
				List<Impuesto> retenciones = new LinkedList<Impuesto>();
				this.getEmpresa().buscarRetencionesPago(retenciones);
				for(Impuesto impuesto: retenciones){
					crearImpuestoRetencion(impuesto);				
				}
			}		
		}
	}
	
	private void crearImpuestoRetencion(Impuesto impuesto){
		ItemPagoRetencion retencion = new ItemPagoRetencion();
		retencion.setPago(this);
		retencion.setImpuesto(impuesto);
		XPersistence.getManager().persist(retencion);
		this.getRetenciones().add(retencion);
	}
	
	public void grabarTransaccion(){
		super.grabarTransaccion();
		
		if ((this.getMoneda() != null) && (this.getMoneda1() != null)){
			if (this.getProveedor() != null){
				if (this.getProveedor().getMoneda() != null){
					if (!this.getMoneda().equals(this.getProveedor().getMoneda())){
						throw new ValidationException("La moneda debe ser " + this.getProveedor().getMoneda().getNombre());
					}
				}				
			}
		}
	}
	
	public Collection<ItemPagoRetencion> retencionesCalculadas(){
		List<ItemPagoRetencion> retenciones = new LinkedList<ItemPagoRetencion>();
		if (this.getRetenciones() != null){
			for(ItemPagoRetencion item: this.getRetenciones()){
				if (item.getRetencionActual().compareTo(BigDecimal.ZERO) > 0){
					retenciones.add(item);
				}
			}
		}
		return retenciones;
	}
	
	@Override
	protected void posConfirmarTransaccion(){
		super.posConfirmarTransaccion();
		
		for(ItemPagoRetencion itemRetencion: this.retencionesCalculadas()){
			if (itemRetencion.getRetencionActual().compareTo(BigDecimal.ZERO) > 0){
				Numerador numerador = itemRetencion.getImpuesto().buscarNumerador(this);
				if (numerador != null){
					numerador.numerarObjetoNegocio(itemRetencion);
				}
				else if (itemRetencion.getImpuesto().getTipo().debeNumerar()){
					throw new ValidationException("No se pudo numerar la retención " + itemRetencion.getImpuesto().toString() + ". No se encontró numerador definido");
				}
			}
		}
		
		if ((this.getComprobantesPorPagar() != null) && (!this.getComprobantesPorPagar().isEmpty())) {
			for(CuentaCorrienteCompra ctacte: this.getComprobantesPorPagar()){
				if (Is.emptyString(ctacte.getIdPagoProveedores())){
					// se marcan los comprobantes, para saber cual fue el primer pago que los procesó. 
					ctacte.setIdPagoProveedores(this.getId());
				}
			}
			
			// se imputan los comprobantes con el pago
			List<CuentaCorriente> comprobantesCuentaCorriente = new LinkedList<CuentaCorriente>();
			List<Imputacion> imputaciones = new LinkedList<Imputacion>();
			comprobantesCuentaCorriente.addAll(this.getComprobantesPorPagar());
			comprobantesCuentaCorriente.add(this.comprobanteCuentaCorriente());
			Imputacion.imputarComprobantes(comprobantesCuentaCorriente, imputaciones);
			
			for(Imputacion imputacion: imputaciones){
				imputacion.asignarGeneradaPor(this);
			}
		}
	}
	
	@Override
	protected void validacionesPreAnularTransaccion(Messages errores){
		super.validacionesPreAnularTransaccion(errores);
		
		if (!this.retencionesCalculadas().isEmpty()){
			PagoProveedores ultimoPago;
			try {
				ultimoPago = PagoProveedores.ultimoPagoMesProveedor(this.getFecha(), this.getProveedor());
				if ((ultimoPago != null) && (!ultimoPago.equals(this))){
					errores.add("No se puede anular porque calculo retenciones y el último pago del mes del proveedor es: " + ultimoPago.toString());
				}
			} catch (Exception e) {
				errores.add(e.toString());
			}
			
		}
	}
	
	@Override
	protected void posAnularTransaccion(){
		super.posAnularTransaccion();
		if (this.getComprobantesPorPagar() != null){
			for(CuentaCorrienteCompra ctacte: this.getComprobantesPorPagar()){
				if (Is.equalAsString(ctacte.getIdPagoProveedores(), this.getId())){
					ctacte.setIdPagoProveedores(null);
				}
			}
		}		
	}
	
	@Override
	public void imputacionesGeneradas(Collection<Imputacion> imputacionesGeneradas){
		Query query = XPersistence.getManager().createQuery("from ImputacionCompra where generadaPor = :pago and tipoEntidad = :tipoEntidad");
		query.setParameter("pago", this.getId());
		query.setParameter("tipoEntidad", this.getClass().getSimpleName());
		List<?> results = query.getResultList();
		for(Object result: results){
			imputacionesGeneradas.add((Imputacion) result);
		}
	}
	
	public boolean tieneOtroPagoAsociado(CuentaCorrienteCompra ctacte){
		return !Is.emptyString(ctacte.getIdPagoProveedores());
	}
	
	public Date fechaUltimoPago(){
		Date fecha = null;
		Query query = XPersistence.getManager().createQuery("from " + this.getClass().getSimpleName() + " where estado = :estado and id != :id and proveedor.id = :proveedor order by fecha desc");
		query.setMaxResults(1);
		query.setParameter("estado", Estado.Confirmada);
		query.setParameter("proveedor", this.getProveedor().getId());
		if (Is.emptyString(this.getId())){
			query.setParameter("id", "null");
		}
		else{
			query.setParameter("id", this.getId());
		}
		List<?> list = query.getResultList();
		if (!list.isEmpty()){
			fecha = ((PagoProveedores)list.get(0)).getFecha();
		}
		return fecha;
	}
	
	public void agregarParametrosImpresion(Map<String, Object> parameters) {
		super.agregarParametrosImpresion(parameters);
		
		if (this.getProveedor() != null){
			parameters.put("PROVEEDOR_NOMBRE", this.getProveedor().getNombre());
			parameters.put("PROVEEDOR_CODIGO", this.getProveedor().getCodigo());
		}
		else{
			parameters.put("PROVEEDOR_NOMBRE", "");
			parameters.put("PROVEEDOR_CODIGO", "");
		}
		
		parameters.put("TOTAL", this.getTotal());
		parameters.put("MONEDA", this.getMoneda().getNombre());
		parameters.put("VALORES", this.getValoresTotal());
		parameters.put("APAGAR", this.getaPagar());
		parameters.put("RETENCIONES", this.getRetencionesTotal());
		parameters.put("SALDO", this.getSaldoComprobantes());
		
		String totalLetras = "";
		try{
			totalLetras = NumberToLetterConverter.convertNumberToLetter(this.getTotal());
		}
		catch(Exception e){
		}
		parameters.put("TOTALLETRAS", totalLetras);
	}
	
	@Override
	public void CtaCteReferenciarCuentaCorriente(CuentaCorriente ctacte) {
		this.setCtacte((CuentaCorrienteCompra)ctacte);		
	}
	
	@Override
	public OperadorComercial operadorFinanciero(){
		return this.getProveedor();
	}
	
	@Override
	public Integer CtaCteCoeficiente() {
		return -1;
	}
	
	@Override
	public boolean generaCtaCte(){
		return true;
	}
	
	@Override
	protected boolean agregarItemDesdeMultiseleccion(Map<?, ?> key, Map<String, Object> itemsMultiseleccion){
		try {
			Valor cheque = (Valor)MapFacade.findEntity("Valor", key);
			this.agregarCheque(cheque);
			return true;
		} catch (Exception e) {
			String error = e.getMessage();
			if (Is.emptyString(error)) error = e.toString();
			throw new ValidationException("Error al agregar cheque: " + error);
		}
		
	}
	
	private boolean agregarCheque(Valor cheque){
		boolean chequeRepetido = false;
		for(ItemPagoProveedores item: this.getValores()){
			if (item.getReferencia().equals(cheque)){
				chequeRepetido = true;
				break;
			}
		}		
		if (!chequeRepetido){
			this.crearItemPagoProveedores(cheque);
			
		}
		return !chequeRepetido;
	}
	
	private ItemPagoProveedores crearItemPagoProveedores(Valor valor){
		ItemPagoProveedores item = new ItemPagoProveedores();
		item.setPago(this);
		item.setEmpresa(valor.getEmpresa());
		item.setOrigen(valor.getTesoreria());
		item.setTipoValor(valor.getTipoValor());
		item.setReferencia(valor);
		item.setImporteOriginal(valor.getImporte());
		item.setDetalle(valor.getDetalle());
		
		item.recalcular();
		this.getValores().add(item);
		XPersistence.getManager().persist(item);
		return item;
	}
	
	@Override
	@Hidden
	public Sucursal getSucursalDestino() {
		return null;
	}
	
	private BigDecimal convertirMonedaRetencion(BigDecimal importeRetencion){
		// Siempre el importe de la retención se calcula en moneda de la contabilidad, es decir, pesos
		return this.convertirImporteEnMonedaTr(this.getMoneda1(), importeRetencion);
	}
	
	@Override
	public EmpresaExterna ContabilidadOriginante() {
		EmpresaExterna empresaExterna = null;
		if (this.getProveedor() != null){
			empresaExterna = XPersistence.getManager().find(EmpresaExterna.class, this.getProveedor().getId());
		}
		return empresaExterna;		
	}
	
	@Override
	public String viewName(){
		String viewName = super.viewName();
		if (Is.equal(this.getEstado(), Estado.Abierta) && this.soloLectura()){
			if (this.soloLectura()){
				// cuando esta abierta en estado solo lectura, habilitamos esta opción para modificar la fecha del comprobante
				viewName = Transaccion.VISTACAMBIOATRIBUTOSESPECIALES;
			}
		}
		return viewName;
	}
	
	@Override
	public BigDecimal ContabilidadTotal() {
		return this.calcularImporte1(this.getTotal());
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
	
	public void recalcularSaldoAPagar(){
		BigDecimal aPagar = this.getSaldoComprobantes();
		this.setaPagar(aPagar);
	}
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Transaccion")
	private CuentaCorrienteCompra ctacte;

	public CuentaCorrienteCompra getCtacte() {
		return ctacte;
	}

	public void setCtacte(CuentaCorrienteCompra ctacte) {
		this.ctacte = ctacte;
	}
}
