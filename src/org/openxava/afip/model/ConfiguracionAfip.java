package org.openxava.afip.model;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Query;

import org.openxava.annotations.DefaultValueCalculator;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.ListProperties;
import org.openxava.annotations.View;
import org.openxava.base.model.Empresa;
import org.openxava.calculators.FalseCalculator;
import org.openxava.compras.model.Proveedor;
import org.openxava.fisco.model.RegimenFacturacionFiscal;
import org.openxava.fisco.model.TipoFacturacion;
import org.openxava.impuestos.model.Impuesto;
import org.openxava.impuestos.model.PosicionAnteImpuesto;
import org.openxava.jpa.XPersistence;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.Cliente;
import org.openxava.ventas.model.Producto;
import org.openxava.ventas.model.VentaElectronica;

@Entity

@View(members=
	"Principal{" +
			"id;" +
			"FacturaCreditoElectronica[activarFCE, minimoFCE; empresa];" + 
			"RegInfCompraVenta[proveedoresDespacho; impuestosDespacho; excluirPuntoVentaManuales];" +
	"}Iva0{" +
			"activarRegimenIva0;" +
			"productosIva0;" +
			"posicionesIva0;" +
			"clientesIva0;" + 			
	"}"		
)

public class ConfiguracionAfip {
	
	public static ConfiguracionAfip getConfigurador() {
		Query query = XPersistence.getManager().createQuery("from ConfiguracionAfip");
		List<?> result = query.getResultList();
		if (result.size() == 0){
			throw new ValidationException("No esta definido el configurador de afip (Modulo Configuracion Afip)");
		}
		else if (result.size() > 1){
			throw new ValidationException("Hay mas de un configurador de afip definido");
		}
		else{
			return (ConfiguracionAfip)result.get(0);
		}
	}
	
	@Id
	private int id;
	
	private BigDecimal minimoFCE;
	
	private Boolean activarFCE;
	
	@ElementCollection
	@ListProperties("empresa.codigo, cbu, alias, tipoSistemaFCE, creditoFCE")
	private Collection<ConfiguracionAfipEmpresa> empresa;
	
	@ManyToMany(fetch=FetchType.LAZY)
	@ListProperties("codigo, nombre")
	private Collection<Proveedor> proveedoresDespacho;
	
	@ManyToMany(fetch=FetchType.LAZY)
	@ListProperties("codigo, nombre")
	private Collection<Impuesto> impuestosDespacho;

	@DefaultValueCalculator(value=FalseCalculator.class)
	@Hidden
	private Boolean excluirPuntoVentaManuales = Boolean.FALSE;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public BigDecimal getMinimoFCE() {
		return minimoFCE == null? BigDecimal.ZERO : minimoFCE;
	}

	public void setMinimoFCE(BigDecimal minimoFCE) {
		this.minimoFCE = minimoFCE;
	}

	public Boolean getActivarFCE() {
		return activarFCE == null ? Boolean.FALSE : activarFCE;
	}

	public void setActivarFCE(Boolean activarFCE) {
		this.activarFCE = activarFCE;
	}

	public Collection<Proveedor> getProveedoresDespacho() {
		return proveedoresDespacho;
	}

	public void setProveedoresDespacho(Collection<Proveedor> proveedoresDespacho) {
		this.proveedoresDespacho = proveedoresDespacho;
	}

	public Collection<Impuesto> getImpuestosDespacho() {
		return impuestosDespacho;
	}

	public void setImpuestosDespacho(Collection<Impuesto> impuestosDespacho) {
		this.impuestosDespacho = impuestosDespacho;
	}
	
