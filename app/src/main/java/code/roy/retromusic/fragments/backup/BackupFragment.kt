package code.roy.retromusic.fragments.backup

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import code.roy.retromusic.R
import code.roy.retromusic.adapter.backup.BackupAdapter
import code.roy.retromusic.databinding.FBackupBinding
import code.roy.retromusic.extensions.accentColor
import code.roy.retromusic.extensions.accentOutlineColor
import code.roy.retromusic.extensions.materialDialog
import code.roy.retromusic.extensions.showToast
import code.roy.retromusic.helper.BackupHelper
import code.roy.retromusic.helper.sanitize
import code.roy.retromusic.util.Share
import com.afollestad.materialdialogs.input.input
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class BackupFragment : Fragment(R.layout.f_backup), BackupAdapter.BackupClickedListener {

    private val backupViewModel by viewModels<BackupViewModel>()
    private var backupAdapter: BackupAdapter? = null

    private var _binding: FBackupBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FBackupBinding.bind(view)

        initAdapter()
        setupRecyclerview()
        backupViewModel.backupsLiveData.observe(viewLifecycleOwner) {
            if (it.isNotEmpty())
                backupAdapter?.swapDataset(it)
            else
                backupAdapter?.swapDataset(listOf())
        }
        backupViewModel.loadBackups()
        val openFilePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            lifecycleScope.launch(Dispatchers.IO) {
                it?.let {
                    startActivity(Intent(context, RestoreActivity::class.java).apply {
                        data = it
                    })
                }
            }
        }
        binding.createBackup.accentOutlineColor()
        binding.restoreBackup.accentColor()
        binding.createBackup.setOnClickListener {
            showCreateBackupDialog()
        }
        binding.restoreBackup.setOnClickListener {
            openFilePicker.launch(arrayOf("application/octet-stream"))
        }
    }

    private fun initAdapter() {
        backupAdapter = BackupAdapter(
            activity = requireActivity(),
            dataSet = ArrayList(),
            backupClickedListener = this
        )
        backupAdapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkIsEmpty()
            }
        })
    }

    private fun checkIsEmpty() {
        val isEmpty = backupAdapter?.itemCount == 0
        binding.backupTitle.isVisible = !isEmpty
        binding.backupRecyclerview.isVisible = !isEmpty
    }

    private fun setupRecyclerview() {
        binding.backupRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = backupAdapter
        }
    }

    @SuppressLint("CheckResult")
    private fun showCreateBackupDialog() {
        materialDialog().show {
            title(res = R.string.action_rename)
            input(prefill = BackupHelper.getTimeStamp()) { _, text ->
                // Text submitted with the action button
                lifecycleScope.launch {
                    BackupHelper.createBackup(requireContext(), text.sanitize())
                    backupViewModel.loadBackups()
                }
            }
            positiveButton(android.R.string.ok)
            negativeButton(R.string.action_cancel)
            setTitle(R.string.title_new_backup)
        }
    }

    override fun onBackupClicked(file: File) {
        lifecycleScope.launch {
            startActivity(Intent(context, RestoreActivity::class.java).apply {
                data = file.toUri()
            })
        }
    }

    @SuppressLint("CheckResult")
    override fun onBackupMenuClicked(file: File, menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_delete -> {
                try {
                    file.delete()
                } catch (exception: SecurityException) {
                    showToast(R.string.error_delete_backup)
                }
                backupViewModel.loadBackups()
                return true
            }

            R.id.action_share -> {
                Share.shareFile(context = requireContext(), file = file, mimeType = "*/*")
                return true
            }

            R.id.action_rename -> {
                materialDialog().show {
                    title(res = R.string.action_rename)
                    input(prefill = file.nameWithoutExtension) { _, text ->
                        // Text submitted with the action button
                        val renamedFile = File(
                            /* parent = */ file.parent,
                            /* child = */ "$text${BackupHelper.APPEND_EXTENSION}"
                        )
                        if (!renamedFile.exists()) {
                            file.renameTo(renamedFile)
                            backupViewModel.loadBackups()
                        } else {
                            showToast(R.string.file_already_exists)
                        }
                    }
                    positiveButton(android.R.string.ok)
                    negativeButton(R.string.action_cancel)
                    setTitle(R.string.action_rename)
                }
                return true
            }
        }
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
