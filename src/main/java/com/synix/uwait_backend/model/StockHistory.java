package com.synix.uwait_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "stock_history",
        uniqueConstraints = @UniqueConstraint(columnNames = {"shop_id", "date"}))
@Getter
@Setter
public class StockHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Shop shop;

    private LocalDate date;

    // total stock available at END of that day
    private Long stock;
}

