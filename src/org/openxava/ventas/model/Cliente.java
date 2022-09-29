package org.openxava.ventas.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.afip.model.ClienteFacturaExportacion;
import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.base.validators.*;
import org.openxava.calculators.*;
import org.openxava.clasificadores.model.*;
import org.openxava.contabilidad.calculators.*;
import org.openxava.contabilidad.model.*;
import org.openxava.cuentacorriente.model.*;
import org.openxava.fisco.calculators.TipoFacturacionCalculator;
import org.openxava.fisco.model.TipoFacturacion;
import org.openxava.impuestos.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.calculators.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.calculators.*;
import org.openxava.ventas.filter.*;

import com.allin.percepciones.model.*;

@Entity

@Views({
	@View(members=
		"Principal{" + 
			"Principal[" +
				"codigo, nombre;" + 
				"nombreFantasia, sinIdentificacion;" +
				"tipoDocumento, numeroDocumento, tipo, posicionIva, regimenFacturacion;" +
				"contacto, telefono;" + 
				"mail1, mail2, web;" +
				"consignacion, porcentajeComisiones];" +
			"domicilioLegal;" + 
			"vendedor;"	+
			"observaciones}" +
		"Clasificadores{" +
			"clienteClasificador1, clienteClasificador2, clienteClasificador3;}" + 	
		"Entrega{" +
			"lugaresEntrega;}" +  
		"Precios{" +
		 	"listaPrecio;" +
		 	"condicionVenta;" +
		 	"porcentajeDescuento, porcentajeFinanciero;" +
		 	"descuentosAplicadosPrecio}" +
		"Credito{" +
		 	"limiteCredito;" + 
		 	"SituacionCrediticiaActual[calculos];" +
		 	"cuentaCorriente}" +		 	
		"Auditoria{" +
		 	"fechaCreacion;" + 
		 	"usuario;" +
		 	"activo;}" + 
		 "Contabilidad{" +
		 	"cuentaContableVentas;}" +  
		 "Impuestos{" +
		 	"numeroIIBB, condicionIIBB;" +
		 	"percepcionCABA[" + 		 	 	
		 	 	"percepcionCABA];" + 
		 	 "percepcionARBA[" + 		 	 	
		 	 	"percepcionARBA]}" +
		 "Sucursales{" +
		 	"sucursal; sucursales;}"
			),
	@View(name="Simple",
			members="codigo, nombre"),
	@View(name="Transaccion",
		members="codigo, nombre;" +
				"numeroDocumento, posicionIva"),
	@View(name="VentaElectronica",
		members="codigo;"),
	@View(name="PedidoVenta",
		members="codigo, nombre;" +			
			"calculos, limiteCredito;"),
	@View(name="Cobranza",
		members="codigo, nombre;" +
				"calculos;"),
	@View(name="CuentaCorriente",
		members="Principal[codigo, nombre];" +	
				"calculos;" +		
				"CtaCteAcumulada{cuentaCorrienteAcumulada}CtaCtePendiente{cuentaCorriente};"  				
				) 		
			
})

@Tabs({
	@Tab(
		properties="codigo, nombre, activo, tipoDocumento, numeroDocumento, contacto, telefono, observaciones",
		filter=ClienteFilter.class,
		baseCondition=ClienteFilter.BASECONDITION
		),
	@Tab(
		properties="codigo, nombre, activo, tipoDocumento, numeroDocumento",
		name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS),
	@Tab(
		properties="codigo, nombre, activo, vendedor.nombre, calculos.saldoCtaCteEmpresa1Moneda1, calculos.saldoCtaCteEmpresa2Moneda1, calculos.saldoCtaCteMoneda1, calculos.saldoCtaCteMoneda1MesActual, calculos.saldoCtaCteMoneda1MesAnt1, calculos.saldoCtaCteMoneda1MesAnt2, calculos.saldoCtaCteMoneda1MesAnt3, calculos.saldoCtaCteMoneda1MesAnt4, calculos.saldoCtaCteMoneda1MesAnt5, calculos.saldoCtaCteMoneda1MesAnt6",
		name="CuentaCorriente",
		filter=ClienteFilter.class,
		baseCondition=ClienteFilter.BASECONDITION),
	@Tab(name="ImportacionCSV", 
		properties="codigo, nombre, nombreFantasia, tipoDocumento, numeroDocumento, posicionIva.codigo, tipo, contacto, telefono, mail1, mail2, web, vendedor.codigo, domicilio.ciudad.codigoPostal, domicilio.direccion, domicilio.observaciones, observaciones, cuentaContableVentas.codigo, clienteClasificador1.codigo, clienteClasificador2.codigo, clienteClasificador3.codigo, domicilioLegal.horario, domicilioLegal.zona.codigo, domicilioLegal.medioTransporte.codigo, domicilioLegal.frecuencia.codigo, domicilio.latitud, domicilio.longitud",
		baseCondition=ObjetoEstatico.CONDITION_ACTIVOS)
})

