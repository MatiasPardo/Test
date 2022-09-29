package org.openxava.tesoreria.model;

import java.math.*;
import java.text.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.compras.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.filter.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;



@Entity

@Views({
	@View(name="Simple", members="numero, importe"),
	@View(name="SimpleCheque", members="numero, importe, fechaEmision, fechaVencimiento"),
	@View(name="Efectivo", 
		members="empresa;" +
				"tesoreria;" + 
				"tipoValor, moneda;" +
				"importe"),
	@View(name="ChequePropio", members=
		"Principal{" + 
			"empresa;" +
			"tesoreria;" + 
			"Principal[tipoValor, moneda;" +
			"importe;" + 
			"estado;" + 
			"detalle];" + 
			"Datos[numero, banco;" + 
			"fechaEmision, fechaVencimiento;" + 
			"firmante, cuitFirmante, nroCuentaFirmante];" + 
			"proveedor;" + 
		"}" + 
		"Seguimiento{seguimiento}" +
		"Auditoria{usuario, fechaCreacion; auditoria}"
			),
	@View(name="ChequeTercero", members=
		"Principal{" +
			"empresa;" +
			"tesoreria;" + 
			"Principal[tipoValor, moneda;" +
			"importe;" + 
			"estado;" + 
			"detalle];" + 
			"Datos[numero, banco;" + 
			"fechaEmision, fechaVencimiento;" + 
			"firmante, cuitFirmante, nroCuentaFirmante];" + 
			"cliente; proveedor;" + 
		"}" + 
		"Seguimiento{seguimiento}" +
		"Auditoria{usuario, fechaCreacion; auditoria}"
			),
	@View(name="RechazoChequeTercero", members=
		"numero, importe, moneda, estado;" +
		"Datos[banco;" + 
		"fechaEmision, fechaVencimiento;" + 
		"firmante, cuitFirmante, nroCuentaFirmante];" +
		"tesoreria;" + 
		"empresa;" +  
		"cliente; " +
		"proveedor;" + 
		"seguimiento;" 	
		),
	@View(members="empresa;" +
			"tesoreria;" + 
			"Principal[tipoValor, moneda;" +
			"importe;" + 
			"estado;" + 
			"detalle];" + 
			"Datos[numero, banco;" + 
			"fechaEmision, fechaVencimiento;" + 
			"firmante, cuitFirmante, nroCuentaFirmante];"),
	@View(name="CambioVencimiento", members="fechaVencimiento"),
	@View(name="CambioNumero", members="numero"),
})

