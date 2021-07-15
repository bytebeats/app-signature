package me.bytebeats.app_signature

import android.content.Context
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.EnvironmentCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.bytebeats.app_signature.databinding.FragmentSecondBinding
import me.bytebeats.signature.AppSignature
import java.io.File
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
}