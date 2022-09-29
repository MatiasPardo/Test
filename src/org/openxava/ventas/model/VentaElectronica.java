package org.openxava.ventas.model;

import java.math.*;
import java.text.*;
import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.apache.commons.lang3.time.*;
import org.openxava.afip.calculators.*;
import org.openxava.afip.model.ClienteFacturaExportacion;
import org.openxava.afip.model.ConfiguracionAfip;
import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;
import org.openxava.codigobarras.model.IControlCodigoBarra;
import org.openxava.codigobarras.model.IItemControlCodigoBarras;
import org.openxava.contabilidad.model.*;
import org.openxava.contratos.model.Contrato;
import org.openxava.cuentacorriente.model.*;
import org.openxava.distribucion.model.ZonaReparto;
import org.openxava.fisco.model.RegimenFacturacionFiscal;
import org.openxava.fisco.model.TipoComprobante;
import org.openxava.impuestos.model.*;
import org.openxava.inventario.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.calculators.ObjetoPrincipalCalculator;
import org.openxava.negocio.model.*;
import org.openxava.tesoreria.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.actions.*;
import org.openxava.ventas.calculators.*;

import com.allin.interfacesafip.model.*;
import com.allin.interfacesafip.util.*;

@Entity

@Views({
	@View(name="Simple", 
		members="numero, tipo, estado"), 
	@View(name="Reversion", 
		members="numero, tipo, ctacte"),
	@View(name="FacturaVentaContado", members=
		"Principal{ Principal[#" + 
				"descripcion, moneda, cotizacion;" +
				"fecha, fechaVencimiento, fechaCreacion;" +
				"empresa, puntoVenta, tipo;" + 
				"numero, cae, fechaVencimientoCAE;" +
				"estado, subestado;" + 
				"Cliente[cliente, razonSocial;" + 
					"cuit, posicionIva, tipoDocumento;" + 
					"listaPrecio, email];" +
				"Domicilio[direccion;ciudad];" + 
				"observaciones];" + 
		"Descuentos[#" +
				"porcentajeDescuento, porcentajeFinanciero];" +			
		"items; " + 
		"subtotalSinDescuento;" +
		"descuento;" + 		
		"subtotal;" + 
		"iva, percepcion1, percepcion2;" + 
		"total;}" +
		"Trazabilidad{trazabilidad} CuentaCorriente{ctacte}; "  
	) 
})

@Tab(
	filter=EmpresaFilter.class,
	baseCondition=EmpresaFilter.BASECONDITION,
	properties="fecha, numero, tipo.tipo, estado, tipoOperacion, cae, fechaVencimientoCAE, cliente.codigo, cliente.nombre, total, subtotal, iva, descuento, subtotalSinDescuento",
	defaultOrder="${fechaCreacion} desc")

public class VentaElectronica extends Transaccion implements ITransaccionCtaCte, ITransaccionContable, ITrCalculaPercepcionVenta, IDestinoEMail, IVenta, IControlCodigoBarra{
	
	public final static String ACCIONSOLICITARCAE = "VentaCAE.solicitarCAE";
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate 
    @NoModify
    @ReferenceView("VentaElectronica")
	@NoFrame
	@OnChange(OnChangeClienteVentaElectronicaAction.class)
	private Cliente cliente;
	
	@Column(length=75)
	@Stereotype("EMAIL") 
	@Action(value="ModificarAtributoConAuditoria.Cambiar", alwaysEnabled=true, notForViews="CambioEmail")
	private String email;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate 
    @NoModify
    @DescriptionsList(descriptionProperties="nombre", 
    				condition="${costo} = 'f'")
	private ListaPrecio listaPrecio;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceViews({
		@ReferenceView(value="Simple", notForViews="FacturaVentaContado"),
		@ReferenceView(value="CodigoExterno", forViews="FacturaVentaContado"),
	})
	@SearchAction(value="ReferenciaDomicilioVenta.buscar")
	@NoFrame(forViews="FacturaVentaContado")
	private Domicilio domicilioEntrega;
	
	@Required @DefaultValueCalculator(CurrentDateCalculator.class) 
	private Date fechaVencimiento = new Date();
	
	@Required @DefaultValueCalculator(CurrentDateCalculator.class)
	private Date fechaServicio = new Date();
	
	/*no funciona, uso el onchange
	@DefaultValueCalculator(  
			value=TipoComprobanteCalculator.class,
			properties={@PropertyValue(name="codigoPosicionIVA", from="posicionIva.codigo"),
						@PropertyValue(name="idPuntoVenta", from="puntoVenta.id")}
		)*/
	@Required
	@ReadOnly
    @DescriptionsList(descriptionProperties="tipo")
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@JoinColumn(name="tipo", nullable=false)
	private TipoComprobante tipo;
	
	@Column(length=15)
	@ReadOnly
	private String tipoOperacion;
	
	@DescriptionsList @NoCreate @NoModify
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DefaultValueCalculator(PuntoVentaDefaultCalculator.class)
	@OnChange(OnChangePuntoVenta.class)
	private PuntoVenta puntoVenta;
	
	@Column(length=20)
	@ReadOnly
	private String cae;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre", condition="${ventas} = 't'")
	@NoCreate @NoModify
	@DefaultValueCalculator(  
			value=ObjetoPrincipalCalculator.class,
			properties={@PropertyValue(name="entidad", value="CondicionVenta")})		
	private CondicionVenta condicionVenta;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	private ZonaReparto zonaReparto;
	
	@DefaultValueCalculator(CurrentDateCalculator.class)
	@ReadOnly
	@Stereotype("DATETIME") 
	private Date fechaVencimientoCAE = new Date();
	
	@Digits(integer=19, fraction=4)
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal total = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal total1;
	
	@ReadOnly
	private BigDecimal total2;
	
	@org.hibernate.annotations.Formula("total1 * coeficiente")
	private BigDecimal totalCtaCte1;
	
	public BigDecimal getTotalCtaCte1() {
		return totalCtaCte1;
	}
	
	@org.hibernate.annotations.Formula("total2 * coeficiente")
	private BigDecimal totalCtaCte2;
	
	public BigDecimal getTotalCtaCte2() {
		return totalCtaCte2;
	}

	@Digits(integer=19, fraction=4)
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal iva = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal iva1;
	
	@ReadOnly
	private BigDecimal iva2;
	
	@Digits(integer=19, fraction=4)
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal impuestosInternos = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal impuestosInternos1;
	
	@ReadOnly
	private BigDecimal impuestosInternos2;
	
	@Digits(integer=19, fraction=4)
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal percepcion1 = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal percepcion11 = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal percepcion12 = BigDecimal.ZERO;
	
	@Digits(integer=19, fraction=4)
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal percepcion2 = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal percepcion21 = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal percepcion22 = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal alicuota1 = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal alicuota2 = BigDecimal.ZERO;
	
	@Digits(integer=19, fraction=4)
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal subtotal = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal subtotal1;
	
	@ReadOnly
	private BigDecimal subtotal2;
	
	@ReadOnly
	private Boolean intercompany = Boolean.FALSE; 
	
	@org.hibernate.annotations.Formula("subtotal1 * coeficiente")
	private BigDecimal subtotalCtaCte1;
	
	public BigDecimal getSubtotalCtaCte1() {
		return subtotalCtaCte1;
	}
	
	@org.hibernate.annotations.Formula("subtotal2 * coeficiente")
	private BigDecimal subtotalCtaCte2;

	public BigDecimal getSubtotalCtaCte2() {
		return subtotalCtaCte2;
	}
	
	@Min(value=0, message="No puede menor a 0")
	@Max(value=100, message="No puede ser mayor a 100")
	private BigDecimal porcentajeDescuento = BigDecimal.ZERO;
	
	@Min(value=0, message="No puede menor a 0")
	@Max(value=100, message="No puede ser mayor a 100")
	@ReadOnly(forViews="Reversion")
	private BigDecimal porcentajeFinanciero = BigDecimal.ZERO;
	
	@Digits(integer=19, fraction=4)
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal descuento = BigDecimal.ZERO;
	
	@Digits(integer=19, fraction=4)
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal subtotalSinDescuento = BigDecimal.ZERO;
	
	@Required
	private TipoDocumento tipoDocumento;
	
	@Column(length=20)
	@Action(alwaysEnabled=false, value="VentaElectronica.validar", forViews="FacturaVentaContado")
	private String cuit;
	
	@Column(length=100) 
	@Required
	private String razonSocial;
	
	@DescriptionsList(descriptionProperties="descripcion") 
	@NoCreate @NoModify
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
	@OnChange(OnChangePosicionIva.class)
    private PosicionAnteImpuesto posicionIva;
	
