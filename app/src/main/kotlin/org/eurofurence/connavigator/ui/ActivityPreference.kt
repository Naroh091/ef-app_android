package org.eurofurence.connavigator.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.InputType
import android.view.View
import android.widget.TextView
import com.pawegio.kandroid.textWatcher
import org.eurofurence.connavigator.BuildConfig
import org.eurofurence.connavigator.R
import org.eurofurence.connavigator.pref.AnalyticsPreferences
import org.eurofurence.connavigator.pref.AppPreferences
import org.eurofurence.connavigator.pref.DebugPreferences
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar

class ActivitySettings : AppCompatActivity(), AnkoLogger {
    val ui by lazy { SettingsUi() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ui.setContentView(this)
    }
}

class SettingsUi : AnkoComponent<ActivitySettings> {
    override fun createView(ui: AnkoContext<ActivitySettings>) = with(ui) {
        scrollView {
            verticalLayout {
                toolbar {
                    title = "Settings"
                    backgroundResource = R.color.primary
                }
                verticalLayout {
                    textView("UI settings")

                    checkBox {
                        text = "Show old announcements"
                        isChecked = AppPreferences.showOldAnnouncements
                        setOnClickListener { AppPreferences.showOldAnnouncements = !AppPreferences.showOldAnnouncements }
                    }

                    checkBox {
                        text = "Use shortened dates (Wed, Fri) instead of complete dates (Aug 18)"
                        isChecked = AppPreferences.shortenDates
                        setOnClickListener { AppPreferences.shortenDates = !AppPreferences.shortenDates }
                    }

                    editText {
                        hint = "The count of minutes before an event that we'll send a notification"
                        setText(AppPreferences.notificationMinutesBefore.toString(), TextView.BufferType.EDITABLE)
                        textWatcher {
                            afterTextChanged { text ->
                                if(text!!.isNotEmpty())AppPreferences.notificationMinutesBefore = text.toString().toInt()
                            }
                        }
                        inputType = InputType.TYPE_CLASS_NUMBER
                    }
                }
                verticalLayout {
                    textView("Analytics settings") {
                        padding = dip(25)
                    }

                    checkBox {
                        text = "Track usage with analytics"
                        isChecked = AnalyticsPreferences.enabled
                        setOnCheckedChangeListener { _, value -> AnalyticsPreferences.enabled = value }
                        padding = dip(25)
                    }

                    checkBox {
                        text = "Track performance statistics"
                        isChecked = AnalyticsPreferences.performanceTracking
                        setOnCheckedChangeListener { _, value -> AnalyticsPreferences.performanceTracking = value }
                        padding = dip(25)
                    }
                }

                verticalLayout {
                    textView("Debug settings")

                    checkBox {
                        text = "Tweak event days so it seems like it's the current date"
                        isChecked = DebugPreferences.debugDates
                        setOnClickListener { DebugPreferences.debugDates = !DebugPreferences.debugDates }
                    }

                    editText {
                        hint = "The amount of days to offset by. Must be integer"
                        setText(DebugPreferences.eventDateOffset.toString(), TextView.BufferType.EDITABLE)
                        textWatcher {
                            afterTextChanged { text ->
                                if(text!!.isNotEmpty())DebugPreferences.eventDateOffset = text.toString().toInt()
                            }
                        }
                        inputType = InputType.TYPE_CLASS_NUMBER
                    }

                    checkBox {
                        text = "Schedule notifications in 5 minutes instead of event time"
                        isChecked = DebugPreferences.scheduleNotificationsForTest
                        setOnClickListener { DebugPreferences.scheduleNotificationsForTest = !DebugPreferences.scheduleNotificationsForTest }
                    }

                    visibility = if (BuildConfig.DEBUG) View.VISIBLE else View.GONE
                }

                lparams(matchParent, matchParent)
            }
        }
    }
}

