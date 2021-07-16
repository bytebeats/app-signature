package me.bytebeats.spm

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.SparseArray
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

/**
 * Created by bytebeats on 2021/7/16 : 14:26
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */
class ReportFragment : Fragment() {
    companion object {
        private const val USE_ACTIVITY_RESULT_API = "use_activity_result_api"
        fun newInstance(useActivityResultApi: Boolean = true): ReportFragment {
            val args = Bundle()
            args.putBoolean(USE_ACTIVITY_RESULT_API, useActivityResultApi)
            val fragment = ReportFragment()
            fragment.arguments = args
            return fragment
        }
    }

    var storageAccessRequestCode: Int = 1024
    private val mCallbacks by lazy { mutableMapOf<Int, StoragePermissionManager.OnStoragePermissionResult?>() }
    private val useActivityResultApi by lazy {
        requireArguments().getBoolean(
            USE_ACTIVITY_RESULT_API,
            false
        )
    }

    private val mRStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            performRStoragePermissionResult(result.resultCode)
        }
    private val mMStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            if (results.values.all { it == true })
                mCallbacks[storageAccessRequestCode]?.onPermissionGranted()
            else
                mCallbacks[storageAccessRequestCode]?.onPermissionGranted()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    fun requestStoragePermission(callback: StoragePermissionManager.OnStoragePermissionResult?) {
        mCallbacks[storageAccessRequestCode] = callback
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                mCallbacks[storageAccessRequestCode]?.onPermissionGranted()
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:${requireContext().packageName}")
                if (useActivityResultApi) {
                    mRStoragePermissionLauncher.launch(intent)
                } else {
                    startActivityForResult(intent, storageAccessRequestCode)
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE) &&
                isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ) {
                mCallbacks[storageAccessRequestCode]?.onPermissionGranted()
            } else {
                val storagePermissions = arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                if (useActivityResultApi) {
                    mMStoragePermissionLauncher.launch(
                        storagePermissions
                    )
                } else {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        storagePermissions,
                        storageAccessRequestCode
                    )
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        performMStoragePermissionResult(requestCode)
    }

    private fun performMStoragePermissionResult(requestCode: Int) {
        if (storageAccessRequestCode == requestCode) {
            if (isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE) &&
                isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ) {
                mCallbacks[storageAccessRequestCode]?.onPermissionGranted()
            } else {
                mCallbacks[storageAccessRequestCode]?.onPermissionDenied()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        performRStoragePermissionResult(requestCode)
    }

    private fun performRStoragePermissionResult(requestCode: Int) {
        if (storageAccessRequestCode == requestCode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                mCallbacks[storageAccessRequestCode]?.onPermissionGranted()
            } else {
                mCallbacks[storageAccessRequestCode]?.onPermissionGranted()
            }
        }
    }

    private fun isPermissionGranted(permission: String): Boolean =
        ActivityCompat.checkSelfPermission(
            requireActivity(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
}