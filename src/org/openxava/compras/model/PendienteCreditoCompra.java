package org.openxava.compras.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Query;

import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.RowStyle;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;
import org.openxava.base.filter.EmpresaFilter;
import org.openxava.base.model.Pendiente;
import org.openxava.base.model.Transaccion;
import org.openxava.inventario.model.EgresoPorDevolucion;
import org.openxava.jpa.XPersistence;


@Entity

@View(members=
	"Principal[#" + 
			"fecha, fechaUltimaActualizacion, fechaCreacion;" +
			"cumplido, fechaCumplimiento, empresa];" + 
	"devolucion;" 
)

@Tab(
		filter=EmpresaFilter.class,
		properties="fecha, ejecutado, devolucion.numero, devolucion.proveedor.codigo, devolucion.proveedor.nombre, fechaCumplimiento, fechaUltimaActualizacion, fechaCreacion",
		rowStyles={
			@RowStyle(style="pendiente-ejecutado", property="ejecutado", value="true")	
		},
		defaultOrder="${fechaCreacion} desc", 
		baseCondition=EmpresaFilter.BASECONDITION + " and " + Pendiente.BASECONDITION)

public class PendienteCreditoCompra extends Pendiente{

	public static Pendiente buscarPendienteCreditoVenta(CompraElectronica credito) {
		Query query = XPersistence.getManager().createQuery("from PendienteCreditoCompra p where p.idTrOrigen = :id");
		query.setParameter("id", credito.getIdCreadaPor());
		query.setMaxResults(1);
		List<?> result = query.getResultList();
		if (!result.isEmpty()){
			return (PendienteCreditoCompra)result.get(0);
		}
		else{
			return null;
		}
	}
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	private EgresoPorDevolucion devolucion;
	
	public EgresoPorDevolucion getDevolucion() {
		return devolucion;
	}

	public void setDevolucion(EgresoPorDevolucion devolucion) {
		this.devolucion = devolucion;
	}

	public PendienteCreditoCompra(){
	}
	
	public PendienteCreditoCompra(Transaccion origen){
		super(origen);
		this.setDevolucion((EgresoPorDevolucion)origen);
	}
	
	@Override
	public Transaccion origen() {
		return this.getDevolucion();
	}

	@Override
	public String tipoEntidadDestino(Transaccion origen) {
		if (origen.getEmpresa().getInscriptoIva()){
			return CreditoCompra.class.getSimpleName();
		}
		else{
			return CreditoInternoCompra.class.getSimpleName();
		}
	}

	@Override
	public Transaccion crearTransaccionDestino() {
		if (this.getTipoTrDestino().equals(CreditoCompra.class.getSimpleName())){
			return new CreditoCompra();
		}
		else{
			return new CreditoInternoCompra();
		}
	}

	@Override
	public boolean permiteProcesarJunto(Pendiente pendiente) {
		boolean permite = false;
		return permite;
	}
}
