package ru.edu.qamid.ui

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
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
import ru.edu.qamid.adapter.OnOurMissionItemClickListener
import ru.edu.qamid.adapter.OurMissionItemListAdapter
import ru.edu.qamid.databinding.FragmentOurMissionBinding
import ru.edu.qamid.ui.viewdata.OurMissionItemViewData
import ru.edu.qamid.viewmodel.AuthViewModel
import ru.edu.qamid.viewmodel.OurMissionViewModel

@AndroidEntryPoint
class OurMissionFragment : Fragment(R.layout.fragment_our_mission) {
    private val viewModel: OurMissionViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentOurMissionBinding.bind(view)

        val mainMenu = PopupMenu(
            context,
            binding.ourMissionAppBar.mainMenuImageButton
        )
        mainMenu.inflate(R.menu.menu_main)
        binding.ourMissionAppBar.mainMenuImageButton.setOnClickListener {
            mainMenu.show()
        }
        mainMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_item_main -> {
                    findNavController().navigate(R.id.action_our_mission_fragment_to_mainFragment)
                    true
                }

                R.id.menu_item_news -> {
                    findNavController().navigate(R.id.action_our_mission_fragment_to_newsListFragment)
                    true
                }

                else -> false
            }
        }

        val authorizationMenu = PopupMenu(
            context,
            binding.ourMissionAppBar.authorizationImageButton
        )
        authorizationMenu.inflate(R.menu.authorization)

        binding.ourMissionAppBar.authorizationImageButton.setOnClickListener {
            authorizationMenu.show()
        }
        authorizationMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.authorization_logout_menu_item -> {
                    authViewModel.logOut()
                    findNavController().navigate(R.id.action_our_mission_fragment_to_authFragment)
                    true
                }

                else -> false
            }
        }

        val adapter = OurMissionItemListAdapter(object : OnOurMissionItemClickListener {
            override fun onCard(ourMissionItem: OurMissionItemViewData) {
                viewModel.onCard(ourMissionItem)
            }
        }, viewModel)

        binding.ourMissionItemListRecyclerView.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest {
                    adapter.submitList(it)
                }
            }
        }
    }
}