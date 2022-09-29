package org.openxava.ventas.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.inventario.model.*;

@Entity

@View(members=
	"Principal[#" + 
			"fecha, fechaUltimaActualizacion, fechaCreacion;" +
			"cumplido, fechaCumplimiento, empresa];" + 
	"devolucion;" 
)

@Tab(
		filter=EmpresaFilter.class,
		properties="fecha, ejecutado, devolucion.numero, devolucion.cliente.codigo, devolucion.cliente.nombre, fechaCumplimiento, fechaUltimaActualizacion, fechaCreacion",
		rowStyles={
			@RowStyle(style="pendiente-ejecutado", property="ejecutado", value="true")	
		},
		defaultOrder="${fechaCreacion} desc", 
		baseCondition=EmpresaFilter.BASECONDITION + " and " + Pendiente.BASECONDITION)

public class PendienteCreditoVenta extends Pendiente{

	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	private IngresoPorDevolucion devolucion;
	
	public PendienteCreditoVenta(){
	}
	
	public PendienteCreditoVenta(Transaccion origen){
		super(origen);
		this.setDevolucion((IngresoPorDevolucion)origen);		
	}
	
	@Override
	public Transaccion origen() {
		return this.getDevolucion();
	}

	@Override
	public String tipoEntidadDestino(Transaccion origen) {
		if (origen.getEmpresa().getInscriptoIva()){
			return CreditoVenta.class.getSimpleName();
		}
		else{
			return CreditoInternoVenta.class.getSimpleName();
		}
		
	}

	@Override
	public Transaccion crearTransaccionDestino() {
		if (this.getTipoTrDestino().equals(CreditoVenta.class.getSimpleName())){
			return new CreditoVenta();
		}
		else{
			return new CreditoInternoVenta();
		}
	}

	@Override
	public boolean permiteProcesarJunto(Pendiente pendiente) {
		return true;
	}
	
	public IngresoPorDevolucion getDevolucion() {
		return devolucion;
	}

	public void setDevolucion(IngresoPorDevolucion devolucion) {
		this.devolucion = devolucion;
	}	
}

