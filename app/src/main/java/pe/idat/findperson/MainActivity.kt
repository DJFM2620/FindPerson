package pe.idat.findperson

import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import pe.idat.findperson.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), OnClickListener, MainAux {

    lateinit var mBinding: ActivityMainBinding
    private lateinit var mAdapter: PersonAdapter
    private lateinit var mGridLayoutManager: GridLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        //Nueva forma de vincular las vistas
        mBinding = ActivityMainBinding.inflate(layoutInflater) //Cargamos el mBinding con el Layout Principal
        setContentView(mBinding.root)

        //Evento del boton registrar persona
        mBinding.fabNewPerson.setOnClickListener{

            launchFragment()
        }

        //Configuracion del RecyclerView
        mAdapter = PersonAdapter(mutableListOf(),this)
        mGridLayoutManager = GridLayoutManager(this, 2)

        doAsync {
            //Aqui va la lista de personas de la BD
            val personsDb = PersonApplication.database.PersonDao().findAll()

            uiThread {
                mAdapter.setCollection(personsDb)//Aqui enviamos la lista de personas de la BD a dicho metodo
            }
        }

        mBinding.recyclerView.apply {

            setHasFixedSize(true)//No cambiara su tamaño, o sea, para que no se pueda modificar su tamaño
            adapter = mAdapter
            layoutManager = mGridLayoutManager
        }
    }

    override fun onClick(person: PersonEntity) {

        val bundle = Bundle()//Es como el Map<> Guarda valores con una key
        bundle.putLong("keyId", person.personId)

        launchFragment(bundle)
    }

    override fun onClickFavorite(person: PersonEntity) {
    }

    override fun onClickDelete(person: PersonEntity) {

        val items = arrayOf("Eliminar")

        MaterialAlertDialogBuilder(this)
            .setTitle("Que desea hacer?")
            .setItems(items, DialogInterface.OnClickListener { dialogInterface, i ->

                when(i){
                    0 -> confirmDelete(person)
                }
            }).show()
    }

    //Lanza el fragmento
    private fun launchFragment(bundle: Bundle?=null){

        val fragment = PersonRegisterFragment() //El Fragmento

        if(bundle != null){

            fragment.arguments = bundle
        }

        val fragmentManager = supportFragmentManager //Manejador de Fragmentos
        val fragmentTransaction = fragmentManager.beginTransaction()

        //Añadimos una transaccion al framento
        fragmentTransaction.add(R.id.Container, fragment)
        //Para volver atras de un fragmento
        fragmentTransaction.addToBackStack(null)
        //Aplicamos los cambios
        fragmentTransaction.commit()

        mBinding.fabNewPerson.hide()
    }

    override fun insertMemory(personEntity: PersonEntity) {

        mAdapter.insertMemory(personEntity)
    }

    override fun updateMemory(personEntity: PersonEntity) {

        mAdapter.updateMemory(personEntity)
    }

    private fun confirmDelete(personEntity: PersonEntity){

        MaterialAlertDialogBuilder(this)
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
}