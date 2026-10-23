package com.sony.braviaremote.model

/**
 * Standard Sony Bravia IRCC (Infra-Red Compatible Control) Base64 Codes
 * Supported natively by Sony Bravia KD-32W830K and Google TV / Android TV models.
 */
enum class IrccCommand(
    val code: String,
    val displayName: String,
    val androidKeyCode: Int = 0,
    val appUrl: String? = null
) {
    // Power
    POWER("AAAAAQAAAAEAAAAVAw==", "Power", androidKeyCode = 26),
    POWER_ON("AAAAAQAAAAEAAAA1Aw==", "Power On", androidKeyCode = 224),
    POWER_OFF("AAAAAQAAAAEAAAAuAw==", "Power Off", androidKeyCode = 223),

    // Volume & Sound
    VOLUME_UP("AAAAAQAAAAEAAAASAw==", "Vol +", androidKeyCode = 24),
    VOLUME_DOWN("AAAAAQAAAAEAAAATAw==", "Vol -", androidKeyCode = 25),
    MUTE("AAAAAQAAAAEAAAAUAw==", "Mute", androidKeyCode = 164),

    // Navigation & D-Pad
    UP("AAAAAQAAAAEAAAB0Aw==", "Up", androidKeyCode = 19),
    DOWN("AAAAAQAAAAEAAAB1Aw==", "Down", androidKeyCode = 20),
    LEFT("AAAAAQAAAAEAAAA0Aw==", "Left", androidKeyCode = 21),
    RIGHT("AAAAAQAAAAEAAAAzAw==", "Right", androidKeyCode = 22),
    ENTER("AAAAAQAAAAEAAABlAw==", "OK", androidKeyCode = 23),
    BACK("AAAAAgAAAJcAAAAjAw==", "Back", androidKeyCode = 4),
    HOME("AAAAAQAAAAEAAABgAw==", "Home", androidKeyCode = 3),
    INPUT("AAAAAQAAAAEAAAAlAw==", "Input", androidKeyCode = 178),

    // Menu & TV System
    OPTIONS("AAAAAgAAAJcAAAA2Aw==", "Options", androidKeyCode = 82),
    ACTION_MENU("AAAAAgAAAMQAAABLAw==", "Action Menu", androidKeyCode = 82),
    SETTINGS("AAAAAgAAABoAAAB6Aw==", "Settings", androidKeyCode = 176),
    HELP("AAAAAgAAAMQAAABNAw==", "Help", androidKeyCode = 259),

    // Media Playback
    PLAY("AAAAAgAAAJcAAAAaAw==", "Play", androidKeyCode = 126),
    PAUSE("AAAAAgAAAJcAAAAZAw==", "Pause", androidKeyCode = 127),
    STOP("AAAAAgAAAJcAAAAYAw==", "Stop", androidKeyCode = 86),
    REWIND("AAAAAgAAAJcAAAAbAw==", "Rewind", androidKeyCode = 89),
    FAST_FORWARD("AAAAAgAAAJcAAAAcAw==", "Fast Forward", androidKeyCode = 90),
    PREV("AAAAAgAAAJcAAAA8Aw==", "Prev", androidKeyCode = 88),
    NEXT("AAAAAgAAAJcAAAA9Aw==", "Next", androidKeyCode = 87),

    // Channels
    CHANNEL_UP("AAAAAQAAAAEAAAAQAw==", "CH +", androidKeyCode = 166),
    CHANNEL_DOWN("AAAAAQAAAAEAAAARAw==", "CH -", androidKeyCode = 167),

    // Streaming Apps
    YOUTUBE("AAAAAgAAAMQAAABHAw==", "YouTube", appUrl = "https://www.youtube.com"),
    NETFLIX("AAAAAgAAABoAAAB8Aw==", "Netflix", appUrl = "https://www.netflix.com"),
    PRIME_VIDEO("AAAAAgAAABoAAABbAw==", "Prime Video", appUrl = "https://app.primevideo.com"),

    // Numeric Digits (0-9)
    NUM_0("AAAAAQAAAAEAAAAJAw==", "0", androidKeyCode = 7),
    NUM_1("AAAAAQAAAAEAAAAAAw==", "1", androidKeyCode = 8),
    NUM_2("AAAAAQAAAAEAAAABAw==", "2", androidKeyCode = 9),
    NUM_3("AAAAAQAAAAEAAAACAw==", "3", androidKeyCode = 10),
    NUM_4("AAAAAQAAAAEAAAADAw==", "4", androidKeyCode = 11),
    NUM_5("AAAAAQAAAAEAAAAEAw==", "5", androidKeyCode = 12),
    NUM_6("AAAAAQAAAAEAAAAFAw==", "6", androidKeyCode = 13),
    NUM_7("AAAAAQAAAAEAAAAGAw==", "7", androidKeyCode = 14),
    NUM_8("AAAAAQAAAAEAAAAHAw==", "8", androidKeyCode = 15),
    NUM_9("AAAAAQAAAAEAAAAIAw==", "9", androidKeyCode = 16),
    DOT("AAAAAgAAAJcAAAAmAw==", ".", androidKeyCode = 56);

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
