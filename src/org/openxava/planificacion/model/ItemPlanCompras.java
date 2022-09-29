package org.openxava.planificacion.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.View;
import org.openxava.base.model.Transaccion;

@Entity

@View(members=
	"producto;" + 
	"periodoPlanificacion1, periodoPlanificacion2, periodoPlanificacion3, periodoPlanificacion4;" +  
	"periodoPlanificacion5, periodoPlanificacion6, periodoPlanificacion7, periodoPlanificacion8;" +			
	"periodoPlanificacion9, periodoPlanificacion10, periodoPlanificacion11, periodoPlanificacion12;" 	  		
)

public class ItemPlanCompras extends ItemPlanificacion{


	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@ReadOnly
	private PlanCompras plan;
	
	public PlanCompras getPlan() {
		return plan;
	}

	public void setPlan(PlanCompras plan) {
		this.plan = plan;
	}

	@Override
	public Transaccion transaccion() {
		return this.getPlan();
	}

	@Override
	public void recalcular() {	
	}
}
