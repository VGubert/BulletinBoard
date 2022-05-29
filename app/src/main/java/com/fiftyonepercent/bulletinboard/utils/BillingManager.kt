package com.fiftyonepercent.bulletinboard.utils

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*

class BillingManager(val act: AppCompatActivity) {
    private var billingClient: BillingClient? = null

    init {
        setUpBillingClient()
    }

    private fun setUpBillingClient() {
        billingClient = BillingClient.newBuilder(act).setListener(getPurchaseListener()) //getPurchaseListener() следит за покупкой
            .enablePendingPurchases().build()
    }

    private fun savePurchase(isPurchased: Boolean) {
        val pref = act.getSharedPreferences(MAIN_PREF, Context.MODE_PRIVATE)
        var editor = pref.edit()
        editor.putBoolean(REMOVE_ADS_PREF, isPurchased)
        editor.apply()
    }

    fun startConnection() { //функ будет запускаться когда хотим реализовать покупку
        billingClient?.startConnection(object : BillingClientStateListener{
            override fun onBillingServiceDisconnected() {
            }

            override fun onBillingSetupFinished(result: BillingResult) {
                getItem()
            }

        })
    }

    private fun getItem() {
        val skuList = ArrayList<String>() //список с покупками
        skuList.add(REMOVE_ADS) //добавляем покупку
        val skuDetails = SkuDetailsParams.newBuilder() //инфа о покупке
        skuDetails.setSkusList(skuList).setType(BillingClient.SkuType.INAPP) //передаем что за покупки. Встроенные покупки
        billingClient?.querySkuDetailsAsync(skuDetails.build()){
                result, list ->
            run {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) { //если прошло все успешно
                    if(!list.isNullOrEmpty()) {
                        val billingFlowParams = BillingFlowParams
                            .newBuilder().setSkuDetails(list[0]).build()
                        billingClient?.launchBillingFlow(act, billingFlowParams) //выходит диалог о покупке
                    }
                }
            }
        }
    }

    private fun nonConsumableItem(purchase: Purchase) { //Функция для потверждения покупки
        if(purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if(!purchase.isAcknowledged) { //если покупка не потверждена
                val acParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken).build()
                billingClient?.acknowledgePurchase(acParams) { result ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        savePurchase(true)
                        Toast.makeText(act, "Спасибо за покупку!", Toast.LENGTH_SHORT).show()
                    } else {
                        savePurchase(false)
                        Toast.makeText(act, "Не удалось реализовать покупку", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun getPurchaseListener(): PurchasesUpdatedListener { //слушатель который слушает что происходит с покупками. То есть следить за состоянием что происходит
        return PurchasesUpdatedListener{
            result, list ->
            run {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) { //если прошло все успешно
                    list?.get(0)?.let{ nonConsumableItem(it)} //одна покупка позиция 0
                }
            }
        }
    }

    fun closeConnection() {
        billingClient?.endConnection()
    }

    companion object{
        const val REMOVE_ADS_PREF = "remove_ads_pef"
        const val REMOVE_ADS = "remove_ads"
        const val MAIN_PREF = "main_pref"
    }
}