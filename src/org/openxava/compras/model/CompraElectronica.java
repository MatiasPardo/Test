package org.openxava.compras.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;
import org.openxava.compras.actions.*;
import org.openxava.contabilidad.model.*;
import org.openxava.cuentacorriente.model.*;
import org.openxava.fisco.model.TipoComprobante;
import org.openxava.impuestos.model.*;
import org.openxava.inventario.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.calculators.CondicionVentaPrincipalCalculator;
import org.openxava.ventas.model.CondicionVenta;

import com.allin.interfacesafip.util.*;

@Entity

@Views({
	@View(members=
		"Auditoria[descripcion, fechaCreacion, usuario];" +
		"Principal[#" + 
				"fecha, fechaReal, estado;" +
				"tipo, numero;" + 
				"cae, fechaVencimientoCAE;" +
				"proveedor;" + 
				"moneda;" + 
				"observaciones];" + 	
		"items;" +
		"impuestos;" +
		"Totales[subtotal, iva, otrosImpuestos, total];" +
		"Descuentos[subtotalSinDescuento, descuento];" +
		"despacho;"
	),
	@View(name="Cerrado", members=
		"Auditoria[descripcion, fechaCreacion, usuario];" +
		"Principal[#" + 
				"fecha, fechaReal, estado;" +
				"tipo, numero;" + 
				"cae, fechaVencimientoCAE;" +
				"proveedor;" + 
				"moneda;" + 
				"observaciones];" + 	
		"items;" +
		"impuestos;" + 
		"Totales[subtotal, iva, otrosImpuestos, total];" +
		"Descuentos[subtotalSinDescuento, descuento];" + 
		"despacho;"
	),
	@View(name="Simple", members="numero, estado;")
})

@Tab(
		filter=EmpresaFilter.class,
		properties="fecha, tipoOperacion, numero, cae, estado, proveedor.nombre, total, subtotal, iva, fechaCreacion, usuario",
		baseCondition=EmpresaFilter.BASECONDITION + " and estado = 1",
		defaultOrder="${fechaCreacion} desc")

public class CompraElectronica extends Transaccion implements ITransaccionCtaCte, ITransaccionContable{

	@DefaultValueCalculator(CurrentDateCalculator.class)
	@Required
	private Date fechaReal = new Date(); 
	
	@Required @DefaultValueCalculator(CurrentDateCalculator.class)
	private Date fechaVencimiento = new Date();
	
	@Required @DefaultValueCalculator(CurrentDateCalculator.class)
	private Date fechaServicio = new Date();
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate 
    @NoModify
    @ReferenceView("Transaccion")
	@OnChange(OnChangeProveedorCompraAction.class)
	private Proveedor proveedor;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre", condition="${compras} = 't'")
	@NoCreate @NoModify
	@DefaultValueCalculator(value=CondicionVentaPrincipalCalculator.class, 
			properties={@PropertyValue(name="ventas", value="false")})
	private CondicionVenta condicionCompra;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReadOnly
	@ReferenceView("Simple")
	private Ciudad ciudad;
	
	@Column(length=15)
	@ReadOnly
	private String tipoOperacion;
	
	@DefaultValueCalculator(TipoComprobanteComprasCalculator.class)
	@DescriptionsList(descriptionProperties="tipo")
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@JoinColumn(name="tipo", nullable=false)
	@NoCreate @NoModify
	private TipoComprobante tipo;
	
	@Column(length=20)	
	private String cae;
	
	@Stereotype("DATE") 
	private Date fechaVencimientoCAE;
	
	@Stereotype("MONEY")
	private BigDecimal total;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal total1;
	
	@ReadOnly
	@Stereotype("MONEY")
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
	
	@Stereotype("MONEY")
	private BigDecimal iva;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal iva1;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal iva2;
	
	@Stereotype("MONEY")
	@ReadOnly
	private BigDecimal otrosImpuestos;
	
	@Stereotype("MONEY")
	private BigDecimal subtotal;

	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal subtotal1;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal subtotal2;
	
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
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal descuento = BigDecimal.ZERO;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal subtotalSinDescuento = BigDecimal.ZERO;
	
	@OneToMany(mappedBy="compra", cascade=CascadeType.ALL) 
	@ListProperties("producto.codigo, producto.nombre, cantidad, precioUnitario, suma, tasaiva")
	@SaveAction(value="ItemTransaccion.save")
	@NewAction(value="ItemTransaccion.new")
	@HideDetailAction(value="ItemTransaccion.hideDetail")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	@EditAction("ItemTransaccion.edit")
	@CollectionViews({
		@CollectionView(forViews="FacturaCompra", value="FacturaCompra")
	})
	private Collection<ItemCompraElectronica> items; 
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private DespachoImportacion despacho;
	
