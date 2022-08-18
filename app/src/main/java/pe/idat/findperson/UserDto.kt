package pe.idat.findperson

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("nombre") var name: String,
    @SerializedName("apellido") var surname: String,
    @SerializedName("celular") var phone: String,
    @SerializedName("email") var email: String,
)