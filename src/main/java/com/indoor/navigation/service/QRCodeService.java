package com.indoor.navigation.service;

import com.indoor.navigation.model.*;
import com.indoor.navigation.utils.QRCodeGenerator;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enhanced QR Code service with better scanning simulation and management
 */
public class QRCodeService {
    private Scanner scanner;
    private NavigationGraph graph;
    private Map<String, String> qrCodeDatabase;
    private List<String> scanHistory;
    private boolean isSimulationMode;
    
    public QRCodeService(NavigationGraph graph) {
        this.scanner = new Scanner(System.in);
        this.graph = graph;
        this.qrCodeDatabase = new HashMap<>();
        this.scanHistory = new ArrayList<>();
        this.isSimulationMode = true;
        initializeQRDatabase();
    }
    
    private void initializeQRDatabase() {
        qrCodeDatabase.clear();
        for (Room room : graph.getAllRooms()) {
            qrCodeDatabase.put(room.getQrCode(), room.getId());
        }
    }
    
    public Room scanQRCode() {
        System.out.println("\n📱 QR Code Scanner");
        System.out.println("═══════════════════════════════════════");
        
        if (isSimulationMode) {
            return simulatedQRScan();
        } else {
            return realQRScan();
        }
    }
    
    private Room simulatedQRScan() {
        // Group rooms by floor for better organization
        Map<Integer, List<Room>> roomsByFloor = graph.getAllRooms().stream()
            .collect(Collectors.groupingBy(Room::getFloor));
        
        System.out.println("Available QR Codes by Floor:");
        
        int optionNumber = 1;
        Map<Integer, Room> optionMap = new HashMap<>();
        
        for (Integer floor : roomsByFloor.keySet().stream().sorted().collect(Collectors.toList())) {
            System.out.printf("\n🏢 FLOOR %d:%n", floor);
            
            for (Room room : roomsByFloor.get(floor)) {
                String statusIcons = getStatusIcons(room);
                System.out.printf("%2d. %-25s (%-15s) %s%n", 
                                optionNumber, 
                                room.getName(), 
                                room.getQrCode(),
                                statusIcons);
                
                // Show room description and features
                System.out.printf("    %s%n", room.getDescription());
                if (!room.getFeatures().isEmpty()) {
                    String features = room.getFeatures().entrySet().stream()
                        .map(e -> e.getKey().replace("_", " "))
                        .collect(Collectors.joining(", "));
                    System.out.printf("    🔧 Features: %s%n", features);
                }
                if (!room.getLandmarks().isEmpty()) {
                    System.out.printf("    🏷️ Near: %s%n", String.join(", ", room.getLandmarks()));
                }
                
                optionMap.put(optionNumber, room);
                optionNumber++;
            }
        }
        
        // Show recent scan history
        if (!scanHistory.isEmpty()) {
            System.out.println("\n📜 Recent Scans:");
            for (int i = 0; i < Math.min(scanHistory.size(), 3); i++) {
                System.out.printf("   • %s%n", scanHistory.get(i));
            }
        }
        
        System.out.print("\nEnter option number, QR code, or room name: ");
        String input = scanner.nextLine().trim();
        
        // Try to parse as number first
        try {
            int roomNumber = Integer.parseInt(input);
            if (optionMap.containsKey(roomNumber)) {
                Room selectedRoom = optionMap.get(roomNumber);
                addToScanHistory(selectedRoom);
                return selectedRoom;
            }
            System.out.println("❌ Invalid option number!");
            return null;
        } catch (NumberFormatException e) {
            // Not a number, continue with other matching
        }
        
        // Try to find by QR code
        Room room = graph.getRoomByQRCode(input);
        if (room != null) {
            addToScanHistory(room);
            return room;
        }
        
        // Try to find by room name (partial match)
        List<Room> matchingRooms = graph.searchRooms(input);
        if (matchingRooms.size() == 1) {
            Room selectedRoom = matchingRooms.get(0);
            addToScanHistory(selectedRoom);
            return selectedRoom;
        } else if (matchingRooms.size() > 1) {
            System.out.println("\n🔍 Multiple rooms found:");
            for (int i = 0; i < Math.min(matchingRooms.size(), 5); i++) {
                Room matchedRoom = matchingRooms.get(i);
                System.out.printf("%d. %s - %s%n", i + 1, matchedRoom.getName(), matchedRoom.getDescription());
            }
            
            System.out.print("Select room (1-" + Math.min(matchingRooms.size(), 5) + "): ");
            try {
                int selection = Integer.parseInt(scanner.nextLine().trim());
                if (selection >= 1 && selection <= Math.min(matchingRooms.size(), 5)) {
                    Room selectedRoom = matchingRooms.get(selection - 1);
                    addToScanHistory(selectedRoom);
                    return selectedRoom;
                }
            } catch (NumberFormatException ex) {
                // Invalid selection
            }
        }
        
        System.out.println("❌ Room not found! Please try again.");
        return null;
    }
    
    private Room realQRScan() {
        // This would interface with actual QR code scanner hardware/library
        System.out.println("📷 Point camera at QR code...");
        System.out.println("⏳ Scanning...");
        
        // Simulate scanning delay
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.print("Enter scanned QR code: ");
        String qrCode = scanner.nextLine().trim();
        
        Room room = graph.getRoomByQRCode(qrCode);
        if (room != null) {
            addToScanHistory(room);
            System.out.printf("✅ QR Code scanned successfully: %s%n", room.getName());
        } else {
            System.out.println("❌ QR Code not recognized!");
        }
        
        return room;
    }
    
