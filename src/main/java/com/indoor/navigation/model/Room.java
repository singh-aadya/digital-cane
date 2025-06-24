package com.indoor.navigation.model;

import java.util.*;

public class Room {
    private String id;
    private String name;
    private String description;
    private RoomType roomType;
    private int floor;
    private boolean isAccessible;
    private boolean isBlocked;
    private String qrCode;
    private Map<String, String> features; // accessibility features
    private List<String> landmarks; // Notable landmarks in/near this room
    private double width; // corridor width in meters (for accessibility)
    private boolean isEmergencyExit;
    
    public Room(String id, String name, String description) {
        this(id, name, description, RoomType.CORRIDOR, 1);
    }
    
    public Room(String id, String name, String description, RoomType roomType, int floor) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.roomType = roomType;
        this.floor = floor;
        this.isAccessible = true;
        this.isBlocked = false;
        this.qrCode = generateQRCode(id);
        this.features = new HashMap<>();
        this.landmarks = new ArrayList<>();
        this.width = 2.0; // Default 2 meters wide
        this.isEmergencyExit = roomType == RoomType.EMERGENCY_EXIT;
        
        // Set default accessibility based on room type
        if (roomType == RoomType.STAIRS) {
            this.isAccessible = false;
        }
    }
    
    private String generateQRCode(String roomId) {
        return "QR_" + roomId.toUpperCase() + "_" + System.currentTimeMillis() % 10000;
    }
    
    public void addLandmark(String landmark) {
        this.landmarks.add(landmark);
    }
    
    public void removeLandmark(String landmark) {
        this.landmarks.remove(landmark);
    }
    
    public String getLandmarkDescription() {
        if (landmarks.isEmpty()) {
            return "";
        }
        return "Near " + String.join(", ", landmarks);
    }
    
    // Getters and setters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public RoomType getRoomType() { return roomType; }
    public int getFloor() { return floor; }
    public boolean isAccessible() { return isAccessible; }
    public boolean isBlocked() { return isBlocked; }
    public String getQrCode() { return qrCode; }
    public Map<String, String> getFeatures() { return features; }
    public List<String> getLandmarks() { return landmarks; }
    public double getWidth() { return width; }
    public boolean isEmergencyExit() { return isEmergencyExit; }
    
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setRoomType(RoomType roomType) { 
        this.roomType = roomType;
        if (roomType == RoomType.STAIRS) {
            this.isAccessible = false;
        }
        this.isEmergencyExit = roomType == RoomType.EMERGENCY_EXIT;
    }
    public void setFloor(int floor) { this.floor = floor; }
    public void setAccessible(boolean accessible) { this.isAccessible = accessible; }
    public void setBlocked(boolean blocked) { this.isBlocked = blocked; }
    public void setWidth(double width) { this.width = width; }
    public void setEmergencyExit(boolean emergencyExit) { this.isEmergencyExit = emergencyExit; }
    
    public void addFeature(String key, String value) { 
        this.features.put(key, value); 
    }
    
    public void removeFeature(String key) {
        this.features.remove(key);
    }
    
    public String getFullDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(description);
        
        if (!landmarks.isEmpty()) {
            sb.append(" (").append(getLandmarkDescription()).append(")");
        }
        
        if (floor != 1) {
            sb.append(" - Floor ").append(floor);
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("Room{id='%s', name='%s', type=%s, floor=%d, accessible=%s, blocked=%s}", 
                           id, name, roomType.getDisplayName(), floor, isAccessible, isBlocked);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Room room = (Room) obj;
        return Objects.equals(id, room.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}