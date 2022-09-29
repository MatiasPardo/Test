package org.openxava.reclamos.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;
import org.openxava.inventario.calculators.*;
import org.openxava.inventario.model.*;
import org.openxava.negocio.calculators.*;
import org.openxava.util.*;
import org.openxava.validators.*;


@Entity

@Views({
	@View(members= "Principal{ fecha, numero;" + 
		 	"reclamo;" + 
			"tipoTarea, resultado, deposito;" +  	
			"observaciones, realizadoPor;" +  	
			"items;}" + 
			"Auditoria{usuario, fechaCreacion}"),
	
	@View(name="Reclamo", 
		members="fecha, numero, fechaCreacion, usuario;" + 
				"tipoTarea, deposito;" + 
				"observaciones;" + 
				"items"
	)

})

@Tab(
		properties="numero, fecha, estado, reclamo.numero, tipoTarea.nombre, fechaCreacion, usuario",
		filter=EmpresaFilter.class,		
		baseCondition=EmpresaFilter.BASECONDITION,
		defaultOrder="${fechaCreacion} desc"
	)

public class CumplimientoReclamo extends Transaccion implements ITransaccionInventario{
	
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@DefaultValueCalculator(value= ObjetoPrincipalCalculator.class,
			properties= {@PropertyValue(name="entidad",value="TipoTarea")
					})
	@NoCreate @NoModify
	private TipoTarea tipoTarea;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@DefaultValueCalculator(value= ObjetoPrincipalCalculator.class,
			properties= {@PropertyValue(name="entidad",value="TipoResultadoTarea")
					})
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="codigo, nombre")
	private TipoResultadoTarea resultado;  
	
	public TipoResultadoTarea getResultado() {
		return resultado;
	}

	public void setResultado(TipoResultadoTarea resultado) {
		this.resultado = resultado;
	}

	@ManyToOne(fetch=FetchType.LAZY, optional=false)
//	@DefaultValueCalculator(
//			value=RealizadoPorDefectoCalculator.class,
//			properties= {@PropertyValue(name="reclamoID", from="reclamo.id")})
	@NoModify @NoCreate
	@ReferenceView("Cumplimiento")
	private UsuarioReclamo realizadoPor;
	
	@OneToMany(mappedBy="cumplimiento",cascade=CascadeType.ALL)
	@ListProperties(value="producto.codigo, producto.nombre, unidadMedida.nombre, cantidad, detalle")
	@SaveAction(value="ItemTransaccion.save")
	@NewAction(value="ItemTransaccion.new")
	@HideDetailAction(value="ItemTransaccion.hideDetail")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	@EditAction("ItemTransaccion.edit")
	private Collection<ItemCumplimientoReclamo> items;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoModify
	@NoCreate
	@ReferenceView("Cumplimiento")
	@SearchListCondition("${cumplimiento} is null and ${estado} = 1")
	private Reclamo reclamo;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@DefaultValueCalculator(DepositoDefaultCalculator.class)
	@NoCreate @NoModify
	private Deposito deposito;
	
	public UsuarioReclamo getRealizadoPor() {
		return realizadoPor;
	}
	
	public void setRealizadoPor(UsuarioReclamo realizadoPor) {
//		UsuarioReclamo realizado = this.getReclamo().getAsignadoA();
//		if(!(realizado == null)) {
//			this.realizadoPor = realizado;
//		}else 
			this.realizadoPor = realizadoPor;
	}

	@Override
	protected void inicializar() {
		super.inicializar();
		if(!Is.empty(this.getReclamo()) ){
			if(Is.empty(this.getRealizadoPor())){
				this.setRealizadoPor(this.getReclamo().getAsignadoA()); 
			} 
		}
		// si el Deposito ya esta asignado, no hace nada 
		  if (this.getDeposito() == null){
		     ICalculator defaultCalculator = new DepositoDefaultCalculator();
		     try {                  this.setDeposito((Deposito)defaultCalculator.calculate());
		     } catch (Exception e) {
		     }
		 }
		// si el Resultado ya esta asignado, no hace nada 
		  if (this.getResultado() == null){
		     ObjetoPrincipalCalculator defaultCalculator = new ObjetoPrincipalCalculator();
		     try {             
		    	 defaultCalculator.setEntidad("TipoResultadoTarea");
		    	 this.setResultado((TipoResultadoTarea)defaultCalculator.calculate());
		     } catch (Exception e) {
		     }
		 }
		// si el Tipo Tarea ya esta asignado, no hace nada 
		  if (this.getTipoTarea() == null){
		     ObjetoPrincipalCalculator defaultCalculator = new ObjetoPrincipalCalculator();
		     try {                  
		    	 defaultCalculator.setEntidad("TipoTarea");
		    	 this.setTipoTarea((TipoTarea)defaultCalculator.calculate());
		     } catch (Exception e) {
		     }
		 }
		   
	}
	
	public Deposito getDeposito() {
		return deposito;
	}

	public void setDeposito(Deposito deposito) {
		this.deposito = deposito;
	}

	public Reclamo getReclamo() {
		return reclamo;
	}

	public void setReclamo(Reclamo reclamo) {
		this.reclamo = reclamo;
	}

	public TipoTarea getTipoTarea() {
		return tipoTarea;
	}

	public void setTipoTarea(TipoTarea tipoTarea) {
		this.tipoTarea = tipoTarea;
	}

	public Collection<ItemCumplimientoReclamo> getItems() {
		return items;
	}

	public void setItems(Collection<ItemCumplimientoReclamo> items) {
		this.items = items;
	}

	@Override
	public String descripcionTipoTransaccion() {
		
		return "Cumplimiento Reclamo" ;
	}

	@Override
	public ArrayList<IItemMovimientoInventario> movimientosInventario(){
		ArrayList<IItemMovimientoInventario> movimientos = new ArrayList<IItemMovimientoInventario>();
		movimientos.addAll(this.getItems()); //Ver metodo item
		return movimientos;
	}
	
	@Override
	public boolean revierteInventarioAlAnular() {
		return true;
	}
	
	@Override 
	protected void posConfirmarTransaccion() {
		super.posConfirmarTransaccion();
		if (this.getReclamo().getCumplimiento() == null){
			this.getReclamo().setCumplimiento(this);
		}
		else{
			throw new ValidationException("El reclamo ya fue cumplido");
		}
	}
	
	@Override
	protected void posAnularTransaccion() {
		super.posAnularTransaccion();
		if (Is.equal(this.getReclamo().getCumplimiento(), this)){
			this.getReclamo().setCumplimiento(null);
		}
		else if (this.getReclamo().getCumplimiento() == null){
			throw new ValidationException("El reclamo no tiene asociado el cumplimiento que se quiere anular");
		}		
	}

	@Override
	public EmpresaExterna empresaExternaInventario() {
		return null;
	}
}


