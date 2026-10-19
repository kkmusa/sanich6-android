package com.example.sanich

import android.content.Context
import android.util.Log
import com.example.sanich.data.DatabaseProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object DatabaseCheck {


    fun check(context: Context) {


        CoroutineScope(Dispatchers.IO).launch {


            try {

                val db = DatabaseProvider.getDatabase(context)

                val dao = db.pedDao()


                val result = dao.getResultPeds(
                    95,
                    16,
                    114,
                    5.2
                )


                result.forEach {

                    Log.d(
                        "PED_TEST",
                        "ПЭД: ${it.name}, завод: ${it.manufacturer}, приток: ${it.flow}"
                    )

                }


            } catch (e: Exception) {

                Log.e(
                    "PED_TEST",
                    "Ошибка базы: ${e.message}"
                )

            }

        }

    }

}