    private String getStatusIcons(Room room) {
        StringBuilder icons = new StringBuilder();
        
        if (room.isBlocked()) {
            icons.append("🚫 ");
        }
        if (!room.isAccessible()) {
            icons.append("⚠️ ");
        }
        if (room.isEmergencyExit()) {
            icons.append("🚪 ");
        }
        if (room.getFeatures().containsKey("wheelchair_access") && 
            "yes".equals(room.getFeatures().get("wheelchair_access"))) {
            icons.append("♿ ");
        }
        if (room.getRoomType() == RoomType.ELEVATOR) {
            icons.append("🛗 ");
        }
        if (room.getRoomType() == RoomType.RESTROOM) {
            icons.append("🚻 ");
        }
        if (room.getRoomType() == RoomType.CAFETERIA) {
            icons.append("🍽️ ");
        }
        if (room.getRoomType() == RoomType.ICU) {
            icons.append("🏥 ");
        }
        
        return icons.toString().trim();
    }
    
    private void addToScanHistory(Room room) {
        String historyEntry = String.format("%s (%s) - %s", 
                                          room.getName(), 
                                          room.getQrCode(),
                                          new Date().toString().substring(11, 19)); // Time only
        scanHistory.add(0, historyEntry);
        
        // Keep only last 10 scans
        if (scanHistory.size() > 10) {
            scanHistory = scanHistory.subList(0, 10);
        }
    }
    
    public void generateQRCodes(String outputDirectory) {
        System.out.println("\n🏭 Generating QR Codes...");
        
        QRCodeGenerator.generateQRCodesForRooms(graph.getAllRooms(), outputDirectory);
        QRCodeGenerator.generateQRCodeLookupTable(graph.getAllRooms(), 
                                                 outputDirectory + "/qr_lookup.txt");
        QRCodeGenerator.generateInstallationGuide(outputDirectory + "/installation_guide.txt");
        
        System.out.printf("✅ QR codes generated for %d rooms in: %s%n", 
                         graph.getAllRooms().size(), outputDirectory);
    }
    
    public void showQRCodeStatistics() {
        System.out.println("\n📊 QR CODE STATISTICS");
        System.out.println("═══════════════════════════════════════");
        
        Map<RoomType, Long> roomTypeCount = graph.getAllRooms().stream()
            .collect(Collectors.groupingBy(Room::getRoomType, Collectors.counting()));
        
        System.out.printf("Total QR Codes: %d%n", graph.getAllRooms().size());
        System.out.printf("Accessible Rooms: %d%n", 
                         graph.getAllRooms().stream().mapToInt(r -> r.isAccessible() ? 1 : 0).sum());
        System.out.printf("Emergency Exits: %d%n", 
                         graph.getAllRooms().stream().mapToInt(r -> r.isEmergencyExit() ? 1 : 0).sum());
        
        System.out.println("\nRoom Types:");
        roomTypeCount.entrySet().stream()
            .sorted(Map.Entry.<RoomType, Long>comparingByValue().reversed())
            .forEach(entry -> System.out.printf("  %-20s: %d%n", 
                                               entry.getKey().getDisplayName(), 
                                               entry.getValue()));
        
        System.out.println("\nFloor Distribution:");
        Map<Integer, Long> floorCount = graph.getAllRooms().stream()
            .collect(Collectors.groupingBy(Room::getFloor, Collectors.counting()));
        floorCount.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> System.out.printf("  Floor %d: %d rooms%n", 
                                               entry.getKey(), 
                                               entry.getValue()));
        
        if (!scanHistory.isEmpty()) {
            System.out.printf("\nRecent Scans: %d%n", scanHistory.size());
        }
    }
    
    public void validateQRCodes() {
        System.out.println("\n🔍 Validating QR Codes...");
        
        List<String> issues = new ArrayList<>();
        Set<String> duplicateQRCodes = new HashSet<>();
        Map<String, Integer> qrCodeCount = new HashMap<>();
        
        // Check for duplicates and invalid codes
        for (Room room : graph.getAllRooms()) {
            String qrCode = room.getQrCode();
            
            if (qrCode == null || qrCode.trim().isEmpty()) {
                issues.add("Room " + room.getId() + " has empty QR code");
                continue;
            }
            
            qrCodeCount.put(qrCode, qrCodeCount.getOrDefault(qrCode, 0) + 1);
            if (qrCodeCount.get(qrCode) > 1) {
                duplicateQRCodes.add(qrCode);
            }
            
            // Check QR code format
            if (!qrCode.startsWith("QR_")) {
                issues.add("Room " + room.getId() + " has invalid QR code format: " + qrCode);
            }
        }
        
        // Report issues
        if (issues.isEmpty() && duplicateQRCodes.isEmpty()) {
            System.out.println("✅ All QR codes are valid!");
        } else {
            System.out.println("⚠️ Issues found:");
            
            for (String issue : issues) {
                System.out.println("  • " + issue);
            }
            
            for (String duplicateQR : duplicateQRCodes) {
                System.out.println("  • Duplicate QR code: " + duplicateQR);
            }
        }
    }
    
    public void updateQRDatabase() {
        initializeQRDatabase();
        System.out.println("✅ QR code database updated");
    }
    
    public void clearScanHistory() {
        scanHistory.clear();
        System.out.println("✅ Scan history cleared");
    }
    
    // Getters and setters
    public boolean isSimulationMode() { return isSimulationMode; }
    public void setSimulationMode(boolean simulationMode) { 
        this.isSimulationMode = simulationMode;
        System.out.println("QR Scanner mode: " + (simulationMode ? "Simulation" : "Real"));
    }
    
    public List<String> getScanHistory() { return new ArrayList<>(scanHistory); }
    public Map<String, String> getQRCodeDatabase() { return new HashMap<>(qrCodeDatabase); }
}