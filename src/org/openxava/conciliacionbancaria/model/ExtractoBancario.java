package org.openxava.conciliacionbancaria.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Query;

import org.openxava.annotations.CollectionView;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.ListProperties;
import org.openxava.annotations.PreDelete;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.RowStyle;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;
import org.openxava.base.model.ItemTransaccion;
import org.openxava.base.model.Transaccion;
import org.openxava.base.model.UtilERP;
import org.openxava.compras.model.Proveedor;
import org.openxava.jpa.XPersistence;
import org.openxava.tesoreria.model.ConceptoTesoreria;
import org.openxava.tesoreria.model.CuentaBancaria;
import org.openxava.tesoreria.model.EgresoFinanzas;
import org.openxava.tesoreria.model.IngresoFinanzas;
import org.openxava.tesoreria.model.ItemEgresoFinanzas;
import org.openxava.tesoreria.model.ItemIngresoFinanzas;
import org.openxava.tesoreria.model.ItemPagoProveedores;
import org.openxava.tesoreria.model.ItemReciboCobranza;
import org.openxava.tesoreria.model.MovimientoValores;
import org.openxava.tesoreria.model.PagoProveedores;
import org.openxava.tesoreria.model.ReciboCobranza;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.Cliente;

@Entity

@Views({
	@View(members="Principal{" + 
			"resumen;" +
			"fecha, concepto;" + 
			"importe, credito, debito, saldo;" + 
			"observaciones;" + 
		"}" + 
		"Conciliacion{" +
			"conciliado, tipoConciliacion;" +
			"movimientoValoresConciliados;" + 
		"}"),
	@View(name="Simple", 
		members="fecha, concepto, importe;" +
				"observaciones")
})

@Tab(properties="resumen.cuenta.nombre, fecha, concepto, importe, credito, debito, saldo, observaciones, conciliado, resumen.cuenta.codigo", 
	defaultOrder="fecha desc, nroFila asc",
	rowStyles={	@RowStyle(style="pendiente-ejecutado", property="conciliado", value="true")})

public class ExtractoBancario implements IObjetoConciliable{
	
	public static void conciliar(Collection<MovimientoValores> movimientosAConciliar, Collection<ExtractoBancario> extractoAConciliar) {
		
		for(IObjetoConciliable objeto: movimientosAConciliar){
			if (objeto.getConciliado()){
				throw new ValidationException("Movimiento financiero " + objeto.toString() + " ya esta conciliado");
			}
		}
		for(IObjetoConciliable objeto: extractoAConciliar){
			if (objeto.getConciliado()){
				throw new ValidationException("Extracto " + objeto.toString() + " ya esta conciliado");
			}
		}
		
		if (extractoAConciliar.isEmpty()){
			if (!movimientosAConciliar.isEmpty()){
				// Conciliar solo los movimientos
				if (movimientosAConciliar.size() == 2){
					Iterator<MovimientoValores> iterator = movimientosAConciliar.iterator();
					ExtractoBancario.conciliarMovimientoValores(iterator.next(), iterator.next());					
				}
				else{
					throw new ValidationException("Para conciliar movimientos entre ellos mismos deben ser de a dos");
				}
			}
			else{
				throw new ValidationException("Debe seleccionar los movimientos que desea conciliar");
			}
		}
		else if (movimientosAConciliar.isEmpty()){
			if (extractoAConciliar.size() == 2){
				Iterator<ExtractoBancario> iterator = extractoAConciliar.iterator(); 
				ExtractoBancario extracto = iterator.next();
				extracto.conciliarCon(iterator.next());
			}
			else{
				throw new ValidationException("Para conciliar el extracto bancario entre ellos mismos deben ser de a dos");
			}
		}
		else{			
			if ((extractoAConciliar.size() == 1) && (movimientosAConciliar.size() == 1)){
				ExtractoBancario extracto = extractoAConciliar.iterator().next();
				MovimientoValores movimiento = movimientosAConciliar.iterator().next();
				extracto.conciliarCon(movimiento);				
			}
			else if (extractoAConciliar.size() == 1){
				// conciliacion extracto 1 a movimientos muchos
				ExtractoBancario extracto = extractoAConciliar.iterator().next();
				extracto.conciliarCon(movimientosAConciliar);				
			}
			else if (movimientosAConciliar.size() == 1){
				// conciliacion extracto muchos a 1 movimiento
				MovimientoValores movimiento = movimientosAConciliar.iterator().next();
				extractoAConciliar.iterator().next().conciliarMovimientos(movimiento, extractoAConciliar);
				
			}
			else{
				// conciliación muchos a muchos
				GrupoConciliacion grupo = new GrupoConciliacion();
							
				BigDecimal totalMov = BigDecimal.ZERO;
				for(MovimientoValores mov: movimientosAConciliar){
					if (grupo.getId() == null){
						grupo.setCuenta(XPersistence.getManager().find(CuentaBancaria.class, mov.getTesoreria().getId()));
						// se debe persistir el grupo, para que tenga un ID
						XPersistence.getManager().persist(grupo);
					}
					
					totalMov = totalMov.add(mov.getImporteOriginal());
					mov.setTipoConciliacion(TipoConciliacionBancaria.Grupo);
					mov.setConciliadoCon(grupo.getId());
					mov.setConciliado(true);					
				}
				
				BigDecimal totalExtracto = BigDecimal.ZERO;
				for(ExtractoBancario ext: extractoAConciliar){
					totalExtracto = totalExtracto.add(ext.getImporte());
					ext.setTipoConciliacion(TipoConciliacionBancaria.Grupo);
					ext.setConciliadoCon(grupo.getId());
					ext.setConciliado(true);
				}
				
				if (totalMov.compareTo(totalExtracto) != 0){
					throw new ValidationException("La sumatoria del extracto bancario " + UtilERP.convertirString(totalExtracto) + " no coincide con la sumatoria de los comprobantes " + UtilERP.convertirString(totalMov));
				}
			}			
		}		
	}
	
