package com.edgardo.corasensor


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class medicionListFragment : Fragment() {

    var onMedicionClick: ((Medicion) -> Unit)? = null
        set(value) {
            field = value
            medicionAdapter?.listener = value
        }

    var mediciones: List<Medicion>
        get() = medicionAdapter?.mediciones ?: emptyList()
        set(value) {
            medicionAdapter = MedicionAdapter(value, medicionAdapter?.listener)

            with(view as RecyclerView) {
                adapter = medicionAdapter
            }
        }

    private var medicionAdapter : MedicionAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let{
            mediciones = it.getParcelableArrayList(MEDICION_ARRAY)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_list_medicion, container, false)

        val medicionesList = mediciones

        if(medicionesList != null) {
            medicionAdapter = MedicionAdapter(medicionesList, onMedicionClick).apply {
                listener = onMedicionClick
            }
        }

        // Set the adapter
        if (rootView is RecyclerView) {
            with(rootView) {
                layoutManager = android.support.v7.widget.LinearLayoutManager(context)
                adapter = medicionAdapter
            }
        }

        return rootView
    }

    companion object {
        const val MEDICION_ARRAY = "medicionarray"
        @JvmStatic
        fun newInstance(mediciones: ArrayList<Medicion>) =
                medicionListFragment().apply {
                    arguments = Bundle().apply {
                        putParcelableArrayList(MEDICION_ARRAY, mediciones)
                    }
                }
    }

}












