package com.example.ledger.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

object AuthSession {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isVip = MutableStateFlow(false)
    val isVip: StateFlow<Boolean> = _isVip.asStateFlow()

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    private val _userId = MutableStateFlow("")
    val userId: StateFlow<String> = _userId.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()

    private val _vipExpireAt = MutableStateFlow<Long?>(null)
    val vipExpireAt: StateFlow<Long?> = _vipExpireAt.asStateFlow()

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        scope.launch {
            val prefs = context.applicationContext.dataStore.data.first()
            val savedToken = prefs[KEY_TOKEN]
            val savedIsVip = prefs[KEY_IS_VIP] ?: false
            val savedUserId = prefs[KEY_USER_ID] ?: ""
            _lastSyncTime.value = prefs[KEY_LAST_SYNC_TIME]
            _vipExpireAt.value = prefs[KEY_VIP_EXPIRE_AT]
            if (!savedToken.isNullOrBlank()) {
                _token.value = savedToken
                _isVip.value = savedIsVip
                _userId.value = savedUserId
                _isLoggedIn.value = true
            }
        }
    }

    fun login(token: String, isVip: Boolean, userId: String, vipExpireAt: Long?) {
        _token.value = token
        _isVip.value = isVip
        _userId.value = userId
        _vipExpireAt.value = vipExpireAt
        _isLoggedIn.value = true
        persist()
    }

    fun logout() {
        _token.value = null
        _isVip.value = false
        _userId.value = ""
        _lastSyncTime.value = null
        _vipExpireAt.value = null
        _isLoggedIn.value = false
        persist()
    }

    fun updateVipStatus(isVip: Boolean) {
        _isVip.value = isVip
        persist()
    }

    fun updateVipWithExpiry(isVip: Boolean, vipExpireAt: Long?) {
        _isVip.value = isVip
        _vipExpireAt.value = vipExpireAt
        persist()
    }

    private val _syncEvent = MutableStateFlow<String?>(null)
    val syncEvent: StateFlow<String?> = _syncEvent.asStateFlow()

    fun publishSyncEvent(message: String) { _syncEvent.value = message }
    fun clearSyncEvent() { _syncEvent.value = null }

    fun isPermanentVip(): Boolean {
        val exp = _vipExpireAt.value ?: return false
        return exp - System.currentTimeMillis() > 50L * 365 * 24 * 3600 * 1000
    }

    fun updateLastSyncTime(time: Long) {
        _lastSyncTime.value = time
        persist()
    }

    private fun persist() {
        val ctx = appContext ?: return
        scope.launch {
            ctx.dataStore.edit { prefs ->
                prefs[KEY_TOKEN] = _token.value ?: ""
                prefs[KEY_IS_VIP] = _isVip.value
                prefs[KEY_USER_ID] = _userId.value
                _lastSyncTime.value?.let { prefs[KEY_LAST_SYNC_TIME] = it }
                    ?: prefs.remove(KEY_LAST_SYNC_TIME)
                _vipExpireAt.value?.let { prefs[KEY_VIP_EXPIRE_AT] = it }
                    ?: prefs.remove(KEY_VIP_EXPIRE_AT)
            }
        }
    }

    private val KEY_TOKEN = stringPreferencesKey("auth_token")
    private val KEY_IS_VIP = booleanPreferencesKey("is_vip")
    private val KEY_USER_ID = stringPreferencesKey("user_id")
    private val KEY_LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
    private val KEY_VIP_EXPIRE_AT = longPreferencesKey("vip_expire_at")
}
