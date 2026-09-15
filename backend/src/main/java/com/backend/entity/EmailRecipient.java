package com.backend.entity;

import com.backend.domain.Sector;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_recipients", indexes = {
    @Index(name = "idx_email_recipients_group", columnList = "group_id"),
    @Index(name = "idx_email_recipients_email", columnList = "email")
})
public class EmailRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private RecipientGroup group;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(length = 100)
    private String district;

    @Column(length = 100)
    private String city;

    @Column(length = 300)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Sector sector;

    @Column(name = "recipient_type", length = 50)
    private String recipientType = "B2B_EXPORTER";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public EmailRecipient() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RecipientGroup getGroup() {
        return group;
    }

    public void setGroup(RecipientGroup group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
