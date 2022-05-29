package com.fiftyonepercent.bulletinboard.accounthelper

import android.util.Log
import android.widget.Toast
import com.fiftyonepercent.bulletinboard.MainActivity
import com.fiftyonepercent.bulletinboard.R
import com.fiftyonepercent.bulletinboard.constans.FirebaseAuthConstants
import com.fiftyonepercent.bulletinboard.dialoghelper.GoogleAccConst
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import com.google.firebase.ktx.Firebase

class AccountHelper(act:MainActivity) { //Передаем MA чтобы был контекс и доступ к элементам класса
    private val act = act // передаем act(MainActivity), чтобы использовать в функциях
    private lateinit var signInClient: GoogleSignInClient

    fun signUpWithEmail(email: String, password: String) { //Функция для регистрации. Передаем email и пароль
        if (email.isNotEmpty() && password.isNotEmpty()) { // Проверка на пустоту
            act.mAuth.currentUser?.delete()?.addOnCompleteListener { //если это анонимный то удаляем
                task ->
                if(task.isSuccessful){
                    act.mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task -> //ждет email и пароль. addOnCompleteListener слушатель который говорит зарег. или нет
                        if (task.isSuccessful) { // task несет инфу о регистрации
                            signUpWithEmailSuccessful(task.result.user!!)
                        } else {
                            signUpWithEmailException(task.exception!!, email, password)
                        }
                    }
                }
            }
        }
    }

    private fun signUpWithEmailSuccessful(user: FirebaseUser) {
        sendEmailVerification(user!!) // получаем потверждающее письмо
        act.uiUpdate(user)
    }

    private fun signUpWithEmailException(e: Exception, email: String, password: String) { //проверка на ошибки

            if (e is FirebaseAuthUserCollisionException) { //проверка ошибки если почта уже используется
                val exception = e as FirebaseAuthUserCollisionException
                if (exception.errorCode == FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE) { //по константе выводим ошибку
                    linkEmailToG(email, password) //Link email
                }
            } else if (e is FirebaseAuthInvalidCredentialsException) { //проверка на неверный ввод символов
                val exception = e as FirebaseAuthInvalidCredentialsException
                if (exception.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) { //по константе выводим ошибку
                    Toast.makeText(act, FirebaseAuthConstants.ERROR_INVALID_EMAIL, Toast.LENGTH_LONG).show()
                }
            }
            if (e is FirebaseAuthWeakPasswordException) { //проверка на ввод символов меньше 6

                //Log.d("MyLog", "Exception : ${e.errorCode}")
                if (e.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) { //по константе выводим ошибку
                    Toast.makeText(act, FirebaseAuthConstants.ERROR_WEAK_PASSWORD, Toast.LENGTH_LONG).show()
                }
            }
    }

    fun signInWithEmail(email: String, password: String){ //Функция для входа. Передаем email и пароль
        if(email.isNotEmpty() && password.isNotEmpty()) { // Проверка на пустоту
            act.mAuth.currentUser?.delete()?.addOnCompleteListener {
                task ->
                if(task.isSuccessful) {
                    act.mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task -> //ждет email и пароль. addOnCompleteListener слушатель который говорит зарег. или нет
                        if (task.isSuccessful) { // task несет инфу о регистрации
                            act.uiUpdate(task.result?.user)
                        } else {
                            signUpWithEmailException(task.exception!!, email, password)
                        }
                    }
                }
            }
        }
    }

    private fun signInWithEmailException(e: Exception, email: String, password: String) {
            //Log.d("MyLog", "Exception : ${e.exception}")
            if (e is FirebaseAuthInvalidCredentialsException) { //проверка на неверный ввод символов
                //Log.d("MyLog", "Exception : ${task.exception}")
                val exception = e as FirebaseAuthInvalidCredentialsException //ошибка некореектного ввода или существующего акка
                //Log.d("MyLog", "Exception : ${task.exception}")
                if (exception.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) { //по константе выводим ошибку
                    Toast.makeText(act, FirebaseAuthConstants.ERROR_INVALID_EMAIL, Toast.LENGTH_LONG).show()
                } else if (exception.errorCode == FirebaseAuthConstants.ERROR_WRONG_PASSWORD) { //по константе выводим ошибку
                    Toast.makeText(act, FirebaseAuthConstants.ERROR_WRONG_PASSWORD, Toast.LENGTH_LONG).show()
                }
            } else if(e is FirebaseAuthInvalidUserException) { // ошибка когда вводишь не сущ акка

                if (e.errorCode == FirebaseAuthConstants.ERROR_USER_NOT_FOUND) {
                    Toast.makeText(act, FirebaseAuthConstants.ERROR_USER_NOT_FOUND, Toast.LENGTH_LONG).show()
                }
            }
        }

    private fun linkEmailToG(email: String, password: String) { // Соединение гугл и емайл акка.
        val credential = EmailAuthProvider.getCredential(email, password)
        if (act.mAuth.currentUser != null) {
            act.mAuth.currentUser?.linkWithCredential(credential)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(act, act.resources.getString(R.string.link_done), Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(act, act.resources.getString(R.string.enter_to_g), Toast.LENGTH_LONG)
                .show()
        }
    }


    private fun getSignClient():GoogleSignInClient{ //вход по гугл аккаунту. Получили клиента
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(act.getString(R.string.default_web_client_id)).requestEmail().build()
        return GoogleSignIn.getClient(act,gso) //создаем клиента
    }

    fun signInWithGoogle () { //при нажатии на вход испоьозум гугл аккаунт
        signInClient = getSignClient() //создаем клиента
        val intent = signInClient.signInIntent //перменная для входа. Берем Intent которая содержит всю инфу
        act.googleSignInLauncher.launch(intent) //оправляем intent в MA и ждем результата
    }

    fun signOutG () { //фун для выхода из гугл акка
        getSignClient().signOut()
    }

    fun signInFirebaseWithGoogle(token: String) { //регистрируем в базу по гугл акка
        val credential = GoogleAuthProvider.getCredential(token, null)
        act.mAuth.currentUser?.delete()?.addOnCompleteListener { //если анонимный пользователь зарегистрировался, то удаляем анонимного пользователя
                    task ->
                if (task.isSuccessful) { //если анонимны пользователь удалился, то создаем нового зарегистрированого пользователя
                    act.mAuth.signInWithCredential(credential).addOnCompleteListener {task2 ->
                        if (task2.isSuccessful) {
                            Toast.makeText(act, "Sign in done", Toast.LENGTH_LONG).show()
                            act.uiUpdate(task2.result?.user)// для обновления интерфейса
                        } else {
                            Toast.makeText(act, "Google sign in exception", Toast.LENGTH_LONG).show()
                            Log.d("MyLog", "Google sign in exception : ${task.exception}")
                        }
                    }
                }
            }
    }

    private fun sendEmailVerification(user: FirebaseUser) { //для потверждения регистрации. user несет инфу о пользователе
        user.sendEmailVerification().addOnCompleteListener {task-> // слушатель для проверки письма
            if(task.isSuccessful) { // Если прошло успешно
                Toast.makeText(act, act.resources.getString(R.string.send_verification_done), Toast.LENGTH_LONG).show()
            } else{
                Toast.makeText(act, act.resources.getString(R.string.send_verification_email_error), Toast.LENGTH_LONG).show()
            }
         }

    }

    fun signInAnonymously(listener: Listener) { //вход для анонимных пользователей
        act.mAuth.signInAnonymously().addOnCompleteListener {
            task ->
            if(task.isSuccessful) { //если успешно зашли как анонимный пользователь
                listener.onComplete()
            Toast.makeText(act, "Вы вошли как Гость", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(act, "Не удалось войти как Гость", Toast.LENGTH_SHORT).show()
            }
        }

    }
    interface Listener { //интерфейс для входа
        fun onComplete()
    }
}