package ru.a1024bits.bytheway.model

/**
 * Created by x220 on 25.11.2017.
 */
class AccessToken {

    val accessToken: String? = null
    private var tokenType: String? = null

    fun getTokenType(): String {
        // OAuth requires uppercase Authorization HTTP header value for token type
        if (!Character.isUpperCase(tokenType!![0])) {
            tokenType = Character
                    .toString(tokenType!![0])
                    .toUpperCase() + tokenType!!.substring(1)
        }

        return tokenType as String
    }
}