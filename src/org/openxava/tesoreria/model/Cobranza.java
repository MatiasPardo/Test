package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.cuentacorriente.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

@Entity

@Views({
	@View(members=
		"Principal[#" + 
				"numero, fecha, fechaCreacion;" +
				"estado, subestado, ultimaTransicion;" +
				"moneda, cotizacion;" + 
				"cliente;" +
				"observaciones];" +
		"valores{comprobantesCobrar; items; impuestos};" +
		"historico{historicoEstados};" +
		"recibos{recibosGenerados};" 
	),
	@View(name="Simple", members="numero")
})

@Tabs({
	@Tab(
		properties="fecha, numero, estado, cliente.nombre, moneda.nombre, cotizacion, observaciones, total",
		filter=EmpresaFilter.class,		
		baseCondition=EmpresaFilter.BASECONDITION,
		defaultOrder="${fechaCreacion} desc")
})

public class Cobranza extends IngresoValores{
	
	@ManyToMany(fetch=FetchType.LAZY)
	@ListProperties("fecha, tipo, numero, importeOriginal, monedaOriginal.nombre, saldo1, saldo2")
	@OrderBy("fecha asc") 
	@CollectionView(value="Simple")
	@NewAction("ComprobantesPorCobrar.add")
	private Collection<CuentaCorrienteVenta> comprobantesCobrar;
	
	@OneToMany(mappedBy="cobranza", cascade=CascadeType.ALL)
	@ListProperties("empresa.nombre, destino.nombre, tipoValor.nombre, importeOriginal, cotizacion, importe, detalle, numero, fechaEmision, fechaVencimiento, banco.nombre")
	@SaveAction(value="ItemTransaccion.save")
	@NewAction(value="ItemTransaccion.new")
	@HideDetailAction(value="ItemTransaccion.hideDetail")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")	
	@EditAction("ItemTransaccion.edit")
	private Collection<ItemCobranza> items;
	
	@ElementCollection
	@ListProperties("impuesto.codigo, importe, fecha, numero")	
	private Collection<ImpuestoCobranza> impuestos;
	
	public Collection<ItemCobranza> getItems() {
		return items;
	}

	public void setItems(Collection<ItemCobranza> items) {
		this.items = items;
	}

	public Collection<ImpuestoCobranza> getImpuestos() {
		return impuestos;
	}

	public void setImpuestos(Collection<ImpuestoCobranza> impuestos) {
		this.impuestos = impuestos;
	}

	@Override
	public String descripcionTipoTransaccion() {
		return "Cobranza";
	}
		
	@Override
	public boolean actualizarFinanzasAlConfirmar() {
		return true;
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
		// se genera una transacción de recibo de cobranza por empresa para que impacte en la cuenta corriente y la contabilidad
		
		// Se orden por empresa para no tener problema de concurrencia, ya que la transacción numera por empresa
		List<ItemIngresoValores> items = new LinkedList<ItemIngresoValores>();
		items.addAll(this.getItems());
		items.sort(new ComparatorItemIngresoValoresPorEmpresa());
		
		Iterator<ItemIngresoValores> it = items.iterator();
		Empresa empresa = null;
		ReciboCobranza recibo = null;
		this.recibosCreados = new ArrayList<ReciboCobranza>();
		
		ReciboCobranza reciboEmpresa1 = null;
		while(it.hasNext()){
			ItemIngresoValores item = it.next();
			if ((empresa == null) || (!item.getEmpresa().equals(empresa))){
				empresa = item.getEmpresa();
				recibo = new ReciboCobranza();
				recibo.copiarPropiedades(this);
				recibo.setCobranza(this);
				recibo.setEmpresa(empresa);
				recibo.setFecha(this.getFecha());
				recibo.setItems(new ArrayList<ItemReciboCobranza>());
				recibo.setComprobantesCobrar(new ArrayList<CuentaCorrienteVenta>());
				XPersistence.getManager().persist(recibo);
				this.recibosCreados.add(recibo);
				
				if (empresa.getNumero() == 1){
					reciboEmpresa1 = recibo;
				}
			}
			
			ItemReciboCobranza itemRecibo = new ItemReciboCobranza();
			itemRecibo.copiarPropiedades(item);
			itemRecibo.setReciboCobranza(recibo);
			itemRecibo.setIdItemCobranza(item.getId());
			XPersistence.getManager().persist(itemRecibo);
			recibo.getItems().add(itemRecibo);
		}
		
		if (recibosCreados.isEmpty()){
			throw new ValidationException("Falta agregar los items");
		}
		else{
			Map<String, ReciboCobranza> map = new  HashMap<String, ReciboCobranza>();
			for(ReciboCobranza rec: this.recibosCreados){
				map.put(rec.getEmpresa().getId(), rec);
			}
			
			for(CuentaCorrienteVenta ctacte: this.getComprobantesCobrar()){
				if (map.containsKey(ctacte.getEmpresa().getId())){
					ReciboCobranza rec = map.get(ctacte.getEmpresa().getId());
					rec.getComprobantesCobrar().add(ctacte);
				}
				else{
					throw new ValidationException("No puede agregar el comprobante por cobrar " + ctacte.getNumero() + ": no hay valores registrados en esa empresa");
				}
			}
		}
		
		if (!this.getImpuestos().isEmpty()){ 
			// los impuestos van a parar a la empresa1
			
			if (reciboEmpresa1 == null){
				throw new ValidationException("Los impuestos no se pueden agregar al recibo, ya que no hay valores ingresados para la empresa A");
			}
			for(ImpuestoCobranza impuesto: this.getImpuestos()){			
				ItemReciboCobranzaRetencion retencion = new ItemReciboCobranzaRetencion();
				retencion.setReciboCobranza(reciboEmpresa1);
				retencion.copiarPropiedades(impuesto);
				XPersistence.getManager().persist(retencion);
				reciboEmpresa1.getRetenciones().add(retencion);
			}
		}
		
		for (ReciboCobranza rec: this.recibosCreados){
			rec.confirmarTransaccion();
		}		
	}
	
