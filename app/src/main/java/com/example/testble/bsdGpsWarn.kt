package com.example.testble


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton


class bsdGpsWarn: BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val myview= inflater.inflate(R.layout.itemforbsdgpswarn, container, false)
        return myview
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val btnAccept=view.findViewById<MaterialButton>(R.id.BTNAccept)
        btnAccept.setOnClickListener {
            val myclass= GpsTurningOn()
            myclass.displayLocationSettingsRequest(requireActivity(),requireContext())
            dismiss()
        }
        super.onViewCreated(view, savedInstanceState)
    }
}