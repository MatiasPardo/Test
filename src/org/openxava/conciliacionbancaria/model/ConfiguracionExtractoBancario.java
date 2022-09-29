package org.openxava.conciliacionbancaria.model;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Query;

import org.openxava.annotations.DefaultValueCalculator;
import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.ListProperties;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.PropertyValue;
import org.openxava.annotations.Required;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;
import org.openxava.base.model.ObjetoEstatico;
import org.openxava.calculators.IntegerCalculator;
import org.openxava.calculators.StringCalculator;
import org.openxava.jpa.XPersistence;
import org.openxava.tesoreria.model.Banco;
import org.openxava.tesoreria.model.ConceptoTesoreria;
import org.openxava.tesoreria.model.CuentaBancaria;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;

@Entity

@Views({
	@View(members="Principal{" +
			"codigo, activo; nombre;" +
			"banco;" + 
			"filaComienzo;" + 
			
			"Fecha[columnaFecha, mascaraFecha;];" + 
			"Importe[columnaConcepto, columnaObservaciones;" +
				"columnaDebito, columnaCredito, columnaSaldo;" +
			"];" + 
			"Cred/Deb[columnaTipoImporte, valorTipoCredito, valorTipoDebito]" +
		"}" +
		"Conceptos{relacionesConceptosExtracto}" 
		),
	@View(name="Simple",
		members="codigo, nombre")	
})

public class ConfiguracionExtractoBancario extends ObjetoEstatico{
	
	private static final String MASCARAFECHADEFAULT = "dd/MM/yyyy";
	
	public static ConfiguracionExtractoBancario buscar(CuentaBancaria cuenta) {
		if (cuenta.getBanco() != null){
			Query query = XPersistence.getManager().createQuery("from ConfiguracionExtractoBancario where banco.id = :banco");
			query.setParameter("banco", cuenta.getBanco().getId());
			query.setMaxResults(1);
			try{
				return (ConfiguracionExtractoBancario)query.getSingleResult();
			}
			catch(Exception e){
				throw new ValidationException("No esta definido la configuración de importación de extracto bancario para el banco " + cuenta.getBanco().toString());
			}
		}
		else{
			throw new ValidationException("Banco no asignado en la cuenta " + cuenta.toString());
		}		
	}	
	
	@DefaultValueCalculator(value=IntegerCalculator.class, 
			properties={@PropertyValue(name="value", value="1")})
	@Required
	@Hidden
	private Integer filaComienzo = 1;
	
	@Required
	@Hidden
	private Integer columnaFecha;
	
	@Required
	@Hidden
	private Integer columnaConcepto;
	
	@Required
	@Hidden
	private Integer columnaDebito;
	
	@Required
	@Hidden
	private Integer columnaCredito;
	
	@Hidden
	private Integer columnaSaldo;
	
	@Hidden
	private Integer columnaObservaciones;
	
	@Column(length=10)
	@DefaultValueCalculator(value=StringCalculator.class, 
				properties={@PropertyValue(name="string", value=MASCARAFECHADEFAULT)})
	@Required
	@Hidden
	private String mascaraFecha = MASCARAFECHADEFAULT;
	
	@Column(length=10)
	@Hidden
	private String valorTipoCredito;
	
	@Column(length=10)
	@Hidden
	private String valorTipoDebito;
	
	@Hidden
	private Integer columnaTipoImporte;

	@OneToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify 
	@DescriptionsList(descriptionProperties="nombre")
	private Banco banco;
	
