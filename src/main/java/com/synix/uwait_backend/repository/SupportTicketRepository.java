package com.synix.uwait_backend.repository;

import com.synix.uwait_backend.model.Shop;
import com.synix.uwait_backend.model.supports.SupportTicket;
import com.synix.uwait_backend.model.supports.TicketTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    List<SupportTicket> findByShop(Shop shop);

    List<SupportTicket> findByTarget(TicketTarget target);

    Optional<SupportTicket> findByIdAndSenderEmail(Long id, String senderEmail);
}
