package org.openxava.ventas.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Query;

import org.openxava.afip.model.FacturaElectronicaAfip;
import org.openxava.annotations.EditOnly;
import org.openxava.annotations.ListProperties;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;
import org.openxava.base.model.Estado;
import org.openxava.base.model.Transaccion;
import org.openxava.jpa.XPersistence;
import org.openxava.negocio.filter.SucursalEmpresaFilter;
import org.openxava.negocio.model.Sucursal;
import org.openxava.util.Is;
import org.openxava.util.Messages;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.calculators.PuntoVentaDefaultCalculator;

@Entity

@View(members=
			"Devolucion[sucursal; numero, estado];" +
			"observaciones;" +
			"items;" +			
			"venta;" + 
			"credito;")

@Tab(filter=SucursalEmpresaFilter.class,
	baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL,
	properties="fecha, numero, estado",
	defaultOrder="${fechaCreacion} desc")

public class DevolucionFacturaContado extends Transaccion{

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReadOnly
	@ReferenceView(value="DevolucionContado")	
	private FacturaVentaContado venta;
	
	@ElementCollection
	@EditOnly
	@ListProperties("producto.codigo, producto.nombre, cantidad, devolucion, unidadMedida.codigo")	
	private Collection<ItemDevolucionFacturaContado> items;
	
	@ReferenceView(value="Simple")
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReadOnly
	private VentaElectronica credito;
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Devolución";
	}

	public FacturaVentaContado getVenta() {
		return venta;
	}

	public void setVenta(FacturaVentaContado venta) {
		this.venta = venta;
	}

	public Collection<ItemDevolucionFacturaContado> getItems() {
		if (items == null) this.items = new ArrayList<ItemDevolucionFacturaContado>();
		return items;
	}

	public void setItems(Collection<ItemDevolucionFacturaContado> items) {
		this.items = items;
	}

	public VentaElectronica getCredito() {
		return credito;
	}

	public void setCredito(VentaElectronica credito) {
		this.credito = credito;
	}
	
	@Override
	public void copiarPropiedades(Object objeto){
		super.copiarPropiedades(objeto);
		this.setItems(null);
		this.setVenta(null);
		this.setCredito(null);
	}

	@Override
	protected void validacionesPreGrabarTransaccion(Messages errores){
		super.validacionesPreGrabarTransaccion(errores);
		
		if (this.getItems().isEmpty()){
			errores.add("sin_items");
		}
		else{
			boolean sinDevolucion = true;
			for(ItemDevolucionFacturaContado item: this.getItems()){
				if (item.getDevolucion().compareTo(BigDecimal.ZERO) > 0){
					sinDevolucion = false;
					if (item.getDevolucion().compareTo(item.getCantidad()) > 0){
						errores.add(item.getProducto().getCodigo() + " puede devolver como máximo " + item.getCantidad().toString());
					}
				}
			}
			if (sinDevolucion){
				errores.add("Cantidad en cero. No hay devoluciones.");
			}
		}
	}
		
	public void autorizarAfipCreditoPorDevolucion() throws Exception {
		
		if (this.getCredito() == null){		
			FacturaVentaContado factura = this.getVenta();
			VentaElectronica credito = (VentaElectronica) factura.generarComprobanteReversion(this.getSucursal());
			credito.asignarCreadoPor(this);			
			// se actualizan las cantidades
			Collection<ItemVentaElectronica> itemsCredito = new LinkedList<ItemVentaElectronica>();
			itemsCredito.addAll(credito.getItems());
			
			for(ItemDevolucionFacturaContado itemDevolucion: this.getItems()){
				ItemVentaElectronica itemCredito = this.buscarItemCoincidaCon(itemsCredito, itemDevolucion);
				if (itemCredito != null){				
					if (itemDevolucion.getDevolucion().compareTo(BigDecimal.ZERO) > 0){
						itemCredito.setCantidad(itemDevolucion.getDevolucion());
						itemCredito.recalcular();
					}
					else{
						credito.getItems().remove(itemCredito);
						XPersistence.getManager().remove(itemCredito);
					}				
					itemsCredito.remove(itemCredito);
				}
				else{
					throw new ValidationException("No se encontró item de crédito de compra que coincida con la devolución");
				}
			}
			credito.recalcularTotales();
			this.setCredito(credito);
		}
		if (!this.getCredito().finalizada()){
			FacturaElectronicaAfip fe = new FacturaElectronicaAfip();
			fe.SolicitarCAE(this.getCredito(), this.getVenta().tipoTransaccionRevierte());
		}
	}

	public ItemVentaElectronica buscarItemCoincidaCon(Collection<ItemVentaElectronica> items, ItemDevolucionFacturaContado item){
		for(ItemVentaElectronica itemBusqueda: items){
			if (itemBusqueda.getProducto().equals(item.getProducto())){
				if (itemBusqueda.getCantidad().compareTo(item.getCantidad()) <= 0){
					return itemBusqueda;
				}				
			}
		}
		return null;
	}
	
	public FacturaVentaContado generarFacturaPorDevolucion() {
		Query query = XPersistence.getManager().createQuery("from FacturaVentaContado where idCreadaPor = :id and estado != :cancelada");
		query.setParameter("id", this.getId());
		query.setParameter("cancelada", Estado.Cancelada);
		query.setMaxResults(1);
		List<?> result = query.getResultList();
		
		FacturaVentaContado factura = null;
		if (!result.isEmpty()){
			factura = (FacturaVentaContado)result.get(0);
		}
		else{		
			factura = new FacturaVentaContado();
			factura.copiarPropiedades(this.getVenta());
			if (!Is.equal(factura.getSucursal(), this.getSucursal())){
				factura.setSucursal(this.getSucursal());
				try{
					PuntoVentaDefaultCalculator ptoVenta = new PuntoVentaDefaultCalculator();
					ptoVenta.setSucursal(factura.getSucursal());
					factura.setPuntoVenta((PuntoVenta)ptoVenta.calculate());
				}
				catch(Exception e){
				}
			}
			
			factura.asignarCreadoPor(this);
			XPersistence.getManager().persist(factura);
			factura.grabarTransaccion();		
		}
		return factura;
	}
	
	@Override
	protected void asignarSucursal(){
		if (this.getSucursal() != null){
			if (!this.getSucursal().usuarioHabilitado()){
				this.setSucursal(Sucursal.sucursalDefault());
				if (this.getSucursal() == null){
					throw new ValidationException("Usuario no tiene sucursales habilitadas");
				}
			}
		}
	}
}

