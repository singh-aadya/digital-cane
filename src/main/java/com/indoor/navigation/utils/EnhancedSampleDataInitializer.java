package com.indoor.navigation.utils;

import com.indoor.navigation.model.*;

/**
 * Enhanced sample data initializer with multi-floor building, room types, and landmarks
 */
public class EnhancedSampleDataInitializer {
    
    public static NavigationGraph createComprehensiveSampleMap() {
        NavigationGraph graph = new NavigationGraph("Medical Center Building", 
                                                   "Multi-floor medical facility with comprehensive navigation");
        
        // GROUND FLOOR (Floor 1)
        Room mainEntrance = new Room("ENT", "Main Entrance", "Primary building entrance with automatic doors", 
                                   RoomType.ENTRANCE, 1);
        mainEntrance.addFeature("wheelchair_access", "yes");
        mainEntrance.addFeature("automatic_doors", "yes");
        mainEntrance.addFeature("audio_announcement", "yes");
        mainEntrance.addLandmark("Information Desk");
        mainEntrance.addLandmark("Security Station");
        mainEntrance.setWidth(4.0); // Wide entrance
        
        Room lobby = new Room("LOB", "Main Lobby", "Central reception area with seating", 
                            RoomType.LOBBY, 1);
        lobby.addFeature("wheelchair_access", "yes");
        lobby.addFeature("seating_available", "yes");
        lobby.addLandmark("Reception Desk");
        lobby.addLandmark("Directory Board");
        lobby.addLandmark("Waiting Area");
        lobby.setWidth(6.0); // Very wide lobby
        
        Room reception = new Room("REC", "Reception Desk", "Main information and registration point", 
                                RoomType.RECEPTION, 1);
        reception.addFeature("wheelchair_access", "yes");
        reception.addFeature("hearing_loop", "yes");
        reception.addLandmark("Information Counter");
        
        Room elevator1 = new Room("ELV1", "Main Elevator Bank", "Primary elevator access to all floors", 
                                RoomType.ELEVATOR, 1);
        elevator1.addFeature("wheelchair_access", "yes");
        elevator1.addFeature("braille_buttons", "yes");
        elevator1.addFeature("audio_announcement", "yes");
        elevator1.addFeature("emergency_phone", "yes");
        elevator1.addLandmark("Elevator Control Panel");
        
        Room restroom1 = new Room("REST1", "Ground Floor Restroom", "Accessible restroom facilities", 
                                RoomType.RESTROOM, 1);
        restroom1.addFeature("wheelchair_access", "yes");
        restroom1.addFeature("grab_bars", "yes");
        restroom1.addFeature("baby_changing", "yes");
        restroom1.addLandmark("Accessibility Sign");
        
        Room cafeteria = new Room("CAF", "Hospital Cafeteria", "Dining area with variety of food options", 
                                RoomType.CAFETERIA, 1);
        cafeteria.addFeature("wheelchair_access", "yes");
        cafeteria.addFeature("varied_seating", "yes");
        cafeteria.addLandmark("Food Court");
        cafeteria.addLandmark("Vending Machines");
        
        Room pharmacy = new Room("PHAR", "Hospital Pharmacy", "Prescription and medical supplies", 
                               RoomType.OFFICE, 1);
        pharmacy.addFeature("wheelchair_access", "yes");
        pharmacy.addLandmark("Prescription Counter");
        
        Room emergencyExit1 = new Room("EXIT1", "Emergency Exit A", "Primary emergency exit", 
                                     RoomType.EMERGENCY_EXIT, 1);
        emergencyExit1.addFeature("wheelchair_access", "yes");
        emergencyExit1.addFeature("emergency_lighting", "yes");
        emergencyExit1.setEmergencyExit(true);
        
        // SECOND FLOOR (Floor 2)
        Room elevator2 = new Room("ELV2", "Second Floor Elevator", "Elevator access on second floor", 
                                RoomType.ELEVATOR, 2);
        elevator2.addFeature("wheelchair_access", "yes");
        elevator2.addFeature("braille_buttons", "yes");
        elevator2.addFeature("audio_announcement", "yes");
        
        Room waitingArea2 = new Room("WAIT2", "Second Floor Waiting Area", "Patient waiting area with seating", 
                                   RoomType.WAITING_AREA, 2);
        waitingArea2.addFeature("wheelchair_access", "yes");
        waitingArea2.addFeature("comfortable_seating", "yes");
        waitingArea2.addLandmark("TV Screen");
        waitingArea2.addLandmark("Magazine Rack");
        
        Room consultRoom1 = new Room("CONS1", "Consultation Room 201", "Medical consultation room", 
                                   RoomType.OFFICE, 2);
        consultRoom1.addFeature("wheelchair_access", "yes");
        consultRoom1.addFeature("privacy_assured", "yes");
        
        Room consultRoom2 = new Room("CONS2", "Consultation Room 202", "Medical consultation room", 
                                   RoomType.OFFICE, 2);
        consultRoom2.addFeature("wheelchair_access", "yes");
        consultRoom2.addFeature("privacy_assured", "yes");
        
        Room laboratory = new Room("LAB", "Medical Laboratory", "Blood tests and medical analysis", 
                                 RoomType.OFFICE, 2);
        laboratory.addFeature("wheelchair_access", "yes");
        laboratory.addLandmark("Sample Collection Window");
        
        Room restroom2 = new Room("REST2", "Second Floor Restroom", "Accessible restroom facilities", 
                                RoomType.RESTROOM, 2);
        restroom2.addFeature("wheelchair_access", "yes");
        restroom2.addFeature("grab_bars", "yes");
        
        Room stairs2 = new Room("STAIR2", "Stairwell B", "Emergency stairs to all floors", 
                              RoomType.STAIRS, 2);
        stairs2.addFeature("emergency_lighting", "yes");
        stairs2.addFeature("handrails", "yes");
        stairs2.setAccessible(false); // Stairs not wheelchair accessible
        
        // THIRD FLOOR (Floor 3)
        Room elevator3 = new Room("ELV3", "Third Floor Elevator", "Elevator access on third floor", 
                                RoomType.ELEVATOR, 3);
        elevator3.addFeature("wheelchair_access", "yes");
        elevator3.addFeature("braille_buttons", "yes");
        elevator3.addFeature("audio_announcement", "yes");
        
        Room icu = new Room("ICU", "Intensive Care Unit", "Critical care medical unit", 
                          RoomType.ICU, 3);
        icu.addFeature("restricted_access", "yes");
        icu.addFeature("quiet_zone", "yes");
        icu.addLandmark("Nurses Station");
        
        Room conferenceRoom = new Room("CONF", "Medical Conference Room", "Large meeting room for medical staff", 
                                     RoomType.CONFERENCE_ROOM, 3);
        conferenceRoom.addFeature("wheelchair_access", "yes");
        conferenceRoom.addFeature("av_equipment", "yes");
        conferenceRoom.addLandmark("Projection Screen");
        
        Room library = new Room("LIB", "Medical Library", "Medical reference and study area", 
                               RoomType.LIBRARY, 3);
        library.addFeature("wheelchair_access", "yes");
        library.addFeature("quiet_study", "yes");
        library.addLandmark("Reference Desk");
        library.addLandmark("Computer Terminals");
        
        Room adminOffice = new Room("ADMIN", "Administration Office", "Hospital administration", 
                                  RoomType.OFFICE, 3);
        adminOffice.addFeature("wheelchair_access", "yes");
        adminOffice.addLandmark("Reception Window");
        
        Room emergencyExit3 = new Room("EXIT3", "Emergency Exit C", "Third floor emergency exit", 
                                     RoomType.EMERGENCY_EXIT, 3);
        emergencyExit3.addFeature("wheelchair_access", "yes");
        emergencyExit3.addFeature("emergency_lighting", "yes");
        emergencyExit3.setEmergencyExit(true);
        
        // Add all rooms to graph
        Room[] allRooms = {
            mainEntrance, lobby, reception, elevator1, restroom1, cafeteria, pharmacy, emergencyExit1,
            elevator2, waitingArea2, consultRoom1, consultRoom2, laboratory, restroom2, stairs2,
            elevator3, icu, conferenceRoom, library, adminOffice, emergencyExit3
        };
        
        for (Room room : allRooms) {
            graph.addRoom(room);
        }
        
        // Create paths with enhanced instructions and landmarks
        
        // GROUND FLOOR PATHS
        graph.addPath(new Path(mainEntrance, lobby, 8.0, 
                             "Walk straight through the main doors into the lobby", "corridor", 4.0));
        
        graph.addPath(new Path(lobby, reception, 5.0, 
                             "Head towards the reception desk on your left", "corridor", 3.0));
        
        graph.addPath(new Path(lobby, elevator1, 12.0, 
                             "Walk towards the elevator bank on the right side of the lobby", "corridor", 3.0));
        
        graph.addPath(new Path(lobby, restroom1, 15.0, 
                             "Follow the corridor to the left, restroom is on your right", "corridor", 2.5));
        
        graph.addPath(new Path(lobby, cafeteria, 20.0, 
                             "Walk straight past the reception desk, cafeteria entrance is ahead", "corridor", 3.0));
        
        graph.addPath(new Path(cafeteria, pharmacy, 8.0, 
                             "Exit cafeteria and turn left, pharmacy is next door", "corridor", 2.0));
        
        graph.addPath(new Path(restroom1, emergencyExit1, 10.0, 
                             "Continue down the corridor, emergency exit is at the end", "corridor", 2.5));
        
        // ELEVATOR CONNECTIONS (Multi-floor)
        graph.addPath(new Path(elevator1, elevator2, 0.5, 
                             "Take the elevator up to the second floor", "elevator", 2.0));
        
        graph.addPath(new Path(elevator2, elevator3, 0.5, 
                             "Take the elevator up to the third floor", "elevator", 2.0));
        
        graph.addPath(new Path(elevator1, elevator3, 1.0, 
                             "Take the elevator directly to the third floor", "elevator", 2.0));
        
        // SECOND FLOOR PATHS
        graph.addPath(new Path(elevator2, waitingArea2, 6.0, 
                             "Exit elevator and turn right, waiting area is ahead", "corridor", 3.0));
        
        graph.addPath(new Path(waitingArea2, consultRoom1, 8.0, 
                             "Walk down the corridor, room 201 is on your left", "corridor", 2.5));
        
        graph.addPath(new Path(consultRoom1, consultRoom2, 4.0, 
                             "Continue down the corridor, room 202 is next door", "corridor", 2.5));
        
        graph.addPath(new Path(waitingArea2, laboratory, 12.0, 
                             "Walk towards the lab, follow signs for blood work", "corridor", 2.5));
        
        graph.addPath(new Path(elevator2, restroom2, 10.0, 
                             "Turn left from elevator, restroom is at the end of the hall", "corridor", 2.5));
        
        graph.addPath(new Path(elevator2, stairs2, 15.0, 
                             "Walk to the far end of the corridor, stairs are on the right", "corridor", 2.0));
        
        // THIRD FLOOR PATHS
        graph.addPath(new Path(elevator3, icu, 8.0, 
                             "Turn left from elevator, ICU entrance is through the double doors", "corridor", 3.0));
        
        graph.addPath(new Path(elevator3, conferenceRoom, 10.0, 
                             "Walk straight from elevator, conference room is on your right", "corridor", 2.5));
        
        graph.addPath(new Path(conferenceRoom, library, 12.0, 
                             "Continue down the corridor, library entrance is on your left", "corridor", 2.5));
        
        graph.addPath(new Path(library, adminOffice, 6.0, 
                             "Walk towards the administration area, office is at the end", "corridor", 2.0));
        
        graph.addPath(new Path(elevator3, emergencyExit3, 18.0, 
                             "Walk to the end of the corridor, emergency exit is on your right", "corridor", 2.5));
        
        // STAIR CONNECTIONS (Alternative routes, not accessible)
        graph.addPath(new Path(lobby, stairs2, 25.0, 
                             "Take the stairs to the second floor (not wheelchair accessible)", "stairs", 1.5));
        
        graph.addPath(new Path(stairs2, emergencyExit3, 20.0, 
                             "Continue up the stairs to the third floor emergency exit", "stairs", 1.5));
        
        // ALTERNATIVE ELEVATOR ROUTES (Backup paths)
        graph.addPath(new Path(cafeteria, elevator1, 18.0, 
                             "Walk back towards the lobby, elevators are on your right", "corridor", 3.0));
        
        graph.addPath(new Path(consultRoom2, restroom2, 15.0, 
                             "Walk towards the elevator area, restroom is on your left", "corridor", 2.5));
        
        return graph;
    }
    