	@Column(length=100) 
	private String direccion;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
    @ReferenceView("Simple")
	@NoCreate @NoModify
	private Ciudad ciudad;
		
	@OneToMany(mappedBy="venta", cascade=CascadeType.ALL)
	@ListProperties("producto.codigo, producto.nombre, cantidad, precioUnitario, descuento, suma, descuentoGlobal, descuentoFinanciero, subtotal, tasaiva, iva")
	@NoCreate(forViews="Reversion")
	@SaveAction(value="ItemTransaccion.save")
	@NewAction(value="ItemTransaccion.new")
	@HideDetailAction(value="ItemTransaccion.hideDetail")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	@EditAction("ItemTransaccion.edit")
	@ListAction("LectorCodigoBarras.CrearPorCodigoBarra")
	private Collection<ItemVentaElectronica> items;
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Transaccion")
	private CuentaCorrienteVenta ctacte;

	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly
	@ReferenceView("Reversion")
	private VentaElectronica revierte;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly	
	private Remito remito;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly
	private LiquidacionConsignacion liquidacion;
	
	@Column(length=32)
	@ReadOnly
	@Hidden
	private String idObjetoAsociado;
	
	// Para asociar una ventaElectronica con otra: ejemplo Intercompany
	@Column(length=32)
	@ReadOnly
	@Hidden
	private String idCreadaPor;
	
	@ReadOnly
	@Hidden
	@Column(length=100)
	private String tipoEntidadCreadaPor;
	
	@Hidden
	@ReadOnly
	private Integer coeficiente = 0;
	
	public CuentaCorrienteVenta getCtacte() {
		return ctacte;
	}


	public void setCtacte(CuentaCorrienteVenta ctacte) {
		this.ctacte = ctacte;
	}


	public Cliente getCliente() {
		return cliente;
	}


	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Domicilio getDomicilioEntrega() {
		return domicilioEntrega;
	}


	public void setDomicilioEntrega(Domicilio domicilioEntrega) {
		this.domicilioEntrega = domicilioEntrega;
	}


	public Date getFechaVencimiento() {
		return fechaVencimiento;
	}


	public void setFechaVencimiento(Date fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}

	public Date getFechaServicio() {
		return fechaServicio;
	}

	public void setFechaServicio(Date fechaServicio) {
		if (fechaServicio != null){
			this.fechaServicio = fechaServicio;
		}
		else{
			this.fechaServicio = this.getFecha();
		}
	}

	public TipoComprobante getTipo() {
		return tipo;
	}


	public void setTipo(TipoComprobante tipo) {
		this.tipo = tipo;
	}

	public String getTipoOperacion() {
		return tipoOperacion;
	}


	public void setTipoOperacion(String tipoOperacion) {
		this.tipoOperacion = tipoOperacion;
	}


	public PuntoVenta getPuntoVenta() {
		return puntoVenta;
	}


	public void setPuntoVenta(PuntoVenta puntoVenta) {
		this.puntoVenta = puntoVenta;
	}


	public String getCae() {
		return cae;
	}


	public void setCae(String cae) {
		this.cae = cae;
	}


	public Date getFechaVencimientoCAE() {
		return fechaVencimientoCAE;
	}


	public void setFechaVencimientoCAE(Date fechaVencimientoCAE) {
		this.fechaVencimientoCAE = fechaVencimientoCAE;
	}


	public BigDecimal getTotal() {
		return total == null ? BigDecimal.ZERO : this.total;
	}


	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public BigDecimal getTotal1() {
		return total1 == null ? BigDecimal.ZERO : this.total1;
	}

	public void setTotal1(BigDecimal total1) {
		
		this.total1 = total1;
	}

	public BigDecimal getTotal2() {
		return total2 == null ? BigDecimal.ZERO : this.total2;
	}

	public void setTotal2(BigDecimal total2) {
		this.total2 = total2;
	}

	public BigDecimal getSubtotal1() {
		return subtotal1  == null ? BigDecimal.ZERO : this.subtotal1;
	}

	public void setSubtotal1(BigDecimal subtotal1) {
		this.subtotal1 = subtotal1;
	}

	public BigDecimal getSubtotal2() {
		return subtotal2 == null ? BigDecimal.ZERO : this.subtotal2;
	}

	public void setSubtotal2(BigDecimal subtotal2) {
		this.subtotal2 = subtotal2;
	}


	public BigDecimal getIva() {
		return iva == null ? BigDecimal.ZERO : this.iva;
	}


	public void setIva(BigDecimal iva) {
		this.iva = iva;
	}

	public BigDecimal getIva1() {
		return iva1 == null ? BigDecimal.ZERO : iva1;
	}

	public void setIva1(BigDecimal iva1) {
		this.iva1 = iva1;
	}


	public BigDecimal getIva2() {
		return iva2 == null ? BigDecimal.ZERO : iva2;
	}


	public void setIva2(BigDecimal iva2) {
		this.iva2 = iva2;
	}


	public BigDecimal getPercepcion1() {
		return percepcion1 == null ? BigDecimal.ZERO : this.percepcion1;
	}
	
	public BigDecimal getPercepcion11() {
		return percepcion11 == null ? BigDecimal.ZERO : this.percepcion11;
	}


	public void setPercepcion11(BigDecimal percepcion11) {
		this.percepcion11 = percepcion11;
	}


	public BigDecimal getPercepcion12() {
		return percepcion12 == null ? BigDecimal.ZERO : this.percepcion12;
	}


	public void setPercepcion12(BigDecimal percepcion12) {
		this.percepcion12 = percepcion12;
	}


	public void setPercepcion1(BigDecimal percepcion1) {
		this.percepcion1 = percepcion1;
	}

	public BigDecimal getPercepcion2() {
		return percepcion2 == null ? BigDecimal.ZERO : this.percepcion2;
	}

	public void setPercepcion2(BigDecimal percepcion2) {
		this.percepcion2 = percepcion2;
	}

	public BigDecimal getPercepcion21() {
		return percepcion21 == null ? BigDecimal.ZERO : percepcion21;
	}

	public void setPercepcion21(BigDecimal percepcion21) {
		this.percepcion21 = percepcion21;
	}

	public BigDecimal getPercepcion22() {
		return percepcion22 == null ? BigDecimal.ZERO : percepcion22;
	}

	public void setPercepcion22(BigDecimal percepcion22) {
		this.percepcion22 = percepcion22;
	}

	public BigDecimal getAlicuota1() {
		return alicuota1 == null ? BigDecimal.ZERO : this.alicuota1;
	}

	public void setAlicuota1(BigDecimal alicuota1) {
		this.alicuota1 = alicuota1;
	}

		
	public BigDecimal getAlicuota2() {
		return alicuota2 == null ? BigDecimal.ZERO : this.alicuota2;
	}

	public void setAlicuota2(BigDecimal alicuota2) {
		this.alicuota2 = alicuota2;
	}


