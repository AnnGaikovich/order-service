package org.example.orderservice.specification;

import org.example.orderservice.entity.Order;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
import java.util.List;

public class OrderSpecifications {

    public static Specification<Order> notDeleted() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("deleted"), false);
    }

    public static Specification<Order> hasStatusIn(List<String> statuses) {
        return (root, query, criteriaBuilder) ->
                statuses != null && !statuses.isEmpty() ?
                        root.get("status").in(statuses) :
                        criteriaBuilder.conjunction();
    }

    public static Specification<Order> createdAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(root.get("createdAt"), startDate, endDate);
            } else if (startDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate);
            } else if (endDate != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate);
            }
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<Order> hasUserId(Long userId) {
        return (root, query, criteriaBuilder) ->
                userId != null ?
                        criteriaBuilder.equal(root.get("userId"), userId) :
                        criteriaBuilder.conjunction();
    }
}