    /**
     * Create a smaller, simpler building for testing
     */
    public static NavigationGraph createSimpleTestMap() {
        NavigationGraph graph = new NavigationGraph("Simple Test Building", 
                                                   "Single floor building for testing");
        
        Room entrance = new Room("ENT", "Entrance", "Building entrance", RoomType.ENTRANCE, 1);
        Room hallway = new Room("HALL", "Main Hallway", "Central corridor", RoomType.CORRIDOR, 1);
        Room office1 = new Room("OFF1", "Office A", "First office", RoomType.OFFICE, 1);
        Room office2 = new Room("OFF2", "Office B", "Second office", RoomType.OFFICE, 1);
        Room restroom = new Room("REST", "Restroom", "Bathroom facilities", RoomType.RESTROOM, 1);
        Room exit = new Room("EXIT", "Emergency Exit", "Emergency exit", RoomType.EMERGENCY_EXIT, 1);
        
        // Add accessibility features
        entrance.addFeature("wheelchair_access", "yes");
        restroom.addFeature("wheelchair_access", "yes");
        restroom.addFeature("grab_bars", "yes");
        
        // Add landmarks
        entrance.addLandmark("Reception Desk");
        hallway.addLandmark("Directory Board");
        
        // Add rooms
        graph.addRoom(entrance);
        graph.addRoom(hallway);
        graph.addRoom(office1);
        graph.addRoom(office2);
        graph.addRoom(restroom);
        graph.addRoom(exit);
        
        // Add paths
        graph.addPath(new Path(entrance, hallway, 5.0, "Go straight down the main hallway", "corridor"));
        graph.addPath(new Path(hallway, office1, 8.0, "Turn left, office A is on your right", "corridor"));
        graph.addPath(new Path(hallway, office2, 10.0, "Turn right, office B is on your left", "corridor"));
        graph.addPath(new Path(hallway, restroom, 6.0, "Walk straight, restroom is on your left", "corridor"));
        graph.addPath(new Path(restroom, exit, 4.0, "Continue to the end of the hall, exit is on your right", "corridor"));
        graph.addPath(new Path(office1, office2, 12.0, "Walk across the hallway to office B", "corridor"));
        
        return graph;
    }
    
