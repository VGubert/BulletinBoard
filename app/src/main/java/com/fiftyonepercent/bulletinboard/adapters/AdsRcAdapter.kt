package com.fiftyonepercent.bulletinboard.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fiftyonepercent.bulletinboard.MainActivity
import com.fiftyonepercent.bulletinboard.R
import com.fiftyonepercent.bulletinboard.act.DescriptionActivity
import com.fiftyonepercent.bulletinboard.act.EditAdsAct
import com.fiftyonepercent.bulletinboard.model.Ad
import com.fiftyonepercent.bulletinboard.databinding.AdListItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AdsRcAdapter(val act: MainActivity) : RecyclerView.Adapter<AdsRcAdapter.AdHolder>() {
    val adArray = ArrayList<Ad>() //массив из объвлений. Массив нужен чтобы хранить там объявления
    private var timeFormatter: SimpleDateFormat? = null

    init {
        timeFormatter = SimpleDateFormat("dd/MM/yyyy - hh:mm", Locale.getDefault()) //формат времени
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdHolder { //функция запускается для каждого объявления. Рисует разметку на экране
        val binding = AdListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false) //надуваем разметку
        return AdHolder(binding, act, timeFormatter!!) //Сохраняем все в AdHolder
    }

    override fun onBindViewHolder(holder: AdHolder, position: Int) { //заполняет то что было ранее нарисовано
        holder.setData(adArray[position]) //берем с массив позицию на которой сейчас рисуется объявление
    }

    override fun getItemCount(): Int {
        return adArray.size //берет размер массива
    }

    fun updateAdapter(newList: List<Ad>) { //будем сюда предавать из MA то что считали из бд
        val tempArray = ArrayList<Ad>() //создаем новый список
        tempArray.addAll(adArray) //добавляем старый список
        tempArray.addAll(newList) //добавляем новый список
        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(adArray, tempArray)) //calculateDiff будет вычеслять что нужно с элементами сделать и какую функцию применить
        diffResult.dispatchUpdatesTo(this) //dispatchUpdatesTo применить все обновления которые высчитали выше
        adArray.clear() //очищаем массив
        adArray.addAll(tempArray) //добавляем новый список, который содержит новые и старые элементы
    }

    fun updateAdapterWithClear(newList: List<Ad>) { //будем сюда предавать из MA то что считали из бд
        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(adArray, newList)) //calculateDiff будет вычеслять что нужно с элементами сделать и какую функцию применить
        diffResult.dispatchUpdatesTo(this) //dispatchUpdatesTo применить все обновления которые высчитали выше
        adArray.clear() //очищаем массив
        adArray.addAll(newList) //добавляем новый список,
    }

    class AdHolder(val binding: AdListItemBinding,val act: MainActivity, val formatter: SimpleDateFormat) : RecyclerView.ViewHolder(binding.root) { //этот класс для одинарного объявления. Данные в этом классе будет в каждом объявлении

        fun setData(ad: Ad) = with(binding) {
                tvDescription.text = ad.description
                tvPrice.text = ad.price
                tvTitle.text = ad.title
                tvViewCounter.text = ad.viewsCounter
                tvFavCounter.text = ad.favCounter //заполянем счетсчик
                val publishTime =  act.resources.getString(R.string.time_publish) + getTimeFromMillis(ad.time)
                tvPublishTime.text = publishTime //добавляем время публикации
                Picasso.get().load(ad.mainImage).into(mainImage) //заполняет картинку в адаптере
                isFav(ad) //Функция для избранного
                showEditPanel(isOwner(ad)) //функция показа панели, если  владелец объявления или нет
                mainOnClick(ad)

        }

        private fun getTimeFromMillis(timeMillis: String): String { //превращаем милли в реальное время
            val c = Calendar.getInstance()
            c.timeInMillis = timeMillis.toLong()
            return formatter.format(c.time)
        }

        private fun mainOnClick (ad: Ad) = with(binding) {
            ibFav.setOnClickListener{
                if(act.mAuth.currentUser?.isAnonymous == false) act.onFavClicked(ad) //если вошли как гость то не работвет кнопка избранное
            }
            itemView.setOnClickListener { //функция чтобы засчитывать просмотры
                act.onAdViewed(ad) //уходит на MA с помощью интерфейса
            }
            ibEditAd.setOnClickListener(onClickEdit(ad)) //редактирует данные
            ibDeleteAd.setOnClickListener{
                act.onDeleteItem(ad) //при нажатии на кнопку удаляется объявление, с помощью интерфейса который сработает на MA
            }
        }

        private fun isFav(ad: Ad) { //функция для избранного
            if(ad.isFav) { //если сердечко нажато
                binding.ibFav.setImageResource(R.drawable.ic_fav_pressed)
            } else { //если не нажато
                binding.ibFav.setImageResource(R.drawable.ic_fav_normal)
            }
        }

        private fun onClickEdit(ad: Ad): View.OnClickListener { //функция при нажатие на редактирование
            return View.OnClickListener {
                val editIntent = Intent(act, EditAdsAct::class.java).apply {//act активити на которм сейчас нахожусь это MA, EditAdsAct акт которое хочу открыть
                    putExtra(MainActivity.EDIT_STATE, true) //по константе в MA будем ждать редактирования
                    putExtra(MainActivity.ADS_DATA, ad) //по константе в MA передаем данные для редактирования
                }
                act.startActivity(editIntent) //запускаем с помощью активити и отправляем интент
            }
        }

        private fun isOwner(ad: Ad): Boolean { // функция для владельца аккаунта
            return ad.uid == act.mAuth.uid //индетификатор объявления будет равен индетификатору аккаунта в который зашли
        }
        private fun showEditPanel(isOwner: Boolean) {
            if(isOwner) { //Если индетификатор объявления будет равен индетификатору аккаунта в который зашли
                binding.editPanel.visibility = View.VISIBLE //показываем панель
            } else {
                binding.editPanel.visibility = View.GONE //убираем панель
            }
        }
    }
    interface Listener{ //интерфейс для удаления и просмотров
        fun onDeleteItem(ad: Ad)
        fun onAdViewed(ad: Ad)
        fun onFavClicked(ad: Ad)
    }
}