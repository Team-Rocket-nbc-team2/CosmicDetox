package com.rocket.cosmic_detox.presentation.component.bottomsheet.modifyallowapp

import android.util.Log
import androidx.lifecycle.ViewModel
import com.rocket.cosmic_detox.domain.repository.AllowAppRepository
import com.rocket.cosmic_detox.presentation.model.App
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AllowAppViewModel @Inject constructor(
    private val repository: AllowAppRepository
) : ViewModel() {

    private val _installedApps = MutableStateFlow<List<App>>(emptyList())
    val installedApps = _installedApps.asStateFlow()

    fun loadInstalledApps() {
        _installedApps.value = repository.getInstalledApps()

        Log.d("AllowAppViewModel", "loadInstalledApps: ${_installedApps.value}")
    }
}