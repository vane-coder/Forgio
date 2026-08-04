package com.forgio.repository;

import com.forgio.entity.ShipmentItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShipmentItemRepository extends JpaRepository<ShipmentItem, UUID> {
}
