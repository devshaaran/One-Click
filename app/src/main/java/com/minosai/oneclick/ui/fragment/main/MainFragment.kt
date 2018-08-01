package com.minosai.oneclick.ui.fragment.main

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.minosai.oneclick.R
import com.minosai.oneclick.di.Injectable
import com.minosai.oneclick.model.AccountInfo
import com.minosai.oneclick.util.service.WebService
import com.minosai.oneclick.util.helper.Constants
import com.minosai.oneclick.util.helper.LoginLogoutBroadcastHelper
import com.minosai.oneclick.util.receiver.listener.LoginLogoutListener
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject

class MainFragment : Fragment(), Injectable, LoginLogoutListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var webService: WebService

    lateinit var mainViewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)

        text_home_displayname.text = "Hello, ${mainViewModel.displayName}"
//        text_home_username.text = preferences.getString(Constants.PREF_USERNAME, "")

//        mainViewModel.getLiveActiveAccount().observe(this, Observer { info ->
//            info?.let {
//                text_home_username.text = it.username
//                text_home_usage.text = it.usage
//            }
//        })

        mainViewModel.getAllAccounts().observe(this, Observer { allAccounts ->
            updateUi(allAccounts ?: listOf())
        })

//        input_userid.setText(preferences.getString(Constants.PREF_USERNAME, ""))
//        input_password.setText(preferences.getString(Constants.PREF_PASSWORD, ""))

        button_login.setOnClickListener {
            val userName = input_userid.text.toString()
            val password = input_password.text.toString()
            webService.login(this)
            saveUser(userName,  password)
        }

        button_logout.setOnClickListener { webService.logout(this) }
    }

    private fun updateUi(accounts: List<AccountInfo>) {
        accounts.forEach { info ->
            if (info.isActiveAccount) {
                with(info) {
                    text_home_username.text = username
                    text_home_usage.text = usage
                    text_home_usage.text = usage
                }
            }
        }
    }

    private fun saveUser(userName: String, password: String) {
        preferences.edit()
                .putString(Constants.PREF_USERNAME, userName)
                .putString(Constants.PREF_PASSWORD, password)
                .apply()
    }

    override fun onLoggedListener(requestType: WebService.Companion.RequestType, isLogged: Boolean) {

    }

}