	@Id @Hidden
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly 
	@ReferenceView("ExtractoBancario")
	private ResumenExtractoBancario resumen;
	
	@ReadOnly
	private Integer nroFila;
	
	@ReadOnly
	private Date fecha;
	
	@ReadOnly
	@Column(length=100)	
	private String concepto;
	
	@ReadOnly
	private BigDecimal importe;
	
	@ReadOnly
	private BigDecimal debito;
	
	@ReadOnly
	private BigDecimal credito;
	
	@ReadOnly
	private BigDecimal saldo;
	
	@ReadOnly
	@Column(length=100)	
	private String observaciones;
	
	@ReadOnly
	private Boolean conciliado = Boolean.FALSE;
	
	@ReadOnly 
	@Hidden
	private TipoConciliacionBancaria tipoConciliacion;
	
	@ReadOnly 
	@Hidden
	private Long conciliadoCon;
	
	@SuppressWarnings("unchecked")
	@ReadOnly
	@ListProperties("fechaComprobante, tipoComprobante, numeroComprobante, importeOriginal, detalle")
	@CollectionView("Simple") 
	public Collection<MovimientoValores> getMovimientoValoresConciliados(){
		if (this.getConciliado()){
			if (this.getTipoConciliacion().equals(TipoConciliacionBancaria.MovimientoFinanzas)){
				if (this.getConciliadoCon() != null){
					Query query = XPersistence.getManager().createQuery("from MovimientoValores where id = :id");
					query.setParameter("id", this.getConciliadoCon());
					query.setMaxResults(1);
					return query.getResultList();
				}
				else{
					Query query = XPersistence.getManager().createQuery("from MovimientoValores where conciliadoCon = :id");
					query.setParameter("id", this.getId());
					return query.getResultList();
				}	
			}
			else{
				return Collections.EMPTY_LIST;
			}
		}
		else{
			return Collections.EMPTY_LIST;
		}
	}
		
	
	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public String getConcepto() {
		return concepto;
	}

	public void setConcepto(String concepto) {
		int max = 100;
		if ((concepto != null) && (concepto.length() > max)){
			this.concepto = concepto.substring(0, max - 1);
		}
		else{
			this.concepto = concepto;
		}
	}

	public BigDecimal getDebito() {
		return debito;
	}

	public void setDebito(BigDecimal debito) {
		this.debito = debito;
	}

	public BigDecimal getCredito() {
		return credito;
	}

	public void setCredito(BigDecimal credito) {
		this.credito = credito;
	}