@EntityValidators({
	@EntityValidator(
			value=UnicidadValidator.class, 
			properties= {
				@PropertyValue(name="id"), 
				@PropertyValue(name="atributo", value="codigo"),
				@PropertyValue(name="valor", from="codigo"),
				@PropertyValue(name="modelo", value="Cliente"),
				@PropertyValue(name="idMessage", value="codigo_repetido")
				
			}
	)
})


public class Cliente extends OperadorComercial{
	
	/*public static Cliente buscarConsumidorFinal() {
		Query query = XPersistence.getManager().createQuery("from Cliente where activo = :activo and posicionIva.codigo = :consumidorFinal");
		query.setParameter("activo", Boolean.TRUE);
		query.setParameter("consumidorFinal", PosicionAnteImpuesto.CONSUMIDORFINAL);
		query.setMaxResults(1);		
		List<?> result = query.getResultList();
		if (!result.isEmpty()){
			return (Cliente) result.get(0);
		}
		else{
			return null;
		}
	}*/
	
	public static Cliente buscarSinIdentificacion() {
		Query query = XPersistence.getManager().createQuery("from Cliente where activo = :activo and sinIdentificacion = :sinIdentificacion");
		query.setParameter("activo", Boolean.TRUE);
		query.setParameter("sinIdentificacion", Boolean.TRUE);
		query.setMaxResults(1);		
		List<?> result = query.getResultList();
		if (!result.isEmpty()){
			return (Cliente) result.get(0);
		}
		else{
			return null;
		}
	}
	
	@Column(length=100) @Required
    private String nombreFantasia;
		
	@AsEmbedded
	@NoSearch
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private LugarEntregaMercaderia domicilioLegal;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean sucursal = Boolean.FALSE;
	
	@OneToMany(mappedBy="cliente", fetch=FetchType.LAZY)
	private Collection<SucursalCliente> sucursales;
	
	@DescriptionsList @NoCreate @NoModify
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private PosicionAnteImpuesto posicionIva;

