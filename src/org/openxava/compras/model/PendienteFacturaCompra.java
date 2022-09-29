package org.openxava.compras.model;

import java.util.List;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.inventario.model.*;
import org.openxava.util.Is;

@Entity

@View(members=
"Principal[#" + 
		"fecha, fechaUltimaActualizacion, fechaCreacion;" +
		"cumplido, fechaCumplimiento, empresa];" + 
		"recepcion;" 
)

@Tab(
		filter=EmpresaFilter.class,
		properties="fecha, ejecutado, recepcion.numero, recepcion.proveedor.codigo, recepcion.proveedor.nombre, fechaCumplimiento, fechaUltimaActualizacion, fechaCreacion",
		rowStyles={
				@RowStyle(style="pendiente-ejecutado", property="ejecutado", value="true")	
		},				
		defaultOrder="${fechaCreacion} desc", 
		baseCondition=EmpresaFilter.BASECONDITION + " and " + Pendiente.BASECONDITION)

public class PendienteFacturaCompra extends Pendiente{

	public PendienteFacturaCompra(){
	}
	
	public PendienteFacturaCompra(Transaccion origen){
		super(origen);
		this.setRecepcion((RecepcionMercaderia)origen);		
	}
	
	@Override
	public void inicializar(Transaccion origen){
		super.inicializar(origen);
		for(ItemRecepcionMercaderia item: this.getRecepcion().getItems()){
			item.setPendienteFacturacion(item.getCantidad());
		}
	}
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	private RecepcionMercaderia recepcion;

	public RecepcionMercaderia getRecepcion() {
		return recepcion;
	}

	public void setRecepcion(RecepcionMercaderia recepcion) {
		this.recepcion = recepcion;
	}

	@Override
	public Transaccion origen() {
		return this.getRecepcion();
	}

	@Override
	public String tipoEntidadDestino(Transaccion origen) {
		return FacturaCompra.class.getSimpleName();
	}

	@Override
	public Transaccion crearTransaccionDestino() {
		return new FacturaCompra();
	}	
	
	@Override
	public boolean permiteProcesarJunto(Pendiente pendiente) {
		boolean procesaJunto = super.permiteProcesarJunto(pendiente);
		if (procesaJunto){
			procesaJunto = false;
			RecepcionMercaderia recepPendiente = ((PendienteFacturaCompra)pendiente).getRecepcion();
			RecepcionMercaderia recep = this.getRecepcion();
			if (Is.equal(recepPendiente.getProveedor(), recep.getProveedor())){				
				procesaJunto = true;
			}
		}
		return procesaJunto;
	}
	
	@Override
	public void itemsPendientes(List<IItemPendiente> items){
		for(ItemRecepcionMercaderia item: this.getRecepcion().getItems()){
			IItemPendiente itemPendiente = item.itemPendienteFacturaCompraProxy();
			if (itemPendiente != null){
				items.add(itemPendiente);
			}
		}
	}
}
