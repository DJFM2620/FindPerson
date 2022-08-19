package pe.idat.findperson

import com.google.gson.annotations.SerializedName

data class YearResponse(
    @SerializedName("añoId") var yearId: Integer,
    @SerializedName("año") var year: String,
)
