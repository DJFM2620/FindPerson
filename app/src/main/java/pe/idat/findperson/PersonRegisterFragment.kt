package pe.idat.findperson

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import pe.idat.findperson.databinding.FragmentRegisterPersonBinding
import java.util.*


class PersonRegisterFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mBinding: FragmentRegisterPersonBinding
    private lateinit var mContext: Context
    private lateinit var map: GoogleMap

    private var mActivity:MainActivity? = null
    private var mPersonEntity: PersonEntity? = null
    private var mIsEditMode:Boolean = false
    private var imageUri: Uri? = null
    private var currentMarker: Marker? = null

    companion object {
        const val REQUEST_CODE_LOCATION = 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mBinding = FragmentRegisterPersonBinding.inflate(inflater, container, false)

        val supportMapFragment = childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment?
        supportMapFragment!!.getMapAsync(this)

        return mBinding.root
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val personId = arguments?.getLong("keyId", 0)

        val imageUri = personId?.let { ImageController.getImageUri(requireActivity(), it) }
        mBinding.imgView.setImageURI(imageUri)

        if (personId != null && personId != 0L){//Si existe

            callPersonDb(personId)
            mPersonEntity?.personId
            mIsEditMode = true
            Snackbar.make(mBinding.root, "$personId", Snackbar.LENGTH_SHORT).show()
        }
        actionBar()

        mBinding.btnGallery.setOnClickListener {
            requestPermissions()
        }
    }

    //Llamar al menu al momento de empezar la actividad
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.menu_save, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    //Para ejecutar eventos dentro del menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when(item.itemId){

            android.R.id.home -> {
                //Flecha de retroceso
                mActivity?.onBackPressed()
                true
            }
            R.id.action_save -> {
                //Boton menu save
                if(mIsEditMode){
                    updatePerson()
                }else{
                    registerPerson()
                }
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    //Para cerrar correctamente el fragmento
    override fun onDestroy() {

        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)//Flecha de retroceso
        mActivity?.supportActionBar?.title = getString(R.string.app_name)//Titulo

        mActivity?.mBinding?.fabNewPerson?.show()

        super.onDestroy()
    }

    override fun onMapReady(googlemap: GoogleMap) {

        map = googlemap

        enableLocation()

        map.setOnMapClickListener {
            val markerOptions = MarkerOptions().position(it)
            if(currentMarker != null){
                currentMarker!!.remove()
            }
            currentMarker = map.addMarker(markerOptions.snippet(getAddress(it)).draggable(true))
            map.animateCamera(CameraUpdateFactory.newLatLng(it))
        }
    }

    private fun getAddress(latLong: LatLng): String?{

        val geoCoder = Geocoder(context, Locale.getDefault())
        val addresses = geoCoder.getFromLocation(latLong.latitude, latLong.longitude, 1)

        mBinding.ietDirection.setText(addresses[0].getAddressLine(0).toString())

        return addresses[0].getAddressLine(0).toString()
    }

    @SuppressLint("MissingPermission")
    private fun enableLocation(){
        if(!::map.isInitialized) return
        if(isLocationPermissionGranted()){
            map.isMyLocationEnabled = true
        }else{
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission(){
        if(activity?.let{
            ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_FINE_LOCATION)
            } == true){

            Toast.makeText(activity, "Ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        }else{
            activity?.let {
                ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_LOCATION)
            }
        }
    }

    private fun isLocationPermissionGranted() = context?.let {
        ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION)
    } == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when(requestCode){
            REQUEST_CODE_LOCATION -> if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                map.isMyLocationEnabled = true
            }else{
                Toast.makeText(activity, "Para activar la localizacion ve a ajusters y acepta los permisos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()

        if(!::map.isInitialized) return
        if(!isLocationPermissionGranted()){

            map.isMyLocationEnabled = false
            Toast.makeText(activity, "Para activar la localizacion ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestPermissions(){

        //Obtenemos el nivel de API para obtener los permisos en tiempo de ejecucion
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            when{ //Averiguar si los permisos estan habilitados
                //Verificamos si el permiso dentro del ContextCompat es igua al permiso dentro del manifest, supongo, y asi se valida si hay permisos o no
                this.context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) } == PackageManager.PERMISSION_GRANTED -> {

                    pickPhotoFromGallery()
                }
                else -> requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }else{//Sirve para abrir la galeria en caso de que los permisos se habiliten en tiempo de instalacion?

            pickPhotoFromGallery()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){

        //Aca se maneja si se acepta o se deniegan los permisos
            isGranted ->
        if(isGranted){
            pickPhotoFromGallery()
        }else{
            Toast.makeText(activity, "Se necesita habilitar los permisos", Toast.LENGTH_SHORT).show()
        }
    }
    private val startForActivityGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){

        //Aqui manejamos el resultado de la actividad a donde nos hemos dirigido
            result ->
        if(result.resultCode == Activity.RESULT_OK){ //Esto quiere decir que si se cojio una foto de la geleria

            val data = result.data?.data //Almacenamos la foto

            val personId = arguments?.getLong("keyId", 0)

            if (data != null) {
                mPersonEntity?.photo = data.toString()
                imageUri = data
                mBinding.imgView.setImageURI(data)
            }

            if (personId != null) {
                mPersonEntity?.personId = personId
                if (data != null) {
                    ImageController.saveImage(mActivity?.baseContext!!,personId, data)
                }
            }

            /*if (data != null) {

                Log.d("MENSAJEEEEEEEEEEEEEEEEEEEEEEEEEE", "DATA ---> $data" +
                        "\nDATA-PATH ---> ${data.path}" +
                        "\nFILE FROM URI ---> ${getFileFromUri(requireActivity(), data)}" +
                        //"\nFILE TO BASE64 ---> ${file2Base64(getFileFromUri(requireActivity(), data))}" +
                        "\nBITMAP ---> ${convertBase64ToBitmap(file2Base64(getFileFromUri(requireActivity(), data)))}" +
                        "\nDRAWABLE IMG VIEW ---> ${mBinding.imgView.drawable}")

                activity?.let {
                    Glide.with(it)
                        .load(convertBase64ToBitmap(file2Base64(getFileFromUri(requireActivity(), data))))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(mBinding.imgView)
                }

                //mBinding.imgView.setImageBitmap(convertBase64ToBitmap(file2Base64(getFileFromUri(requireActivity(), data))))

                val myDrawable: Drawable = mBinding.imgView.drawable
                val myBitmap: Bitmap = myDrawable.toBitmap()

                Log.d("MENSAJEEEEEEEEEEEEEEEEEEEEEEEEE", "\nDRAWABLE TO BITMAP ---> $myBitmap")
            }*/
        }
    }

    private fun pickPhotoFromGallery() {

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startForActivityGallery.launch(intent)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun callPersonDb(Id: Long){
        doAsync {
            mPersonEntity = PersonApplication.database.PersonDao().findByIdDB(Id)
            uiThread {
                with(mBinding){

                    ietCode.text = mPersonEntity?.personId.toString().editable()
                    tilCode.hint = "Codigo"

                    ietName.text = mPersonEntity?.name?.editable()
                    ietAge.text = mPersonEntity?.age?.editable()
                    ietHeight.text = mPersonEntity?.height?.editable()
                    ietWeight.text = mPersonEntity?.weight?.editable()
                    ietDirection.text = mPersonEntity?.direction?.editable()

                    val imageUri =
                        mPersonEntity?.personId?.let { ImageController.getImageUri(requireActivity(), it) }

                    imgView.setImageURI(imageUri)

                    /*activity?.let {
                        Glide.with(it)
                            .load(mPersonEntity?.photo)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .into(mBinding.imgView)
                    }*/

                    //val myDrawable: Drawable = image.getDrawable()
                    //val myBitmap: Bitmap = myDrawable.getBitmap()

                    /*Glide.with(mContext)
                        .load(convertBase64ToBitmap())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(holder.binding.ivImage)*/
                }
            }
        }
    }

    private fun updatePerson(){

        //val myDrawable: Drawable = mBinding.imgView.drawable
        //val myBitmap: Bitmap = myDrawable.toBitmap()

        imageUri?.let {
            ImageController.saveImage(requireActivity(),mPersonEntity!!.personId , it)
        }

        val person = PersonEntity(name = mBinding.ietName.text.toString().trim(),
                                  age = mBinding.ietAge.text.toString().trim(),
                                  height = mBinding.ietHeight.text.toString().trim(),
                                  weight = mBinding.ietWeight.text.toString().trim(),
                                  photo = imageUri.toString(),
                                  direction = mBinding.ietDirection.text.toString().trim())

        doAsync {
            person.personId = mPersonEntity!!.personId //Le seteamos el ID que se encontre en la BD al nuevo person con los datos actualizados
            PersonApplication.database.PersonDao().updateDB(person)

            uiThread {
                hideKeyboard()
                mActivity?.updateMemory(person)
                Snackbar.make(mBinding.root, getString(R.string.person_updated), Snackbar.LENGTH_SHORT).show()
                mActivity?.onBackPressed()
            }
        }
    }

    private fun registerPerson(){

        doAsync {
            var Id = PersonApplication.database.PersonDao().maxId()

            imageUri?.let {
                ImageController.saveImage(requireActivity(), Id + 1L, it)
            }

            val person = PersonEntity(personId = Id+1,
                name = mBinding.ietName.text.toString().trim(),
                age = mBinding.ietAge.text.toString().trim(),
                height = mBinding.ietHeight.text.toString().trim(),
                weight = mBinding.ietWeight.text.toString().trim(),
                photo = imageUri.toString(),
                direction = mBinding.ietDirection.text.toString().trim())

            PersonApplication.database.PersonDao().insertDB(person)
            uiThread {

                hideKeyboard()
                mActivity?.insertMemory(person)
                Snackbar.make(mBinding.root, getString(R.string.person_saved), Snackbar.LENGTH_SHORT).show()
                mActivity?.onBackPressed()
            }
        }
    }

    private fun String.editable(): Editable {

        return Editable.Factory.getInstance().newEditable(this)
    }

    private fun actionBar(){

        mActivity = activity as? MainActivity

        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mActivity?.supportActionBar?.title =
            if(mIsEditMode){
                getString(R.string.person_title_update)
            }else{
                getString(R.string.person_title_add)
            }
        setHasOptionsMenu(true)
    }

    /*
    private fun getFileFromUri(context: Context, uri: Uri?): File? {
        uri ?: null
        uri?.path ?: null

        var newUriString = uri.toString()
        newUriString = newUriString.replace(
            "content://com.android.providers.downloads.documents/",
            "content://com.android.providers.media.documents/"
        )
        newUriString = newUriString.replace(
            "/msf%3A", "/image%3A"
        )
        val newUri = Uri.parse(newUriString)

        var realPath = String()
        val databaseUri: Uri
        val selection: String?
        val selectionArgs: Array<String>?
        if (newUri.path?.contains("/document/image:") == true) {
            databaseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            selection = "_id=?"
            selectionArgs = arrayOf(DocumentsContract.getDocumentId(newUri).split(":")[1])
        } else {
            databaseUri = newUri
            selection = null
            selectionArgs = null
        }
        try {
            val column = "_data"
            val projection = arrayOf(column)
            val cursor = context.contentResolver.query(
                databaseUri,
                projection,
                selection,
                selectionArgs,
                null
            )
            cursor?.let {
                if (it.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(column)
                    realPath = cursor.getString(columnIndex)
                }
                cursor.close()
            }
        } catch (e: Exception) {
            Log.i("GetFileUri Exception:", e.message ?: "")
        }
        val path = realPath.ifEmpty {
            when {
                newUri.path?.contains("/document/raw:") == true -> newUri.path?.replace(
                    "/document/raw:",
                    ""
                )
                newUri.path?.contains("/document/primary:") == true -> newUri.path?.replace(
                    "/document/primary:",
                    "/storage/emulated/0/"
                )
                else -> return null
            }
        }
        return if (path.isNullOrEmpty()) null else File(path)
    }

    private fun convertBase64ToBitmap(b64: String?): Bitmap? {

        val imageAsBytes = Base64.decode(b64?.toByteArray(), Base64.DEFAULT)

        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size)
    }

    private fun file2Base64(filePath: File?): String? {
        var fis: FileInputStream? = null
        var base64String = ""
        val bos = ByteArrayOutputStream()

        try {
            fis = FileInputStream(filePath)
            val buffer = ByteArray(1024 * 100)
            var count = 0
            while (fis.read(buffer).also { count = it } != -1) {
                bos.write(buffer, 0, count)
            }
            fis.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        base64String = Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT)

        Snackbar.make(mBinding.root, "El Base64 es: ${base64String.toString()}", Snackbar.LENGTH_SHORT).show()

        return base64String
    }

    private fun stringToBitMap(encodedString: String?): Bitmap? {
        return try {
            val encodeByte = Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        } catch (e: java.lang.Exception) {
            e.message
            null
        }
    }
    */

    @SuppressLint("UseRequireInsteadOfGet")
    private fun hideKeyboard(){

        val imm = mActivity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if(view != null){//Si la vista existe

            imm.hideSoftInputFromWindow(view!!.windowToken, 0)//Ocultamos el teclado
        }
    }
}