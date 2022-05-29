package com.fiftyonepercent.bulletinboard.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fiftyonepercent.bulletinboard.R
import com.fiftyonepercent.bulletinboard.act.EditAdsAct

class RcViewDialogSpinnerAdapter(var tvSelection: TextView, var dialog:AlertDialog) : RecyclerView.Adapter<RcViewDialogSpinnerAdapter.SpViewHolder>() {
    private val mainList = ArrayList<String>() //создаем массив. Заполняем в функции updateAdapter


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpViewHolder { //Рисуем элемент
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sp_list_item, parent, false) //надуваем разметку. #1 - рисуется View
        return SpViewHolder(view, tvSelection, dialog) // каждый раз когда рисуется список используем этот класс с разметкой. #2 view передает SpViewHolder, теперь он хранит ссылки tvSpItem
    }

    override fun onBindViewHolder(holder: SpViewHolder, position: Int) { // Подключаем текст. #4 После того как нарисовалась onCreateViewHolder. Он возращает holder: SpViewHolder. Внутри Holdera есть функции напр. setData
        holder.setData(mainList[position]) //с помощью функции setData указывает значение
    }

    override fun getItemCount(): Int { // Узнаем сколько элементов нужно нарисовать
        return mainList.size //нарисует столько элементов сколько есть в mainList
    }

    class SpViewHolder(itemView: View, var tvSelection: TextView, var dialog: AlertDialog) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var itemText = ""
        fun setData(text: String){ //Будем обновлять
            val tvSpItem = itemView.findViewById<TextView>(R.id.tvSpItem) //каждый отдельный элемент будет передаваться в ViewHolder. Будет столько ViewHolder сколько жлементов
            tvSpItem.text = text // пишем текст
            itemText = text //записуют текст
            itemView.setOnClickListener(this) //при нажатии на кнопку будет запускаться функция onClick
        }

        override fun onClick(v: View?) { //при нажатии на TextView выберите город
            tvSelection.text = itemText // Для доступа к root элементам
            dialog.dismiss()
        }
    }
    fun updateAdapter(list: ArrayList<String>) {
        mainList.clear() //очищаем список если там что то было
        mainList.addAll(list) //передаем все что было в списке
        notifyDataSetChanged() //говорим нашему адаптеру что данные изменились
    }
}