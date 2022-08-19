package pe.idat.findperson

import com.google.gson.annotations.SerializedName

data class YearDepartmentResponse(
    @SerializedName("cantidad") var quantity: Integer,
    @SerializedName("departamento") var department: DepartmentResponse
)