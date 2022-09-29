package org.openxava.compras.validators;

import java.math.BigDecimal;

import org.openxava.base.validators.ItemTransaccionValidator;
import org.openxava.negocio.model.UnidadMedida;
import org.openxava.util.Messages;
import org.openxava.ventas.model.Producto;

@SuppressWarnings("serial")
public class ItemCompraElectronicaValidator extends ItemTransaccionValidator {

	private BigDecimal cantidad;
	
	private UnidadMedida unidadMedida;
	
	private BigDecimal cantidadExcede;
	
	private BigDecimal pendienteFacturar;		
	
	private Producto producto;
	
	public BigDecimal getCantidad() {
		return cantidad == null ? BigDecimal.ZERO : this.cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}

	public BigDecimal getCantidadExcede() {
		return cantidadExcede == null ? BigDecimal.ZERO : this.cantidadExcede;
	}

	public void setCantidadExcede(BigDecimal cantidadExcede) {
		this.cantidadExcede = cantidadExcede;
	}

	public BigDecimal getPendienteFacturar() {
		return pendienteFacturar == null ? BigDecimal.ZERO : pendienteFacturar;
	}

	public void setPendienteFacturar(BigDecimal pendienteFacturar) {
		this.pendienteFacturar = pendienteFacturar;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public UnidadMedida getUnidadMedida() {
		return unidadMedida;
	}

	public void setUnidadMedida(UnidadMedida unidadMedida) {
		this.unidadMedida = unidadMedida;
	}

	@Override
	protected void validarItemTransaccion(Messages errores) {
		if ((this.getProducto() != null) && (this.getUnidadMedida() != null)){
			if (this.getPendienteFacturar().compareTo(BigDecimal.ZERO) > 0){
				if (this.getCantidadExcede().compareTo(this.getCantidad()) > 0){
					errores.add(this.getProducto().getCodigo() + ": La cantidad que excede no puede superar a la cantidad facturada");
				}
				else{
					BigDecimal cantidadACancelar = this.getCantidad().subtract(this.getCantidadExcede());
					int comp = this.getPendienteFacturar().compareTo(cantidadACancelar);
					if (comp > 0){
						if (this.getCantidadExcede().compareTo(BigDecimal.ZERO) > 0){
							errores.add(this.getProducto().getCodigo() + ": La cantidad que excede es incorrecta. Hay pendiente " + this.getPendienteFacturar().toString() + " y la cantidad a cancelar es " + cantidadACancelar.toString());
						}
					}
					else if (comp < 0){
						errores.add(this.getProducto().getCodigo() + ": La cantidad a cancelar " + cantidadACancelar.toString() + " supera lo pendiente " + this.getPendienteFacturar().toString());
					}
				}
			}
			else{
				if (this.getCantidadExcede().compareTo(BigDecimal.ZERO) != 0){
					errores.add(this.getProducto().getCodigo() + ": La cantidad que excede debe ser cero. El item no fue generado desde una recepción de mercadería");
				}
			}
		}
	}

}

