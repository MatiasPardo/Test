package org.openxava.compras.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.base.validators.*;
import org.openxava.clasificadores.model.Clasificador;
import org.openxava.compras.MetricasProveedor;
import org.openxava.contabilidad.calculators.*;
import org.openxava.contabilidad.model.*;
import org.openxava.cuentacorriente.model.*;
import org.openxava.impuestos.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.calculators.*;
import org.openxava.negocio.model.*;
import org.openxava.tesoreria.model.Banco;
import org.openxava.tesoreria.model.SucursalBanco;
import org.openxava.ventas.calculators.CondicionVentaPrincipalCalculator;
import org.openxava.ventas.model.CondicionVenta;
import org.openxava.ventas.model.TipoCuentaBancaria;

@Entity

@Views({
	@View(members=
			"Principal{" + 
				"Principal[" +
					"codigo, nombre;" + 
					"tipoDocumento, numeroDocumento, tipo;" +
					"moneda, condicionCompra;" +
					"contacto, telefono;" + 
					"mail1, mail2, web];" +
				"domicilio;" + 				
				"observaciones}" +
			"Clasificadores{" +
				"proveedorClasificador1, proveedorClasificador2, proveedorClasificador3;}" +	
			"Auditoria{" +
			 	"fechaCreacion;" + 
			 	"usuario;" +
			 	"activo;}" + 
			 "Contabilidad{" +
			 	"cuentaContableCompras;}" +  
			 "Impuestos{" + 
			 	"posicionIva, numeroIIBB, condicionIIBB;" + 
			 	"retencionCABA[retencionCABA];" + 		 	 	
			 	"retencionARBA[retencionARBA];" + 
			 	"retenciones;}" +	
			 "Banco{banco, sucursalBancaria; cuentaBancaria; tipoCuenta, numeroCuenta; claveBancariaUniforme}"
				),
	@View(name="Simple",
			members="codigo, nombre"),
	@View(name="Transaccion",
		members="codigo, nombre;"),
	
	@View(name="CuentaCorriente",
		members="Principal[codigo, nombre];" +	
			"Saldos[calculos];" + 		 	
			"CtaCteAcumulada{cuentaCorrienteAcumulada}CtaCtePendiente{cuentaCorriente};") 		
		
})

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS),
	
	@Tab(name="CuentaCorriente",
		properties="codigo, nombre, activo, calculos.saldoCtaCteEmpresa1Moneda1, calculos.saldoCtaCteEmpresa2Moneda1, calculos.saldoCtaCteMoneda1"),
	
	@Tab(name="ImportacionCSV", 
		properties="codigo, nombre, tipoDocumento, numeroDocumento, contacto, telefono, mail1, mail2, web, posicionIva.codigo, cuentaContableCompras.codigo, domicilio.ciudad.codigoPostal, domicilio.direccion, domicilio.observaciones, observaciones, banco.codigo, cuentaBancaria, claveBancariaUniforme, " + 
					"proveedorClasificador1.codigo, proveedorClasificador2.codigo, proveedorClasificador3.codigo," +
					"numeroCuenta, sucursalBancaria.codigo, tipoCuenta",
		baseCondition=ObjetoEstatico.CONDITION_ACTIVOS)
})

@EntityValidators({
	@EntityValidator(
			value=UnicidadValidator.class, 
			properties= {
				@PropertyValue(name="id"), 
				@PropertyValue(name="atributo", value="codigo"),
				@PropertyValue(name="valor", from="codigo"),
				@PropertyValue(name="modelo", value="Proveedor"),
				@PropertyValue(name="idMessage", value="codigo_repetido")
				
			}
	)
})

public class Proveedor extends OperadorComercial{

	//final private static int CANTIDAD_MESES_PASADOS_CTACTE = 6;
	
	@DescriptionsList @NoCreate @NoModify
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private PosicionAnteImpuesto posicionIva;

	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	@DefaultValueCalculator(value=ObjetoPrincipalCalculator.class, 
			properties={@PropertyValue(name="entidad", value="Moneda")})
	private Moneda moneda;

	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre", condition="${compras} = 't'")
	@NoCreate @NoModify
	@DefaultValueCalculator(value=CondicionVentaPrincipalCalculator.class, 
			properties={@PropertyValue(name="ventas", value="false")})
	private CondicionVenta condicionCompra;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView(value="Simple")
	@NoCreate @NoModify
	@DefaultValueCalculator(value=CuentaContableComprasDefaultCalculator.class)
	private CuentaContable cuentaContableCompras;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
    @NoSearch
    @ReferenceView("Simple")
    @AsEmbedded
    @NoFrame
	private EntidadImpuesto retencionCABA;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
    @NoSearch
    @ReferenceView("Simple")
    @AsEmbedded
    @NoFrame
	private EntidadImpuesto retencionARBA;
	
