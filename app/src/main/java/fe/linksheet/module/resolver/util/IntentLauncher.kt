package fe.linksheet.module.resolver.util //[span_0](start_span)[span_0](end_span)

import android.content.ComponentName //[span_1](start_span)[span_1](end_span)
import android.content.Intent //[span_2](start_span)[span_2](end_span)
import android.content.pm.PackageManager //[span_3](start_span)[span_3](end_span)
import android.net.Uri //[span_4](start_span)[span_4](end_span)
import app.linksheet.feature.app.core.ActivityAppInfo //[span_5](start_span)[span_5](end_span)
import app.linksheet.feature.browser.core.Browser //[span_6](start_span)[span_6](end_span)
import app.linksheet.feature.profile.core.CrossProfile //[span_7](start_span)[span_7](end_span)
import app.linksheet.lib.flavors.LinkSheetReferrer //[span_8](start_span)[span_8](end_span)
import fe.composekit.core.AndroidPackageUri //[span_9](start_span)[span_9](end_span)
import fe.composekit.core.Scheme //[span_10](start_span)[span_10](end_span)

interface IntentLauncher {
    fun launch(info: ActivityAppInfo, intent: Intent, referrer: Uri?, browser: Browser?): LaunchIntent //[span_11](start_span)[span_11](end_span)
}

class DefaultIntentLauncher(
    val getComponentEnabledSetting: (ComponentName) -> Int, //[span_12](start_span)[span_12](end_span)
    val showAsReferrer: () -> Boolean, //[span_13](start_span)[span_13](end_span)
    val selfPackage: String, //[span_14](start_span)[span_14](end_span)
) : IntentLauncher {

    override fun launch(info: ActivityAppInfo, intent: Intent, referrer: Uri?, browser: Browser?): LaunchIntent {
        if (isComponentDisabled(info)) {
            return LaunchMainIntent(createMainIntent(intent, info.packageName)) //[span_15](start_span)[span_15](end_span)
        }

        browser?.requestPrivateBrowsing(intent) //[span_16](start_span)[span_16](end_span)

        intent.component = info.componentName //[span_17](start_span)[span_17](end_span)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) //[span_18](start_span)[span_18](end_span)

        // --- INJECTED PARTIAL CUSTOM TAB EXTRAS ---
        intent.putExtra("androidx.browser.customtabs.extra.INITIAL_ACTIVITY_HEIGHT_PX", 400)
        intent.putExtra("androidx.browser.customtabs.extra.ACTIVITY_HEIGHT_RESIZE_BEHAVIOR", 2)
        // ------------------------------------------

        val showAsReferrer = showAsReferrer() //[span_19](start_span)[span_19](end_span)
        intent.putExtra(
            LinkSheetReferrer.EXTRA_REFERRER, //[span_20](start_span)[span_20](end_span)
            if (showAsReferrer) AndroidPackageUri.create(Scheme.Package, selfPackage) else referrer //[span_21](start_span)[span_21](end_span)
        )

        if (!showAsReferrer) {
            intent.putExtra(Intent.EXTRA_REFERRER, referrer) //[span_22](start_span)[span_22](end_span)
        }

        return LaunchViewIntent(intent) //[span_23](start_span)[span_23](end_span)
    }

    private fun isComponentDisabled(info: ActivityAppInfo): Boolean {
        val status = getComponentEnabledSetting(info.componentName) //[span_24](start_span)[span_24](end_span)
        return status == PackageManager.COMPONENT_ENABLED_STATE_DISABLED //[span_25](start_span)[span_25](end_span)
    }

    private fun createMainIntent(intent: Intent, packageName: String): Intent {
        val mainIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER) //[span_26](start_span)[span_26](end_span)
        mainIntent.selector = intent
            .addCategory(Intent.CATEGORY_BROWSABLE) //[span_27](start_span)[span_27](end_span)
            .setPackage(packageName) //[span_28](start_span)[span_28](end_span)

        return mainIntent //[span_29](start_span)[span_29](end_span)
    }
}

sealed interface Launchable { //[span_30](start_span)[span_30](end_span)

}
sealed class LaunchIntent(val intent: Intent) : Launchable { //[span_31](start_span)[span_31](end_span)

}

class LaunchOtherProfileIntent(val profile: CrossProfile, val url: String) : Launchable //[span_32](start_span)[span_32](end_span)
class LaunchRawIntent(intent: Intent) : LaunchIntent(intent) //[span_33](start_span)[span_33](end_span)
class LaunchMainIntent(intent: Intent) : LaunchIntent(intent) //[span_34](start_span)[span_34](end_span)
class LaunchViewIntent(intent: Intent) : LaunchIntent(intent) //[span_35](start_span)[span_35](end_span)