@Tabs({
	@Tab(
		properties="tipoValor.nombre, estado, numero, tesoreria.nombre, tipoValor.moneda.nombre, importe, empresa.nombre, detalle, fechaCreacion, usuario",	
		filter=SucursalEmpresaFilter.class,		
		baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL + Valor.BASECONDITION_VALORESACTIVOS),
	@Tab(
		name="ValoresEfectivo",
		properties="tipoValor.nombre, tesoreria.nombre, tipoValor.moneda.nombre, importe, empresa.nombre, detalle, fechaCreacion, usuario",
		filter=SucursalEmpresaFilter.class,		
		baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL + Valor.BASECONDITION_VALORESACTIVOS + " and e.tipoValor.comportamiento = 0 and e.anulado = 'f'"),	
	@Tab(
		name="ValoresEnCartera",
		properties="tipoValor.nombre, numero, tesoreria.nombre, tipoValor.moneda.nombre, importe, empresa.nombre, detalle, fechaCreacion, usuario, cliente.nombre, proveedor.nombre",
		filter=SucursalEmpresaFilter.class,		
		baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL + Valor.BASECONDITION_VALORESACTIVOS + " and e.estado = 0 and e.tipoValor.comportamiento != 0",
		defaultOrder="${fechaVencimiento} asc"),
	@Tab(
		name="ValoresTerceros",
		properties="tipoValor.nombre, numero, tesoreria.nombre, tipoValor.moneda.nombre, importe, empresa.nombre, detalle, fechaCreacion, usuario, cliente.nombre",
		filter=SucursalEmpresaFilter.class,		
		baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL + Valor.BASECONDITION_VALORESACTIVOS + " and e.estado = 0 and e.tipoValor.comportamiento = 1",
		defaultOrder="${fechaVencimiento} asc"),
	@Tab(
		name="ValoresPropios",
		properties="tipoValor.nombre, numero, tesoreria.nombre, tipoValor.moneda.nombre, importe, empresa.nombre, detalle, fechaCreacion, usuario, proveedor.nombre",
		filter=SucursalEmpresaFilter.class,		
		baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL + Valor.BASECONDITION_VALORESACTIVOS + " and e.estado = 0 and e.tipoValor.comportamiento = 2",
		defaultOrder="${fechaVencimiento} asc"),
	@Tab(
		name="ValoresHistorico",
		properties="tipoValor.nombre, numero, tesoreria.nombre, tipoValor.moneda.nombre, importe, empresa.nombre, detalle, fechaCreacion, usuario, cliente.nombre, proveedor.nombre",
		filter=SucursalEmpresaFilter.class,		
		baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL + Valor.BASECONDITION_VALORESACTIVOS + " and e.estado = 1 and e.tipoValor.comportamiento != 0",
		defaultOrder="${fechaVencimiento} desc"),
	@Tab(
		name="ValoresAnulados",
		properties="tipoValor.nombre, numero, tesoreria.nombre, tipoValor.moneda.nombre, importe, empresa.nombre, detalle, fechaCreacion, usuario",
		filter=SucursalEmpresaFilter.class,		
		baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL + Valor.BASECONDITION_VALORESACTIVOS + " and e.estado = 2",
		defaultOrder="${fechaVencimiento} desc"),
	@Tab(
		name="ValoresRechazados",
		properties="tipoValor.nombre, numero, tesoreria.nombre, tipoValor.moneda.nombre, importe, empresa.nombre, detalle, fechaCreacion, usuario",
		filter=SucursalEmpresaFilter.class,		
		baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL + Valor.BASECONDITION_VALORESACTIVOS + " and e.estado = 3",
		defaultOrder="${fechaVencimiento} desc"),
	@Tab(name="ChequesParaDepositar", 
		properties="numero, importe, fechaVencimiento, moneda.nombre, tesoreria.nombre, empresa.nombre, detalle, tipoValor.nombre",
		filter=SucursalEmpresaFilter.class, 
		baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL + Valor.BASECONDITION_VALORESACTIVOS + "  and e.tipoValor.comportamiento = 1 and e.estado = 0",
		defaultOrder="${fechaVencimiento} asc"),
	@Tab(name="ChequesParaPagar", 
		properties="numero, importe, fechaVencimiento, moneda.nombre, tesoreria.nombre, empresa.nombre, detalle, tipoValor.nombre",
		filter=SucursalEmpresaFilter.class, 
		baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL + Valor.BASECONDITION_VALORESACTIVOS + "  and e.tipoValor.comportamiento = 1 and e.estado = 0",
		defaultOrder="${fechaVencimiento} asc")	
})

public class Valor extends ObjetoNegocio{

	public static final String BASECONDITION_VALORESACTIVOS = " and (e.tesoreria.activo = 't' or e.importe != 0)";
	
	public static Valor buscarValorGeneradoPor(Transaccion tr, IItemMovimientoValores movimientoValores) {
		TipoValorConfiguracion tipoValor = movimientoValores.getTipoValor();
		String sql = "from Valor v where v.tipoValor.id = :tipoValor and idTransaccion = :transaccion";
		String numero = null;
		if (tipoValor.getComportamiento().equals(TipoValor.ChequeTercero)){
			sql.concat(" and numero = :numero");
			numero = movimientoValores.getNumeroValor();
		}
		
		Query query = XPersistence.getManager().createQuery(sql);
		query.setParameter("tipoValor", tipoValor.getId());
		query.setParameter("transaccion", tr.getId());
		if (numero != null){
			query.setParameter("numero", numero);
		}
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<Valor> result = (List<Valor>)query.getResultList();
		Valor valorEncontrado = null;
		if (!result.isEmpty()){
			valorEncontrado = (Valor)result.get(0);
		}
		return valorEncontrado;
	}
	
