package com.shamilovstas.text_encrypt.importdata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.shamilovstas.text_encrypt.R
import com.shamilovstas.text_encrypt.base.ToolbarFragment
import com.shamilovstas.text_encrypt.databinding.FragmentImportDashboardBinding
import com.shamilovstas.text_encrypt.notes.compose.ComposeNoteFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ImportDashboardFragment(@Inject @JvmField var resultRegistry: ActivityResultRegistry) : ToolbarFragment() {

    private var binding: FragmentImportDashboardBinding? = null

    private val filePickLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument(), resultRegistry) {
        if (it != null) {
            findNavController().navigate(
                R.id.action_from_import_dashboard_to_import_file,
                ComposeNoteFragment.fileImportArgs(it)
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentImportDashboardBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() = with(binding!!) {
        btnOpenImportMessage.setOnClickListener {
            findNavController().navigate(R.id.action_from_import_dashboard_to_import_file, ComposeNoteFragment.importMessageArgs())
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