package org.overcode250204.warehouseservice.repositories;


import org.overcode250204.warehouseservice.model.entities.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VendorRepository extends JpaRepository<Vendor, UUID> {
    Optional<Vendor> findByVendorNameIgnoreCase(String vendorName);
}
