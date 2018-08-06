package com.minosai.oneclick.util.service

import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.support.annotation.RequiresApi
import android.util.Log
import com.minosai.oneclick.R
import com.minosai.oneclick.util.service.WebService.Companion.RequestType
import com.minosai.oneclick.util.helper.Constants
import com.minosai.oneclick.util.helper.LoginLogoutBroadcastHelper
import com.minosai.oneclick.util.receiver.LoginLogoutReceiver
import com.minosai.oneclick.util.receiver.listener.WifiConnectivityListener
import com.minosai.oneclick.util.receiver.WifiReceiver
import com.minosai.oneclick.util.receiver.listener.LoginLogoutListener
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker
import com.treebo.internetavailabilitychecker.InternetConnectivityListener
import dagger.android.AndroidInjection
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.N)
class OneClickTileService :TileService(),
        InternetConnectivityListener,
        WifiConnectivityListener,
        LoginLogoutListener {

    val TAG = javaClass.simpleName ?: Constants.PACKAGE_NAME

    @Inject
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var webService: WebService

    private lateinit var wifiReceiver: WifiReceiver
    private lateinit var loginLogoutReceiver: LoginLogoutReceiver
    private lateinit var mInternetAvailabilityChecker:InternetAvailabilityChecker

    private var isWifiConnected = false
    private var isOnline = false

    override fun onCreate() {
        super.onCreate()

        AndroidInjection.inject(this)
        InternetAvailabilityChecker.init(this)

        wifiReceiver = WifiReceiver(this)
//        loginLogoutReceiver = LoginLogoutReceiver(this)

        registerWifiReceiver()
//        registerLoginLogoutReceiver()
    }

    override fun onStartListening() {
        super.onStartListening()
        mInternetAvailabilityChecker = InternetAvailabilityChecker.getInstance()
        mInternetAvailabilityChecker.addInternetConnectivityListener(this)
    }

    override fun onClick() {
//        var type = ""
//        when(qsTile.state) {
//            Tile.STATE_ACTIVE -> type = "LOGOUT"
//            Tile.STATE_INACTIVE -> type = "LOGIN"
//        }
//        LoginLogoutBroadcastHelper.sendLoginLogoutBroadcast(this, type)
//        updateState()

        when(qsTile.state) {
            Tile.STATE_INACTIVE -> webService.login(this)
            Tile.STATE_ACTIVE -> webService.logout(this)
        }
    }

    override fun onStopListening() {
        mInternetAvailabilityChecker.removeInternetConnectivityChangeListener(this)
        super.onStopListening()
    }

    override fun onDestroy() {
        unregisterWifiReceiver()
//        unregisterLoginLogoutReceiver()
        super.onDestroy()
    }


    private fun registerWifiReceiver() {
        try {
            val intentFilter = IntentFilter()
            with(intentFilter) {
                addAction("android.net.wifi.WIFI_SATE_CHANGED")
                addAction("android.net.conn.CONNECTIVITY_CHANGE")
                addAction("android.net.wifi.supplicant.CONNECTION_CHANGE")
                addAction("android.net.wifi.STATE_CHANGE")
            }
            registerReceiver(wifiReceiver, intentFilter)
            Log.d(TAG, "WifiReceiver registered")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "WifiReceiver already registered")
        }
    }

    private fun unregisterWifiReceiver() {
        try {
            unregisterReceiver(wifiReceiver)
        } catch (e: Exception) {
            // already unregistered
        }
    }

    private fun registerLoginLogoutReceiver() {
        try {
            val intentFilter = IntentFilter()
            intentFilter.addAction(LoginLogoutBroadcastHelper.LOGIN_LOGOUT_ACTION)
            registerReceiver(loginLogoutReceiver, intentFilter)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun unregisterLoginLogoutReceiver() {
        try {
            unregisterReceiver(loginLogoutReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onInternetConnectivityChanged(isConnected: Boolean) {
        isOnline = isConnected
        updateState()
    }

    override fun onWifiStateChanged(isConnectedToWifi: Boolean, ssid: String) {
        isWifiConnected = isConnectedToWifi
        updateState()
    }

    override fun onLoggedListener(requestType: RequestType, isLogged: Boolean) {
        isOnline = isLogged
        when(requestType) {
            RequestType.LOGIN -> {
                isOnline = isLogged
            }
            RequestType.LOGOUT -> {
                if (isOnline && isLogged) {
                    isOnline = false
                }
            }
        }
        updateState()
    }


    private fun updateState() {
        if (isWifiConnected) {
            if (isOnline) changeStateLogout() else changeStateLogin()
        } else {
            changeStateDisable()
        }
    }

    private fun changeStateDisable() = with(qsTile) {
        state = Tile.STATE_UNAVAILABLE
        label = "One click"
        updateTile()
    }

    private fun changeStateLogin() = with(qsTile) {
        state = Tile.STATE_INACTIVE
        icon = Icon.createWithResource(this@OneClickTileService, R.drawable.ic_login)
        label = "Login"
        updateTile()
    }

    private fun changeStateLogout() = with(qsTile) {
        state = Tile.STATE_ACTIVE
        icon = Icon.createWithResource(this@OneClickTileService, R.drawable.ic_logout)
        label = "Logout"
        updateTile()
    }
}
