package com.fiftyonepercent.bulletinboard.act

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.fiftyonepercent.bulletinboard.R
import com.fiftyonepercent.bulletinboard.databinding.ActivityFilterBinding
import com.fiftyonepercent.bulletinboard.dialogs.DialogSpinnerHelper
import com.fiftyonepercent.bulletinboard.utils.CityHelper

class FilterActivity : AppCompatActivity() {
    lateinit var binding: ActivityFilterBinding
    private val dialog = DialogSpinnerHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onClickSelectCountry()
        onClickSelectCity()
        onClickDone()
        onClickClear()
        actionBarSettings()
        getFilter()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) finish() //когда нажали на стрелочку назад закрываем активити
        return super.onOptionsItemSelected(item)
    }

    private fun getFilter() = with(binding) { //получаем фильтр
        val filter = intent.getStringExtra(FILTER_KEY) //если у нас есть фильтр то запуститься ниже
        if(filter != null && filter != "empty") {
            val filterArray = filter.split("_") //превращаем все в массив
            if(filterArray[0] != "empty")  tvCountry.text = filterArray[0] //если не чего не выбрали то значит и не чего не показывали
            if(filterArray[1] != "empty")  tvCity.text = filterArray[1] //если не чего не выбрали то значит и не чего не показывали
            if(filterArray[2] != "empty") edIndex.setText(filterArray[2])  //если не чего не выбрали то значит и не чего не показывали
            checkBoxWithSend.isChecked = filterArray[3].toBoolean()
        }
    }

     private fun onClickSelectCountry() = with(binding) { //выбор страны
        tvCountry.setOnClickListener{ //при нажатие на выбор страны
            val listCountry = CityHelper.getAllCountries(this@FilterActivity) //Создаем спиннер для прокручивания списка
            dialog.showSpinnerDialog(this@FilterActivity, listCountry,tvCountry) // запускаем диалог
            if(tvCity.text.toString() == getString(R.string.select_city)) {// если знач по умолчанию значит город не был выбран
                tvCity.text = getString(R.string.select_city)
            }
        }

    }

    private fun onClickSelectCity() = with(binding) { //выбор города
        tvCity.setOnClickListener {
            val selectedCountry = binding.tvCountry.text.toString()
            if (selectedCountry != getString(R.string.select_country)) { // select_country значение по умолчание. если значение не по умолчанию то запуститься код
                val listCity = CityHelper.getAllCities(selectedCountry, this@FilterActivity) //Создаем спиннер для прокручивания списка
                dialog.showSpinnerDialog(this@FilterActivity, listCity, tvCity) // запускаем диалог
            } else {
                Toast.makeText(this@FilterActivity, "No country selected ", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onClickDone() = with(binding) { //функция при нажаьтие применить фильтр
        btDone.setOnClickListener {
            val i = Intent().apply {
                putExtra(FILTER_KEY, createFilter()) //Создали фильтр и поместили в intent
            }
            setResult(RESULT_OK, i) //возвращаем результат
            finish()
        }
    }

    private fun onClickClear() = with(binding) { //функция при нажатие очистить фильтр
        btClear.setOnClickListener {
            tvCountry.text = getString(R.string.select_country)
            tvCity.text = getString(R.string.select_city)
            edIndex.setText("")
            checkBoxWithSend.isChecked = false
            setResult(RESULT_CANCELED) //очищаем фильтр когда применили
        }
    }

    private fun createFilter(): String = with(binding) { //функция для применения фильтра
        val sBuilder = StringBuilder() //класс который собирает все в один string
        val arrayTempFilter = listOf(
            tvCountry.text,
            tvCity.text,
            edIndex.text,
            checkBoxWithSend.isChecked.toString() //временный массив
        )
        for ((i, s) in arrayTempFilter.withIndex()) { //в s по очереди будут записываться элементы, в i будет записываться id позиции
            if (s != getString(R.string.select_country) && s != getString(R.string.select_city) && s.isNotEmpty()) { //если что то не выбрано, значит не добавляем в фильтр
                sBuilder.append(s) //если все выше выбрано s передаем в sBuilder
            if (i != arrayTempFilter.size - 1) sBuilder.append("_") //если элемент не последний то добавляем _, иначе он последний и ничего не добавляем
        } else { //если не выбрано записываем empty
                sBuilder.append("empty") //если все выше выбрано s передаем в sBuilder
                if (i != arrayTempFilter.size - 1) sBuilder.append("_") //если элемент не последний то добавляем _, иначе он последний и ничего не добавляем
        }
    }
        return sBuilder.toString() //выдает собранный String
    }

    fun actionBarSettings() {
        val ab = supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true) //добавляет стрелочку назад

    }
    companion object{
        const val FILTER_KEY = "filter_key"
    }
}