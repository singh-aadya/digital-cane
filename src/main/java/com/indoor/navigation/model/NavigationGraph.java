package com.indoor.navigation.model;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Enhanced navigation graph with multi-floor support and dynamic obstacle handling
 */
public class NavigationGraph {
    private Map<String, Room> rooms;
    private Map<String, List<Path>> adjacencyList;
    private Map<Integer, List<Room>> floorMap; // Floor number -> Rooms on that floor
    private List<Path> temporarilyBlockedPaths;
    private String graphName;
    private String description;
    
    public NavigationGraph() {
        this("Default Building", "Indoor navigation graph");
    }
    
    public NavigationGraph(String graphName, String description) {
        this.rooms = new HashMap<>();
        this.adjacencyList = new HashMap<>();
        this.floorMap = new HashMap<>();
        this.temporarilyBlockedPaths = new ArrayList<>();
        this.graphName = graphName;
        this.description = description;
    }
    
    public void addRoom(Room room) {
        rooms.put(room.getId(), room);
        adjacencyList.putIfAbsent(room.getId(), new ArrayList<>());
        
        // Add to floor map
        floorMap.computeIfAbsent(room.getFloor(), k -> new ArrayList<>()).add(room);
    }
    
    public void addPath(Path path) {
        String fromId = path.getFromRoom().getId();
        String toId = path.getToRoom().getId();
        
        // Add to adjacency list (bidirectional)
        adjacencyList.get(fromId).add(path);
        
        // Create reverse path
        Path reversePath = new Path(path.getToRoom(), path.getFromRoom(), 
                                  path.getDistance(), reverseInstruction(path.getInstruction()), 
                                  path.getPathType(), path.getWidth());
        reversePath.setLandmarkInstruction(reverseInstruction(path.getLandmarkInstruction()));
        reversePath.setAccessible(path.isAccessible());
        reversePath.setBlocked(path.isBlocked());
        
        adjacencyList.get(toId).add(reversePath);
    }
    
    private String reverseInstruction(String instruction) {
        if (instruction == null || instruction.isEmpty()) return instruction;
        
        // Simple reversal logic - could be enhanced with more sophisticated parsing
        return instruction.replace("Go straight", "Go straight back")
                         .replace("Turn left", "Turn right")
                         .replace("Turn right", "Turn left")
                         .replace("ahead", "behind you")
                         .replace("up to", "down to")
                         .replace("down to", "up to")
                         .replace("towards", "away from")
                         .replace("Head towards", "Head away from");
    }
    
    public void removePath(String fromRoomId, String toRoomId) {
        List<Path> fromPaths = adjacencyList.get(fromRoomId);
        List<Path> toPaths = adjacencyList.get(toRoomId);
        
        if (fromPaths != null) {
            fromPaths.removeIf(path -> path.getToRoom().getId().equals(toRoomId));
        }
        if (toPaths != null) {
            toPaths.removeIf(path -> path.getToRoom().getId().equals(fromRoomId));
        }
    }
    
    public void removeRoom(String roomId) {
        Room room = rooms.remove(roomId);
        if (room != null) {
            // Remove from floor map
            List<Room> floorRooms = floorMap.get(room.getFloor());
            if (floorRooms != null) {
                floorRooms.remove(room);
                if (floorRooms.isEmpty()) {
                    floorMap.remove(room.getFloor());
                }
            }
            
            // Remove all paths to/from this room
            adjacencyList.remove(roomId);
            adjacencyList.values().forEach(paths -> 
                paths.removeIf(path -> path.getToRoom().getId().equals(roomId)));
        }
    }
    
    public void blockRoom(String roomId, boolean blocked) {
        Room room = rooms.get(roomId);
        if (room != null) {
            room.setBlocked(blocked);
        }
    }
    
    public void blockPath(String fromRoomId, String toRoomId, boolean blocked) {
        blockPath(fromRoomId, toRoomId, blocked, blocked ? "Manual block" : null);
    }
    
    public void blockPath(String fromRoomId, String toRoomId, boolean blocked, String reason) {
        List<Path> paths = adjacencyList.get(fromRoomId);
        if (paths != null) {
            paths.stream()
                 .filter(path -> path.getToRoom().getId().equals(toRoomId))
                 .forEach(path -> {
                     if (blocked) {
                         path.setTemporarilyBlocked(true, reason);
                         if (!temporarilyBlockedPaths.contains(path)) {
                             temporarilyBlockedPaths.add(path);
                         }
                     } else {
                         path.setTemporarilyBlocked(false, null);
                         temporarilyBlockedPaths.remove(path);
                     }
                 });
        }
        
        // Block reverse path too
        List<Path> reversePaths = adjacencyList.get(toRoomId);
        if (reversePaths != null) {
            reversePaths.stream()
                       .filter(path -> path.getToRoom().getId().equals(fromRoomId))
                       .forEach(path -> {
                           if (blocked) {
                               path.setTemporarilyBlocked(true, reason);
                               if (!temporarilyBlockedPaths.contains(path)) {
                                   temporarilyBlockedPaths.add(path);
                               }
                           } else {
                               path.setTemporarilyBlocked(false, null);
                               temporarilyBlockedPaths.remove(path);
                           }
                       });
        }
    }
    
