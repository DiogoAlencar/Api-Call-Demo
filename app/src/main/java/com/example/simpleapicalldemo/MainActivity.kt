package com.example.simpleapicalldemo

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CallApiLoginCoroutineTask().startApiCall()

    }

    private inner class CallApiLoginCoroutineTask() {

        private lateinit var customProgressDialog: Dialog

        fun startApiCall() {
            showProgressDialog()
            lifecycleScope.launch(Dispatchers.IO) {
                val stringResult = makeApiCall("diogo", "123")
                afterCallFinish(stringResult)
            }
        }

        fun makeApiCall(username: String, password: String): String {
            var result:String
            var connection: HttpURLConnection?=null
            try{
                val url= URL("http://www.mocky.io/v2/5e3826143100006a00d37ffa")
                //Returns a URLConnection instance that represents a connection to the remote object referred to by the URL.
                connection= url.openConnection() as HttpURLConnection?

                //doInput tells if we get any data(by default doInput will be true and doOutput false)
                connection!!.doInput=true

                //doOutput tells if we send any data with the api call
                connection!!.doOutput=true

                // INSERIR DADOS COM MÉTODO POST
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.useCaches = false
                val writeDataOutputStream = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                jsonRequest.put("username", username)
                jsonRequest.put("password", password)
                writeDataOutputStream.writeBytes(jsonRequest.toString())
                writeDataOutputStream.flush()
                writeDataOutputStream.close()

                val httpResult : Int = connection.responseCode

                if(httpResult==HttpURLConnection.HTTP_OK){
                    //now once we have established a successful connection, we want to read the data.
                    //Returns an input stream that reads from this open connection. A SocketTimeoutException can be thrown when
                    // reading from the returned input stream if the read timeout expires before data is available for read.
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder : StringBuilder = StringBuilder()
                    var line:String?
                    try{
                        while (reader.readLine().also { line=it }!=null) {
                            stringBuilder.append(line+"\n")
                            Log.i("TAG", "doInBackground: $line\n")
                        }
                    }
                    catch (e: IOException){
                        e.printStackTrace()
                    }
                    finally {
                        try {  //there could be some error while closing the inputStream
                            inputStream.close()
                        }
                        catch (e:IOException){
                            e.printStackTrace()
                        }
                    }

                    result = stringBuilder.toString()

                } else {  //if the response code is not OK
                    result = connection.responseMessage
                }
            }
            catch (e: SocketTimeoutException){
                result="Connection Timeout"
            }
            catch (e:Exception){
                result="Error + ${e.message}"
            }
            finally {
                connection?.disconnect()
            }
            return result
        }
        fun afterCallFinish(result: String?) {
            cancelProgressDialog()
            Log.i("JSON RESPONSE RESULT", result.toString())

            // Usando biblioteca GSON
            val responseData = Gson().fromJson(result, DataResponse::class.java)
            Log.i("GSON Message", responseData.message)
            Log.i("GSON User Id", responseData.user_id)
            Log.i("GSON Name", responseData.name)
            Log.i("GSON Email", responseData.email)
            Log.i("GSON Mobile", "${responseData.mobile}")

            Log.i("GSON Profile Completed", "${responseData.profile_details.is_profile_completed}")
            Log.i("GSON Rating", "${responseData.profile_details.rating}")

            Log.i("GSON List Size", "${responseData.data_list.size}")

            // Recupera objetos em lista em um Json
            for (item in responseData.data_list.indices) {
                Log.i("GSON List Size", "${responseData.data_list[item]}")

                Log.i("GSON ID", "${responseData.data_list[item].id}")
                Log.i("GSON Value", "${responseData.data_list[item].value}")
            }

            // Manipular os dados do JSON
            val jsonObject = JSONObject(result)
            val name = jsonObject.optString("Name")
            val message = jsonObject.optString("message")

            // Recupera um objeto Json dentro de outro
            val profileDetailObjetc = jsonObject.optJSONObject("profile_details")
            val isProfileCompleted = profileDetailObjetc?.optBoolean("is_profile_completed")
            Log.i("Is Profile Completed", "$isProfileCompleted")

            // Recupera uma lista dentro de um Json
            val dataListArray = jsonObject.optJSONArray("data_list")
            Log.i("Tamanho da lista", "${dataListArray.length()}")

            for (item in 0 until dataListArray.length()) {
                Log.i("Value $item", "${dataListArray[item]}")

                /*  recupera cada item da lista como um objeto ex:
                    {
                        "id":1,
                        "name":"Messi"
                    }
                */
                val dataItemObject: JSONObject = dataListArray[item] as JSONObject

                val id = dataItemObject.optInt("id")
                Log.i("ID", "$id")
                val name = dataItemObject.optString("value")
                Log.i("Value", "$name")
            }

        }

        private fun cancelProgressDialog(){
            customProgressDialog.dismiss()
        }

        private fun showProgressDialog(){
            customProgressDialog= Dialog(this@MainActivity)
            customProgressDialog.setContentView(R.layout.dialog_custom_progress)
            customProgressDialog.setCancelable(false)
            customProgressDialog.show()
        }

    }

}