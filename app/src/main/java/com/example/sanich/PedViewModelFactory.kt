package com.example.sanich

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sanich.data.PedRepository


class PedViewModelFactory(
    private val repository: PedRepository
) : ViewModelProvider.Factory {


    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {


        if (modelClass.isAssignableFrom(PedViewModel::class.java)) {

            return PedViewModel(repository) as T

        }


        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )

    }

}