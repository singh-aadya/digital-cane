package com.indoor.navigation.algorithm;

import com.indoor.navigation.model.*;
import java.util.*;

/**
 * Enhanced Dijkstra implementation with mode-aware pathfinding
 */
public class EnhancedDijkstraPathfinder {
    
    public static class PathResult {
        private final List<Room> path;
        private final List<String> instructions;
        private final List<String> landmarkInstructions;
        private final double totalDistance;
        private final boolean isAccessible;
        private final NavigationMode mode;
        private final List<Path> pathSegments;
        
        public PathResult(List<Room> path, List<String> instructions, List<String> landmarkInstructions,
                         double totalDistance, boolean isAccessible, NavigationMode mode, List<Path> pathSegments) {
            this.path = path;
            this.instructions = instructions;
            this.landmarkInstructions = landmarkInstructions;
            this.totalDistance = totalDistance;
            this.isAccessible = isAccessible;
            this.mode = mode;
            this.pathSegments = pathSegments;
        }
        
        public List<Room> getPath() { return path; }
        public List<String> getInstructions() { return instructions; }
        public List<String> getLandmarkInstructions() { return landmarkInstructions; }
        public double getTotalDistance() { return totalDistance; }
        public boolean isAccessible() { return isAccessible; }
        public NavigationMode getMode() { return mode; }
        public List<Path> getPathSegments() { return pathSegments; }
        
        public boolean isEmpty() {
            return path.isEmpty();
        }
        
        public String getSummary() {
            if (isEmpty()) return "No path found";
            
            return String.format("Route: %s → %s (%.1f meters, %s mode)", 
                               path.get(0).getName(), 
                               path.get(path.size() - 1).getName(),
                               totalDistance,
                               mode.getDisplayName());
        }
    }
    
    private static class Node implements Comparable<Node> {
        String roomId;
        double distance;
        
        Node(String roomId, double distance) {
            this.roomId = roomId;
            this.distance = distance;
        }
        
        @Override
        public int compareTo(Node other) {
            return Double.compare(this.distance, other.distance);
        }
    }
    
    public PathResult findShortestPath(NavigationGraph graph, String startRoomId, String endRoomId, 
                                     NavigationMode mode, UserPreferences preferences) {
        
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        Map<String, Path> pathsTaken = new HashMap<>();
        PriorityQueue<Node> queue = new PriorityQueue<>();
        Set<String> visited = new HashSet<>();
        
        // Initialize distances
        for (Room room : graph.getAllRooms()) {
            distances.put(room.getId(), Double.POSITIVE_INFINITY);
        }
        distances.put(startRoomId, 0.0);
        queue.offer(new Node(startRoomId, 0.0));
        
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            String currentRoomId = current.roomId;
            
            if (visited.contains(currentRoomId)) continue;
            visited.add(currentRoomId);
            
            if (currentRoomId.equals(endRoomId)) break;
            
            Room currentRoom = graph.getRoomById(currentRoomId);
            if (currentRoom.isBlocked()) continue;
            
            // Check if user preferences avoid this room type
            if (preferences != null && preferences.getAvoidRoomTypes().contains(currentRoom.getRoomType())) {
                continue;
            }
            
            for (Path path : graph.getPathsFromRoom(currentRoomId)) {
                Room neighborRoom = path.getToRoom();
                String neighborId = neighborRoom.getId();
                
                // Skip if path or room is blocked
                if (path.isCurrentlyBlocked() || neighborRoom.isBlocked()) continue;
                
                // Check navigation mode constraints
                if (!mode.isPathAllowed(path)) continue;
                
                // Check user preferences for path types
                if (preferences != null && preferences.getAvoidPathTypes().contains(path.getPathType())) {
                    continue;
                }
                
                // Check user preferences for room types
                if (preferences != null && preferences.getAvoidRoomTypes().contains(neighborRoom.getRoomType())) {
                    continue;
                }
                
                // Calculate weight based on navigation mode
                double pathWeight = mode.calculatePathWeight(path, currentRoom, neighborRoom);
                
                // Apply additional preferences-based adjustments
                if (preferences != null) {
                    pathWeight = applyPreferenceWeights(pathWeight, path, currentRoom, neighborRoom, preferences);
                }
                
                double newDistance = distances.get(currentRoomId) + pathWeight;
                
                if (newDistance < distances.get(neighborId)) {
                    distances.put(neighborId, newDistance);
                    previous.put(neighborId, currentRoomId);
                    pathsTaken.put(neighborId, path);
                    queue.offer(new Node(neighborId, newDistance));
                }
            }
        }
        
