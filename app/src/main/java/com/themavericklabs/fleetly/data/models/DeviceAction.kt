package com.themavericklabs.fleetly.data.models

import com.google.gson.annotations.SerializedName

enum class DeviceAction(val endpointPath: String, val displayName: String) {
    SYNC("syncDevice", "Sync"),
    WIPE("wipe", "Wipe"),
    RETIRE("retire", "Retire"),
    FRESH_START("freshStart", "Fresh Start"),
    ROTATE_ADMIN_PASSWORD("rotateLocalAdminPassword", "Rotate Admin Password"),
    COLLECT_DIAGNOSTIC_DATA("collectDiagnosticData", "Collect Diagnostic Data"),
    DELETE("delete", "Delete"),
    RESTART("rebootNow", "Restart"),
    SHUTDOWN("shutDown", "Shutdown"),
    REMOTE_LOCK("remoteLock", "Remote Lock"),
    AUTOPILOT_RESET("autopilotReset", "Autopilot Reset")
} 