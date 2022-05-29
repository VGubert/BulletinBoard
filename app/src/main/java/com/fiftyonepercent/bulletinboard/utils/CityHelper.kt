package com.fiftyonepercent.bulletinboard.utils

import android.content.Context
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList

object CityHelper {
    fun getAllCountries(context: Context): ArrayList<String> { //массив для получения стран. Context нужен чтобы добраться до файла
        var tempArray = ArrayList<String>() //создание массива
        try {
            val inputStream : InputStream = context.assets.open("countriesToCities.json") // InputStream получаем данные из различных источников
            val size:Int = inputStream.available() // получаем размер
            val bytesArray = ByteArray(size) // указываем размер
            inputStream.read(bytesArray) //записываем размер
            val jsonFile = String(bytesArray) // записываем в String. В этой переменной будет весь файл
            val jsonObject = JSONObject(jsonFile) // превращаем в json object
            val countriesNames = jsonObject.names() //здесь будут все названия стран
            if(countriesNames != null) { // Если перемн=енная не пустая, то запускаем цикл
                for (n in 0 until countriesNames.length()) {//Цикл от 0 до конца массива
                    tempArray.add(countriesNames.getString(n)) //добавляем все страны в перменную tempArray
                }
            }

        } catch (e:IOException) { //выдает ошибку если не удалось запустить

        }
        return tempArray
    }

    fun getAllCities(country: String, context: Context): ArrayList<String> { //массив для получения городов. Context нужен чтобы добраться до файла. Пердаем country для выюора города по стране
        var tempArray = ArrayList<String>() //создание массива
        try {
            val inputStream : InputStream = context.assets.open("countriesToCities.json") // InputStream получаем данные из различных источников
            val size:Int = inputStream.available() // получаем размер
            val bytesArray = ByteArray(size) // указываем размер
            inputStream.read(bytesArray) //записываем размер
            val jsonFile = String(bytesArray) // записываем в String. В этой переменной будет весь файл
            val jsonObject = JSONObject(jsonFile) // превращаем в json object
            val cityNames = jsonObject.getJSONArray(country) //здесь будет массив стран. По ним выбираем город

                for (n in 0 until cityNames.length()) {//Цикл от 0 до конца массива
                    tempArray.add(cityNames.getString(n)) //добавляем все города в перменную tempArray
                }

        } catch (e:IOException) { //выдает ошибку если не удалось запустить

        }
        return tempArray
    }

    fun filterListData(list: ArrayList<String>, searchText : String?) : ArrayList<String> { //функция для обновления списка
        val tempList = ArrayList<String>() //создаем новый временный массив
        tempList.clear() //очищаем список
        if(searchText == null) {
            tempList.add("No result")
            return tempList
        }
        for(selection: String in list) {
            if(selection.toLowerCase(Locale.ROOT).startsWith(searchText.toLowerCase())) //Проверяем совпадения по буква. .toLowerCase() все песатные буквы
                tempList.add(selection) //если совпадает то добавляем во временный массив
        }
        if(tempList.size == 0)tempList.add("No result")//если нет совпадений
        return tempList
    }
}