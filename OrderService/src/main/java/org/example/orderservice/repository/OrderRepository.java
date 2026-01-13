package org.example.orderservice.repository;

import org.example.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Optional<Order> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);

    List<Order> findByUserIdAndDeletedFalse(Long userId);

    Optional<Order> findByIdAndDeletedFalse(Long id);

}