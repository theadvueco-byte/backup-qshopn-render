package com.synix.uwait_backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;          // Short info
    private String message;        // Full notification


    public Boolean getRead() {
        return isRead;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }

    private Boolean isRead = false;  // Default unread
    private LocalDateTime createdAt = LocalDateTime.now();

    private String sentBy = "SYSTEM";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    @JsonBackReference
    private Shop shop;             // Notification belongs to a shop

//    // Default SYSTEM/admin sender
//    Notification n1 = Notification.builder()
//            .shop(shop)
//            .title("New Order")
//            .message("You have a new order")
//            .build(); // sentBy = "SYSTEM" automatically
//
//    // Overriding sender
//    Notification n2 = Notification.builder()
//            .shop(shop)
//            .title("Promo")
//            .message("Special discount for your shop")
//            .sentBy("ADMIN")
//            .build();

public static Notification create(Shop shop, String title, String message, String sentBy) {
    Notification n = new Notification();
    n.setShop(shop);
    n.setTitle(title);
    n.setMessage(message);
    n.setSentBy(sentBy);
    return n;
}

}
