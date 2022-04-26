package com.example.android.foodieexpressserver.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.android.foodieexpressserver.R
import com.example.android.foodieexpressserver.model.eventBus.LoadOrderEvent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.greenrobot.eventbus.EventBus

class BottomSheetOrderFragment : BottomSheetDialogFragment() {

    lateinit var placed_filter:LinearLayout
    lateinit var shipping_filter:LinearLayout
    lateinit var shipped_filter:LinearLayout
    lateinit var cancelled_filter:LinearLayout

    companion object {
        val instance: BottomSheetOrderFragment ? =null
            get() = field ?: BottomSheetOrderFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val itemView = inflater.inflate(R.layout.fragment_order_filter,container,false)
        placed_filter = itemView.findViewById(R.id.placed_filter)
        shipping_filter = itemView.findViewById(R.id.shipping_filter)
        shipped_filter = itemView.findViewById(R.id.shipped_filter)
        cancelled_filter = itemView.findViewById(R.id.cancelled_filter)
        return itemView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        placed_filter.setOnClickListener {
            EventBus.getDefault().postSticky(LoadOrderEvent(0))
            dismiss()
        }
        shipping_filter.setOnClickListener {
            EventBus.getDefault().postSticky(LoadOrderEvent(1))
            dismiss()
        }


        shipped_filter.setOnClickListener {
            EventBus.getDefault().postSticky(LoadOrderEvent(2))
            dismiss()
        }

        cancelled_filter.setOnClickListener {
            EventBus.getDefault().postSticky(LoadOrderEvent(-1))
            dismiss()
        }
    }
}