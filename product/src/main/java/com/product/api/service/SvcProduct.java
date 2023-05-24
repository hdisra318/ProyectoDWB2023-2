package com.product.api.service;

import java.util.List;

import com.product.api.dto.ApiResponse;
import com.product.api.dto.DtoProductList;
import com.product.api.entity.Category;
import com.product.api.entity.Product;

public interface SvcProduct {

	/**
	 * Devuelve un lista de DtoProductList dado el id de la categoria
	 */
	public List<DtoProductList> getProducts(Integer category_id);
	
	/**
	 * Devuelve el producto dado el gtin
	 */
	public Product getProduct(String gtin);
	
	/**
	 * Crea un producto
	 */
	public ApiResponse createProduct(Product in);
	
	/**
	 * Actualiza la informacion de un producto dado
	 */
	public ApiResponse updateProduct(Product in, Integer id);
	
	/**
	 * Actualiza el stock de un producto dado
	 */
	public ApiResponse updateProductStock(String gtin, Integer stock);
	
	/**
	 * Elimina el producto dado
	 */
	public ApiResponse deleteProduct(Integer id);
	
	
	/** 
	 * Actualiza la categoria del producto
	 */
	public ApiResponse updateProductCategory(Integer category_id, String gtin);
	

}
