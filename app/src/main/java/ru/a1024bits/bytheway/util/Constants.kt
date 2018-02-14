package ru.a1024bits.bytheway.util

/**
 * Created by tikhon.osipov on 26.11.17
 */
object Constants {
    val APP_PREFERENCES = "string_save"
    val FIRST_ENTER = "first_enter_in_app"
    val ACCESS_TOKEN = "access_t"
    val REFRESH_TOKEN = "refresh_t"
    val TYPE_TOKEN = "type_t"
    var PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_FROM = 1
    var PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_TO = 2
    val PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_FROM_MIDDLE_CITY: Int = 3
    val PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_TO_NEW_CITY: Int = 4
    val ERROR: Int = -1
    val SUCCESS: Int = 1
    val LAST_INDEX_CITY = "last_city"
    val TWO_INDEX_CITY = "two_city"
    val FIRST_INDEX_CITY = "first_city"
    val START_DATE = "start_date"
    val END_DATE = "end_date"
    val TWO_DATE = "two_date"
    const val FCM_SRV = "notify_service"
    const val FCM_TOKEN = "token"
    const val FCM_CMD_UPDATE = "fcm_token_update"
    const val FCM_CMD_SHOW_USER = "show_user"
}