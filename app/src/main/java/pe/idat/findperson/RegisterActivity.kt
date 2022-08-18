package pe.idat.findperson

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pe.idat.findperson.databinding.ActivityRegisterBinding
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RegisterActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    private val baseURL: String = "http://192.168.1.16:8090/EVC/Api/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        navigateToLogin()

        mBinding.ietEmail.setOnFocusChangeListener { view, b ->

            mBinding.tilEmail.error = null
        }
        mBinding.ietPassword.setOnFocusChangeListener { view, b ->

            mBinding.tilPassword.error = null
            mBinding.tilPassword.isPasswordVisibilityToggleEnabled = true
        }
        mBinding.ietConfirmPassword.setOnFocusChangeListener { view, b ->

            mBinding.tilConfirmPassword.error = null
            mBinding.tilConfirmPassword.isPasswordVisibilityToggleEnabled = true
        }

        mBinding.btnRegister.setOnClickListener {
            if(isValidateEmpty(mBinding.tilEmail)){
                if (isValidatePass()) {
                    insertClient()
                }
            }
        }
    }

    private fun isValidateEmpty (vararg txtArray: TextInputLayout): Boolean{

        var isValid = true

        for (txt in txtArray){

            if(txt.editText?.text.toString()?.trim().isEmpty()){

                txt.error = getString(R.string.helper_required)
                isValid = false
            }
        }
        return isValid
    }

    private fun isValidatePass(): Boolean{

        var isValid = true

        val pass = mBinding.ietPassword.text.toString().trim()
        val confirmPass = mBinding.ietConfirmPassword.text.toString().trim()

        val tilPass = mBinding.tilPassword
        val tilConfirmPass = mBinding.tilConfirmPassword

        tilPass.clearFocus()
        tilConfirmPass.clearFocus()

         if (!mBinding.cbTermsAndConditions.isChecked) {

            mBinding.cbTermsAndConditions.error = "Requerido que acepte"

            isValid = false

        } else if (pass.length <= 6) {

            tilPass.error = "Caracteres Mayor o igual a 7"
            tilPass.isPasswordVisibilityToggleEnabled = false
            isValid = false

        } else if (pass != confirmPass) {

            tilConfirmPass.error = "Las contraseñas no son iguales"
            tilConfirmPass.isPasswordVisibilityToggleEnabled = false

            tilPass.error = " "
            tilPass.isPasswordVisibilityToggleEnabled = false

            isValid = false

        }
        else{
            tilPass.error = null
            tilConfirmPass.error = null
            mBinding.cbTermsAndConditions.error = null

            tilPass.isPasswordVisibilityToggleEnabled = true
            tilConfirmPass.isPasswordVisibilityToggleEnabled = true
        }
        return isValid
    }

    private fun createUser(email: String, pass: String){

        auth = Firebase.auth
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this){

            if (it.isSuccessful){
                showHome(email, pass)
            }else{
                showError()
            }
        }
    }

    private fun getRetrofit(): Retrofit {

        val gson = GsonBuilder()
            .setLenient()
            .create()

        return Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(NullOnEmptyConverterFactory())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun insertClient(){

        var name = mBinding.ietName.text.toString().trim()
        var surname = mBinding.ietSurname.text.toString().trim()
        var email = mBinding.ietEmail.text.toString().trim()
        var phone = mBinding.ietPhone.text.toString().trim()
        var pass = mBinding.ietPassword.text.toString().trim()

        val userDto = UserDto(
            name = name,
            surname = surname,
            email = email,
            phone = phone
        )

        CoroutineScope(Dispatchers.IO).launch{

            val call = getRetrofit().create(APIService::class.java).postUser(userDto)

            runOnUiThread {
                if (call.isSuccessful){
                    Toast.makeText(this@RegisterActivity, "Se registro exitosamente...!!", Toast.LENGTH_SHORT).show()
                    createUser(email, pass)

                }else{
                    showError()
                }
            }
        }
    }

    private fun showHome(email: String, pass: String){

        val intent = Intent(this, MainActivity::class.java).apply {
        }
        intent.putExtra("Email", email)
        intent.putExtra("Pass", pass)
        startActivity(intent)
    }

    private fun showError(){
        Toast.makeText(this, "Ha ocurrido un error al registrarse", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToLogin() {

        mBinding.tvLogin.setOnClickListener {

            val intent = Intent(this, LoginActivity::class.java).apply {
            }
            startActivity(intent)
        }
    }
}