	public BigDecimal getSubtotal() {
		return subtotal == null ? BigDecimal.ZERO : this.subtotal;
	}


	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}


	public BigDecimal getPorcentajeDescuento() {
		return this.porcentajeDescuento == null ? BigDecimal.ZERO : this.porcentajeDescuento;
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


	public BigDecimal getDescuento() {
		return descuento == null ? BigDecimal.ZERO : this.descuento;
	}


	public void setDescuento(BigDecimal descuento) {
		this.descuento = descuento;
	}


	public BigDecimal getSubtotalSinDescuento() {
		return subtotalSinDescuento == null ? BigDecimal.ZERO : this.subtotalSinDescuento;
	}


	public void setSubtotalSinDescuento(BigDecimal subtotalSinDescuento) {
		this.subtotalSinDescuento = subtotalSinDescuento;
	}


	public Collection<ItemVentaElectronica> getItems() {
		return items;
	}

	public void setItems(Collection<ItemVentaElectronica> items) {
		this.items = items;
	}
		
	public String getCuit() {
		return cuit;
	}
	
	public void setCuit(String cuit) {
		if (Is.emptyString(cuit)){
			this.cuit = cuit;
		}
		else{
			this.cuit = cuit.trim();
		}
	}
	
	public String getDireccion() {
		return direccion;
	}


	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}


	public Ciudad getCiudad() {
		return ciudad;
	}


	public void setCiudad(Ciudad ciudad) {
		this.ciudad = ciudad;
	}

	@Override
	public Boolean numeraSistema(){
		return !this.debeAutorizaAfip();		
	}

	@Override
	public Date CtaCteFecha() {
		return this.getFecha();
	}


	@Override
	public BigDecimal CtaCteImporte() {
		return this.getTotal();
	}

	@Override
	public BigDecimal CtaCteNeto() {
		return this.getSubtotal();
	}
	
	@Override
	public String CtaCteTipo() {
		throw new ValidationException("Debe definir el método CtaCteTipo");
	}


	@Override
	public Cliente CtaCteOperadorComercial() {
		return this.getCliente();
	}

	@Override
	public IResponsableCuentaCorriente CtaCteResponsable() {
		if (getCliente() != null){
			return this.getCliente().getVendedor();
		}
		else{
			return null;
		}
	}

	@Override
	public Date CtaCteFechaVencimiento() {
		return this.getFechaVencimiento();
	}


	@Override
	public String CtaCteNumero() {
		return this.getNumero();
	}


	@Override
	public Transaccion CtaCteTransaccion() {
		return this;
	}
	
	@Override
	public boolean generadaPorDiferenciaCambio(){
		return false;
	}
	
	@Override
	public void detalleDiferenciaCambio(Collection<DiferenciaCambioVenta> detalleDifCambio){
		if (this.generadaPorDiferenciaCambio()){
			for(ItemVentaElectronica item: this.getItems()){
				DiferenciaCambioVenta difCambio = item.generadoPorDiferenciaCambio();
				if (difCambio != null){
					detalleDifCambio.add(difCambio);
				}
			}
		}
	}
	
	protected boolean cumpleCondicionesRecalculoTotales(){
		return super.cumpleCondicionesRecalculoTotales();
	}
	
	@Override
	public void recalcularTotales(){
		super.recalcularTotales();
		
		if (this.getListaPrecio() == null){
			this.setListaPrecio(this.getCliente().getListaPrecio());
		}
		
		if (!this.getCliente().getSinIdentificacion()){
			this.setPosicionIva(this.getCliente().getPosicionIva());
		}
						
		Boolean calcularImpuestos = this.calculaImpuestos(); 
		this.setSubtotalSinDescuento(BigDecimal.ZERO);
		this.setSubtotal(BigDecimal.ZERO);
		this.setIva(BigDecimal.ZERO);
		this.setImpuestosInternos(BigDecimal.ZERO);
		this.setDescuento(BigDecimal.ZERO);
		this.setTotal(BigDecimal.ZERO);
		
		List<ItemVentaElectronica> itemsInteresesCalculados = new LinkedList<ItemVentaElectronica>();
		boolean calcularIntereses = this.calculaIntereses();
		
		if (this.getItems() != null){
			for(ItemVentaElectronica item: this.getItems()){				
				if (calcularIntereses && item.esInteres()){
					itemsInteresesCalculados.add(item);
				}
				else{
					item.recalcular();
					BigDecimal importeSinDescuentoItem = item.getSumaSinDescuento();
					BigDecimal subtotalItem = item.getSubtotal();
					BigDecimal descuentoItem = importeSinDescuentoItem.subtract(subtotalItem);
					this.setSubtotalSinDescuento(this.getSubtotalSinDescuento().add(importeSinDescuentoItem));
					this.setDescuento(this.getDescuento().add(descuentoItem));
					this.setSubtotal(this.getSubtotal().add(subtotalItem));
					if (calcularImpuestos){
						this.setIva(this.getIva().add(item.getIva()));
						this.setImpuestosInternos(this.getImpuestosInternos().add(item.getImpuestoInterno()));
					}
					else{
						this.setIva(BigDecimal.ZERO);
					}
				}
			}
			
			BigDecimal totalPercepciones = this.calcularPercepciones(calcularImpuestos);
			BigDecimal total = this.calcularTotal(totalPercepciones);
			
			if (calcularIntereses){
				ItemVentaElectronica itemInteres = null;
				if (!itemsInteresesCalculados.isEmpty()){
					for(ItemVentaElectronica item: itemsInteresesCalculados){
						// debería ser 1 solo item el de crédito, pero si hay más, se eliminan
						if (itemInteres == null){
							itemInteres = item;
						}
						else{
							this.getItems().remove(item);
							XPersistence.getManager().remove(item);
						}						
					}					
				}
								
				BigDecimal intereses = this.calcularIntereses(total);
				if (intereses.compareTo(BigDecimal.ZERO) > 0){
					if (itemInteres == null){
						itemInteres = new ItemVentaElectronica();
					}
					itemInteres.setVenta(this);
					itemInteres.setProducto(Producto.buscarInteres());
					itemInteres.setCantidad(new BigDecimal(1));
					itemInteres.calcularPrecioUnitarioSegunImporteTotal(intereses);
					itemInteres.setAutomatico(true);
					itemInteres.recalcular();					
					this.getItems().add(itemInteres);
					if (itemInteres.esNuevo()){
						XPersistence.getManager().persist(itemInteres);
					}
					
					// se recalculan los totales
					BigDecimal subtotalItem = itemInteres.getSubtotal();
					this.setSubtotalSinDescuento(this.getSubtotalSinDescuento().add(subtotalItem));
					this.setSubtotal(this.getSubtotal().add(subtotalItem));
					if (calcularImpuestos){
						this.setIva(this.getIva().add(itemInteres.getIva()));
						this.setImpuestosInternos(this.getImpuestosInternos().add(itemInteres.getImpuestoInterno()));
					}
					else{
						this.setIva(BigDecimal.ZERO);
					}
					
					totalPercepciones = this.calcularPercepciones(calcularImpuestos);
					total = this.calcularTotal(totalPercepciones);
				}
				else if (itemInteres != null){
					this.getItems().remove(itemInteres);
					XPersistence.getManager().remove(itemInteres);
				}
			}
						
			// ASIGNACION TOTAL
			this.setTotal(total);
						
			// se hace al final de todo porque necesita que los importes de la factura estén calculados
			TipoComprobanteCalculator defaultCalculator = new TipoComprobanteCalculator();
			defaultCalculator.setPosicion(this.getPosicionIva());
			defaultCalculator.setPuntoVenta(this.getPuntoVenta()); 
			defaultCalculator.setVentaElectronica(this);
			try{
				this.setTipo((TipoComprobante)defaultCalculator.calculate());
			} catch(Exception e){
			}
			
		}
	}
	
	private BigDecimal calcularTotal(BigDecimal totalPercepciones){
		return this.getSubtotal().add(this.getIva()).add(totalPercepciones).add(this.getImpuestosInternos());
	}
	
	private BigDecimal calcularPercepciones(boolean calcularImpuestos){
		BigDecimal totalPercepciones = BigDecimal.ZERO;
		if (calcularImpuestos){
			this.getEmpresa().calcularPercepciones(this);
			for (int i=1; i <= Empresa.CANTIDADPERCEPCIONESVENTA; i++){
				try {
					totalPercepciones = totalPercepciones.add((BigDecimal)this.getClass().getMethod("getPercepcion" + Integer.toString(i)).invoke(this));
				} catch (Exception e) {
					throw new ValidationException("Error al sumar percepciones " + e.toString());
				}
			}	
		}
		else{
			this.ponerEnCeroPercepciones();
		}
		return totalPercepciones;
	}
	
	private BigDecimal calcularIntereses(BigDecimal total){
		Collection<InteresFacturacionVenta> financiamiento = this.financiamiento();
		BigDecimal intereses = BigDecimal.ZERO;
		BigDecimal importeParaFinanciar = total;
		Map<String, Object> condicionesFinanciamiento = new HashMap<String, Object>();
		
		for(InteresFacturacionVenta distribucionInteres: financiamiento){
			if (condicionesFinanciamiento.containsKey(distribucionInteres.getCondicionVenta().getId())){
				throw new ValidationException("En la financiación no puede estar repetida la condición de venta");
			}
			else{
				condicionesFinanciamiento.put(distribucionInteres.getCondicionVenta().getId(), null);
			}
			
			if (importeParaFinanciar.compareTo(BigDecimal.ZERO) >= 0){					
				if (distribucionInteres.getImporte().compareTo(BigDecimal.ZERO) == 0){
					// se refinancia el saldo
					intereses = intereses.add(importeParaFinanciar.multiply(distribucionInteres.getCondicionVenta().getPorcentajeInteres()).divide(new BigDecimal(100), 4, RoundingMode.HALF_EVEN));							 
					importeParaFinanciar = BigDecimal.ZERO;
				}
				else{
					intereses = intereses.add(distribucionInteres.getImporte().multiply(distribucionInteres.getCondicionVenta().getPorcentajeInteres()).divide(new BigDecimal(100), 4, RoundingMode.HALF_EVEN));
					importeParaFinanciar = importeParaFinanciar.subtract(distribucionInteres.getImporte());
					if (importeParaFinanciar.compareTo(BigDecimal.ZERO) < 0){
						throw new ValidationException("El importe para financiar no puede superar el total");
					}
				}
			}
			else{
				throw new ValidationException("El importe para financiar no puede superar el total");
			}
		}
		return intereses.setScale(2, RoundingMode.HALF_EVEN);
	}
	
	public void ponerEnCeroPercepciones(){
		for (int i=1; i <= Empresa.CANTIDADPERCEPCIONESVENTA; i++){
			try{
				this.getClass().getMethod("setPercepcion" + Integer.toString(i), BigDecimal.class).invoke(this, BigDecimal.ZERO);
			} catch (Exception e) {
				throw new ValidationException("Poner en cero las percepciones: " + e.toString());
			}	
		}
	}
	
	public Boolean calculaImpuestos(){
		Boolean calcula = Boolean.TRUE;
		if (this.getEmpresa() != null){
			calcula = this.getEmpresa().getInscriptoIva();
			if (calcula){
				calcula = !this.getEmpresa().esMonotributista();
			}
		}
		if (calcula){
			calcula = this.getTipo().calculaImpuestos();
		}
		return calcula;
	}
	
	protected boolean calculaIntereses(){
		return false;
	}
	
	protected Collection<InteresFacturacionVenta> financiamiento(){
		return new LinkedList<InteresFacturacionVenta>();
	}
		
	@Override
	public void copiarPropiedades(Object objeto){
		super.copiarPropiedades(objeto);
		this.setItems(null);
		this.setCae("");
		this.setNumero("");
		this.setNumeroInterno(new Long(0));
		this.setSubtotal(BigDecimal.ZERO);
		this.setDescuento(BigDecimal.ZERO);
		this.setSubtotalSinDescuento(BigDecimal.ZERO);
		this.setTotal(BigDecimal.ZERO);
		this.setIva(BigDecimal.ZERO);
		this.setCtacte(null);
		this.setFechaVencimiento(this.getFecha());
		
		this.setRevierte(null);
		this.asignarCreadoPor(null);
		this.setIdObjetoAsociado(null);		
	}
	
	@Override
	protected void inicializar(){
		super.inicializar();
		this.setCoeficiente(this.CtaCteCoeficiente());
		if (this.getPuntoVenta() == null){
			PuntoVentaDefaultCalculator defaultCalculator = new PuntoVentaDefaultCalculator();
			defaultCalculator.setCliente(this.getCliente());
			try {
				this.setPuntoVenta((PuntoVenta)defaultCalculator.calculate());
			} catch (Exception e) {
			}
		}
		
		if (this.getCondicionVenta() == null){
			if (this.getCliente() != null && this.getCliente().getCondicionVenta() != null){
				this.setCondicionVenta(this.getCliente().getCondicionVenta());
			}
			else{
				CondicionVentaPrincipalCalculator calculator = new CondicionVentaPrincipalCalculator();
				try{
					this.setCondicionVenta((CondicionVenta)calculator.calculate());
				}
				catch(Exception e){
				}
			}
		}
		
		if (this.getTipo() == null){
			if (this.getCliente() != null){
				if (this.getPosicionIva() == null){
					this.setPosicionIva(this.getCliente().getPosicionIva());
				}
				TipoComprobanteCalculator defaultCalculator = new TipoComprobanteCalculator();
				if (cliente != null){
					defaultCalculator.setPosicion(this.getPosicionIva());
					defaultCalculator.setPuntoVenta(this.getPuntoVenta());
					try{
						this.setTipo((TipoComprobante)defaultCalculator.calculate());
					} catch(Exception e){
					
					}
				}
			}
		}
	}
	
	@Override
	public void agregarParametrosImpresion(Map<String, Object> parameters) {
		super.agregarParametrosImpresion(parameters);
		
		parameters.put("CAE", this.getCae());
		parameters.put("FECHAVENCIMIENTOCAE", DateFormat.getDateInstance(DateFormat.LONG).format(this.getFechaVencimientoCAE()));
		parameters.put("FECHAVENCIMIENTOCAEDATE", this.getFechaVencimientoCAE());
		parameters.put("FECHAVENCIMIENTODATE", this.getFechaVencimiento());
		parameters.put("TIPO", this.getTipo().toString());
		parameters.put("RAZONSOCIAL_CLIENTE", this.getRazonSocial());
		parameters.put("CODIGO_CLIENTE", this.getCliente().getCodigo());
		parameters.put("CUIT_CLIENTE", this.getCuit());
		parameters.put("TIPODOCUMENTO_CLIENTE", this.getTipoDocumento().toString());
		parameters.put("POSICIONIVA_CLIENTE", this.getPosicionIva().getDescripcion());
		parameters.put("DIRECCION_CLIENTE", this.getDireccion());
		parameters.put("CODIGOPOSTAL_CLIENTE", this.getCiudad().getCodigoPostal().toString());
		parameters.put("CONTACTO_CLIENTE", this.getCliente().getContacto());
		parameters.put("CIUDAD_CLIENTE", this.getCiudad().getCiudad());
		parameters.put("PROVINCIA_CLIENTE", this.getCiudad().getProvincia().getProvincia());
		parameters.put("PERCEPCIONES", this.getPercepcion1().add(this.getPercepcion2()));
		parameters.put("PERCEPCION1", this.getPercepcion1());
		parameters.put("PERCEPCION2", this.getPercepcion2());
		parameters.put("IVA", this.getIva());
		parameters.put("IMPUESTOSINTERNOS", this.getImpuestosInternos());
		parameters.put("SUBTOTAL", this.getSubtotal());
		parameters.put("TOTAL", this.getTotal());
		parameters.put("DESCUENTOS", this.getDescuento());
		parameters.put("PORCENTAJEDESCUENTOGLOBAL", this.getPorcentajeDescuento());
		parameters.put("SUBTOTALSINDESCUENTO", this.getSubtotalSinDescuento());
		
		parameters.put("COTIZACION", this.getCotizacion());
		parameters.put("TOTAL1", this.getTotal1());
		parameters.put("SUBTOTAL1", this.getSubtotal1());
		parameters.put("IVA1", this.getIva1());
		parameters.put("IMPUESTOSINTERNOS1", this.getImpuestosInternos1());
		parameters.put("PERCEPCION1_1", this.getPercepcion11());
		parameters.put("PERCEPCION2_1", this.getPercepcion21());		
		parameters.put("SUBTOTALSINDESCUENTO1", this.convertirImporteEnMoneda1Tr(this.getMoneda(), this.getSubtotalSinDescuento()));
		parameters.put("DESCUENTOS1", this.convertirImporteEnMoneda1Tr(this.getMoneda(), this.getDescuento()));
		
		parameters.put("CODIGO_PUNTOVENTA", this.getPuntoVenta().getCodigo());
		parameters.put("NOMBRE_PUNTOVENTA", this.getPuntoVenta().getNombre());
		Domicilio domicilioPuntoVta = this.domicilioPuntoVenta();
		parameters.put("DIRECCION_PUNTOVENTA", domicilioPuntoVta.getDireccion());
		parameters.put("CIUDAD_PUNTOVENTA", domicilioPuntoVta.getCiudad().getCiudad());
		parameters.put("PROVINCIAL_PUNTOVENTA", domicilioPuntoVta.getCiudad().getProvincia().getProvincia());
		parameters.put("CODIGOPOSTAL_PUNTOVENTA", domicilioPuntoVta.getCiudad().getCodigoPostal());
		
		ObjetoEstatico vendedor = (ObjetoEstatico)this.CtaCteResponsable();
		if (vendedor != null){
			parameters.put("VENDEDOR_CODIGO", vendedor.getCodigo());
			parameters.put("VENDEDOR_NOMBRE", vendedor.getNombre());
		}
		else{
			parameters.put("VENDEDOR_CODIGO", "");
			parameters.put("VENDEDOR_NOMBRE", "");
		}
		
		if (!this.getTipo().discriminaIVA()){
			BigDecimal subtotalSinDescMasIVA = BigDecimal.ZERO;
			for(ItemVentaElectronica item: this.getItems()){
				subtotalSinDescMasIVA = subtotalSinDescMasIVA.add(item.sumarIVA(item.getSumaSinDescuento()));
			}
			BigDecimal subtotalMasIVA = this.getSubtotal().add(this.getIva());
			BigDecimal descuentosMasIVA = subtotalMasIVA.subtract(subtotalSinDescMasIVA);
			parameters.put("SUBTOTALSINDESCUENTOMASIVA", subtotalSinDescMasIVA);
			parameters.put("DESCUENTOSMASIVA", descuentosMasIVA);
			parameters.put("SUBTOTALMASIVA", subtotalMasIVA);
			
			parameters.put("SUBTOTALSINDESCUENTOMASIVA1", this.convertirImporteEnMoneda1Tr(this.getMoneda(), subtotalSinDescMasIVA));
			parameters.put("DESCUENTOSMASIVA1", this.convertirImporteEnMoneda1Tr(this.getMoneda(), descuentosMasIVA));
			parameters.put("SUBTOTALMASIVA1", this.convertirImporteEnMoneda1Tr(this.getMoneda(), subtotalMasIVA));
		}
		else{
			parameters.put("SUBTOTALSINDESCUENTOMASIVA", BigDecimal.ZERO);
			parameters.put("DESCUENTOSMASIVA", BigDecimal.ZERO);
			parameters.put("SUBTOTALMASIVA", BigDecimal.ZERO);
			
			parameters.put("SUBTOTALSINDESCUENTOMASIVA1", BigDecimal.ZERO);
			parameters.put("DESCUENTOSMASIVA1", BigDecimal.ZERO);
			parameters.put("SUBTOTALMASIVA1", BigDecimal.ZERO);
		}
		
		if (this.debeAutorizaAfip()){
			String codigoBarras = "";
			try{
				AfipGeneradorCodigoBarras generadorCodigoBarras = new AfipGeneradorCodigoBarras(this.getCuit(), 						
						this.getTipo().codigoFiscal(this.getClass().getSimpleName()), this.getPuntoVenta().getNumero(), this.getCae(), this.getFechaVencimientoCAE());
				codigoBarras = generadorCodigoBarras.generarCodigoBarras();
			}
			catch(Exception ex){
				
			}
			parameters.put("CODIGOBARRAS", codigoBarras);
			
			String observacionesMonotributista = "";
			if (this.getPosicionIva().esMonotributista() && this.getTipo().tipoComprobanteAfip().discriminaIVA()){
				observacionesMonotributista = ConfiguracionAfip.buscarObservacionMonotributista();
			}
			parameters.put("OBSERVACIONES_MONOTRIBUTISTA", observacionesMonotributista);
			
			String codigoQR = "INVALIDO";
			if (!Is.emptyString(this.getCae())){
				boolean esCaea = false;
				try{
					AfipCodigoQR generadorQR = new AfipCodigoQR();
					
					codigoQR = generadorQR.codigoQRFacturacion(this.getFecha(), this.getEmpresa().getCuit(), this.getPuntoVenta().getNumero(), 
							this.AfipTipoComprobante(), this.getNumeroInterno(), this.getTotal(), 
							this.monedaAfip(), this.cotizacionAfip(), this.getTipoDocumento().getCodigoAfip().toString(), this.getCuit(), 
							this.getCae(), esCaea);
				}
				catch(Exception ex){
					codigoQR = "ERRORQR";
				}
			}
			parameters.put("CODIGOQR", codigoQR);
		}
		
		String totalLetras = "";
		try{
			totalLetras = NumberToLetterConverter.convertNumberToLetter(this.getTotal());
		}
		catch(Exception e){
		}
		parameters.put("TOTALLETRAS", totalLetras);
		
		String totalLetras1 = "";
		try{
			totalLetras1 = NumberToLetterConverter.convertNumberToLetter(this.getTotal1());
		}
		catch(Exception e){
		}
		parameters.put("TOTALLETRAS1", totalLetras1);
		
		if (this.getCondicionVenta() != null){
			parameters.put("CONDICIONVENTA_CODIGO", this.getCondicionVenta().getCodigo());
			parameters.put("CONDICIONVENTA_NOMBRE", this.getCondicionVenta().getNombre());
		}
		else{
			parameters.put("CONDICIONVENTA_CODIGO", "");
			parameters.put("CONDICIONVENTA_NOMBRE", "");
		}
		
		if (this.getPuntoVenta().getTipo().equals(TipoPuntoVenta.ExportacionElectronico)){
			ClienteFacturaExportacion exportacion = this.getCliente().getDatosFacturaExportacion();
			if (exportacion != null){
				parameters.put("EXPORTACION_IDIOMA", exportacion.getIdioma().toString());
				parameters.put("EXPORTACION_CODIGOIDIOMA", exportacion.getIdioma().getCodigo());
				parameters.put("EXPORTACION_INCOTERMS", exportacion.getIncoterms().toString());
				parameters.put("EXPORTACION_DESCRIPCIONINCOTERMS", exportacion.getIncotermsDescripcion());
			}
		}
	}


	@Override
	public String descripcionTipoTransaccion() {
		throw new ValidationException("Falta Implementar el método descripcionTipoTransaccion");
	}
	
	@Override
	protected void atributosConversionesMoneda(List<String> atributos){
		super.atributosConversionesMoneda(atributos);
		atributos.add("Total");
		atributos.add("Subtotal");
		atributos.add("Iva");
		atributos.add("ImpuestosInternos");
		atributos.add("Percepcion1");
		atributos.add("Percepcion2");
	}


	@Override
	public CuentaCorriente CtaCteNuevaCuentaCorriente() {
		return new CuentaCorrienteVenta();
	}

	@Override
	public boolean generaContabilidad(){
		return true;
	}
	
	@Override
	public void generadorPasesContable(Collection<IGeneradorItemContable> items) {
		int comparacion = this.CtaCteCoeficiente().compareTo(0);
		if (comparacion != 0){
			
			for(ItemVentaElectronica itemVenta: this.getItems()){
				itemVenta.agregarPasesContables(items);				
			}
			CuentaContable cuenta;
						
			// IVA
			Impuesto impuesto = Impuesto.buscarPorDefinicionImpuesto(DefinicionImpuesto.IvaVenta);
			cuenta = TipoCuentaContable.Impuesto.CuentaContablePorTipo(impuesto);
			GeneradorItemContablePorTr paseIva = new GeneradorItemContablePorTr(this, cuenta);
			if (comparacion > 0){
				paseIva.setHaber(this.getIva());
			}
			else{
				paseIva.setDebe(this.getIva());
			}
			items.add(paseIva);
						
			// Percepciones
			for(int i=1; i <= Empresa.CANTIDADPERCEPCIONESVENTA; i++){
				try {
					BigDecimal percepcion = (BigDecimal)this.getClass().getMethod("getPercepcion" + Integer.toString(i)).invoke(this);
					if (percepcion.compareTo(BigDecimal.ZERO) != 0){
						impuesto = Impuesto.buscarPorDefinicionImpuesto((DefinicionImpuesto)this.getEmpresa().getClass().getMethod("getPercepcion" + Integer.toString(i)).invoke(this.getEmpresa()));
						cuenta = TipoCuentaContable.Impuesto.CuentaContablePorTipo(impuesto);
						GeneradorItemContablePorTr pasePercepcion = new GeneradorItemContablePorTr(this, cuenta);
						if (comparacion > 0){
							pasePercepcion.setHaber(percepcion);
						}
						else{
							pasePercepcion.setDebe(percepcion);
						}
						items.add(pasePercepcion);
					}
				} catch (Exception e) {
					throw new ValidationException("Error en contabilización de las percepciones " + e.toString());
				}
				
			}
			
			// Cliente			
			cuenta = TipoCuentaContable.Ventas.CuentaContablePorTipo(this.getCliente());
			GeneradorItemContablePorTr paseCliente = new GeneradorItemContablePorTr(this, cuenta);			
			if (comparacion > 0){
				paseCliente.setDebe(this.getTotal());
			}
			else{
				paseCliente.setHaber(this.getTotal());
			}
			items.add(paseCliente);
		}
	}
	
	@Override
	public void grabarTransaccion(){
		super.grabarTransaccion();
		
		// se aplanan los datos del cliente y el domicilio
		if (this.getCliente() != null){
			if (!this.getCliente().getSinIdentificacion()){
				this.copiarDatosCliente();
			}
			else{
				this.copiarDatosClienteSinID();
			}
		}		
	}

	private void copiarDatosCliente(){
		this.setCuit(this.getCliente().getNumeroDocumento());
		this.setRazonSocial(this.getCliente().getNombre());
		this.setTipoDocumento(this.getCliente().getTipoDocumento());
		if (this.getDomicilioEntrega() == null){
			this.setDomicilioEntrega(this.getCliente().getDomicilio());
		}
		if (this.getDomicilioEntrega() != null){
			this.setDireccion(this.getDomicilioEntrega().getDireccion());
			this.setCiudad(this.getDomicilioEntrega().getCiudad());
		}
		if (Is.emptyString(this.getEmail())){
			this.setEmail(this.getCliente().getMail1());
		}
	}
	
	private void copiarDatosClienteSinID(){
		if (this.getDomicilioEntrega() == null){
			this.setDomicilioEntrega(this.getCliente().getDomicilio());			
		}
		if (this.getDomicilioEntrega() != null){
			if (Is.emptyString(this.getDireccion())){
				this.setDireccion(this.getDomicilioEntrega().getDireccion());
			}
			else if (this.getCiudad() == null){
				this.setCiudad(this.getDomicilioEntrega().getCiudad());
			}
		}	
		if (Is.emptyString(this.getRazonSocial())){
			this.setRazonSocial(this.getCliente().getNombre());			
		}
		if (Is.emptyString(this.getCuit())){
			this.setCuit(this.getCliente().getNumeroDocumento());			
		}
		if (this.getTipoDocumento() == null){
			this.setTipoDocumento(this.getCliente().getTipoDocumento());
		}
	}
	
	public boolean debeAutorizaAfip(){
		return false;
	}
	
	public Integer AfipTipoComprobante() {
		throw new ValidationException("No es un comprobante válido para la Afip");
	}
	
	@Override
	public void accionesValidas(List<String> showActions, List<String> hideActions) {
		super.accionesValidas(showActions, hideActions);
		
		// a futuro la acción de confirmar será SolicitarCAE, así es la misma
		if (this.debeAutorizaAfip()){
			hideActions.add(Transaccion.ACCIONCONFIRMAR);
			showActions.remove(Transaccion.ACCIONCONFIRMAR);
			
			if (getEstado().equals(Estado.Confirmada)){
				hideActions.add(VentaElectronica.ACCIONSOLICITARCAE);
			}
			else if (getEstado().equals(Estado.Anulada)){
				hideActions.add(VentaElectronica.ACCIONSOLICITARCAE);
			}
			else if (getEstado().equals(Estado.Cancelada)){
				hideActions.add(VentaElectronica.ACCIONSOLICITARCAE);
			}
			else{
				String idSubestado = null;
				if (this.getSubestado() != null) idSubestado = this.getSubestado().getId();
				
				if (TransicionEstado.tieneTransiciones(UtilERP.tipoEntidad(this).getSimpleName(), idSubestado)){
					hideActions.add(VentaElectronica.ACCIONSOLICITARCAE);
				}
				else{
					showActions.add(VentaElectronica.ACCIONSOLICITARCAE);
				}
			}
		}
		
	}
	
	protected void validacionesPreAnularTransaccion(Messages errores){
		if (this.debeAutorizaAfip()){
			boolean permiteAnular = false;
			
			/*if (this.generadaPorDiferenciaCambio()){
				if (!this.getPuntoVenta().getTipo().equals(TipoPuntoVenta.Electronico)){
					// son diferencias de cambio que no están autorizada por afip, así que se dejan anular sin hacer un crédito/débito
					permiteAnular = true;
				}
			}*/
			
			// El nuevo criterio, solo los comprobantes electrónicos no se pueden anular
			if (!this.getPuntoVenta().getTipo().solicitarCae()){
				permiteAnular = true;
			}
			
			if (!permiteAnular){
				errores.add("La operación tiene CAE: debe generar un Crédito/Débito y solicitar CAE");
			}
		}
	}


	@Override
	public Domicilio domicilioCalculoPercepcion() {
		return this.getDomicilioEntrega();
	}


	@Override
	public void imputacionesGeneradas(Collection<Imputacion> imputacionesGeneradas) {	
	}
	
	@Override
	public void CtaCteReferenciarCuentaCorriente(CuentaCorriente ctacte){
		this.setCtacte((CuentaCorrienteVenta)ctacte);
	}

	public VentaElectronica getRevierte() {
		return revierte;
	}

	public void setRevierte(VentaElectronica revierte) {
		this.revierte = revierte;
	}
	
	@Override
	public boolean revierteTransaccion() {
		if (this.getRevierte() != null){
			return true;			
		}
		else{
			return false;
		}
	}
		
	@Override
	protected boolean debeBuscarCotizacion(){
		if (this.getRevierte() != null){
			return false;
		}
		else{
			return super.debeBuscarCotizacion();
		}
	}
		
	public boolean tieneAccionesPosCommitAutorizaciones(){
		return this.revierteTransaccion();
	}
	
	public void ejecutarAccionesPosCommitAutorizaciones(){
		
		if (this.getRevierte() != null){
			Messages errores = new Messages();
			
			if (this.generaCtaCte()){
				// se imputan el crédito con la factura/debito revertido
				try{
					List<CuentaCorriente> comprobantesCuentaCorriente = new LinkedList<CuentaCorriente>();
					comprobantesCuentaCorriente.add(this.getRevierte().comprobanteCuentaCorriente());
					comprobantesCuentaCorriente.add(this.comprobanteCuentaCorriente());
					List<Imputacion> imputaciones = new LinkedList<Imputacion>();		
					Imputacion.imputarComprobantes(comprobantesCuentaCorriente, imputaciones);
								
					for(Imputacion imputacion: imputaciones){
						imputacion.asignarGeneradaPor(this);
					}
				}
				catch(Exception e){
					errores.add("No se pudo imputar el crédito con la factura: " + e.toString() );
				}
			}
			
			if (this.getRevierte() != null){
				// la transacción fue generada por un remito, se libera el pendiente que cumplió para que se pueda volver a generar después
				try{
					this.getRevierte().liberarPendientesCumplidos();
				}
				catch(Exception e){
					errores.add("No se pudo habilitar el pendiente que generó el comprobante " + this.getRevierte().toString() + ": " + e.toString());
				}
			}
			
			if (!errores.isEmpty()){
				throw new ValidationException(errores);
			}
		}
	}
	
	@Override
	public void impactarTransaccion(){
		if (this.debeAutorizaAfip()){
			throw new ValidationException("Debe solicitar el CAE en forma manual");
		}
		else{
			super.impactarTransaccion();
		}
	}

	@Override
	public String emailPara() {
		String emailPara = this.getEmail();
		if (Is.emptyString(emailPara)){
			emailPara = this.getCliente().getMail1();			
		}
		return emailPara;
	}


	@Override
	public String emailCC() {
		String email = null;
		if (this.getCliente()!= null){
			email = this.getCliente().getMail2();
		}
		return email;
	}
	
	public Remito getRemito() {
		return remito;
	}

	public void setRemito(Remito remito) {
		this.remito = remito;
	}

	public LiquidacionConsignacion getLiquidacion() {
		return liquidacion;
	}

	public void setLiquidacion(LiquidacionConsignacion liquidacion) {
		this.liquidacion = liquidacion;
		if (liquidacion != null){
			this.remito = liquidacion.getRemito();
		}
	}


	public String getIdObjetoAsociado() {
		return idObjetoAsociado;
	}


	public void setIdObjetoAsociado(String idObjetoAsociado) {
		this.idObjetoAsociado = idObjetoAsociado;
	}


	public String getIdCreadaPor() {
		return idCreadaPor;
	}


	public void setIdCreadaPor(String idCreadaPor) {
		this.idCreadaPor = idCreadaPor;
	}


	public String getTipoEntidadCreadaPor() {
		return tipoEntidadCreadaPor;
	}


	public void setTipoEntidadCreadaPor(String tipoEntidadCreadaPor) {
		this.tipoEntidadCreadaPor = tipoEntidadCreadaPor;
	}
	
	// Sirve para asociar al WorkFlow
	public void asignarCreadoPor(ObjetoNegocio objeto){
		if (objeto != null){
			this.setIdCreadaPor(objeto.getId());
			this.setTipoEntidadCreadaPor(objeto.getClass().getSimpleName());
		}
		else{
			this.setIdCreadaPor(null);
			this.setTipoEntidadCreadaPor(null);
		}
	}
	
	public void asignarPrecioUnitario(ItemVentaElectronica item){
		EstadisticaPedidoVenta itempedido = null;
		
		if (item.getItemLiquidacion() != null){
			if (!this.getEmpresa().getConsignacionPrecioActual()){
				if (item.getItemRemito().getItemOrdenPreparacion() != null){
					itempedido = item.getItemRemito().getItemOrdenPreparacion().getItemPedidoVenta();
				}
			}
		}
		else if (item.getItemRemito() != null){
			if (item.getItemRemito().getItemOrdenPreparacion() != null){
				itempedido = item.getItemRemito().getItemOrdenPreparacion().getItemPedidoVenta();
			}
		}
		
		if (itempedido != null){					
			// si viene de remito, busca el precio en el pedido.
			item.setPrecioUnitario(Transaccion.convertirMoneda(itempedido.getVenta(), this, itempedido.getPrecioUnitario()));
			item.setPorcentajeDescuento(itempedido.getPorcentajeDescuento());
		}
		else{ 
			BigDecimal precio = this.buscarPrecioActual(item);
			item.setPrecioUnitario(precio);		
		}		
	}
	
	
	protected BigDecimal buscarPrecioHistorico(ItemVentaElectronica item){
		String sql = "select i.precioUnitario, v.moneda_id, v.fechaconfirmacion from ItemVentaElectronica i " + 
				"join VentaElectronica v on v.id = i.venta_id and v.cliente_id = :cliente " +
				"where i.producto_id = :producto and precioUnitario != 0 " +
				"order by v.fechaConfirmacion desc ";

		Query query = XPersistence.getManager().createNativeQuery(sql);
		query.setParameter("producto", item.getProducto().getId());
		query.setParameter("cliente", this.getCliente().getId());
		query.setMaxResults(1);
		query.setFlushMode(FlushModeType.COMMIT);
		
		BigDecimal precio = BigDecimal.ZERO;
		List<?> result = query.getResultList();		
		if (!result.isEmpty()){
			Object[] array = (Object[]) result.get(0);
			precio = (BigDecimal)array[0];
			String idMoneda = (String)array[1];
			Moneda moneda = XPersistence.getManager().find(Moneda.class, idMoneda);
			BigDecimal cotizacion = this.buscarCotizacionTrConRespectoA(moneda);
			return precio.multiply(cotizacion).setScale(2, RoundingMode.HALF_EVEN);			
		}	
		return precio;
	}
	
	protected BigDecimal buscarPrecioActual(ItemVentaElectronica item){
		BigDecimal precio = this.getCliente().calcularPrecio(this.getListaPrecio(), item.getProducto(), item.getUnidadMedida(), item.getCantidad(), this);				
		return precio;
	}
	
	public VentaElectronica buscarPrimerObjetoAsociado(String entidad, String objetoAsociadoId){
		if (!Is.emptyString(objetoAsociadoId)){
			Query query = XPersistence.getManager().createQuery("from " + entidad + " where idObjetoAsociado = :id");
			query.setParameter("id", objetoAsociadoId);
			query.setMaxResults(1);
			List<?> results = query.getResultList();
			if (!results.isEmpty()){
				return (VentaElectronica)results.get(0);
			}
			else{
				return null;
			}
		}
		else{
			return null;
		}
	}
	
	protected boolean debeGenerarDescuentoFinanciero(){
		return (this.getPorcentajeFinanciero().compareTo(BigDecimal.ZERO) > 0) && (Is.emptyString(this.getIdObjetoAsociado()));
	}


	public Integer getCoeficiente() {
		return this.CtaCteCoeficiente();
	}


	public void setCoeficiente(Integer coeficiente) {
		this.coeficiente = this.CtaCteCoeficiente();
	}
	
	@Override
	public Integer CtaCteCoeficiente() {
		return 1;
	}
	
	@Override
	public boolean contabilizaEnCero(){
		// Se pueden hacer facturas en $0. Cuando una operación es exenta.
		return true;
	}
	
	@Override
	protected void validacionesPreGrabarTransaccion(Messages errores){
		super.validacionesPreGrabarTransaccion(errores);
		
		if ((this.getFechaVencimiento() != null) && (this.getFecha() != null)){
			if (DateUtils.truncatedCompareTo(this.getFechaVencimiento(), this.getFecha(), Calendar.DATE) < 0){			
				errores.add("Fecha vencimiento no puede ser anterior a la fecha del comprobante");
			}
		}
		
		if ((this.getCliente() != null) && (this.getPuntoVenta() != null)){
			if (this.getCliente().getRegimenFacturacion().getRegimenFacturacion().equals(RegimenFacturacionFiscal.Exportacion)
				&& !this.getPuntoVenta().getTipo().exportacion()){
 
				if (!this.generadaPorDiferenciaCambio()){
					errores.add("El cliente factura al exterior. El punto de venta debe ser de exportación");
				}
				else if (!this.getPuntoVenta().getTipo().equals(TipoPuntoVenta.Manual)){
					// si es generada por diferencia de cambio, puede ser un comprobante manual "no fiscal"
					errores.add("El cliente factura al exterior. El punto de venta debe ser de exportación");
				}
			}
		}
	}
		
	@Override
	public boolean generaCtaCte(){
		if (this.revierteTransaccion()){
			return this.getRevierte().generaCtaCte();
		}
		else{
			return true;
		}
	}
	
	protected Class<?> tipoTransaccionRevierte(){
		return null;
	}
			
	public VentaElectronica generarComprobanteReversion(Sucursal otraSucursal){
		if (this.getEstado().equals(Estado.Confirmada)){
			if (this.generaCtaCte()){
				CuentaCorriente ctacte = CuentaCorriente.buscarCuentaCorriente(this);
				if (ctacte != null){
					if (ctacte.saldadoMonedaOriginal()){
						throw new ValidationException("El comprobante no tiene saldo en cuenta corriente");
					}
				}
			}
			else{
				if (this.estaRevertida()){
					throw new ValidationException("El comprobante ya fue anulado");
				}
			}
			
			VentaElectronica reversion = creditoPendiente();			
			if (reversion == null){				
				try{
					reversion = (VentaElectronica)this.tipoTransaccionRevierte().newInstance();
				}
				catch(Exception e){
					throw new ValidationException("No se puede instanciar el comprobante para revertir");
				}
				reversion.copiarPropiedades(this);
				if(otraSucursal != null){
					if (!Is.equal(reversion.getSucursal(), otraSucursal)){
						reversion.setSucursal(otraSucursal);
						try{
							PuntoVentaDefaultCalculator ptoVenta = new PuntoVentaDefaultCalculator();
							ptoVenta.setSucursal(reversion.getSucursal());
							reversion.setPuntoVenta((PuntoVenta)ptoVenta.calculate());
						}
						catch(Exception e){
						}
					}
				}
				reversion.setItems(new ArrayList<ItemVentaElectronica>());
				reversion.setRevierte(this);
				XPersistence.getManager().persist(reversion);
				
				for(ItemVentaElectronica item: this.getItems()){
					ItemVentaElectronica itemReversion = new ItemVentaElectronica();
					itemReversion.copiarPropiedades(item);
					itemReversion.setVenta(reversion);
					reversion.getItems().add(itemReversion);			
					itemReversion.recalcular();
					XPersistence.getManager().persist(itemReversion);
				}
				reversion.recalcularTotales();					
			}
			return reversion;
		}
		else{
			throw new ValidationException("La factura no esta confirmada");
		}
	}
	
	private CreditoVenta creditoPendiente(){
		Query query = XPersistence.getManager().createQuery("from CreditoVenta where revierte.id = :id");
		query.setParameter("id", this.getId());
		List<?> results = query.getResultList();
		CreditoVenta creditoPend = null;
		if (!results.isEmpty()){
			for(Object obj: results){
				CreditoVenta credito = (CreditoVenta)obj;
				if (!credito.finalizada()){
					creditoPend = credito;
					break;
				}
			}
		}
		return creditoPend;		
	}
	
	public boolean tieneOperacionQueRevierte(){
		boolean tiene = false;
		if (!this.esNuevo() && (this.cerrado())){
			Query query = XPersistence.getManager().createQuery("from VentaElectronica where revierte.id = :id");
			query.setParameter("id", this.getId());
			List<?> results = query.getResultList();
			for(Object res: results){
				VentaElectronica venta = (VentaElectronica)res;
				if (!venta.finalizada()){
					tiene = true;
					break;
				}
				else if (venta.getEstado().equals(Estado.Confirmada)){
					tiene = true;
					break;
				}
			}
		}
		return tiene;		
	}
		
	@Override
	protected boolean estaRevertida(){
		boolean revertido = false;
		if (!this.esNuevo() && (this.cerrado())){
			Query query = XPersistence.getManager().createQuery("from VentaElectronica where revierte.id = :id and estado = :confirmada");
			query.setParameter("id", this.getId());
			query.setParameter("confirmada", Estado.Confirmada);
			query.setMaxResults(1);
			List<?> results = query.getResultList();
			revertido = !results.isEmpty();
		}
		return revertido;		
	}


	public BigDecimal getImpuestosInternos() {
		return impuestosInternos == null ? BigDecimal.ZERO : this.impuestosInternos;
	}


	public void setImpuestosInternos(BigDecimal impuestosInternos) {
		this.impuestosInternos = impuestosInternos;
	}


	public BigDecimal getImpuestosInternos1() {
		return impuestosInternos1 == null ? BigDecimal.ZERO : this.impuestosInternos1;
	}


	public void setImpuestosInternos1(BigDecimal impuestosInternos1) {
		this.impuestosInternos1 = impuestosInternos1;
	}


	public BigDecimal getImpuestosInternos2() {
		return impuestosInternos2 == null ? BigDecimal.ZERO : this.impuestosInternos2;
	}


	public void setImpuestosInternos2(BigDecimal impuestosInternos2) {
		this.impuestosInternos2 = impuestosInternos2;
	}
	
	public boolean puedeGenerarTransaccionIntercompany(){
		if (!this.getEstado().equals(Estado.Confirmada)){
			throw new ValidationException("Estado inválido");
		}
		else{
			return true;
		}
	}
	
	public void generarTransaccionIntercompany(BigDecimal porcentaje){		
	}

	public TipoDocumento getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(TipoDocumento tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		int maxLong = 100;
		if (!Is.emptyString(razonSocial)){
			if (razonSocial.length() > maxLong){
				this.razonSocial = razonSocial.substring(0, maxLong);
			}
			else{
				this.razonSocial = razonSocial;
			}
		}
		else{
			this.razonSocial = razonSocial;
		}		
	}

	public PosicionAnteImpuesto getPosicionIva() {
		return posicionIva;
	}

	public void setPosicionIva(PosicionAnteImpuesto posicionIva) {
		this.posicionIva = posicionIva;
	}
	
	@Override
	public void propiedadesSoloLecturaAlEditar(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		super.propiedadesSoloLecturaAlEditar(propiedadesSoloLectura, propiedadesEditables, configuracion);
		
		if ((this.getCliente() != null) && (this.getCliente().getSinIdentificacion())){
			propiedadesEditables.add("cuit");
			propiedadesEditables.add("razonSocial");
			propiedadesEditables.add("posicionIva");
			propiedadesEditables.add("tipoDocumento");
		}
		else{
			propiedadesSoloLectura.add("cuit");
			propiedadesSoloLectura.add("razonSocial");
			propiedadesSoloLectura.add("posicionIva");
			propiedadesSoloLectura.add("tipoDocumento");
		}
	}

	public ListaPrecio getListaPrecio() {
		return listaPrecio;
	}

	public void setListaPrecio(ListaPrecio listaPrecio) {
		this.listaPrecio = listaPrecio;
	}


	public boolean verificarPrecioUnitario(ItemVentaElectronica item) {
		if (item.getPrecioUnitario().compareTo(BigDecimal.ZERO) == 0){
			return true;
		}
		else{
			return false;
		}
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	@Override
	protected void asignarSucursal(){
		if (this.getPuntoVenta() != null){
			this.setSucursal(this.getPuntoVenta().getSucursal());
		}
	}
	
	private Domicilio domicilioPuntoVenta(){
		Domicilio dom = null;
		if (this.getPuntoVenta() != null){
			dom = this.getPuntoVenta().getDomicilio();
		}
		if ((dom == null) && (this.getEmpresa() != null)){
			dom = this.getEmpresa().getDomicilio();
		}
		return dom;
	}
	
	public boolean facturaSoloConceptos(){	
		boolean soloConceptos = false;
		if ((this.getItems()!= null) && (!this.getItems().isEmpty())){
			soloConceptos = true;
			for(ItemVentaElectronica item: this.getItems()){
				if (item.getProducto().getTipo().equals(TipoProducto.Producto)){
					soloConceptos = false;
					break;
				}
			}
		}		
		return soloConceptos;	
	}


	public boolean generaRemito() {
		return this.cumpleCondicionGeneracionPendiente(Remito.class);
	}
	
	public ReciboCobranza buscarReciboCobranzaContado(){
		if (!Is.emptyString(this.getIdObjetoAsociado())){
			return XPersistence.getManager().find(ReciboCobranza.class, this.getIdObjetoAsociado());
		}
		else{
			return null;
		}
	}

	public CondicionVenta getCondicionVenta() {
		return condicionVenta;
	}

	public void setCondicionVenta(CondicionVenta condicionVenta) {
		this.condicionVenta = condicionVenta;
	}


	public ZonaReparto getZonaReparto() {
		return zonaReparto;
	}


	public void setZonaReparto(ZonaReparto zonaReparto) {
		this.zonaReparto = zonaReparto;
	}
	
	@Override
	public EmpresaExterna ContabilidadOriginante() {
		EmpresaExterna empresaExterna = null;
		if (this.getCliente() != null){
			empresaExterna = XPersistence.getManager().find(EmpresaExterna.class, this.getCliente().getId());
		}
		return empresaExterna;		
	}
	
	public EmpresaExterna empresaExternaInventario() {
		if (this.getCliente() != null){
			return (EmpresaExterna)XPersistence.getManager().find(EmpresaExterna.class, this.getCliente().getId());
		}
		else{
			return null;
		}
	}


	@Override
	public CondicionVenta condicionVentaCalculoPrecio() {
		return this.getCondicionVenta();
	}
	
	public ObjetoNegocio creadoPor(){
		if (!Is.emptyString(this.getIdCreadaPor())){
			Query query = XPersistence.getManager().createQuery("from " + this.getTipoEntidadCreadaPor() + " where id = :id" );
			query.setParameter("id", this.getIdCreadaPor());
			query.setMaxResults(1);
			return (ObjetoNegocio)query.getSingleResult();
		}
		else{
			return null;
		}
		
	}
	
	@Transient
	private ConfiguracionAfip afip = null;
	
	private ConfiguracionAfip configuradorAfip(){
		if (this.afip == null){
			this.afip = ConfiguracionAfip.getConfigurador();
		}
		return this.afip;
	}
	
	public BigDecimal alicuotaIva(Producto producto){		
		if (this.configuradorAfip().participaRegimenIva0(producto, this.getPosicionIva(), this.getCliente())){
			return BigDecimal.ZERO;
		}
		else{
			return producto.getTasaIva().getPorcentaje();
		}
	}
		
	@Override
	protected void validarFechaComprobante(Messages errores){
		if (Is.equalAsString(this.getTipoEntidadCreadaPor(), Contrato.class.getSimpleName())){
			if (Is.equal(Estado.Procesando, this.getEstado()) || Is.equal(Estado.Confirmada, this.getEstado())){
				super.validarFechaComprobante(errores);
			}
		}
		else{
			super.validarFechaComprobante(errores);
		}
	}
	
	@Override
	public BigDecimal ContabilidadTotal() {
		return this.getTotal1();
	}


	public Boolean getIntercompany() {
		return intercompany;
	}

	public void setIntercompany(Boolean intercompany) {
		if (intercompany != null){
			this.intercompany = intercompany;
		}
	}
	
	@Override
	public String nombreReporteImpresion() {
		String reporte = super.nombreReporteImpresion();
		if (this.getPuntoVenta().getTipo().equals(TipoPuntoVenta.ExportacionElectronico)){
			reporte = "Exportacion_" + reporte;
		}
		return reporte;
	}
	
	public String monedaAfip(){
		return "PES";
	}
	
	public BigDecimal cotizacionAfip(){
		return new BigDecimal(1);
	}


	public boolean esFactura() {
		return false;
	}


	@SuppressWarnings("unchecked")
	@Override
	public void itemsParaControlarPorCodigoBarra(List<IItemControlCodigoBarras> items, Producto producto,
			BigDecimal cantidadControlar) {
		if (!this.esNuevo()){
			Query query = XPersistence.getManager().createQuery("from ItemVentaElectronica where venta = :tr and producto = :producto");
			query.setParameter("producto", producto);
			query.setParameter("tr", this);
			query.setFlushMode(FlushModeType.COMMIT);
			items.addAll(query.getResultList());
		}		
	}


	@Override
	public boolean permiteCantidadesNegativas() {
		return false;
	}


	@Override
	public IItemControlCodigoBarras crearItemDesdeCodigoBarras(Producto producto, BigDecimal cantidad,
			String codigoLote, String codigoSerie, Date vencimiento) {
		ItemVentaElectronica item = new ItemVentaElectronica();
		item.setVenta(this);
		item.setProducto(producto);
		if (producto.getLote()){
			if (!Is.emptyString(codigoSerie)){
				Lote lote = Lote.buscarPorCodigo(codigoLote, producto.getCodigo());
				if(lote != null){
					item.setLote(lote);
				}
			}			
		}
		item.setUnidadMedida(producto.getUnidadMedida());
		item.setCantidad(cantidad);
		item.recalcular();
		XPersistence.getManager().persist(item);
		
		if (this.getItems() == null){
			this.setItems(new LinkedList<ItemVentaElectronica>());			
		}
		this.getItems().add(item);
		return item;
	}


	@Override
	public BigDecimal mostrarTotalLectorCodigoBarras() {
		return this.getTotal();
	}
}
