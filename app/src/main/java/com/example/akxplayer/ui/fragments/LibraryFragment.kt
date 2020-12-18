package com.example.akxplayer.ui.fragments

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.akxplayer.databinding.FragmentMainBinding
import com.example.akxplayer.ui.adapters.SectionsPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

private const val TAG = "LibraryFragment"
class LibraryFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(
            inflater,
            container,
            false
        )
        (activity as AppCompatActivity?)!!.setSupportActionBar(binding.toolbar as Toolbar)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requestPermission()
    }

    private fun requestPermission() {
        if (checkReadExternalStoragePermission()) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                99
            )
        } else {
            setFragments()
        }
    }

    private fun checkReadExternalStoragePermission(): Boolean = (
            ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED)

    private fun setFragments() {
        Log.d(TAG, "setFragments: MainStart stack count = ${parentFragmentManager.backStackEntryCount}")
        val sectionsPagerAdapter =
            SectionsPagerAdapter(
                requireContext(),
                requireActivity()
            )
        val viewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        sectionsPagerAdapter.connectTabWitPager(viewPager, binding.tabs)
        val settings = requireActivity().getSharedPreferences("AppSettingsPref", 0)
        val startPage = settings.getInt("startPage", 0)
        binding.viewPager.currentItem = startPage
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 99) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    99
                )
            } else {
                setFragments()
            }
        }
    }
}