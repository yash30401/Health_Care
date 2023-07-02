package com.healthcare.yash.preeti.ui.fragments
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.leanback.app.BrowseFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.ObjectAdapter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowHeaderPresenter
import androidx.leanback.widget.VerticalGridPresenter
import com.healthcare.yash.preeti.AppCard
import com.healthcare.yash.preeti.R

class TvLauncherFragment : BrowseFragment() {
    private lateinit var adapter: ArrayObjectAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = getString(R.string.app_name)
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        adapter = ArrayObjectAdapter(ListRowPresenter())

        val apps = getInstalledApps()
        for (app in apps) {
            val appIcon = app.applicationInfo.loadIcon(activity.packageManager)
            val appLabel = app.applicationInfo.loadLabel(activity.packageManager)
            val appIntent = activity.packageManager.getLaunchIntentForPackage(app.packageName)

            if (appIntent != null) {
                val appClickedListener = OnItemViewClickedListener { _, item, _, _ ->
                    try {
                        val intent = (item as AppCard).appIntent
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(activity, "Failed to launch app", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "Failed to launch app", e)
                    }
                }

                val appCard = AppCard(activity)
                appCard.setAppIcon(appIcon)
                appCard.setAppLabel(appLabel)
                appCard.appIntent = appIntent

                adapter.add(appCard)
                onItemViewClickedListener = appClickedListener
            }

        }



        // Set the adapter for the browse fragment
        this.adapter = adapter
    }

    private fun getInstalledApps(): List<PackageInfo> {
        val installedApps = mutableListOf<PackageInfo>()
        val packageManager = activity.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val resolveInfoList = packageManager.queryIntentActivities(intent, 0)

        for (resolveInfo in resolveInfoList) {
            try {
                val packageName = resolveInfo.activityInfo.packageName
                val packageInfo = packageManager.getPackageInfo(packageName, 0)
                if (packageInfo != null) {
                    installedApps.add(packageInfo)
                }
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e(TAG, "Error getting package info for app", e)
            }
        }

        return installedApps
    }

    companion object {
        private const val TAG = "TvLauncherFragment"
    }
}
