package com.carto.advanced.kotlin.sections.base

import android.app.Activity
import android.content.Context
import android.view.View
import com.carto.advanced.kotlin.R
import com.carto.advanced.kotlin.components.PopupButton
import com.carto.advanced.kotlin.components.popupcontent.packagepopupcontent.PackagePopupContent
import com.carto.layers.CartoBaseMapStyle
import com.carto.layers.CartoOfflineVectorTileLayer
import com.carto.layers.CartoOnlineVectorTileLayer
import com.carto.packagemanager.CartoPackageManager
import com.carto.packagemanager.PackageStatus

/**
 * Created by aareundo on 12/07/2017.
 */
class PackageDownloadBaseView(context: Context) : DownloadBaseView(context) {

    var countryButton: PopupButton? = null

    var packageContent: PackagePopupContent? = null

    var onlineLayer: CartoOnlineVectorTileLayer? = null
    var offlineLayer: CartoOfflineVectorTileLayer? = null

    var currentDownload: com.carto.advanced.kotlin.utils.Package? = null
    var folder = ""

    var manager: CartoPackageManager? = null

    init {
        countryButton = PopupButton(context, R.drawable.icon_global)
        addButton(countryButton!!)

        packageContent = PackagePopupContent(context)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
    }

    override fun addListeners() {
        super.addListeners()

        countryButton?.setOnClickListener {
            popup.setPopupContent(packageContent!!)
            popup.popup.header.setText("SELECT A PACKAGE")
            popup.show()
        }
    }

    override fun removeListeners() {
        super.removeListeners()

        countryButton?.setOnClickListener(null)
    }

    fun setOnlineMode() {
        map.layers?.remove(offlineLayer)
        map.layers?.insert(0, onlineLayer)
    }

    fun setOfflineMode(manager: CartoPackageManager) {
        map.layers?.remove(onlineLayer)
        offlineLayer = CartoOfflineVectorTileLayer(manager, CartoBaseMapStyle.CARTO_BASEMAP_STYLE_DEFAULT)
        map.layers?.insert(0, offlineLayer)
    }

    fun updatePackages() {
        packageContent?.addPackages(getPackages())
    }

    fun setOfflineMode() {
        setOfflineMode(manager!!)
    }

    fun onPackageClick(item: com.carto.advanced.kotlin.utils.Package) {

        if (item.isGroup()) {

        }
    }

    fun onStatusChanged(status: PackageStatus) {
        (context as Activity).runOnUiThread {

            if (this.currentDownload == null) {
                // TODO in case a download has been started and the controller is reloaded
                return@runOnUiThread
            }

            val text = "Downloading " + currentDownload?.name + ": " + status.progress.toString()
            progressLabel.update(text)
            progressLabel.updateProgressBar(status.progress)

            currentDownload?.status = manager?.getLocalPackageStatus(currentDownload?.id, -1)
            packageContent?.findAndUpdate(currentDownload!!, status.progress)
        }
    }

    fun downloadComplete(id: String) {
        (context as Activity).runOnUiThread {
            if (currentDownload != null) {
                currentDownload?.status = manager?.getLocalPackageStatus(id, -1)
                packageContent?.findAndUpdate(currentDownload!!)
            }
        }
    }

    fun onPopupBackButtonClick() {
        folder = folder.substring(folder.length - 1)
        val lastSlash = folder.lastIndexOf("/")

        if (lastSlash == -1) {
            folder = ""
            popup.popup.header.backButton.visibility = View.GONE
        } else {
            folder = folder.substring(lastSlash + 1)
        }

        packageContent?.addPackages(getPackages())
    }

    fun getPackages(): MutableList<com.carto.advanced.kotlin.utils.Package> {

        val list = mutableListOf<com.carto.advanced.kotlin.utils.Package>()

        return  list
    }
}