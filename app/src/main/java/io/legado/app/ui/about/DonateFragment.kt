package io.legado.app.ui.about

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.legado.app.R
import io.legado.app.utils.openUrl


class DonateFragment : PreferenceFragmentCompat() {

    private val zfbSkRwmUrl =
        "http://www.liuhuiling.cn/images/zfb-1690893415320.jpg"
    private val wxZsRwmUrl =
        "http://www.liuhuiling.cn/images/wx-1690893415243.png"


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.donate)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView.overScrollMode = View.OVER_SCROLL_NEVER
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            "wxZsm" -> requireContext().openUrl(wxZsRwmUrl)

            "zfbSkRwm" -> requireContext().openUrl(zfbSkRwmUrl)
        }
        return super.onPreferenceTreeClick(preference)
    }

}