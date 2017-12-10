package ru.a1024bits.bytheway.model

import com.google.gson.annotations.SerializedName

/**
 * Created by x220 on 25.11.2017.
 */
class AccessToken {
    @SerializedName("access_token")
    var accessToken: String? = null
    @SerializedName("refresh_token")
    var refresToken: String? = null
    @SerializedName("token_type")
    private var tokenType: String? = null

    fun getTokenType(): String {
        // OAuth requires uppercase Authorization HTTP header value for token type
        if (!Character.isUpperCase(tokenType.orEmpty()[0])) {
            tokenType = Character
                    .toString(tokenType.orEmpty()[0])
                    .toUpperCase() + tokenType?.substring(1)
        }

        return tokenType as String
    }
}