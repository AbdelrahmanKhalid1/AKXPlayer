package com.example.akxplayer.ui.fragments

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.akxplayer.R
import com.example.akxplayer.databinding.FragmentMainBinding
import com.example.akxplayer.ui.activities.SettingsActivity
import com.example.akxplayer.ui.adapters.SectionsPagerAdapter
import com.example.akxplayer.ui.dialogs.AboutDialog

private const val TAG = "LibraryFragment"
class LibraryFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(
            inflater,
            container,
            false
        )
        (activity as AppCompatActivity?)!!.setSupportActionBar(binding.toolbar as Toolbar)
        binding.lifecycleOwner = this
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)

        val item = menu.findItem(R.id.action_search)
        val mSearchView = item.actionView as SearchView

        val searchManager =
            requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))

        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                return s.isEmpty() // if true will not submit
            }

            override fun onQueryTextChange(s: String): Boolean {
//                if (s.isEmpty()) {
//                    menu.findItem(R.id.action_camera_scan).isVisible = true
//                    mAdapter.setItems(ArrayList())
//                } else
//                    menu.findItem(R.id.action_camera_scan).isVisible = false
                return false
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> Toast.makeText(requireContext(), "Search", Toast.LENGTH_SHORT).show()
            R.id.action_setting -> startActivity(Intent(requireActivity(), SettingsActivity::class.java))
            R.id.action_about -> openDialogAbout()
            else ->
                return false
        }
        return true
    }

    private fun openDialogAbout() {
        val aboutDialog = AboutDialog()
        aboutDialog.show(parentFragmentManager, "About Dialog")
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
        ) != PackageManager.PERMISSION_GRANTED
        )

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
