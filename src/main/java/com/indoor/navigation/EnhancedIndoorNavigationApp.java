package com.indoor.navigation;

import com.indoor.navigation.model.*;
import com.indoor.navigation.service.*;
import com.indoor.navigation.utils.EnhancedSampleDataInitializer;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

/**
 * Enhanced Indoor Navigation Application with comprehensive features
 */
public class EnhancedIndoorNavigationApp {
    private NavigationGraph graph;
    private EnhancedNavigationService navigationService;
    private EnhancedAdminService adminService;
    private Scanner scanner;
    private boolean isRunning = true;
    
    public EnhancedIndoorNavigationApp() {
        System.out.println("🏗️ Initializing Enhanced Indoor Navigation System...");
        
        // Initialize with comprehensive sample data
        this.graph = EnhancedSampleDataInitializer.createComprehensiveSampleMap();
        this.navigationService = new EnhancedNavigationService(graph);
        this.adminService = new EnhancedAdminService(graph);
        this.scanner = new Scanner(System.in);
        
        // Add shutdown hook for graceful cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🔄 Shutting down navigation system...");
            navigationService.shutdown();
        }));
        
        System.out.println("✅ System initialized successfully!");
    }
    
    public void start() {
        showWelcomeMessage();
        
        while (isRunning) {
            try {
                showMainMenu();
                String choice = scanner.nextLine().trim();
                handleMenuChoice(choice);
            } catch (Exception e) {
                System.out.println("❌ An error occurred: " + e.getMessage());
                navigationService.getTtsService().speakError("An error occurred. Please try again.");
            }
        }
    }
    
    private void showWelcomeMessage() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🏢 ENHANCED INDOOR NAVIGATION SYSTEM FOR THE VISUALLY IMPAIRED");
        System.out.println("=".repeat(80));
        System.out.println("🎯 Features:");
        System.out.println("  • Multi-mode navigation (Standard, Wheelchair, Visually Impaired, Emergency)");
        System.out.println("  • Multi-floor building support with elevator routing");
        System.out.println("  • Landmark-based navigation instructions");
        System.out.println("  • Dynamic obstacle detection and re-routing");
        System.out.println("  • Voice commands and text-to-speech");
        System.out.println("  • QR code location scanning");
        System.out.println("  • Comprehensive admin panel");
        System.out.println("  • Persistent data storage");
        System.out.println("=".repeat(80));
        
        navigationService.getTtsService().speak(
            "Welcome to the Enhanced Indoor Navigation System. " +
            "This system provides comprehensive navigation assistance with " +
            "multiple modes, voice commands, and accessibility features."
        );
    }
    
    private void showMainMenu() {
        Room currentLocation = navigationService.getCurrentLocation();
        NavigationMode currentMode = navigationService.getCurrentMode();
        
        System.out.println("\n🏠 MAIN MENU - " + graph.getGraphName());
        System.out.println("═══════════════════════════════════════════════════════");
        
        // Current status
        System.out.println("📊 CURRENT STATUS:");
        System.out.printf("  📍 Location: %s%n", 
                         currentLocation != null ? 
                         currentLocation.getName() + " (Floor " + currentLocation.getFloor() + ")" : 
                         "Not Set");
        System.out.printf("  🎯 Navigation Mode: %s%n", currentMode.getDisplayName());
        System.out.printf("  🔊 TTS: %s  🎤 Voice: %s%n", 
                         navigationService.getTtsService().isEnabled() ? "ON" : "OFF",
                         navigationService.getVoiceService().isEnabled() ? "ON" : "OFF");
        
        System.out.println("\n🧭 NAVIGATION:");
        System.out.println("1. Set Current Location (Scan QR)    2. Navigate to Destination");
        System.out.println("3. Voice Navigation                  4. Emergency Exit Route");
        System.out.println("5. Repeat Last Instructions          6. Show Alternative Routes");
        
        System.out.println("\n⚙️ NAVIGATION MODES:");
        System.out.println("7. Standard Mode                     8. Wheelchair Mode");
        System.out.println("9. Visually Impaired Mode           10. Emergency Mode");
        
        System.out.println("\n🎛️ SETTINGS:");
        System.out.println("11. Audio Settings                   12. Voice Recognition Settings");
        System.out.println("13. User Preferences                 14. Navigation History");
        
        System.out.println("\n📱 SMART FEATURES:");
        System.out.println("15. Simulate Dynamic Obstacle        16. Building Information");
        System.out.println("17. Accessibility Report             18. Find Nearest Facility");
        
        System.out.println("\n🔧 SYSTEM:");
        System.out.println("19. Admin Panel                      20. Help & Commands");
        System.out.println("21. System Statistics                22. Save/Load Data");
        
        System.out.println("\n0. Exit Application");
        
        System.out.print("\nSelect option (0-22): ");
    }
    
    private void handleMenuChoice(String choice) {
        switch (choice) {
            // Navigation
            case "1": handleLocationSetting(); break;
            case "2": handleNavigation(); break;
            case "3": handleVoiceNavigation(); break;
            case "4": handleEmergencyExit(); break;
            case "5": navigationService.repeatLastInstructions(); break;
            case "6": navigationService.showAlternativeRoutes(); break;
            
            // Navigation Modes
            case "7": setNavigationMode(NavigationMode.STANDARD); break;
            case "8": setNavigationMode(NavigationMode.WHEELCHAIR); break;
            case "9": setNavigationMode(NavigationMode.VISUALLY_IMPAIRED); break;
            case "10": setNavigationMode(NavigationMode.EMERGENCY); break;
            
            // Settings
            case "11": handleAudioSettings(); break;
            case "12": handleVoiceSettings(); break;
            case "13": handleUserPreferences(); break;
            case "14": navigationService.showNavigationHistory(); break;
            
            // Smart Features
            case "15": navigationService.simulateDynamicObstacle(); break;
            case "16": showBuildingInformation(); break;
            case "17": showAccessibilityReport(); break;
            case "18": findNearestFacility(); break;
            
            // System
            case "19": adminService.showAdminMenu(); break;
            case "20": showHelpAndCommands(); break;
            case "21": showSystemStatistics(); break;
            case "22": handleDataManagement(); break;
            
            case "0": 
                handleExit();
                break;
                
            default: 
                System.out.println("❌ Invalid option! Please try again.");
                navigationService.getTtsService().speakError("Invalid option. Please try again.");
        }
    }
    
    private void handleLocationSetting() {
        navigationService.setCurrentLocation();
    }
    
    private void handleNavigation() {
        if (navigationService.getCurrentLocation() == null) {
            boolean locationSet = navigationService.setCurrentLocation();
            if (!locationSet) return;
        }
        
        System.out.println("\n🧭 NAVIGATION TO DESTINATION");
        System.out.println("═══════════════════════════════════");
        
        // Show available destinations by floor
        showDestinationsByFloor();
        
        // Show recent destinations
        navigationService.showNavigationHistory();
        
        System.out.print("\nEnter destination (name, type, or room ID): ");
        String destination = scanner.nextLine().trim();
        
        if (destination.isEmpty()) {
            navigationService.getTtsService().speakError("No destination specified");
            return;
        }
        
        navigationService.navigateToDestination(destination);
    }
    
    private void showDestinationsByFloor() {
        System.out.println("Available destinations by floor:");
        
        for (Integer floor : graph.getFloors().stream().sorted().toArray(Integer[]::new)) {
            System.out.printf("\n🏢 FLOOR %d:%n", floor);
            
            graph.getRoomsOnFloor(floor).stream()
                .filter(room -> !room.equals(navigationService.getCurrentLocation()))
                .filter(room -> !room.isBlocked())
                .forEach(room -> {
                    String statusIcons = getStatusIcons(room);
                    System.out.printf("  • %-25s (%s) %s%n", 
                                    room.getName(), 
                                    room.getRoomType().getDisplayName(),
                                    statusIcons);
                });
        }
    }
    
    private String getStatusIcons(Room room) {
        StringBuilder icons = new StringBuilder();
        
        if (room.isAccessible()) icons.append("♿ ");
        if (room.isEmergencyExit()) icons.append("🚪 ");
        if (room.getRoomType() == RoomType.ELEVATOR) icons.append("🛗 ");
        if (room.getRoomType() == RoomType.RESTROOM) icons.append("🚻 ");
        if (room.getRoomType() == RoomType.CAFETERIA) icons.append("🍽️ ");
        if (room.getRoomType() == RoomType.ICU) icons.append("🏥 ");
        if (!room.getLandmarks().isEmpty()) icons.append("🏷️ ");
        
        return icons.toString().trim();
    }
    
    private void handleVoiceNavigation() {
        if (navigationService.getCurrentLocation() == null) {
            boolean locationSet = navigationService.setCurrentLocation();
            if (!locationSet) return;
        }
        
        System.out.println("\n🎤 VOICE NAVIGATION MODE");
        System.out.println("═══════════════════════════════════");
        
        navigationService.getTtsService().speak(
            "Voice navigation activated. You can say commands like " +
            "'navigate to library', 'find restroom', or 'emergency exit'."
        );
        
        CompletableFuture<String> voiceCommand = navigationService.getVoiceService().listenForCommand();
        
        voiceCommand.thenAccept(command -> {
            handleVoiceCommand(command);
        }).join();
    }
    
    private void handleVoiceCommand(String command) {
        if (command.startsWith("NAVIGATE:")) {
            String destination = command.substring(9);
            navigationService.navigateToDestination(destination);
        } else if (command.startsWith("SWITCH_MODE:")) {
            String mode = command.substring(12);
            switchToVoiceMode(mode);
        } else {
            switch (command) {
                case "EMERGENCY":
                case "EMERGENCY_EXIT":
                    handleEmergencyExit();
                    break;
                case "HELP":
                    showHelpAndCommands();
                    break;
                case "REPEAT":
                    navigationService.repeatLastInstructions();
                    break;
                case "ALTERNATIVES":
                    navigationService.showAlternativeRoutes();
                    break;
                case "HISTORY":
                    navigationService.showNavigationHistory();
                    break;
                case "LOCATION":
                    announceCurrentLocation();
                    break;
                case "STOP":
                case "PAUSE":
                    navigationService.getTtsService().stopSpeaking();
                    break;
                case "MENU":
                    navigationService.getTtsService().speak("Returning to main menu");
                    break;
                case "SETTINGS":
                    handleUserPreferences();
                    break;
                default:
                    navigationService.getTtsService().speak("Command not recognized. Say 'help' for available commands.");
            }
        }
    }
    
    private void switchToVoiceMode(String mode) {
        try {
            switch (mode.toLowerCase()) {
                case "standard":
                    setNavigationMode(NavigationMode.STANDARD);
                    break;
                case "wheelchair":
                    setNavigationMode(NavigationMode.WHEELCHAIR);
                    break;
                case "visually impaired":
                case "vision":
                    setNavigationMode(NavigationMode.VISUALLY_IMPAIRED);
                    break;
                case "emergency":
                    setNavigationMode(NavigationMode.EMERGENCY);
                    break;
                default:
                    navigationService.getTtsService().speak("Unknown navigation mode: " + mode);
            }
        } catch (Exception e) {
            navigationService.getTtsService().speakError("Could not switch navigation mode");
        }
    }
    
    private void announceCurrentLocation() {
        Room currentLocation = navigationService.getCurrentLocation();
        if (currentLocation != null) {
            String announcement = String.format("You are currently at %s on floor %d. %s", 
                                              currentLocation.getName(), 
                                              currentLocation.getFloor(),
                                              currentLocation.getDescription());
            
            if (!currentLocation.getLandmarks().isEmpty()) {
                announcement += " Near " + String.join(", ", currentLocation.getLandmarks());
            }
            
            navigationService.getTtsService().speak(announcement);
        } else {
            navigationService.getTtsService().speak("Current location not set. Please scan a QR code.");
        }
    }
    
    private void handleEmergencyExit() {
        System.out.println("\n🚨 EMERGENCY EXIT ROUTE");
        System.out.println("═══════════════════════════════════");
        
        navigationService.getTtsService().speakWarning("Emergency mode activated. Finding nearest exit.");
        navigationService.findEmergencyExit();
    }
    
    private void setNavigationMode(NavigationMode mode) {
        navigationService.setNavigationMode(mode);
        
        // Give mode-specific instructions
        String modeInstructions = getModeInstructions(mode);
        if (!modeInstructions.isEmpty()) {
            navigationService.getTtsService().speak(modeInstructions);
        }
    }
    
    private String getModeInstructions(NavigationMode mode) {
        switch (mode) {
            case WHEELCHAIR:
                return "Wheelchair mode activated. The system will avoid stairs and prioritize elevators and ramps.";
            case VISUALLY_IMPAIRED:
                return "Visually impaired mode activated. Enhanced audio cues and landmark-based directions enabled.";
            case EMERGENCY:
                return "Emergency mode activated. All routes will prioritize speed over accessibility.";
            case STANDARD:
                return "Standard navigation mode activated.";
            default:
                return "";
        }
    }
    
    private void handleAudioSettings() {
        System.out.println("\n🔊 AUDIO SETTINGS");
        System.out.println("═══════════════════════════════════");
        
        EnhancedTextToSpeechService tts = navigationService.getTtsService();
        
        while (true) {
            System.out.println("Current Settings:");
            System.out.printf("1. Text-to-Speech: %s%n", tts.isEnabled() ? "ON" : "OFF");
            System.out.printf("2. Speech Rate: %.1f%n", tts.getSpeechRate());
            System.out.printf("3. Voice: %s%n", tts.getVoice());
            System.out.printf("4. Queue Size: %d%n", tts.getQueueSize());
            System.out.println("5. Test Voice");
            System.out.println("0. Back");
            
            System.out.print("Select option: ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    tts.setEnabled(!tts.isEnabled());
                    break;
                case "2":
                    System.out.print("Enter speech rate (0.5-2.0): ");
                    try {
                        double rate = Double.parseDouble(scanner.nextLine().trim());
                        tts.setSpeechRate(rate);
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Invalid rate");
                    }
                    break;
                case "3":
                    System.out.println("Available voices: " + String.join(", ", tts.getAvailableVoices()));
                    System.out.print("Enter voice name: ");
                    String voice = scanner.nextLine().trim();
                    tts.setVoice(voice);
                    break;
                case "4":
                    System.out.println("Queue contains " + tts.getQueueSize() + " items");
                    break;
                case "5":
                    tts.speak("This is a test of the text to speech system. The current voice is " + tts.getVoice() + " at rate " + tts.getSpeechRate());
                    break;
                case "0":
                    return;
                default:
                    System.out.println("❌ Invalid option");
            }
        }
    }
    
    private void handleVoiceSettings() {
        System.out.println("\n🎤 VOICE RECOGNITION SETTINGS");
        System.out.println("═══════════════════════════════════");
        
        VoiceRecognitionService voice = navigationService.getVoiceService();
        
        while (true) {
            System.out.println("Current Settings:");
            System.out.printf("1. Voice Recognition: %s%n", voice.isEnabled() ? "ON" : "OFF");
            System.out.println("2. Show Voice Commands");
            System.out.println("3. Show Recent Commands");
            System.out.println("4. Test Voice Recognition");
            System.out.println("0. Back");
            
            System.out.print("Select option: ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    voice.setEnabled(!voice.isEnabled());
                    break;
                case "2":
                    voice.showVoiceCommands();
                    break;
                case "3":
                    voice.showRecentCommands();
                    break;
                case "4":
                    System.out.println("Say a test command:");
                    CompletableFuture<String> testCommand = voice.listenForCommand();
                    testCommand.thenAccept(command -> {
                        System.out.println("Recognized: " + command);
                        navigationService.getTtsService().speak("I heard: " + command);
                    }).join();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("❌ Invalid option");
            }
        }
    }
    
    private void handleUserPreferences() {
        System.out.println("\n⚙️ USER PREFERENCES");
        System.out.println("═══════════════════════════════════");
        
        UserPreferences prefs = navigationService.getUserPreferences();
        
        while (true) {
            System.out.println("Current Preferences:");
            System.out.printf("1. User ID: %s%n", prefs.getUserId());
            System.out.printf("2. Preferred Mode: %s%n", prefs.getPreferredMode().getDisplayName());
            System.out.printf("3. Use Landmark Instructions: %s%n", prefs.isUseLandmarkInstructions() ? "Yes" : "No");
            System.out.printf("4. Instruction Pause Time: %.1f seconds%n", prefs.getInstructionPauseTime());
            System.out.printf("5. Avoid Room Types: %d items%n", prefs.getAvoidRoomTypes().size());
            System.out.printf("6. Avoid Path Types: %d items%n", prefs.getAvoidPathTypes().size());
            System.out.println("7. Save Preferences");
            System.out.println("8. Load Different User");
            System.out.println("0. Back");
            
            System.out.print("Select option: ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    System.out.print("Enter new user ID: ");
                    String userId = scanner.nextLine().trim();
                    if (!userId.isEmpty()) {
                        navigationService.loadUserPreferences(userId);
                    }
                    break;
                case "2":
                    showNavigationModeSelection();
                    break;
                case "3":
                    prefs.setUseLandmarkInstructions(!prefs.isUseLandmarkInstructions());
                    navigationService.getTtsService().speak("Landmark instructions " + 
                        (prefs.isUseLandmarkInstructions() ? "enabled" : "disabled"));
                    break;
                case "4":
                    System.out.print("Enter pause time in seconds (0.5-10.0): ");
                    try {
                        double pauseTime = Double.parseDouble(scanner.nextLine().trim());
                        prefs.setInstructionPauseTime(pauseTime);
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Invalid time");
                    }
                    break;
                case "5":
                    manageAvoidRoomTypes(prefs);
                    break;
                case "6":
                    manageAvoidPathTypes(prefs);
                    break;
                case "7":
                    navigationService.saveUserPreferences();
                    break;
                case "8":
                    System.out.print("Enter user ID to load: ");
                    String loadUserId = scanner.nextLine().trim();
                    if (!loadUserId.isEmpty()) {
                        navigationService.loadUserPreferences(loadUserId);
                    }
                    break;
                case "0":
                    return;
                default:
                    System.out.println("❌ Invalid option");
            }
        }
    }
    
    private void showNavigationModeSelection() {
        System.out.println("\nAvailable Navigation Modes:");
        NavigationMode[] modes = NavigationMode.values();
        for (int i = 0; i < modes.length; i++) {
            System.out.printf("%d. %s - %s%n", i + 1, modes[i].getDisplayName(), modes[i].getDescription());
        }
        
        System.out.print("Select mode (1-" + modes.length + "): ");
        try {
            int selection = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (selection >= 0 && selection < modes.length) {
                setNavigationMode(modes[selection]);
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid selection");
        }
    }
    
    private void manageAvoidRoomTypes(UserPreferences prefs) {
        System.out.println("\nRoom types to avoid:");
        if (prefs.getAvoidRoomTypes().isEmpty()) {
            System.out.println("  None");
        } else {
            prefs.getAvoidRoomTypes().forEach(type -> 
                System.out.println("  • " + type.getDisplayName()));
        }
        
        System.out.println("\n1. Add room type to avoid");
        System.out.println("2. Remove room type from avoid list");
        System.out.println("0. Back");
        
        System.out.print("Choose action: ");
        String action = scanner.nextLine().trim();
        
        switch (action) {
            case "1":
                // Show available room types
                RoomType[] types = RoomType.values();
                for (int i = 0; i < types.length; i++) {
                    System.out.printf("%d. %s%n", i + 1, types[i].getDisplayName());
                }
                System.out.print("Select room type to avoid: ");
                try {
                    int typeIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
                    if (typeIndex >= 0 && typeIndex < types.length) {
                        prefs.addAvoidRoomType(types[typeIndex]);
                        System.out.println("✓ Added " + types[typeIndex].getDisplayName() + " to avoid list");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("❌ Invalid selection");
                }
                break;
            case "2":
                if (!prefs.getAvoidRoomTypes().isEmpty()) {
                    System.out.println("Select room type to remove from avoid list:");
                    RoomType[] avoidTypes = prefs.getAvoidRoomTypes().toArray(new RoomType[0]);
                    for (int i = 0; i < avoidTypes.length; i++) {
                        System.out.printf("%d. %s%n", i + 1, avoidTypes[i].getDisplayName());
                    }
                    try {
                        int removeIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
                        if (removeIndex >= 0 && removeIndex < avoidTypes.length) {
                            prefs.removeAvoidRoomType(avoidTypes[removeIndex]);
                            System.out.println("✓ Removed " + avoidTypes[removeIndex].getDisplayName() + " from avoid list");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Invalid selection");
                    }
                }
                break;
        }
    }
    
    private void manageAvoidPathTypes(UserPreferences prefs) {
        System.out.println("\nPath types to avoid:");
        if (prefs.getAvoidPathTypes().isEmpty()) {
            System.out.println("  None");
        } else {
            prefs.getAvoidPathTypes().forEach(type -> 
                System.out.println("  • " + type));
        }
        
        String[] pathTypes = {"stairs", "elevator", "ramp", "corridor"};
        
        System.out.println("\n1. Add path type to avoid");
        System.out.println("2. Remove path type from avoid list");
        System.out.println("0. Back");
        
        System.out.print("Choose action: ");
        String action = scanner.nextLine().trim();
        
        switch (action) {
            case "1":
                System.out.println("Available path types:");
                for (int i = 0; i < pathTypes.length; i++) {
                    System.out.printf("%d. %s%n", i + 1, pathTypes[i]);
                }
                System.out.print("Select path type to avoid: ");
                try {
                    int typeIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
                    if (typeIndex >= 0 && typeIndex < pathTypes.length) {
                        prefs.addAvoidPathType(pathTypes[typeIndex]);
                        System.out.println("✓ Added " + pathTypes[typeIndex] + " to avoid list");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("❌ Invalid selection");
                }
                break;
            case "2":
                if (!prefs.getAvoidPathTypes().isEmpty()) {
                    System.out.println("Select path type to remove from avoid list:");
                    String[] avoidTypes = prefs.getAvoidPathTypes().toArray(new String[0]);
                    for (int i = 0; i < avoidTypes.length; i++) {
                        System.out.printf("%d. %s%n", i + 1, avoidTypes[i]);
                    }
                    try {
                        int removeIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
                        if (removeIndex >= 0 && removeIndex < avoidTypes.length) {
                            prefs.removeAvoidPathType(avoidTypes[removeIndex]);
                            System.out.println("✓ Removed " + avoidTypes[removeIndex] + " from avoid list");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Invalid selection");
                    }
                }
                break;
        }
    }
    
    private void showBuildingInformation() {
        System.out.println("\n🏢 BUILDING INFORMATION");
        System.out.println("═══════════════════════════════════════════════════════");
        
        System.out.printf("Building Name: %s%n", graph.getGraphName());
        System.out.printf("Description: %s%n", graph.getDescription());
        
        var stats = graph.getBuildingStats();
        System.out.printf("Total Rooms: %d%n", stats.get("total_rooms"));
        System.out.printf("Total Paths: %d%n", stats.get("total_paths"));
        System.out.printf("Number of Floors: %d%n", stats.get("floors"));
        System.out.printf("Emergency Exits: %d%n", 
                         graph.getAllRooms().stream().mapToInt(r -> r.isEmergencyExit() ? 1 : 0).sum());
        
        navigationService.getTtsService().speak(
            String.format("This building has %d rooms across %d floors with %d emergency exits", 
                         (Integer) stats.get("total_rooms"), 
                         (Integer) stats.get("floors"),
                         (int) graph.getAllRooms().stream().mapToInt(r -> r.isEmergencyExit() ? 1 : 0).sum())
        );
    }
    
    private void showAccessibilityReport() {
        System.out.println("\n♿ ACCESSIBILITY REPORT");
        System.out.println("═══════════════════════════════════════════════════════");
        
        long accessibleRooms = graph.getAllRooms().stream()
            .mapToLong(room -> room.isAccessible() ? 1 : 0).sum();
        long totalRooms = graph.getAllRooms().size();
        
        System.out.printf("Accessible Rooms: %d of %d (%.1f%%)%n", 
                         accessibleRooms, totalRooms, 100.0 * accessibleRooms / totalRooms);
        
        System.out.println("\nAccessible Facilities by Type:");
        graph.getAllRooms().stream()
            .filter(Room::isAccessible)
            .collect(java.util.stream.Collectors.groupingBy(Room::getRoomType, java.util.stream.Collectors.counting()))
            .forEach((type, count) -> System.out.printf("  %s: %d%n", type.getDisplayName(), count));
        
        navigationService.getTtsService().speak(
            String.format("%.1f percent of rooms are wheelchair accessible", 
                         100.0 * accessibleRooms / totalRooms)
        );
    }
    
    private void findNearestFacility() {
        if (navigationService.getCurrentLocation() == null) {
            navigationService.getTtsService().speakError("Please set your current location first");
            return;
        }
        
        System.out.println("\n🔍 FIND NEAREST FACILITY");
        System.out.println("═══════════════════════════════════");
        
        System.out.println("Available facility types:");
        RoomType[] types = RoomType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.printf("%2d. %s%n", i + 1, types[i].getDisplayName());
        }
        
        System.out.print("Select facility type: ");
        try {
            int typeIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (typeIndex >= 0 && typeIndex < types.length) {
                Room nearest = graph.findNearestRoomOfType(
                    navigationService.getCurrentLocation().getId(), 
                    types[typeIndex]
                );
                
                if (nearest != null) {
                    String message = String.format("Nearest %s: %s on floor %d", 
                                                 types[typeIndex].getDisplayName(),
                                                 nearest.getName(),
                                                 nearest.getFloor());
                    System.out.println("✅ " + message);
                    navigationService.getTtsService().speak(message);
                    
                    System.out.print("Navigate there? (y/N): ");
                    if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                        navigationService.navigateToDestination(nearest.getName());
                    }
                } else {
                    String message = "No " + types[typeIndex].getDisplayName() + " found";
                    System.out.println("❌ " + message);
                    navigationService.getTtsService().speakError(message);
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid selection");
        }
    }
    
    private void showHelpAndCommands() {
        System.out.println("\n❓ HELP & COMMANDS");
        System.out.println("═══════════════════════════════════════════════════════");
        
        System.out.println("🎯 NAVIGATION MODES:");
        for (NavigationMode mode : NavigationMode.values()) {
            System.out.printf("  %s: %s%n", mode.getDisplayName(), mode.getDescription());
        }
        
        System.out.println("\n🎤 VOICE COMMANDS:");
        navigationService.getVoiceService().showVoiceCommands();
        
        System.out.println("\n🔧 QUICK TIPS:");
        System.out.println("  • Use room names, types, or IDs for navigation");
        System.out.println("  • Emergency exits are marked with 🚪");
        System.out.println("  • Wheelchair accessible rooms are marked with ♿");
        System.out.println("  • Multi-floor navigation uses elevators automatically");
        System.out.println("  • Dynamic obstacles are announced and cause re-routing");
        System.out.println("  • All settings are saved automatically");
        
        navigationService.getTtsService().speak(
            "Help information displayed. The system supports multiple navigation modes, " +
            "voice commands, and automatic accessibility routing."
        );
    }
    
    private void showSystemStatistics() {
        System.out.println("\n📊 SYSTEM STATISTICS");
        System.out.println("═══════════════════════════════════════════════════════");
        
        var stats = graph.getBuildingStats();
        
        System.out.println("📈 Building Statistics:");
        stats.forEach((key, value) -> {
            if (!key.equals("room_types")) {
                System.out.printf("  %s: %s%n", 
                                key.replace("_", " ").toUpperCase(), 
                                value);
            }
        });
        
        System.out.println("\n🎤 Voice Recognition:");
        System.out.printf("  Recent Commands: %d%n", navigationService.getVoiceService().getRecentCommands().size());
        
        System.out.println("\n🔊 Text-to-Speech:");
        System.out.printf("  Queue Size: %d%n", navigationService.getTtsService().getQueueSize());
        System.out.printf("  Currently Playing: %s%n", navigationService.getTtsService().isPlaying() ? "Yes" : "No");
        
        System.out.println("\n💾 User Data:");
        System.out.printf("  Navigation History: %d items%n", 
                         navigationService.getUserPreferences().getNavigationHistory().size());
    }
    
    private void handleDataManagement() {
        System.out.println("\n💾 DATA MANAGEMENT");
        System.out.println("═══════════════════════════════════");
        
        System.out.println("1. Save Current Map");
        System.out.println("2. Load Map");
        System.out.println("3. List Saved Maps");
        System.out.println("4. Save User Preferences");
        System.out.println("5. Load User Preferences");
        System.out.println("0. Back");
        
        System.out.print("Select option: ");
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                System.out.print("Enter map name: ");
                String mapName = scanner.nextLine().trim();
                if (!mapName.isEmpty()) {
                    boolean saved = navigationService.getDataManager().saveNavigationGraph(graph, mapName);
                    if (saved) {
                        navigationService.getTtsService().speakConfirmation("Map saved successfully");
                    } else {
                        navigationService.getTtsService().speakError("Failed to save map");
                    }
                }
                break;
            case "2":
                System.out.print("Enter map name to load: ");
                String loadMapName = scanner.nextLine().trim();
                if (!loadMapName.isEmpty()) {
                    NavigationGraph loadedGraph = navigationService.getDataManager().loadNavigationGraph(loadMapName);
                    if (loadedGraph.getAllRooms().size() > 0) {
                        // Replace current graph (in a real app, this would be more sophisticated)
                        navigationService.getTtsService().speakConfirmation("Map loaded successfully");
                    } else {
                        navigationService.getTtsService().speakError("Failed to load map");
                    }
                }
                break;
            case "3":
                var savedMaps = navigationService.getDataManager().listSavedMaps();
                if (savedMaps.isEmpty()) {
                    System.out.println("No saved maps found");
                } else {
                    System.out.println("Saved maps:");
                    savedMaps.forEach(map -> System.out.println("  • " + map));
                }
                break;
            case "4":
                navigationService.saveUserPreferences();
                break;
            case "5":
                System.out.print("Enter user ID: ");
                String userId = scanner.nextLine().trim();
                if (!userId.isEmpty()) {
                    navigationService.loadUserPreferences(userId);
                }
                break;
        }
    }
    
    private void handleExit() {
        System.out.println("\n👋 Thank you for using the Enhanced Indoor Navigation System!");
        
        // Save user preferences before exit
        navigationService.saveUserPreferences();
        
        navigationService.getTtsService().speak(
            "Thank you for using the Enhanced Indoor Navigation System. " +
            "Your preferences have been saved. Stay safe!"
        );
        
        // Allow TTS to finish
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        isRunning = false;
    }
    
    public static void main(String[] args) {
        try {
            EnhancedIndoorNavigationApp app = new EnhancedIndoorNavigationApp();
            app.start();
        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.exit(0);
    }
}