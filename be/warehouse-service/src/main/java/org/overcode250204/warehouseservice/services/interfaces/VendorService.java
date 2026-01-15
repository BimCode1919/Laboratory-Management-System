package org.overcode250204.warehouseservice.services.interfaces;

import org.overcode250204.warehouseservice.model.dto.reagent.VendorRequest;
import org.overcode250204.warehouseservice.model.dto.reagent.VendorResponse;

import java.util.List;
import java.util.UUID;

public interface VendorService {
    VendorResponse createVendor(VendorRequest request);
    VendorResponse updateVendor(UUID id, VendorRequest request);
    String deleteVendor(UUID id);
    List<VendorResponse> getAllVendors();
}