	@OneToMany(mappedBy="configuracionExtracto", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@ListProperties("extracto, conceptoTesoreria.codigo, conceptoTesoreria.nombre")
	private Collection<RelacionConceptoExtractoBancario> relacionesConceptosExtracto;
	
	public Integer getFilaComienzo() {
		return filaComienzo;
	}

	public void setFilaComienzo(Integer filaComienzo) {
		this.filaComienzo = filaComienzo;
	}

	public Integer getColumnaFecha() {
		return columnaFecha;
	}

	public void setColumnaFecha(Integer columnaFecha) {
		this.columnaFecha = columnaFecha;
	}

	public Integer getColumnaConcepto() {
		return columnaConcepto;
	}

	public void setColumnaConcepto(Integer columnaConcepto) {
		this.columnaConcepto = columnaConcepto;
	}

	public Integer getColumnaDebito() {
		return columnaDebito;
	}

	public void setColumnaDebito(Integer columnaDebito) {
		this.columnaDebito = columnaDebito;
	}

	public Integer getColumnaCredito() {
		return columnaCredito;
	}

	public void setColumnaCredito(Integer columnaCredito) {
		this.columnaCredito = columnaCredito;
	}

	public Integer getColumnaSaldo() {
		return columnaSaldo;
	}

	public void setColumnaSaldo(Integer columnaSaldo) {
		this.columnaSaldo = columnaSaldo;
	}

	public Integer getColumnaObservaciones() {
		return columnaObservaciones;
	}

	public void setColumnaObservaciones(Integer columnaObservaciones) {
		this.columnaObservaciones = columnaObservaciones;
	}

	public String getMascaraFecha() {
		return mascaraFecha;
	}

	public void setMascaraFecha(String mascaraFecha) {
		if (mascaraFecha != null){
			this.mascaraFecha = mascaraFecha;
		}		
	}
	
	public String getValorTipoCredito() {
		return valorTipoCredito;
	}

	public void setValorTipoCredito(String valorTipoCredito) {
		this.valorTipoCredito = valorTipoCredito;
	}

	public String getValorTipoDebito() {
		return valorTipoDebito;
	}

	public void setValorTipoDebito(String valorTipoDebito) {
		this.valorTipoDebito = valorTipoDebito;
	}

	public Integer getColumnaTipoImporte() {
		return columnaTipoImporte;
	}

	public void setColumnaTipoImporte(Integer columnaTipoImporte) {
		this.columnaTipoImporte = columnaTipoImporte;
	}
	
	public Banco getBanco() {
		return banco;
	}

	public void setBanco(Banco banco) {
		this.banco = banco;
	}

	public boolean tieneCreditoDebito() {
		if (this.getColumnaCredito() != null){
			return !this.getColumnaCredito().equals(this.getColumnaDebito());
		}
		else{
			return false;
		}
	}

	public BigDecimal establecerSignoImporte(BigDecimal importe, String tipoImporte) {
		if (this.getColumnaTipoImporte() != null){
			if (!Is.emptyString(tipoImporte)){
				if (Is.equalAsString(tipoImporte, this.getValorTipoCredito())){
					return importe.abs();
				}
				else if (Is.equalAsString(tipoImporte, this.getValorTipoDebito())){
					return importe.abs().negate();
				}
				else{
					throw new ValidationException("tipo de importe: "+ tipoImporte + " debe coincidir con el valor del tipo credito o debito de la configuracion");
				}
			}
			else{
				throw new ValidationException("tipo de importe no puede estar vacío");
			}
		}
		else{
			return importe;
		}
	}

	public Collection<RelacionConceptoExtractoBancario> getRelacionesConceptosExtracto() {
		return relacionesConceptosExtracto;
	}

	public void setRelacionesConceptosExtracto(Collection<RelacionConceptoExtractoBancario> relacionesConceptosExtracto) {
		this.relacionesConceptosExtracto = relacionesConceptosExtracto;
	}
	
	public ConceptoTesoreria buscarConcepto(String extracto) {
		Query query = XPersistence.getManager().createQuery("from RelacionConceptoExtractoBancario where extracto = :extracto");
		query.setParameter("extracto", extracto);
		List<?> results = query.getResultList();
		if (results.size() == 1){
			return ((RelacionConceptoExtractoBancario)results.get(0)).getConceptoTesoreria();
		}
		return null;
	}
}
