package org.openxava.cuentacorriente.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.tesoreria.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

@Entity

@Views({
	@View(members=
		"Principal[#" + 
				"fechaCreacion, usuario, estado;" + 
				"fecha, numero;" +
				"empresa;" + 
				"cliente;" +
				"observaciones;" +		
				"origen;" + 
				"destino];" + 
		"Imputacion[#" +
				"importe, monedaImputacion, diasMora;" +
				"diferenciaCambio, monedaDifCambio];"),
		@View(name="Simple", 
				members="origen; destino; Imputacion[importe, moneda, diasMora]")
})

@Tabs({
	@Tab(filter=EmpresaFilter.class,
		baseCondition=EmpresaFilter.BASECONDITION,
		properties="empresa.nombre, cliente.codigo, cliente.nombre, fecha, numero, origen.tipo, origen.numero, destino.tipo, destino.numero, importe, monedaImputacion.nombre, diasMora",
		defaultOrder="${fechaCreacion} desc")
})

public class ImputacionVenta extends Imputacion{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView(value="Simple")
	private Cliente cliente;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate
	@SearchAction(value="ReferenciaCCImputacionVenta.buscar")
	@ReferenceView("Simple")
	private CuentaCorrienteVenta origen;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate
	@SearchAction(value="ReferenciaCCImputacionVenta.buscar")
	@ReferenceView("Simple")
	private CuentaCorrienteVenta destino;
	
	@ReadOnly
	private int diasMora = 0;
	
