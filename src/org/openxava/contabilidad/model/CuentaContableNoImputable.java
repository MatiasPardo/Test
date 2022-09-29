package org.openxava.contabilidad.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.*;
import javax.validation.ValidationException;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.contabilidad.actions.*;

@Entity

@Views({
	@View(members="imputa;"
			+ "codigo, nombre;"
			+ "tipo"),
	@View(name="Simple",
		members="codigo, nombre")	
})

@Tab(properties="codigo, nombre, tipo, imputa.codigo, imputa.nombre, fechaCreacion, usuario")

public class CuentaContableNoImputable extends ObjetoEstatico{
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate
	@ReferenceView("Simple")
	@OnChange(OnChangeImputaCuentaContableAction.class)
	private CuentaContableNoImputable imputa;
	
	private TipoCuentaNoImputable tipo;

	public CuentaContableNoImputable getImputa() {
		return imputa;
	}

	public void setImputa(CuentaContableNoImputable imputa) {
		this.imputa = imputa;
	}

	public TipoCuentaNoImputable getTipo() {
		return tipo;
	}

	public void setTipo(TipoCuentaNoImputable tipo) {
		this.tipo = tipo;
	}
	
	@Override
	protected void onPrePersist() {
		super.onPrePersist();
		this.validarImputa();
	}
	
	@Override
	protected void onPreUpdate() {
		super.onPreUpdate();
		this.validarImputa();
	}

	private void validarImputa() {
		if (this.getImputa() != null){
			Map<String, Object> niveles = new HashMap<String, Object>();
			if (!this.esNuevo()){
				niveles.put(this.getId(), null);
			}
			this.verificarReferenciasCirculares(niveles, this.getImputa());
		}
	}
	
	private void verificarReferenciasCirculares(Map<String, Object> niveles, CuentaContableNoImputable imputa){
		if (niveles.containsKey(imputa.getId())){
			throw new ValidationException("Error referencia circular: " + imputa.toString());
		}
		else{
			niveles.put(imputa.getId(), null);
			if (imputa.getImputa() != null){
				this.verificarReferenciasCirculares(niveles, imputa.getImputa());
			}
		}
	}
	
	
}
