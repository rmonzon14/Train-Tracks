package com.example.traintracks.screens

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.traintracks.SearchApiService
import com.example.traintracks.Workout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SharedViewModel : ViewModel() {
    val _difficulties = MutableLiveData<Array<String>>()
    val difficulties: LiveData<Array<String>> = _difficulties

    val _muscles = MutableLiveData<Array<String>>()
    val muscles: LiveData<Array<String>> = _muscles

    val _types = MutableLiveData<Array<String>>()
    val types: LiveData<Array<String>> = _types

    fun initializeData(apiService: SearchApiService) {
        viewModelScope.launch {
            val dbRef = FirebaseDatabase.getInstance().getReference("/data")

            // Use a suspending operation to get the snapshot
            val dataSnapshot = withContext(Dispatchers.IO) {
                suspendCoroutine<DataSnapshot> { continuation ->
                    dbRef.get().addOnSuccessListener { snapshot ->
                        continuation.resume(snapshot)
                    }.addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
                }
            }

            // Check if data is empty and fetch/store data
            if (!dataSnapshot.exists()) {
                fetchAndStoreData(apiService, dbRef) // Correctly called inside a coroutine
            }

            // Collect unique values for types, muscles, and difficulties
            val fetchedDifficulties = mutableSetOf<String>()
            val fetchedMuscles = mutableSetOf<String>()
            val fetchedTypes = mutableSetOf<String>()

            dataSnapshot.children.forEach { childSnapshot ->
                val workout = childSnapshot.getValue(Workout::class.java)
                workout?.let {
                    fetchedDifficulties.add(it.difficulty)
                    fetchedMuscles.add(it.muscle)
                    fetchedTypes.add(it.type)
                }
            }

            // Update LiveData
            _difficulties.postValue(fetchedDifficulties.toTypedArray())
            _muscles.postValue(fetchedMuscles.toTypedArray())
            _types.postValue(fetchedTypes.toTypedArray())
        }
    }

}