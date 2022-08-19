package pe.idat.findperson

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pe.idat.findperson.databinding.FragmentStatisticsBinding
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class StatisticsFragment : Fragment() {

    private lateinit var mBinding: FragmentStatisticsBinding
    private lateinit var spinner: Spinner

    private var listYearDepartment = ArrayList<YearDepartmentResponse>()
    private var listYears = ArrayList<String>()
    private val baseURL: String = "http://192.168.1.16:8090/EVC/Api/"
    private var mActivity:MainActivity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mBinding = FragmentStatisticsBinding.inflate(inflater, container, false)

        spinner = mBinding.sYears

        getYears()
        actionBar()

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) {

                getYearDepartments(listYears[position])
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

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

    private fun getYears(){

        CoroutineScope(Dispatchers.IO).launch {
            val call = getRetrofit().create(APIService::class.java).getYears()
            val listYearsResponse = call.body()

            activity?.runOnUiThread {
                if (call.isSuccessful){
                    listYears.clear()
                    if (listYearsResponse != null) {

                        for (item in listYearsResponse.indices) {

                            listYears.add(listYearsResponse[item].year)
                        }

                        spinner.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, listYears)
                    }
                }
            }
        }
    }

    private fun getYearDepartments(year: String) {

        CoroutineScope(Dispatchers.IO).launch {

            val call = getRetrofit().create(APIService::class.java).getYearDepartments("Año_Departamento/$year")
            val listYDResponse = call.body()

            activity?.runOnUiThread {
                if (call.isSuccessful) {

                    listYearDepartment.clear()
                    if (listYDResponse != null) {
                        listYearDepartment.addAll(listYDResponse)
                        setBarChartValues(listYearDepartment)
                    }
                } else {
                    showError()
                }
            }
        }
    }

    private fun setBarChartValues(listYDResponse: ArrayList<YearDepartmentResponse>){

        var xValues = ArrayList<String>()

        for (item in listYDResponse.indices) {

            xValues.add(listYDResponse[item].department.name)
        }

        var barList = ArrayList<BarEntry>()

        for (item in listYDResponse.indices) {

            barList.add(BarEntry(listYDResponse[item].quantity.toFloat(), item))
        }

        var barDataSet = BarDataSet(barList, "2020")

        var data = BarData(xValues, barDataSet)
        data.setValueTextSize(13.0F)

        mBinding.barChart.data = data
        mBinding.barChart.animateXY(5000,5000)
    }

    private fun actionBar(){

        mActivity = activity as? MainActivity

        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        mActivity?.supportActionBar?.title = getString(R.string.statistics_title)
    }

    private fun showError(){
        Toast.makeText(activity, "Ha ocurrido un error al cargar los datos", Toast.LENGTH_SHORT).show()
    }
}