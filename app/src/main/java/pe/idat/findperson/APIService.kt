package pe.idat.findperson

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface APIService {

    @POST("Usuario/Insertar")
    suspend fun postUser(@Body userDto: UserDto): Response<*>
}