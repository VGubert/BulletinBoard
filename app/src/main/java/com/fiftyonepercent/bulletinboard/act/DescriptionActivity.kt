package com.fiftyonepercent.bulletinboard.act

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.net.toUri
import androidx.viewpager2.widget.ViewPager2
import com.fiftyonepercent.bulletinboard.R
import com.fiftyonepercent.bulletinboard.adapters.ImageAdapter
import com.fiftyonepercent.bulletinboard.databinding.ActivityDescriptionBinding
import com.fiftyonepercent.bulletinboard.model.Ad
import com.fiftyonepercent.bulletinboard.utils.ImageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.internal.artificialFrame
import kotlinx.coroutines.launch

class DescriptionActivity : AppCompatActivity() {
    lateinit var binding: ActivityDescriptionBinding
    lateinit var adapter: ImageAdapter //иницилизируем адаптер
    private var ad: Ad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        binding.fbTel.setOnClickListener{ call() }//будет передавать номер телефона на приложение для звоноков
        binding.fbEmail.setOnClickListener{ sendEmail() } //будет передавать email на приложение для почты
    }

    private fun init() {
        adapter = ImageAdapter() //иницилилизируем адаптер
        binding.apply{
            viewPager.adapter = adapter //присваем viewPager адаптер
        }
        getIntentFromMainAct()
        imageChangeCounter()
    }
    private fun getIntentFromMainAct() { //функция которая будет получать сссылки и класс ad, картинки
        ad = intent.getSerializableExtra(AD) as Ad // получить как класс Ad
        if(ad != null) updateUI(ad!!)
    }

    private fun updateUI(ad: Ad) {
        ImageManager.fillImageArray(ad, adapter)
        fillTextViews(ad)
    }

    private fun fillTextViews(ad: Ad) = with(binding) { //функция которая будет получать сссылки и класс ad, текст
        tvTitle.text = ad.title
        tvDescription.text = ad.description
        tvEmail.text = ad.email
        tvPrice.text = ad.price
        tvTel.text = ad.tel
        tvCountry.text = ad.country
        tvCity.text = ad.city
        tvIndex.text = ad.index
        tvWithSend.text = isWithSent(ad.withSent.toBoolean())
    }

    private fun isWithSent(withSent: Boolean): String {
        return if(withSent) resources.getString(R.string.yes) else resources.getString(R.string.no)
    }

    private fun call() { //функция для звонка
        val callUri = "tel:${ad?.tel}" //предаем телефон из класса ad
        val iCall = Intent(Intent.ACTION_DIAL) //куда будем отправлять
        iCall.data = callUri.toUri() //передаем туда данные
        startActivity(iCall)
    }

    private fun sendEmail() { //отправляем письмо на email
        val iSendEmail = Intent(Intent.ACTION_SEND) //куда будем отправлять
        iSendEmail.type = "message/rfc822"
        iSendEmail.apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(ad?.email)) //чтобы уже email был заполнел
            putExtra(Intent.EXTRA_SUBJECT, "Объявление")
            putExtra(Intent.EXTRA_TEXT, "Меня интересует ваше объявление") //чтобы уже email был заполнел
        }
        try { //если нет приложения для отправки письма
            startActivity(Intent.createChooser(iSendEmail, "Октрыть с"))
        } catch (e: ActivityNotFoundException) {

        }
    }
    
    private fun imageChangeCounter() { //измение счетчика
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){ //OnPageChangeCallback() будет запускаться каждый раз когда скроллим viewPager
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val imageCounter = "${position + 1}/${binding.viewPager.adapter?.itemCount}" //к 0 прибавляем картинку
                binding.tvImageCounter.text = imageCounter
            }
        })
    }

    companion object {
        const val AD = "AD" //конст для класса AD
    }
}