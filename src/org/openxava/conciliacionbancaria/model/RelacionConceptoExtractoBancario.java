package org.openxava.conciliacionbancaria.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.base.model.ObjetoNegocio;
import org.openxava.tesoreria.model.ConceptoTesoreria;

@Entity

public class RelacionConceptoExtractoBancario extends ObjetoNegocio{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private ConfiguracionExtractoBancario configuracionExtracto;
	
	@Column(length=100)	
	private String extracto;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private ConceptoTesoreria conceptoTesoreria;

	public ConfiguracionExtractoBancario getConfiguracionExtracto() {
		return configuracionExtracto;
	}

	public void setConfiguracionExtracto(ConfiguracionExtractoBancario configuracionExtracto) {
		this.configuracionExtracto = configuracionExtracto;
	}

	public String getExtracto() {
		return extracto;
	}

	public void setExtracto(String extracto) {
		this.extracto = extracto;
	}

	public ConceptoTesoreria getConceptoTesoreria() {
		return conceptoTesoreria;
	}

	public void setConceptoTesoreria(ConceptoTesoreria conceptoTesoreria) {
		this.conceptoTesoreria = conceptoTesoreria;
	}
}
