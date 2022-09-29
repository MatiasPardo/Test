package org.openxava.conciliacionbancaria.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.Views;
import org.openxava.model.MapFacade;
import org.openxava.negocio.model.IParametrosReporte;
import org.openxava.tesoreria.model.ConceptoTesoreria;
import org.openxava.util.Is;
import org.openxava.view.View;

@Views({
	@org.openxava.annotations.View(members="concepto"),
	@org.openxava.annotations.View(
			name="MultiplesConceptos",
			members="total;" +
					"concepto1, importeConcepto1;" + 
					"concepto2, importeConcepto2;"),
})

public class ParametrosGenerarMovimientoExtractoBancario implements IParametrosReporte{

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre", 
				condition="${activo} = 't'")
	@NoCreate @NoModify
	private ConceptoTesoreria concepto;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre", 
				condition="${activo} = 't'")
	@NoCreate @NoModify
	private ConceptoTesoreria concepto1;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre", 
				condition="${activo} = 't'")
	@NoCreate @NoModify
	private ConceptoTesoreria concepto2;
	
	private BigDecimal total;
	
	private BigDecimal importeConcepto1;
	
	private BigDecimal importeConcepto2;
	
	public ConceptoTesoreria getConcepto() {
		return concepto;
	}

	public void setConcepto(ConceptoTesoreria concepto) {
		this.concepto = concepto;
	}
	
	public ConceptoTesoreria getConcepto1() {
		return concepto1;
	}

	public void setConcepto1(ConceptoTesoreria concepto1) {
		this.concepto1 = concepto1;
	}

	public ConceptoTesoreria getConcepto2() {
		return concepto2;
	}

	public void setConcepto2(ConceptoTesoreria concepto2) {
		this.concepto2 = concepto2;
	}
	
	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public BigDecimal getImporteConcepto1() {
		return importeConcepto1;
	}

	public void setImporteConcepto1(BigDecimal importeConcepto1) {
		this.importeConcepto1 = importeConcepto1;
	}

	public BigDecimal getImporteConcepto2() {
		return importeConcepto2;
	}

	public void setImporteConcepto2(BigDecimal importeConcepto2) {
		this.importeConcepto2 = importeConcepto2;
	}

	@Override
	public void asignarValoresIniciales(View view, View previousView, Map<?, ?>[] idsSeleccionados) {
		if (idsSeleccionados.length > 0){
			
			if (Is.equalAsString(view.getViewName(), "MultiplesConceptos")){
				Map<?, ?> key = idsSeleccionados[0];
				try{
					ExtractoBancario extracto = (ExtractoBancario)MapFacade.findEntity("ExtractoBancario", key);
					view.setValue("total", extracto.getImporte());
				}
				catch(Exception e){
					
				}
				
			}
			else{
				// en la vista por defecto
				Map<?, ?> key = idsSeleccionados[0];
				ExtractoBancario extracto = null;
				try{
					extracto = (ExtractoBancario)MapFacade.findEntity("ExtractoBancario", key);
					if (extracto != null && !Is.emptyString(extracto.getConcepto())){
						ConfiguracionExtractoBancario config = ConfiguracionExtractoBancario.buscar(extracto.getResumen().getCuenta());
						ConceptoTesoreria concepto = config.buscarConcepto(extracto.getConcepto());
						if (concepto != null){
							Map<String, Object> values = new HashMap<String, Object>();
							values.put("id", concepto.getId());
							view.setValue("concepto", values);
						}
					}
				}
				catch(Exception e){				
				}
			}			
		}
	}
}
