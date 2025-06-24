package com.indoor.navigation.service;

import com.indoor.navigation.model.UserPreferences;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Enhanced Voice Recognition Service with better command processing
 */
public class VoiceRecognitionService {
    private boolean isEnabled;
    private Scanner scanner;
    private Map<String, String> voiceCommands;
    private Map<String, List<String>> commandAliases;
    private UserPreferences preferences;
    private List<String> recentCommands;
    
    public VoiceRecognitionService() {
        this.isEnabled = true;
        this.scanner = new Scanner(System.in);
        this.voiceCommands = initializeVoiceCommands();
        this.commandAliases = initializeCommandAliases();
        this.recentCommands = new ArrayList<>();
    }
    
    public void setUserPreferences(UserPreferences preferences) {
        this.preferences = preferences;
        if (preferences != null) {
            this.isEnabled = preferences.isVoiceRecognitionEnabled();
        }
    }
    
    private Map<String, String> initializeVoiceCommands() {
        Map<String, String> commands = new HashMap<>();
        
        // Navigation commands
        commands.put("navigate to", "NAVIGATE");
        commands.put("go to", "NAVIGATE");
        commands.put("find", "NAVIGATE");
        commands.put("where is", "NAVIGATE");
        commands.put("take me to", "NAVIGATE");
        commands.put("directions to", "NAVIGATE");
        
        // Mode commands
        commands.put("switch to", "SWITCH_MODE");
        commands.put("change mode to", "SWITCH_MODE");
        commands.put("set mode", "SWITCH_MODE");
        
        // Emergency commands
        commands.put("emergency", "EMERGENCY");
        commands.put("emergency exit", "EMERGENCY_EXIT");
        commands.put("nearest exit", "EMERGENCY_EXIT");
        commands.put("help me", "EMERGENCY");
        
        // Information commands
        commands.put("help", "HELP");
        commands.put("repeat", "REPEAT");
        commands.put("alternatives", "ALTERNATIVES");
        commands.put("history", "HISTORY");
        commands.put("where am i", "LOCATION");
        commands.put("current location", "LOCATION");
        
        // Control commands
        commands.put("stop", "STOP");
        commands.put("pause", "PAUSE");
        commands.put("continue", "CONTINUE");
        commands.put("menu", "MENU");
        commands.put("exit", "EXIT");
        commands.put("quit", "EXIT");
        
        // Settings commands
        commands.put("settings", "SETTINGS");
        commands.put("preferences", "SETTINGS");
        commands.put("turn on", "TURN_ON");
        commands.put("turn off", "TURN_OFF");
        commands.put("enable", "TURN_ON");
        commands.put("disable", "TURN_OFF");
        
        return commands;
    }
    
    private Map<String, List<String>> initializeCommandAliases() {
        Map<String, List<String>> aliases = new HashMap<>();
        
        // Navigation aliases
        aliases.put("NAVIGATE", Arrays.asList("route to", "path to", "way to", "show me"));
        
        // Room type aliases
        aliases.put("restroom", Arrays.asList("bathroom", "toilet", "washroom", "wc"));
        aliases.put("cafeteria", Arrays.asList("dining", "food court", "restaurant", "cafe"));
        aliases.put("library", Arrays.asList("reading room", "study area", "books"));
        aliases.put("elevator", Arrays.asList("lift", "elevators"));
        aliases.put("stairs", Arrays.asList("stairway", "staircase", "steps"));
        aliases.put("exit", Arrays.asList("way out", "door", "entrance"));
        
        return aliases;
    }
    
    public CompletableFuture<String> listenForCommand() {
        return CompletableFuture.supplyAsync(() -> {
            if (!isEnabled) {
                return getTextInput();
            }
            
            System.out.print("🎤 Listening... (speak your command or type): ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                return "INVALID_INPUT";
            }
            
            // Add to recent commands
            addToRecentCommands(input);
            
            return processVoiceInput(input);
        });
    }
    
    public CompletableFuture<String> listenForDestination() {
        return CompletableFuture.supplyAsync(() -> {
            System.out.print("🎤 Where would you like to go? ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                return "INVALID_INPUT";
            }
            
            // Process destination with aliases
            return processDestinationInput(input);
        });
    }
    
    private String getTextInput() {
        System.out.print("💬 Enter your command: ");
        return scanner.nextLine().trim();
    }
    
    private void addToRecentCommands(String command) {
        recentCommands.add(0, command);
        if (recentCommands.size() > 10) {
            recentCommands = recentCommands.subList(0, 10);
        }
    }
    
