package ru.edu.qamid.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.os.BundleCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.edu.qamid.R
import ru.edu.qamid.adapter.NewsListAdapter
import ru.edu.qamid.databinding.FragmentNewsListBinding
import ru.edu.qamid.dto.NewsFilterArgs
import ru.edu.qamid.enum.FragmentsTags
import ru.edu.qamid.utils.Utils.convertNewsCategory
import ru.edu.qamid.viewmodel.AuthViewModel
import ru.edu.qamid.viewmodel.NewsViewModel

@AndroidEntryPoint
class NewsListFragment : Fragment(R.layout.fragment_news_list) {
    private lateinit var binding: FragmentNewsListBinding
    private val viewModel: NewsViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            viewModel.onRefresh()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNewsListBinding.bind(view)
        val mainMenu = PopupMenu(
            context,
            binding.newsListAppBar.mainMenuImageButton
        )
        mainMenu.inflate(R.menu.menu_main)
        val menuItemNews = mainMenu.menu.getItem(1)
        menuItemNews.isEnabled = false

        mainMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_item_main -> {
                    findNavController().navigate(R.id.action_newsListFragment_to_mainFragment)
                    true
                }
                else -> {
                    false
                }
            }
        }

        binding.newsListAppBar.ourMissionImageButton.setOnClickListener {
            findNavController().navigate(R.id.action_newsListFragment_to_our_mission_fragment)
        }

        val authorizationMenu = PopupMenu(
            context,
            binding.newsListAppBar.authorizationImageButton
        )
        authorizationMenu.inflate(R.menu.authorization)

        binding.newsListAppBar.authorizationImageButton.setOnClickListener {
            authorizationMenu.show()
        }

        authorizationMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.authorization_logout_menu_item -> {
                    authViewModel.logOut()
                    findNavController().navigate(R.id.action_newsListFragment_to_authFragment)
                    true
                }
                else -> false
            }
        }

        binding.apply {
            newsListContainer.allNewsTextView.visibility = View.GONE
            newsListContainer.expandMaterialButton.visibility = View.GONE
        }

        val adapter = NewsListAdapter(viewModel)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest {
                    binding.newsListSwipeRefresh.isRefreshing = false
                    adapter.submitList(it)
                    if (it.isEmpty()) {
                        binding.newsListContainer.emptyNewsListGroup.isVisible = true
                        binding.newsListContainer.newsRetryMaterialButton.setOnClickListener {
                            binding.newsListSwipeRefresh.isRefreshing = true
                            viewModel.onRefresh()
                            binding.newsListSwipeRefresh.isRefreshing = false
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadNewsExceptionEvent.collect {
                    val activity = activity ?: return@collect
                    val dialog = AlertDialog.Builder(activity)
                    dialog.setMessage(R.string.error)
                        .setPositiveButton(R.string.fragment_positive_button) { alertDialog, _ ->
                            alertDialog.cancel()
                        }
                        .create()
                        .show()
                }
            }
        }

        binding.newsListSwipeRefresh.setOnRefreshListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.onRefresh()
                delay(200)
                binding.newsListContainer.newsListRecyclerView.scrollToPosition(
                    0
                )
            }
        }

        with(binding) {
            newsListContainer.newsEditButton.setOnClickListener {
                if (viewModel.currentUser.admin) {
                    findNavController().navigate(
                        R.id.action_newsListFragment_to_newsControlPanelFragment
                    )
                } else {
                    Toast.makeText(
                        requireContext(),
                        R.string.no_rules_for_news_control_panel,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            newsListContainer.newsSortButton.setOnClickListener {
                viewModel.onSortDirectionButtonClicked()
            }

            newsListAppBar.mainMenuImageButton.setOnClickListener {
                mainMenu.show()
            }

            newsListContainer.newsFilterButton.setOnClickListener {
                val action = NewsListFragmentDirections.actionNewsListFragmentToFilterNewsFragment(
                    FragmentsTags.NEWS_LIST_FRAGMENT
                )
                findNavController().navigate(action)
            }
        }

        binding.newsListContainer.newsListRecyclerView.adapter = adapter

        setFragmentResultListener("requestKey") { _, bundle ->
            val args = BundleCompat.getParcelable(bundle, "filterArgs", NewsFilterArgs::class.java)
            viewModel.onFilterNewsClicked(
                args?.category?.let { convertNewsCategory(it) },
                args?.dates?.get(0),
                args?.dates?.get(1)
            )
        }
    }
}
