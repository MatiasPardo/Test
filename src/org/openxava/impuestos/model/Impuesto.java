package org.openxava.impuestos.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;
import org.openxava.contabilidad.model.*;
import org.openxava.impuestos.calculators.*;
import org.openxava.impuestos.validators.*;
import org.openxava.jpa.*;
import org.openxava.validators.*;

@Entity

@Views({
	@View(members=
			"Principal{codigo, activo;" + 
			"nombre;" + 
			"tipo, grupo;" +
			"alicuotaGeneral, minimoImponible;" + 
			"Visibilidad[cobranzas, compras, pagos, permiteRepetidos];" +
			"alicuotas;}" +			
			"Contabilidad{cuentaContableImpuestos}" +
			"Numeracion{numerador1}"	
	),
	@View(name="Simple", members="codigo, nombre")
})

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})

@EntityValidator(
		value=ImpuestoValidator.class, 
		properties= {
			@PropertyValue(name="id"), 
			@PropertyValue(name="atributo", value="tipo"),
			@PropertyValue(name="valor", from="tipo"),
			@PropertyValue(name="modelo", value="Impuesto"),
			@PropertyValue(name="idMessage", value="tipo_repetido_impuesto"),
			@PropertyValue(name="pagos", from="pagos"),
			@PropertyValue(name="minimoImponible"),
			@PropertyValue(name="alicuotaGeneral")
		}
)

public class Impuesto extends ObjetoEstatico{
	
	public static Impuesto buscarPorDefinicionImpuesto(DefinicionImpuesto tipo) {
		Query query = XPersistence.getManager().createQuery("from Impuesto where tipo = :tipo");
		
		query.setParameter("tipo", tipo);
		try{
			Impuesto impuesto = (Impuesto)query.getSingleResult();
			return impuesto;
		}
		catch(NoResultException e){
			throw new ValidationException("No se encontró la configuración del impuesto " + tipo.toString());
		}
	}
	
	@Required
	private DefinicionImpuesto tipo;
	
	@Required
	@DefaultValueCalculator(value=GrupoImpuestoCalculator.class, 
							properties={@PropertyValue(from="tipo", name="definicionImpuesto")})
	private GrupoImpuesto grupo;
	
	@DefaultValueCalculator(value=BigDecimalCalculator.class, 
							properties={@PropertyValue(name="value", value="0")})
	private BigDecimal alicuotaGeneral = BigDecimal.ZERO;
		
	@DefaultValueCalculator(value=BigDecimalCalculator.class, 
			properties={@PropertyValue(name="value", value="0")})
	private BigDecimal minimoImponible = BigDecimal.ZERO;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean compras = Boolean.FALSE;
	
	@DefaultValueCalculator(value=ImpuestoFiltroPagosCalculator.class, properties={@PropertyValue(from="tipo", name="definicionImpuesto")})
	private Boolean pagos = Boolean.FALSE;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean cobranzas = Boolean.FALSE;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView(value="Simple")
	@NoCreate @NoModify
	private CuentaContable cuentaContableImpuestos;
	
	@OneToMany(mappedBy="impuesto", cascade=CascadeType.ALL)	
	@ListProperties("codigo, nombre, posicion, porcentaje, escalas.nombre, activo")
	private Collection<AlicuotaImpuesto> alicuotas;
		
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView(value="Simple")
	private Numerador numerador1;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean permiteRepetidos = Boolean.FALSE;
	
	public Numerador getNumerador1() {
		return numerador1;
	}

	public void setNumerador1(Numerador numerador1) {
		this.numerador1 = numerador1;
	}

	public DefinicionImpuesto getTipo() {
		return tipo;
	}

	public void setTipo(DefinicionImpuesto tipo) {
		this.tipo = tipo;
	}

	public CuentaContable getCuentaContableImpuestos() {
		return cuentaContableImpuestos;
	}

	public void setCuentaContableImpuestos(CuentaContable cuentaContableImpuestos) {
		this.cuentaContableImpuestos = cuentaContableImpuestos;
	}
	
	@Override
	public void onPrePersist(){
		super.onPrePersist();
		this.getTipo().validar(this);
	}
	
	@Override
	public void onPreUpdate(){
		super.onPreUpdate();
		this.getTipo().validar(this);
	}

	public BigDecimal getAlicuotaGeneral() {
		return alicuotaGeneral == null ? BigDecimal.ZERO : this.alicuotaGeneral;
	}

	public void setAlicuotaGeneral(BigDecimal alicuotaGeneral) {
		this.alicuotaGeneral = alicuotaGeneral;
	}

	public Boolean getCompras() {
		return compras;
	}

	public void setCompras(Boolean compras) {
		this.compras = compras;
	}

	public BigDecimal getMinimoImponible() {
		return minimoImponible == null ? BigDecimal.ZERO : minimoImponible;
	}

	public void setMinimoImponible(BigDecimal minimoImponible) {
		this.minimoImponible = minimoImponible;
	}

	public Collection<AlicuotaImpuesto> getAlicuotas() {
		return alicuotas;
	}

	public void setAlicuotas(Collection<AlicuotaImpuesto> alicuotas) {
		this.alicuotas = alicuotas;
	}

	public Numerador buscarNumerador(Transaccion transaccion){
		Numerador numerador = null;
		try {
			int i = 1;
			while (i <= 10){
				Numerador num;
				num = (Numerador)this.getClass().getMethod("getNumerador" + Integer.toString(i)).invoke(this);
				if (num != null){
					if (num.getEmpresa().equals(transaccion.getEmpresa())){
						numerador = num;
						break;
					}
				}
				i += 1;
			}
		} catch (NoSuchMethodException e) {
			
		} catch (Exception e) {
			throw new ValidationException("Error al buscar numerador en impuesto: " + e.toString());
		}
		
		return numerador;
	}

	public Boolean getCobranzas() {
		return cobranzas;
	}

	public void setCobranzas(Boolean cobranzas) {
		this.cobranzas = cobranzas;
	}

	public Boolean getPagos() {
		return pagos;
	}

	public void setPagos(Boolean pagos) {
		this.pagos = pagos;
	}

	public GrupoImpuesto getGrupo() {
		return grupo;
	}

	public void setGrupo(GrupoImpuesto grupo) {
		this.grupo = grupo;
	}

	public Boolean getPermiteRepetidos() {
		return permiteRepetidos == null ? Boolean.FALSE : permiteRepetidos;
	}

	public void setPermiteRepetidos(Boolean permiteRepetidos) {
		this.permiteRepetidos = permiteRepetidos;
	}
	
	public AlicuotaImpuesto buscarAlicuota(PosicionAnteRetencion posicion){
		AlicuotaImpuesto alicuota = null;
		if (this.getAlicuotas() != null){
			for(AlicuotaImpuesto al: this.getAlicuotas()){
				if (al.getPosicion().equals(posicion)){
					if (alicuota != null){
						throw new ValidationException("En el impuesto " + this.getCodigo() + " Hay más de una alicuota definida para " + posicion.toString());
					}
					alicuota = al;
				}
			}
		}
		return alicuota;
	}
}
