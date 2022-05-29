package com.fiftyonepercent.bulletinboard.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fiftyonepercent.bulletinboard.R
import com.fiftyonepercent.bulletinboard.utils.CityHelper

class DialogSpinnerHelper {

    fun showSpinnerDialog(context: Context, list:ArrayList<String>, tvSelection: TextView) { //Пердаем context с EditActivity и список
        val builder = AlertDialog.Builder(context) //для создания диалога
        val dialog = builder.create() //создали диалог
        val rootView = LayoutInflater.from(context).inflate(R.layout.spinner_layout, null) //создаем разметку
        val adapter = RcViewDialogSpinnerAdapter(tvSelection, dialog) //Создаем чтобы использовать адаптер
        val rcView = rootView.findViewById<RecyclerView>(R.id.rcSpView) //ищем список
        val sv = rootView.findViewById<SearchView>(R.id.svSpinner) //ищем searchView
        rcView.layoutManager = LinearLayoutManager(context) //как он будет выглядить
        rcView.adapter = adapter //говорим списку какой адаптер будем использовать
        dialog.setView(rootView) //пердаем view в диалог
        adapter.updateAdapter(list) // в адаптер передаем список из CityHelper
        setSearchView(adapter, list, sv) //запускаем фун
        dialog.show()

    }

    private fun setSearchView(adapter: RcViewDialogSpinnerAdapter, list: ArrayList<String>, sv: SearchView?) { // функция для поиска
        sv?.setOnQueryTextListener(object: SearchView.OnQueryTextListener{  // setOnQueryTextListener слушатель изменения текста
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val tempList = CityHelper.filterListData(list, newText) //Фильтруются данные. И возращает где есть совпадения с этими буквами
                adapter.updateAdapter(tempList) //предаем в адаптер новым списком
                return true
            }
        })

    }

}