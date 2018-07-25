package com.minosai.oneclick.util.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.wifi.WifiManager
import android.net.NetworkInfo
import android.util.Log
import android.widget.Toast
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.minosai.oneclick.util.helper.Constants
import com.minosai.oneclick.util.receiver.listener.WifiConnectivityListener
import com.minosai.oneclick.util.service.WebService
import dagger.android.AndroidInjection
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject


class WifiReceiver() : BroadcastReceiver() {

    constructor(wifiConnectivityListener: WifiConnectivityListener): this() {
        this.wifiConnectivityListener = wifiConnectivityListener
    }

    @Inject
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var webService: WebService

    private val TAG = javaClass.simpleName ?: Constants.PACKAGE_NAME

    companion object {
        private val SSID_LIST = listOf("\"VIT2.4G\"", "\"VIT5G\"", "\"VOLSBB\"")
    }

    private var wifiConnectivityListener: WifiConnectivityListener? = null

    override fun onReceive(context: Context?, intent: Intent?) {

        AndroidInjection.inject(this, context)

        intent?.let { intent ->

            if (intent.action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION, true)) {

                val info = intent.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)
                Log.i(TAG, "isConnected : ${info.isConnected} to network: ${info.extraInfo}")

                if (info.isConnected && info.extraInfo in SSID_LIST) {
                    wifiConnectivityListener?.onWifiStateChanged(true)
                    //TODO: auto login based on user preference
                } else {
                    wifiConnectivityListener?.onWifiStateChanged(false)
                }
            }
        }
    }
}