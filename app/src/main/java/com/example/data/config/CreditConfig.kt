package com.example.data.config

object CreditConfig {
    // Feature costs (Central configuration)
    const val CHAT = 0
    const val RESEARCH = 0
    const val PDF_ANALYSIS = 3
    const val IMAGE_GENERATION = 5
    const val VIDEO_GENERATION = 20
    const val MUSIC_GENERATION = 10
    const val VOICE_GENERATION = 5
    const val TEXT_TO_SPEECH = 2

    // Pricing & Code Info
    const val PRICE_RUPEES = 10
    const val CREDITS_PER_CODE = 50
    const val INITIAL_USER_BALANCE = 50
    const val WHATSAPP_CHANNEL_URL = "https://whatsapp.com/channel/0029Vb9ByYZ9MF8slYWjrv1o"

    // DAKU AI Credit Code Format Specification
    // Format: [CAPITAL LETTER][a]DAKU[SPECIAL SYMBOL][5 DIGITS]=RG
    // Example: KaDAKU@12345=RG
    val ALLOWED_SPECIAL_SYMBOLS = listOf('@', '#', '₹', '&')
    val CODE_REGEX = Regex("^[A-Z]aDAKU[@#₹&]\\d{5}=RG$")

    /**
     * Validates the 5 structural conditions of a DAKU AI Credit Code:
     * 1. Must contain exact word "DAKU"
     * 2. Must contain exactly ONE special symbol from allowed symbols (@, #, ₹, &)
     * 3. Must contain one uppercase letter at position 0, then lowercase 'a' at position 1
     * 4. Must contain EXACTLY 5 numeric digits
     * 5. Must end with exact suffix "=RG"
     */
    fun isCodeFormatValid(code: String): Boolean {
        val trimmed = code.trim()
        if (trimmed.length != 15) return false
        if (!trimmed.contains("DAKU")) return false
        if (!trimmed.endsWith("=RG")) return false
        return CODE_REGEX.matches(trimmed)
    }
}

