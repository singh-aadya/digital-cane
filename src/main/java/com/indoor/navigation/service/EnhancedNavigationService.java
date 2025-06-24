package com.indoor.navigation.service;

import com.indoor.navigation.algorithm.EnhancedDijkstraPathfinder;
import com.indoor.navigation.algorithm.EnhancedDijkstraPathfinder.PathResult;
import com.indoor.navigation.model.*;
import com.indoor.navigation.storage.DataPersistenceManager;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Enhanced navigation service with mode-aware pathfinding and dynamic re-routing
 */
public class EnhancedNavigationService {
    private NavigationGraph graph;
    private EnhancedTextToSpeechService ttsService;
    private VoiceRecognitionService voiceService;
    private QRCodeService qrService;
    private DataPersistenceManager dataManager;
    private Room currentLocation;
    private PathResult lastPath;
    private UserPreferences userPreferences;
    private NavigationMode currentMode;
    private List<PathResult> alternativeRoutes;
    private Timer reRoutingTimer;
    
    public EnhancedNavigationService(NavigationGraph graph) {
        this.graph = graph;
        this.ttsService = new EnhancedTextToSpeechService();
        this.voiceService = new VoiceRecognitionService();
        this.qrService = new QRCodeService(graph);
        this.dataManager = new DataPersistenceManager();
        this.userPreferences = new UserPreferences();
        this.currentMode = NavigationMode.STANDARD;
        this.alternativeRoutes = new ArrayList<>();
        
        // Set up services with preferences
        ttsService.setUserPreferences(userPreferences);
        
        // Load user preferences
        loadUserPreferences("default_user");
    }
    
    public void loadUserPreferences(String userId) {
        userPreferences = dataManager.loadUserPreferences(userId);
        currentMode = userPreferences.getPreferredMode();
        ttsService.setUserPreferences(userPreferences);
        
        ttsService.speak("Welcome back " + userId + ". Navigation mode set to " + currentMode.getDisplayName());
    }
    
    public void saveUserPreferences() {
        dataManager.saveUserPreferences(userPreferences);
        ttsService.speakConfirmation("Preferences saved");
    }
    
    public boolean setCurrentLocation() {
        ttsService.speak("Please scan the QR code at your current location.");
        
        Room scannedRoom = qrService.scanQRCode();
        if (scannedRoom != null) {
            currentLocation = scannedRoom;
            String message = String.format("Current location set to: %s on floor %d - %s", 
                                         scannedRoom.getName(), scannedRoom.getFloor(), scannedRoom.getDescription());
            System.out.println("📍 " + message);
            ttsService.speakConfirmation(message);
            
            // Add to navigation history
            userPreferences.addToHistory(scannedRoom.getName());
            return true;
        }
        return false;
    }
    
    public PathResult navigateToDestination(String destination) {
        return navigateToDestination(destination, currentMode, true);
    }
    
    public PathResult navigateToDestination(String destination, NavigationMode mode, boolean usePreferences) {
        if (currentLocation == null) {
            String error = "Please scan QR code to set your current location first.";
            System.out.println("❌ " + error);
            ttsService.speakError(error);
            return null;
        }
        
        Room targetRoom = findDestinationRoom(destination);
        if (targetRoom == null) {
            String error = "Destination not found: " + destination;
            System.out.println("❌ " + error);
            ttsService.speakError(error);
            suggestSimilarDestinations(destination);
            return null;
        }
        
        if (targetRoom.equals(currentLocation)) {
            String message = "You are already at your destination.";
            System.out.println("✅ " + message);
            ttsService.speakConfirmation(message);
            return null;
        }
        
        // Check if destination is blocked
        if (targetRoom.isBlocked()) {
            String warning = targetRoom.getName() + " is currently blocked. Finding alternative nearby locations.";
            ttsService.speakWarning(warning);
            findAlternativeDestinations(targetRoom);
            return null;
        }
        
        EnhancedDijkstraPathfinder pathfinder = new EnhancedDijkstraPathfinder();
        UserPreferences prefs = usePreferences ? userPreferences : null;
        
        PathResult result = pathfinder.findShortestPath(graph, currentLocation.getId(), 
                                                      targetRoom.getId(), mode, prefs);
        
        if (result.isEmpty()) {
            String error = "No path found to " + destination + " using " + mode.getDisplayName() + " mode";
            System.out.println("❌ " + error);
            ttsService.speakError(error);
            
            // Try to find alternative routes with different modes
            tryAlternativeModes(destination, mode);
            return null;
        }
        
        lastPath = result;
        
        // Find alternative routes for backup
        alternativeRoutes = pathfinder.findAlternativeRoutes(graph, currentLocation.getId(), 
                                                           targetRoom.getId(), mode, prefs, 3);
        
        displayNavigationInstructions(result);
        
        // Add to navigation history
        userPreferences.addToHistory(destination);
        
        // Start monitoring for dynamic obstacles
        startDynamicMonitoring(result);
        
        return result;
    }
    