	@OneToMany(mappedBy="proveedor", cascade=CascadeType.ALL)
	@ListProperties("impuesto.codigo, impuesto.nombre, alicuota.codigo, calcula")
	private Collection<EntidadRetencionProveedor> retenciones;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private Banco banco;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="numeroSucursal, banco.nombre",depends="this.banco",condition="${banco.id} = ?")
	private SucursalBanco sucursalBancaria;
	
	@Column(length=20)	
	private String cuentaBancaria;
	
	private TipoCuentaBancaria tipoCuenta;
	
	private Integer numeroCuenta;
	
	@Column(length=25, name="cbu")
	private String claveBancariaUniforme;
	
	@OneToOne(optional = true, fetch = FetchType.LAZY, targetEntity = MetricasProveedor.class, mappedBy = "proveedor")
	@ReadOnly
	@NoFrame
	@ReferenceViews({
		@ReferenceView(forViews = "CuentaCorriente", value = "CuentaCorriente")})
	private MetricasProveedor calculos;
	
	public Moneda getMoneda() {
		return moneda;
	}

	public void setMoneda(Moneda moneda) {
		this.moneda = moneda;
	}
	
	public CondicionVenta getCondicionCompra() {
		return condicionCompra;
	}

	public void setCondicionCompra(CondicionVenta condicionCompra) {
		this.condicionCompra = condicionCompra;
	}

	public CuentaContable getCuentaContableCompras() {
		return cuentaContableCompras;
	}

	public void setCuentaContableCompras(CuentaContable cuentaContableCompras) {
		this.cuentaContableCompras = cuentaContableCompras;
	}

	public EntidadImpuesto getRetencionCABA() {
		return retencionCABA;
	}

	public void setRetencionCABA(EntidadImpuesto retencionCABA) {
		this.retencionCABA = retencionCABA;
	}

	public EntidadImpuesto getRetencionARBA() {
		return retencionARBA;
	}

	public void setRetencionARBA(EntidadImpuesto retencionARBA) {
		this.retencionARBA = retencionARBA;
	}

	public Collection<EntidadRetencionProveedor> getRetenciones() {
		return retenciones;
	}

	public void setRetenciones(Collection<EntidadRetencionProveedor> retenciones) {
		this.retenciones = retenciones;
	}

	public PosicionAnteImpuesto getPosicionIva() {
		return posicionIva;
	}

	public void setPosicionIva(PosicionAnteImpuesto posicionIva) {
		this.posicionIva = posicionIva;
	}

	public EntidadRetencionProveedor configuracionImpuesto(Impuesto impuesto){
		EntidadRetencionProveedor entidad = null;
		if (impuesto.getTipo().equals(DefinicionImpuesto.RetencionGanancias) ||
			impuesto.getTipo().equals(DefinicionImpuesto.RetencionMonotributo) ||
			impuesto.getTipo().equals(DefinicionImpuesto.RetencionIva)){
			if (this.getRetenciones() != null){
				for(EntidadRetencionProveedor configuracion: this.getRetenciones()){
					if (configuracion.getImpuesto().equals(impuesto)){
						if (configuracion.getCalcula()){
							entidad = configuracion;
							break;
						}
					}
				}
			}
		}
		return entidad;
	}
	
	@OneToMany(mappedBy="proveedor", cascade=CascadeType.ALL)
	@ReadOnly
	@ListProperties("fecha, tipo, numero, importeOriginal, monedaOriginal.nombre, cotizacion, saldo1, saldo2, empresa.nombre")
	@Condition("${anulado} = 'f' AND ${pendiente} = 't' AND ${proveedor.id} = ${this.id}")
	@OrderBy("fechaCreacion desc") 
	@ViewAction("EditarGeneradoPorEnColeccion.view")
	private Collection<CuentaCorrienteCompra> cuentaCorriente;

	public Collection<CuentaCorrienteCompra> getCuentaCorriente() {
		return cuentaCorriente;
	}

	public void setCuentaCorriente(Collection<CuentaCorrienteCompra> cuentaCorriente) {
		this.cuentaCorriente = cuentaCorriente;
	}
	
	public void calcularSaldoCtaCteFecha(Date fecha, ArrayList<BigDecimal> saldos){
		
		String sql = "select sum(importe1) s1, sum(importe2) s2" + 
				 " from " + Esquema.concatenarEsquema("CuentaCorriente") + 
				 " where fecha <= :desde and proveedor_id = :id";
		Query query = XPersistence.getManager().createNativeQuery(sql);
		query.setParameter("desde", fecha);
		query.setParameter("id", this.getId());
		List<?> results = query.getResultList();
		BigDecimal saldo1 = BigDecimal.ZERO;
		BigDecimal saldo2 = BigDecimal.ZERO;
		if (!results.isEmpty()){
			Object[] res = (Object[])results.get(0);
			if (res[0] != null){
				saldo1 = (BigDecimal)res[0];
			}
			if (res[1] != null){
				saldo2 = (BigDecimal)res[1];
			}			
		}
		saldos.add(saldo1);
		saldos.add(saldo2);
	}

	public Banco getBanco() {
		return banco;
	}

	public void setBanco(Banco banco) {
		this.banco = banco;
	}

	public String getCuentaBancaria() {
		return cuentaBancaria;
	}

	public void setCuentaBancaria(String cuentaBancaria) {
		this.cuentaBancaria = cuentaBancaria;
	}

	public String getClaveBancariaUniforme() {
		return claveBancariaUniforme;
	}

	public void setClaveBancariaUniforme(String claveBancariaUniforme) {
		this.claveBancariaUniforme = claveBancariaUniforme;
	}
	
	@OneToMany(mappedBy="proveedor", cascade=CascadeType.ALL)
	@ReadOnly
	@ListProperties("fecha, tipo, numero, cotizacion, ingreso1, egreso1, saldoAcumulado1, ingreso2, egreso2, saldoAcumulado2, empresa.nombre, dias, fechaVencimiento, fechaCreacion")
	@Condition("${proveedor.id} = ${this.id}")
	@OrderBy("fecha desc, fechaCreacion desc") 
	@ViewAction("EditarGeneradoPorEnColeccion.view")
	private Collection<CtaCteCompraAcumulado> cuentaCorrienteAcumulada;
	
	public Collection<CtaCteCompraAcumulado> getCuentaCorrienteAcumulada() {
		return cuentaCorrienteAcumulada;
	}

	public void setCuentaCorrienteAcumulada(Collection<CtaCteCompraAcumulado> cuentaCorrienteAcumulada) {
		this.cuentaCorrienteAcumulada = cuentaCorrienteAcumulada;
	}
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre", 
			condition="${tipoClasificador.numero} = 1 and ${tipoClasificador.modulo} = 'Proveedor'" + Clasificador.CONDICION)
	private Clasificador proveedorClasificador1;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre", 
			condition="${tipoClasificador.numero} = 2 and ${tipoClasificador.modulo} = 'Proveedor'" + Clasificador.CONDICION)
	private Clasificador proveedorClasificador2;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre", 
		condition="${tipoClasificador.numero} = 3 and ${tipoClasificador.modulo} = 'Proveedor'" + Clasificador.CONDICION)
	private Clasificador proveedorClasificador3;

	public Clasificador getProveedorClasificador1() {
		return proveedorClasificador1;
	}

	public void setProveedorClasificador1(Clasificador proveedorClasificador1) {
		this.proveedorClasificador1 = proveedorClasificador1;
	}

	public Clasificador getProveedorClasificador2() {
		return proveedorClasificador2;
	}

	public void setProveedorClasificador2(Clasificador proveedorClasificador2) {
		this.proveedorClasificador2 = proveedorClasificador2;
	}

	public Clasificador getProveedorClasificador3() {
		return proveedorClasificador3;
	}

	public void setProveedorClasificador3(Clasificador proveedorClasificador3) {
		this.proveedorClasificador3 = proveedorClasificador3;
	}

	public SucursalBanco getSucursalBancaria() {
		return sucursalBancaria;
	}

	public void setSucursalBancaria(SucursalBanco sucursalBancaria) {
		this.sucursalBancaria = sucursalBancaria;
	}

	public TipoCuentaBancaria getTipoCuenta() {
		return tipoCuenta;
	}

	public void setTipoCuenta(TipoCuentaBancaria tipoCuenta) {
		this.tipoCuenta = tipoCuenta;
	}

	public Integer getNumeroCuenta() {
		return numeroCuenta;
	}

	public void setNumeroCuenta(Integer numeroCuenta) {
		this.numeroCuenta = numeroCuenta;
	}
	
	public MetricasProveedor getCalculos() {
		return calculos;
	}

	public void setCalculos(MetricasProveedor calculos) {
		this.calculos = calculos;
	}
}
