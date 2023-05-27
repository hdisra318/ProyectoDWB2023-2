package com.invoice.api.repository;

import java.time.LocalDateTime;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.invoice.api.entity.Invoice;



@Repository
public interface RepoInvoice extends JpaRepository<Invoice, Integer>{

	List<Invoice> findByRfcAndStatus(String rfc, Integer status);

	/**
	 * Agregar una factura
	 */
	@Modifying
	@Transactional
	@Query(value = "UPDATE invoice SET invoice_id = :invoice_id, rfc = :rfc, subtotal = :subtotal, taxes = :taxes, total = :total, created_at = :created_at, status = 1 WHERE status = 1 AND invoice_id = :invoice_id", nativeQuery = true)
	void generateInvoice(@Param("invoice_id") Integer invoice_id, @Param("rfc") String rfc, @Param("subtotal") Double subtotal, 
	@Param("taxes") Double taxes, @Param("total") Double total, @Param("created_at") LocalDateTime created_at);
	

}
