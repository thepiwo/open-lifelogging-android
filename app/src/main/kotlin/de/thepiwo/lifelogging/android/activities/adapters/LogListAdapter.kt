package de.thepiwo.lifelogging.android.activities.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.mcxiaoke.koi.ext.onClick
import de.thepiwo.lifelogging.android.api.models.LogEntityReturn
import de.thepiwo.lifelogging.android.api.models.LogList
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.textView


class LogListAdapter(private val logList: LogList) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val logItem = getItem(position)

        return with(parent!!.context) {
            relativeLayout {
                textView("${logItem.createdAtClientString()}: ${logItem.data.latitude}, ${logItem.data.longitude}")
                        .onClick { openMap(this.context, logItem) }
            }
        }
    }

    private fun openMap(context: Context, logItem: LogEntityReturn) {
        val uri = "geo:${logItem.data.latitude},${logItem.data.longitude}?q=${logItem.data.latitude},${logItem.data.longitude}(Location+${logItem.createdAtClientString()})"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        context.startActivity(intent)
    }

    override fun getItem(position: Int): LogEntityReturn = logList.logs[position]

    override fun getItemId(position: Int): Long = getItem(position).id!!

    override fun getCount(): Int = logList.logs.size
}