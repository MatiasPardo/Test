package org.openxava.planificacion.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;

@Entity

@View(members=
	"producto;" + 
	"periodoPlanificacion1, periodoPlanificacion2, periodoPlanificacion3, periodoPlanificacion4;" +  
	"periodoPlanificacion5, periodoPlanificacion6, periodoPlanificacion7, periodoPlanificacion8;" +			
	"periodoPlanificacion9, periodoPlanificacion10, periodoPlanificacion11, periodoPlanificacion12;" 	  		
)

public class ItemPlanVentas extends ItemPlanificacion{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@ReadOnly
	private PlanVentas planVentas;
	
	public PlanVentas getPlanVentas() {
		return planVentas;
	}

	public void setPlanVentas(PlanVentas planVentas) {
		this.planVentas = planVentas;
	}

	@Override
	public Transaccion transaccion() {
		return this.getPlanVentas();
	}

	@Override
	public void recalcular() {	
	}

}
