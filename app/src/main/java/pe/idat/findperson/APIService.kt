package pe.idat.findperson

import retrofit2.Response
import retrofit2.http.*

interface APIService {

    @POST("Usuario/Insertar")
    suspend fun postUser(@Body userDto: UserDto): Response<*>

    @PUT("Usuario/Actualizar")
    suspend fun putUser(@Body userDto: UserDto): Response<*>

    @GET
    suspend fun getUserByEmail(@Url url: String):Response<UserResponse>

    @GET
    suspend fun getYearDepartments(@Url url: String):Response<ArrayList<YearDepartmentResponse>>

    @GET("Años/Listar")
    suspend fun getYears():Response<ArrayList<YearResponse>>
}