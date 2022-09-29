package org.openxava.ventas.actions;

import java.io.*;

import org.openxava.base.actions.*;
import org.openxava.base.calculators.*;
import org.openxava.base.model.*;
import org.openxava.clasificadores.model.*;
import org.openxava.compras.model.*;
import org.openxava.contabilidad.model.*;
import org.openxava.impuestos.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.calculators.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

import com.csvreader.*;

public class ProcesarCSVProductoAction extends ProcesarCSVGenericoAction{
	
	@Override
	protected void procesarLineaCSV(CsvReader csvReader) throws IOException {
		int cantidadColumnas = csvReader.getColumnCount();
		String codigo = csvReader.get(0);
		if (!Is.emptyString(codigo)){
			Producto producto = (Producto)ObjetoEstatico.buscarPorCodigo(codigo, Producto.class.getSimpleName());			
			if (producto == null){
				producto = crearProducto(codigo);
			}
			producto.setNombre(csvReader.get(1));
			String iva = (String)csvReader.get(2);
			producto.setTasaIva(TasaImpuesto.buscarTasaPorPorcentaje(this.convertirStrDecimal(iva)));												
			producto.setMarca((Marca)ObjetoEstatico.buscarPorCodigo((String)csvReader.get(3), Marca.class.getSimpleName()));
			producto.setGenero((Genero)ObjetoEstatico.buscarPorCodigo((String)csvReader.get(4), Genero.class.getSimpleName()));
			producto.setLinea((Linea)ObjetoEstatico.buscarPorCodigo((String)csvReader.get(5), Linea.class.getSimpleName()));
			producto.setRubro((Rubro)ObjetoEstatico.buscarPorCodigo((String)csvReader.get(6), Rubro.class.getSimpleName()));
			producto.setFamilia((Familia)ObjetoEstatico.buscarPorCodigo((String)csvReader.get(7), Familia.class.getSimpleName()));
			producto.setColor((Color)ObjetoEstatico.buscarPorCodigo((String)csvReader.get(8), Color.class.getSimpleName()));
			producto.setFabricante((Fabricante)ObjetoEstatico.buscarPorCodigo((String)csvReader.get(9), Fabricante.class.getSimpleName()));
			producto.setCuentaContableVentas((CuentaContable)ObjetoEstatico.buscarPorCodigo((String)csvReader.get(10), CuentaContable.class.getSimpleName()));
			producto.setCuentaContableCompras((CuentaContable)ObjetoEstatico.buscarPorCodigo((String)csvReader.get(11), CuentaContable.class.getSimpleName()));
			producto.setRegimenRetencionGanancias((Impuesto)ObjetoEstatico.buscarPorCodigo((String)csvReader.get(12), Impuesto.class.getSimpleName()));			
			
			int i = 13;
			if (cantidadColumnas > i){
				if (!Is.emptyString(csvReader.get(i))){
					producto.setProductoNumero1(this.convertirStrDecimal(csvReader.get(i)));
				}
			}
			i++;
			if (cantidadColumnas > i){
				if (!Is.emptyString(csvReader.get(i))){
					producto.setProductoNumero2(this.convertirStrDecimal(csvReader.get(i)));
				}
			}
			i++;
			if (cantidadColumnas > i){
				if (!Is.emptyString(csvReader.get(i))){
					producto.setProductoNumero3(this.convertirStrDecimal(csvReader.get(i)));
				}
			}
			i++;
			if (cantidadColumnas > i){
				producto.setCajaProducto((CajaProducto)ObjetoEstatico.buscarPorCodigo((String)csvReader.get(i), CajaProducto.class.getSimpleName()));
			}
			i++;
			if (cantidadColumnas > i){
				String codigoAnterior = csvReader.get(i);
				if (!Is.emptyString(codigoAnterior)){
					producto.setCodigoAnterior(codigoAnterior);
				}
			}
			i++;
			if (cantidadColumnas > i){
				String codProv = csvReader.get(i);
				if (!Is.emptyString(codProv)){
					producto.setProveedor((Proveedor)ObjetoEstatico.buscarPorCodigo(codProv, Proveedor.class.getSimpleName()));
				}
			}
			i++;
			if (cantidadColumnas > i){
				String codProductoProveedor = csvReader.get(i);
				if (!Is.emptyString(codProductoProveedor)){
					producto.setCodigoProveedor(codProductoProveedor);
				}
			}
			
			i++;
			if (cantidadColumnas > i){
				producto.setSubfamilia((Subfamilia)ObjetoEstatico.buscarPorCodigo((String)csvReader.get(i), Subfamilia.class.getSimpleName())); 
			}
			
			i++;
			if (cantidadColumnas > i){
				producto.setModelo((Modelo)ObjetoEstatico.buscarPorCodigo((String)csvReader.get(i), Modelo.class.getSimpleName())); 
			}
			
			i++;
			if (cantidadColumnas > i){
				producto.setCategoria((CategoriaProducto)ObjetoEstatico.buscarPorCodigo((String)csvReader.get(i), CategoriaProducto.class.getSimpleName())); 
			}
			
			i++;
			if (cantidadColumnas > i){
				producto.setImpuestoInterno((Impuesto)ObjetoEstatico.buscarPorCodigo((String)csvReader.get(i), Impuesto.class.getSimpleName()));
			}
			
			if (producto.esNuevo()){
				i++;
				if (cantidadColumnas > i){
					producto.setDespacho(this.convertirStrLogico(csvReader.get(i)));
				}
				
				i++;
				if (cantidadColumnas > i){
					producto.setLote(this.convertirStrLogico(csvReader.get(i)));
				}
			}
			
			XPersistence.getManager().persist(producto);
		}
		else{
			throw new ValidationException("Código no asignado");
		}
	}
	
	private Producto crearProducto(String codigo){
		Producto p = new Producto();
		p.setCodigo(codigo);
		ObjetoPrincipalCalculator calculatorPrincipal = new ObjetoPrincipalCalculator();
		calculatorPrincipal.setEntidad("UnidadMedida");
		
		try {
			p.setUnidadMedida((UnidadMedida)calculatorPrincipal.calculate());
		} catch (Exception e) {		
		}
		
		try{
			ValoresDefectoEsquemaCalculator calculator = new ValoresDefectoEsquemaCalculator();
			calculator.setAtributo("UtilizaDespacho");
			p.setDespacho((Boolean)calculator.calculate());
		}
		catch(Exception e){
			throw new ValidationException("No se pudo asignar despacho: " + e.toString());
		}
		
		try{
			ValoresDefectoEsquemaCalculator calculator = new ValoresDefectoEsquemaCalculator();
			calculator.setAtributo("UtilizaLote");
			p.setLote((Boolean)calculator.calculate());
		}
		catch(Exception e){
			throw new ValidationException("No se pudo asignar lote: " + e.toString());
		}
		
		p.setTipo(TipoProducto.Producto);
		return p;
	}

	@Override
	protected void preProcesarCSV() throws Exception {		
	}
	
	@Override
	protected void posProcesarCSV() throws Exception {		
	}	
}

