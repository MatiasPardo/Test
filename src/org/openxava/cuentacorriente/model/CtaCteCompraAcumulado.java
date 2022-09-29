package org.openxava.cuentacorriente.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;
import org.openxava.compras.model.Proveedor;

@Entity

@Table(name="VIEW_CTACTECOMPRAACUMULADO")

public class CtaCteCompraAcumulado extends CtaCteSaldoAcumulado{
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@ReadOnly
	private Proveedor proveedor;
		
	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}
}