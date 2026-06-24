package com.forgio.repository;

import com.forgio.entity.WasteRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WasteRecordRepository extends JpaRepository<WasteRecord, UUID> {
    List<WasteRecord> findByFactory_FactoryId(UUID factoryId);
    List<WasteRecord> findByEntry_EntryId(UUID entryId);
}
