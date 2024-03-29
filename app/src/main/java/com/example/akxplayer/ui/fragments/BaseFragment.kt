package com.example.akxplayer.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.akxplayer.databinding.FragmentListBinding
import com.example.akxplayer.ui.listeners.OnItemClickListener
import com.example.akxplayer.ui.viewmodels.MediaViewModel

abstract class BaseFragment<i, v : RecyclerView.ViewHolder> : Fragment(), OnItemClickListener {
    lateinit var viewModel: MediaViewModel
    private var _binding: FragmentListBinding? = null
    val binding get() = _binding!!
    lateinit var items: List<i>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListBinding.inflate(
            inflater,
            container,
            false
        )
        viewModel = ViewModelProvider(activity!!).get(MediaViewModel::class.java)

        binding.lifecycleOwner = this
        return binding.root
    }

    fun buildRecycler(layoutManager: RecyclerView.LayoutManager, itemAdapter: RecyclerView.Adapter<v>) {
        binding.recycler.apply {
            adapter = itemAdapter
            this.layoutManager = layoutManager
            setHasFixedSize(true)
        }
    }
}