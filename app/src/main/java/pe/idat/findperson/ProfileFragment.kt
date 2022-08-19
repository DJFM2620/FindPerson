package pe.idat.findperson

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pe.idat.findperson.databinding.FragmentProfileBinding
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProfileFragment : Fragment() {

    private lateinit var mBinding: FragmentProfileBinding

    private var mActivity:MainActivity? = null
    private val baseURL: String = "http://192.168.1.16:8090/EVC/Api/"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mBinding = FragmentProfileBinding.inflate(inflater, container, false)

        actionBar()
        getUserByEmail()

        mBinding.btnUpdate.setOnClickListener {
            updateClient()
        }

        return mBinding.root
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

    private fun updateClient(){

        var name = mBinding.ietName.text.toString().trim()
        var surname = mBinding.ietSurname.text.toString().trim()
        var email = mBinding.ietEmail.text.toString().trim()
        var phone = mBinding.ietPhone.text.toString().trim()

        val userDto = UserDto(
            name = name,
            surname = surname,
            email = email,
            phone = phone
        )

        CoroutineScope(Dispatchers.IO).launch{

            val call = getRetrofit().create(APIService::class.java).putUser(userDto)

            activity?.runOnUiThread {
                if (call.isSuccessful){

                    Toast.makeText(activity, "Se actualizo correctamente los datos...!!", Toast.LENGTH_SHORT).show()
                }else{
                    showError()
                }
            }
        }
    }

    private fun getUserByEmail() {

        val user = Firebase.auth.currentUser
        var email = user!!.email.toString()

        CoroutineScope(Dispatchers.IO).launch {

            val call = getRetrofit().create(APIService::class.java).getUserByEmail("Usuario/$email")
            val client = call.body()

            activity?.runOnUiThread {
                if (call.isSuccessful) {
                    if (client != null) {

                        mBinding.ietCod.setText(client.userId.toString())
                        mBinding.ietName.setText(client.name)
                        mBinding.ietSurname.setText(client.surname)
                        mBinding.ietEmail.setText(client.email)
                        mBinding.ietPhone.setText(client.phone)
                    } else {
                        Toast.makeText(activity,"Error al cargar los datos", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    showError()
                }
            }
        }
    }

    private fun actionBar(){

        mActivity = activity as? MainActivity

        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        mActivity?.supportActionBar?.title = getString(R.string.profile_title)

        setHasOptionsMenu(true)
    }

    private fun showError(){
        Toast.makeText(activity, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
    }
}