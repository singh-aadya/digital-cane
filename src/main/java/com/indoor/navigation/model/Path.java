package com.indoor.navigation.model;

public class Path {
    private Room fromRoom;
    private Room toRoom;
    private double distance; // in meters
    private String instruction;
    private String landmarkInstruction; // Human-like instruction using landmarks
    private boolean isAccessible;
    private boolean isBlocked;
    private boolean isTemporarilyBlocked; // For dynamic obstacles
    private String pathType; // "corridor", "stairs", "elevator", "ramp"
    private double width; // path width in meters
    private String blockageReason; // Why the path is blocked
    private long blockageTimestamp; // When the path was blocked
    
    public Path(Room fromRoom, Room toRoom, double distance, String instruction) {
        this.fromRoom = fromRoom;
        this.toRoom = toRoom;
        this.distance = distance;
        this.instruction = instruction;
        this.isAccessible = true;
        this.isBlocked = false;
        this.isTemporarilyBlocked = false;
        this.pathType = "corridor";
        this.width = 2.0; // Default 2 meters wide
        this.landmarkInstruction = generateLandmarkInstruction();
    }
    
    public Path(Room fromRoom, Room toRoom, double distance, String instruction, String pathType) {
        this(fromRoom, toRoom, distance, instruction);
        this.pathType = pathType;
        this.isAccessible = !pathType.equals("stairs"); // stairs not accessible by default
        this.landmarkInstruction = generateLandmarkInstruction();
    }
    
    public Path(Room fromRoom, Room toRoom, double distance, String instruction, String pathType, double width) {
        this(fromRoom, toRoom, distance, instruction, pathType);
        this.width = width;
    }
    
    private String generateLandmarkInstruction() {
        StringBuilder sb = new StringBuilder();
        
        // Use landmarks from destination room if available
        if (!toRoom.getLandmarks().isEmpty()) {
            sb.append("Head towards ").append(toRoom.getLandmarks().get(0));
        } else {
            // Use room type for more natural instructions
            switch (toRoom.getRoomType()) {
                case ELEVATOR:
                    sb.append("Walk to the elevators");
                    break;
                case RESTROOM:
                    sb.append("Go to the restrooms");
                    break;
                case STAIRS:
                    sb.append("Take the stairs");
                    break;
                case CAFETERIA:
                    sb.append("Head to the cafeteria area");
                    break;
                case LIBRARY:
                    sb.append("Go to the library section");
                    break;
                case EMERGENCY_EXIT:
                    sb.append("Proceed to the emergency exit");
                    break;
                default:
                    sb.append("Go to ").append(toRoom.getName());
            }
        }
        
        // Add directional information based on path type
        switch (pathType) {
            case "elevator":
                sb.append(" and take the elevator");
                if (fromRoom.getFloor() != toRoom.getFloor()) {
                    if (toRoom.getFloor() > fromRoom.getFloor()) {
                        sb.append(" up to floor ").append(toRoom.getFloor());
                    } else {
                        sb.append(" down to floor ").append(toRoom.getFloor());
                    }
                }
                break;
            case "stairs":
                sb.append(" via the staircase");
                if (fromRoom.getFloor() != toRoom.getFloor()) {
                    if (toRoom.getFloor() > fromRoom.getFloor()) {
                        sb.append(" going up to floor ").append(toRoom.getFloor());
                    } else {
                        sb.append(" going down to floor ").append(toRoom.getFloor());
                    }
                }
                break;
            case "ramp":
                sb.append(" using the ramp");
                break;
        }
        
        return sb.toString();
    }
    
    public void setTemporarilyBlocked(boolean blocked, String reason) {
        this.isTemporarilyBlocked = blocked;
        this.blockageReason = reason;
        this.blockageTimestamp = blocked ? System.currentTimeMillis() : 0;
    }
    
    public boolean isCurrentlyBlocked() {
        return isBlocked || isTemporarilyBlocked;
    }
    
    public String getBlockageInfo() {
        if (isTemporarilyBlocked) {
            return "Temporarily blocked: " + (blockageReason != null ? blockageReason : "Unknown reason");
        } else if (isBlocked) {
            return "Permanently blocked";
        }
        return "Not blocked";
    }
    
    // Getters and setters
    public Room getFromRoom() { return fromRoom; }
    public Room getToRoom() { return toRoom; }
    public double getDistance() { return distance; }
    public String getInstruction() { return instruction; }
    public String getLandmarkInstruction() { return landmarkInstruction; }
    public boolean isAccessible() { return isAccessible; }
    public boolean isBlocked() { return isBlocked; }
    public boolean isTemporarilyBlocked() { return isTemporarilyBlocked; }
    public String getPathType() { return pathType; }
    public double getWidth() { return width; }
    public String getBlockageReason() { return blockageReason; }
    public long getBlockageTimestamp() { return blockageTimestamp; }
    
    public void setDistance(double distance) { this.distance = distance; }
    public void setInstruction(String instruction) { 
        this.instruction = instruction;
        this.landmarkInstruction = generateLandmarkInstruction();
    }
    public void setLandmarkInstruction(String landmarkInstruction) { 
        this.landmarkInstruction = landmarkInstruction; 
    }
    public void setAccessible(boolean accessible) { this.isAccessible = accessible; }
    public void setBlocked(boolean blocked) { this.isBlocked = blocked; }
    public void setPathType(String pathType) { 
        this.pathType = pathType;
        // Auto-set accessibility based on path type
        if (pathType.equals("stairs")) {
            this.isAccessible = false;
        }
        this.landmarkInstruction = generateLandmarkInstruction();
    }
    public void setWidth(double width) { this.width = width; }
    
    @Override
    public String toString() {
        String status = "";
        if (isCurrentlyBlocked()) {
            status = " [BLOCKED]";
        } else if (!isAccessible) {
            status = " [NOT ACCESSIBLE]";
        }
        
        return String.format("Path{%s → %s: %.1fm (%s)%s}", 
                           fromRoom.getName(), toRoom.getName(), distance, pathType, status);
    }
}