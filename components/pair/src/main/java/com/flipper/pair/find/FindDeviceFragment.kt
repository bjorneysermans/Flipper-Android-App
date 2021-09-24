package com.flipper.pair.find

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.core.content.edit
import androidx.fragment.app.viewModels
import com.flipper.bridge.api.scanner.DiscoveredBluetoothDevice
import com.flipper.core.api.BottomNavigationActivityApi
import com.flipper.core.di.ComponentHolder
import com.flipper.core.utils.preference.FlipperSharedPreferences
import com.flipper.core.utils.preference.FlipperSharedPreferencesKey
import com.flipper.core.view.ComposeFragment
import com.flipper.pair.di.PairComponent
import com.flipper.pair.find.compose.ComposeFindDevice
import com.flipper.pair.find.service.BLEDeviceViewModel
import com.github.terrakok.cicerone.Router
import javax.inject.Inject

class FindDeviceFragment : ComposeFragment() {
    @Inject
    lateinit var router: Router

    @Inject
    lateinit var bottomNavigationActivityApi: BottomNavigationActivityApi

    @Inject
    lateinit var sharedPreferences: FlipperSharedPreferences

    private val bleDeviceViewModel by viewModels<BLEDeviceViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ComponentHolder.component<PairComponent>().inject(this)
    }

    @Composable
    override fun renderView() {
        ComposeFindDevice(bleDeviceViewModel) { bleDevice ->
            onDeviceSelected(bleDevice)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bleDeviceViewModel.startScanIfNotYet()
    }

    private fun onDeviceSelected(discoveredBluetoothDevice: DiscoveredBluetoothDevice) {
        bleDeviceViewModel.stopScanAndReset()
        sharedPreferences.edit {
            putString(
                FlipperSharedPreferencesKey.DEVICE_ID,
                discoveredBluetoothDevice.address
            )
        }
        bottomNavigationActivityApi.openBottomNavigationScreen()
        requireActivity().finish()
    }
}