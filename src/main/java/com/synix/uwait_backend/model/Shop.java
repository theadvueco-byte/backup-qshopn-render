package com.synix.uwait_backend.model;

import com.synix.uwait_backend.model.subscription.Subscription;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "shops",
        indexes = {
                @Index(name = "idx_shop_public_code", columnList = "publicCode", unique = true),
                @Index(name = "idx_shop_slug", columnList = "slug")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Shop {

    @Id
    @UuidGenerator
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 10)
    private String publicCode;

    @Column(unique = true)
    private String slug;

    @Column(name = "qr_code", columnDefinition = "bytea")
    private byte[] qrCode;


    @Column(precision = 5, scale = 2)
    private BigDecimal shopOfferPercent; // e.g., 25 for 25%

    @Column
    private String shopOfferName;

    @Column
    private Boolean offerActive; // true if festival offer is active


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;


    private String shopName;
    private String category;
    private String whatsappNumber;

    private String fullAddress;
    private String area;
    private String city;
    private String pincode;
    private String upiId;

    private String logoPath;

    @Column(length = 120)
    private String shortBio;
    private String instagramId;

    private String shopPhotoPath;
    @Column(length = 100)
    private String gpsLocation;


    private String upiScreenshotPath;
    private Boolean upiVerified;

    @Enumerated(EnumType.STRING)
    private ShopStatus status;

    @OneToOne(mappedBy = "shop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Subscription subscription;

    @Column(nullable = false)
    private boolean active;

    private String ownerEmail;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }
}