    private String processVoiceInput(String input) {
        String lowerInput = input.toLowerCase().trim();
        
        // Check for exact command matches first
        for (Map.Entry<String, String> entry : voiceCommands.entrySet()) {
            if (lowerInput.startsWith(entry.getKey())) {
                String command = entry.getValue();
                String parameter = lowerInput.substring(entry.getKey().length()).trim();
                
                // Handle commands with parameters
                if (command.equals("NAVIGATE") && !parameter.isEmpty()) {
                    return "NAVIGATE:" + processDestinationInput(parameter);
                } else if (command.equals("SWITCH_MODE") && !parameter.isEmpty()) {
                    return "SWITCH_MODE:" + parameter;
                } else if (command.equals("TURN_ON") && !parameter.isEmpty()) {
                    return "TURN_ON:" + parameter;
                } else if (command.equals("TURN_OFF") && !parameter.isEmpty()) {
                    return "TURN_OFF:" + parameter;
                }
                
                return command;
            }
        }
        
        // Check for aliases
        for (Map.Entry<String, List<String>> aliasEntry : commandAliases.entrySet()) {
            if (aliasEntry.getKey().equals("NAVIGATE")) continue; // Skip navigation aliases here
            
            for (String alias : aliasEntry.getValue()) {
                if (lowerInput.contains(alias)) {
                    return "NAVIGATE:" + aliasEntry.getKey();
                }
            }
        }
        
        // Check for navigation aliases
        List<String> navAliases = commandAliases.get("NAVIGATE");
        if (navAliases != null) {
            for (String alias : navAliases) {
                if (lowerInput.startsWith(alias)) {
                    String destination = lowerInput.substring(alias.length()).trim();
                    if (!destination.isEmpty()) {
                        return "NAVIGATE:" + processDestinationInput(destination);
                    }
                }
            }
        }
        
        // If no command pattern matches, assume it's a destination
        if (lowerInput.contains("go") || lowerInput.contains("find") || lowerInput.contains("where")) {
            return "NAVIGATE:" + processDestinationInput(input);
        }
        
        // Check for numbers (room selection)
        try {
            int number = Integer.parseInt(lowerInput);
            return "SELECT:" + number;
        } catch (NumberFormatException e) {
            // Not a number
        }
        
        return "UNKNOWN:" + input;
    }
    
    private String processDestinationInput(String input) {
        String lowerInput = input.toLowerCase().trim();
        
        // Check for room type aliases
        for (Map.Entry<String, List<String>> entry : commandAliases.entrySet()) {
            if (entry.getKey().equals("NAVIGATE")) continue;
            
            // Check main key
            if (lowerInput.contains(entry.getKey().toLowerCase())) {
                return entry.getKey();
            }
            
            // Check aliases
            for (String alias : entry.getValue()) {
                if (lowerInput.contains(alias.toLowerCase())) {
                    return entry.getKey();
                }
            }
        }
        
        // Return original input if no aliases match
        return input;
    }
    
    public void showVoiceCommands() {
        System.out.println("\n🎤 VOICE COMMANDS");
        System.out.println("═══════════════════════════════════════");
        
        System.out.println("NAVIGATION:");
        System.out.println("• 'Navigate to [destination]' or 'Go to [destination]'");
        System.out.println("• 'Find [room type]' or 'Where is [location]'");
        System.out.println("• 'Take me to [destination]'");
        
        System.out.println("\nMODES:");
        System.out.println("• 'Switch to wheelchair mode'");
        System.out.println("• 'Change mode to visually impaired'");
        System.out.println("• 'Set mode emergency'");
        
        System.out.println("\nEMERGENCY:");
        System.out.println("• 'Emergency' or 'Help me'");
        System.out.println("• 'Emergency exit' or 'Nearest exit'");
        
        System.out.println("\nINFORMATION:");
        System.out.println("• 'Help' - Show help information");
        System.out.println("• 'Repeat' - Repeat last instructions");
        System.out.println("• 'Alternatives' - Show alternative routes");
        System.out.println("• 'History' - Show navigation history");
        System.out.println("• 'Where am I' - Current location");
        
        System.out.println("\nCONTROL:");
        System.out.println("• 'Stop' or 'Pause' - Stop current action");
        System.out.println("• 'Menu' - Return to main menu");
        System.out.println("• 'Exit' or 'Quit' - Exit application");
        
        System.out.println("\nROOM TYPES:");
        System.out.println("• Restroom (bathroom, toilet, washroom)");
        System.out.println("• Cafeteria (dining, food court, restaurant)");
        System.out.println("• Library (reading room, study area)");
        System.out.println("• Elevator (lift)");
        System.out.println("• Stairs (stairway, staircase)");
    }
    
    public void showRecentCommands() {
        if (recentCommands.isEmpty()) {
            System.out.println("No recent voice commands");
            return;
        }
        
        System.out.println("\n📜 RECENT VOICE COMMANDS");
        System.out.println("═══════════════════════════════════");
        for (int i = 0; i < Math.min(recentCommands.size(), 5); i++) {
            System.out.printf("%d. %s%n", i + 1, recentCommands.get(i));
        }
    }
    
    public boolean isCommandRecognized(String input) {
        String result = processVoiceInput(input);
        return !result.startsWith("UNKNOWN:");
    }
    
    public void trainVoiceCommand(String phrase, String command) {
        voiceCommands.put(phrase.toLowerCase(), command);
        System.out.printf("Voice command trained: '%s' → %s%n", phrase, command);
    }
    
    // Getters and setters
    public void setEnabled(boolean enabled) { 
        this.isEnabled = enabled;
        System.out.println("🎤 Voice Recognition " + (enabled ? "enabled" : "disabled"));
    }
    
    public boolean isEnabled() { return isEnabled; }
    public List<String> getRecentCommands() { return new ArrayList<>(recentCommands); }
    public Map<String, String> getVoiceCommands() { return new HashMap<>(voiceCommands); }
}