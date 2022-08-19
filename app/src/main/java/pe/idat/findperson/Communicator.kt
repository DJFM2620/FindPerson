package pe.idat.findperson

import android.os.Bundle
import androidx.fragment.app.Fragment

interface Communicator {

    fun startFragment(fragment: Fragment)
    fun passBundle(bundle: Bundle)
}