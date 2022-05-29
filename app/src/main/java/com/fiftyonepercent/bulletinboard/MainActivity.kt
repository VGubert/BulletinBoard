package com.fiftyonepercent.bulletinboard

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fiftyonepercent.bulletinboard.accounthelper.AccountHelper
import com.fiftyonepercent.bulletinboard.act.DescriptionActivity
import com.fiftyonepercent.bulletinboard.act.EditAdsAct
import com.fiftyonepercent.bulletinboard.act.FilterActivity
import com.fiftyonepercent.bulletinboard.adapters.AdsRcAdapter
import com.fiftyonepercent.bulletinboard.databinding.ActivityMainBinding
import com.fiftyonepercent.bulletinboard.dialoghelper.DialogConst
import com.fiftyonepercent.bulletinboard.dialoghelper.DialogHelper
import com.fiftyonepercent.bulletinboard.model.Ad
import com.fiftyonepercent.bulletinboard.utils.AppMainState
import com.fiftyonepercent.bulletinboard.utils.BillingManager
import com.fiftyonepercent.bulletinboard.utils.FilterManager
import com.fiftyonepercent.bulletinboard.viewmodel.FirebaseViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, AdsRcAdapter.Listener {
    private lateinit var tvAccount: TextView // переменная для изменения значений Header
    private lateinit var imAccount: ImageView // переменная для изменения значений Header
    private lateinit var binding: ActivityMainBinding // объявляем переменную которая будет типом ActivityMainBinding(activity_main). Хранит разметук в виде класса
    private val dialogHelper = DialogHelper(this) // создаем переменную и присваеваем класс DialogHelper. this(пердаем MA)
    val mAuth = Firebase.auth
    val adapter = AdsRcAdapter(this) //иницилизируем адаптер
    lateinit var googleSignInLauncher:ActivityResultLauncher<Intent>
    lateinit var filterLauncher:ActivityResultLauncher<Intent>
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    private var clearUpdate: Boolean = true
    private var currentCategory: String? = null
    private var filter: String = "empty"
    private var filterDb: String = "" //фильтр для DbManager
    private var pref: SharedPreferences? = null
    private var isPremiumUser = false
    private var bManager: BillingManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater) //раздувает разметку
        setContentView(binding.root) // рисует разметку
        pref = getSharedPreferences(BillingManager.MAIN_PREF, MODE_PRIVATE)
        isPremiumUser = pref?.getBoolean(BillingManager.REMOVE_ADS_PREF, false)!!
        if(!isPremiumUser) { //если не премиум пользователь
                (application as AppMainState).showAdIfAvailable(this) { //запускаем рекламу
                //запускаем диалгои которые идут после рекламы, например диалог для регистрации
            }
            initAds()
        } else {
            binding.mainContent.adView2.visibility = View.GONE //убираем рекламу
        }
        initAds()
        init()
        initRecyclerView()
        initViewModel()
        bottomMenuOnClick()
        scrollListener()
        onActivityResultFilter()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.id_filter) {
            val i = Intent(this@MainActivity, FilterActivity::class.java).apply { //создаем intent
                putExtra(FilterActivity.FILTER_KEY,filter) //помещаем сюда фильтр
            }
            filterLauncher.launch(i)
        } //переходим на класс для фильтрации
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        binding.mainContent.bNavView.selectedItemId = R.id.id_home //когда обратно возращаемся с становится на кнопку home
        binding.mainContent.adView2.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.mainContent.adView2.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mainContent.adView2.pause()
        bManager?.closeConnection()
    }

    private fun initAds(){ //реклама
        MobileAds.initialize(this) //иницилизация
        val adRequest = AdRequest.Builder().build()
        binding.mainContent.adView2.loadAd(adRequest) //загружаем рекламу
    }

    private fun onActivityResult() { //ждем результата с filter act
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data) // когда выбираем аккаунт передаем акка
            try { //что бы не было ошибок

                val account = task.getResult(ApiException::class.java) //Берем результат. ApiException слежу за ошибками
                if (account != null){ //Проверяем на пустоты
                    dialogHelper.accHelper.signInFirebaseWithGoogle(account.idToken!!)

                }

            }catch (e:ApiException){
                Log.d("MyLog", "Api error : ${e.message}")
            }
        }
    }

    private fun onActivityResultFilter() {
        filterLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == RESULT_OK) { //если прислали данные
                filter = it.data?.getStringExtra(FilterActivity.FILTER_KEY)!!
                //Log.d("MyLog", "Filter: $filter")
                //Log.d("MyLog", "getFilter: ${FilterManager.getFilter(filter)}")
                filterDb = FilterManager.getFilter(filter)
            } else if(it.resultCode == RESULT_CANCELED){ //иначе очищаем фильтр
                filterDb = ""
                filter = "empty"
            }
        }
    }

    override fun onStart() {
        super.onStart()
        uiUpdate(mAuth.currentUser) //при запуске запускаем функуцию чтобы показать фунцию
    }

    private fun initViewModel(){
        firebaseViewModel.liveAdsData.observe(this) {
            val list = getAdsByCategory(it)
            if(!clearUpdate){ //если будет false
                adapter.updateAdapter(list) //значит не очищаем список, а добавляем новый элемент
            } else { //иначе очищаем список
                adapter.updateAdapterWithClear(list)
            }
            binding.mainContent.tvEmpty.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE //если список пустой то показываем надпись "Пусто", иначе убираем
        } //здесь будет наблюдать когда будут изменния
    }

    private fun getAdsByCategory(list: ArrayList<Ad>): ArrayList<Ad> {
        val tempList = ArrayList<Ad>() //создаем временный список
        tempList.addAll(list) //во временный список загружаем все объявления
        if(currentCategory != getString(R.string.def)) { //если это не категория разное
            tempList.clear() //стираем список
            list.forEach{
                if(currentCategory == it.category) tempList.add(it) //добавляем объявление из определенной категории
            }
        }
        tempList.reverse() //переворачиваем список который разное
        return tempList
    }

    private fun init(){ //обработчик нажатий
        currentCategory = getString(R.string.def)
        setSupportActionBar(binding.mainContent.toolbar) //встроенный Toolbar делаем основным, чтобы помещать туда элементы
        onActivityResult()
        navViewSettings()
        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.mainContent.toolbar,R.string.open, R.string.close) //добавление кнопки(три полоски)
        binding.drawerLayout.addDrawerListener(toggle) //Значит что, DrawerLayout будет открываться при нажатии на кнопку
        toggle.syncState() //Добавление кнопки для вызова меню
        binding.navView.setNavigationItemSelectedListener (this) //Означает что NavigationView будет передавать события в этот класс (this)MainActivity
        tvAccount = binding.navView.getHeaderView(0).findViewById(R.id.tvAccountEmail) // Получаем доступ к Header(аватарка и email). 0 потому что он один. И получаю доступ к TextView
        imAccount = binding.navView.getHeaderView(0).findViewById(R.id.imAccountImage) // Получаем доступ к Header(аватарка и email). 0 потому что он один. И получаю доступ к ImageView
    }

    private fun bottomMenuOnClick() = with(binding) { //слушатель нажатий на bNavView
        mainContent.bNavView.setOnNavigationItemSelectedListener { item-> //слушатель нажатий
            clearUpdate = true
            when(item.itemId) {
                R.id.id_new_ad -> {
                    val i = Intent(this@MainActivity, EditAdsAct::class.java) //указываем на какое активи хотим перейти
                    startActivity(i) //при нажатии на кнопку запускается новое активити
                }
                R.id.id_my_ads -> {
                    firebaseViewModel.loadMyAds()//показывает мои объявления
                    mainContent.toolbar.title = getString(R.string.ad_my_ads) //меняет заголовок
                }
                R.id.id_favs -> {
                    firebaseViewModel.loadMyFavs()
                }
                R.id.id_home -> {
                    currentCategory = getString(R.string.def)
                    firebaseViewModel.loadAllAdsFirstPage(filterDb) //показывает все объявления
                    mainContent.toolbar.title = getString(R.string.def) //меняет заголовок
                }

            }
            true
        }
    }

    private fun initRecyclerView() { //будем иницилизировать RecyclerView
        binding.apply {
            mainContent.rcView.layoutManager = LinearLayoutManager(this@MainActivity)
            mainContent.rcView.adapter = adapter //передаем адаптер
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean { //будет принимать нажатия на любой элемент из меню
        clearUpdate = true //когда нажимаем на категорию старый список очищается и добавляется новый списоек категории
        when(item.itemId) { //берем по id
            R.id.id_my_ads->{

            }
            R.id.id_car->{
                getAdsFromCat(getString(R.string.ad_car))
            }
            R.id.id_pc->{
                getAdsFromCat(getString(R.string.ad_pc))
            }
            R.id.id_smart->{
                getAdsFromCat(getString(R.string.ad_smartphone))
            }
            R.id.id_dn->{
                getAdsFromCat(getString(R.string.ad_dn))
            }

            R.id.id_remove_ads->{
                bManager = BillingManager(this)
                bManager?.startConnection()
            }
            R.id.id_sign_up->{
                dialogHelper.createSignDialog(DialogConst.SIGN_UP_STATE) //передаем класс DialogHelper, из которого запускаем функцию createSignDialog, которая создает диалог. Используем константу которая пойдет в условный оператор
            }
            R.id.id_sign_in->{
                dialogHelper.createSignDialog(DialogConst.SIGN_IN_STATE)

            }
            R.id.id_sign_out->{
                if(mAuth.currentUser?.isAnonymous == true){ //если mAuth.currentUser?.isAnonymous то false
                    binding.drawerLayout.closeDrawer(GravityCompat.START) //закрывем DrawerLayout
                    return true //если анонимный пользователь то дальше не пройдет, то не будет создваться новый
                }
                uiUpdate(null)
                mAuth.signOut() //выход из аккаунта
                dialogHelper.accHelper.signOutG()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START) //закрывем DrawerLayout
            return true
        }

    private fun getAdsFromCat(cat: String) {
        currentCategory = cat
        firebaseViewModel.loadAllAdsFromCat(cat, filterDb) //передаем категорию и время
    }
    fun uiUpdate(user: FirebaseUser?) { // с функции будем брать email и записывать в Header
        if(user == null) { // если текст пустой, если не зарегистрировался
            dialogHelper.accHelper.signInAnonymously(object : AccountHelper.Listener { //заходим как гость
                override fun onComplete() {
                    tvAccount.text = resources.getString(R.string.anonimus)
                    imAccount.setImageResource(R.drawable.ic_account_def) //картинка по умолчанию
                }
            })
        } else if(user.isAnonymous){ //если вышел и зашел в приложение то показываем как анонимный гость
            tvAccount.text = resources.getString(R.string.anonimus)
            imAccount.setImageResource(R.drawable.ic_account_def) //картинка по умолчанию
        } else if(!user.isAnonymous) { //если зашли как нормальный пользователь
            tvAccount.text = user.email
            Picasso.get().load(user.photoUrl).into(imAccount) //картинка от аккаунта
        }
    }

    override fun onDeleteItem(ad: Ad) { //функция из интерфеса который в адаптере для удаления
        firebaseViewModel.deleteItem(ad)
    }

    override fun onAdViewed(ad: Ad) { //функция из интерфеса который в адаптере для просмотров
        firebaseViewModel.adViewed(ad)
        val i = Intent(this, DescriptionActivity::class.java)
        i.putExtra(DescriptionActivity.AD, ad) //предаем информацию
        startActivity(i)
    }

    override fun onFavClicked(ad: Ad) {//функция из интерфеса который в адаптере для избранного
        firebaseViewModel.onFavClick(ad)
    }

    private fun navViewSettings() = with(binding) { //чтобы менять цвет
        val menu = navView.menu
        val adsCat = menu.findItem(R.id.adsCat)
        val spanAdsCat = SpannableString(adsCat.title) //SpannableString красит текст
        spanAdsCat.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this@MainActivity, R.color.color_red)),
            0, adsCat.title.length, 0) //окрашиваем текст от 0 до конца
        adsCat.title = spanAdsCat //делаем текст перекрашенным

        val accCat = menu.findItem(R.id.accCat)
        val spanAccCat = SpannableString(accCat.title) //SpannableString красит текст
        spanAccCat.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this@MainActivity, R.color.color_red)),
            0, accCat.title.length, 0) //окрашиваем текст от 0 до конца
        accCat.title = spanAccCat //делаем текст перекрашенным

    }
    private fun scrollListener() = with(binding.mainContent) { //функция которая будет замечать что мы дошли до последнего объявления
        rcView.addOnScrollListener(object: RecyclerView.OnScrollListener(){ //слушатель который будет замечать что мы дошли до последнего объявления
            override fun onScrollStateChanged(recView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recView, newState)
                if(recView.canScrollVertically(SCROLL_DOWN) && newState == RecyclerView.SCROLL_STATE_IDLE) { //проверяем может ли наш recView скроллиться вниз
                    clearUpdate = false //значит не очищаем список, а добавляем новый элемент
                    val adsList = firebaseViewModel.liveAdsData.value!!
                    if(adsList.isNotEmpty()) {
                        getAdsFromCat(adsList)//берем время последнего объявления и подгружаем его
                    }
                }
            }
        })
    }

    private fun getAdsFromCat(adsList: ArrayList<Ad>) {
        adsList[0].let {
        if(currentCategory == getString(R.string.def)) { //если у нас категория разное. currentCategory будет нести категорию на которой мы находимся
            firebaseViewModel.loadAllAdsNextPage(it.time, filterDb) //определяет что нужно загружать категорию разное
        } else { //либо загружать другие категории
                firebaseViewModel.loadAllAdsFromCatNextPage(it.category!!, it.time, filterDb) //либо загружать другие категории
        }
        }
    }
    companion object {
        const val EDIT_STATE = "edit_state" //конст для редактирования
        const val ADS_DATA = "ads_data" //конст будем передавать все объявление
        const val SCROLL_DOWN = 1
    }
}