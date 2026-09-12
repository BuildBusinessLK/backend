package com.backend.repository;

import com.backend.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByBusiness_Id(Long businessId);
    void deleteByBusiness_Id(Long businessId);
}