	public BigDecimal getSaldo() {
		return saldo;
	}

	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		int max = 100;
		if ((observaciones != null) && (observaciones.length() > max)){
			this.observaciones = observaciones.substring(0, max - 1);
		}
		else{
			this.observaciones = observaciones;
		}
		
	}

	public Boolean getConciliado() {
		return conciliado;
	}

	public void setConciliado(Boolean conciliado) {
		if (conciliado != null){
			this.conciliado = conciliado;
		}
	}

	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}

	public void asignarCreditoDebito(BigDecimal cred, BigDecimal deb) {
		if ((cred != null) && (deb != null)){
			this.setCredito(cred.abs());
			this.setDebito(deb.abs());
			if (cred.compareTo(BigDecimal.ZERO) == 0){
				this.setImporte(this.getDebito().negate());
			}
			else{
				this.setImporte(this.getCredito());
			}
		}
		else{
			throw new ValidationException("Credito y Débito no pueden estar vacios");
		}
	}

	public void asignarImporte(BigDecimal imp) {
		if(imp != null){
			this.setImporte(imp);
			if (imp.compareTo(BigDecimal.ZERO) > 0){
				this.setDebito(BigDecimal.ZERO);
				this.setCredito(imp);
			}
			else{
				this.setDebito(imp.abs());
				this.setCredito(BigDecimal.ZERO);
			}
		}
		else{
			throw new ValidationException("Importe no puede estar vacio");
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ResumenExtractoBancario getResumen() {
		return resumen;
	}

	public void setResumen(ResumenExtractoBancario resumen) {
		this.resumen = resumen;
	}

	public Integer getNroFila() {
		return nroFila;
	}

	public void setNroFila(Integer nroFila) {
		this.nroFila = nroFila;
	}
	
	public String toString(){
		return this.getConcepto().toString() + " Fila " + Integer.toString(this.getNroFila());		
	}
	
	@PreDelete
	public void onPreDelete(){
		if (this.getConciliado()){
			throw new ValidationException("No se puede eliminar el extracto si esta conciliado");
		}
	}

	public TipoConciliacionBancaria getTipoConciliacion() {
		return tipoConciliacion;
	}

	public void setTipoConciliacion(TipoConciliacionBancaria tipoConciliacion) {
		this.tipoConciliacion = tipoConciliacion;
	}

	public Long getConciliadoCon() {
		return conciliadoCon;
	}

	public void setConciliadoCon(Long conciliadoCon) {
		this.conciliadoCon = conciliadoCon;
	}
	
	private void conciliarCon(MovimientoValores movimiento) {
		this.validarImportesConciliacion(movimiento.getImporteOriginal(), this.getImporte());
		
		this.setTipoConciliacion(TipoConciliacionBancaria.MovimientoFinanzas);
		this.setConciliadoCon(movimiento.getId());
		this.setConciliado(true);
		
		movimiento.setTipoConciliacion(TipoConciliacionBancaria.ExtractoBancario);
		movimiento.setConciliadoCon(this.getId());
		movimiento.setConciliado(true);
	}
	
	private void conciliarCon(Collection<MovimientoValores> movimientosAConciliar) {
		this.setTipoConciliacion(TipoConciliacionBancaria.MovimientoFinanzas);
		this.setConciliado(true);
		
		BigDecimal importeMovimientos = BigDecimal.ZERO;
		for(MovimientoValores movimiento: movimientosAConciliar){
			movimiento.setTipoConciliacion(TipoConciliacionBancaria.ExtractoBancario);
			movimiento.setConciliadoCon(this.getId());
			movimiento.setConciliado(true);			
			importeMovimientos = importeMovimientos.add(movimiento.getImporteOriginal()); 
		}
		
		this.validarImportesConciliacion(importeMovimientos, this.getImporte());
	}
	
	private void conciliarMovimientos(MovimientoValores movimiento, Collection<ExtractoBancario> extractoAConciliar) {
		movimiento.setTipoConciliacion(TipoConciliacionBancaria.ExtractoBancario);
		movimiento.setConciliado(true);
		
		BigDecimal importeExtractos = BigDecimal.ZERO;
		for(ExtractoBancario extracto: extractoAConciliar){
			extracto.setTipoConciliacion(TipoConciliacionBancaria.MovimientoFinanzas);
			extracto.setConciliadoCon(movimiento.getId());
			extracto.setConciliado(true);
			importeExtractos = importeExtractos.add(extracto.getImporte());
		}
		this.validarImportesConciliacion(movimiento.getImporteOriginal(), importeExtractos);
	}
	
	private void conciliarCon(ExtractoBancario extracto) {
		BigDecimal neto = extracto.getImporte().add(this.getImporte());
		if (neto.compareTo(BigDecimal.ZERO) == 0){
			this.setTipoConciliacion(TipoConciliacionBancaria.ExtractoBancario);
			this.setConciliadoCon(extracto.getId());
			this.setConciliado(true);
			
			extracto.setTipoConciliacion(TipoConciliacionBancaria.ExtractoBancario);
			extracto.setConciliadoCon(this.getId());
			extracto.setConciliado(true);
		}
		else{
			throw new ValidationException("No coinciden los importes a conciliar: Queda un saldo de " + UtilERP.convertirString(neto));
		}
	}
	
	private static void conciliarMovimientoValores(MovimientoValores mov1, MovimientoValores mov2) {
		BigDecimal neto = mov1.getImporteOriginal().add(mov2.getImporteOriginal());
		if (neto.compareTo(BigDecimal.ZERO) == 0){
			if (mov1.getAnulacion() && mov2.getAnulacion()){
				throw new ValidationException("No se pueden conciliar dos movimientos anulados");								
			}
			else if (!mov1.getAnulacion() && !mov2.getAnulacion()){
				throw new ValidationException("No se puede conciliar dos movimientos que no estén anulados");
			}
			else{				
				if (Is.equalAsString(mov1.getIdItem(), mov2.getIdItem())){
					mov1.setTipoConciliacion(TipoConciliacionBancaria.Anulacion);
					mov1.setConciliadoCon(mov2.getId());
					mov1.setConciliado(true);
					
					mov2.setTipoConciliacion(TipoConciliacionBancaria.Anulacion);
					mov2.setConciliadoCon(mov1.getId());
					mov2.setConciliado(true);
				}
				else{
					throw new ValidationException("Solo se puede conciliar dos movimientos que uno sea la anulación del otro");
				}				
			}
		}
		else{
			throw new ValidationException("No coinciden los importes a conciliar: Queda un saldo de " + UtilERP.convertirString(neto));
		}
	}
	
	private void validarImportesConciliacion(BigDecimal importe1, BigDecimal importe2){
		if (importe1.compareTo(importe2) != 0){
			throw new ValidationException("No coinciden los importes a conciliar: " + UtilERP.convertirString(importe1) + " distinto de " + UtilERP.convertirString(importe2));
		}
	}

	@Override
	public void anularConciliacion() {
		if (this.getConciliado()){
			if (this.getTipoConciliacion().equals(TipoConciliacionBancaria.MovimientoFinanzas)){
				if (this.getConciliadoCon() != null){
					Query query = XPersistence.getManager().createQuery("from MovimientoValores where id = :id");
					query.setParameter("id", this.getConciliadoCon());
					query.setMaxResults(1);
					MovimientoValores mov = (MovimientoValores)query.getSingleResult();
					
					if (mov.getConciliadoCon() == null){
						// se buscan todos los extractos bancarios conciliados con el movimiento, para anularlos
						// Se excluye el extracto this, ya que es el que se esta anulando
						query = XPersistence.getManager().createQuery("from ExtractoBancario where conciliadoCon = :idMovimientoValores and id != :id");
						query.setParameter("idMovimientoValores", mov.getId());
						query.setParameter("id", this.getId());
						List<?> result = query.getResultList();
						for(Object obj: result){
							ExtractoBancario.cancelarConciliacion((IObjetoConciliable)obj);
						}
					}
					// Se anula el movimiento de valor con el que se concilio	
					ExtractoBancario.cancelarConciliacion(mov);
				}
				else{
					// se busca todos los movimientos de valores conciliados con el movimiento financiero y se anulan las conciliaciones
					Query query = XPersistence.getManager().createQuery("from MovimientoValores where conciliadoCon = :id");
					query.setParameter("id", this.getId());
					List<?> result = query.getResultList();
					for(Object res: result){
						ExtractoBancario.cancelarConciliacion((IObjetoConciliable)res);
					}
				}
			}
			else if (this.getTipoConciliacion().equals(TipoConciliacionBancaria.ExtractoBancario)){
				// Se busca el extracto conciliado 
				Query query  = XPersistence.getManager().createQuery("from ExtractoBancario where conciliadoCon = :id");
				query.setParameter("id", this.getId());
				query.setMaxResults(1);
				ExtractoBancario.cancelarConciliacion((IObjetoConciliable)query.getSingleResult());
			}
			
			ExtractoBancario.cancelarConciliacion(this);
		}
		else{
			throw new ValidationException("No esta conciliado");
		}
		
	}
	
	public static void anularConciliacionMovimientoValores(MovimientoValores movimientoValores) {
		if (movimientoValores.getConciliado()){
			if (movimientoValores.getTipoConciliacion().equals(TipoConciliacionBancaria.Anulacion)){
				throw new ValidationException("Es un comprobante anulado, no se puede anular la conciliación");
			}
			else if (movimientoValores.getTipoConciliacion().equals(TipoConciliacionBancaria.ExtractoBancario)){
				if (movimientoValores.getConciliadoCon() != null){
					ExtractoBancario extracto = XPersistence.getManager().find(ExtractoBancario.class, movimientoValores.getConciliadoCon());
					extracto.anularConciliacion();
				}
				else{
					Query query = XPersistence.getManager().createQuery("from ExtractoBancario where conciliadoCon = :id");
					query.setParameter("id", movimientoValores.getId());
					query.setMaxResults(1);
					List<?> result = query.getResultList();
					if (!result.isEmpty()){
						// tomo el primero para que dispare en cascada la anulación de conciliación
						((ExtractoBancario)result.get(0)).anularConciliacion();
					}
				}
			}
			else{
				throw new ValidationException("Tipo de conciliación Movimiento de valores no soportada");
			}
		}
		else{
			throw new ValidationException("No esta conciliado");
		}
	}
	
	public static void anularConciliacionGrupo(IObjetoConciliable obj){
		if (obj.getConciliado()){
			if (obj.getTipoConciliacion().equals(TipoConciliacionBancaria.Anulacion)){
				throw new ValidationException("Es un comprobante anulado, no se puede anular la conciliación " + obj.toString());
			}
			else if (!obj.getTipoConciliacion().equals(TipoConciliacionBancaria.Grupo)){
				throw new ValidationException("Tipo de conciliación Movimiento de valores no soportada");
			}
			else{
				ExtractoBancario.cancelarConciliacion(obj);
			}
		}
		else{
			throw new ValidationException("No esta conciliado " + obj.toString());
		}
	}
	
	private static void cancelarConciliacion(IObjetoConciliable obj){
		obj.setConciliado(false);
		obj.setTipoConciliacion(TipoConciliacionBancaria.SinConciliar);
		obj.setConciliadoCon(null);
	}


	public Transaccion generarMovimientoFinanciero(String idConcepto) {
		Collection<ArrayList<Object>> conceptos = new ArrayList<ArrayList<Object>>(1);
		conceptos.add(new ArrayList<>(Arrays.asList(idConcepto, this.getImporte())));
		return this.generarMovimientoFinanciero(conceptos);
	}
	
	public Transaccion generarMovimientoFinanciero(Collection<ArrayList<Object>> idsConceptos) {
		if (this.getConciliado()){
			throw new ValidationException("Ya esta conciliado");
		}
		
		CuentaBancaria cuenta = this.getResumen().getCuenta();
		Transaccion movimientoFinanciero;
		Collection<MovimientoValores> conciliarAutomaticamente = new LinkedList<MovimientoValores>();
		int compare = this.getImporte().compareTo(BigDecimal.ZERO);
		if (compare > 0){
			movimientoFinanciero = new IngresoFinanzas();
			((IngresoFinanzas)movimientoFinanciero).setMovimientosParaConciliar(conciliarAutomaticamente);
		}
		else if (compare < 0){
			movimientoFinanciero = new EgresoFinanzas();
			((EgresoFinanzas)movimientoFinanciero).setMovimientosParaConciliar(conciliarAutomaticamente);
		}
		else{
			throw new ValidationException("Importe cero");
		}
		
		Collection<ItemTransaccion> itemsTransaccion = new LinkedList<ItemTransaccion>();
		BigDecimal controlImporte = this.getImporte(); 
		for(ArrayList<Object> array: idsConceptos){
			String idConcepto = (String)array.get(0);
			BigDecimal importeConcepto = ((BigDecimal)array.get(1));
			
			controlImporte = controlImporte.subtract(importeConcepto);
			
			ConceptoTesoreria conceptoTesoreria = XPersistence.getManager().find(ConceptoTesoreria.class, idConcepto);
			ItemTransaccion itemMovimientoFinanciero;			
			if (compare > 0){
				if (importeConcepto.compareTo(BigDecimal.ZERO) < 0){
					throw new ValidationException("El importe no puede ser negativo: " + UtilERP.convertirString(importeConcepto));
				}
				// ingreso
				itemMovimientoFinanciero = new ItemIngresoFinanzas();
				
				((ItemIngresoFinanzas)itemMovimientoFinanciero).setIngresoFinanzas((IngresoFinanzas)movimientoFinanciero);
				((ItemIngresoFinanzas)itemMovimientoFinanciero).setConcepto(conceptoTesoreria);
				((ItemIngresoFinanzas)itemMovimientoFinanciero).setDestino(cuenta);
				((ItemIngresoFinanzas)itemMovimientoFinanciero).setTipoValor(cuenta.getEfectivo());
				((ItemIngresoFinanzas)itemMovimientoFinanciero).setImporteOriginal(importeConcepto);
				((ItemIngresoFinanzas)itemMovimientoFinanciero).setDetalle(this.toString());
				((ItemIngresoFinanzas)itemMovimientoFinanciero).setFechaEmision(this.getFecha());
				((ItemIngresoFinanzas)itemMovimientoFinanciero).setFechaVencimiento(this.getFecha());
				
			}
			else if (compare < 0){
				if (importeConcepto.compareTo(BigDecimal.ZERO) > 0){
					throw new ValidationException("El importe no puede ser positivo: " + UtilERP.convertirString(importeConcepto));
				}
				// egreso				
				itemMovimientoFinanciero = new ItemEgresoFinanzas();				
				((ItemEgresoFinanzas)itemMovimientoFinanciero).setEgresoFinanzas((EgresoFinanzas)movimientoFinanciero);
				((ItemEgresoFinanzas)itemMovimientoFinanciero).setConcepto(conceptoTesoreria);
				((ItemEgresoFinanzas)itemMovimientoFinanciero).setOrigen(cuenta);
				((ItemEgresoFinanzas)itemMovimientoFinanciero).setTipoValor(cuenta.getEfectivo());
				((ItemEgresoFinanzas)itemMovimientoFinanciero).setImporteOriginal(importeConcepto.abs());
				((ItemEgresoFinanzas)itemMovimientoFinanciero).setDetalle(this.toString());
				((ItemEgresoFinanzas)itemMovimientoFinanciero).setFechaEmision(this.getFecha());
				((ItemEgresoFinanzas)itemMovimientoFinanciero).setFechaVencimiento(this.getFecha());			
			}
			else{
				throw new ValidationException("Importe cero");
			}
			itemsTransaccion.add(itemMovimientoFinanciero);
		}
		
		if (controlImporte.compareTo(BigDecimal.ZERO) != 0){
			throw new ValidationException("Los importes deben sumar " + UtilERP.convertirString(this.getImporte()) + ". Hay una diferencia de " + UtilERP.convertirString(controlImporte));
		}
		
		movimientoFinanciero.setFecha(this.getFecha());
		movimientoFinanciero.setEmpresa(cuenta.getEmpresa());
		movimientoFinanciero.setMoneda(cuenta.getEfectivo().getMoneda());
		movimientoFinanciero.setObservaciones(this.getObservaciones());
		XPersistence.getManager().persist(movimientoFinanciero);
		
		if (compare > 0){
			((IngresoFinanzas)movimientoFinanciero).setItems(new LinkedList<ItemIngresoFinanzas>());
			for(ItemTransaccion item: itemsTransaccion){
				((IngresoFinanzas)movimientoFinanciero).getItems().add((ItemIngresoFinanzas) item);
				item.recalcular();
				XPersistence.getManager().persist(item);
			}			
		}
		else{
			((EgresoFinanzas)movimientoFinanciero).setItems(new LinkedList<ItemEgresoFinanzas>());
			for(ItemTransaccion item: itemsTransaccion){
				((EgresoFinanzas)movimientoFinanciero).getItems().add((ItemEgresoFinanzas)item);
				item.recalcular();
				XPersistence.getManager().persist(item);
			}
		}
		
		movimientoFinanciero.confirmarTransaccion();
		
		this.conciliarCon(conciliarAutomaticamente);
		
		return movimientoFinanciero;	
	}	
	

	public Transaccion generarReciboCobranza(String idCliente) {
		if (this.getConciliado()){
			throw new ValidationException("Ya esta conciliado");
		}
		int compare = this.getImporte().compareTo(BigDecimal.ZERO);		
		if (compare <= 0){
			throw new ValidationException("El importe debe ser mayor a cero");
		}
		Cliente cliente = XPersistence.getManager().find(Cliente.class, idCliente);
		CuentaBancaria cuenta = this.getResumen().getCuenta();
		ReciboCobranza movimientoFinanciero = new ReciboCobranza();
		ItemReciboCobranza itemMovimientoFinanciero = new ItemReciboCobranza();
			
		itemMovimientoFinanciero.setReciboCobranza(movimientoFinanciero);
		itemMovimientoFinanciero.setDestino(cuenta);
		itemMovimientoFinanciero.setTipoValor(cuenta.getEfectivo());
		itemMovimientoFinanciero.setImporteOriginal(this.getImporte());
		itemMovimientoFinanciero.setDetalle(this.toString());
		itemMovimientoFinanciero.setFechaEmision(this.getFecha());
		itemMovimientoFinanciero.setFechaVencimiento(this.getFecha());			
				
		movimientoFinanciero.setFecha(this.getFecha());
		movimientoFinanciero.setEmpresa(cuenta.getEmpresa());
		movimientoFinanciero.setMoneda(cuenta.getEfectivo().getMoneda());
		movimientoFinanciero.setObservaciones(this.getObservaciones());
		movimientoFinanciero.setCliente(cliente);
		movimientoFinanciero.setGeneradoPorExtracto(this.getId());
		XPersistence.getManager().persist(movimientoFinanciero);
				
		movimientoFinanciero.setItems(new LinkedList<ItemReciboCobranza>());
		((ReciboCobranza)movimientoFinanciero).getItems().add((ItemReciboCobranza)itemMovimientoFinanciero);		
		itemMovimientoFinanciero.recalcular();
				
		return movimientoFinanciero;	
	}
	
	public Transaccion generarPagoProveedores(String idProveedor) {
		if (this.getConciliado()){
			throw new ValidationException("Ya esta conciliado");
		}
		int compare = this.getImporte().compareTo(BigDecimal.ZERO);		
		if (compare >= 0){
			throw new ValidationException("El importe debe ser menor a cero");
		}
		Proveedor proveedor = XPersistence.getManager().find(Proveedor.class, idProveedor);
		CuentaBancaria cuenta = this.getResumen().getCuenta();
		PagoProveedores movimientoFinanciero = new PagoProveedores();
		ItemPagoProveedores itemMovimientoFinanciero = new ItemPagoProveedores();
			
		itemMovimientoFinanciero.setPago(movimientoFinanciero);
		itemMovimientoFinanciero.setOrigen(cuenta);
		itemMovimientoFinanciero.setTipoValor(cuenta.getEfectivo());
		itemMovimientoFinanciero.setImporteOriginal(this.getImporte().abs());
		itemMovimientoFinanciero.setDetalle(this.toString());
		itemMovimientoFinanciero.setFechaEmision(this.getFecha());
		itemMovimientoFinanciero.setFechaVencimiento(this.getFecha());			
				
		movimientoFinanciero.setFecha(this.getFecha());
		movimientoFinanciero.setEmpresa(cuenta.getEmpresa());
		movimientoFinanciero.setMoneda(cuenta.getEfectivo().getMoneda());
		movimientoFinanciero.setObservaciones(this.getObservaciones());
		movimientoFinanciero.setProveedor(proveedor);
		movimientoFinanciero.setGeneradoPorExtracto(this.getId());
		XPersistence.getManager().persist(movimientoFinanciero);
		movimientoFinanciero.setValores(new LinkedList<ItemPagoProveedores>());
		((PagoProveedores)movimientoFinanciero).getValores().add((ItemPagoProveedores)itemMovimientoFinanciero);		
		itemMovimientoFinanciero.recalcular();
				
		return movimientoFinanciero;	
	}	
}