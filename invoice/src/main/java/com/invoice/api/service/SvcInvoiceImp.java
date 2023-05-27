package com.invoice.api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.invoice.api.dto.ApiResponse;
import com.invoice.api.dto.DtoProduct;
import com.invoice.api.entity.Cart;
import com.invoice.api.entity.Invoice;
import com.invoice.api.entity.Item;
import com.invoice.api.repository.RepoCart;
import com.invoice.api.repository.RepoInvoice;
import com.invoice.api.repository.RepoItem;
import com.invoice.configuration.client.CustomerClient;
import com.invoice.configuration.client.ProductClient;
import com.invoice.exception.ApiException;

@Service
public class SvcInvoiceImp implements SvcInvoice {

	@Autowired
	RepoInvoice repo;
	
	@Autowired
	RepoItem repoItem;
	
	@Autowired
	RepoCart repoCart;
	
	@Autowired
	CustomerClient customerCl;
	
	@Autowired
	ProductClient productCl;

	Integer idFactura = 0;

	@Override
	public List<Invoice> getInvoices(String rfc) {
		return repo.findByRfcAndStatus(rfc, 1);
	}

	@Override
	public List<Item> getInvoiceItems(Integer invoice_id) {
		return repoItem.getInvoiceItems(invoice_id);
	}

	@Override
	public ApiResponse generateInvoice(String rfc) {
		/*
		 * Requerimiento 5
		 * Implementar el método para generar una factura 
		 */
		
		List<Cart> carts = repoCart.findByRfcAndStatus(rfc, 1);
		// Si su carro no tiene productos
		if (carts == null || carts.isEmpty()) {
			throw new ApiException(HttpStatus.NOT_FOUND, "cart has no items");
		}
		
		// Creando y guardando una factura vacia
		Invoice fac = new Invoice();
		fac.setCreated_at(LocalDateTime.now());
		fac.setRfc(rfc);
		fac.setStatus(1);
		fac.setSubtotal(0.0);
		fac.setTotal(0.0);
		fac.setTaxes(-1.0);
		repo.save(fac);

		ResponseEntity<DtoProduct> response;
		
		// Obteniendo factura recien creada
		List<Invoice> facturas = repo.findByRfcAndStatus(rfc, 1);
		Invoice factura = null;
		for(int i = 0; i<facturas.size(); i++){
			if(facturas.get(i).getTaxes() == -1.0){
				factura = facturas.get(i);
				break;
			}
		}

		// Generando articulos de factura
		List<Item> listItem = new ArrayList<>();
		Item item;
		for(Cart cart : carts) {
			
			item = new Item();
			response = productCl.getProduct(cart.getGtin());
			item.setId_invoice(factura.getInvoice_id());
			item.setGtin(cart.getGtin());
			item.setQuantity(cart.getQuantity());
			item.setUnit_price(response.getBody().getPrice());
		 	item.setTotal(response.getBody().getPrice() * item.getQuantity());
		 	item.setTaxes(.16 * item.getTotal());
			item.setSubtotal(item.getTotal()- item.getTaxes());
			item.setStatus(1);

			// Guardando articulo en la base de datos 
			repoItem.save(item);
			
			System.out.println(item);
			// Agregando articulo a la lista
			listItem.add(item);
			
			// Actualizando el quantity del producto y limpiando el carrito
			productCl.updateProductStock(cart.getGtin(), cart.getQuantity());
			repoCart.clearCart(cart.getRfc());

		}
		
		// Generando datos de la factura
		Double total = 0.0;
		Double taxes = 0.0;
		Double subtotal = 0.0;

		for(Item i : listItem) {
			
			total += i.getTotal();
			taxes += i.getTaxes();
			subtotal += i.getSubtotal();
			
		}
		
		factura.setSubtotal(subtotal);
		factura.setTotal(total);
		factura.setTaxes(taxes);
		factura.setCreated_at(LocalDateTime.now());
		factura.setStatus(1);
		
		// Guardando factura en la base de datos
		repo.generateInvoice(factura.getInvoice_id(), rfc, subtotal, taxes, total, factura.getCreated_at());

		return new ApiResponse("invoice generated");

	}

}
