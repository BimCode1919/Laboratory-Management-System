package org.overcode250204.warehouseservice.seeder;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.overcode250204.warehouseservice.model.entities.*;
import org.overcode250204.warehouseservice.model.enums.Status;
import org.overcode250204.warehouseservice.model.enums.SupplyStatus;
import org.overcode250204.warehouseservice.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseSeeder implements CommandLineRunner {

    private final VendorRepository vendorRepo;
    private final ReagentsRepository reagentRepo;
    private final ReagentSupplyHistoryRepository supplyRepo;
    private final InstrumentsRepository instrumentRepo;
    private final ConfigurationRepository configRepo;
    private final InstrumentReagentsRepository instrumentReagentRepo;

    private static final UUID SYSTEM_UUID = UUID.fromString("06ec466c-47c7-492d-9694-84ef7b245c13");

    @Transactional
    public void seedData() {

        // === STEP 1: Vendors ===
        List<Vendor> savedVendors = new ArrayList<>();
        if (vendorRepo.count() == 0) {
            List<Vendor> vendors = List.of(
                    // Dùng constructor (String) hoặc builder()
                    // Giả sử bạn có constructor (String vendorName)
                    new Vendor(null, "Sysmex Corporation"),
                    new Vendor(null, "Beckman Coulter"),
                    new Vendor(null, "Abbott Diagnostics"),
                    new Vendor(null, "Mindray Bio-Medical"),
                    new Vendor(null, "Erba Mannheim")
            );
            savedVendors = vendorRepo.saveAll(vendors);
        } else {
            savedVendors = vendorRepo.findAll();
        }
        Map<String, Vendor> vendorMap = savedVendors.stream()
                .collect(Collectors.toMap(Vendor::getVendorName, v -> v));


        // === STEP 2: Reagents ===
        List<Reagent> savedReagents = new ArrayList<>();
        if (reagentRepo.count() == 0) {
            List<Reagent> reagents = List.of(
                    Reagent.builder()
                            .createdBy(SYSTEM_UUID)
                            .updatedBy(SYSTEM_UUID)
                            .name("DILUENT")
                            .catalogNumber("RGT-001")
                            .manufacturer("Sysmex")
                            .casNumber("9004-70-0")
                            .createdAt(LocalDateTime.of(2025, 11, 10, 10, 32, 44))
                            .updatedAt(LocalDateTime.of(2025, 11, 10, 10, 32, 44))
                            .build(),
                    Reagent.builder()
                            .createdBy(SYSTEM_UUID)
                            .updatedBy(SYSTEM_UUID)
                            .name("LYSING")
                            .catalogNumber("RGT-002")
                            .manufacturer("Beckman Coulter")
                            .casNumber("8002-43-5")
                            .createdAt(LocalDateTime.of(2025, 11, 10, 10, 32, 44))
                            .updatedAt(LocalDateTime.of(2025, 11, 10, 10, 32, 44))
                            .build(),
                    // BẠN ĐÃ THIẾU 2 MỤC NÀY
                    Reagent.builder()
                            .createdBy(SYSTEM_UUID)
                            .updatedBy(SYSTEM_UUID)
                            .name("STAINING")
                            .catalogNumber("RGT-003")
                            .manufacturer("Abbott Diagnostics")
                            .casNumber("7647-14-5")
                            .createdAt(LocalDateTime.of(2025, 11, 10, 10, 32, 44))
                            .updatedAt(LocalDateTime.of(2025, 11, 10, 10, 32, 44))
                            .build(),
                    Reagent.builder()
                            .createdBy(SYSTEM_UUID)
                            .updatedBy(SYSTEM_UUID)
                            .name("CLOTTING")
                            .catalogNumber("RGT-004")
                            .manufacturer("Mindray")
                            .casNumber("7786-30-3")
                            .createdAt(LocalDateTime.of(2025, 11, 10, 10, 32, 44))
                            .updatedAt(LocalDateTime.of(2025, 11, 10, 10, 32, 44))
                            .build(),
                    Reagent.builder()
                            .createdBy(SYSTEM_UUID)
                            .updatedBy(SYSTEM_UUID)
                            .name("CLEANER")
                            .catalogNumber("RGT-005")
                            .manufacturer("Erba Mannheim")
                            .casNumber("7732-18-5")
                            .createdAt(LocalDateTime.of(2025, 11, 10, 10, 32, 44))
                            .updatedAt(LocalDateTime.of(2025, 11, 10, 10, 32, 44))
                            .build(),
                    // SECOND DILUENT (to match warehouse.sql which has two DILUENT entries)
                    Reagent.builder()
                            .createdBy(SYSTEM_UUID)
                            .updatedBy(SYSTEM_UUID)
                            .name("DILUENT")
                            .catalogNumber("RGT-001")
                            .manufacturer("Sysmex")
                            .casNumber("9004-70-0")
                            .createdAt(LocalDateTime.of(2025, 11, 10, 10, 32, 44))
                            .updatedAt(LocalDateTime.of(2025, 11, 10, 10, 32, 44))
                            .build()
            );
            savedReagents = reagentRepo.saveAll(reagents);
        } else {
            savedReagents = reagentRepo.findAll();
        }
        Map<String, List<Reagent>> reagentMap = savedReagents.stream()
                .collect(Collectors.groupingBy(Reagent::getName));

        // === STEP 3: Instruments ===
        List<Instrument> savedInstruments = new ArrayList<>();
        if (instrumentRepo.count() == 0) {
            List<Instrument> instruments = List.of(
                    Instrument.builder()
                            .createdAt(LocalDateTime.of(2025, 11, 7, 23, 9, 11, 999907000))
                            .updatedAt(LocalDateTime.of(2025, 11, 8, 3, 25, 34))
                            .createdBy(SYSTEM_UUID)
                            .updatedBy(SYSTEM_UUID)
                            .status(Status.MAINTENANCE)
                            .model("i2000SR")
                            .name("Abbott Architect i2000SR")
                            .errorMessage("Abbott")
                            .serialNumber("ABB-2000")
                            .configVersion("25.01.02-LTS")
                            .instrumentCode("JAVA-08")
                            .location("F-TOWN-1")
                            .build(),
                    // BẠN ĐÃ THIẾU MỤC NÀY
                    Instrument.builder()
                            .createdAt(LocalDateTime.of(2025, 11, 7, 23, 9, 11, 999887000))
                            .updatedAt(LocalDateTime.of(2025, 11, 8, 3, 25, 32))
                            .createdBy(SYSTEM_UUID)
                            .updatedBy(SYSTEM_UUID)
                            .status(Status.INACTIVE)
                            .model("AU480")
                            .name("Beckman AU480")
                            .errorMessage("Abbott")
                            .serialNumber("BKM-480")
                            .configVersion("25.01.02-LTS")
                            .instrumentCode("JAVA-09")
                            .location("F-TOWN-2")
                            .build(),
                    Instrument.builder()
                            .createdAt(LocalDateTime.of(2025, 11, 7, 23, 9, 11, 999844000))
                            .updatedAt(LocalDateTime.of(2025, 11, 8, 3, 25, 26))
                            .createdBy(SYSTEM_UUID)
                            .updatedBy(SYSTEM_UUID)
                            .status(Status.READY)
                            .model("XN-1000")
                            .name("Sysmex XN-1000")
                            .errorMessage("Abbott")
                            .serialNumber("SYS-001")
                            .configVersion("25.01.02-LTS")
                            .instrumentCode("JAVA-07")
                            .location("F-TOWN-3")
                            .build()
            );
            savedInstruments = instrumentRepo.saveAll(instruments);
        } else {
            savedInstruments = instrumentRepo.findAll();
        }
        // TẠO MAP ĐỂ DÙNG CHO STEP 4
        Map<String, Instrument> instrumentMap = savedInstruments.stream()
                .collect(Collectors.toMap(Instrument::getName, i -> i));


        // === STEP 4: Configurations ===
        if (configRepo.count() == 0) {
            // Lấy instruments từ map
            Instrument sysmex = instrumentMap.get("Sysmex XN-1000");
            Instrument abbott = instrumentMap.get("Abbott Architect i2000SR");
            Instrument beckman = instrumentMap.get("Beckman AU480");

            if (sysmex == null || abbott == null || beckman == null) {
                throw new RuntimeException("Không tìm thấy instrument để seed config");
            }

            List<Configuration> configs = List.of(
                    Configuration.builder()
                            .isGlobal(true)
                            .createdAt(LocalDateTime.of(2025, 11, 7, 23, 9, 11, 983569000))
                            .updatedAt(LocalDateTime.of(2025, 11, 8, 3, 27, 0))
                            .createdBy(SYSTEM_UUID)
                            .updatedBy(SYSTEM_UUID)
                            .configKey("CBC")
                            .configName("Configuration for CBC test type")
                            .configValue("CBC")
                            .defaultValue("CBC")
                            .description("CBC (Complete Blood Count) — used to evaluate overall health and detect blood disorders by measuring red cells, white cells, hemoglobin, hematocrit, and platelets.")
                            .build(),
                    Configuration.builder()
                            .isGlobal(true)
                            .createdAt(LocalDateTime.of(2025, 11, 7, 23, 9, 11, 983580000))
                            .updatedAt(LocalDateTime.of(2025, 11, 8, 3, 27, 10))
                            .createdBy(SYSTEM_UUID)
                            .updatedBy(SYSTEM_UUID)
                            .configKey("HBA1C")
                            .configName("Configuration for HBA1C test type")
                            .configValue("HBA1C")
                            .defaultValue("HBA1C")
                            .description("Hemoglobin A1C (HBA1C) — used to monitor long-term blood glucose levels in diabetic patients by measuring the average blood sugar over the past 2–3 months.")
                            .build(),
                    Configuration.builder()
                            .isGlobal(true)
                            .createdAt(LocalDateTime.of(2025, 11, 7, 23, 9, 11, 983558000))
                            .updatedAt(LocalDateTime.of(2025, 11, 8, 3, 27, 8))
                            .createdBy(SYSTEM_UUID)
                            .updatedBy(SYSTEM_UUID)
                            .configKey("LFT")
                            .configName("Configuration for LFT test type")
                            .configValue("LFT")
                            .defaultValue("LFT")
                            .description("HBA1C (Hemoglobin A1C) — used to monitor long-term blood glucose levels in diabetic patients by measuring the average blood sugar over the past 2–3 months.")
                            .build(),
                    // Config cho Sysmex
                    Configuration.builder()
                            .isGlobal(false)
                            .instrument(sysmex) // GÁN INSTRUMENT
                            .createdAt(LocalDateTime.of(2025, 11, 7, 23, 9, 11, 983569000))
                            .updatedAt(LocalDateTime.of(2025, 11, 8, 3, 27, 0))
                            .createdBy(SYSTEM_UUID)
                            .updatedBy(SYSTEM_UUID)
                            .configKey("CBC")
                            .configName("Configuration for CBC test type")
                            .configValue("CBC")
                            .defaultValue("CBC")
                            .description("CBC (Complete Blood Count) — used to evaluate overall health and detect blood disorders by measuring red cells, white cells, hemoglobin, hematocrit, and platelets.")
                            .build(),
                    Configuration.builder()
                            .isGlobal(false)
                            .instrument(sysmex) // GÁN INSTRUMENT
                            .createdAt(LocalDateTime.of(2025, 11, 7, 23, 9, 11, 983580000))
                            .updatedAt(LocalDateTime.of(2025, 11, 8, 3, 27, 10))
                            .createdBy(SYSTEM_UUID)
                            .updatedBy(SYSTEM_UUID)
                            .configKey("HBA1C")
                            .configName("Configuration for HBA1C test type")
                            .configValue("HBA1C")
                            .defaultValue("HBA1C")
                            .description("Hemoglobin A1C (HBA1C) — used to monitor long-term blood glucose levels in diabetic patients by measuring the average blood sugar over the past 2–3 months.")
                            .build(),
                    // Config cho Abbott
                    Configuration.builder()
                            .isGlobal(false)
                            .instrument(abbott) // GÁN INSTRUMENT
                            .createdAt(LocalDateTime.of(2025, 11, 7, 23, 9, 11, 983546000))
                            .updatedAt(LocalDateTime.of(2025, 11, 8, 3, 27, 0))
                            .createdBy(SYSTEM_UUID)
                            .updatedBy(SYSTEM_UUID)
                            .configKey("HBA1C")
                            .configName("Configuration for HBA1C test type")
                            .configValue("HBA1C")
                            .defaultValue("HBA1C")
                            .description("Hemoglobin A1C (HBA1C) — used to monitor long-term blood glucose levels in diabetic patients by measuring the average blood sugar over the past 2–3 months.")
                            .build(),
                    Configuration.builder()
                            .isGlobal(false)
                            .instrument(abbott) // GÁN INSTRUMENT
                            .createdAt(LocalDateTime.of(2025, 11, 7, 23, 9, 11, 983492000))
                            .updatedAt(LocalDateTime.of(2025, 11, 8, 3, 27, 0))
                            .createdBy(SYSTEM_UUID)
                            .updatedBy(SYSTEM_UUID)
                            .configKey("CBC")
                            .configName("Configuration for CBC test type")
                            .configValue("CBC")
                            .defaultValue("CBC")
                            .description("CBC (Complete Blood Count) — used to evaluate overall health and detect blood disorders by measuring red cells, white cells, hemoglobin, hematocrit, and platelets.")
                            .build(),
                    // Config cho Beckman
                    Configuration.builder()
                            .isGlobal(false)
                            .instrument(beckman) // GÁN INSTRUMENT
                            .createdAt(LocalDateTime.of(2025, 11, 7, 23, 9, 11, 983558000))
                            .updatedAt(LocalDateTime.of(2025, 11, 8, 3, 27, 8))
                            .createdBy(SYSTEM_UUID)
                            .updatedBy(SYSTEM_UUID)
                            .configKey("LFT")
                            .configName("Configuration for LFT test type")
                            .configValue("LFT")
                            .defaultValue("LFT")
                            .description("HBA1C (Hemoglobin A1C) — used to monitor long-term blood glucose levels in diabetic patients by measuring the average blood sugar over the past 2–3 months.")
                            .build()
            );
            configRepo.saveAll(configs);
        }

        // === STEP 5: Instrument ↔ Reagents ===
        if (instrumentReagentRepo.count() == 0) {
            // Bây giờ savedInstruments (3) và savedReagents (5) đã ĐẦY ĐỦ
            List<InstrumentReagents> links = new ArrayList<>();

            for (Instrument ins : savedInstruments) {
                for (Reagent r : savedReagents) {
                    links.add(InstrumentReagents.builder()
                            .instrument(ins)
                            .reagent(r)
                            .quantity(50.0)
                            .assignedAt(LocalDateTime.of(2025, 11, 7, 23, 9, 12))
                            .build());
                }
            }
            instrumentReagentRepo.saveAll(links);
        }

        // === STEP 6: Reagent Supply History ===
        // Code này của bạn đã đúng, và bây giờ nó sẽ chạy
        // vì STEP 2 đã được sửa.
        if (supplyRepo.count() == 0) {
            Vendor sysmexVendor = vendorMap.get("Sysmex Corporation");
            if (sysmexVendor == null) {
                throw new RuntimeException("Không tìm thấy vendor 'Sysmex Corporation' để seed");
            }

            List<Reagent> diluentList = reagentMap.getOrDefault("DILUENT", Collections.emptyList());
            Reagent diluentReagent1 = diluentList.size() > 0 ? diluentList.get(0) : null;
            Reagent diluentReagent2 = diluentList.size() > 1 ? diluentList.get(1) : null;
            Reagent lysingReagent = reagentMap.getOrDefault("LYSING", Collections.emptyList()).stream().findFirst().orElse(null);
            Reagent stainingReagent = reagentMap.getOrDefault("STAINING", Collections.emptyList()).stream().findFirst().orElse(null);
            Reagent clottingReagent = reagentMap.getOrDefault("CLOTTING", Collections.emptyList()).stream().findFirst().orElse(null);
            Reagent cleanerReagent = reagentMap.getOrDefault("CLEANER", Collections.emptyList()).stream().findFirst().orElse(null);

            if (diluentReagent1 == null || diluentReagent2 == null || lysingReagent == null || stainingReagent == null || clottingReagent == null || cleanerReagent == null) {
                throw new RuntimeException("Một hoặc nhiều reagents không tìm thấy trong reagentMap.");
            }

            List<ReagentSupplyHistory> supplies = List.of(
                    ReagentSupplyHistory.builder()
                            .reagent(diluentReagent1)
                            .vendor(sysmexVendor)
                            .quantity(BigDecimal.valueOf(1000.00))
                            .unitOfMeasure("ml")
                            .orderDate(LocalDate.of(2025, 10, 28))
                            .receiptDate(LocalDate.of(2025, 11, 4))
                            .expirationDate(LocalDate.of(2026, 5, 7))
                            .lotNumber("LOT-29f4d4")
                            .poNumber("PO-RGT-001")
                            .storageLocation("Warehouse A - Shelf 1")
                            .note("Initial stock supply.")
                            .status(SupplyStatus.RECEIVED)
                            .receivedBy(SYSTEM_UUID)
                            .receiptTimestamp(LocalDateTime.of(2025, 11, 7, 23, 9, 11, 959691000))
                            .build(),
                    ReagentSupplyHistory.builder()
                            .reagent(lysingReagent)
                            .vendor(sysmexVendor)
                            .quantity(BigDecimal.valueOf(1000.00))
                            .unitOfMeasure("ml")
                            .orderDate(LocalDate.of(2025, 10, 28))
                            .receiptDate(LocalDate.of(2025, 11, 4))
                            .expirationDate(LocalDate.of(2026, 5, 7))
                            .lotNumber("LOT-6aaa19")
                            .poNumber("PO-RGT-002")
                            .storageLocation("Warehouse A - Shelf 1")
                            .note("Initial stock supply.")
                            .status(SupplyStatus.RECEIVED)
                            .receivedBy(SYSTEM_UUID)
                            .receiptTimestamp(LocalDateTime.of(2025, 11, 7, 23, 9, 11, 959757000))
                            .build(),
                    ReagentSupplyHistory.builder()
                            .reagent(stainingReagent)
                            .vendor(sysmexVendor)
                            .quantity(BigDecimal.valueOf(1000.00))
                            .unitOfMeasure("ml")
                            .orderDate(LocalDate.of(2025, 10, 28))
                            .receiptDate(LocalDate.of(2025, 11, 4))
                            .expirationDate(LocalDate.of(2026, 5, 7))
                            .lotNumber("LOT-1baf3b")
                            .poNumber("PO-RGT-003")
                            .storageLocation("Warehouse A - Shelf 1")
                            .note("Initial stock supply.")
                            .status(SupplyStatus.RECEIVED)
                            .receivedBy(SYSTEM_UUID)
                            .receiptTimestamp(LocalDateTime.of(2025, 11, 7, 23, 9, 11, 959787000))
                            .build(),
                    ReagentSupplyHistory.builder()
                            .reagent(clottingReagent)
                            .vendor(sysmexVendor)
                            .quantity(BigDecimal.valueOf(1000.00))
                            .unitOfMeasure("ml")
                            .orderDate(LocalDate.of(2025, 10, 28))
                            .receiptDate(LocalDate.of(2025, 11, 4))
                            .expirationDate(LocalDate.of(2026, 5, 7))
                            .lotNumber("LOT-701548")
                            .poNumber("PO-RGT-004")
                            .storageLocation("Warehouse A - Shelf 1")
                            .note("Initial stock supply.")
                            .status(SupplyStatus.RECEIVED)
                            .receivedBy(SYSTEM_UUID)
                            .receiptTimestamp(LocalDateTime.of(2025, 11, 7, 23, 9, 11, 959809000))
                            .build(),
                    ReagentSupplyHistory.builder()
                            .reagent(cleanerReagent)
                            .vendor(sysmexVendor)
                            .quantity(BigDecimal.valueOf(1000.00))
                            .unitOfMeasure("ml")
                            .orderDate(LocalDate.of(2025, 10, 28))
                            .receiptDate(LocalDate.of(2025, 11, 4))
                            .expirationDate(LocalDate.of(2026, 5, 7))
                            .lotNumber("LOT-504bd9")
                            .poNumber("PO-RGT-005")
                            .storageLocation("Warehouse A - Shelf 1")
                            .note("Initial stock supply.")
                            .status(SupplyStatus.RECEIVED)
                            .receivedBy(SYSTEM_UUID)
                            .receiptTimestamp(LocalDateTime.of(2025, 11, 7, 23, 9, 11, 959834000))
                            .build(),
                    // Second DILUENT supply (match SQL which has two DILUENT supplies)
                    ReagentSupplyHistory.builder()
                            .reagent(diluentReagent2)
                            .vendor(sysmexVendor)
                            .quantity(BigDecimal.valueOf(1000.00))
                            .unitOfMeasure("ml")
                            .orderDate(LocalDate.of(2025, 10, 28))
                            .receiptDate(LocalDate.of(2025, 11, 4))
                            .expirationDate(LocalDate.of(2026, 5, 7))
                            .lotNumber("LOT-6aaa19")
                            .poNumber("PO-RGT-002")
                            .storageLocation("Warehouse A - Shelf 1")
                            .note("Initial stock supply.")
                            .status(SupplyStatus.RECEIVED)
                            .receivedBy(SYSTEM_UUID)
                            .receiptTimestamp(LocalDateTime.of(2025, 11, 7, 23, 9, 11, 959757000))
                            .build()
            );

            supplyRepo.saveAll(supplies);
        }
    }

    @Override
    public void run(String... args) {
        seedData();
    }
}
