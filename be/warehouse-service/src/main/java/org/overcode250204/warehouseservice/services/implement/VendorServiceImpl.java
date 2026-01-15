package org.overcode250204.warehouseservice.services.implement;

import lombok.RequiredArgsConstructor;
import org.overcode250204.warehouseservice.exceptions.ErrorCode;
import org.overcode250204.warehouseservice.exceptions.WarehouseException;
import org.overcode250204.warehouseservice.model.dto.reagent.VendorRequest;
import org.overcode250204.warehouseservice.model.dto.reagent.VendorResponse;
import org.overcode250204.warehouseservice.model.entities.Vendor;
import org.overcode250204.warehouseservice.repositories.VendorRepository;
import org.overcode250204.warehouseservice.services.interfaces.VendorService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendorServiceImpl implements VendorService {

    private final VendorRepository vendorRepository;

    @Override
    public VendorResponse createVendor(VendorRequest request) {
        if (request == null) {
            throw new WarehouseException(ErrorCode.INVALID_REQUEST);
        }
        boolean exists = vendorRepository.findByVendorNameIgnoreCase(request.getVendorName()).isPresent();
        if (exists) {
            throw new WarehouseException(ErrorCode.VENDOR_NAME_ALREADY_EXISTS);
        }

        Vendor vendor = new Vendor();
        vendor.setVendorName(request.getVendorName());
        Vendor saved = vendorRepository.save(vendor);

        return toResponse(saved);
    }

    @Override
    public VendorResponse updateVendor(UUID id, VendorRequest request) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new WarehouseException(ErrorCode.VENDOR_NOT_FOUND));

        vendor.setVendorName(request.getVendorName());
        Vendor updated = vendorRepository.save(vendor);
        return toResponse(updated);
    }

    @Override
    public String deleteVendor(UUID id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new WarehouseException(ErrorCode.VENDOR_NOT_FOUND));
        vendorRepository.delete(vendor);
        return "Vendor with id '" + id + "' deleted successfully!";
    }

    @Override
    public List<VendorResponse> getAllVendors() {
        List<Vendor> vendors = vendorRepository.findAll();
        if (vendors.isEmpty()) {
            throw new WarehouseException(ErrorCode.NO_DATA);
        }
        return vendors.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private VendorResponse toResponse(Vendor vendor) {
        return VendorResponse.builder()
                .vendorId(vendor.getVendorId())
                .vendorName(vendor.getVendorName())
                .build();
    }
}
