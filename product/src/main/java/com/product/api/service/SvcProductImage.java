package com.product.api.service;

import java.util.List;

import com.product.api.dto.ApiResponse;
import com.product.api.entity.ProductImage;

public interface SvcProductImage {

	/* ---- Endpoints ---- */
	
	public List<ProductImage> getProductImages(Integer product_id);
	
	public ApiResponse createImageProduct(ProductImage in);
	
	public ApiResponse deleteImageProduct(Integer id);
	
}
