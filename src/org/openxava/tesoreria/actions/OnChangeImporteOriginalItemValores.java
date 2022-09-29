package org.openxava.tesoreria.actions;

import java.math.*;
import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.model.*;
import org.openxava.negocio.model.*;
import org.openxava.tesoreria.model.*;
import org.openxava.util.*;

public class OnChangeImporteOriginalItemValores extends OnChangePropertyBaseAction{

	@Override
	public void execute() throws Exception {
		if(this.getNewValue() != null){
			boolean cancelarOnChange = false;
			if (getView().getValue("referencia") != null){
				String idValor = getView().getValueString("referencia.id");
				if (!Is.emptyString(idValor)){
					Valor valor = (Valor)XPersistence.getManager().find(Valor.class, idValor);
					if (valor.getImporte().compareTo((BigDecimal)this.getNewValue()) != 0){
						getView().setValueNotifying("importeOriginal", valor.getImporte());
						cancelarOnChange = true;
					}
				}
			}
			
			if (!cancelarOnChange){
				String id = getView().getValueString("tipoValor.id");
				if (!Is.emptyString(id)){
					TipoValorConfiguracion tipoValor = (TipoValorConfiguracion)XPersistence.getManager().find(TipoValorConfiguracion.class, id);
					String idMonedaTr = getView().getParent().getValueString("moneda.id");
					if (!Is.emptyString(idMonedaTr)){
						BigDecimal importe = (BigDecimal)getNewValue();
						BigDecimal cotizacion = new BigDecimal(1);
						if (!tipoValor.getMoneda().getId().equals(idMonedaTr)){
							Moneda monedaTr = (Moneda)XPersistence.getManager().find(Moneda.class, idMonedaTr);
							Date fecha = (Date)getView().getParent().getValue("fecha");
							try{
								cotizacion = instanciarTransaccion().buscarCotizacionTrConRespectoA(tipoValor.getMoneda());								
								importe = importe.divide(cotizacion, 2, RoundingMode.HALF_EVEN);	
							}
							catch(Exception e){
								cotizacion = Cotizacion.buscarCotizacion(tipoValor.getMoneda(), monedaTr, fecha);
								importe = importe.multiply(cotizacion).setScale(2, RoundingMode.HALF_EVEN);
							}														
						}
						
						try{
							if (getView().getMetaProperty("cotizacion") != null){
								getView().setValue("cotizacion", cotizacion);
							}
						}
						catch(ElementNotFoundException e){
						}
						getView().setValueNotifying("importe", importe);
					}
				}
			}
		}
	}
	
	private Transaccion instanciarTransaccion() throws Exception{
		return (Transaccion)MapFacade.findEntity(getView().getParent().getModelName(), getView().getParent().getKeyValues());		
	}
	
}