	public Cliente getCliente() {
		
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
	
	public CuentaCorrienteVenta getOrigen() {
		return origen;
	}

	public void setOrigen(CuentaCorrienteVenta origen) {
		this.origen = origen;
	}

	public CuentaCorrienteVenta getDestino() {
		return destino;
	}

	public void setDestino(CuentaCorrienteVenta destino) {
		this.destino = destino;
	}

	public int getDiasMora() {
		return diasMora;
	}

	public void setDiasMora(int diasMora) {
		this.diasMora = diasMora;
	}

	@Override
	public CuentaCorriente comprobanteOrigen() {
		return this.getOrigen();
	}

	@Override
	public CuentaCorriente comprobanteDestino() {
		return this.getDestino();
	}

	@Override
	public OperadorComercial operadorCtaCte(){
		return this.getCliente();
	}

	@Override
	public String descripcionTipoTransaccion() {
		return "Imputación Venta";
	}
		
	private int calcularDiasMora(CuentaCorriente origen, CuentaCorriente destino){
		Date desde = origen.getFechaVencimiento();
		Date hasta = destino.getFecha();
		int diasCobranzas = 0;
		if (Is.equalAsString(destino.getTipoEntidad(), ReciboCobranza.class.getSimpleName())){
			diasCobranzas = ((ReciboCobranza)destino.buscarTransaccion()).getDias();
		}				
		long dias = diasCobranzas + UtilERP.diferenciaDias(desde, hasta);		
		return (int)dias;
	}
	
	@Override
	public void tipoTrsDestino(Collection<Class<?>> tipoTrsDestino){
		tipoTrsDestino.add(CreditoVenta.class);
		tipoTrsDestino.add(CreditoInternoVenta.class);
		tipoTrsDestino.add(DebitoVenta.class);
		tipoTrsDestino.add(DebitoInternoVenta.class);
	}
	
	@Override
	public Class<?> getTipoPendiente(Class<?> tipoTransaccionDestino){
		if (CreditoVenta.class.equals(tipoTransaccionDestino)){
			return DiferenciaCambioCreditoVenta.class;
		}
		else if (CreditoInternoVenta.class.equals(tipoTransaccionDestino)){
			return DiferenciaCambioCreditoVenta.class;
		}
		else if (DebitoVenta.class.equals(tipoTransaccionDestino)){
			return DiferenciaCambioDebitoVenta.class;
		}
		else if (DebitoInternoVenta.class.equals(tipoTransaccionDestino)){
			return DiferenciaCambioDebitoVenta.class;
		}
		else{
			return super.getTipoPendiente(tipoTransaccionDestino);
		}
	}
	
	@Override
	protected boolean cumpleCondicionGeneracionPendiente(Class<?> tipoTrDestino){
		boolean cumple = false;
		if (Is.equal(this.getMonedaDifCambio(), this.getMoneda1())){
			if (CreditoVenta.class.equals(tipoTrDestino)){
				if (this.getDiferenciaCambio().compareTo(BigDecimal.ZERO) < 0){
					if (this.getEmpresa().getNumero() == 1){
						cumple = true;
					}
				}
			}
			else if (CreditoInternoVenta.class.equals(tipoTrDestino)){
				if (this.getDiferenciaCambio().compareTo(BigDecimal.ZERO) < 0){
					if (this.getEmpresa().getNumero() == 2){
						cumple = true;
					}
				}
			}
			else if (DebitoVenta.class.equals(tipoTrDestino)){
				if (this.getDiferenciaCambio().compareTo(BigDecimal.ZERO) > 0){
					if (this.getEmpresa().getNumero() == 1){
						cumple = true;
					}
				}
			}
			else if (DebitoInternoVenta.class.equals(tipoTrDestino)){
				if (this.getDiferenciaCambio().compareTo(BigDecimal.ZERO) > 0){
					if (this.getEmpresa().getNumero() == 2){
						cumple = true;
					}
				}
			}
			else{
				cumple = super.cumpleCondicionGeneracionPendiente(tipoTrDestino);
			}			
		}
		return cumple;
	}
		
	protected void pasajeAtributosWorkFlowSinItemsPrePersist(Transaccion destino, List<Pendiente> pendientes){
		DiferenciaCambioVenta difCambio = (DiferenciaCambioVenta)pendientes.get(0);
		destino.setMoneda(difCambio.getMoneda());
		if (destino instanceof VentaElectronica){
			((VentaElectronica)destino).setDomicilioEntrega(((VentaElectronica)destino).getCliente().domicilioEntregaPrincipal());
		}
		if (destino instanceof DebitoVenta){
			((DebitoVenta)destino).setDiferenciaCambio(Boolean.TRUE);			
		}
		if (destino instanceof DebitoInternoVenta){
			((DebitoInternoVenta)destino).setDiferenciaCambio(Boolean.TRUE);			
		}
		else if (destino instanceof CreditoVenta){
			((CreditoVenta)destino).setDiferenciaCambio(Boolean.TRUE);
		}
		else if (destino instanceof CreditoInternoVenta){
			((CreditoInternoVenta)destino).setDiferenciaCambio(Boolean.TRUE);
		}
	}
	
	protected void pasajeAtributosWorkFlowSinItemsPosPersist(Transaccion destino, List<Pendiente> pendientes){
		// se crea un item por cada diferencia de cambio
		Producto concepto = destino.getEmpresa().getConceptoDiferenciaCambio();
		if (concepto == null){
			throw new ValidationException("Falta definir el concepto de diferencia de cambio en la empresa " + destino.getEmpresa().getNombre());
		}		
		for(Pendiente pendiente: pendientes){
			DiferenciaCambioVenta difCambio = (DiferenciaCambioVenta)pendiente;
			ItemVentaElectronica item = new ItemVentaElectronica();
			item.setProducto(concepto);
			item.setCantidad(new BigDecimal(1));
			item.setUnidadMedida(concepto.getUnidadMedida());			
			item.setVenta((VentaElectronica)destino);
			item.setIdDiferenciaCambio(difCambio.getId());
			item.calcularPrecioUnitarioSegunImporteTotal(difCambio.getImporte().abs());
			item.recalcular();
			XPersistence.getManager().persist(item);
			if (((VentaElectronica)destino).getItems() == null){
				((VentaElectronica)destino).setItems(new ArrayList<ItemVentaElectronica>());
			}
			((VentaElectronica)destino).getItems().add(item);
		}
	}
	
	@Override
	public void getTransaccionesGeneradas(Collection<Transaccion> trs){
		// se buscan las notas de crédito/débito por diferencia de cambio
		String sql = "select v.dtype, v.id from " + Esquema.concatenarEsquema("ventaelectronica") + " v " +  
				"join " + Esquema.concatenarEsquema("itemventaelectronica") + " i on i.venta_id = v.id " +
				"join " + Esquema.concatenarEsquema("diferenciacambioventa") + " difcambio on difcambio.id = i.iddiferenciacambio and difcambio.imputacion_id = :id " +
				"group by v.dtype, v.id";
		Query query = XPersistence.getManager().createNativeQuery(sql);
		query.setParameter("id", this.getId());
		
		@SuppressWarnings("unchecked")
		List<Object[]> result = query.getResultList();
		for(Object[] res: result ){
			String tipoEntidad = (String)res[0];
			String id = (String)res[1];
			
			Query queryPorID = XPersistence.getManager().createQuery("from " + tipoEntidad + " where id = :id");
			queryPorID.setParameter("id", id);
			queryPorID.setMaxResults(1);
			trs.add((Transaccion)queryPorID.getSingleResult());
		}
	}
	
	public boolean aplicaDiferenciaCambio(CuentaCorriente comprobante){
		boolean aplica = false;
		// solo hay diferencia de cambio en moneda de la contabilidad (moneda 1)
		if ((this.getDiferenciaCambio().compareTo(BigDecimal.ZERO) != 0) &&
			(Is.equal(this.getMonedaDifCambio(), this.getMoneda1()))){
			if (comprobante.ingresa()){ 
				if (this.getDiferenciaCambio().compareTo(BigDecimal.ZERO) < 0){
					aplica = true;
				}
			}
			else{
				if (this.getDiferenciaCambio().compareTo(BigDecimal.ZERO) > 0){
					aplica = true;
				}
			}	
		}
		return aplica;
	}
	
	@Override
	protected void posImputarComprobantes(boolean imputacionPorDiferenciaCambio){
		super.posImputarComprobantes(imputacionPorDiferenciaCambio);
		int diasMora = 0;
		if (!imputacionPorDiferenciaCambio){
			diasMora = calcularDiasMora(this.getOrigen(), this.getDestino());
		}
		this.setDiasMora(diasMora);
	}	
}
