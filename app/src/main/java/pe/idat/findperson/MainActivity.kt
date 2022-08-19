package pe.idat.findperson

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import pe.idat.findperson.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), Communicator {

    lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        //Nueva forma de vincular las vistas
        mBinding = ActivityMainBinding.inflate(layoutInflater) //Cargamos el mBinding con el Layout Principal
        setContentView(mBinding.root)

        replaceFragment(HomeFragment())

        mBinding.bottomNavigationView.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.menu_home -> replaceFragment(HomeFragment())
                R.id.menu_profile -> replaceFragment(ProfileFragment())
                R.id.menu_statistics -> replaceFragment(StatisticsFragment())
                else -> {
                }
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    override fun startFragment(fragment: Fragment) {

        replaceFragment(fragment)
    }

    override fun passBundle(bundle: Bundle) {

        val fragment = PersonRegisterFragment()
        fragment.arguments = bundle

        replaceFragment(fragment)
    }
}