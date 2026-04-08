package com.edu.uptc.EnVivo.repository;

import com.edu.uptc.EnVivo.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

	interface EventSalesProjection {
		Long getEventId();
		String getEventName();
		Long getTicketsSold();
		Long getRevenue();
	}

	interface TicketTypeSalesProjection {
		Long getEventId();
		String getTicketTypeName();
		Integer getUnitPrice();
		Long getSoldQuantity();
		Long getRevenue();
	}

	List<Purchase> findByUserIdOrderByPurchaseDateDesc(Long userId);

	@Query("""
			SELECT e.event_id as eventId,
			       e.name as eventName,
			       COALESCE(SUM(d.quantity), 0) as ticketsSold,
			       COALESCE(SUM(d.subtotal), 0) as revenue
			FROM Purchase p
			JOIN p.details d
			JOIN d.ticket t
			JOIN t.event e
			WHERE (:eventId IS NULL OR e.event_id = :eventId)
			GROUP BY e.event_id, e.name
			ORDER BY e.name ASC
			""")
	List<EventSalesProjection> findEventSalesByEvent(@Param("eventId") Long eventId);

	@Query("""
			SELECT e.event_id as eventId,
			       tt.name as ticketTypeName,
			       d.unitPrice as unitPrice,
			       COALESCE(SUM(d.quantity), 0) as soldQuantity,
			       COALESCE(SUM(d.subtotal), 0) as revenue
			FROM Purchase p
			JOIN p.details d
			JOIN d.ticket t
			JOIN t.event e
			JOIN t.ticketType tt
			WHERE (:eventId IS NULL OR e.event_id = :eventId)
			GROUP BY e.event_id, tt.name, d.unitPrice
			ORDER BY tt.name ASC
			""")
	List<TicketTypeSalesProjection> findTicketTypeSalesByEvent(@Param("eventId") Long eventId);

	@Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM Purchase p")
	Long getTotalRevenueAllSales();

	@Query("SELECT COALESCE(SUM(d.quantity), 0) FROM PurchaseDetail d")
	Long getTotalTicketsSoldAllSales();
}

