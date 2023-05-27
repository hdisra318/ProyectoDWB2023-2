package com.invoice.api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
		
		
		Item item;
		ResponseEntity<DtoProduct> response;
		List<Item> listItem = new ArrayList<>();
		
		// Generando articulos de factura
		for(Cart cart : carts) {
			
			item = new Item();
			response = productCl.getProduct(cart.getGtin());
			item.setGtin(cart.getGtin());
			item.setQuantity(cart.getQuantity());
			item.setUnit_price(response.getBody().getPrice());
			item.setTotal(response.getBody().getPrice() * item.getQuantity());
			item.setTaxes(.16 * item.getTotal());
			item.setSubtotal(item.getTotal()- item.getTaxes());
			item.setId_invoice(idFactura);
			
			listItem.add(item);
			
			// Guardando articulo en la base de datos 
			repoItem.save(item);
			
		}
		
		
		// Generando factura
		Invoice factura = new Invoice();
		
		Double total = 0.0;
		Double taxes = 0.0;
		Double subtotal = 0.0;
		LocalDateTime created_at = LocalDateTime.now();
		for(Item i : listItem) {
			
			total += i.getTotal();
			taxes += i.getTaxes();
			subtotal += i.getSubtotal();
			productCl.updateProductStock(rfc, i.getQuantity());
			
		}
		
		factura.setCreated_at(created_at);
		factura.setRfc(rfc);
		factura.setStatus(1);
		factura.setSubtotal(subtotal);
		factura.setTotal(total);
		factura.setTaxes(taxes);
		factura.setInvoice_id(idFactura++);
		
		
		
		// Guardando factura en la base de datos
		repo.save(factura);
		
		// Vaciando carrito
		repoCart.clearCart(rfc);
		
		
		return new ApiResponse("invoice generated");

	}

}
