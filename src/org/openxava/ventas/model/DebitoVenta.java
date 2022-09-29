package org.openxava.ventas.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;

@Entity

@Views({
	@View(members=
	"Principal{ Principal[#" + 
			"descripcion, moneda, cotizacion;" +
			"fecha, fechaVencimiento, fechaServicio, fechaCreacion;" +
			"puntoVenta, tipo, estado;" + 
			"numero, cae, fechaVencimientoCAE;" +
			"Cliente[cliente, razonSocial;" + 
				"cuit, posicionIva, tipoDocumento, condicionVenta];" +
			"domicilioEntrega;" + 
			"observaciones];" + 
	"Descuentos[#" +
			"porcentajeDescuento];" +
	"items; " +
	"subtotalSinDescuento;" +
	"descuento;" + 		
	"subtotal;" + 
	"iva, percepcion1, percepcion2, impuestosInternos;" + 
	"total;}" +
	"CuentaCorriente{ctacte}"  
	),
	@View(name="Simple",
			members="numero, estado")
})

@Tab(
		filter=EmpresaFilter.class,
		baseCondition=EmpresaFilter.BASECONDITION,
		properties="fecha, numero, tipo.tipo, estado, tipoOperacion, cae, fechaVencimientoCAE, cliente.codigo, cliente.nombre, total, subtotal, iva, descuento, subtotalSinDescuento",
		defaultOrder="${fechaCreacion} desc")

public class DebitoVenta extends VentaElectronica{
	
	@ReadOnly
	private Boolean diferenciaCambio = Boolean.FALSE;
	
	public Boolean getDiferenciaCambio() {
		return diferenciaCambio;
	}

	public void setDiferenciaCambio(Boolean diferenciaCambio) {
		this.diferenciaCambio = diferenciaCambio;
	}
	
	@Override
	public void onPreCreate(){
		super.onPreCreate();
		this.setTipoOperacion("Debito");
	}
	
	@Override
	public String CtaCteTipo(){
		return "DEBITO";
	}
		
	@Override
	public String descripcionTipoTransaccion() {
		return "Débito de Venta";
	}
	
	@Override
	public boolean generadaPorDiferenciaCambio(){
		return this.getDiferenciaCambio();
	}
	
	@Override
	protected IEstrategiaCancelacionPendiente establecerEstrategiaCancelacionPendiente(){
		if (this.getDiferenciaCambio()){
			EstrategiaCancelacionPendientePorUso estrategia = new EstrategiaCancelacionPendientePorUso();
			for(ItemVentaElectronica item: this.getItems()){
				estrategia.getPendientes().add(item.generadoPorDiferenciaCambio());
			}
			return estrategia;
		}
		else{
			EstrategiaCancelacionPendientePorUso estrategia = null;
			Query query = XPersistence.getManager().createQuery("from PendienteDebitoVenta where idDebitoVenta = :id");
			query.setParameter("id", this.getId());
			List<?> results = query.getResultList();
			if (!results.isEmpty()){
				estrategia = new EstrategiaCancelacionPendientePorUso();
				for(Object result: results){
					estrategia.getPendientes().add((Pendiente)result);
				}
			}			
			if (estrategia != null){
				return estrategia;
			}
			else{
				return super.establecerEstrategiaCancelacionPendiente();
			}
		}
	}
	
	@Override
	public Integer AfipTipoComprobante(){
		return this.getTipo().codigoFiscal("DebitoVenta");
		/*if (this.getTipo().equals(TipoComprobanteAfip.A)){
			return 2;	
		}
		else if (this.getTipo().equals(TipoComprobanteAfip.B)){
			return 7;
		}
		else if (this.getTipo().equals(TipoComprobanteAfip.E)){
			return 20;
		}
		else if (this.getTipo().equals(TipoComprobanteAfip.C)){
			return 12;
		}
		else{
		    throw new ValidationException("Tipo de comprobante AFIP Incorrecto");
		} */
	}
	
	@Override
	public boolean debeAutorizaAfip(){
		return true;
	}

	@Override
	protected Class<?> tipoTransaccionRevierte() {
		return CreditoVenta.class;
	}
}
