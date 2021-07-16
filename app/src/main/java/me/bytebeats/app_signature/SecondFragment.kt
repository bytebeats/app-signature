package me.bytebeats.app_signature

import android.content.Context
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.bytebeats.app_signature.databinding.FragmentSecondBinding
import me.bytebeats.signature.AppSignature
import me.bytebeats.spm.StoragePermissionManager
import java.io.File
import java.io.FileOutputStream
import java.io.FilenameFilter

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val adapter by lazy {
        AppSignatureAdapter(requireContext()).apply {
            onItemClickListener = object : OnItemClickListener {
                override fun onItemClick(position: Int) {
                    binding.textviewSignature.text = apkFiles[position].absolutePath
                }
            }
        }
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            binding.textviewSecond.setText(it.path)
        }
    }

    private val apkFiles by lazy {
        Environment.getExternalStorageDirectory().listFiles(ExtFilter("apk"))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonZero.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
        binding.allInstalledAppSignature.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.allInstalledAppSignature.adapter = adapter
        adapter.update(apkFiles?.map { it.absolutePath })
        binding.textviewSignature.setOnClickListener {
            binding.textviewSignature.text = AppSignature.getLocalApkFirstCryptedSignature(
                requireContext(),
                binding.textviewSecond.text.toString()
            )
        }
        binding.buttonSecond.setOnClickListener {
//            launcher.launch(APK_MIME_TYPE)
            binding.textviewSecond.setText(
                File(
                    requireContext().filesDir,
                    APK_FILE_NAME
                ).absolutePath
            )
        }

        binding.buttonThird.setOnClickListener {
            StoragePermissionManager.with(requireActivity()).requestStoragePermission(object :
                StoragePermissionManager.OnStoragePermissionResult {
                override fun onPermissionDenied() {
                    Toast.makeText(requireContext(), "No Storage Permission", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onPermissionGranted() {
                    File(
                        requireContext().filesDir,
                        APK_FILE_NAME
                    ).let {
                        if (it.exists()) it.delete()
                        if (!it.exists()) it.parentFile.mkdirs()
                        FileOutputStream(it).use { fos ->
                            resources.openRawResource(R.raw.app_debug_apk).use { ris ->
                                fos.write(ris.readBytes())
                            }
                        }
                    }
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class ExtFilter(private val suffix: String) : FilenameFilter {

        override fun accept(dir: File?, name: String?): Boolean {
            return name?.endsWith(".$suffix") ?: false
        }
    }


    private class AppSignatureAdapter(val context: Context) :
        RecyclerView.Adapter<AppSignatureAdapter.AppSignatureViewHolder>() {
        var onItemClickListener: OnItemClickListener? = null

        private val localApks = mutableListOf<String>()

        fun update(signatures: Collection<String>?) {
            this.localApks.clear()
            if (signatures != null) {
                this.localApks.addAll(signatures)
            }
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppSignatureViewHolder {
            return AppSignatureViewHolder(
                LayoutInflater.from(context).inflate(R.layout.list_item_app_signature, null)
            )
        }

        override fun onBindViewHolder(holder: AppSignatureViewHolder, position: Int) {
            holder.set(getItem(position))
        }

        override fun getItemCount(): Int {
            return localApks.size
        }

        fun getItem(position: Int): String {
            return localApks[position]
        }

        private inner class AppSignatureViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val appSignature = view.findViewById<TextView>(R.id.item_app_signature)

            init {
                appSignature.setOnClickListener { onItemClickListener?.onItemClick(layoutPosition) }
            }

            fun set(signature: String) {
                appSignature.text = signature
            }
        }
    }

    private interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    companion object {
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
        private const val APK_FILE_NAME = "app-signature.apk"
    }
}