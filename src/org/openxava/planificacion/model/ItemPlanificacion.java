package org.openxava.planificacion.model;

import java.math.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.ventas.model.*;

@MappedSuperclass

public abstract class ItemPlanificacion extends ItemTransaccion{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView(value="Simple")
	@NoCreate @NoModify
	private Producto producto;
	
	private BigDecimal periodoPlanificacion1;
	
	private BigDecimal periodoPlanificacion2;
	
	private BigDecimal periodoPlanificacion3;
	
	private BigDecimal periodoPlanificacion4;
	
	private BigDecimal periodoPlanificacion5;
	
	private BigDecimal periodoPlanificacion6;
	
	private BigDecimal periodoPlanificacion7;
	
	private BigDecimal periodoPlanificacion8;
	
	private BigDecimal periodoPlanificacion9;
	
	private BigDecimal periodoPlanificacion10;
	
	private BigDecimal periodoPlanificacion11;
	
	private BigDecimal periodoPlanificacion12;

	public BigDecimal getPeriodoPlanificacion1() {
		return periodoPlanificacion1;
	}

	public void setPeriodoPlanificacion1(BigDecimal periodoPlanificacion1) {
		this.periodoPlanificacion1 = periodoPlanificacion1;
	}

	public BigDecimal getPeriodoPlanificacion2() {
		return periodoPlanificacion2;
	}

	public void setPeriodoPlanificacion2(BigDecimal periodoPlanificacion2) {
		this.periodoPlanificacion2 = periodoPlanificacion2;
	}

	public BigDecimal getPeriodoPlanificacion3() {
		return periodoPlanificacion3;
	}

	public void setPeriodoPlanificacion3(BigDecimal periodoPlanificacion3) {
		this.periodoPlanificacion3 = periodoPlanificacion3;
	}

	public BigDecimal getPeriodoPlanificacion4() {
		return periodoPlanificacion4;
	}

	public void setPeriodoPlanificacion4(BigDecimal periodoPlanificacion4) {
		this.periodoPlanificacion4 = periodoPlanificacion4;
	}

	public BigDecimal getPeriodoPlanificacion5() {
		return periodoPlanificacion5;
	}

	public void setPeriodoPlanificacion5(BigDecimal periodoPlanificacion5) {
		this.periodoPlanificacion5 = periodoPlanificacion5;
	}

	public BigDecimal getPeriodoPlanificacion6() {
		return periodoPlanificacion6;
	}

	public void setPeriodoPlanificacion6(BigDecimal periodoPlanificacion6) {
		this.periodoPlanificacion6 = periodoPlanificacion6;
	}

	public BigDecimal getPeriodoPlanificacion7() {
		return periodoPlanificacion7;
	}

	public void setPeriodoPlanificacion7(BigDecimal periodoPlanificacion7) {
		this.periodoPlanificacion7 = periodoPlanificacion7;
	}

	public BigDecimal getPeriodoPlanificacion8() {
		return periodoPlanificacion8;
	}

	public void setPeriodoPlanificacion8(BigDecimal periodoPlanificacion8) {
		this.periodoPlanificacion8 = periodoPlanificacion8;
	}

	public BigDecimal getPeriodoPlanificacion9() {
		return periodoPlanificacion9;
	}

	public void setPeriodoPlanificacion9(BigDecimal periodoPlanificacion9) {
		this.periodoPlanificacion9 = periodoPlanificacion9;
	}

	public BigDecimal getPeriodoPlanificacion10() {
		return periodoPlanificacion10;
	}

	public void setPeriodoPlanificacion10(BigDecimal periodoPlanificacion10) {
		this.periodoPlanificacion10 = periodoPlanificacion10;
	}

	public BigDecimal getPeriodoPlanificacion11() {
		return periodoPlanificacion11;
	}

	public void setPeriodoPlanificacion11(BigDecimal periodoPlanificacion11) {
		this.periodoPlanificacion11 = periodoPlanificacion11;
	}

	public BigDecimal getPeriodoPlanificacion12() {
		return periodoPlanificacion12;
	}

	public void setPeriodoPlanificacion12(BigDecimal periodoPlanificacion12) {
		this.periodoPlanificacion12 = periodoPlanificacion12;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}
}
