package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import org.openxava.base.model.*;
import org.openxava.negocio.model.*;

public interface IItemMovimientoValores {
	
	public TipoValorConfiguracion getTipoValor();
	
	public Banco getBanco();
	
	public void setBanco(Banco banco);
	
	public String getDetalle();
	
	public void setDetalle(String detalle);
	
	public Date getFechaEmision();
	
	public void setFechaEmision(Date fechaEmision);
	
	public Date getFechaVencimiento();
	
	public void setFechaVencimiento(Date fechaVencimiento);
	
	public String getNumeroValor();
	
	public void setNumeroValor(String numeroValor);
	
	public Empresa getEmpresa();
	
	public void setEmpresa(Empresa empresa);
	
	public String getFirmante();
	
	public String getCuitFirmante();
	
	public String getNroCuentaFirmante();
	
	public Tesoreria tesoreriaAfectada();
	
	public IChequera chequera();
	
	public Tesoreria transfiere();
	
	public Valor referenciaValor();
	
	public void asignarReferenciaValor(Valor valor);
	
	public BigDecimal importeOriginalValores();
		
	public BigDecimal importeMonedaTrValores(Transaccion transaccion);
	
	public TipoMovimientoValores tipoMovimientoValores(boolean reversion);

	public void asignarOperadorComercial(Valor valor, Transaccion transaccion);
	
	public OperadorComercial operadorComercialValores(Transaccion transaccion);
	
	public ConceptoTesoreria conceptoTesoreria(); 
	
	public boolean noGenerarDetalle();
	
	public ObjetoNegocio itemTrValores();
}
