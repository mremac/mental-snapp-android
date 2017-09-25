package com.SMobiLogger;

public enum LogType {
    ASSERT("Assert"), VERBOSE("Verbose"), DEBUG("Debug"), INFO("Information"), WARN(
            "Warning"), ERROR("Error"), OTHER("Other");

    String type;

    private LogType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}