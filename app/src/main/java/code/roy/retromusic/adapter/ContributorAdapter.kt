package code.roy.retromusic.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import code.roy.retromusic.R
import code.roy.retromusic.extensions.openUrl
import code.roy.retromusic.model.Contributor
import code.roy.retromusic.views.RetroShapeableImageView
import com.bumptech.glide.Glide

class ContributorAdapter(
    private var contributors: List<Contributor>,
) : RecyclerView.Adapter<ContributorAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == HEADER) {
            ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    /* resource = */ R.layout.v_item_contributor_header,
                    /* root = */ parent,
                    /* attachToRoot = */ false
                )
            )
        } else ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                /* resource = */ R.layout.v_item_contributor,
                /* root = */ parent,
                /* attachToRoot = */ false
            )
        )
    }

    companion object {
        const val HEADER: Int = 0
        const val ITEM: Int = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            HEADER
        } else {
            ITEM
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contributor = contributors[position]
        holder.bindData(contributor)
        holder.itemView.setOnClickListener {
            it?.context?.openUrl(contributors[position].link)
        }
    }

    override fun getItemCount(): Int {
        return contributors.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun swapData(it: List<Contributor>) {
        contributors = it
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val text: TextView = itemView.findViewById(R.id.text)
        val image: RetroShapeableImageView = itemView.findViewById(R.id.icon)

        internal fun bindData(contributor: Contributor) {
            title.text = contributor.name
            text.text = contributor.summary
            Glide.with(image.context)
                .load("file:///android_asset/images/${contributor.image}".toUri())
                .error(R.drawable.ic_account)
                .placeholder(R.drawable.ic_account)
                .into(image)
        }
    }
}