	public static boolean existeCheque(TipoValorConfiguracion tipoValor, String numero, Banco banco, Empresa empresa) {
		String sql = "from Valor v where v.numero = :numero and v.tipoValor.id = :tipoValor and v.anulado = :anulado";
		if (banco != null){
			sql += " and v.banco.id = :banco";
		}
		else{
			sql += " and v.banco is null";
		}
		Query query = XPersistence.getManager().createQuery(sql);
		query.setParameter("tipoValor", tipoValor.getId());
		query.setParameter("anulado", Boolean.FALSE);
		query.setParameter("numero", numero);
		if (banco != null){
			query.setParameter("banco", banco.getId());
		}
		
		List<?> result = query.getResultList();
		return !result.isEmpty();
	}
	
	public static Valor crearValor(Transaccion transaccion, IItemMovimientoValores movimientoValores){
		Valor nuevo = new Valor();
		nuevo.setBanco(movimientoValores.getBanco());
		nuevo.setDetalle(movimientoValores.getDetalle());
		nuevo.setFechaEmision(movimientoValores.getFechaEmision());
		nuevo.setFechaVencimiento(movimientoValores.getFechaVencimiento());
		if (movimientoValores.getTipoValor().getConsolidaAutomaticamente()){
			nuevo.setEstado(EstadoValor.Historico);
		}		
		nuevo.setMoneda(movimientoValores.getTipoValor().getMoneda());
		nuevo.setNumero(movimientoValores.getNumeroValor());
		nuevo.setTesoreria(movimientoValores.tesoreriaAfectada());
		nuevo.setTipoValor(movimientoValores.getTipoValor());
		nuevo.setEmpresa(movimientoValores.getEmpresa());
		nuevo.setFirmante(movimientoValores.getFirmante());
		nuevo.setCuitFirmante(movimientoValores.getCuitFirmante());
		nuevo.setNroCuentaFirmante(movimientoValores.getNroCuentaFirmante());
		nuevo.setIdTransaccion(transaccion.getId());
		nuevo.setTipoTransaccion(transaccion.getClass().getSimpleName());
		BigDecimal importe = movimientoValores.importeOriginalValores();
		nuevo.setImporte(importe);
		movimientoValores.asignarOperadorComercial(nuevo, transaccion);
		XPersistence.getManager().persist(nuevo);
		
		return nuevo;
	}
	
	public static Valor crearValorConsolidacion(TipoValorConfiguracion tipoValor, Tesoreria tesoreria, Empresa empresa){
		Valor nuevo = new Valor();
		//nuevo.setHistorico(Boolean.FALSE);
		nuevo.setEstado(EstadoValor.EnCartera);
		nuevo.setTesoreria(tesoreria);
		nuevo.setMoneda(tipoValor.getMoneda());
		nuevo.setTipoValor(tipoValor);
		nuevo.setEmpresa(empresa);
		XPersistence.getManager().persist(nuevo);
		
		return nuevo;
	}
	
