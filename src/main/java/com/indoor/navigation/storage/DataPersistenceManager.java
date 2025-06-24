package com.indoor.navigation.storage;

import com.indoor.navigation.model.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Manages persistent storage of navigation data using JSON-like format
 */
public class DataPersistenceManager {
    private static final Logger logger = Logger.getLogger(DataPersistenceManager.class.getName());
    private static final String DATA_DIR = "data";
    private static final String MAPS_FILE = "navigation_maps.txt";
    private static final String PREFERENCES_FILE = "user_preferences.txt";
    private static final String HISTORY_FILE = "navigation_history.txt";
    
    public DataPersistenceManager() {
        createDataDirectory();
    }
    
    private void createDataDirectory() {
        try {
            Path dataPath = Paths.get(DATA_DIR);
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
            }
        } catch (IOException e) {
            logger.warning("Could not create data directory: " + e.getMessage());
        }
    }
    
    /**
     * Save navigation graph to file
     */
    public boolean saveNavigationGraph(NavigationGraph graph, String mapName) {
        try {
            Path filePath = Paths.get(DATA_DIR, mapName + "_map.txt");
            
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath))) {
                writer.println("# Navigation Map: " + mapName);
                writer.println("# Generated: " + new Date());
                writer.println();
                
                // Save rooms
                writer.println("ROOMS:");
                for (Room room : graph.getAllRooms()) {
                    writer.printf("ROOM|%s|%s|%s|%s|%d|%s|%s|%.1f|%s%n",
                                room.getId(),
                                escapeString(room.getName()),
                                escapeString(room.getDescription()),
                                room.getRoomType().name(),
                                room.getFloor(),
                                room.isAccessible(),
                                room.isBlocked(),
                                room.getWidth(),
                                room.isEmergencyExit());
                    
                    // Save room features
                    for (Map.Entry<String, String> feature : room.getFeatures().entrySet()) {
                        writer.printf("FEATURE|%s|%s|%s%n", 
                                    room.getId(), 
                                    escapeString(feature.getKey()),
                                    escapeString(feature.getValue()));
                    }
                    
                    // Save room landmarks
                    for (String landmark : room.getLandmarks()) {
                        writer.printf("LANDMARK|%s|%s%n", room.getId(), escapeString(landmark));
                    }
                }
                
                writer.println();
                writer.println("PATHS:");
                
                // Save paths (avoid duplicates by only saving from lower ID to higher ID)
                Set<String> savedPaths = new HashSet<>();
                for (Room room : graph.getAllRooms()) {
                    for (Path path : graph.getPathsFromRoom(room.getId())) {
                        String pathKey = room.getId().compareTo(path.getToRoom().getId()) < 0 ?
                                       room.getId() + "->" + path.getToRoom().getId() :
                                       path.getToRoom().getId() + "->" + room.getId();
                        
                        if (!savedPaths.contains(pathKey)) {
                            writer.printf("PATH|%s|%s|%.1f|%s|%s|%s|%s|%s|%.1f%n",
                                        path.getFromRoom().getId(),
                                        path.getToRoom().getId(),
                                        path.getDistance(),
                                        escapeString(path.getInstruction()),
                                        escapeString(path.getLandmarkInstruction()),
                                        path.getPathType(),
                                        path.isAccessible(),
                                        path.isBlocked(),
                                        path.getWidth());
                            savedPaths.add(pathKey);
                        }
                    }
                }
            }
            
            logger.info("Navigation graph saved to: " + filePath);
            return true;
            
        } catch (IOException e) {
            logger.severe("Error saving navigation graph: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Load navigation graph from file
     */
    public NavigationGraph loadNavigationGraph(String mapName) {
        NavigationGraph graph = new NavigationGraph();
        Path filePath = Paths.get(DATA_DIR, mapName + "_map.txt");
        
        if (!Files.exists(filePath)) {
            logger.warning("Map file not found: " + filePath);
            return graph;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                
                String[] parts = line.split("\\|");
                if (parts.length == 0) continue;
                
                switch (parts[0]) {
                    case "ROOM":
                        if (parts.length >= 9) {
                            parseRoom(parts, graph);
                        }
                        break;
                    case "FEATURE":
                        if (parts.length >= 4) {
                            parseRoomFeature(parts, graph);
                        }
                        break;
                    case "LANDMARK":
                        if (parts.length >= 3) {
                            parseRoomLandmark(parts, graph);
                        }
                        break;
                    case "PATH":
                        if (parts.length >= 9) {
                            parsePath(parts, graph);
                        }
                        break;
                }
            }
            
            logger.info("Navigation graph loaded from: " + filePath);
            
        } catch (IOException e) {
            logger.severe("Error loading navigation graph: " + e.getMessage());
        }
        
        return graph;
    }
    
    private void parseRoom(String[] parts, NavigationGraph graph) {
        try {
            String id = parts[1];
            String name = unescapeString(parts[2]);
            String description = unescapeString(parts[3]);
            RoomType roomType = RoomType.valueOf(parts[4]);
            int floor = Integer.parseInt(parts[5]);
            boolean accessible = Boolean.parseBoolean(parts[6]);
            boolean blocked = Boolean.parseBoolean(parts[7]);
            double width = Double.parseDouble(parts[8]);
            boolean emergencyExit = Boolean.parseBoolean(parts[9]);
            
            Room room = new Room(id, name, description, roomType, floor);
            room.setAccessible(accessible);
            room.setBlocked(blocked);
            room.setWidth(width);
            room.setEmergencyExit(emergencyExit);
            
            graph.addRoom(room);
            
        } catch (Exception e) {
            logger.warning("Error parsing room: " + e.getMessage());
        }
    }
    
    private void parseRoomFeature(String[] parts, NavigationGraph graph) {
        try {
            String roomId = parts[1];
            String key = unescapeString(parts[2]);
            String value = unescapeString(parts[3]);
            
            Room room = graph.getRoomById(roomId);
            if (room != null) {
                room.addFeature(key, value);
            }
        } catch (Exception e) {
            logger.warning("Error parsing room feature: " + e.getMessage());
        }
    }
    
    private void parseRoomLandmark(String[] parts, NavigationGraph graph) {
        try {
            String roomId = parts[1];
            String landmark = unescapeString(parts[2]);
            
            Room room = graph.getRoomById(roomId);
            if (room != null) {
                room.addLandmark(landmark);
            }
        } catch (Exception e) {
            logger.warning("Error parsing room landmark: " + e.getMessage());
        }
    }
    
    private void parsePath(String[] parts, NavigationGraph graph) {
        try {
            String fromId = parts[1];
            String toId = parts[2];
            double distance = Double.parseDouble(parts[3]);
            String instruction = unescapeString(parts[4]);
            String landmarkInstruction = unescapeString(parts[5]);
            String pathType = parts[6];
            boolean accessible = Boolean.parseBoolean(parts[7]);
            boolean blocked = Boolean.parseBoolean(parts[8]);
            double width = Double.parseDouble(parts[9]);
            
            Room fromRoom = graph.getRoomById(fromId);
            Room toRoom = graph.getRoomById(toId);
            
            if (fromRoom != null && toRoom != null) {
                Path path = new Path(fromRoom, toRoom, distance, instruction, pathType, width);
                path.setLandmarkInstruction(landmarkInstruction);
                path.setAccessible(accessible);
                path.setBlocked(blocked);
                
                graph.addPath(path);
            }
            
        } catch (Exception e) {
            logger.warning("Error parsing path: " + e.getMessage());
        }
    }
    
    /**
     * Save user preferences
     */
    public boolean saveUserPreferences(UserPreferences preferences) {
        try {
            Path filePath = Paths.get(DATA_DIR, preferences.getUserId() + "_preferences.txt");
            
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath))) {
                writer.println("# User Preferences: " + preferences.getUserId());
                writer.println("# Saved: " + new Date());
                writer.println();
                
                writer.println("USER_ID=" + preferences.getUserId());
                writer.println("PREFERRED_MODE=" + preferences.getPreferredMode().name());
                writer.println("TTS_ENABLED=" + preferences.isTtsEnabled());
                writer.println("VOICE_RECOGNITION_ENABLED=" + preferences.isVoiceRecognitionEnabled());
                writer.println("SPEECH_RATE=" + preferences.getSpeechRate());
                writer.println("USE_LANDMARK_INSTRUCTIONS=" + preferences.isUseLandmarkInstructions());
                writer.println("PREFERRED_VOICE=" + preferences.getPreferredVoice());
                writer.println("INSTRUCTION_PAUSE_TIME=" + preferences.getInstructionPauseTime());
                
                // Save avoid room types
                if (!preferences.getAvoidRoomTypes().isEmpty()) {
                    writer.print("AVOID_ROOM_TYPES=");
                    writer.println(String.join(",", preferences.getAvoidRoomTypes().stream()
                                                    .map(RoomType::name).toArray(String[]::new)));
                }
                
                // Save avoid path types
                if (!preferences.getAvoidPathTypes().isEmpty()) {
                    writer.println("AVOID_PATH_TYPES=" + String.join(",", preferences.getAvoidPathTypes()));
                }
                
                // Save navigation history
                if (!preferences.getNavigationHistory().isEmpty()) {
                    writer.println("NAVIGATION_HISTORY=" + String.join(",", preferences.getNavigationHistory()));
                }
                
                // Save custom settings
                for (Map.Entry<String, Object> setting : preferences.getCustomSettings().entrySet()) {
                    writer.println("CUSTOM_" + setting.getKey() + "=" + setting.getValue());
                }
            }
            
            logger.info("User preferences saved to: " + filePath);
            return true;
            
        } catch (IOException e) {
            logger.severe("Error saving user preferences: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Load user preferences
     */
    public UserPreferences loadUserPreferences(String userId) {
        UserPreferences preferences = new UserPreferences(userId);
        Path filePath = Paths.get(DATA_DIR, userId + "_preferences.txt");
        
        if (!Files.exists(filePath)) {
            logger.info("Preferences file not found, using defaults: " + filePath);
            return preferences;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                
                String[] parts = line.split("=", 2);
                if (parts.length != 2) continue;
                
                String key = parts[0];
                String value = parts[1];
                
                try {
                    switch (key) {
                        case "PREFERRED_MODE":
                            preferences.setPreferredMode(NavigationMode.valueOf(value));
                            break;
                        case "TTS_ENABLED":
                            preferences.setTtsEnabled(Boolean.parseBoolean(value));
                            break;
                        case "VOICE_RECOGNITION_ENABLED":
                            preferences.setVoiceRecognitionEnabled(Boolean.parseBoolean(value));
                            break;
                        case "SPEECH_RATE":
                            preferences.setSpeechRate(Double.parseDouble(value));
                            break;
                        case "USE_LANDMARK_INSTRUCTIONS":
                            preferences.setUseLandmarkInstructions(Boolean.parseBoolean(value));
                            break;
                        case "PREFERRED_VOICE":
                            preferences.setPreferredVoice(value);
                            break;
                        case "INSTRUCTION_PAUSE_TIME":
                            preferences.setInstructionPauseTime(Double.parseDouble(value));
                            break;
                        case "AVOID_ROOM_TYPES":
                            for (String roomType : value.split(",")) {
                                preferences.addAvoidRoomType(RoomType.valueOf(roomType.trim()));
                            }
                            break;
                        case "AVOID_PATH_TYPES":
                            for (String pathType : value.split(",")) {
                                preferences.addAvoidPathType(pathType.trim());
                            }
                            break;
                        case "NAVIGATION_HISTORY":
                            List<String> history = Arrays.asList(value.split(","));
                            for (String destination : history) {
                                preferences.addToHistory(destination.trim());
                            }
                            break;
                        default:
                            if (key.startsWith("CUSTOM_")) {
                                String customKey = key.substring(7);
                                preferences.setCustomSetting(customKey, value);
                            }
                            break;
                    }
                } catch (Exception e) {
                    logger.warning("Error parsing preference: " + key + " = " + value);
                }
            }
            
            logger.info("User preferences loaded from: " + filePath);
            
        } catch (IOException e) {
            logger.severe("Error loading user preferences: " + e.getMessage());
        }
        
        return preferences;
    }
    
    /**
     * List available saved maps
     */
    public List<String> listSavedMaps() {
        List<String> maps = new ArrayList<>();
        
        try {
            Path dataPath = Paths.get(DATA_DIR);
            if (Files.exists(dataPath)) {
                Files.list(dataPath)
                     .filter(path -> path.getFileName().toString().endsWith("_map.txt"))
                     .forEach(path -> {
                         String fileName = path.getFileName().toString();
                         String mapName = fileName.substring(0, fileName.length() - 8); // Remove "_map.txt"
                         maps.add(mapName);
                     });
            }
        } catch (IOException e) {
            logger.warning("Error listing saved maps: " + e.getMessage());
        }
        
        Collections.sort(maps);
        return maps;
    }
    
    private String escapeString(String str) {
        return str.replace("|", "\\|").replace("\n", "\\n").replace("\r", "\\r");
    }
    
    private String unescapeString(String str) {
        return str.replace("\\|", "|").replace("\\n", "\n").replace("\\r", "\r");
    }
}