    /**
     * Simulate dynamic obstacles for testing
     */
    public static void simulateRandomObstacles(NavigationGraph graph) {
        // This would be called periodically to simulate real-world obstacles
        String[] obstacles = {
            "Maintenance work",
            "Cleaning in progress",
            "Delivery in progress",
            "Temporary closure",
            "Equipment blocking path"
        };
        
        // Get random paths and block them temporarily
        java.util.Random random = new java.util.Random();
        
        for (Room room : graph.getAllRooms()) {
            if (random.nextInt(20) == 0) { // 5% chance of obstacle
                var paths = graph.getPathsFromRoom(room.getId());
                if (!paths.isEmpty()) {
                    Path pathToBlock = paths.get(random.nextInt(paths.size()));
                    String obstacle = obstacles[random.nextInt(obstacles.length)];
                    
                    graph.simulateDynamicObstacle(
                        pathToBlock.getFromRoom().getId(),
                        pathToBlock.getToRoom().getId(),
                        obstacle,
                        30000L + random.nextInt(120000L) // 30 seconds to 2.5 minutes
                    );
                    
                    System.out.printf("🚧 Simulated obstacle: %s blocking path %s → %s%n",
                                    obstacle, 
                                    pathToBlock.getFromRoom().getName(),
                                    pathToBlock.getToRoom().getName());
                }
            }
        }
    }
}