	public static void actualizarValores(Transaccion transaccion, List<IItemMovimientoValores> movimientosValores,	Collection<MovimientoValores> detalleMovFinancieros, boolean revierte) {
		// se buscan los valores que se deben consolidar, para bloquearlos
		
		List<IItemMovimientoValores> valores = new LinkedList<IItemMovimientoValores>();
		
		for(IItemMovimientoValores item: movimientosValores){
			if (item.getEmpresa() == null){
				item.setEmpresa(transaccion.getEmpresa());
			}
			if (item.getEmpresa() == null){
				throw new ValidationException("Empresa no asignada");
			}
			else if (!item.tesoreriaAfectada().getEmpresa().equals(item.getEmpresa())){
				throw new ValidationException("No coincide la empresa " + item.getEmpresa().toString() + " de " + item.tesoreriaAfectada().getNombre());
			}
			
			if (item.tesoreriaAfectada().permiteTipoValor(item.getTipoValor())){
				item.tipoMovimientoValores(revierte).validarAtributosValores(item);
			}
			else{
				throw new ValidationException(item.tesoreriaAfectada().toString() + " no permite el tipo de valor " + item.getTipoValor().toString());
			}
			TipoValor comportamiento = item.getTipoValor().getComportamiento();
						
			if (comportamiento.equals(TipoValor.ChequePropio)){
				if (item.getTipoValor().getConsolidaAutomaticamente()){
					// se agrega un movimiento para debitar el efectivo automáticamente. 
					ItemTransEfectivo itemMovEfectivo = new ItemTransEfectivo();
					itemMovEfectivo.setTipoValorEfectivo(item.tipoMovimientoValores(revierte).tipoValorConsolida(item.tesoreriaAfectada(), item.getTipoValor()));
					itemMovEfectivo.setImporteEfectivo(item.importeOriginalValores().multiply(item.getTipoValor().getComportamiento().coeficienteConsolidacion()));
					itemMovEfectivo.setGeneradoPor(item);					
					// como el cheque ya genera un movimiento financiero, este se inhibe
					itemMovEfectivo.setNoGenerarDefalle(true);
					valores.add(itemMovEfectivo);
				}
			}			
		}
		
		ComparatorItemMovValores comparator = new ComparatorItemMovValores();
		comparator.setReversion(revierte);
		valores.addAll(movimientosValores);
		valores.sort(comparator);
		
		Map<String, Object> procesados = new HashMap<String, Object>();
		Map<String, Object> bloqueos = new HashMap<String, Object>();
		
		Map<String, Valor> efectivosNegativos = new HashMap<String, Valor>();
		for (IItemMovimientoValores item: valores){
			TipoMovimientoValores tipoMovimiento = item.tipoMovimientoValores(revierte);
			tipoMovimiento.bloquearValorParaActualizar(item, bloqueos);
			Valor valor = tipoMovimiento.actualizarValor(item, transaccion, procesados);
			if (valor != null){
				if (item.referenciaValor() == null){
					item.asignarReferenciaValor(valor);
				}
				else{
					valor.copiarAtributosValoresEnItem(item);
				}
				
				if (valor.getTipoValor().getComportamiento().equals(TipoValor.Efectivo)){
					if (!efectivosNegativos.containsKey(valor.getId())){
						efectivosNegativos.put(valor.getId(), valor);
					}
				}
			}
			if (!item.noGenerarDetalle()){
				detalleMovFinancieros.add(Valor.crearDetalleMovimientoFinanciero(transaccion, item, revierte));
			}
		}
		
		for(Valor efectivo: efectivosNegativos.values()){
			if (efectivo.getImporte().compareTo(BigDecimal.ZERO) < 0){
				if (!efectivo.getTesoreria().getPermitirEfectivoNegativo() && !efectivo.getTipoValor().getPermitirNegativo()){
					throw new ValidationException(efectivo.toString() + " saldo en negativo: " + UtilERP.convertirString(efectivo.getImporte()));
				}
			}
		}
	}	
	
	public void copiarAtributosValoresEnItem(IItemMovimientoValores item) {
		item.setNumeroValor(this.getNumero());
		item.setBanco(this.getBanco());
		item.setFechaEmision(this.getFechaEmision());
		item.setFechaVencimiento(this.getFechaVencimiento());
		if (Is.emptyString(item.getDetalle())){
			item.setDetalle(this.getDetalle());
		}
	}

