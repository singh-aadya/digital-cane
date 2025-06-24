package com.indoor.navigation.model;

/**
 * Different navigation modes with specific preferences and constraints
 */
public enum NavigationMode {
    VISUALLY_IMPAIRED("Visually Impaired", 
                     "Prefers wider corridors, avoids stairs, needs audio cues") {
        @Override
        public double calculatePathWeight(Path path, Room fromRoom, Room toRoom) {
            double baseWeight = path.getDistance();
            
            // Heavily penalize stairs
            if (path.getPathType().equals("stairs")) {
                return baseWeight * 5.0; // Make stairs very unattractive
            }
            
            // Prefer elevators and ramps
            if (path.getPathType().equals("elevator") || path.getPathType().equals("ramp")) {
                return baseWeight * 0.8;
            }
            
            // Avoid crowded areas
            baseWeight *= fromRoom.getRoomType().getCrowdFactor();
            baseWeight *= toRoom.getRoomType().getCrowdFactor();
            
            // Prefer wider corridors (simulated by corridor paths being preferred)
            if (path.getPathType().equals("corridor")) {
                return baseWeight * 0.9;
            }
            
            return baseWeight;
        }
        
        @Override
        public boolean isPathAllowed(Path path) {
            return !path.isBlocked() && path.isAccessible();
        }
    },
    
    WHEELCHAIR("Wheelchair User", 
              "Requires wheelchair accessible paths, no stairs") {
        @Override
        public double calculatePathWeight(Path path, Room fromRoom, Room toRoom) {
            double baseWeight = path.getDistance();
            
            // Completely avoid stairs
            if (path.getPathType().equals("stairs")) {
                return Double.POSITIVE_INFINITY; // Impossible path
            }
            
            // Strongly prefer ramps and elevators
            if (path.getPathType().equals("ramp")) {
                return baseWeight * 0.7; // Prefer ramps
            }
            if (path.getPathType().equals("elevator")) {
                return baseWeight * 0.8; // Prefer elevators
            }
            
            // Consider room accessibility
            if (!fromRoom.isAccessible() || !toRoom.isAccessible()) {
                return baseWeight * 1.5; // Penalize non-accessible rooms
            }
            
            return baseWeight;
        }
        
        @Override
        public boolean isPathAllowed(Path path) {
            return !path.isBlocked() && path.isAccessible() && 
                   !path.getPathType().equals("stairs");
        }
    },
    
    EMERGENCY("Emergency Mode", 
             "Fastest route regardless of accessibility") {
        @Override
        public double calculatePathWeight(Path path, Room fromRoom, Room toRoom) {
            // In emergency mode, only distance matters
            return path.getDistance();
        }
        
        @Override
        public boolean isPathAllowed(Path path) {
            return !path.isBlocked(); // Allow any non-blocked path
        }
    },
    
    STANDARD("Standard", 
            "Normal navigation with no special requirements") {
        @Override
        public double calculatePathWeight(Path path, Room fromRoom, Room toRoom) {
            double baseWeight = path.getDistance();
            
            // Slight preference for elevators over stairs
            if (path.getPathType().equals("stairs")) {
                return baseWeight * 1.2;
            }
            if (path.getPathType().equals("elevator")) {
                return baseWeight * 1.1;
            }
            
            // Consider crowd factors
            baseWeight *= (fromRoom.getRoomType().getCrowdFactor() + 
                          toRoom.getRoomType().getCrowdFactor()) / 2.0;
            
            return baseWeight;
        }
        
        @Override
        public boolean isPathAllowed(Path path) {
            return !path.isBlocked();
        }
    };

    private final String displayName;
    private final String description;

    NavigationMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    /**
     * Calculate the weight of a path based on this navigation mode
     */
    public abstract double calculatePathWeight(Path path, Room fromRoom, Room toRoom);

    /**
     * Determine if a path is allowed for this navigation mode
     */
    public abstract boolean isPathAllowed(Path path);
}