    private void suggestSimilarDestinations(String query) {
        List<Room> suggestions = graph.searchRooms(query);
        if (!suggestions.isEmpty() && suggestions.size() <= 5) {
            ttsService.speak("Did you mean one of these locations?");
            System.out.println("\n💡 Similar destinations found:");
            for (int i = 0; i < suggestions.size(); i++) {
                Room room = suggestions.get(i);
                String suggestion = String.format("%d. %s - %s", i + 1, room.getName(), room.getDescription());
                System.out.println(suggestion);
                ttsService.speak(suggestion);
            }
        }
    }
    
    private void findAlternativeDestinations(Room blockedRoom) {
        // Find similar room types nearby
        List<Room> alternatives = graph.getRoomsByType(blockedRoom.getRoomType()).stream()
                                      .filter(room -> !room.isBlocked() && !room.equals(blockedRoom))
                                      .limit(3)
                                      .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        
        if (!alternatives.isEmpty()) {
            ttsService.speak("Alternative locations found:");
            System.out.println("\n🔄 Alternative destinations:");
            for (int i = 0; i < alternatives.size(); i++) {
                Room room = alternatives.get(i);
                String alternative = String.format("%d. %s - %s", i + 1, room.getName(), room.getFullDescription());
                System.out.println(alternative);
                ttsService.speak(alternative);
            }
        }
    }
    
    private void tryAlternativeModes(String destination, NavigationMode originalMode) {
        System.out.println("\n🔄 Trying alternative navigation modes...");
        
        for (NavigationMode mode : NavigationMode.values()) {
            if (mode == originalMode) continue;
            
            ttsService.speak("Trying " + mode.getDisplayName() + " mode");
            PathResult result = navigateToDestination(destination, mode, false);
            if (result != null && !result.isEmpty()) {
                String suggestion = String.format("Alternative route found using %s mode. Would you like to use this route?", 
                                                mode.getDisplayName());
                ttsService.speak(suggestion);
                System.out.println("💡 " + suggestion);
                return;
            }
        }
        
        ttsService.speak("No alternative routes found. Please check if the destination is accessible or try a different location.");
    }
    