	private static MovimientoValores crearDetalleMovimientoFinanciero(Transaccion transaccion, IItemMovimientoValores item, boolean revierte) {
		MovimientoValores detalle = new MovimientoValores();
		detalle.setAnulacion(revierte);
		detalle.setDetalle(item.getDetalle());
		detalle.setEmpresa(item.getEmpresa());
		detalle.setFechaComprobante(transaccion.getFecha());
		detalle.setFechaEmision(item.getFechaEmision());
		detalle.setFechaVencimiento(item.getFechaVencimiento());
		detalle.setNumero(item.getNumeroValor());
		detalle.setIdTransaccion(transaccion.getId());
		detalle.setNumeroComprobante(transaccion.getNumero());
		detalle.setTipoComprobante(transaccion.descripcionTipoTransaccion());
		detalle.setTipoTrDestino(transaccion.getClass().getSimpleName());
		detalle.setTesoreria(item.tesoreriaAfectada());
		detalle.setTipoValor(item.getTipoValor());
		detalle.setMoneda(item.getTipoValor().getMoneda());
		detalle.setMoneda1(transaccion.getMoneda1());
		detalle.setMoneda2(transaccion.getMoneda2());
		detalle.setUsuario(Users.getCurrent());
		ObjetoNegocio itemTrValores = item.itemTrValores();
		if (itemTrValores != null){
			detalle.setIdItem(itemTrValores.getId());
			detalle.setTipoItem(itemTrValores.getClass().getSimpleName());
		}
		
		OperadorComercial operador = item.operadorComercialValores(transaccion);
		if (operador != null){
			if (operador instanceof Cliente){
				detalle.setCliente((Cliente)operador);
			}
			else if (operador instanceof Proveedor){
				detalle.setProveedor((Proveedor)operador);
			}
			else{
				throw new ValidationException("El objeto no implementa Cliente/Proveedor");
			}
		}
		detalle.setConcepto(item.conceptoTesoreria());
		detalle.setValor(item.referenciaValor());
		
		// Importes y cotizaciones
		TipoMovimientoValores tipoMov = item.tipoMovimientoValores(revierte);
				
		BigDecimal coeficiente = tipoMov.coeficiente(item);
		detalle.setImporteOriginal(item.importeOriginalValores().multiply(coeficiente));		
		
		BigDecimal importeMonedaTr = item.importeMonedaTrValores(transaccion).multiply(coeficiente);
		if (detalle.getMoneda().equals(detalle.getMoneda1())){
			detalle.setImporteMoneda1(detalle.getImporteOriginal());
		}
		else{
			detalle.setImporteMoneda1(importeMonedaTr.multiply(transaccion.buscarCotizacionTrConRespectoA(transaccion.getMoneda1())).setScale(2, RoundingMode.HALF_EVEN));
		}
		if (detalle.getMoneda().equals(detalle.getMoneda2())){
			detalle.setImporteMoneda2(detalle.getImporteOriginal());
		}
		else{
			if (detalle.getMoneda().equals(detalle.getMoneda1())){
				detalle.setImporteMoneda2(detalle.getImporteMoneda1().divide(transaccion.getCotizacion2(), 2, RoundingMode.HALF_EVEN));				
			}
			else{
				detalle.setImporteMoneda2(importeMonedaTr.multiply(transaccion.buscarCotizacionTrConRespectoA(transaccion.getMoneda2())).setScale(2, RoundingMode.HALF_EVEN));				
			}
		}				
		return detalle;
	}

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	@DescriptionsList(descriptionProperties="nombre")
	private Empresa empresa;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	@ReferenceView(value="Simple")
	private Tesoreria tesoreria;
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private Sucursal sucursal;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@SearchKey
	private TipoValorConfiguracion tipoValor;
	
	@Stereotype("MONEY")
	@ReadOnly
	private BigDecimal importe;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	@DescriptionsList(descriptionProperties="nombre")
	private Moneda moneda;
	
	@ReadOnly
	@Required
	private EstadoValor estado = EstadoValor.EnCartera;
	
	@ReadOnly
	private Boolean historico = Boolean.FALSE;
	
	@ReadOnly
	private Boolean anulado = Boolean.FALSE;
	
	@Column(length=30)
	@ReadOnly(notForViews="CambioNumero")
	@SearchKey	
	@Action(notForViews="CambioNumero, RechazoChequeTercero, Simple, SimpleCheque", value="ModificarNumeroValor.Cambiar", alwaysEnabled=true)
	private String numero;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@ReadOnly
	private Banco banco;
	
	@ReadOnly
	private Date fechaEmision = new Date();
	
	@ReadOnly(notForViews="CambioVencimiento")
	@Action(notForViews="CambioVencimiento, RechazoChequeTercero, Simple, SimpleCheque", value="ModificarVencimientoValor.Cambiar", alwaysEnabled=true)	
	private Date fechaVencimiento = new Date();
	
