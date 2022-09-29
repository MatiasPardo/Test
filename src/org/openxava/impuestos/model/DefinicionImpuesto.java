package org.openxava.impuestos.model;

import org.openxava.contabilidad.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

public enum DefinicionImpuesto {
	IvaVenta(0, false, false, false, TipoCuentaContable.Impuesto, null, null), 
	PercepcionBsAs(1, false, false, false, TipoCuentaContable.Impuesto, GrupoImpuesto.PercepcionesIIBB, CalculadorPercepcionesBsAs.class), 
	PercepcionCABA(2, false, false, false, TipoCuentaContable.Impuesto, GrupoImpuesto.PercepcionesIIBB, CalculadorPercepcionesCABA.class),
	RetencionGanancias(3, true, true, true, TipoCuentaContable.Impuesto, null, CalculadorRetencionesGanancias.class),
	RetencionBsAs(4, false, true, true, TipoCuentaContable.Impuesto, GrupoImpuesto.RetencionesIIBB, CalculadorRetencionesBsAs.class),
	RetencionCABA(5, false, true, true, TipoCuentaContable.Impuesto, GrupoImpuesto.RetencionesIIBB, CalculadorRetencionesCABA.class),
	SinTipo(6, true, false, false, TipoCuentaContable.Impuesto, null, null),
	IvaCompra(7, false, false, false, TipoCuentaContable.Impuesto, null, null),
	PercepcionIVA(8, true, false, false, TipoCuentaContable.Impuesto, GrupoImpuesto.PercepcionesIVA, null),
	ImpuestosInternos(9, true, false, false, TipoCuentaContable.Impuesto, null, null),
	PercepcionNeuquen(10, false, false, false, TipoCuentaContable.Impuesto, GrupoImpuesto.PercepcionesIIBB, CalculadorPercepcionesGlobal.class),
	RetencionMonotributo(11, true, true, true, TipoCuentaContable.Impuesto, null, CalculadorRetencionesMonotributo.class),
	RetencionIva(12, false, true, true, TipoCuentaContable.Impuesto, GrupoImpuesto.RetencionesIVA, CalculadorRetencionesIVA.class);
	
	private TipoCuentaContable cuentaContable1 = null;
	
	private boolean multiplesRegimenes = false;
	
	private boolean numerar = false;
	
	private boolean pagos = false;
	
	private int indice;
	
	private GrupoImpuesto grupo;
	
	private Class<?> claseCalculador;
	
	DefinicionImpuesto(int indice, boolean multiplesRegimenes, boolean numerar, boolean pagos, TipoCuentaContable tipo1, 
			GrupoImpuesto grupo, Class<?> claseCalculador){
		this.indice = indice;
		this.pagos = pagos;
		this.numerar = numerar;
		this.cuentaContable1 = tipo1;
		this.multiplesRegimenes = multiplesRegimenes;
		this.grupo = grupo;
		this.claseCalculador = claseCalculador;
		if (this.ordinal() != indice){
			throw new ValidationException("No se puede cambiar el orden ni el indice del enumerado de definición de impuestos");
		}
	}
	
	public void validar(Impuesto impuesto){
		if (this.cuentaContable1 != null){
			this.cuentaContable1.CuentaContablePorTipo(impuesto);
		}
		
		if (this.getGrupo() != null){
			// si tiene grupo asignado, debe coincidir
			if (!Is.equal(this.getGrupo(), impuesto.getGrupo())){
				throw new ValidationException("El grupo asignado no corresponde al tipo de impuesto");
			}
		}
	}
	
	public ICalculadorImpuesto calculadorImpuesto(){
		if (this.equals(DefinicionImpuesto.PercepcionCABA)){
			return new CalculadorPercepcionesCABA();
		}
		else if (this.equals(DefinicionImpuesto.PercepcionBsAs)){
			return new CalculadorPercepcionesBsAs();
		}
		else if (this.equals(DefinicionImpuesto.PercepcionNeuquen)){
			return new CalculadorPercepcionesGlobal(this);
		}
		else if (this.equals(DefinicionImpuesto.RetencionGanancias)){
			return new CalculadorRetencionesGanancias();
		}
		else if (this.equals(DefinicionImpuesto.RetencionBsAs)){
			return new CalculadorRetencionesBsAs();
		}
		else if (this.equals(DefinicionImpuesto.RetencionCABA)){
			return new CalculadorRetencionesCABA();
		}
		else{
			Class<?> className = this.getClaseCalculador();
			if (className != null){
				try{
					return (ICalculadorImpuesto)className.newInstance();
				}
				catch(Exception e){
					throw new ValidationException("Error calculador impuestos: " + e.toString());
				}
			}
			else{
				throw new ValidationException("El impuesto " + this.name() + " no esta implementado");
			}
		}
	}

	public boolean isMultiplesRegimenes() {
		return multiplesRegimenes;
	}
	
	public boolean isPagos(){
		return pagos;
	}
	
	public int getIndice(){
		return this.indice;
	}
	
	public boolean debeNumerar(){
		return this.numerar;
	}

	public GrupoImpuesto getGrupo() {
		return grupo;
	}

	private Class<?> getClaseCalculador() {
		return claseCalculador;
	}	
}