	public Collection<ConfiguracionAfipEmpresa> getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Collection<ConfiguracionAfipEmpresa> empresa) {
		this.empresa = empresa;
	}

	public boolean evaluarRegimenFacturaCreditoElectronico(VentaElectronica venta) {
		boolean fce = false;
		if (this.getActivarFCE()){
			if (venta.getTotal().compareTo(this.getMinimoFCE()) > 0){
				TipoFacturacion tipoFacturacion = venta.getCliente().getRegimenFacturacion();
				if (tipoFacturacion.getRegimenFacturacion().equals(RegimenFacturacionFiscal.FCE)){
					if (this.empresaHabilitadaFCE(venta.getEmpresa()) != null){
						fce = true;
					}
				}
			}
		}
		return fce;
	}
	
	public ConfiguracionAfipEmpresa empresaHabilitadaFCE(Empresa empresa){
		if (this.getEmpresa() != null){
			for(ConfiguracionAfipEmpresa configEmpresa: this.getEmpresa()){
				if (configEmpresa.getEmpresa().equals(empresa)){
					return configEmpresa;
				}
			}
		}
		return null;
	}
	
	@PrePersist
	protected void onPrePersist() {
		this.validarPreCommit();
	}
	
	@PreUpdate
	protected void onPreUpdate() {
		this.validarPreCommit();
	}
	
	private void validarPreCommit(){
		if (this.getActivarFCE()){
			boolean faltaEmpresa = false;
			if (this.getEmpresa() == null){
				faltaEmpresa = true;
			}
			else if (this.getEmpresa().size() == 0){
				faltaEmpresa = true;
			}
			
			if (faltaEmpresa){
				throw new ValidationException("Para activar Factura Credito electronica debe definir la empresa con el alias y cbu");
			}
			
			if (this.getMinimoFCE().compareTo(BigDecimal.ZERO) <= 0){
				throw new ValidationException("El mínimo de factura credito electrónica no puede ser cero");
			}
		}
	}
	
	
	/* IVA 0 */
	/*-------*/
	private Boolean activarRegimenIva0 = Boolean.FALSE;
	
	@ManyToMany(fetch=FetchType.LAZY)
	@ListProperties("codigo, nombre")
	private Collection<Producto> productosIva0;
	
	@ManyToMany(fetch=FetchType.LAZY)
	@ListProperties("codigo, descripcion")
	private Collection<PosicionAnteImpuesto> posicionesIva0;
	
	@ManyToMany(fetch=FetchType.LAZY)
	@ListProperties("codigo, nombre")
	private Collection<Cliente> clientesIva0;

	public Boolean getActivarRegimenIva0() {
		return activarRegimenIva0 == null? Boolean.FALSE : this.activarRegimenIva0;
	}

	public void setActivarRegimenIva0(Boolean activarRegimenIva0) {
		this.activarRegimenIva0 = activarRegimenIva0;
	}

	@SuppressWarnings("unchecked")
	public Collection<Producto> getProductosIva0() {
		return productosIva0 == null ? java.util.Collections.EMPTY_LIST : this.productosIva0;
	}

	public void setProductosIva0(Collection<Producto> productosIva0) {
		this.productosIva0 = productosIva0;
	}

	@SuppressWarnings("unchecked")
	public Collection<PosicionAnteImpuesto> getPosicionesIva0() {
		return posicionesIva0 == null ? java.util.Collections.EMPTY_LIST : this.posicionesIva0;
	}

	public void setPosicionesIva0(Collection<PosicionAnteImpuesto> posicionesIva0) {
		this.posicionesIva0 = posicionesIva0;
	}

	@SuppressWarnings("unchecked")
	public Collection<Cliente> getClientesIva0() {
		return clientesIva0 == null ? java.util.Collections.EMPTY_LIST : this.clientesIva0;
	}

	public void setClientesIva0(Collection<Cliente> clientesIva0) {
		this.clientesIva0 = clientesIva0;
	}

	public boolean participaRegimenIva0(Producto producto, PosicionAnteImpuesto posicionIva, Cliente cliente) {
		boolean participa = false;
		if (this.getActivarRegimenIva0()){
			if ((producto != null) && (posicionIva != null) && (cliente != null)){
				if (this.getProductosIva0().contains(producto)){
					if (this.getPosicionesIva0().contains(posicionIva)){
						participa = true;
					}
					else if (this.getClientesIva0().contains(cliente)){
						participa = true;
					}
				}
			}
		}
		return participa;
	}

	public Boolean getExcluirPuntoVentaManuales() {
		return excluirPuntoVentaManuales == null ? Boolean.FALSE : this.excluirPuntoVentaManuales;
	}

	public void setExcluirPuntoVentaManuales(Boolean excluirPuntoVentaManuales) {
		this.excluirPuntoVentaManuales = excluirPuntoVentaManuales;		
	}
	
	public static String buscarObservacionMonotributista() {
		try{
			Query query = XPersistence.getManager().createNativeQuery("select obs_monotributo from public.parametros_afip where obs_monotributo is not null");
			query.setMaxResults(1);
			List<?> result = query.getResultList();
			if (!result.isEmpty()){
				return result.get(0).toString();
			}
			else{
				return "";
			}
		}
		catch(Exception e){
			return "";
		}
	}
}
