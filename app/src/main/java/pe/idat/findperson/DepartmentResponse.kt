package pe.idat.findperson

import com.google.gson.annotations.SerializedName

data class DepartmentResponse(
    @SerializedName("departamentoId") var departmentId: Int,
    @SerializedName("nombre") var name: String,
)
