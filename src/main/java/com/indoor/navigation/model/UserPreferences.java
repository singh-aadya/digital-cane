package com.indoor.navigation.model;

import java.util.*;

/**
 * User preferences for navigation
 */
public class UserPreferences {
    private String userId;
    private NavigationMode preferredMode;
    private boolean ttsEnabled;
    private boolean voiceRecognitionEnabled;
    private double speechRate;
    private boolean useLandmarkInstructions;
    private List<RoomType> avoidRoomTypes;
    private List<String> avoidPathTypes;
    private Map<String, Object> customSettings;
    private List<String> navigationHistory;
    private String preferredVoice;
    private double instructionPauseTime; // seconds between instructions
    
    public UserPreferences() {
        this("default_user");
    }
    
    public UserPreferences(String userId) {
        this.userId = userId;
        this.preferredMode = NavigationMode.STANDARD;
        this.ttsEnabled = true;
        this.voiceRecognitionEnabled = true;
        this.speechRate = 1.0;
        this.useLandmarkInstructions = true;
        this.avoidRoomTypes = new ArrayList<>();
        this.avoidPathTypes = new ArrayList<>();
        this.customSettings = new HashMap<>();
        this.navigationHistory = new ArrayList<>();
        this.preferredVoice = "default";
        this.instructionPauseTime = 2.0;
    }
    
    public void addToHistory(String destination) {
        navigationHistory.add(0, destination); // Add to beginning
        // Keep only last 10 destinations
        if (navigationHistory.size() > 10) {
            navigationHistory = navigationHistory.subList(0, 10);
        }
    }
    
    public void addAvoidRoomType(RoomType roomType) {
        if (!avoidRoomTypes.contains(roomType)) {
            avoidRoomTypes.add(roomType);
        }
    }
    
    public void removeAvoidRoomType(RoomType roomType) {
        avoidRoomTypes.remove(roomType);
    }
    
    public void addAvoidPathType(String pathType) {
        if (!avoidPathTypes.contains(pathType)) {
            avoidPathTypes.add(pathType);
        }
    }
    
    public void removeAvoidPathType(String pathType) {
        avoidPathTypes.remove(pathType);
    }
    
    public void setCustomSetting(String key, Object value) {
        customSettings.put(key, value);
    }
    
    public Object getCustomSetting(String key) {
        return customSettings.get(key);
    }
    
    // Getters and setters
    public String getUserId() { return userId; }
    public NavigationMode getPreferredMode() { return preferredMode; }
    public boolean isTtsEnabled() { return ttsEnabled; }
    public boolean isVoiceRecognitionEnabled() { return voiceRecognitionEnabled; }
    public double getSpeechRate() { return speechRate; }
    public boolean isUseLandmarkInstructions() { return useLandmarkInstructions; }
    public List<RoomType> getAvoidRoomTypes() { return avoidRoomTypes; }
    public List<String> getAvoidPathTypes() { return avoidPathTypes; }
    public Map<String, Object> getCustomSettings() { return customSettings; }
    public List<String> getNavigationHistory() { return navigationHistory; }
    public String getPreferredVoice() { return preferredVoice; }
    public double getInstructionPauseTime() { return instructionPauseTime; }
    
    public void setUserId(String userId) { this.userId = userId; }
    public void setPreferredMode(NavigationMode preferredMode) { this.preferredMode = preferredMode; }
    public void setTtsEnabled(boolean ttsEnabled) { this.ttsEnabled = ttsEnabled; }
    public void setVoiceRecognitionEnabled(boolean voiceRecognitionEnabled) { 
        this.voiceRecognitionEnabled = voiceRecognitionEnabled; 
    }
    public void setSpeechRate(double speechRate) { 
        this.speechRate = Math.max(0.5, Math.min(2.0, speechRate)); 
    }
    public void setUseLandmarkInstructions(boolean useLandmarkInstructions) { 
        this.useLandmarkInstructions = useLandmarkInstructions; 
    }
    public void setPreferredVoice(String preferredVoice) { this.preferredVoice = preferredVoice; }
    public void setInstructionPauseTime(double instructionPauseTime) { 
        this.instructionPauseTime = Math.max(0.5, Math.min(10.0, instructionPauseTime)); 
    }
    
    @Override
    public String toString() {
        return String.format("UserPreferences{userId='%s', mode=%s, tts=%s, voice=%s}", 
                           userId, preferredMode.getDisplayName(), ttsEnabled, voiceRecognitionEnabled);
    }
}