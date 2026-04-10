package com.hanu.registration.model;

public class RuleConfig {

    private boolean enabled;
    private String startTime;

    private int retryInterval;
    private int maxRetries;

    private boolean queuePriority;
    private boolean stopWhenSuccess;
    private boolean avoidConflicts;
    private boolean onlyWhenOpen;

    private int minSlots;
    private int targetCredits;

    private String preferredSession;
    private boolean notifications;

    public RuleConfig() {
    }

    // ===== GETTER / SETTER =====

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public int getRetryInterval() { return retryInterval; }
    public void setRetryInterval(int retryInterval) { this.retryInterval = retryInterval; }

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }

    public boolean isQueuePriority() { return queuePriority; }
    public void setQueuePriority(boolean queuePriority) { this.queuePriority = queuePriority; }

    public boolean isStopWhenSuccess() { return stopWhenSuccess; }
    public void setStopWhenSuccess(boolean stopWhenSuccess) { this.stopWhenSuccess = stopWhenSuccess; }

    public boolean isAvoidConflicts() { return avoidConflicts; }
    public void setAvoidConflicts(boolean avoidConflicts) { this.avoidConflicts = avoidConflicts; }

    public boolean isOnlyWhenOpen() { return onlyWhenOpen; }
    public void setOnlyWhenOpen(boolean onlyWhenOpen) { this.onlyWhenOpen = onlyWhenOpen; }

    public int getMinSlots() { return minSlots; }
    public void setMinSlots(int minSlots) { this.minSlots = minSlots; }

    public int getTargetCredits() { return targetCredits; }
    public void setTargetCredits(int targetCredits) { this.targetCredits = targetCredits; }

    public String getPreferredSession() { return preferredSession; }
    public void setPreferredSession(String preferredSession) { this.preferredSession = preferredSession; }

    public boolean isNotifications() { return notifications; }
    public void setNotifications(boolean notifications) { this.notifications = notifications; }
}