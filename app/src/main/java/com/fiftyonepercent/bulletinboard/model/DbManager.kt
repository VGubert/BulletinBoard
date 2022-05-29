package com.fiftyonepercent.bulletinboard.model

import com.fiftyonepercent.bulletinboard.utils.FilterManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class DbManager {
    val db = Firebase.database.getReference(MAIN_NODE) //получаем инстанцию базы данных. getReference создаем конкретный узел
    val dbStorage = Firebase.storage.getReference(MAIN_NODE) //получаем инстанцию базы данных. getReference создаем конкретный узел
    val auth = Firebase.auth // индетификатор пользователя

    fun publishAd(ad: Ad, finishListener: FinishWorkListener) {
        if (auth.uid != null) db.child(ad.key ?: "empty")
            .child(auth.uid!!).child(AD_NODE)
            .setValue(ad).addOnCompleteListener { //setValue() для сохранения данных по указанной ссылке, заменяя любые существующие данные по этому пути. child внутри узкла main будет еще один узел

                val adFilter = FilterManager.createFilter(ad) //например запишется Машины_23423552
                db.child(ad.key ?: "empty").child(FILTER_NODE)
                    .setValue(adFilter).addOnCompleteListener { //setValue() для сохранения данных по указанной ссылке, заменяя любые существующие данные по этому пути. child внутри узкла main будет еще один узел
                        finishListener.onFinish(it.isSuccessful) //запуститься на editActivityif(it.isSuccessful)
                    }
            }
    }

    fun adViewed(ad: Ad) { //Просмотры. Записываем один просмотр к данному объявлению
        var counter = ad.viewsCounter.toInt() //кол-во просмотров
        counter++ //увеличиваем просмотры
        if(auth.uid != null) db.child(ad.key ?: "empty")
            .child(INFO_NODE).setValue(InfoItem(counter.toString(), ad.emailsCounter, ad.callsCounter)) //записываем просмотры
    }

    fun onFavClick(ad: Ad, listener: FinishWorkListener) { //при нажатие на избранное
        if(ad.isFav) { //если объявление уже в избранных
            removeFromFavs(ad, listener) //удаляем из избранных
        } else {
            addToFavs(ad, listener) //удаляем из избранных
        }
    }

    private fun addToFavs(ad: Ad, listener: FinishWorkListener) { //добавить в избранное
        ad.key?.let { //если key не равен null что запуститься, если нет то не запуститься
            auth.uid?.let {
                    uid -> db.child(it).
            child(FAVS_NODE).
            child(uid).
            setValue(uid).addOnCompleteListener{
                if(it.isSuccessful) listener.onFinish(true)
            }
            }
        }
    }

    private fun removeFromFavs(ad: Ad, listener: FinishWorkListener) { //удаление из избранное
        ad.key?.let { //если key не равен null что запуститься, если нет то не запуститься
            auth.uid?.let {
                    uid -> db.child(it).
            child(FAVS_NODE).
            child(uid).
            removeValue().addOnCompleteListener{
                if(it.isSuccessful) listener.onFinish(true)
            }
            }
        }
    }

    fun getMyAds(readDataCallBack: ReadDataCallback?) { //Функция показа моих объявлений
        val query = db.orderByChild(auth.uid + "/ad/uid").equalTo(auth.uid) //указываем что будем фильтровать. auth.uid выдает индетификатор, дальше слово ad, и uid. equalTo(auth.uid) чтобы выдал все объявления на этом пути
        readDataFromDb(query, readDataCallBack)
    }

    fun getMyFavs(readDataCallBack: ReadDataCallback?) { //Фильтрация избранных
        val query = db.orderByChild( "/favs/${auth.uid}").equalTo(auth.uid) //указываем что будем фильтровать. /favs/$auth.uid добираемся до избранных
        readDataFromDb(query, readDataCallBack)
    }

    fun getAllAdsFirstPage(filter: String,readDataCallBack: ReadDataCallback?) { //Функция показа всех объявлений на первой стрпнице
        val query = if(filter.isEmpty()){ //если фильтр пустой
            db.orderByChild("/adFilter/time").limitToLast(ADS_LIMIT)//указываем что будем фильтровать. auth.uid выдает индетификатор, дальше слово ad, и uid. equalTo(auth.uid) чтобы выдал все объявления на этом пути
        } else{ //если фильтр не пустой
            getAllAdsByFilterFirstPage(filter) //Функция показа всех объявлений из выбраного фильтра
        }
        readDataFromDb(query, readDataCallBack)
    }

    fun getAllAdsByFilterFirstPage(tempFilter: String): Query { //Функция показа всех объявлений из выбраного фильтра
        val orderBy = tempFilter.split("|")[0] //название узла, в массиве у него 0 позиция
        val filter = tempFilter.split("|")[1] //сам фильтр у него позиция 1
        return db.orderByChild("/adFilter/$orderBy")
            .startAt(filter).endAt(filter + "_\uf8ff").limitToLast(ADS_LIMIT) //указываем что будем фильтровать. auth.uid выдает индетификатор, дальше слово ad, и uid. equalTo(auth.uid) чтобы выдал все объявления на этом пути
    }

    fun getAllAdsNextPage(time: String, filter: String, readDataCallBack: ReadDataCallback?) { //Функция показа всех объявлений когда скроллим
        if(filter.isEmpty()){ //если фильтр пустой
            val query = db.orderByChild("/adFilter/time").endBefore(time).limitToLast(ADS_LIMIT)//указываем что будем фильтровать. auth.uid выдает индетификатор, дальше слово ad, и uid. equalTo(auth.uid) чтобы выдал все объявления на этом пути
            readDataFromDb(query, readDataCallBack)
        } else{ //если фильтр не пустой
            getAllAdsByFilterNextPage(filter, time, readDataCallBack) //Функция показа всех объявлений из выбраного фильтра
        }
    }

    private fun getAllAdsByFilterNextPage(tempFilter: String, time: String, readDataCallBack: ReadDataCallback?) { //Функция показа всех объявлений из выбраного фильтра
        val orderBy = tempFilter.split("|")[0] //название узла, в массиве у него 0 позиция
        val filter = tempFilter.split("|")[1] //сам фильтр у него позиция 1
        val query = db.orderByChild("/adFilter/$orderBy")
            .endBefore(filter + "_$time").limitToLast(ADS_LIMIT) //указываем что будем фильтровать. auth.uid выдает индетификатор, дальше слово ad, и uid. equalTo(auth.uid) чтобы выдал все объявления на этом пути
        readNextPageFromDb(query, filter, orderBy, readDataCallBack)
    }


    fun getAllAdsFromCatFirstPage(cat: String, filter:String, readDataCallBack: ReadDataCallback?) { //Функция показа всех объявлений из категории
        val query = if(filter.isEmpty()) {
            db.orderByChild("/adFilter/cat_time")//указываем что будем фильтровать. auth.uid выдает индетификатор, дальше слово ad, и uid. equalTo(auth.uid) чтобы выдал все объявления на этом пути
                .startAt(cat).endAt(cat + "_\uf8ff").limitToLast(ADS_LIMIT)
        } else {
            getAllAdsFromCatByFilterFirstPage(cat, filter)
        }
        readDataFromDb(query, readDataCallBack)
    }

    fun getAllAdsFromCatByFilterFirstPage(cat: String, tempFilter: String): Query { //Функция показа всех объявлений какой-либо категории из выбраного фильтра
        val orderBy = "cat_" + tempFilter.split("|")[0] //название узла, в массиве у него 0 позиция
        val filter = cat + "_" + tempFilter.split("|")[1] //сам фильтр у него позиция 1
        return db.orderByChild("/adFilter/$orderBy")
            .startAt(filter).endAt(filter + "_\uf8ff").limitToLast(ADS_LIMIT) //указываем что будем фильтровать. auth.uid выдает индетификатор, дальше слово ad, и uid. equalTo(auth.uid) чтобы выдал все объявления на этом пути
    }

    fun getAllAdsFromCatNextPage(cat: String, time: String, filter: String, readDataCallBack: ReadDataCallback?) { //Функция показа всех объявлений из категории
        if(filter.isEmpty()){ //если фильтр пустой
            val query = db.orderByChild("/adFilter/cat_time")
                .endBefore(cat + "_" + time).limitToLast(ADS_LIMIT) //указываем что будем фильтровать. auth.uid выдает индетификатор, дальше слово ad, и uid. equalTo(auth.uid) чтобы выдал все объявления на этом пути
            readDataFromDb(query, readDataCallBack)
        } else { //если фиьтр выбран
            getAllAdsFromCatByFilterNextPage(cat, time, filter, readDataCallBack)
        }
    }

    private fun getAllAdsFromCatByFilterNextPage(cat: String, time: String, tempFilter: String, readDataCallBack: ReadDataCallback?) { //Функция показа всех объявлений какой-либо категории из выбраного фильтра
        val orderBy = "cat_" + tempFilter.split("|")[0] //название узла, в массиве у него 0 позиция
        val filter = cat + "_" + tempFilter.split("|")[1] //сам фильтр у него позиция 1
        val query = db.orderByChild("/adFilter/$orderBy")
            .endBefore(filter + "_" + time).limitToLast(ADS_LIMIT) //указываем что будем фильтровать. auth.uid выдает индетификатор, дальше слово ad, и uid. equalTo(auth.uid) чтобы выдал все объявления на этом пути
        readNextPageFromDb(query, filter, orderBy, readDataCallBack) //позволяет отсортировать не нежные объявления
    }


    fun deleteAd(ad: Ad, listener: FinishWorkListener) { //удаление объявления
        if(ad.key == null || ad.uid == null) return //если key или uid null то ничего не даем делать
        db.child(ad.key).child(ad.uid).removeValue().addOnCompleteListener{ //addOnCompleteListener чтобы узнать когда произошло удаление
            if(it.isSuccessful) listener.onFinish(true)
        }
    }

    private fun readDataFromDb(query: Query, readDataCallBack: ReadDataCallback?) { //считываем данные. Query фильтрует данные
        query.addListenerForSingleValueEvent(object: ValueEventListener{ //не будет обновляется в реальном времени, один раз считает. Будет обновляется, например при нажатии на кнопку обновления

            override fun onDataChange(snapshot: DataSnapshot) {
                val adArray = ArrayList<Ad>() //создаем массив
                for(item in snapshot.children) { //запускается цикл и берет по очереди объекты children (key)
                    var ad: Ad? = null
                    item.children.forEach { //пробегает внутри узла. их там два один из них info
                        if(ad == null) ad = it.child(AD_NODE).getValue(Ad::class.java) //добираемя до объявления. Превращаем все в класс
                    }
                    val infoItem = item.child(INFO_NODE).getValue(InfoItem::class.java) //добираемся до info

                    val favCounter = item.child(FAVS_NODE).childrenCount //добираемся до favs
                    val isFav = auth.uid?.let { item.child(FAVS_NODE).child(it).getValue(String::class.java) } //добираемся до нашего id в объявлениии если он там есть, то есть в избранных
                    ad?.isFav = isFav != null //если строкой выше что то записалось то это будет не null, то есть true, то есть запишется в избранное
                    ad?.favCounter = favCounter.toString() //счесчтик избранных

                    ad?.viewsCounter = infoItem?.viewsCounter ?: "0"
                    ad?.emailsCounter = infoItem?.emailsCounter ?: "0"
                    ad?.callsCounter = infoItem?.callsCounter ?: "0"
                    if(ad != null) adArray.add(ad!!) //добавляем одно объявление
                }
                readDataCallBack?.readData(adArray) //запускаем callBack который будет на MA
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    private fun readNextPageFromDb(query: Query, filter: String, orderBy: String, readDataCallBack: ReadDataCallback?) { //считываем данные. Query фильтрует данные
        query.addListenerForSingleValueEvent(object: ValueEventListener{ //не будет обновляется в реальном времени, один раз считает. Будет обновляется, например при нажатии на кнопку обновления

            override fun onDataChange(snapshot: DataSnapshot) {
                val adArray = ArrayList<Ad>() //создаем массив
                for(item in snapshot.children) { //запускается цикл и берет по очереди объекты children (key)
                    var ad: Ad? = null
                    item.children.forEach { //пробегает внутри узла. их там два один из них info
                        if(ad == null) ad = it.child(AD_NODE).getValue(Ad::class.java) //добираемя до объявления. Превращаем все в класс
                    }
                    val infoItem = item.child(INFO_NODE).getValue(InfoItem::class.java) //добираемся до info
                    val filterNodeValue = item.child(FILTER_NODE).child(orderBy).value.toString() //добираемся до filter node. Получаем полностью фильтр

                    val favCounter = item.child(FAVS_NODE).childrenCount //добираемся до favs
                    val isFav = auth.uid?.let { item.child(FAVS_NODE).child(it).getValue(String::class.java) } //добираемся до нашего id в объявлениии если он там есть, то есть в избранных
                    ad?.isFav = isFav != null //если строкой выше что то записалось то это будет не null, то есть true, то есть запишется в избранное
                    ad?.favCounter = favCounter.toString() //счесчтик избранных

                    ad?.viewsCounter = infoItem?.viewsCounter ?: "0"
                    ad?.emailsCounter = infoItem?.emailsCounter ?: "0"
                    ad?.callsCounter = infoItem?.callsCounter ?: "0"
                    if(ad != null &&  filterNodeValue.startsWith(filter))adArray.add(ad!!) //добавляем одно объявлениев массив
                }
                readDataCallBack?.readData(adArray) //запускаем callBack который будет на MA
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }
    interface ReadDataCallback { //считаем данные
        fun readData(list: ArrayList<Ad>)
    }
    interface FinishWorkListener { //при нажатии на кнопку опубликовать закрывается активити
        fun onFinish(isDone: Boolean)
    }

    companion object {
        const val AD_NODE = "ad"
        const val FILTER_NODE = "adFilter"
        const val INFO_NODE = "info"
        const val MAIN_NODE = "main"
        const val FAVS_NODE = "favs"
        const val ADS_LIMIT = 2
    }
}