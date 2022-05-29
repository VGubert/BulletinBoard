package com.fiftyonepercent.bulletinboard.act

import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.fiftyonepercent.bulletinboard.MainActivity
import com.fiftyonepercent.bulletinboard.R
import com.fiftyonepercent.bulletinboard.adapters.ImageAdapter
import com.fiftyonepercent.bulletinboard.model.Ad
import com.fiftyonepercent.bulletinboard.model.DbManager
import com.fiftyonepercent.bulletinboard.databinding.ActivityEditAdsBinding
import com.fiftyonepercent.bulletinboard.dialogs.DialogSpinnerHelper
import com.fiftyonepercent.bulletinboard.frag.FragmentCloseInterface
import com.fiftyonepercent.bulletinboard.frag.ImageListFrag
import com.fiftyonepercent.bulletinboard.utils.CityHelper
import com.fiftyonepercent.bulletinboard.utils.ImageManager
import com.fiftyonepercent.bulletinboard.utils.ImagePicker
import com.google.android.gms.tasks.OnCompleteListener
import java.io.ByteArrayOutputStream

class EditAdsAct : AppCompatActivity(), FragmentCloseInterface {
    var chooseImageFrag : ImageListFrag? = null //Он null значит этот фрагмент еще не создали
    lateinit var binding: ActivityEditAdsBinding
    private val dialog = DialogSpinnerHelper()
    lateinit var imageAdapter: ImageAdapter
    private val dbManager = DbManager() //иницилизируем класс DbManager().
    var editImagePos = 0 //позиция чтобы редактировать фото
    private var imageIndex = 0 //индекс фото
    private var isEditState = false //для редактирования объявления
    private var ad: Ad? = null //если null то это создание, если нет то редактирование

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        checkEditState()
        imageChangeCounter()
    }

    private fun checkEditState(){ //проверяет состояние
        isEditState = isEditState() //если переменная равна функции, которая true
        if(isEditState()) { //если editState true //если true то это редактирование фото
            ad = (intent.getSerializableExtra(MainActivity.ADS_DATA) as Ad)
            if(ad != null) fillViews(ad!!) //принимаем класс Ad
        }
    }

    private fun isEditState(): Boolean { //будет проверять для редактирования или создания нового объявления
        return intent.getBooleanExtra(MainActivity.EDIT_STATE, false)
    }

    private fun fillViews(ad: Ad) = with(binding)  { //Функция в которой будем заполнять view
        tvCountry.text = ad.country
        tvCity.text = ad.city
        editTel.setText(ad.tel)
        edIndex.setText(ad.index)
        checkBoxWithSend.isChecked = ad.withSent.toBoolean()
        tvCat.text = ad.category
        edTitle.setText(ad.title)
        edPrice.setText(ad.price)
        edDescription.setText(ad.description)
        updateImageCounter(0)
        ImageManager.fillImageArray(ad, imageAdapter)
    }

    private fun init() {
        imageAdapter = ImageAdapter() //иницилизировали класс ImageAdapter
        binding.vpImages.adapter = imageAdapter //подключаем vpImages к адаптеру
    }

    //OnClicks
    fun onClickSelectCountry(view:View) { //выбор страны
        val listCountry = CityHelper.getAllCountries(this) //Создаем спиннер для прокручивания списка
        dialog.showSpinnerDialog(this, listCountry, binding.tvCountry) // запускаем диалог
        if(binding.tvCity.text.toString() == getString(R.string.select_city)) {// если знач по умолчанию значит город не был выбран
            binding.tvCity.text = getString(R.string.select_city)
        }
    }

    fun onClickSelectCity(view:View) { //выбор города
        val selectedCountry = binding.tvCountry.text.toString()
        if (selectedCountry != getString(R.string.select_country)) { // select_country значение по умолчание. если значение не по умолчанию то запуститься код
            val listCity = CityHelper.getAllCities(selectedCountry, this) //Создаем спиннер для прокручивания списка
            dialog.showSpinnerDialog(this, listCity, binding.tvCity) // запускаем диалог
        } else {
            Toast.makeText(this, "No country selected ", Toast.LENGTH_LONG).show()
        }
    }

    fun onClickSelectCat(view:View) { //выбор категории

            val listCity = resources.getStringArray(R.array.category).toMutableList() as ArrayList //берем массив.toMutableList() превращаем массив в список
            dialog.showSpinnerDialog(this, listCity, binding.tvCat) // запускаем диалог
    }

    fun onClickGetImages(view:View) { //Будем запускать ImagePicker
        if (imageAdapter.mainArray.size == 0) { //если imageAdapter не будет картинок
            ImagePicker.getMultiImages(this,3) //запускаем лаунчер. передаем максиму 3 фото
        } else {

            openChooseImageFrag(null) //добавляем изображения если в адаптере они есть
            chooseImageFrag?.updateAdapterFromEdit(imageAdapter.mainArray)
        }
    }

    fun onClickPublish(view: View) { //слушатель нажатий  для записи в бд
        if(isFieldsEmpty()) { //проверяем заполнены ли все поля
            showToast("Все поля должны быть заполнены!")
            return
        }
        binding.progressLayout.visibility = View.VISIBLE //делаем видимым прогресс бар
        ad = fillAd()
            uploadImages() //загружаем картинки
        }

    private fun isFieldsEmpty(): Boolean = with(binding) {
        return tvCountry.text.toString() == getString(R.string.select_country) //поля страны должно быть заполнены
                || tvCity.text.toString() == getString(R.string.select_city)
                || tvCat.text.toString() == getString(R.string.select_category)
                || edTitle.text.isEmpty()
                || edPrice.text.isEmpty()
                || edIndex.text.isEmpty()
                || edDescription.text.isEmpty()
                || editTel.text.isEmpty()
    }

    private fun onPublishFinish(): DbManager.FinishWorkListener { //функция закрытия активити при нажатии на кнопку опубликовать
        return object: DbManager.FinishWorkListener{
            override fun onFinish(isDone: Boolean) {
                binding.progressLayout.visibility = View.GONE
                if(isDone)finish()//закрываем активти
            }
        }
    }

    private fun fillAd() : Ad { //следит за разными состояниями
        val adTemp: Ad
        binding.apply {
            adTemp = Ad(tvCountry.text.toString(),
                tvCity.text.toString(),
                editTel.text.toString(),
                edIndex.text.toString(),
                checkBoxWithSend.isChecked.toString(),
                tvCat.text.toString(),
                edTitle.text.toString(),
                edPrice.text.toString(),
                edDescription.text.toString(),
                editEmail.text.toString(),
                ad?.mainImage?:"empty",
                ad?.image2?:"empty", //если нет никого фото то записуем
                ad?.image3?:"empty",
                ad?.key?: dbManager.db.push().key, "0",
                dbManager.auth.uid,
                ad?.time?: System.currentTimeMillis().toString() //генерируем случайный ключ и берем uid
            ) //иницилизируем класс Ad и берем элементы из activity_edit_ads
        }
        return adTemp
    }

    override fun onFragClose(list : ArrayList<Bitmap>) { //когда пользователь выбрал картинки, нажал готово и возращается на экран
        binding.scrollViewMain.visibility = View.VISIBLE
        imageAdapter.update(list)
        chooseImageFrag = null //когда возращаемся очищаем фрагмент. Потому что при повторном добавлении фрагмент не открывается
        updateImageCounter(binding.vpImages.currentItem) //обновялем счетчик
    }
    fun openChooseImageFrag(newList : ArrayList<Uri>?) { //при выборе изображения во фрагменте

        chooseImageFrag = ImageListFrag(this) //ссылка на фрагмент
        if(newList != null) chooseImageFrag?.resizeSelectedImage(newList, true, this) //перезаписуем фрагмент с картинками
        binding.scrollViewMain.visibility = View.GONE //убираем scrollView чтобы он ен прекрывал фрагмент
        val fn = supportFragmentManager.beginTransaction() //Создаем фрагмент
        fn.replace(R.id.place_holder, chooseImageFrag!!) //заменяем place_holder на фрагмент
        fn.commit() //применяем настройки. Запуститься фрагмент
    }

    private fun uploadImages() {
        if(imageIndex == 3) { //если нету картинок. Запускается 3 раза
            dbManager.publishAd(ad!!, onPublishFinish()) //Добавляем функцию publishAd(). и в ней срабатывает функция заполнения данных
            return
        }
        val oldUrl = getUrlFromAd()
        if(imageAdapter.mainArray.size > imageIndex) {

            val byteArray = prepareImageByteArray(imageAdapter.mainArray[imageIndex])
            if(oldUrl.startsWith("http")) { //проверяем была сслыка или нет
                updateImage(byteArray, oldUrl) { //на место старой ссылки запускаем новую
                    nextImage(it.result.toString())
                }
            }else { //если ничего ранее не было записано
                uploadImage(byteArray) {
                    //dbManager.publishAd(ad!!, onPublishFinish()) //Добавляем функцию publishAd(). и в ней срабатывает функция заполнения данных
                    nextImage(it.result.toString())
                }
            }

        } else {
            if(oldUrl.startsWith("http")) { //когда удалили, осталась ссылка, и на ее место записуем новую картинку
                deleteImageByUrl(oldUrl) {
                    nextImage("empty")
                }
            } else {
                nextImage("empty")
            }
        }
    }
    private fun nextImage(uri: String) { //увеличивание индекса
        setImageUriToAd(uri) //выбираем картинку по позиции
        imageIndex++ //увеличиваем индекс на 1
        uploadImages()
    }

    private fun setImageUriToAd(uri: String) {
        when(imageIndex) { //если imageIndex на 0 позиции то это главное фото
            0 -> ad = ad?.copy(mainImage = uri) //копируем mainImage с класса ad
            1 -> ad = ad?.copy(image2 = uri) //копируем image2 с класса ad
            2 -> ad = ad?.copy(image3 = uri) //копируем image3 с класса ad
        }
    }

    private fun getUrlFromAd(): String{ //берем картинки которые были в ad
        return listOf(ad?.mainImage!!, ad?.image2!!, ad?.image3!!)[imageIndex] //каждый раз когда счетсчик запускается берет с разной позиции ссылку
    }
    private fun prepareImageByteArray(bitmap: Bitmap): ByteArray { //превращаем картинки в ByteArray
        val outStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outStream) //сжимаем картинку в формат jpeg. Compress сжимает все в outStream
        return outStream.toByteArray()
    }
    private fun uploadImage(byteArray: ByteArray, listener: OnCompleteListener<Uri>) { //загружаем картнку
        val imStorageRef = dbManager.dbStorage
            .child(dbManager.auth.uid!!)
            .child("image_${System.currentTimeMillis()}")//ссылка на место где хотим сохранить картинку. System.currentTimeMillis() время узел для картинок,
        val upTask = imStorageRef.putBytes(byteArray) // byteArray запишется на этот путь imStorageRef
        upTask.continueWithTask{ //когда все загрузится нам выдаст ссылку с firebase storage
            task -> imStorageRef.downloadUrl //скачиваем ссылку картинки которую загрузили
        }.addOnCompleteListener(listener)
    }

    private fun deleteImageByUrl(oldUrl: String, listener: OnCompleteListener<Void>) { //для удаления
        dbManager.dbStorage.storage.getReferenceFromUrl(oldUrl).delete().addOnCompleteListener(listener) //удаление
    }

    private fun updateImage(byteArray: ByteArray, url: String, listener: OnCompleteListener<Uri>) { //обновляем картнку
        val imStorageRef = dbManager.dbStorage.storage.getReferenceFromUrl(url)
        val upTask = imStorageRef.putBytes(byteArray) // byteArray запишется на этот путь imStorageRef
        upTask.continueWithTask{ //когда все загрузится нам выдаст ссылку с firebase storage
                task -> imStorageRef.downloadUrl //скачиваем ссылку картинки которую загрузили
        }.addOnCompleteListener(listener)
    }

    private fun imageChangeCounter() { //измение счетчика
        binding.vpImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){ //OnPageChangeCallback() будет запускаться каждый раз когда скроллим viewPager
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateImageCounter(position)
            }
        })
    }

    private fun updateImageCounter(counter: Int) { //обновление счетчика
        var index = 1
        val itemCount = binding.vpImages.adapter?.itemCount
        if(itemCount == 0) index = 0
        val imageCounter = "${counter + index}/$itemCount" //к 0 прибавляем картинку
        binding.tvImageCounter.text = imageCounter
    }
}