	@Hidden
	@ReadOnly
	private Integer coeficiente = 0;
	
	public Date getFechaReal() {
		return fechaReal;
	}

	public void setFechaReal(Date fechaReal) {
		this.fechaReal = fechaReal;
	}

	public String getTipoOperacion() {
		return tipoOperacion;
	}

	public void setTipoOperacion(String tipoOperacion) {
		this.tipoOperacion = tipoOperacion;
	}

	@ElementCollection
	@ListProperties("impuesto.codigo, impuesto.nombre, importe, alicuota")	
	private Collection<ImpuestoCompra> impuestos;
	
	public Collection<ImpuestoCompra> getImpuestos() {
		return impuestos;
	}

	public void setImpuestos(Collection<ImpuestoCompra> impuestos) {
		this.impuestos = impuestos;
	}
	
	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}
			
	public CondicionVenta getCondicionCompra() {
		return condicionCompra;
	}

	public void setCondicionCompra(CondicionVenta condicionCompra) {
		this.condicionCompra = condicionCompra;
	}

	public Collection<ItemCompraElectronica> getItems() {
		return items;
	}

	public void setItems(Collection<ItemCompraElectronica> items) {
		this.items = items;
	}

	public BigDecimal getOtrosImpuestos() {
		return otrosImpuestos == null ? BigDecimal.ZERO : otrosImpuestos;
	}

	public void setOtrosImpuestos(BigDecimal otrosImpuestos) {
		this.otrosImpuestos = otrosImpuestos;
	}

	public BigDecimal getTotal() {
		return total == null ? BigDecimal.ZERO : total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public BigDecimal getTotal1() {
		return total1 == null ? BigDecimal.ZERO : total1;
	}

	public void setTotal1(BigDecimal total1) {
		this.total1 = total1;
	}

	public BigDecimal getTotal2() {
		return total2 == null ? BigDecimal.ZERO : total2;
	}

	public void setTotal2(BigDecimal total2) {
		this.total2 = total2;
	}

	public BigDecimal getIva() {
		return iva == null ? BigDecimal.ZERO : iva;
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

	public BigDecimal getSubtotal() {
		return subtotal == null ? BigDecimal.ZERO : subtotal;
	}

	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}

	public BigDecimal getSubtotal1() {
		return subtotal1 == null ? BigDecimal.ZERO : subtotal1;
	}

	public void setSubtotal1(BigDecimal subtotal1) {
		this.subtotal1 = subtotal1;
	}

	public BigDecimal getSubtotal2() {
		return subtotal2 == null ? BigDecimal.ZERO : subtotal2;
	}

	public void setSubtotal2(BigDecimal subtotal2) {
		this.subtotal2 = subtotal2;
	}
	
	public TipoComprobante getTipo() {
		return tipo;
	}

	public void setTipo(TipoComprobante tipo) {
		this.tipo = tipo;
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
	
	@Override
	public Moneda buscarMonedaDefault(){
		Moneda monedaDefault = null;
		if (this.getProveedor() != null){
			monedaDefault = this.getProveedor().getMoneda();
		}
		
		if (monedaDefault == null){
			monedaDefault = super.buscarMonedaDefault();
		}
		return monedaDefault;
	}
	
	@Override
	protected void atributosConversionesMoneda(List<String> atributos){
		super.atributosConversionesMoneda(atributos);
		atributos.add("Total");
		atributos.add("Subtotal");
		atributos.add("Iva");
	}
	
	@Override
	public String descripcionTipoTransaccion() {
		throw new ValidationException("Falta Implementar el método descripcionTipoTransaccion");
	}

	public void copiarPropiedades(Object objeto){
		super.copiarPropiedades(objeto);
		this.setItems(null);
		this.setCae("");
		this.setSubtotal(BigDecimal.ZERO);
		this.setSubtotal1(BigDecimal.ZERO);
		this.setSubtotal2(BigDecimal.ZERO);
		this.setIva(BigDecimal.ZERO);
		this.setOtrosImpuestos(BigDecimal.ZERO);
		this.setTotal(BigDecimal.ZERO);
		this.setTotal1(BigDecimal.ZERO);
		this.setTotal2(BigDecimal.ZERO);
		this.setFechaVencimiento(this.getFecha());
	}
	
	protected boolean calcularImpuestos(){
		boolean calcula = true;
		if (this.getEmpresa() != null){
			calcula = this.getEmpresa().getInscriptoIva();			
		}
		
		
		if (calcula){
			calcula = this.getTipo().calculaImpuestos();
		}
		
		return calcula;
	}
	
	@Transient
	private BigDecimal calculoSubtotal = BigDecimal.ZERO;
	@Transient
	private BigDecimal calculoiva = BigDecimal.ZERO;
	@Transient
	private BigDecimal calculoImpuestos = BigDecimal.ZERO;
	@Transient
	private BigDecimal calculoTotal = BigDecimal.ZERO;
				
	@Override
	public void recalcularTotales(){
		if (this.getProveedor() != null){
			this.setCiudad(this.getProveedor().getDomicilio().getCiudad());
		}
		
		Boolean calcularImpuestos = this.calcularImpuestos(); 
				
		this.calculoSubtotal = BigDecimal.ZERO;
		this.calculoiva = BigDecimal.ZERO;
		this.calculoImpuestos = BigDecimal.ZERO;
		this.calculoTotal = BigDecimal.ZERO;
		BigDecimal descuentos = BigDecimal.ZERO; 
		
		boolean estaConfirmando = Is.equal(this.getEstado(), Estado.Confirmada);
		
		if (this.getItems() != null){
			for(ItemCompraElectronica item: this.getItems()){
				item.recalcular();
				descuentos = descuentos.add(item.getDescuento());
				this.calculoSubtotal = this.calculoSubtotal.add(item.getSuma());
				if (calcularImpuestos){
					this.calculoiva = this.calculoiva.add(item.getIva());
				}
				
				if (estaConfirmando){
					item.setCentroCostos(item.igcCentroCostos());
				}				
			}
		}
		
		if (calcularImpuestos){ 
			if (this.getImpuestos() != null){
				List<String> atributos = new LinkedList<String>();
				atributos.add("Importe");
				for(ImpuestoCompra impuesto: this.getImpuestos()){
					this.calculoImpuestos = this.calculoImpuestos.add(impuesto.getImporte());
					this.sincronizarMonedas(impuesto, atributos);
				}
			}
		}
		
		this.calculoSubtotal.setScale(4, RoundingMode.HALF_EVEN);
		this.calculoiva.setScale(4, RoundingMode.HALF_EVEN);
		this.calculoImpuestos.setScale(4, RoundingMode.HALF_EVEN);
		this.calculoTotal = this.calculoSubtotal.add(this.calculoiva).add(this.calculoImpuestos);
		
		this.setSubtotal(sincronizarImporteCalculado(this.getSubtotal(), this.calculoSubtotal));
		this.setIva(sincronizarImporteCalculado(this.getIva(), this.calculoiva));
		// este va directo
		this.setOtrosImpuestos(this.calculoImpuestos);
		// El total es la suma de los anteriores
		this.setTotal(sincronizarImporteCalculado(this.getSubtotal().add(this.getIva()).add(this.getOtrosImpuestos()), this.calculoTotal));
		
		this.setDescuento(descuentos);
		this.setSubtotalSinDescuento(this.getSubtotal().subtract(this.getDescuento()));
	}
	
	public BigDecimal sincronizarImporteCalculado(BigDecimal importe, BigDecimal importeCalculado){
		// permite al usuario modificar en un centavo los totales, por diferencia de redondeo de la factura original con la de nuestro sistema
		if (importe.subtract(importeCalculado).abs().compareTo(new BigDecimal("0.01")) > 0){
			return importeCalculado;
		}
		else{
			return importe;
		}
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
	public OperadorComercial CtaCteOperadorComercial() {
		return this.getProveedor();
	}

	@Override
	public IResponsableCuentaCorriente CtaCteResponsable() {
		return null;
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
	public CuentaCorriente CtaCteNuevaCuentaCorriente() {
		return new CuentaCorrienteCompra();
	}

	@Override
	public boolean generadaPorDiferenciaCambio() {
		return false;
	}

	@Override
	public void detalleDiferenciaCambio(Collection<DiferenciaCambioVenta> detalleDifCambio) {
	}

	@Override
	public void generadorPasesContable(Collection<IGeneradorItemContable> items) {
		int comparacion = this.CtaCteImporte().compareTo(BigDecimal.ZERO);
		if (comparacion != 0){
			for(IGeneradorItemContable itemGC: this.getItems()){
				items.add(itemGC);			
			}
			
			// IVA
			Impuesto impuesto = Impuesto.buscarPorDefinicionImpuesto(DefinicionImpuesto.IvaCompra);
			GeneradorItemContablePorTr paseIva = new GeneradorItemContablePorTr(this, TipoCuentaContable.Impuesto.CuentaContablePorTipo(impuesto));
			if (comparacion > 0){
				paseIva.setDebe(this.getIva());
			}
			else{
				paseIva.setHaber(this.getIva());
			}
			items.add(paseIva);
			
			// otros impuestos
			for(ImpuestoCompra otroImpuesto: this.getImpuestos()){
				GeneradorItemContablePorTr paseImpuesto = new GeneradorItemContablePorTr(this, TipoCuentaContable.Impuesto.CuentaContablePorTipo(otroImpuesto.getImpuesto()));
				if (comparacion > 0){
					paseImpuesto.setDebe(otroImpuesto.getImporte());
				}
				else{
					paseImpuesto.setHaber(otroImpuesto.getImporte());
				}
				items.add(paseImpuesto);
			}
			
			// Proveedor			
			GeneradorItemContablePorTr paseProveedor = new GeneradorItemContablePorTr(this, TipoCuentaContable.Compras.CuentaContablePorTipo(this.getProveedor()));			
			if (comparacion > 0){
				paseProveedor.setHaber(this.getTotal());
			}
			else{
				paseProveedor.setDebe(this.getTotal());
			}
			items.add(paseProveedor);
		}
		
	}

	@Override
	public void imputacionesGeneradas(Collection<Imputacion> imputacionesGeneradas) {	
	}
	
	protected boolean numeroRepetidoCompra(){
		if (!Is.emptyString(this.getId()) && this.getProveedor() != null && this.getEmpresa() != null){
			Query query = XPersistence.getManager().createQuery("from " + this.getClass().getSimpleName() + " where id != :id and tipo = :tipo and " + 
					"proveedor.id = :proveedor and empresa.id = :empresa and numero = :numero and estado != :estadoAnulado and estado != :estadoCancelado");
			query.setParameter("id", this.getId());
			query.setParameter("proveedor", this.getProveedor().getId());
			query.setParameter("empresa", this.getEmpresa().getId());
			query.setParameter("numero", this.getNumero());
			query.setParameter("estadoAnulado", Estado.Anulada);
			query.setParameter("estadoCancelado", Estado.Cancelada);
			query.setParameter("tipo",this.getTipo());
			query.setMaxResults(1);
			return !query.getResultList().isEmpty();
		}
		else{
			return false;
		}
	}
	
	@Override
	public void CtaCteReferenciarCuentaCorriente(CuentaCorriente ctacte) {
		this.setCtacte((CuentaCorrienteCompra)ctacte);
	}
	
	@Override
	public Integer CtaCteCoeficiente() {
		return 1;
	}

	public DespachoImportacion getDespacho() {
		return despacho;
	}

	public void setDespacho(DespachoImportacion despacho) {
		this.despacho = despacho;
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
	
	@Override
	public boolean generaCtaCte(){
		return true;
	}
	
	public void agregarParametrosImpresion(Map<String, Object> parameters) {
		super.agregarParametrosImpresion(parameters);
		
		parameters.put("RAZONSOCIAL_PROVEEDOR", this.getProveedor().getNombre());
		parameters.put("CODIGO_PROVEEDOR", this.getProveedor().getCodigo());
		parameters.put("CUIT_PROVEEDOR", this.getProveedor().getNumeroDocumento());
		parameters.put("TIPODOCUMENTO_PROVEEDOR", this.getProveedor().getTipoDocumento().toString());
		parameters.put("POSICIONIVA_PROVEEDOR", this.getProveedor().getPosicionIva().getDescripcion());
		parameters.put("DIRECCION_PROVEEDOR", this.getProveedor().getDomicilio().getDireccion());
		parameters.put("CODIGOPOSTAL_PROVEEDOR", this.getCiudad().getCodigoPostal());
		parameters.put("CIUDAD_PROVEEDOR", this.getCiudad().getCiudad());
		parameters.put("PROVINCIA_PROVEEDOR", this.getCiudad().getProvincia().getProvincia());
		parameters.put("TOTAL", this.getTotal());
		parameters.put("TOTAL1", this.getTotal1());
		parameters.put("TOTAL2", this.getTotal2());
		parameters.put("SUBTOTAL", this.getSubtotal());
		parameters.put("SUBTOTAL1", this.getSubtotal1());
		parameters.put("SUBTOTAL2", this.getSubtotal2());
		parameters.put("SUBTOTALSINDESCUENTO", this.getSubtotalSinDescuento());
		parameters.put("DESCUENTO", this.getDescuento());
		
		String totalLetras = "";
		try{
			totalLetras = NumberToLetterConverter.convertNumberToLetter(this.getTotal());
		}
		catch(Exception e){
		}
		parameters.put("TOTALLETRAS", totalLetras);
	}

	public Ciudad getCiudad() {
		return ciudad;
	}

	public void setCiudad(Ciudad ciudad) {
		this.ciudad = ciudad;
	}

	public Date getFechaVencimiento() {
		return fechaVencimiento;
	}

	public void setFechaVencimiento(Date fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
		
		if (this.estaCambiandoAtributo()){
			if (this.generaCtaCte()){
				CuentaCorrienteCompra ctacte = (CuentaCorrienteCompra)CuentaCorriente.buscarCuentaCorriente(this);
				if (ctacte != null){
					Collection<ImputacionCompra> imputaciones = ctacte.getImputaciones();
					for(ImputacionCompra imp: imputaciones){
						if (!(imp.getEstado().equals(Estado.Anulada) &&
							imp.getEstado().equals(Estado.Cancelada))){
							throw new ValidationException("No se puede modificar la fecha de vencimiento. El comprobante ya tiene imputaciones");
						}
					}
					ctacte.setFechaVencimiento(fechaVencimiento);
				}
			}
		}
	}
	
	@Override
	protected void validacionesPreGrabarTransaccion(Messages errores){
		super.validacionesPreGrabarTransaccion(errores);
		
		if (this.getImpuestos() != null){
			Map<String, Object> imp = new HashMap<String, Object>();
			for(ImpuestoCompra impuesto: this.getImpuestos()){
				if (impuesto.getImpuesto() != null){
					if (!impuesto.getImpuesto().getPermiteRepetidos()){
						String clave = impuesto.getImpuesto().getId();
						if (imp.containsKey(clave)){
							errores.add(impuesto.getImpuesto().getNombre() + " repetido");
						}
						else{
							imp.put(clave, null);
						}
					}
				}
			}
		}
		
		if (this.getFechaReal() != null){
			UtilERP.validarRangoFecha(this.getFechaReal());
		}
		if (this.getFechaVencimiento() != null){
			UtilERP.validarRangoFecha(this.getFechaVencimiento());
		}
	}

	public Integer getCoeficiente() {
		return this.CtaCteCoeficiente();
	}

	public void setCoeficiente(Integer coeficiente) {
		this.coeficiente = this.CtaCteCoeficiente();
	}
	
	@Override
	protected void inicializar(){
		super.inicializar();
		this.setCoeficiente(this.CtaCteCoeficiente());
		
		if (this.getTipo() == null){
			TipoComprobanteComprasCalculator calculator = new TipoComprobanteComprasCalculator();
			try{
				this.setTipo((TipoComprobante)calculator.calculate());
			}
			catch(Exception e){				
			}
		}
		
		if (this.getCondicionCompra() == null){
			if ((this.getProveedor() != null) && (this.getProveedor().getCondicionCompra() != null)){
				this.setCondicionCompra(this.getProveedor().getCondicionCompra());
			}
			else{
				CondicionVentaPrincipalCalculator calculator = new CondicionVentaPrincipalCalculator();
				calculator.setVentas(false);
				try{
					this.setCondicionCompra((CondicionVenta)calculator.calculate());				
				}
				catch(Exception e){				
				}
			}			
		}
	}

	@Override
	public EmpresaExterna ContabilidadOriginante() {
		EmpresaExterna empresaExterna = null;
		if (this.getProveedor() != null){
			empresaExterna = XPersistence.getManager().find(EmpresaExterna.class, this.getProveedor().getId());
		}
		return empresaExterna;		
	}
		
	// Sirve para asociar al WorkFlow
	@Column(length=32)
	@ReadOnly
	@Hidden
	private String idCreadaPor;
		
	@ReadOnly
	@Hidden
	@Column(length=100)
	private String tipoEntidadCreadaPor;
	
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

	@Override
	public BigDecimal ContabilidadTotal() {
		return this.getTotal1();
	}
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Transaccion")
	private CuentaCorrienteCompra ctacte;

	public CuentaCorrienteCompra getCtacte() {
		return ctacte;
	}

	public void setCtacte(CuentaCorrienteCompra ctacte) {
		this.ctacte = ctacte;
	}
}
