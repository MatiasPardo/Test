package org.openxava.cuentacorriente.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.Esquema;
import org.openxava.base.model.Estado;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.ventas.filter.*;
import org.openxava.ventas.model.*;

@Entity

@Views({
	@View(members=
		"Principal[#" + 
				"empresa, anulado, pendiente, pendienteDiferenciaCambio;" +
				"fecha, tipo, numero;" + 
				"];"+
		"importeOriginal, saldoOriginal, monedaOriginal, cotizacion;" +
		"importe1, saldo1;" + 
		"importe2, saldo2;" + 
		"imputaciones;"
				),
	@View(name="Transaccion", members=	 
		"anulado, pendiente, pendienteDiferenciaCambio, clasificador, fechaProbable;" +
		"importeOriginal, saldoOriginal, monedaOriginal, cotizacion;" +
		"importe1, saldo1;" + 
		"importe2, saldo2;" + 
		"imputaciones;"
			),
	@View(name="Simple", 
		members="empresa;" + 
				"fecha, numero, tipo;" +
				"cliente;" + 
				"monedaOriginal, saldoOriginal, importeOriginal;" + 
				"saldo1, saldo2, pendiente"),
	@View(name="CambioClasificador", members="clasificador"),
	@View(name="CambioFechaProbable", members="fechaProbable")
})

@Tab(
		properties="empresa.nombre, cliente.codigo, cliente.nombre, fecha, numero, tipo, pendiente, anulado, vendedor.nombre, ingreso, egreso, saldo1, importe1, cotizacion2, importe2, saldo2, monedaOriginal.nombre, importeOriginal, saldoOriginal, cotizacion",
		filter=VentasFilter.class,
		baseCondition="(true = ? or ${vendedor.id} = ?) and " + EmpresaFilter.BASECONDITION,
		defaultOrder="${fechaCreacion} desc")

public class CuentaCorrienteVenta extends CuentaCorriente{

	public static CuentaCorrienteVenta buscarCuentaCorrienteVenta(ITransaccionCtaCte transaccion){		
		Query query = XPersistence.getManager().createQuery("from CuentaCorrienteVenta where idTransaccion = :id");
		query.setParameter("id", transaccion.CtaCteTransaccion().getId());
		query.setMaxResults(1);
		List<?> result = query.getResultList();
		CuentaCorrienteVenta ctacte = null;
		if (!result.isEmpty()){
			ctacte = (CuentaCorrienteVenta)result.get(0);
		}		
		return ctacte;
	}
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@ReadOnly
	@Required
	private Cliente cliente;	
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")	
	@NoCreate @NoModify
	private Vendedor vendedor;
	
	@Column(length=32)
	@ReadOnly
	@Hidden
	private String idLiquidacion;
	
	@ReadOnly
	private BigDecimal comision;
	
	@ReadOnly
	private BigDecimal porcentajeComision;
	
	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
	
	public Vendedor getVendedor() {
		return vendedor;
	}

	public void setVendedor(Vendedor vendedor) {
		this.vendedor = vendedor;
	}

	@Override
	protected IResponsableCuentaCorriente getResponsableCtaCte(){
		return this.getVendedor();
	}
	
	@Override
	protected void setResponsableCtaCte(IResponsableCuentaCorriente responsable){
		this.setVendedor((Vendedor)responsable);
	}
	
	@Override
	public OperadorComercial operadorCtaCte(){
		return this.getCliente();
	}
	
	@Override
	protected void setOperadorCtaCte(OperadorComercial operador){
		this.setCliente((Cliente)operador);
		if (operador != null){
			this.setVendedor(((Cliente)operador).getVendedor());
		}
	}
	
	@ReadOnly
	@ListProperties("origen.tipo, origen.numero, estado, destino.tipo, destino.numero, importe, moneda.nombre, diasMora, diferenciaCambio, monedaDifCambio.nombre")
	public Collection<ImputacionVenta> getImputaciones(){
		Collection<ImputacionVenta> items = new ArrayList<ImputacionVenta>();
		if (!Is.emptyString(this.getId())){
			String sql = "from ImputacionVenta i where ";
			if (this.ingresa()){
				sql += "i.origen.id = :id";
			}
			else{
				sql += "i.destino.id = :id";
			}
			Query query = XPersistence.getManager().createQuery(sql);
			query.setParameter("id", this.getId());
			query.setFlushMode(FlushModeType.COMMIT);
			@SuppressWarnings("unchecked")
			List<ImputacionVenta> result = query.getResultList();
			items.addAll(result);
		}
		return items;
	}
	
	@Transient
	private BigDecimal diferenciasCambiosMoneda1 = null;
 	
	public void agregarDifenciasCambiosMoneda1NoImputadas(BigDecimal difCambio){
		this.diferenciasCambiosMoneda1 = this.diferenciasCambioMoneda1NoImputadas().add(difCambio);
	}
	
	@Override
	protected BigDecimal diferenciasCambioMoneda1NoImputadas(){
		if (this.diferenciasCambiosMoneda1 == null){
			this.diferenciasCambiosMoneda1 = BigDecimal.ZERO;
			if ((this.getSaldo1().compareTo(BigDecimal.ZERO) != 0) || (this.getSaldo2().compareTo(BigDecimal.ZERO) != 0)){
				StringBuffer sql = new StringBuffer();
				sql.append("select sum(i.diferenciaCambio) from ").append(Esquema.concatenarEsquema("ImputacionVenta i join "));
				sql.append(Esquema.concatenarEsquema("DiferenciaCambioVenta d on d.idTrOrigen = i.id and cumplido = 'f' and anulado = 'f' "));
				sql.append("where i.diferenciaCambio is not null and estado = :confirmado and i.monedaDifCambio_id = :moneda1 and ");
				if (this.ingresa()){
					sql.append("i.origen_id = :id and i.diferenciaCambio < 0 ");
				}
				else{
					sql.append("i.destino_id = :id and i.diferenciaCambio > 0 ");
				}
				Query query = XPersistence.getManager().createNativeQuery(sql.toString());
				query.setParameter("confirmado", Estado.Confirmada.ordinal());
				query.setParameter("id", this.getId());
				query.setParameter("moneda1", this.getMoneda1().getId());
				query.setFlushMode(FlushModeType.COMMIT);
				List<?> results = query.getResultList();
				if (!results.isEmpty()){
					if (results.get(0) != null){
						diferenciasCambiosMoneda1 = (BigDecimal)results.get(0);
					}				
				}
							
			}
		}
		return this.diferenciasCambiosMoneda1;
	}
	
	@Override
	public Imputacion crearImputacion(CuentaCorriente origen, CuentaCorriente destino){
		ImputacionVenta imputacion = new ImputacionVenta();
		imputacion.setCliente(((CuentaCorrienteVenta)origen).getCliente());		
		imputacion.setOrigen((CuentaCorrienteVenta)origen);
		imputacion.setDestino((CuentaCorrienteVenta)destino);
		return imputacion;
	}

	public String getIdLiquidacion() {
		return idLiquidacion;
	}

	public void setIdLiquidacion(String idLiquidacion) {
		this.idLiquidacion = idLiquidacion;
	}

	public BigDecimal getComision() {
		return comision;
	}

	public void setComision(BigDecimal comision) {
		this.comision = comision;
	}

	public BigDecimal getPorcentajeComision() {
		return porcentajeComision;
	}

	public void setPorcentajeComision(BigDecimal porcentajeComision) {
		this.porcentajeComision = porcentajeComision;
	}
}
