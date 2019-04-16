package com.example.coincollector

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.coincollector.dummy.DummyContent
import com.example.coincollector.models.Coin
import kotlinx.android.synthetic.main.activity_item_detail.*
import kotlinx.android.synthetic.main.item_detail.view.*
import org.json.JSONObject

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a [ItemListActivity]
 * in two-pane mode (on tablets) or a [ItemDetailActivity]
 * on handsets.
 */
class ItemDetailFragment : Fragment() {

    /**
     * The dummy content this fragment is presenting.
     */
    private var item: Coin? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey("ID")) {
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                item = Coin(it.getString("ID"), it.getString("Name"), it.getString("Country"), it.getString("jsonInfo"))
                activity?.toolbar_layout?.title = "Detalles"
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.item_detail, container, false)

        // Show the dummy content as text in a TextView.
        item?.let {
            val data = JSONObject(it.jsonInfo)
            rootView.item_detail.text = "Moneda: "+it.name+"\n"+"País: "+it.country+"\n"+"Año: "+data.getString("year")+"\n"+"Símbolo: "+data.getString("symbol")+"\n"+"Estado: "+data.getString("state")+"\n\n"+"Descripción: "+data.getString("desc")+"\n"
        }

        return rootView
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val ARG_ITEM_ID = "item_id"
    }
}
