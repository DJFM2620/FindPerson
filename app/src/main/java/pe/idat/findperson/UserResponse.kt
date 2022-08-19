package pe.idat.findperson

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("usuarioId") var userId: Int,
    @SerializedName("nombre") var name: String,
    @SerializedName("apellido") var surname: String,
    @SerializedName("celular") var phone: String,
    @SerializedName("email") var email: String,
)
