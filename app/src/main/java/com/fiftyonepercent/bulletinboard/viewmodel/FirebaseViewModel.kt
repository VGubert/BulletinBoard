package com.fiftyonepercent.bulletinboard.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fiftyonepercent.bulletinboard.model.Ad
import com.fiftyonepercent.bulletinboard.model.DbManager

class FirebaseViewModel: ViewModel() {
    private val dbManager = DbManager() //Иницилизируем класс DbManager
    val liveAdsData = MutableLiveData<ArrayList<Ad>>() //это посредник он будет обновлять View когда есть изменения. В нем будем хранить список объявления
    fun loadAllAdsFirstPage(filter: String) { //будет считывать объявления
        dbManager.getAllAdsFirstPage(filter, object: DbManager.ReadDataCallback{ //считываем данные и передаем итерфейс
            override fun readData(list: ArrayList<Ad>) { //используем дынне с интерфейса
                liveAdsData.value = list //передаем данные
            }

        })
    }

    fun loadAllAdsNextPage(time: String, filter: String) { //будет считывать объявления
        dbManager.getAllAdsNextPage(time, filter, object: DbManager.ReadDataCallback{ //считываем данные и передаем итерфейс
            override fun readData(list: ArrayList<Ad>) { //используем дынне с интерфейса
                liveAdsData.value = list //передаем данные
            }

        })
    }

    fun loadAllAdsFromCat(cat: String, filter: String) { //будет считывать объявления
        dbManager.getAllAdsFromCatFirstPage(cat, filter, object: DbManager.ReadDataCallback{ //считываем данные и передаем итерфейс
            override fun readData(list: ArrayList<Ad>) { //используем дынне с интерфейса
                liveAdsData.value = list //передаем данные
            }

        })
    }

    fun loadAllAdsFromCatNextPage(cat: String, time: String, filter: String) { //будет считывать объявления
        dbManager.getAllAdsFromCatNextPage(cat, time, filter, object: DbManager.ReadDataCallback{ //считываем данные и передаем итерфейс
            override fun readData(list: ArrayList<Ad>) { //используем дынне с интерфейса
                liveAdsData.value = list //передаем данные
            }

        })
    }

    fun onFavClick(ad: Ad) { //при нажатие на кнопку избранное
        dbManager.onFavClick(ad, object: DbManager.FinishWorkListener{
            override fun onFinish(isDone: Boolean) {
                val updatedList = liveAdsData.value //берем список с нашими объявлениями
                val pos = updatedList?.indexOf(ad) //объявление по позиции
                if(pos != -1) { //если позиция не равно -1
                    pos?.let {
                        val favCounter = if(ad.isFav) ad.favCounter.toInt() -1 else ad.favCounter.toInt() +1 //отнимаем 1 если нажали на кнопку и оно было избранным, теперь станет не избранным
                        updatedList[pos] = updatedList[pos].copy(isFav = !ad.isFav, favCounter = favCounter.toString()) //!ad.isFav - это значит что меняем значение на противоположное, если было true то делаем false. updatedList[pos].copy(isFav скопировать все а is fav изменить
                    }

                }
                liveAdsData.postValue(updatedList) //данные изменяться и алдаптер тоже обновиться
            }
        })
    }

    fun adViewed(ad: Ad){ //просмотры
        dbManager.adViewed(ad)
    }

    fun loadMyAds() { //будет считывать объявления
        dbManager.getMyAds(object: DbManager.ReadDataCallback{ //считываем данные и передаем итерфейс
            override fun readData(list: ArrayList<Ad>) { //используем дынне с интерфейса
                liveAdsData.value = list //передаем данные
            }

        })
    }

    fun loadMyFavs() {
        dbManager.getMyFavs(object: DbManager.ReadDataCallback{ //считываем данные и передаем итерфейс
            override fun readData(list: ArrayList<Ad>) { //используем дынне с интерфейса
                liveAdsData.value = list //передаем данные
            }

        })
    }

    fun deleteItem(ad: Ad) { //функция удаления
        dbManager.deleteAd(ad, object: DbManager.FinishWorkListener{
            override fun onFinish(isDone: Boolean) {
                val updatedList = liveAdsData.value //берем список с нашими объявлениями
                updatedList?.remove(ad) //удаляем из адаптера элемент
                liveAdsData.postValue(updatedList) //данные изменяться и алдаптер тоже обновиться
            }

        })
    }
}
