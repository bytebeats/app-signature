package me.bytebeats.spm

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/**
 * Created by bytebeats on 2021/7/16 : 14:12
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */
class StoragePermissionManager private constructor(private val activity: FragmentActivity) {

    private val mReportFragment by lazy { findReportFragment() }

    companion object {
        private const val TAG = "StoragePermissionManager"
        fun with(activity: FragmentActivity): StoragePermissionManager {
            return StoragePermissionManager(activity)
        }
    }

    private fun findReportFragment(): ReportFragment {
        val fragmentManager = activity.supportFragmentManager
        var reportFragment = fragmentManager.findFragmentByTag(TAG) as ReportFragment?
        if (reportFragment == null) {
            reportFragment = ReportFragment.newInstance()
            fragmentManager.beginTransaction().add(reportFragment, TAG).commitAllowingStateLoss()
            fragmentManager.executePendingTransactions()
        }
        return reportFragment
    }

    fun requestStoragePermission(callback: OnStoragePermissionResult?) {
        mReportFragment.requestStoragePermission(callback)
    }

    interface OnStoragePermissionResult {
        fun onPermissionGranted()
        fun onPermissionDenied()
    }
}