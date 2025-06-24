package com.indoor.navigation.model;

/**
 * Enumeration of different room types with their characteristics
 */
public enum RoomType {
    ENTRANCE("Entrance", "Building entrance/exit", 1.0, false),
    LOBBY("Lobby", "Central gathering area", 1.2, false),
    ELEVATOR("Elevator", "Vertical transportation", 1.0, false),
    STAIRS("Stairs", "Stairway", 2.0, true),
    RESTROOM("Restroom", "Bathroom facilities", 1.0, false),
    CAFETERIA("Cafeteria", "Dining area", 1.3, false),
    LIBRARY("Library", "Reading/study area", 1.1, false),
    CONFERENCE_ROOM("Conference Room", "Meeting space", 1.0, false),
    OFFICE("Office", "Work space", 1.0, false),
    CORRIDOR("Corridor", "Connecting passage", 1.0, false),
    EMERGENCY_EXIT("Emergency Exit", "Emergency exit point", 0.8, false),
    ICU("ICU", "Intensive Care Unit", 1.5, false),
    WAITING_AREA("Waiting Area", "Waiting/seating area", 1.4, false),
    RECEPTION("Reception", "Information desk", 1.1, false),
    PARKING("Parking", "Vehicle parking area", 1.0, false);

    private final String displayName;
    private final String description;
    private final double crowdFactor; // Higher = more crowded/slower
    private final boolean requiresSpecialAccess;

    RoomType(String displayName, String description, double crowdFactor, boolean requiresSpecialAccess) {
        this.displayName = displayName;
        this.description = description;
        this.crowdFactor = crowdFactor;
        this.requiresSpecialAccess = requiresSpecialAccess;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public double getCrowdFactor() { return crowdFactor; }
    public boolean requiresSpecialAccess() { return requiresSpecialAccess; }

    public static RoomType fromString(String type) {
        for (RoomType roomType : values()) {
            if (roomType.name().equalsIgnoreCase(type) || 
                roomType.displayName.equalsIgnoreCase(type)) {
                return roomType;
            }
        }
        return CORRIDOR; // Default
    }
}