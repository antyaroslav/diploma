package ru.edu.qamid.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.edu.qamid.R
import ru.edu.qamid.adapter.NewsListAdapter
import ru.edu.qamid.databinding.FragmentMainBinding
import ru.edu.qamid.utils.Utils
import ru.edu.qamid.viewmodel.AuthViewModel
import ru.edu.qamid.viewmodel.NewsViewModel

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_main) {
    private lateinit var binding: FragmentMainBinding
    private val newsViewModel: NewsViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            newsViewModel.onRefresh()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                newsViewModel.loadNewsExceptionEvent.collect {
                    if (::binding.isInitialized) {
                        binding.mainSwipeRefresh.isRefreshing = false
                    }
                    showErrorToast(R.string.error)
                }
            }
        }
    }

    @SuppressLint("Recycle")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentMainBinding.bind(view)

        val mainMenu = PopupMenu(
            context,
            binding.mainAppBar.mainMenuImageButton
        )
        mainMenu.inflate(R.menu.menu_main)
        val menuItemMain = mainMenu.menu.getItem(0)
        menuItemMain.isEnabled = false
        binding.mainAppBar.mainMenuImageButton.setOnClickListener {
            mainMenu.show()
        }
        mainMenu.setOnMenuItemClickListener {
            when (it.itemId) {

                R.id.menu_item_news -> {
                    findNavController().navigate(R.id.action_mainFragment_to_newsListFragment)
                    true
                }

                else -> false
            }
        }

        val authorizationMenu = PopupMenu(
            context,
            binding.mainAppBar.authorizationImageButton
        )
        authorizationMenu.inflate(R.menu.authorization)

        binding.mainAppBar.authorizationImageButton.setOnClickListener {
            authorizationMenu.show()
        }

        binding.mainAppBar.ourMissionImageButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_our_mission_fragment)
        }

        authorizationMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.authorization_logout_menu_item -> {
                    authViewModel.logOut()
                    findNavController().navigate(R.id.action_mainFragment_to_authFragment)
                    true
                }

                else -> false
            }
        }

        binding.mainNewsListContainer.apply {
            newsSortButton.visibility = View.GONE
            newsFilterButton.visibility = View.GONE
            newsEditButton.visibility = View.GONE

            expandMaterialButton.setOnClickListener {
                when (allNewsTextView.visibility) {
                    View.GONE -> {
                        allNewsTextView.visibility = View.VISIBLE
                        allNewsCardsBlockConstraintLayout.visibility = View.VISIBLE
                        expandMaterialButton.setIconResource(R.drawable.expand_less_24)
                    }

                    else -> {
                        allNewsTextView.visibility = View.GONE
                        allNewsCardsBlockConstraintLayout.visibility = View.GONE
                        expandMaterialButton.setIconResource(R.drawable.expand_more_24)
                    }
                }
            }

            allNewsTextView.setOnClickListener {
                if (Utils.isOnline(requireContext())) {
                    findNavController().navigate(R.id.action_mainFragment_to_newsListFragment)
                } else {
                    showErrorToast(R.string.error)
                }
            }
        }

        val newsListAdapter = NewsListAdapter(newsViewModel)
        binding.mainNewsListContainer.newsListRecyclerView.adapter =
            newsListAdapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                newsViewModel.data.collectLatest {
                    binding.mainSwipeRefresh.isRefreshing = false
                    newsListAdapter.submitList(it.take(3))
                }
            }
        }

        binding.mainSwipeRefresh.setOnRefreshListener {
            newsViewModel.onRefresh()
        }
    }

    private fun showErrorToast(text: Int) {
        Toast.makeText(
            requireContext(),
            text,
            Toast.LENGTH_LONG
        ).show()
    }
}
