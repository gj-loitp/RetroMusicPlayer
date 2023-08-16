package code.roy.retromusic.fragments.library

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.text.parseAsHtml
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import code.roy.monkey.retromusic.extensions.setUpMediaRouteButton
import code.roy.appthemehelper.common.ATHToolbarActivity.getToolbarBackgroundColor
import code.roy.appthemehelper.ThemeStore
import code.roy.retromusic.R
import code.roy.retromusic.databinding.FLibraryBinding
import code.roy.retromusic.dialogs.CreatePlaylistDialog
import code.roy.retromusic.dialogs.ImportPlaylistDialog
import code.roy.retromusic.extensions.whichFragment
import code.roy.retromusic.fragments.base.AbsMainActivityFragment
import code.roy.retromusic.model.CategoryInfo
import code.roy.retromusic.util.PreferenceUtil

class LibraryFragment : AbsMainActivityFragment(R.layout.f_library) {

    private var _binding: FLibraryBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FLibraryBinding.bind(view)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainActivity.setBottomNavVisibility(true)
        mainActivity.setSupportActionBar(binding.toolbar)
        mainActivity.supportActionBar?.title = null
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(
                resId = R.id.action_search,
                args = null,
                navOptions = navOptions
            )
        }
        setupNavigationController()
        setupTitle()
    }

    private fun setupTitle() {
        val color = ThemeStore.accentColor(requireContext())
        val hexColor = String.format("#%06X", 0xFFFFFF and color)
        val appName = "Retro <span  style='color:$hexColor';>Music</span>".parseAsHtml()
        binding.appNameText.text = appName
    }

    private fun setupNavigationController() {
        val navHostFragment = whichFragment<NavHostFragment>(R.id.fragment_container)
        val navController = navHostFragment.navController
        val navInflater = navController.navInflater
        val navGraph = navInflater.inflate(R.navigation.graph_library)

        val categoryInfo: CategoryInfo = PreferenceUtil.libraryCategory.first { it.visible }
        if (categoryInfo.visible) {
            navGraph.setStartDestination(categoryInfo.category.id)
        }
        navController.graph = navGraph
        NavigationUI.setupWithNavController(mainActivity.navigationView, navController)
        navController.addOnDestinationChangedListener { _, _, _ ->
            binding.appBarLayout.setExpanded(/* expanded = */ true, /* animate = */ true)
        }
    }

    override fun onPrepareMenu(menu: Menu) {
        code.roy.appthemehelper.util.ToolbarContentTintHelper.handleOnPrepareOptionsMenu(
            /* activity = */ requireActivity(),
            /* toolbar = */ binding.toolbar
        )
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        code.roy.appthemehelper.util.ToolbarContentTintHelper.handleOnCreateOptionsMenu(
            /* context = */ requireContext(),
            /* toolbar = */ binding.toolbar,
            /* menu = */ menu,
            /* toolbarColor = */ getToolbarBackgroundColor(binding.toolbar)
        )
        //Setting up cast button
        requireContext().setUpMediaRouteButton(menu)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> findNavController().navigate(
                resId = R.id.settings_fragment,
                args = null,
                navOptions = navOptions
            )

            R.id.action_import_playlist -> ImportPlaylistDialog().show(
                /* manager = */ childFragmentManager,
                /* tag = */ "ImportPlaylist"
            )

            R.id.action_add_to_playlist -> CreatePlaylistDialog.create(emptyList()).show(
                /* manager = */ childFragmentManager,
                /* tag = */ "ShowCreatePlaylistDialog"
            )
        }
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