        // Reconstruct path
        return reconstructPath(graph, startRoomId, endRoomId, previous, pathsTaken, 
                             distances.get(endRoomId), mode, preferences);
    }
    
    private double applyPreferenceWeights(double baseWeight, Path path, Room fromRoom, Room toRoom, 
                                        UserPreferences preferences) {
        double weight = baseWeight;
        
        // Apply custom settings if any
        Object customMultiplier = preferences.getCustomSetting("path_weight_multiplier");
        if (customMultiplier instanceof Double) {
            weight *= (Double) customMultiplier;
        }
        
        // Prefer wider paths if specified
        Object preferWide = preferences.getCustomSetting("prefer_wide_paths");
        if (Boolean.TRUE.equals(preferWide) && path.getWidth() > 2.5) {
            weight *= 0.9; // 10% preference for wider paths
        }
        
        return weight;
    }
    
    private PathResult reconstructPath(NavigationGraph graph, String startRoomId, String endRoomId,
                                     Map<String, String> previous, Map<String, Path> pathsTaken,
                                     double totalDistance, NavigationMode mode, UserPreferences preferences) {
        
        List<Room> path = new ArrayList<>();
        List<String> instructions = new ArrayList<>();
        List<String> landmarkInstructions = new ArrayList<>();
        List<Path> pathSegments = new ArrayList<>();
        
        String current = endRoomId;
        boolean pathFound = previous.containsKey(endRoomId) || startRoomId.equals(endRoomId);
        
        if (!pathFound) {
            return new PathResult(Collections.emptyList(), Collections.emptyList(), 
                                Collections.emptyList(), Double.POSITIVE_INFINITY, false, mode, Collections.emptyList());
        }
        
        // Build path in reverse
        Stack<String> pathStack = new Stack<>();
        while (current != null) {
            pathStack.push(current);
            current = previous.get(current);
        }
        
        // Convert stack to forward path and build instructions
        String prevRoomId = null;
        while (!pathStack.isEmpty()) {
            String roomId = pathStack.pop();
            Room room = graph.getRoomById(roomId);
            path.add(room);
            
            if (prevRoomId != null) {
                Path pathSegment = pathsTaken.get(roomId);
                if (pathSegment != null) {
                    pathSegments.add(pathSegment);
                    
                    // Choose instruction type based on preferences
                    boolean useLandmarks = preferences == null || preferences.isUseLandmarkInstructions();
                    String instruction = useLandmarks ? 
                        pathSegment.getLandmarkInstruction() : pathSegment.getInstruction();
                    
                    instructions.add(instruction);
                    landmarkInstructions.add(pathSegment.getLandmarkInstruction());
                }
            }
            
            prevRoomId = roomId;
        }
        
        boolean isAccessible = checkPathAccessibility(pathSegments, mode);
        
        return new PathResult(path, instructions, landmarkInstructions, 
                            totalDistance, isAccessible, mode, pathSegments);
    }
    
    private boolean checkPathAccessibility(List<Path> pathSegments, NavigationMode mode) {
        for (Path path : pathSegments) {
            if (!mode.isPathAllowed(path)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Find alternative routes in case primary path is blocked
     */
    public List<PathResult> findAlternativeRoutes(NavigationGraph graph, String startRoomId, String endRoomId,
                                                 NavigationMode mode, UserPreferences preferences, int maxAlternatives) {
        List<PathResult> alternatives = new ArrayList<>();
        
        // Find primary route
        PathResult primary = findShortestPath(graph, startRoomId, endRoomId, mode, preferences);
        if (!primary.isEmpty()) {
            alternatives.add(primary);
        }
        
        // Temporarily block paths from primary route to find alternatives
        Set<Path> blockedPaths = new HashSet<>();
        
        for (int i = 0; i < maxAlternatives - 1 && i < primary.getPathSegments().size(); i++) {
            Path pathToBlock = primary.getPathSegments().get(i);
            pathToBlock.setTemporarilyBlocked(true, "Finding alternative route");
            blockedPaths.add(pathToBlock);
            
            PathResult alternative = findShortestPath(graph, startRoomId, endRoomId, mode, preferences);
            if (!alternative.isEmpty() && !alternatives.contains(alternative)) {
                alternatives.add(alternative);
            }
            
            if (alternatives.size() >= maxAlternatives) break;
        }
        
        // Restore blocked paths
        for (Path path : blockedPaths) {
            path.setTemporarilyBlocked(false, null);
        }
        
        return alternatives;
    }
    
    /**
     * Find emergency evacuation route to nearest exit
     */
    public PathResult findEmergencyExit(NavigationGraph graph, String startRoomId) {
        // Find all emergency exits
        List<Room> emergencyExits = new ArrayList<>();
        for (Room room : graph.getAllRooms()) {
            if (room.isEmergencyExit() || room.getRoomType() == RoomType.EMERGENCY_EXIT) {
                emergencyExits.add(room);
            }
        }
        
        if (emergencyExits.isEmpty()) {
            // No emergency exits, find any entrance
            for (Room room : graph.getAllRooms()) {
                if (room.getRoomType() == RoomType.ENTRANCE) {
                    emergencyExits.add(room);
                }
            }
        }
        
        PathResult bestExit = null;
        double shortestDistance = Double.POSITIVE_INFINITY;
        
        // Find closest emergency exit
        for (Room exit : emergencyExits) {
            PathResult result = findShortestPath(graph, startRoomId, exit.getId(), 
                                               NavigationMode.EMERGENCY, null);
            if (!result.isEmpty() && result.getTotalDistance() < shortestDistance) {
                bestExit = result;
                shortestDistance = result.getTotalDistance();
            }
        }
        
        return bestExit != null ? bestExit : 
               new PathResult(Collections.emptyList(), Collections.emptyList(), 
                            Collections.emptyList(), Double.POSITIVE_INFINITY, false, 
                            NavigationMode.EMERGENCY, Collections.emptyList());
    }
}