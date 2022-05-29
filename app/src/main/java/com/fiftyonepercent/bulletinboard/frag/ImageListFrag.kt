package com.fiftyonepercent.bulletinboard.frag

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.get
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.fiftyonepercent.bulletinboard.R
import com.fiftyonepercent.bulletinboard.act.EditAdsAct
import com.fiftyonepercent.bulletinboard.databinding.ListImageFragBinding
import com.fiftyonepercent.bulletinboard.dialoghelper.ProgressDialog
import com.fiftyonepercent.bulletinboard.utils.AdapterCallback
import com.fiftyonepercent.bulletinboard.utils.ImageManager
import com.fiftyonepercent.bulletinboard.utils.ImagePicker
import com.fiftyonepercent.bulletinboard.utils.ItemTouchMoveCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ImageListFrag(private val fragCloseInterface: FragmentCloseInterface) : BaseAdsFrag(), AdapterCallback { //передаем fragCloseInterface: FragmentCloseInterface чтобы закрыть фрагмент
    val adapter = SelectImageRvAdapter(this) //создаем адапетер
    val dragCallback = ItemTouchMoveCallback(adapter) //иницилизируем класс. передаем adapter(SelectImageRvAdapter) Чтобы его исп в ItemTouchMoveCallback
    private var job: Job? = null //Работа, для корутин
    private var addImageItem: MenuItem?  = null
    val touchHelper = ItemTouchHelper(dragCallback ) // ItemTouchHelper() для перетаскивания картинок
    lateinit var binding: ListImageFragBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ListImageFragBinding.inflate(layoutInflater, container, false)
        adView = binding.adView //реклама баннер
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar() //добавляем меню
        binding.apply {
            touchHelper.attachToRecyclerView(rcViewSelectImage) //подключаем touchHelper к rcView
            rcViewSelectImage.layoutManager = LinearLayoutManager(activity) //присваем layoutManager
            rcViewSelectImage.adapter = adapter //в rcView присваеваем адаптер

        }
    }

    override fun onItemDelete() {
        addImageItem?.isVisible = true //когда удалем элемент, когда было 3 элемента, по при удалении одной картинки показываем кнопку добавить картинку
    }

    fun updateAdapterFromEdit(bitmapList: List<Bitmap>) { //обновляем адаптер
        adapter.updateAdapter(bitmapList, true)
    }

    override fun onDetach() { //отсоединяется от Активити. закрываем фрагмент
        super.onDetach()

    }

    override fun onClose() { //Закрытие рекламы
        super.onClose()
        activity?.supportFragmentManager?.beginTransaction()?.remove(this@ImageListFrag)?.commit() // закроется фрагмент
        fragCloseInterface.onFragClose(adapter.mainArray) //запуститься в EditAdsActivity
        job?.cancel() //если я вышел из фрагмента то и операции которые там происходили завершились
    }

    fun resizeSelectedImage(newList: ArrayList<Uri>, needClear: Boolean,activity: Activity ) {

        job = CoroutineScope(Dispatchers.Main).launch { //запускаем корутину потому что imageResize suspend функция. Но корутина запускается на основном потоке. А задачи запускаются на второстепенном
            val dialog = ProgressDialog.createProgressDialog(activity) //добавляем прогресс диалог
            val bitmapList = ImageManager.imageResize(newList, activity) //suspend потому что сначала получаю картинки потом обновляю адаптер
            dialog.dismiss() //когда закончилась обработка картинок закрываем прогресс бар
            adapter.updateAdapter(bitmapList, needClear) //обновляем адаптер. Передаем новый список уже заполненный
            if(adapter.mainArray.size > 2) addImageItem?.isVisible = false //если в адаптере уже есть 3 картинки, то скрываем кнопку чтобы не добавить еще одну
        }
    }

    private fun setUpToolbar() { //функция для подключения toolbar

        binding.apply {
            tb.inflateMenu(R.menu.menu_choose_image) //создаем меню
            val deleteItem = tb.menu.findItem(R.id.id_delete_image) //находим кнопку delete
            addImageItem = tb.menu.findItem(R.id.id_add_image) //находим кнопку add image
            if(adapter.mainArray.size > 2) addImageItem?.isVisible = false //если в адаптере уже есть 3 картинки, то скрываем кнопку чтобы не добавить еще одну
            tb.setNavigationOnClickListener { //Слушатель на кнопку Home

                showInterAd() //показываем рекламу
            }

            deleteItem.setOnMenuItemClickListener { //слушатель нажатий на кнопку delete
                adapter.updateAdapter(ArrayList(), true) //передаем пустой список
                addImageItem?.isVisible =
                    true //когда удалем элемент, когда было 3 элемента, по при удалении одной картинки показываем кнопку добавить картинку
                true
            }
            addImageItem?.setOnMenuItemClickListener { //слушатель нажатий на кнопку добавить фото
                val imageCount =
                    ImagePicker.MAX_IMAGE_COUNT - adapter.mainArray.size //кол-во картинок в данный момент - максимальное число картинок(3)
                ImagePicker.addImages(activity as EditAdsAct, imageCount) //аередаем лаучер .добавляем картинку
                true
            }
        }
    }

    fun updateAdapter(newList: ArrayList<Uri>, activity: Activity){
        resizeSelectedImage(newList, false, activity) } //обновляем адаптер

    fun setSingleImage(uri: Uri, pos : Int){ //функция при редактировании одной картинки
        val pBar = binding.rcViewSelectImage[pos].findViewById<ProgressBar>(R.id.pBar) //находим прогресс бар
        job = CoroutineScope(Dispatchers.Main).launch { //запускаем корутину потому что imageResize suspend функция. Но корутина запускается на основном потоке. А задачи запускаются на второстепенном
            pBar.visibility = View.VISIBLE //делаем видимым прогресс бар
            val bitmapList = ImageManager.imageResize(arrayListOf(uri), activity as Activity) //suspend потому что сначала получаю картинки потом обновляю адаптер.(listOf(uri) массив с одним элементом
            pBar.visibility = View.GONE //убираем прогресс бар когда закончится операция
            adapter.mainArray[pos] = bitmapList[0] //указываем позицию которую хотим презаписать и ссылку которую хотим перезаписать
            adapter.notifyItemChanged(pos) //применяем изменения по конкретной позиции
        }

    }


}