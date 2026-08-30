/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.recorder

import android.widget.CompoundButton
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.materialswitch.MaterialSwitch
import org.lineageos.recorder.utils.PermissionManager
import org.lineageos.recorder.utils.PreferencesManager

class SettingsActivity : AppCompatActivity(R.layout.activity_settings) {
    private val preferences by lazy { PreferencesManager(this) }
    private val permissions by lazy { PermissionManager(this) }
    private lateinit var locationSwitch: MaterialSwitch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.settingsToolbar)
        toolbar.setNavigationOnClickListener { finish() }
        locationSwitch = findViewById(R.id.settingsLocationSwitch)
        val qualitySwitch = findViewById<MaterialSwitch>(R.id.settingsQualitySwitch)

        locationSwitch.isChecked = preferences.tagWithLocation && permissions.hasLocationPermission()
        qualitySwitch.isChecked = preferences.recordInHighQuality

        locationSwitch.setOnCheckedChangeListener { _: CompoundButton?, checked: Boolean ->
            if (checked && !permissions.hasLocationPermission()) {
                permissions.requestLocationPermission()
            } else {
                preferences.tagWithLocation = checked
            }
        }
        qualitySwitch.setOnCheckedChangeListener { _: CompoundButton?, checked: Boolean ->
            preferences.recordInHighQuality = checked
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissionsArray: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissionsArray, grantResults)
        if (requestCode == PermissionManager.REQUEST_CODE) {
            locationSwitch.isChecked = permissions.hasLocationPermission()
            preferences.tagWithLocation = locationSwitch.isChecked
        }
    }
}