    private void startDynamicMonitoring(PathResult pathResult) {
        if (reRoutingTimer != null) {
            reRoutingTimer.cancel();
        }
        
        reRoutingTimer = new Timer();
        reRoutingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkForDynamicObstacles(pathResult);
            }
        }, 10000, 15000); // Check every 15 seconds after initial 10 second delay
    }
    
    private void checkForDynamicObstacles(PathResult pathResult) {
        // Check if any path in the current route is now blocked
        for (Path pathSegment : pathResult.getPathSegments()) {
            if (pathSegment.isCurrentlyBlocked()) {
                handleDynamicObstacle(pathSegment);
                break;
            }
        }
    }
    
    private void handleDynamicObstacle(Path blockedPath) {
        String warning = String.format("Path from %s to %s is now blocked: %s", 
                                     blockedPath.getFromRoom().getName(), 
                                     blockedPath.getToRoom().getName(),
                                     blockedPath.getBlockageInfo());
        
        System.out.println("⚠️ " + warning);
        ttsService.speakWarning(warning);
        
        // Try to use alternative route
        if (!alternativeRoutes.isEmpty()) {
            PathResult alternative = alternativeRoutes.get(1); // Use second route as alternative
            ttsService.speak("Switching to alternative route");
            displayNavigationInstructions(alternative);
            lastPath = alternative;
        } else {
            // Recalculate route
            ttsService.speak("Recalculating route");
            if (lastPath != null && !lastPath.getPath().isEmpty()) {
                String lastDestination = lastPath.getPath().get(lastPath.getPath().size() - 1).getName();
                navigateToDestination(lastDestination);
            }
        }
    }
    
    public void simulateDynamicObstacle() {
        if (lastPath == null || lastPath.getPathSegments().isEmpty()) {
            ttsService.speak("No active navigation to simulate obstacle");
            return;
        }
        
        // Simulate obstacle on a random path segment
        Random random = new Random();
        Path pathToBlock = lastPath.getPathSegments().get(random.nextInt(lastPath.getPathSegments().size()));
        
        String[] reasons = {
            "Maintenance work in progress",
            "Temporary crowd congestion", 
            "Cleaning in progress",
            "Equipment delivery blocking path",
            "Emergency drill"
        };
        
        String reason = reasons[random.nextInt(reasons.length)];
        long duration = 30000L + random.nextInt(60000); // 30-90 seconds
        
        graph.simulateDynamicObstacle(pathToBlock.getFromRoom().getId(), 
                                    pathToBlock.getToRoom().getId(), 
                                    reason, duration);
        
        String message = String.format("Simulated obstacle: %s on path %s → %s", 
                                     reason, pathToBlock.getFromRoom().getName(), pathToBlock.getToRoom().getName());
        System.out.println("🚧 " + message);
        ttsService.speak("Dynamic obstacle simulated: " + reason);
    }
    
    public PathResult findEmergencyExit() {
        if (currentLocation == null) {
            ttsService.speakError("Current location not set. Cannot find emergency exit.");
            return null;
        }
        
        ttsService.speakWarning("Finding nearest emergency exit");
        
        EnhancedDijkstraPathfinder pathfinder = new EnhancedDijkstraPathfinder();
        PathResult result = pathfinder.findEmergencyExit(graph, currentLocation.getId());
        
        if (!result.isEmpty()) {
            ttsService.speakNavigation("Emergency exit route calculated", true);
            displayNavigationInstructions(result);
            lastPath = result;
        } else {
            ttsService.speakError("No emergency exit found from current location");
        }
        
        return result;
    }
    
    private Room findDestinationRoom(String destination) {
        // Try exact match first
        Room room = graph.getRoomById(destination);
        if (room != null) return room;
        
        // Try name search
        room = graph.findRoomByName(destination);
        if (room != null) return room;
        
        // Try room type search
        try {
            RoomType roomType = RoomType.fromString(destination);
            room = graph.findNearestRoomOfType(currentLocation.getId(), roomType);
            if (room != null) return room;
        } catch (Exception e) {
            // Not a room type
        }
        
        // Try search with partial matches
        List<Room> searchResults = graph.searchRooms(destination);
        return searchResults.isEmpty() ? null : searchResults.get(0);
    }
    
    private void displayNavigationInstructions(PathResult result) {
        System.out.println("\n🧭 NAVIGATION INSTRUCTIONS");
        System.out.println("═══════════════════════════════════════════════════════");
        
        List<Room> path = result.getPath();
        List<String> instructions = userPreferences.isUseLandmarkInstructions() ? 
                                   result.getLandmarkInstructions() : result.getInstructions();
        
        // Summary
        String summary = result.getSummary();
        System.out.println(summary);
        ttsService.speakNavigation(summary, false);
        
        // Multi-floor notification
        Set<Integer> floors = new HashSet<>();
        path.forEach(room -> floors.add(room.getFloor()));
        if (floors.size() > 1) {
            String floorInfo = "This route spans " + floors.size() + " floors: " + floors;
            System.out.println("🏢 " + floorInfo);
            ttsService.speak(floorInfo);
        }
        
        // Step-by-step instructions
        System.out.println("\nStep-by-step directions:");
        for (int i = 0; i < path.size(); i++) {
            Room room = path.get(i);
            String step = String.format("Step %d: %s", i + 1, room.getName());
            
            if (room.getFloor() != 1) {
                step += " (Floor " + room.getFloor() + ")";
            }
            
            if (i < instructions.size() && instructions.get(i) != null && !instructions.get(i).isEmpty()) {
                step += " - " + instructions.get(i);
            }
            
            // Add landmark information
            if (!room.getLandmarks().isEmpty()) {
                step += " (" + room.getLandmarkDescription() + ")";
            }
            
            System.out.println(step);
            ttsService.speak(step);
            
            // Pause between instructions
            try {
                Thread.sleep((long) (userPreferences.getInstructionPauseTime() * 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        String completion = "You have arrived at your destination: " + path.get(path.size() - 1).getName();
        System.out.println("\n✅ " + completion);
        ttsService.speakConfirmation(completion);
        
        // Show alternative routes if available
        if (alternativeRoutes.size() > 1) {
            System.out.println("\n🔄 Alternative routes available. Say 'alternatives' to hear them.");
            ttsService.speak("Alternative routes are available if needed");
        }
    }
    
    public void showAlternativeRoutes() {
        if (alternativeRoutes.size() <= 1) {
            ttsService.speak("No alternative routes available");
            return;
        }
        
        ttsService.speak("Available alternative routes:");
        System.out.println("\n🔄 ALTERNATIVE ROUTES");
        System.out.println("═══════════════════════════════════════");
        
        for (int i = 1; i < Math.min(alternativeRoutes.size(), 4); i++) {
            PathResult route = alternativeRoutes.get(i);
            String routeInfo = String.format("Alternative %d: %.1f meters via %s mode", 
                                           i, route.getTotalDistance(), route.getMode().getDisplayName());
            System.out.println(routeInfo);
            ttsService.speak(routeInfo);
        }
    }
    
    public void repeatLastInstructions() {
        if (lastPath != null) {
            System.out.println("\n🔄 Repeating last navigation instructions...");
            ttsService.speak("Repeating last navigation instructions");
            displayNavigationInstructions(lastPath);
        } else {
            String message = "No previous navigation instructions to repeat.";
            System.out.println("❌ " + message);
            ttsService.speakError(message);
        }
    }
    
    public void setNavigationMode(NavigationMode mode) {
        this.currentMode = mode;
        this.userPreferences.setPreferredMode(mode);
        
        String message = "Navigation mode set to " + mode.getDisplayName() + ". " + mode.getDescription();
        System.out.println("⚙️ " + message);
        ttsService.speak(message);
    }
    
    public void showNavigationHistory() {
        List<String> history = userPreferences.getNavigationHistory();
        if (history.isEmpty()) {
            ttsService.speak("No navigation history available");
            return;
        }
        
        System.out.println("\n📜 NAVIGATION HISTORY");
        System.out.println("═══════════════════════════════════");
        ttsService.speak("Recent destinations:");
        
        for (int i = 0; i < Math.min(history.size(), 5); i++) {
            String destination = (i + 1) + ". " + history.get(i);
            System.out.println(destination);
            ttsService.speak(destination);
        }
    }
    
    public void shutdown() {
        if (reRoutingTimer != null) {
            reRoutingTimer.cancel();
        }
        ttsService.shutdown();
        saveUserPreferences();
    }
    
    // Getters and setters
    public Room getCurrentLocation() { return currentLocation; }
    public EnhancedTextToSpeechService getTtsService() { return ttsService; }
    public VoiceRecognitionService getVoiceService() { return voiceService; }
    public NavigationGraph getGraph() { return graph; }
    public UserPreferences getUserPreferences() { return userPreferences; }
    public NavigationMode getCurrentMode() { return currentMode; }
    public DataPersistenceManager getDataManager() { return dataManager; }
}