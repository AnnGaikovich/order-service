package org.example.orderservice.repository;

import org.example.orderservice.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    // Новый метод: Get non-deleted order by ID and user ID
    Optional<Order> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);

    // Get orders by user ID (non-deleted)
    List<Order> findByUserIdAndDeletedFalse(Long userId);

    // Get paginated orders by user ID (non-deleted)
    Page<Order> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);

    // Get non-deleted order by ID
    Optional<Order> findByIdAndDeletedFalse(Long id);

    // Soft delete order
    @Modifying
    @Query("UPDATE Order o SET o.deleted = true, o.updatedAt = CURRENT_TIMESTAMP WHERE o.id = :id")
    void softDeleteById(@Param("id") Long id);

    // Check if order exists and not deleted
    boolean existsByIdAndDeletedFalse(Long id);
}