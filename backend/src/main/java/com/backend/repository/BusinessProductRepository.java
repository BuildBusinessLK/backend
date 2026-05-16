package com.backend.repository;

import com.backend.entity.BusinessProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessProductRepository extends JpaRepository<BusinessProduct, Long> {

    List<BusinessProduct> findByBusiness_IdOrderByIdAsc(Long businessId);

    void deleteByBusiness_Id(Long businessId);
}