	@ReadOnly
	@Column(length=100)
	private String detalle;
	
	@Hidden @ReadOnly
	@Column(length=32)
	private String idTransaccion;

	@Hidden @ReadOnly
	@Column(length=100)
	private String tipoTransaccion;
	
	@Column(length=50)
	@ReadOnly
	private String firmante;
	
	@Column(length=20)
	@ReadOnly
	private String cuitFirmante;
	
	@Column(length=50)
	@ReadOnly
	private String nroCuentaFirmante;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	@ReferenceView("Simple")
	@ReadOnly
	private Cliente cliente;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	@ReferenceView("Simple")
	@ReadOnly
	private Proveedor proveedor;
	
	public Tesoreria getTesoreria() {
		return tesoreria;
	}

	public void setTesoreria(Tesoreria tesoreria) {
		this.tesoreria = tesoreria;
		if (tesoreria != null){
			this.setSucursal(tesoreria.getSucursal());
		}
		else{
			this.setSucursal(null);
		}
	}

	public TipoValorConfiguracion getTipoValor() {
		return tipoValor;
	}

	public void setTipoValor(TipoValorConfiguracion tipoValor) {
		this.tipoValor = tipoValor;
		if (tipoValor != null){
			tipoValor.setMoneda(tipoValor.getMoneda());
		}
	}

	public BigDecimal getImporte() {
		return importe == null ? BigDecimal.ZERO: importe;
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}

	public Moneda getMoneda() {
		return moneda;
	}

	public void setMoneda(Moneda moneda) {
		if (this.getTipoValor() != null){
			if (this.getTipoValor().getMoneda() == null){
				this.moneda = moneda;
			}
		}
		else{
			this.moneda = moneda;
		}
	}
	
	public EstadoValor getEstado() {
		return estado;
	}

	public void setEstado(EstadoValor estado) {
		this.estado = estado;
		if (estado != null){
			if (estado.equals(EstadoValor.EnCartera)){
				this.setHistorico(Boolean.FALSE);
				this.setAnulado(Boolean.FALSE);
			}
			else if (estado.equals(EstadoValor.Historico)){
				this.setHistorico(Boolean.TRUE);
				this.setAnulado(Boolean.FALSE);
			}
			else if (estado.equals(EstadoValor.Anulado)){
				this.setHistorico(Boolean.TRUE);
				this.setAnulado(Boolean.TRUE);
			}
			else if (estado.equals(EstadoValor.Rechazado)){
				this.setHistorico(Boolean.TRUE);
				this.setAnulado(Boolean.FALSE);
			}
		}
	}

	public Boolean getHistorico() {
		return historico;
	}

