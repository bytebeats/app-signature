package me.bytebeats.app_signature

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.bytebeats.app_signature.databinding.FragmentFirstBinding
import me.bytebeats.signature.AppSignature

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val adapter by lazy { AppSignatureAdapter(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonZero.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
        var first = false
        binding.buttonFirst.setOnClickListener {
            first = !first
            binding.appSignatureDisplayer.text = if (first)
                "${AppSignature.getAppFirstCryptedSignature(requireContext())}\n\n${
                    AppSignature.getAppFirstDecryptedSignature(requireContext())
                }"
            else "${AppSignature.getAppTotalCryptedSignature(requireContext())}\n\n${
                AppSignature.getAppTotalDecryptedSignature(requireContext())
            }"
        }
        var encrypted = true
        binding.buttonSecond.setOnClickListener {
            encrypted = !encrypted
            if (encrypted)
                adapter.update(AppSignature.getInstalledAppFirstCryptedSignatures(requireContext()))
            else
                adapter.update(AppSignature.getInstalledAppFirstDecryptedSignature(requireContext()))
        }
        binding.allInstalledAppSignature.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.allInstalledAppSignature.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class AppSignatureAdapter(val context: Context) :
        RecyclerView.Adapter<AppSignatureAdapter.AppSignatureViewHolder>() {

        private val signatures = mutableListOf<String>()

        fun update(signatures: Collection<String>) {
            this.signatures.clear()
            this.signatures.addAll(signatures)
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
            return signatures.size
        }

        fun getItem(position: Int): String {
            return signatures[position]
        }

        private class AppSignatureViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val appSignature = view.findViewById<TextView>(R.id.item_app_signature)
            fun set(signature: String) {
                appSignature.text = signature
            }
        }
    }
}