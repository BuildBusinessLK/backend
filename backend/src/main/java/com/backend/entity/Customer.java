package com.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String district;

    @Column(length = 100)
    private String city;

    private int age;

    @Column(name = "purchase_frequency")
    private int purchaseFrequency;

    @Column(name = "total_spent")
    private double totalSpent;

    @Column(name = "last_purchase_date")
    private LocalDateTime lastPurchaseDate;

    @Column(name = "is_vip")
    private boolean isVip;

    @Column(name = "is_new")
    private boolean isNew;

    @Column(name = "is_frequent_buyer")
    private boolean isFrequentBuyer;

    @Column(name = "is_high_spending")
    private boolean isHighSpending;

    @Column(name = "is_inactive")
    private boolean isInactive;

    @Column(name = "interested_in_discounts")
    private boolean interestedInDiscounts;

    @Column(name = "interested_in_new_products")
    private boolean interestedInNewProducts;

    @Column(name = "product_category", length = 255)
    private String productCategory;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getPurchaseFrequency() {
        return purchaseFrequency;
    }

    public void setPurchaseFrequency(int purchaseFrequency) {
        this.purchaseFrequency = purchaseFrequency;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }

    public LocalDateTime getLastPurchaseDate() {
        return lastPurchaseDate;
    }

    public void setLastPurchaseDate(LocalDateTime lastPurchaseDate) {
        this.lastPurchaseDate = lastPurchaseDate;
    }

    public boolean isVip() {
        return isVip;
    }

    public void setVip(boolean vip) {
        isVip = vip;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public boolean isFrequentBuyer() {
        return isFrequentBuyer;
    }

    public void setFrequentBuyer(boolean frequentBuyer) {
        isFrequentBuyer = frequentBuyer;
    }

    public boolean isHighSpending() {
        return isHighSpending;
    }

    public void setHighSpending(boolean highSpending) {
        isHighSpending = highSpending;
    }

    public boolean isInactive() {
        return isInactive;
    }

    public void setInactive(boolean inactive) {
        isInactive = inactive;
    }

    public boolean isInterestedInDiscounts() {
        return interestedInDiscounts;
    }

    public void setInterestedInDiscounts(boolean interestedInDiscounts) {
        this.interestedInDiscounts = interestedInDiscounts;
    }

    public boolean isInterestedInNewProducts() {
        return interestedInNewProducts;
    }

    public void setInterestedInNewProducts(boolean interestedInNewProducts) {
        this.interestedInNewProducts = interestedInNewProducts;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
