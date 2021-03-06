package com.carto.advanced.kotlin.sections.styles

import android.content.Context
import com.carto.advanced.kotlin.R
import com.carto.advanced.kotlin.components.PopupButton
import com.carto.advanced.kotlin.components.popupcontent.languagepopupcontent.LanguagePopupContent
import com.carto.advanced.kotlin.components.popupcontent.stylepopupcontent.StylePopupContent
import com.carto.advanced.kotlin.components.popupcontent.switchescontent.Switches3DContent
import com.carto.advanced.kotlin.model.Texts
import com.carto.advanced.kotlin.sections.base.views.MapBaseView
import com.carto.datasources.CartoOnlineTileDataSource
import com.carto.datasources.LocalVectorDataSource
import com.carto.layers.*
import com.carto.styles.CompiledStyleSet
import com.carto.utils.AssetUtils
import com.carto.utils.ZippedAssetPackage
import com.carto.vectortiles.MBVectorTileDecoder

/**
 * Created by aareundo on 03/07/2017.
 */
class StyleChoiceView(context: Context) : MapBaseView(context) {

    companion object {
        // Content descriptions for auto tests
        val LANGUAGE_BUTTON_DESCRIPTION = "language_button"
        val BASEMAP_BUTTON_DESCRIPTION = "basemap_button"

        val STYLE_POSITRON_DESCRIPTION = "style_positron"
    }

    var languageButton = PopupButton(context, R.drawable.icon_language)
    var baseMapButton = PopupButton(context, R.drawable.icon_basemap)
    var switchesButton = PopupButton(context, R.drawable.icon_switches)

    var languageContent = LanguagePopupContent(context)
    var baseMapContent = StylePopupContent(context)
    var switchesContent = Switches3DContent(context)

    var currentLanguage: String = ""
    var currentLayer: TileLayer? = null

    private val vectorSource = LocalVectorDataSource(projection)
    private val vectorLayer = VectorLayer(vectorSource)
    private val clickListener = VectorTileListener(vectorLayer)

    init {

        title = Texts.basemapInfoHeader
        description = Texts.basemapInfoContainer

        currentLayer = addBaseLayer(CartoBaseMapStyle.CARTO_BASEMAP_STYLE_VOYAGER)

        map.layers.add(vectorLayer)
        (currentLayer as VectorTileLayer).vectorTileEventListener = clickListener

        addButton(languageButton)
        addButton(baseMapButton)
        addButton(switchesButton)

        layoutSubviews()

        languageButton.contentDescription = LANGUAGE_BUTTON_DESCRIPTION
        baseMapButton.contentDescription = BASEMAP_BUTTON_DESCRIPTION

        baseMapContent.cartoVector.list[1].contentDescription = STYLE_POSITRON_DESCRIPTION
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
    }

    override fun addListeners() {
        super.addListeners()

        languageButton.setOnClickListener {
            popup.setPopupContent(languageContent)
            popup.popup.header.setText("SELECT A LANGUAGE")
            popup.show()
        }

        baseMapButton.setOnClickListener {
            popup.setPopupContent(baseMapContent)
            popup.popup.header.setText("SELECT A BASEMAP")
            popup.show()
        }

        switchesButton.setOnClickListener {
            popup.setPopupContent(switchesContent)
            popup.popup.header.setText("SWITCH TO TURN 3D ON")
            popup.show()
        }

        switchesContent.setOnClickListener({
//             Just catches background clicks, so they wouldn't close the popup
        })
    }

    override fun removeListeners() {
        super.removeListeners()

        languageButton.setOnClickListener(null)
        baseMapButton.setOnClickListener(null)
        switchesButton.setOnClickListener(null)

        switchesContent.setOnClickListener(null)
    }

    fun updateMapLanguage(language: String) {

        if (currentLayer == null) {
            return
        }

        currentLanguage = language
        
        if (currentLayer is CartoOnlineVectorTileLayer) {
            (currentLayer as CartoOnlineVectorTileLayer).language = language
            (currentLayer as CartoOnlineVectorTileLayer).fallbackLanguage = ""
        }
    }

    fun updateBaseLayer(selection: String, source: String) {

        if (source == StylePopupContent.CartoVectorSource) {

            if (selection == StylePopupContent.Voyager) {
                currentLayer = CartoOnlineVectorTileLayer(CartoBaseMapStyle.CARTO_BASEMAP_STYLE_VOYAGER)
            } else if (selection == StylePopupContent.Positron) {
                currentLayer = CartoOnlineVectorTileLayer(CartoBaseMapStyle.CARTO_BASEMAP_STYLE_POSITRON)
            } else if (selection == StylePopupContent.DarkMatter) {
                currentLayer = CartoOnlineVectorTileLayer(CartoBaseMapStyle.CARTO_BASEMAP_STYLE_DARKMATTER)
            }

        } else if (source == StylePopupContent.CartoRasterSource) {

            if (selection == StylePopupContent.HereSatelliteDaySource) {
                currentLayer = CartoOnlineRasterTileLayer("here.satellite.day@2x")
            } else if (selection == StylePopupContent.HereNormalDaySource) {
                currentLayer = CartoOnlineRasterTileLayer("here.normal.day@2x")
            }
        }

        if (source == StylePopupContent.CartoRasterSource) {
            languageButton.disable()
            switchesButton.disable()
        } else {
            languageButton.enable()
            switchesButton.enable()
        }

        map.layers.clear()
        map.layers.add(currentLayer)

        updateMapLanguage(currentLanguage)

        switchesContent.buildingsSwitch.uncheck()
        switchesContent.textsSwitch.check()

        if (currentLayer is VectorTileLayer) {
            map.layers.add(vectorLayer)
            (currentLayer as VectorTileLayer).vectorTileEventListener = clickListener
        }
    }

    fun updateBuildings(isChecked: Boolean) {

        var value = "1"

        if (isChecked) {
            value = "2"
        }

        updateStyle("buildings", value)

        popup.hide()
    }

    fun updateTexts(isChecked: Boolean) {

        var value = "0"

        if (isChecked) {
            value = "1"
        }

        updateStyle("texts3d", value)

        popup.hide()
    }

    private fun updateStyle(key: String, value: String) {
        val decoder = (currentLayer as? VectorTileLayer)?.tileDecoder as? MBVectorTileDecoder
        decoder?.setStyleParameter(key, value)
    }

}