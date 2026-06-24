package com.forgio.repository;

import com.forgio.entity.Factory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface FactoryRepository extends JpaRepository<Factory, UUID> {}