	@Transient
	private Map<String, Object> recibosPorEmpresa = null; 
	
	@Transient
	private Collection<ReciboCobranza> recibosCreados = null;
	
	@ReadOnly
	@ListProperties("numero, empresa.nombre")
	public Collection<ReciboCobranza> getRecibosGenerados(){
		if (!Is.emptyString(this.getId())){
			Query query = XPersistence.getManager().createQuery("from ReciboCobranza where cobranza.id = :cobranza");
			query.setParameter("cobranza", this.getId());
			@SuppressWarnings("unchecked")
			List<ReciboCobranza> result = (List<ReciboCobranza>)query.getResultList();
			Collection<ReciboCobranza> recibos = new ArrayList<ReciboCobranza>(); 
			recibos.addAll(result);
			return recibos;
		}
		else{
			return Collections.emptyList();
		}
	}
	
	private ReciboCobranza buscarReciboPorEmpresa(Empresa empresa){
		if (recibosPorEmpresa == null){
			this.recibosPorEmpresa = new HashMap<String, Object>();
			if (this.recibosCreados == null){
				this.recibosCreados = this.getRecibosGenerados();
			}
			for(ReciboCobranza recibo: this.recibosCreados){
				recibosPorEmpresa.put(recibo.getEmpresa().getId(), recibo);
			}
		}
		return (ReciboCobranza)recibosPorEmpresa.get(empresa.getId());
	}
	
	@Override
	public void antesPersistirMovimientoFinanciero(MovimientoValores item) {
		ReciboCobranza recibo = this.buscarReciboPorEmpresa(item.getEmpresa());
		item.setNumeroComprobante(recibo.getNumero());
		item.setIdTransaccion(recibo.getId());
		item.setTipoComprobante(recibo.descripcionTipoTransaccion());
		item.setTipoTrDestino(recibo.getClass().getSimpleName());
		for(ItemReciboCobranza itemRecibo: recibo.getItems()){
			if (Is.equalAsString(itemRecibo.getIdItemCobranza(), item.getIdItem())){
				item.setTipoItem(itemRecibo.getClass().getSimpleName());
				item.setIdItem(itemRecibo.getId());
				break;
			}
		}
	}
	
	protected void validacionesPreAnularTransaccion(Messages errores){
		errores.add("No se puede anular la cobranza. Debe anular los recibos generados");
	}

	@Override
	protected Collection<ItemIngresoValores> items() {
		Collection<ItemIngresoValores> itemsIngVal = new LinkedList<ItemIngresoValores>();
		if (this.getItems() != null){
			itemsIngVal.addAll(this.getItems());
		}
		return itemsIngVal;
	}
	
	@Override
	public void recalcularTotales(){
		super.recalcularTotales();
		
		if (this.getImpuestos() != null){
			BigDecimal totalImpuestos = BigDecimal.ZERO;
			for(ImpuestoCobranza impuesto: this.getImpuestos()){
				totalImpuestos = totalImpuestos.add(impuesto.getImporte());
			}
			this.setTotal(this.getTotal().add(totalImpuestos));
		}
	}
	
	@Override
	public OperadorComercial operadorFinanciero(){
		return this.getCliente();
	}
}