    /**
     * Simulate dynamic obstacles (maintenance, crowd, etc.)
     */
    public void simulateDynamicObstacle(String fromRoomId, String toRoomId, String reason, long durationMillis) {
        blockPath(fromRoomId, toRoomId, true, reason);
        
        // Schedule automatic unblocking (in a real system, this would be a scheduled task)
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                blockPath(fromRoomId, toRoomId, false, null);
                System.out.printf("🔓 Path %s → %s is now clear (obstacle cleared: %s)%n", 
                                fromRoomId, toRoomId, reason);
            }
        }, durationMillis);
    }
    
    /**
     * Clear all temporary blocks
     */
    public void clearAllTemporaryBlocks() {
        for (Path path : new ArrayList<>(temporarilyBlockedPaths)) {
            path.setTemporarilyBlocked(false, null);
        }
        temporarilyBlockedPaths.clear();
    }
    
    /**
     * Find rooms by type
     */
    public List<Room> getRoomsByType(RoomType roomType) {
        return rooms.values().stream()
                   .filter(room -> room.getRoomType() == roomType)
                   .collect(Collectors.toList());
    }
    
    /**
     * Find rooms on specific floor
     */
    public List<Room> getRoomsOnFloor(int floor) {
        return floorMap.getOrDefault(floor, new ArrayList<>());
    }
    
    /**
     * Find accessible rooms only
     */
    public List<Room> getAccessibleRooms() {
        return rooms.values().stream()
                   .filter(room -> room.isAccessible() && !room.isBlocked())
                   .collect(Collectors.toList());
    }
    
    /**
     * Find nearest room of specific type
     */
    public Room findNearestRoomOfType(String fromRoomId, RoomType roomType) {
        Room fromRoom = rooms.get(fromRoomId);
        if (fromRoom == null) return null;
        
        return getRoomsByType(roomType).stream()
               .filter(room -> !room.isBlocked() && room.isAccessible())
               .min((r1, r2) -> {
                   // Simple distance estimation based on floor difference and room count
                   int dist1 = Math.abs(r1.getFloor() - fromRoom.getFloor()) * 10 + 
                              Math.abs(r1.getId().hashCode() - fromRoom.getId().hashCode()) % 20;
                   int dist2 = Math.abs(r2.getFloor() - fromRoom.getFloor()) * 10 + 
                              Math.abs(r2.getId().hashCode() - fromRoom.getId().hashCode()) % 20;
                   return Integer.compare(dist1, dist2);
               })
               .orElse(null);
    }
    
    /**
     * Get building statistics
     */
    public Map<String, Object> getBuildingStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("total_rooms", rooms.size());
        stats.put("total_paths", adjacencyList.values().stream().mapToInt(List::size).sum() / 2); // Divide by 2 for bidirectional
        stats.put("floors", floorMap.keySet().size());
        stats.put("accessible_rooms", getAccessibleRooms().size());
        stats.put("blocked_rooms", rooms.values().stream().mapToInt(room -> room.isBlocked() ? 1 : 0).sum());
        stats.put("temporarily_blocked_paths", temporarilyBlockedPaths.size() / 2); // Divide by 2 for bidirectional
        
        // Room type distribution
        Map<RoomType, Long> roomTypes = rooms.values().stream()
                                            .collect(Collectors.groupingBy(Room::getRoomType, Collectors.counting()));
        stats.put("room_types", roomTypes);
        
        return stats;
    }
    
    // Getters
    public String getGraphName() { return graphName; }
    public String getDescription() { return description; }
    public Room getRoomById(String id) { return rooms.get(id); }
    public Room getRoomByQRCode(String qrCode) {
        return rooms.values().stream()
                   .filter(room -> room.getQrCode().equals(qrCode))
                   .findFirst()
                   .orElse(null);
    }
    
    public Collection<Room> getAllRooms() { return rooms.values(); }
    public List<Path> getPathsFromRoom(String roomId) { 
        return adjacencyList.getOrDefault(roomId, new ArrayList<>());
    }
    
    public Room findRoomByName(String name) {
        return rooms.values().stream()
                   .filter(room -> room.getName().toLowerCase().contains(name.toLowerCase()))
                   .findFirst()
                   .orElse(null);
    }
    
    public List<Room> searchRooms(String query) {
        String lowerQuery = query.toLowerCase();
        return rooms.values().stream()
                   .filter(room -> 
                       room.getName().toLowerCase().contains(lowerQuery) ||
                       room.getDescription().toLowerCase().contains(lowerQuery) ||
                       room.getRoomType().getDisplayName().toLowerCase().contains(lowerQuery) ||
                       room.getLandmarks().stream().anyMatch(landmark -> 
                           landmark.toLowerCase().contains(lowerQuery)))
                   .collect(Collectors.toList());
    }
    
    public Set<Integer> getFloors() { return floorMap.keySet(); }
    public List<Path> getTemporarilyBlockedPaths() { return new ArrayList<>(temporarilyBlockedPaths); }
    
    public void setGraphName(String graphName) { this.graphName = graphName; }
    public void setDescription(String description) { this.description = description; }
}