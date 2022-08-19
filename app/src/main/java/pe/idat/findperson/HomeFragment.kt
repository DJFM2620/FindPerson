package pe.idat.findperson

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import pe.idat.findperson.databinding.FragmentHomeBinding

class HomeFragment : Fragment(), OnClickListener, MainAux {

    private lateinit var mBinding: FragmentHomeBinding
    private lateinit var mAdapter: PersonAdapter
    private lateinit var mGridLayoutManager: GridLayoutManager
    private lateinit var communicator: Communicator

    private var mActivity:MainActivity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mBinding = FragmentHomeBinding.inflate(inflater, container, false)

        communicator = activity as Communicator

        actionBar()

        //Evento del boton registrar persona
        mBinding.fabNewPerson.setOnClickListener{

            val fragment = PersonRegisterFragment()
            communicator.startFragment(fragment)
        }

        doAsync {
            //Aqui va la lista de personas de la BD
            val personsDb = PersonApplication.database.PersonDao().findAll()

            uiThread {
                mAdapter.setCollection(personsDb)//Aqui enviamos la lista de personas de la BD a dicho metodo
            }
        }

        initRecyclerView()

        return mBinding.root
    }

    private fun initRecyclerView(){

        //Configuracion del RecyclerView
        mAdapter = PersonAdapter(mutableListOf(),this)
        mGridLayoutManager = GridLayoutManager(activity, 2)

        mBinding.recyclerView.layoutManager = mGridLayoutManager
        mBinding.recyclerView.adapter = mAdapter
    }

    override fun onClick(person: PersonEntity) {

        val bundle = Bundle()//Es como el Map<> Guarda valores con una key
        bundle.putLong("keyId", person.personId)

        communicator.passBundle(bundle)
        mBinding.fabNewPerson.hide()
    }

    override fun onClickFavorite(person: PersonEntity) {
    }

    override fun onClickDelete(person: PersonEntity) {

        val items = arrayOf("Eliminar")

        MaterialAlertDialogBuilder(requireActivity())
            .setTitle("Que desea hacer?")
            .setItems(items, DialogInterface.OnClickListener { dialogInterface, i ->

                when(i){
                    0 -> confirmDelete(person)
                }
            }).show()
    }

    override fun insertMemory(personEntity: PersonEntity) {

        mAdapter = PersonAdapter(mutableListOf(),this)
        mAdapter.insertMemory(personEntity)
    }

    override fun updateMemory(personEntity: PersonEntity) {

        mAdapter = PersonAdapter(mutableListOf(),this)
        mAdapter.updateMemory(personEntity)
    }

    private fun confirmDelete(personEntity: PersonEntity){

        MaterialAlertDialogBuilder(requireActivity())
            .setTitle("Esta seguro de eliminar esta persona?")
            .setPositiveButton("Eliminar",
                DialogInterface.OnClickListener { dialogInterface, i ->

                    doAsync {
                        PersonApplication.database.PersonDao().deleteDB(personEntity)

                        uiThread {
                            mAdapter.deleteMemory(personEntity)
                        }
                    }
                }).setNegativeButton("Cancelar", null).show()
    }

    private fun actionBar(){

        mActivity = activity as? MainActivity

        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        mActivity?.supportActionBar?.title = getString(R.string.home_title)
    }
}