package com.shamilovstas.text_encrypt.importdata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.shamilovstas.text_encrypt.R
import com.shamilovstas.text_encrypt.base.ToolbarFragment
import com.shamilovstas.text_encrypt.databinding.FragmentImportDashboardBinding

class ImportDashboardFragment : ToolbarFragment() {

    private var binding: FragmentImportDashboardBinding? = null

    private val filePickLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        findNavController().navigate(
            R.id.action_from_import_dashboard_to_import_message, ImportMessageFragment.fileImportArgs(
                requireNotNull(it)
            )
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentImportDashboardBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() = with(binding!!) {
        btnOpenImportMessage.setOnClickListener {
            findNavController().navigate(R.id.action_from_import_dashboard_to_import_message)
        }

        btnOpenImportFile.setOnClickListener {
            filePickLauncher.launch(arrayOf("application/octet-stream"))
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}