	@DefaultValueCalculator(value=FalseCalculator.class)
	@ReadOnly
	private Boolean sinIdentificacion = Boolean.FALSE;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="tipo")
	@NoCreate @NoModify
	@DefaultValueCalculator(value=TipoFacturacionCalculator.class, 
		properties={@PropertyValue(name="entidad", value="TipoFacturacion")})
	private TipoFacturacion regimenFacturacion;
	
	@NoCreate
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@DefaultValueCalculator(value=ObjetoPrincipalCalculator.class, 
			properties={@PropertyValue(name="entidad", value="ListaPrecio")})
	private ListaPrecio listaPrecio;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre", condition="${ventas} = 't'")
	@NoCreate @NoModify
	@DefaultValueCalculator(value=CondicionVentaPrincipalCalculator.class)
	private CondicionVenta condicionVenta;
	
	@OneToMany(mappedBy="cliente", cascade=CascadeType.ALL)
	@ListProperties("marca.nombre, porcentajeComercial, tipoPorcentajeComercial")
	private Collection<DescuentoVenta> descuentosAplicadosPrecio;
			
	@OneToMany(mappedBy="cliente", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@ListProperties("codigo, nombre, domicilio.direccion, domicilio.ciudad.ciudad, horario, zona.nombre, medioTransporte.nombre")
	private Collection<LugarEntregaMercaderia> lugaresEntrega;
	
	@NoModify @NoCreate
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	private Vendedor vendedor;
	
	@DefaultValueCalculator(value=LimiteCreditoDefaultCalcultor.class)
	@PropertyValidator(value=PositiveValidator.class)
	@ReadOnly(forViews="CuentaCorriente")
	private BigDecimal limiteCredito;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView(value="Simple")
	@NoCreate @NoModify
	@DefaultValueCalculator(value=CuentaContableVentasDefaultCalculator.class)
	private CuentaContable cuentaContableVentas;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean consignacion = Boolean.FALSE;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre", 
			condition="${tipoClasificador.numero} = 1 and ${tipoClasificador.modulo} = 'Cliente'" + Clasificador.CONDICION)
	private Clasificador clienteClasificador1;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre", 
			condition="${tipoClasificador.numero} = 2 and ${tipoClasificador.modulo} = 'Cliente'" + Clasificador.CONDICION)
	private Clasificador clienteClasificador2;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre", 
		condition="${tipoClasificador.numero} = 3 and ${tipoClasificador.modulo} = 'Cliente'" + Clasificador.CONDICION)
	private Clasificador clienteClasificador3;
	
	@Min(value=0, message="No puede menor a 0")
	@Max(value=100, message="No puede ser mayor a 100")
	private BigDecimal porcentajeDescuento = BigDecimal.ZERO;
	
	@Min(value=0, message="No puede menor a 0")
	@Max(value=100, message="No puede ser mayor a 100")
	private BigDecimal porcentajeFinanciero = BigDecimal.ZERO;
	
	@Min(value=0, message="No puede menor a 0")
	@Max(value=100, message="No puede ser mayor a 100")
	@DefaultValueCalculator(value=ZeroBigDecimalCalculator.class)
	private BigDecimal porcentajeComisiones = BigDecimal.ZERO;
	
	@OneToOne(optional=true, fetch=FetchType.LAZY, targetEntity=MetricasCliente.class, mappedBy="cliente")
	@ReadOnly
	@NoFrame
	@ReferenceViews({
		@ReferenceView(forViews="DEFAULT", value="Cliente"),
		@ReferenceView(forViews="Cobranza", value="Cobranza"),
		@ReferenceView(forViews="CuentaCorriente", value="CuentaCorriente"),
		@ReferenceView(forViews="PedidoVenta", value="PedidoVenta")		
	})
	private MetricasCliente calculos;
	
	@OneToOne(mappedBy="cliente", optional=true, fetch=FetchType.LAZY, cascade=CascadeType.REMOVE)
	@ReferenceView("Cliente")
	@ReadOnly 
	private ClienteFacturaExportacion datosFacturaExportacion;
	
	public BigDecimal getPorcentajeDescuento() {
		return porcentajeDescuento == null ? BigDecimal.ZERO : this.porcentajeDescuento;
	}

	public void setPorcentajeDescuento(BigDecimal porcentajeDescuento) {
		this.porcentajeDescuento = porcentajeDescuento;
	}

	public BigDecimal getPorcentajeFinanciero() {
		return porcentajeFinanciero == null ? BigDecimal.ZERO : this.porcentajeFinanciero;
	}

	public void setPorcentajeFinanciero(BigDecimal porcentajeFinanciero) {
		this.porcentajeFinanciero = porcentajeFinanciero;
	}
	
	public Boolean getSucursal() {
		return sucursal == null ? Boolean.FALSE : this.sucursal;
	}

	public void setSucursal(Boolean sucursal) {
		this.sucursal = sucursal;
	}

	@SuppressWarnings("unchecked")
	public Collection<SucursalCliente> getSucursales() {
		return sucursales == null ? Collections.EMPTY_LIST : this.sucursales;
	}

	public void setSucursales(Collection<SucursalCliente> sucursales) {
		this.sucursales = sucursales;
	}

	public CuentaContable getCuentaContableVentas() {
		return cuentaContableVentas;
	}

	public void setCuentaContableVentas(CuentaContable cuentaContableVentas) {
		this.cuentaContableVentas = cuentaContableVentas;
	}
	
	public Vendedor getVendedor() {
		return vendedor;
	}

	public void setVendedor(Vendedor vendedor) {
		this.vendedor = vendedor;
	}

	public PosicionAnteImpuesto getPosicionIva() {
		return posicionIva;
	}

	public void setPosicionIva(PosicionAnteImpuesto posicionIva) {
		this.posicionIva = posicionIva;
	}

	public ListaPrecio getListaPrecio() {
		return listaPrecio;
	}

	public void setListaPrecio(ListaPrecio listaPrecio) {
		this.listaPrecio = listaPrecio;
	}

	public CondicionVenta getCondicionVenta() {
		return condicionVenta;
	}

	public void setCondicionVenta(CondicionVenta condicionVenta) {
		this.condicionVenta = condicionVenta;
	}

	public Collection<DescuentoVenta> getDescuentosAplicadosPrecio() {
		return descuentosAplicadosPrecio;
	}

	public void setDescuentosAplicadosPrecio(Collection<DescuentoVenta> descuentosAplicadosPrecio) {
		this.descuentosAplicadosPrecio = descuentosAplicadosPrecio;
	}

	public BigDecimal getLimiteCredito() {
		return limiteCredito == null ? BigDecimal.ZERO : limiteCredito;
	}

	public void setLimiteCredito(BigDecimal limiteCredito) {
		this.limiteCredito = limiteCredito;
	}
	
	public LugarEntregaMercaderia getDomicilioLegal() {
		return domicilioLegal;
	}

	public void setDomicilioLegal(LugarEntregaMercaderia domicilioLegal) {
		this.domicilioLegal = domicilioLegal;
	}

	public Boolean getConsignacion() {
		return consignacion;
	}

	public void setConsignacion(Boolean consignacion) {
		this.consignacion = consignacion;
	}

	public Clasificador getClienteClasificador1() {
		return clienteClasificador1;
	}

	public void setClienteClasificador1(Clasificador clienteClasificador1) {
		this.clienteClasificador1 = clienteClasificador1;
	}

	public Clasificador getClienteClasificador2() {
		return clienteClasificador2;
	}

	public void setClienteClasificador2(Clasificador clienteClasificador2) {
		this.clienteClasificador2 = clienteClasificador2;
	}

	public Clasificador getClienteClasificador3() {
		return clienteClasificador3;
	}

	public void setClienteClasificador3(Clasificador clienteClasificador3) {
		this.clienteClasificador3 = clienteClasificador3;
	}

	public BigDecimal calcularPrecio(ListaPrecio listaPrecio, Producto producto, UnidadMedida unidadMedida, BigDecimal cantidad, IVenta transaccion){
		BigDecimal precio = null;
		ListaPrecio lista = listaPrecio;
		if (lista == null){
			lista = this.getListaPrecio();
		}
		if (lista != null){
			// Cotizacion
			BigDecimal cotizacion = ((Transaccion)transaccion).buscarCotizacionTrConRespectoA(lista.getMoneda());
			if (cotizacion.compareTo(BigDecimal.ZERO) != 0){
				UnidadMedida uMedida = unidadMedida;
				if (unidadMedida == null){
					uMedida = producto.getUnidadMedida();
				}
				precio = lista.buscarPrecio(producto.getId(), uMedida.getId(), cantidad);
				if (precio != null){
					if (precio.compareTo(BigDecimal.ZERO) != 0){
						Empresa empresa = ((Transaccion)transaccion).getEmpresa();
						if (empresa != null && empresa.utilizaPrecioMasIva()){
							precio = producto.agregarIva(precio);							
						}
						precio = aplicarPoliticaComercial(producto, precio);
					}
					CondicionVenta tipoPago = transaccion.condicionVentaCalculoPrecio();
					if (tipoPago != null){
						precio = tipoPago.aplicarPorcentaje(precio);
					}
					// se convierte a la cotización de la transacción
					precio = precio.divide(cotizacion, 2, RoundingMode.HALF_EVEN);
				}
			}	
		}
		return precio;
	}
	
	public BigDecimal aplicarPoliticaComercial(Producto producto, BigDecimal precio){
		BigDecimal descuentoAplicado = precio;
		if (producto.getMarca() != null){
			Query query = XPersistence.getManager().createQuery("from DescuentoVenta d where d.marca = :marca and d.cliente = :cliente");
			query.setParameter("marca", producto.getMarca());
			query.setParameter("cliente", this);
			query.setMaxResults(1);
			try{
				DescuentoVenta descuentoVta = (DescuentoVenta)query.getSingleResult();
				if (descuentoVta != null){
					descuentoAplicado = precio.add(precio.multiply(descuentoVta.porcentajeComercialCalculo()).divide(new BigDecimal(100)));
				}
			}
			catch(Exception e){
				
			}
		}
		return descuentoAplicado;
	}

	public BigDecimal buscarDescuentoFinanciero() {
		return this.getPorcentajeFinanciero();
	}

	/*@Transient
	private boolean situacionCrediticiaCalculada = false;
	
	@Transient
	private BigDecimal situacionCrediticiaTemp = BigDecimal.ZERO;
	
	public BigDecimal getSituacionCrediticia(){
		if (!this.situacionCrediticiaCalculada){
			BigDecimal importe = this.saldoCuentaCorrientePorMoneda(2);
			importe = importe.add(this.importePedidosNoFacturados(2));
			this.situacionCrediticiaTemp = importe;
			this.situacionCrediticiaCalculada = true;
		}	
		return situacionCrediticiaTemp;
	}
	
	public BigDecimal getCreditoDisponible(){
		return this.getLimiteCredito().subtract(this.getSituacionCrediticia()); 
	}
	
	public BigDecimal getSaldoCtaCteMoneda1(){
		return this.saldoCuentaCorrientePorMoneda(1);
	}
	
	public BigDecimal getSaldoCtaCteMoneda2(){
		return this.saldoCuentaCorrientePorMoneda(2);
	}
	
	public BigDecimal getSaldoPedidos(){
		return this.importePedidosNoFacturados(2);
	}
	
	final private static int CANTIDAD_MESES_PASADOS_CTACTE = 6;
		
	@Transient
	private BigDecimal saldosCC[][] = null;
	
	private void sumarSaldoCC(int i, int j, BigDecimal importe){
		this.saldosCC[i][j] = this.saldosCC[i][j].add(importe);
	}
	
	private void inicializarSaldos(){
		if (this.saldosCC == null){
			this.saldosCC = new BigDecimal[CANTIDAD_MESES_PASADOS_CTACTE + 1][4];
			// se inicializa en cero
			for(int i = 0; i < this.saldosCC.length; i++){
				for (int j = 0; j < this.saldosCC[i].length; j++){
					this.saldosCC[i][j] = BigDecimal.ZERO;
				}
			}
			
			if (!this.esNuevo()){
				String sql = "select to_char(c.fechaVencimiento, 'YYYY/MM') vencimiento, sum(c.saldo1) s1, sum(c.saldo2) s2, e.numero empresa " +
					"from " + Esquema.concatenarEsquema("CuentaCorriente") + " c " + 
					"join " + Esquema.concatenarEsquema("Empresa") + " e on e.id = c.empresa_id " +
					"where cliente_id = :cliente " + 
					"group by vencimiento, e.numero " +
					"having sum(c.saldo1) != 0 or sum(c.saldo2) != 0 " +
					"order by vencimiento desc";
				Query query = XPersistence.getManager().createNativeQuery(sql);
				query.setParameter("cliente", this.getId());
				List<?> results = query.getResultList();
				
				SimpleDateFormat format = new SimpleDateFormat("yyyy/MM");
				Date mes = new Date();
				String mesActual = format.format(mes);
				Calendar c = Calendar.getInstance();
				c.setTime(mes);												
				int i = CANTIDAD_MESES_PASADOS_CTACTE;
				for(Object result: results){
					Object[] fila = (Object[])result;
					String periodo = (String)fila[0];
					BigDecimal saldo1 = (BigDecimal)fila[1];
					BigDecimal saldo2 = (BigDecimal)fila[2];
					Integer nroEmpresa = (Integer)fila[3];
					if (periodo.compareTo(mesActual) >= 0){
						this.sumarSaldoCC(i, nroEmpresa*2 - 2, saldo1);
						this.sumarSaldoCC(i, nroEmpresa*2 - 1, saldo2);						
					}
					else{
						while((periodo.compareTo(mesActual) < 0) && (i > 0)){
							c.add(Calendar.MONTH,  -1);
							mes = c.getTime();
							mesActual = format.format(mes);
							i = i - 1;
						}
						this.sumarSaldoCC(i, nroEmpresa*2 - 2, saldo1);
						this.sumarSaldoCC(i, nroEmpresa*2 - 1, saldo2);						
					}
				}
			}
		}		
	}
	
	
	
	private BigDecimal saldoCuentaCorrientePorMes(Integer nroEmpresa, Integer moneda, Integer mes){
		inicializarSaldos();
		// cada fila es un mes
		// La primer fila, corresponde al ultimo mes anterior, o sea: -6 (CANTIDAD_MESES_PASADOS_CTACTE)
		// Las columnas son: empresa1-moneda1 / empresa1-moneda2 / empresa2-moneda1 / empresa2-moneda2
		int i = mes + CANTIDAD_MESES_PASADOS_CTACTE;		
		int j = nroEmpresa*2 + moneda - 3;
		return saldosCC[i][j];
	}
	
	private BigDecimal saldoCuentaCorrientePorEmpresa(Integer nroEmpresa, Integer moneda){
		inicializarSaldos();
		
		BigDecimal saldo = BigDecimal.ZERO;
		for(int i = 0; i < this.saldosCC.length; i++){		
			int j = nroEmpresa*2 + moneda - 3;
			saldo = saldo.add(this.saldosCC[i][j]);	
		}
		
		return saldo;
	}
	
	private BigDecimal saldoCuentaCorrientePorMoneda(Integer moneda){
		inicializarSaldos();		
		BigDecimal saldo = BigDecimal.ZERO;
		for(int i = 0; i < this.saldosCC.length; i++){		
			saldo = saldo.add(this.saldosCC[i][moneda-1]).add(this.saldosCC[i][moneda+1]);	
		}		
		return saldo;
	}
	
	public BigDecimal getSaldoCtaCteEmpresa1Moneda1(){		
		return this.saldoCuentaCorrientePorEmpresa(1, 1);
	}
	
	public BigDecimal getSaldoCtaCteEmpresa2Moneda1(){
		return this.saldoCuentaCorrientePorEmpresa(2, 1);
	}

	public BigDecimal getSaldoCtaCteEmpresa1Moneda2(){		
		return this.saldoCuentaCorrientePorEmpresa(1, 2);
	}
	
	public BigDecimal getSaldoCtaCteEmpresa2Moneda2(){
		return this.saldoCuentaCorrientePorEmpresa(2, 2);
	}
	
	public BigDecimal getSaldoCtaCteMoneda1MesActual(){
		return this.saldoCuentaCorrientePorMes(1, 1, 0).add(this.saldoCuentaCorrientePorMes(2, 1, 0));
	}
	
	public BigDecimal getSaldoCtaCteMoneda1MesAnt1(){
		return this.saldoCuentaCorrientePorMes(1, 1, -1).add(this.saldoCuentaCorrientePorMes(2, 1, -1));
	}
	
	public BigDecimal getSaldoCtaCteMoneda1MesAnt2(){
		return this.saldoCuentaCorrientePorMes(1, 1, -2).add(this.saldoCuentaCorrientePorMes(2, 1, -2));
	}
	
	public BigDecimal getSaldoCtaCteMoneda1MesAnt3(){
		return this.saldoCuentaCorrientePorMes(1, 1, -3).add(this.saldoCuentaCorrientePorMes(2, 1, -3));
	}
	
	public BigDecimal getSaldoCtaCteMoneda1MesAnt4(){
		return this.saldoCuentaCorrientePorMes(1, 1, -4).add(this.saldoCuentaCorrientePorMes(2, 1, -4));
	}
	
	public BigDecimal getSaldoCtaCteMoneda1MesAnt5(){
		return this.saldoCuentaCorrientePorMes(1, 1, -5).add(this.saldoCuentaCorrientePorMes(2, 1, -5));
	}
	
	public BigDecimal getSaldoCtaCteMoneda1MesAnt6(){
		return this.saldoCuentaCorrientePorMes(1, 1, -6).add(this.saldoCuentaCorrientePorMes(2, 1, -6));
	}
	
	public BigDecimal getSaldoCtaCteMoneda2MesActual(){
		return this.saldoCuentaCorrientePorMes(1, 2, 0).add(this.saldoCuentaCorrientePorMes(2, 2, 0));
	}
	
	public BigDecimal getSaldoCtaCteMoneda2MesAnt1(){
		return this.saldoCuentaCorrientePorMes(1, 2, -1).add(this.saldoCuentaCorrientePorMes(2, 2, -1));
	}
	
	public BigDecimal getSaldoCtaCteMoneda2MesAnt2(){
		return this.saldoCuentaCorrientePorMes(1, 2, -2).add(this.saldoCuentaCorrientePorMes(2, 2, -2));
	}
	
	public BigDecimal getSaldoCtaCteMoneda2MesAnt3(){
		return this.saldoCuentaCorrientePorMes(1, 2, -3).add(this.saldoCuentaCorrientePorMes(2, 2, -3));
	}
	
	public BigDecimal getSaldoCtaCteMoneda2MesAnt4(){
		return this.saldoCuentaCorrientePorMes(1, 2, -4).add(this.saldoCuentaCorrientePorMes(2, 2, -4));
	}
	
	public BigDecimal getSaldoCtaCteMoneda2MesAnt5(){
		return this.saldoCuentaCorrientePorMes(1, 2, -5).add(this.saldoCuentaCorrientePorMes(2, 2, -5));
	}
	
	public BigDecimal getSaldoCtaCteMoneda2MesAnt6(){
		return this.saldoCuentaCorrientePorMes(1, 2, -6).add(this.saldoCuentaCorrientePorMes(2, 2, -6));
	}
	
	private BigDecimal importePedidosNoFacturados(Integer nroMoneda){
		BigDecimal importe = BigDecimal.ZERO;
		if (!Is.emptyString(this.getId())){
			String columnaImporte = "suma" + nroMoneda.toString();
			String sql = "select coalesce(sum(importe), 0) from ( "+ 
							   "select sum((i." + columnaImporte + " * i.pendientePreparacion/ i.cantidad)) importe from " + Esquema.concatenarEsquema("estadisticapedidoventa") + " i " +
							   "join " + Esquema.concatenarEsquema("pedidoventa") + " p on p.id = i.venta_id and p.estado = :estado " +
							   "where p.cliente_id = :cliente " +  
							   "and i.pendientePreparacion > 0 and i.cantidad > 0 and i.suma > 0 " +
							 "union all " +
							   "select sum((iv." + columnaImporte + " * i.cantidad / iv.cantidad)) importe from " + Esquema.concatenarEsquema("itemordenpreparacion") + " i " + 
							   "join " + Esquema.concatenarEsquema("ordenpreparacion") + " o on o.id = i.ordenpreparacion_id and o.estado = :estado " +
							   "join " + Esquema.concatenarEsquema("estadisticapedidoventa") + " iv on iv.id = i.itempedidoventa_id " + 
							   "where i.remitido = :noFacturado and o.cliente_id = :cliente " +
							   "and i.cantidad > 0 and iv.suma > 0 and iv.cantidad > 0 " +							 
							")PedidosPendientes";
			Query query = XPersistence.getManager().createNativeQuery(sql);
			query.setParameter("cliente", this.getId());
			query.setParameter("noFacturado", Boolean.FALSE);
			query.setParameter("estado", Estado.Confirmada.ordinal());
			importe = (BigDecimal)query.getSingleResult();
		}
		return importe;
	}
	*/
	@OneToMany(mappedBy="cliente", cascade=CascadeType.ALL)
	@ReadOnly
	@ListProperties("fecha, tipo, numero, importeOriginal, monedaOriginal.nombre, cotizacion, saldo1, saldo2, empresa.nombre")
	@Condition("${anulado} = 'f' AND ${pendiente} = 't' AND ${cliente.id} = ${this.id}")
	@OrderBy("fechaCreacion desc") 
	@ViewAction("EditarGeneradoPorEnColeccion.view")
	private Collection<CuentaCorrienteVenta> cuentaCorriente;

	public Collection<CuentaCorrienteVenta> getCuentaCorriente() {
		return cuentaCorriente;
	}

	public void setCuentaCorriente(Collection<CuentaCorrienteVenta> cuentaCorriente) {
		this.cuentaCorriente = cuentaCorriente;
	}

	@OneToMany(mappedBy="cliente", cascade=CascadeType.ALL)
	@ReadOnly
	@ListProperties("fecha, tipo, numero, cotizacion, ingreso1, egreso1, saldoAcumulado1, ingreso2, egreso2, saldoAcumulado2, empresa.nombre, dias, fechaVencimiento, fechaCreacion")
	@Condition("${cliente.id} = ${this.id}")
	@OrderBy("fecha desc, fechaCreacion desc") 
	@ViewAction("EditarGeneradoPorEnColeccion.view")
	private Collection<CtaCteVentaAcumulado> cuentaCorrienteAcumulada;
	
	public Collection<CtaCteVentaAcumulado> getCuentaCorrienteAcumulada() {
		return cuentaCorrienteAcumulada;
	}

	public void setCuentaCorrienteAcumulada(Collection<CtaCteVentaAcumulado> cuentaCorrienteAcumulada) {
		this.cuentaCorrienteAcumulada = cuentaCorrienteAcumulada;
	}

	
	public Collection<LugarEntregaMercaderia> getLugaresEntrega() {
		return lugaresEntrega;
	}

	public void setLugaresEntrega(Collection<LugarEntregaMercaderia> lugaresEntrega) {
		this.lugaresEntrega = lugaresEntrega;
	}

	@ManyToOne(optional=true, fetch=FetchType.LAZY)
    @NoSearch
    @ReferenceView("Simple")
    @AsEmbedded
    @NoFrame
	private EntidadImpuesto percepcionCABA;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
    @NoSearch
    @ReferenceView("Simple")
    @AsEmbedded
    @NoFrame
	private EntidadImpuesto percepcionARBA;
	
	@OneToOne(optional=true, fetch=FetchType.LAZY)
	@PrimaryKeyJoinColumn
	@ReadOnly
	@NoFrame
	@ReferenceView(value="Cliente")
	private MetricasTransaccionesCliente comprobantes;
	
	public EntidadImpuesto getPercepcionCABA() {
		return percepcionCABA;
	}

	public void setPercepcionCABA(EntidadImpuesto percepcionCABA) {
		this.percepcionCABA = percepcionCABA;
	}
	
	public EntidadImpuesto getPercepcionARBA() {
		return percepcionARBA;
	}

	public void setPercepcionARBA(EntidadImpuesto percepcionARBA) {
		this.percepcionARBA = percepcionARBA;
	}

	public BigDecimal buscarAlicuotaPercepcionCABA(Date fecha) {
		BigDecimal alicuota = null;
		if (this.getPercepcionCABA() != null){
			EntidadImpuesto entidadImpuesto = this.getPercepcionCABA();
			// Primero se revisa si esta la alicuota cacheada en la entidadImpuesto, sino se busca en el padrón
			if (entidadImpuesto.tieneAlicuota(fecha)){
				alicuota = entidadImpuesto.getAlicuotaVigente();
			}
			else{
				PadronCABA padron = new PadronCABA();
				try{
					AlicuotaPadron alicuotaPadron = padron.buscarAlicuota(fecha, this.getNumeroDocumento(), TipoAlicuotaPadron.Percepcion);
					entidadImpuesto.asignarAlicuota(alicuotaPadron.getAlicuota(), alicuotaPadron.getDesde(), alicuotaPadron.getHasta());
					alicuota = alicuotaPadron.getAlicuota();
				}
				catch(Exception e){
					String error = e.getMessage();
					if (Is.emptyString(error)){
						error = e.toString();
					}
					throw new ValidationException("Error al buscar alicuota de percepción CABA: " + error);
				}
				
			}			
		}
		return alicuota;
	}
	
	public BigDecimal buscarAlicuotaPercepcionBsAs(Date fecha) {
		BigDecimal alicuota = null;
		if (this.getPercepcionARBA() != null){
			EntidadImpuesto entidadImpuesto = this.getPercepcionARBA();
			// Primero se revisa si esta la alicuota cacheada en la entidadImpuesto, sino se busca en el padrón
			if (entidadImpuesto.tieneAlicuota(fecha)){
				alicuota = entidadImpuesto.getAlicuotaVigente();
			}
			else{
				PadronARBA padron = new PadronARBA();
				padron.setUsuario(Esquema.getEsquemaApp().getUsuarioARBA());
				padron.setClave(Esquema.getEsquemaApp().getClaveARBA());
				try{
					AlicuotaPadron alicuotaPadron = padron.buscarAlicuota(fecha, this.getNumeroDocumento(), TipoAlicuotaPadron.Percepcion);
					entidadImpuesto.asignarAlicuota(alicuotaPadron.getAlicuota(), alicuotaPadron.getDesde(), alicuotaPadron.getHasta());
					alicuota = alicuotaPadron.getAlicuota();
				}
				catch(Exception e){
					String error = e.getMessage();
					if (Is.emptyString(error)){
						error = e.toString();
					}
					throw new ValidationException("Error al buscar alicuota de percepción BsAs: " + error);
				}
				
			}			
		}
		return alicuota;
	}
	
	public Domicilio domicilioEntregaPrincipal() {
		Domicilio domicilio = null;
		if (this.getLugaresEntrega() != null){
			for(LugarEntregaMercaderia lugarEntrega: this.getLugaresEntrega()){
				if (lugarEntrega.getPrincipal()){
					domicilio = lugarEntrega.getDomicilio();
					break;
				}
				else if (domicilio == null){
					domicilio = lugarEntrega.getDomicilio();
				}
			}
		}
		
		if (domicilio == null){
			domicilio = this.getDomicilio();
		}
		return domicilio;
	}
	
	@Override
	protected void onPostPersist(){
		super.onPostPersist();
		this.getDomicilioLegal().setCliente(this);
		this.setDomicilio(this.getDomicilioLegal().getDomicilio());
	}
	
	@Override
	public void onPreDelete(){
		super.onPreDelete();
		this.setDomicilioLegal(null);
		this.setDomicilio(null);		
	}

	public LugarEntregaMercaderia lugarEntrega(Domicilio domicilioEntrega) {
		LugarEntregaMercaderia lugar = null;
		if (this.getLugaresEntrega() != null){
			for(LugarEntregaMercaderia l: this.getLugaresEntrega()){
				if (l.getDomicilio().equals(domicilioEntrega)){
					lugar = l;
					break;
				}
			}
		}
		
		return lugar;
	}

	public BigDecimal getPorcentajeComisiones() {
		return porcentajeComisiones == null ? BigDecimal.ZERO: porcentajeComisiones;
	}

	public void setPorcentajeComisiones(BigDecimal porcentajeComisiones) {
		this.porcentajeComisiones = porcentajeComisiones;
	}
	
	public void calcularSaldoCtaCteFecha(Date fecha, Collection<String> empresas, ArrayList<BigDecimal> saldos){
		
		String sql = "select sum(importe1) s1, sum(importe2) s2" + 
				 " from " + Esquema.concatenarEsquema("CuentaCorriente") + 
				 " where fecha < :desde and cliente_id = :cliente and empresa_id in (:empresas)";
		Query query = XPersistence.getManager().createNativeQuery(sql);
		query.setParameter("desde", fecha);
		query.setParameter("cliente", this.getId());
		query.setParameter("empresas", empresas);
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
	
	public TipoFacturacion getRegimenFacturacion() {
		return regimenFacturacion;
	}

	public void setRegimenFacturacion(TipoFacturacion regimenFacturacion) {
		this.regimenFacturacion = regimenFacturacion;
	}

	public String getNombreFantasia() {
		return nombreFantasia;
	}

	public void setNombreFantasia(String nombreFantasia) {
		this.nombreFantasia = nombreFantasia;
	}
	
	public Boolean getSinIdentificacion() {
		return sinIdentificacion == null ? Boolean.FALSE : sinIdentificacion;
	}

	public void setSinIdentificacion(Boolean sinIdentificacion) {
		this.sinIdentificacion = sinIdentificacion;
	}

	public MetricasTransaccionesCliente getComprobantes() {
		return comprobantes;
	}

	public void setComprobantes(MetricasTransaccionesCliente comprobantes) {
		this.comprobantes = comprobantes;
	}
	
	public ClienteFacturaExportacion getDatosFacturaExportacion() {
		return datosFacturaExportacion;
	}

	public void setDatosFacturaExportacion(ClienteFacturaExportacion datosFacturaExportacion) {
		this.datosFacturaExportacion = datosFacturaExportacion;
	}

	public MetricasCliente getCalculos() {
		return calculos;
	}

	public void setCalculos(MetricasCliente calculos) {
		this.calculos = calculos;
	}
}
