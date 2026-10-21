package ru.edu.qamid.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.core.os.BundleCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.edu.qamid.R
import ru.edu.qamid.adapter.NewsControlPanelListAdapter
import ru.edu.qamid.adapter.NewsOnInteractionListener
import ru.edu.qamid.databinding.FragmentNewsControlPanelBinding
import ru.edu.qamid.dto.News
import ru.edu.qamid.dto.NewsFilterArgs
import ru.edu.qamid.dto.NewsWithCategory
import ru.edu.qamid.enum.FragmentsTags
import ru.edu.qamid.utils.Utils
import ru.edu.qamid.viewmodel.AuthViewModel
import ru.edu.qamid.viewmodel.NewsControlPanelViewModel

@AndroidEntryPoint
class NewsControlPanelFragment : Fragment(R.layout.fragment_news_control_panel) {
    private lateinit var binding: FragmentNewsControlPanelBinding
    private val viewModel: NewsControlPanelViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            viewModel.onRefresh()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNewsControlPanelBinding.bind(view)

        val mainMenu = PopupMenu(
            context,
            binding.newsControlPanelAppBar.mainMenuImageButton
        )
        mainMenu.inflate(R.menu.menu_main)
        binding.newsControlPanelAppBar
            .mainMenuImageButton.setOnClickListener {
                mainMenu.show()
            }
        mainMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_item_main -> {
                    findNavController().navigate(R.id.action_newsControlPanelFragment_to_mainFragment)
                    true
                }

                R.id.menu_item_news -> {
                    findNavController().navigate(R.id.action_newsControlPanelFragment_to_newsListFragment)
                    true
                }

                else -> false
            }
        }

        binding.newsControlPanelAppBar.ourMissionImageButton.setOnClickListener {
            findNavController().navigate(R.id.action_newsControlPanelFragment_to_our_mission_fragment)
        }

        val authorizationMenu = PopupMenu(
            context,
            binding.newsControlPanelAppBar.authorizationImageButton
        )
        authorizationMenu.inflate(R.menu.authorization)

        binding.newsControlPanelAppBar.authorizationImageButton.setOnClickListener {
            authorizationMenu.show()
        }

        authorizationMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.authorization_logout_menu_item -> {
                    authViewModel.logOut()
                    findNavController().navigate(R.id.action_newsControlPanelFragment_to_authFragment)
                    true
                }

                else -> false
            }
        }

        val activity = activity ?: return
        val dialog = AlertDialog.Builder(activity)

        val adapter = NewsControlPanelListAdapter(object : NewsOnInteractionListener {
            override fun onCard(newsItem: News) {
                viewModel.onCard(newsItem)
            }

            override fun onEdit(newItemWithCategory: NewsWithCategory) {
                val action = NewsControlPanelFragmentDirections
                    .actionNewsControlPanelFragmentToCreateEditNewsFragment(newItemWithCategory)
                findNavController().navigate(action)
            }

            override fun onRemove(newItemWithCategory: NewsWithCategory) {
                dialog.setMessage(R.string.irrevocable_deletion)
                    .setPositiveButton(R.string.fragment_positive_button) { alertDialog, _ ->
                        newItemWithCategory.newsItem.id?.let { viewModel.remove(it) }
                        alertDialog.cancel()
                    }
                    .setNegativeButton(R.string.cancel) { alertDialog, _ ->
                        alertDialog.cancel()
                    }
                    .create()
                    .show()
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest { state ->
                    adapter.submitList(state)
                    binding.controlPanelEmptyNewsListGroup.isVisible =
                        state.isEmpty()
                    binding.layoutBackgroundImageView.isGone = state.isEmpty()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadNewsExceptionEvent.collect {
                    dialog.setMessage(R.string.error)
                        .setPositiveButton(R.string.fragment_positive_button) { dialog, _ ->
                            dialog.cancel()
                        }
                        .create()
                        .show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.removeNewsItemExceptionEvent.collect {
                    dialog.setMessage(R.string.error_removing)
                        .setPositiveButton(R.string.fragment_positive_button) { dialog, _ ->
                            dialog.cancel()
                        }
                        .create()
                        .show()
                }
            }
        }

        with(binding) {
            newsSortButton.setOnClickListener {
                viewModel.onSortDirectionButtonClicked()
                binding.newsListRecyclerView.post {
                    binding.newsListRecyclerView.scrollToPosition(
                        0
                    )
                }
            }

            addNewsImageView.setOnClickListener {
                findNavController().navigate(
                    R.id.action_newsControlPanelFragment_to_createEditNewsFragment
                )
            }

            newsFilterButton.setOnClickListener {
                val action =
                    NewsControlPanelFragmentDirections.actionNewsControlPanelFragmentToFilterNewsFragment(
                        FragmentsTags.NEWS_CONTROL_PANEL_FRAGMENT
                    )
                findNavController().navigate(action)
            }

            controlPanelNewsRetryMaterialButton.setOnClickListener {
                viewModel.onRefresh()
            }
        }

        binding.newsListRecyclerView.adapter = adapter

        binding.newsControlPanelSwipeToRefresh.setOnRefreshListener {
            viewModel.onRefresh()
            binding.newsControlPanelSwipeToRefresh.isRefreshing = false
        }

        setFragmentResultListener("requestKey") { _, bundle ->
            val args = BundleCompat.getParcelable(bundle, "filterArgs", NewsFilterArgs::class.java)
            viewModel.onFilterNewsClicked(
                args?.category?.let { Utils.convertNewsCategory(it) },
                args?.dates?.get(0),
                args?.dates?.get(1),
                args?.status
            )
        }
    }
}
