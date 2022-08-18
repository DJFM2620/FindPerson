package pe.idat.findperson

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pe.idat.findperson.databinding.ItemPersonaBinding

//Sirve para mostrar los datos en cada elemento de la vista
class PersonAdapter(private var itemPersons:MutableList<PersonEntity>,
                    private var listener: OnClickListener): RecyclerView.Adapter<PersonAdapter.ViewHolder>() {

    private lateinit var mContext:Context

    //Clase interna ViewHolder ---> Nos permite acceder a cada elemento de una vista y no escribir todos los elementos manualmente
    inner class ViewHolder(view:View): RecyclerView.ViewHolder(view){

        //DataBinding ---> sirve para enlanzar componentes de interfaces con fuentes de datos para obtener la informacion de las personas
        val binding = ItemPersonaBinding.bind(view)

        fun setListener(person:PersonEntity){

            binding.root.setOnClickListener{

                listener.onClick(person)
            }
            binding.root.setOnLongClickListener{

                listener.onClickDelete(person)
                true
            }
        }
    }

    //Este metodo crea un objeto ViewHolder cada vez que el RecyclerView necesita uno nuevo
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        mContext = parent.context
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_persona, parent, false) //Le enviaremos los datos a esta vista

        return ViewHolder(view)
    }

    //Este metodo enlaza los datos con cada elemento del ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val person = itemPersons.get(position)//Buscamos a la persona y le asignamos su nombre en el With()

        //With ---> Funcion de Alcance, sirve para definir los valores de cada propiedad omitiendo el nombre de la instancia
        with(holder){

            setListener(person)
            binding.tvNamePerson.text = person.name

            val imageUri = ImageController.getImageUri(mContext, person.personId)

            binding.imgPhoto.setImageURI(imageUri)
        }
        Log.d("MENSAJEEEEEEEEEEEEEEEEEE", "SE VUELVE A CARGAR")
        Log.d("MENSAJEEEEEEEEEEEEEEEEEE", "=========================")
    }

    override fun getItemCount(): Int {

        return itemPersons.size
    }

    fun setCollection(personsDb:MutableList<PersonEntity>) {

        this.itemPersons = personsDb
        notifyDataSetChanged()
    }
    fun insertMemory(person:PersonEntity){

        itemPersons.add(person)
        notifyDataSetChanged()
    }
    fun updateMemory(person: PersonEntity){

        //Se obtiene el indice de la persona en la lista de personas
        val index = itemPersons.indexOf(person)

        if(index != -1){//Si existe

            itemPersons.set(index, person)
        }
        notifyItemChanged(index)
    }
    fun deleteMemory(person: PersonEntity){

        val index = itemPersons.indexOf(person)

        if(index != -1){

            itemPersons.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}