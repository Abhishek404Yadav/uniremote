package com.sony.braviaremote.model

/**
 * Standard Sony Bravia IRCC (Infra-Red Compatible Control) Base64 Codes
 * Supported natively by Sony Bravia KD-32W830K and Google TV / Android TV models.
 */
enum class IrccCommand(val code: String, val displayName: String) {
    // Power
    POWER("AAAAAQAAAAEAAAAVAw==", "Power"),
    POWER_ON("AAAAAQAAAAEAAAA1Aw==", "Power On"),
    POWER_OFF("AAAAAQAAAAEAAAAuAw==", "Power Off"),

    // Volume & Sound
    VOLUME_UP("AAAAAQAAAAEAAAASAw==", "Vol +"),
    VOLUME_DOWN("AAAAAQAAAAEAAAATAw==", "Vol -"),
    MUTE("AAAAAQAAAAEAAAAUAw==", "Mute"),

    // Navigation & D-Pad
    UP("AAAAAQAAAAEAAAB0Aw==", "Up"),
    DOWN("AAAAAQAAAAEAAAB1Aw==", "Down"),
    LEFT("AAAAAQAAAAEAAAA0Aw==", "Left"),
    RIGHT("AAAAAQAAAAEAAAAzAw==", "Right"),
    ENTER("AAAAAQAAAAEAAABlAw==", "OK"),
    BACK("AAAAAgAAAJcAAAAjAw==", "Back"),
    HOME("AAAAAQAAAAEAAABgAw==", "Home"),
    INPUT("AAAAAQAAAAEAAAAlAw==", "Input"),

    // Menu & TV System
    OPTIONS("AAAAAgAAAJcAAAA2Aw==", "Options"),
    ACTION_MENU("AAAAAgAAAMQAAABLAw==", "Action Menu"),
    SETTINGS("AAAAAgAAABoAAAB6Aw==", "Settings"),
    HELP("AAAAAgAAAMQAAABNAw==", "Help"),

    // Media Playback
    PLAY("AAAAAgAAAJcAAAAaAw==", "Play"),
    PAUSE("AAAAAgAAAJcAAAAZAw==", "Pause"),
    STOP("AAAAAgAAAJcAAAAYAw==", "Stop"),
    REWIND("AAAAAgAAAJcAAAAbAw==", "Rewind"),
    FAST_FORWARD("AAAAAgAAAJcAAAAcAw==", "Fast Forward"),
    PREV("AAAAAgAAAJcAAAA8Aw==", "Prev"),
    NEXT("AAAAAgAAAJcAAAA9Aw==", "Next"),

    // Channels
    CHANNEL_UP("AAAAAQAAAAEAAAAQAw==", "CH +"),
    CHANNEL_DOWN("AAAAAQAAAAEAAAARAw==", "CH -"),

    // Streaming Apps
    YOUTUBE("AAAAAgAAAMQAAABHAw==", "YouTube"),
    NETFLIX("AAAAAgAAABoAAAB8Aw==", "Netflix"),
    PRIME_VIDEO("AAAAAgAAABoAAABbAw==", "Prime Video"),

    // Numeric Digits (0-9)
    NUM_0("AAAAAQAAAAEAAAAJAw==", "0"),
    NUM_1("AAAAAQAAAAEAAAAAAw==", "1"),
    NUM_2("AAAAAQAAAAEAAAABAw==", "2"),
    NUM_3("AAAAAQAAAAEAAAACAw==", "3"),
    NUM_4("AAAAAQAAAAEAAAADAw==", "4"),
    NUM_5("AAAAAQAAAAEAAAAEAw==", "5"),
    NUM_6("AAAAAQAAAAEAAAAFAw==", "6"),
    NUM_7("AAAAAQAAAAEAAAAGAw==", "7"),
    NUM_8("AAAAAQAAAAEAAAAHAw==", "8"),
    NUM_9("AAAAAQAAAAEAAAAIAw==", "9"),
    DOT("AAAAAgAAAJcAAAAmAw==", ".");

    companion object {
        fun fromNumber(digit: Int): IrccCommand? = when (digit) {
            0 -> NUM_0
            1 -> NUM_1
            2 -> NUM_2
            3 -> NUM_3
            4 -> NUM_4
            5 -> NUM_5
            6 -> NUM_6
            7 -> NUM_7
            8 -> NUM_8
            9 -> NUM_9
            else -> null
        }
    }
}
