package com.indoor.navigation.service;

import com.indoor.navigation.model.UserPreferences;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Enhanced Text-to-Speech service with better audio simulation and queue management
 */
public class EnhancedTextToSpeechService {
    private boolean isEnabled;
    private double speechRate;
    private String voice;
    private BlockingQueue<String> speechQueue;
    private Thread speechThread;
    private boolean isPlaying;
    private UserPreferences preferences;
    
    public EnhancedTextToSpeechService() {
        this.isEnabled = true;
        this.speechRate = 1.0;
        this.voice = "default";
        this.speechQueue = new LinkedBlockingQueue<>();
        this.isPlaying = false;
        startSpeechThread();
    }
    
    public void setUserPreferences(UserPreferences preferences) {
        this.preferences = preferences;
        if (preferences != null) {
            this.isEnabled = preferences.isTtsEnabled();
            this.speechRate = preferences.getSpeechRate();
            this.voice = preferences.getPreferredVoice();
        }
    }
    
    private void startSpeechThread() {
        speechThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String text = speechQueue.take();
                    if ("STOP_SIGNAL".equals(text)) {
                        break;
                    }
                    speakNow(text);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        speechThread.setDaemon(true);
        speechThread.start();
    }
    
    public void speak(String text) {
        if (!isEnabled || text == null || text.trim().isEmpty()) return;
        
        try {
            speechQueue.offer(text.trim());
        } catch (Exception e) {
            System.err.println("Error queuing speech: " + e.getMessage());
        }
    }
    
    public void speakImmediately(String text) {
        if (!isEnabled || text == null || text.trim().isEmpty()) return;
        
        // Clear queue and speak immediately
        speechQueue.clear();
        speechQueue.offer(text.trim());
    }
    
    public CompletableFuture<Void> speakAsync(String text) {
        return CompletableFuture.runAsync(() -> speak(text));
    }
    
    private void speakNow(String text) {
        if (!isEnabled) return;
        
        isPlaying = true;
        
        try {
            // Enhanced TTS simulation with different voices
            String voiceIndicator = getVoiceIndicator();
            System.out.println("\n🔊 " + voiceIndicator + " TTS: " + text);
            
            // Simulate more realistic speech timing
            simulateRealisticAudioDelay(text);
            
            // Add pause after speech if specified in preferences
            if (preferences != null && preferences.getInstructionPauseTime() > 0) {
                Thread.sleep((long) (preferences.getInstructionPauseTime() * 1000));
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            isPlaying = false;
        }
    }
    
    private String getVoiceIndicator() {
        switch (voice.toLowerCase()) {
            case "male":
                return "👨";
            case "female":
                return "👩";
            case "robotic":
                return "🤖";
            default:
                return "🔊";
        }
    }
    
    private void simulateRealisticAudioDelay(String text) throws InterruptedException {
        // More sophisticated timing calculation
        int characterCount = text.length();
        int wordCount = text.split("\\s+").length;
        int punctuationCount = text.replaceAll("[^.!?,:;]", "").length();
        
        // Base timing: 150 words per minute
        double baseTime = (wordCount * 60.0 / 150.0);
        
        // Adjust for speech rate
        baseTime = baseTime / speechRate;
        
        // Add time for punctuation (pauses)
        baseTime += punctuationCount * 0.3;
        
        // Add time for character processing (for very short phrases)
        if (wordCount < 3) {
            baseTime = Math.max(baseTime, characterCount * 0.05);
        }
        
        // Simulate typing effect for longer texts
        if (text.length() > 50) {
            simulateTypingEffect(text, baseTime);
        } else {
            Thread.sleep((long) (baseTime * 1000));
        }
    }
    
    private void simulateTypingEffect(String text, double totalTime) throws InterruptedException {
        if (!isEnabled) return;
        
        long delayPerChar = (long) ((totalTime * 1000) / text.length());
        delayPerChar = Math.max(50, Math.min(delayPerChar, 200)); // Between 50-200ms per char
        
        System.out.print("📢 Speaking: ");
        for (char c : text.toCharArray()) {
            if (Thread.currentThread().isInterrupted()) break;
            System.out.print(c);
            Thread.sleep(delayPerChar);
        }
        System.out.println();
    }
    
    public void stopSpeaking() {
        speechQueue.clear();
        isPlaying = false;
        System.out.println("🔇 Speech stopped");
    }
    
    public void shutdown() {
        if (speechThread != null && speechThread.isAlive()) {
            speechQueue.offer("STOP_SIGNAL");
            speechThread.interrupt();
        }
    }
    
    public void speakNavigation(String instruction, boolean isEmergency) {
        String prefix = isEmergency ? "⚠️ EMERGENCY: " : "🧭 Navigation: ";
        speak(prefix + instruction);
    }
    
    public void speakWarning(String warning) {
        speakImmediately("⚠️ Warning: " + warning);
    }
    
    public void speakConfirmation(String message) {
        speak("✅ " + message);
    }
    
    public void speakError(String error) {
        speak("❌ Error: " + error);
    }
    
    // Voice options
    public void setVoice(String voice) {
        this.voice = voice;
        speak("Voice changed to " + voice);
    }
    
    public String[] getAvailableVoices() {
        return new String[]{"default", "male", "female", "robotic"};
    }
    
    // Getters and setters
    public void setEnabled(boolean enabled) { 
        this.isEnabled = enabled;
        if (enabled) {
            speak("Text to speech enabled");
        } else {
            System.out.println("🔇 Text to speech disabled");
        }
    }
    
    public void setSpeechRate(double rate) { 
        this.speechRate = Math.max(0.5, Math.min(2.0, rate));
        speak("Speech rate set to " + String.format("%.1f", this.speechRate));
    }
    
    public boolean isEnabled() { return isEnabled; }
    public double getSpeechRate() { return speechRate; }
    public String getVoice() { return voice; }
    public boolean isPlaying() { return isPlaying; }
    public int getQueueSize() { return speechQueue.size(); }
}