	public void setHistorico(Boolean historico) {
		this.historico = historico;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public Date getFechaEmision() {
		return fechaEmision;
	}

	public void setFechaEmision(Date fechaEmision) {
		this.fechaEmision = fechaEmision;
	}

	public Date getFechaVencimiento() {
		return fechaVencimiento;
	}

	public void setFechaVencimiento(Date fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}

	public String getDetalle() {
		return detalle;
	}

	public void setDetalle(String detalle) {
		this.detalle = detalle;
	}

	public Banco getBanco() {
		return banco;
	}

	public void setBanco(Banco banco) {
		this.banco = banco;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public Boolean getAnulado() {
		return anulado;
	}

	public void setAnulado(Boolean anulado) {
		this.anulado = anulado;
	}

	public String getIdTransaccion() {
		return idTransaccion;
	}

	public void setIdTransaccion(String idTransaccion) {
		this.idTransaccion = idTransaccion;
	}

	public String getTipoTransaccion() {
		return tipoTransaccion;
	}

	public void setTipoTransaccion(String tipoTransaccion) {
		this.tipoTransaccion = tipoTransaccion;
	}

	@Override
	public String toString(){
		String texto = "";
		if (this.getTipoValor() != null){
			texto = texto.concat(this.getTipoValor().getNombre()).concat(" ");
		}
		if (!Is.emptyString(this.getNumero())){
			texto = texto.concat(this.getNumero());
		}
		return texto;
	}

	public String getFirmante() {
		return firmante;
	}

	public void setFirmante(String firmante) {
		this.firmante = firmante;
	}

	public String getCuitFirmante() {
		return cuitFirmante;
	}

	public void setCuitFirmante(String cuitFirmante) {
		this.cuitFirmante = cuitFirmante;
	}

	public String getNroCuentaFirmante() {
		return nroCuentaFirmante;
	}

	public void setNroCuentaFirmante(String nroCuentaFirmante) {
		this.nroCuentaFirmante = nroCuentaFirmante;
	}
	
	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}

	public String viewName(){
		 TipoValor comportamiento = this.getTipoValor().getComportamiento();
		 
		 if (comportamiento.equals(TipoValor.Efectivo) || comportamiento.equals(TipoValor.TarjetaCreditoCobranza) ){
			 return "Efectivo";
		 }
		 else if (comportamiento.equals(TipoValor.ChequePropio)){
			 return "ChequePropio";
		 }
		 else if (comportamiento.equals(TipoValor.ChequeTercero)){
			 return "ChequeTercero";
		 }
		 else{
			 return super.viewName();
		 }
	}
	
	@ReadOnly
	@ListProperties("fechaCreacion, usuario, modificacion, valorAnterior, valorNuevo")
	@OrderBy("fechaCreacion desc")
	@OneToMany(mappedBy="referencia")
	private Collection<AuditoriaValor> auditoria;
	
	public Collection<AuditoriaValor> getAuditoria() {
		return auditoria;
	}

	public void setAuditoria(Collection<AuditoriaValor> auditoria) {
		this.auditoria = auditoria;
	}

	@ReadOnly
	@ListProperties("fechaComprobante, tipoComprobante, numeroComprobante, importeOriginal, anulacion, tesoreria.nombre, empresa.nombre, detalle, usuario, fechaCreacion")
	@OrderBy("fechaComprobante desc")
	public Collection<MovimientoValores> getSeguimiento(){		
		if (!Is.emptyString(this.getId()) && (
				this.getTipoValor().getComportamiento().equals(TipoValor.ChequePropio) ||
				this.getTipoValor().getComportamiento().equals(TipoValor.ChequeTercero))
		){
			//Query query = XPersistence.getManager().createQuery("from MovimientoValores m where (m.tipoValor.id = :tipoValor or m.tipoValor.comportamiento = :efectivo) and m.numero = :numero and abs(m.importeOriginal) = :importe order by m.fechaComprobante desc, m.fechaCreacion desc");
			//query.setParameter("tipoValor", this.getTipoValor().getId());
			//query.setParameter("numero", this.getNumero());
			//query.setParameter("efectivo", TipoValor.Efectivo);
			//query.setParameter("importe", this.getImporte());
			Query query = XPersistence.getManager().createQuery("from MovimientoValores m where m.valor.id = :valor order by m.fechaComprobante desc, m.fechaCreacion desc");
			query.setParameter("valor", this.getId());
			try{
				@SuppressWarnings("unchecked")
				List<MovimientoValores> resultado = (List<MovimientoValores>)query.getResultList();
				Collection<MovimientoValores> seguimiento = new ArrayList<MovimientoValores>();
				seguimiento.addAll(resultado);
				return seguimiento;
			}
			catch(Exception e){
				return Collections.emptyList();
			}
		}
		else{
			return Collections.emptyList();
		}
		
		
	}
	
	
	public void cambiarNumero(String numero){
		if (!Is.equalAsString(this.getNumero(), numero)){
			if (!Valor.existeCheque(this.getTipoValor(), numero, this.getBanco(), this.getEmpresa())){
				Collection<MovimientoValores> seguimiento = this.getSeguimiento();
				if (!seguimiento.isEmpty()){
					this.bloquearValorParaModificar();
					
					AuditoriaValor auditoria = crearAuditoriaCambioValor(TipoModificacionValor.Numero);
					auditoria.setValorAnterior(this.getNumero());
					auditoria.setValorNuevo(numero);
					
					this.setNumero(numero);
					for(MovimientoValores movimientoValores: seguimiento){
						movimientoValores.setNumero(numero);
						IItemMovimientoValores generadoPor = movimientoValores.generadaPorItemMovValores();
						if (generadoPor != null){
							generadoPor.setNumeroValor(numero);
						}
					}	
				}
				else{
					throw new ValidationException("No se encontró trazabilidad para el valor");
				}
			}
			else{
				throw new ValidationException("Ya existe un cheque con el número " + numero);
			}
		}
		else{
			throw new ValidationException("Mismo número");
		}
	}
	
	public void cambiarFechaVencimiento(Date vencimiento){
		if (!Is.equalAsString(this.getFechaVencimiento(), vencimiento)){			
			Collection<MovimientoValores> seguimiento = this.getSeguimiento();
			if (!seguimiento.isEmpty()){
				this.bloquearValorParaModificar();
				
				AuditoriaValor auditoria = crearAuditoriaCambioValor(TipoModificacionValor.FechaVencimiento);
				SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
				
				auditoria.setValorAnterior(format.format(this.getFechaVencimiento()));
				auditoria.setValorNuevo(format.format(vencimiento));
				
				this.setFechaVencimiento(vencimiento);
				for(MovimientoValores movimientoValores: seguimiento){
					movimientoValores.setFechaVencimiento(vencimiento);
					IItemMovimientoValores generadoPor = movimientoValores.generadaPorItemMovValores();
					if (generadoPor != null){
						generadoPor.setFechaVencimiento(vencimiento);
					}
				}	
			}
			else{
				throw new ValidationException("No se encontró trazabilidad para el valor");
			}
		}
		else{
			throw new ValidationException("Mismo fecha vencimiento");
		}
	}
	
	private AuditoriaValor crearAuditoriaCambioValor(TipoModificacionValor tipo){
		AuditoriaValor auditoria = new AuditoriaValor();
		auditoria.setReferencia(this);
		auditoria.setModificacion(tipo);
		XPersistence.getManager().persist(auditoria);
		return auditoria;
	}
	
	private void bloquearValorParaModificar(){
		String sql = "select v.id from " +  Esquema.concatenarEsquema("Valor") + " v where v.id = :id for update";
		Query query = XPersistence.getManager().createNativeQuery(sql);
		query.setParameter("id", this.getId());
		query.setMaxResults(1);
		List<?> result = query.getResultList();
		if (result.isEmpty()){
			throw new ValidationException("No se pudo bloquear el valor para actualizar");
		}
	}
	
	public Transaccion trOrigen(){
		if (!Is.emptyString(this.getIdTransaccion())){
			Query query = XPersistence.getManager().createQuery("from " + this.getTipoTransaccion() + " where id = :id");
			query.setParameter("id", this.getIdTransaccion());
			query.setMaxResults(1);
			return (Transaccion)query.getSingleResult();
		}
		else{
			return null;
		}
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public TransferenciaFinanzas crearTransferenciaValores(Tesoreria destino) {
		if (this.getEmpresa().equals(destino.getEmpresa())){
			if (!this.getTesoreria().equals(destino)){
				TransferenciaFinanzas transferencia = new TransferenciaFinanzas();
				transferencia.setEmpresa(destino.getEmpresa());
				transferencia.setOrigen(this.getTesoreria());
				transferencia.setDestino(destino);
				transferencia.setTipoValor(this.getTipoValor());
				transferencia.setMoneda(this.getTipoValor().getMoneda());
				TipoValor comportamiento = this.getTipoValor().getComportamiento();
				if (comportamiento.equals(TipoValor.Efectivo) || comportamiento.equals(TipoValor.TarjetaCreditoCobranza)){
					transferencia.setImporteOriginal(this.getImporte());
				}
				else{
					transferencia.setReferencia(this);
				}
				XPersistence.getManager().persist(transferencia);
				return transferencia;
			}
			else{
				throw new ValidationException("Misma tesorería: " + this.toString());
			}
		}
		else{
			throw new ValidationException("No coinciden las empresas: " + this.toString());
		}
	}
}
