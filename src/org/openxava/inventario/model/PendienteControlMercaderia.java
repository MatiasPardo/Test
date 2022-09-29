package org.openxava.inventario.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.RowStyle;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;
import org.openxava.base.model.IItemPendiente;
import org.openxava.base.model.Pendiente;
import org.openxava.base.model.Transaccion;
import org.openxava.negocio.filter.SucursalEmpresaFilter;

@Entity

@View(members=
	"Principal[#" + 
			"fecha, fechaUltimaActualizacion, fechaCreacion;" +
			"cumplido, fechaCumplimiento, empresa];" + 
	"remito;"
 
)

@Tab(
	filter=SucursalEmpresaFilter.class,
	properties="fecha, ejecutado, remito.numero, origen.nombre, recepcion.nombre, fechaCumplimiento, fechaUltimaActualizacion, fechaCreacion",
	rowStyles={
			@RowStyle(style="pendiente-ejecutado", property="ejecutado", value="true")	
	},		
	defaultOrder="${fechaCreacion} desc", 
	baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL + " and " + Pendiente.BASECONDITION)

public class PendienteControlMercaderia extends Pendiente{
	
	public PendienteControlMercaderia(){
	}
	
	public PendienteControlMercaderia(Transaccion origen){
		super(origen);
		this.setRemito((Remito)origen);
		this.setOrigen(((Remito)origen).getDeposito());
		this.setRecepcion(((Remito)origen).getDepositoPorConsignacion());
		this.setSucursal(this.getRecepcion().getSucursal());
	}
	
	@Override
	public void inicializar(Transaccion origen){
		super.inicializar(origen);
		for(ItemRemito item: this.getRemito().getItems()){
			item.setPendienteLiquidacion(item.getCantidad());
		}
	}
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("ControlMercaderia")
	private Remito remito;
	
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@DescriptionsList(descriptionProperties = "codigo, nombre")
	@NoCreate @NoModify @ReadOnly
	private Deposito origen;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@DescriptionsList(descriptionProperties = "codigo, nombre")
	@NoCreate @NoModify @ReadOnly
	private Deposito recepcion;
	
	public Remito getRemito() {
		return remito;
	}

	public void setRemito(Remito remito) {
		this.remito = remito;
	}

	@Override
	public Transaccion origen() {
		return this.getRemito();
	}

	@Override
	public String tipoEntidadDestino(Transaccion origen) {
		return ControlMercaderia.class.getSimpleName();
	}

	@Override
	public Transaccion crearTransaccionDestino() {
		return new ControlMercaderia();
	}
	
	@Override
	public boolean permiteProcesarJunto(Pendiente pendiente) {
		return false;
	}

	public Deposito getOrigen() {
		return origen;
	}

	public void setOrigen(Deposito origen) {
		this.origen = origen;
	}

	public Deposito getRecepcion() {
		return recepcion;
	}

	public void setRecepcion(Deposito recepcion) {
		this.recepcion = recepcion;
	}
	
	@Override
	public void itemsPendientes(List<IItemPendiente> items){
		for(ItemRemito item: this.getRemito().getItems()){
			IItemPendiente itemPendiente = item.itemPendienteControlMercaderiaProxy(); 
			items.add(itemPendiente);
		}
	}	
}
