package com.indoor.navigation.utils;

import com.indoor.navigation.model.Room;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;

/**
 * QR Code Generator utility for creating printable QR codes for rooms
 */
public class QRCodeGenerator {
    
    /**
     * Generate QR code information for all rooms in ASCII art format
     */
    public static void generateQRCodesForRooms(Collection<Room> rooms, String outputDir) {
        try {
            // Create output directory if it doesn't exist
            Files.createDirectories(Paths.get(outputDir));
            
            // Generate master QR code list
            generateQRCodeList(rooms, outputDir);
            
            // Generate individual QR code files
            for (Room room : rooms) {
                generateIndividualQRCode(room, outputDir);
            }
            
            System.out.println("✅ QR codes generated in directory: " + outputDir);
            
        } catch (IOException e) {
            System.err.println("Error generating QR codes: " + e.getMessage());
        }
    }
    
    private static void generateQRCodeList(Collection<Room> rooms, String outputDir) throws IOException {
        String fileName = outputDir + "/QR_CODE_LIST.txt";
        
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("INDOOR NAVIGATION SYSTEM - QR CODE LIST\n");
            writer.write("Generated: " + new Date() + "\n");
            writer.write("=" + "=".repeat(50) + "\n\n");
            
            writer.write("INSTRUCTIONS FOR ADMINISTRATORS:\n");
            writer.write("1. Print this document and the individual QR code files\n");
            writer.write("2. Place each QR code at the entrance of the corresponding room\n");
            writer.write("3. Ensure QR codes are at a height accessible to all users\n");
            writer.write("4. Consider laminating QR codes for durability\n");
            writer.write("5. Test each QR code with the navigation system before deployment\n\n");
            
            writer.write("QR CODE MAPPING:\n");
            writer.write("-".repeat(80) + "\n");
            writer.write(String.format("%-15s %-25s %-20s %-15s%n", "QR CODE", "ROOM NAME", "ROOM TYPE", "FLOOR"));
            writer.write("-".repeat(80) + "\n");
            
            for (Room room : rooms) {
                writer.write(String.format("%-15s %-25s %-20s %-15s%n",
                    room.getQrCode(),
                    truncate(room.getName(), 24),
                    truncate(room.getRoomType().getDisplayName(), 19),
                    "Floor " + room.getFloor()));
            }
            
            writer.write("\n\nSCANNING INSTRUCTIONS FOR USERS:\n");
            writer.write("1. Open the Indoor Navigation System app\n");
            writer.write("2. Select 'Set Current Location' or 'Scan QR Code'\n");
            writer.write("3. Enter the QR code shown on the sign\n");
            writer.write("4. The system will confirm your location\n");
            writer.write("5. You can now navigate to your destination\n");
        }
    }
    
    private static void generateIndividualQRCode(Room room, String outputDir) throws IOException {
        String fileName = String.format("%s/QR_%s_%s.txt", 
                                      outputDir, 
                                      room.getQrCode(), 
                                      room.getName().replaceAll("[^a-zA-Z0-9]", "_"));
        
        try (FileWriter writer = new FileWriter(fileName)) {
            // ASCII Art QR Code simulation
            writer.write(generateASCIIQRCode(room.getQrCode()));
            writer.write("\n\n");
            
            // Room information
            writer.write("╔" + "═".repeat(50) + "╗\n");
            writer.write("║" + centerText("INDOOR NAVIGATION SYSTEM", 50) + "║\n");
            writer.write("╠" + "═".repeat(50) + "╣\n");
            writer.write("║" + centerText("ROOM INFORMATION", 50) + "║\n");
            writer.write("╠" + "═".repeat(50) + "╣\n");
            writer.write("║ QR Code: " + padRight(room.getQrCode(), 37) + "║\n");
            writer.write("║ Room Name: " + padRight(room.getName(), 36) + "║\n");
            writer.write("║ Room Type: " + padRight(room.getRoomType().getDisplayName(), 36) + "║\n");
            writer.write("║ Floor: " + padRight(String.valueOf(room.getFloor()), 41) + "║\n");
            writer.write("║ Description: " + padRight(truncate(room.getDescription(), 33), 33) + "║\n");
            
            if (!room.getFeatures().isEmpty()) {
                writer.write("╠" + "═".repeat(50) + "╣\n");
                writer.write("║" + centerText("ACCESSIBILITY FEATURES", 50) + "║\n");
                writer.write("╠" + "═".repeat(50) + "╣\n");
                
                for (String feature : room.getFeatures().keySet()) {
                    String featureText = feature.replace("_", " ").toUpperCase() + ": " + 
                                       room.getFeatures().get(feature).toUpperCase();
                    writer.write("║ " + padRight(truncate(featureText, 47), 47) + "║\n");
                }
            }
            
            if (!room.getLandmarks().isEmpty()) {
                writer.write("╠" + "═".repeat(50) + "╣\n");
                writer.write("║" + centerText("NEARBY LANDMARKS", 50) + "║\n");
                writer.write("╠" + "═".repeat(50) + "╣\n");
                
                for (String landmark : room.getLandmarks()) {
                    writer.write("║ • " + padRight(truncate(landmark, 45), 45) + "║\n");
                }
            }
            
            writer.write("╠" + "═".repeat(50) + "╣\n");
            writer.write("║" + centerText("USAGE INSTRUCTIONS", 50) + "║\n");
            writer.write("╠" + "═".repeat(50) + "╣\n");
            writer.write("║ 1. Open Indoor Navigation System          ║\n");
            writer.write("║ 2. Select 'Set Current Location'          ║\n");
            writer.write("║ 3. Enter QR Code: " + padRight(room.getQrCode(), 25) + "║\n");
            writer.write("║ 4. Follow audio navigation instructions    ║\n");
            writer.write("╚" + "═".repeat(50) + "╝\n");
        }
    }
    
    private static String generateASCIIQRCode(String qrCode) {
        // Simple ASCII art representation of QR code
        StringBuilder qr = new StringBuilder();
        
        // Top border
        qr.append("┌" + "─".repeat(32) + "┐\n");
        
        // QR pattern simulation based on QR code
        int hash = qrCode.hashCode();
        for (int row = 0; row < 16; row++) {
            qr.append("│");
            for (int col = 0; col < 16; col++) {
                // Generate pseudo-random pattern based on hash and position
                int pattern = (hash + row * 16 + col) % 4;
                if (pattern == 0 || pattern == 1) {
                    qr.append("██");
                } else {
                    qr.append("  ");
                }
            }
            qr.append("│\n");
        }
        
        // Bottom border
        qr.append("└" + "─".repeat(32) + "┘\n");
        
        // QR code text
        qr.append(centerText("QR CODE: " + qrCode, 34)).append("\n");
        qr.append(centerText("Scan with navigation app", 34)).append("\n");
        
        return qr.toString();
    }
    
    /**
     * Generate a simple QR code lookup table for easy reference
     */
    public static void generateQRCodeLookupTable(Collection<Room> rooms, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("# QR Code Lookup Table\n");
            writer.write("# Generated: " + new Date() + "\n");
            writer.write("# Format: QR_CODE|ROOM_ID|ROOM_NAME|FLOOR\n\n");
            
            for (Room room : rooms) {
                writer.write(String.format("%s|%s|%s|%d%n",
                    room.getQrCode(),
                    room.getId(),
                    room.getName(),
                    room.getFloor()));
            }
            
            System.out.println("✅ QR code lookup table generated: " + fileName);
            
        } catch (IOException e) {
            System.err.println("Error generating QR code lookup table: " + e.getMessage());
        }
    }
    
    /**
     * Generate installation instructions for administrators
     */
    public static void generateInstallationGuide(String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("INDOOR NAVIGATION SYSTEM - QR CODE INSTALLATION GUIDE\n");
            writer.write("=" + "=".repeat(60) + "\n\n");
            
            writer.write("PREPARATION:\n");
            writer.write("1. Print all QR code files on durable paper or cardstock\n");
            writer.write("2. Consider laminating QR codes for weather/wear protection\n");
            writer.write("3. Prepare mounting hardware (tape, screws, or adhesive)\n");
            writer.write("4. Test the navigation system on a mobile device\n\n");
            
            writer.write("INSTALLATION REQUIREMENTS:\n");
            writer.write("• Height: 3-4 feet from ground level for wheelchair accessibility\n");
            writer.write("• Lighting: Ensure adequate lighting for code visibility\n");
            writer.write("• Position: Mount on wall adjacent to room entrance\n");
            writer.write("• Orientation: Keep QR codes level and straight\n");
            writer.write("• Protection: Consider protective covering in high-traffic areas\n\n");
            
            writer.write("INSTALLATION STEPS:\n");
            writer.write("1. Identify the correct QR code for each room\n");
            writer.write("2. Clean the mounting surface\n");
            writer.write("3. Position QR code at appropriate height\n");
            writer.write("4. Secure with chosen mounting method\n");
            writer.write("5. Test QR code scanning with navigation app\n");
            writer.write("6. Document installation location and date\n\n");
            
            writer.write("MAINTENANCE:\n");
            writer.write("• Check QR codes monthly for damage or wear\n");
            writer.write("• Clean codes regularly to maintain scan quality\n");
            writer.write("• Replace damaged codes immediately\n");
            writer.write("• Update navigation system if room changes occur\n\n");
            
            writer.write("TROUBLESHOOTING:\n");
            writer.write("• If code won't scan: Check for damage, dirt, or poor lighting\n");
            writer.write("• If wrong location detected: Verify correct QR code placement\n");
            writer.write("• If app errors occur: Check navigation system configuration\n");
            writer.write("• For technical issues: Contact system administrator\n\n");
            
            System.out.println("✅ Installation guide generated: " + fileName);
            
        } catch (IOException e) {
            System.err.println("Error generating installation guide: " + e.getMessage());
        }
    }
    
    // Helper methods
    private static String centerText(String text, int width) {
        if (text.length() >= width) return text.substring(0, width);
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text + " ".repeat(width - text.length() - padding);
    }
    
    private static String padRight(String text, int width) {
        if (text.length() >= width) return text.substring(0, width);
        return text + " ".repeat(width - text.length());
    }
    
    private static String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}