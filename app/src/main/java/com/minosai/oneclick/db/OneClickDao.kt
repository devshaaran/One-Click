package com.minosai.oneclick.db

import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.minosai.oneclick.model.AccountInfo

@Dao
interface OneClickDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAccount(accountInfo: AccountInfo)

    @Query("SELECT * FROM accountinfo")
    fun getAllAccounts(): DataSource.Factory<Int, AccountInfo>

    @Query("SELECT * FROM accountinfo WHERE isActiveAccount= 1")
    fun getActiveAccount(): AccountInfo

    @Query("UPDATE accountinfo SET isActiveAccount = 0 WHERE 1=1")
    fun resetAccounts()

    @Query("UPDATE accountinfo SET isActiveAccount = 1 WHERE username LIKE :userName")
    fun setActiveAccount(userName: String)

    @Query("SELECT * FROM accountinfo WHERE username LIKE :userName")
    fun getAccoutByUserName(userName: String): LiveData<AccountInfo>

    @Query("UPDATE accountinfo SET usage=:usage WHERE  username LIKE :userName")
    fun updateUsage(userName: String, usage: String)

    @Query("UPDATE accountinfo SET password=:password WHERE username LIKE :userName")
    fun updatePassword(userName: String, password: String)

    @Query("UPDATE accountinfo SET renewaldate=:date WHERE username LIKE :userName")
    fun updateRenewalDate(userName: String, date: String)
}