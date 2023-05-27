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

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO invoice " +
                "(rfc," +
                "subtotal," +
                "taxes," +
                "total," +
                "created_at," +
                "status)" +
            "VALUES " +
                "(:rfc," +
                ":subtotal," +
                ":taxes," +
                ":total," +
                ":created_at," +
                ":status)", nativeQuery = true)
    void createInvoice(@Param("rfc") String rfc,
                       @Param("subtotal") Double subtotal,
                       @Param("taxes") Double taxes,
                       @Param("total") Double total,
                       @Param("created_at") LocalDateTime created_at,
                       @Param("status") Integer status);

}
