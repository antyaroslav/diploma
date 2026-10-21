package ru.edu.qamid.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.edu.qamid.R
import ru.edu.qamid.databinding.FragmentCreateEditNewsBinding
import ru.edu.qamid.dto.News
import ru.edu.qamid.utils.Utils
import ru.edu.qamid.utils.Utils.convertNewsCategory
import ru.edu.qamid.utils.Utils.saveDateTime
import ru.edu.qamid.utils.Utils.updateDateLabel
import ru.edu.qamid.utils.Utils.updateTimeLabel
import ru.edu.qamid.viewmodel.NewsControlPanelViewModel
import java.time.Instant.now
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@AndroidEntryPoint
class CreateEditNewsFragment : Fragment(R.layout.fragment_create_edit_news) {
    private val viewModel: NewsControlPanelViewModel by viewModels()
    private val args: CreateEditNewsFragmentArgs by navArgs()

    private lateinit var vPublishDatePicker: TextInputEditText
    private lateinit var vPublishTimePicker: TextInputEditText
    private lateinit var binding: FragmentCreateEditNewsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            viewModel.saveNewsItemExceptionEvent.collect {
                showErrorToast(R.string.error_saving)
            }
        }
        lifecycleScope.launch {
            viewModel.editNewsItemExceptionEvent.collect {
                showErrorToast(R.string.error_saving)
            }
        }
        lifecycleScope.launch {
            viewModel.newsItemCreatedEvent.collect {
                findNavController().navigateUp()
            }
        }
        lifecycleScope.launch {
            viewModel.editNewsItemSavedEvent.collect {
                findNavController().navigateUp()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCreateEditNewsBinding.bind(view)

        with(binding) {
            createEditNewsAppBar.mainMenuImageButton.visibility =
                View.GONE
            createEditNewsAppBar.authorizationImageButton.visibility =
                View.GONE
            createEditNewsAppBar.ourMissionImageButton.visibility =
                View.GONE
            createEditNewsAppBar.trademarkImageView.visibility =
                View.GONE
            newsCategoryTextInputLayout.isStartIconVisible = false
            if (args.newsItemArg == null) {
                createEditNewsAppBar.customAppBarTitleTextView.apply {
                    visibility = View.VISIBLE
                    setText(R.string.creating)
                    textSize = 18F
                }
                createEditNewsAppBar.customAppBarSubTitleTextView.apply {
                    visibility = View.VISIBLE
                    setText(R.string.news)
                }
            } else {
                createEditNewsAppBar.customAppBarTitleTextView.apply {
                    visibility = View.VISIBLE
                    setText(R.string.editing)
                    textSize = 18F
                }
                createEditNewsAppBar.customAppBarSubTitleTextView.apply {
                    visibility = View.VISIBLE
                    setText(R.string.news)
                }
            }
            args.newsItemArg?.let { newsItem ->
                newsCategoryAutoComplete.setText(newsItem.category.name)
                newsTitleEditText.setText(newsItem.newsItem.title)
                newsPublishDateEditText.setText(
                    Utils.formatDate(newsItem.newsItem.publishDate)
                )
                newsPublishTimeEditText.setText(
                    Utils.formatTime(newsItem.newsItem.publishDate)
                )
                newsDescriptionEditText.setText(newsItem.newsItem.description)
                newsActiveSwitch.isChecked = newsItem.newsItem.publishEnabled
            }

            if (args.newsItemArg == null) {
                newsActiveSwitch.isChecked = true
                newsActiveSwitch.isEnabled = false
            }

            if (newsActiveSwitch.isChecked) {
                newsActiveSwitch.setText(R.string.news_item_active)
            } else {
                newsActiveSwitch.setText(R.string.news_item_not_active)
            }

            newsActiveSwitch.setOnClickListener {
                if (newsActiveSwitch.isChecked) {
                    newsActiveSwitch.setText(R.string.news_item_active)
                } else {
                    newsActiveSwitch.setText(R.string.news_item_not_active)
                }
            }

            newsCancelButton.setOnClickListener {
                val activity = activity ?: return@setOnClickListener
                val dialog = AlertDialog.Builder(activity)
                dialog.setMessage(R.string.cancellation)
                    .setPositiveButton(R.string.fragment_positive_button) { alertDialog, _ ->
                        alertDialog.dismiss()
                        findNavController().navigateUp()
                    }
                    .setNegativeButton(R.string.cancel) { alertDialog, _ ->
                        alertDialog.cancel()
                    }
                    .create()
                    .show()
            }

            newsSaveButton.setOnClickListener {
                if (newsCategoryAutoComplete.text.isNullOrBlank() ||
                    newsTitleEditText.text.isNullOrBlank() ||
                    newsPublishDateEditText.text.isNullOrBlank() ||
                    newsPublishTimeEditText.text.isNullOrBlank() ||
                    newsDescriptionEditText.text.isNullOrBlank()
                ) {
                    emptyFieldWarning()
                    showErrorToast(R.string.empty_fields)
                } else {
                    fillNewsItem()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.getAllNewsCategories().collect { category ->
                val newsCategoryItems = category.map { it.name }

                with(binding) {
                    val adapter =
                        ArrayAdapter(requireContext(), R.layout.menu_item, newsCategoryItems)
                    newsCategoryAutoComplete.setAdapter(adapter)

                    newsCategoryAutoComplete.setOnItemClickListener { parent, _, position, _ ->
                        val selectedItem = parent.getItemAtPosition(position)
                        val title = binding.newsTitleEditText
                        newsCategoryItems.forEach { category ->
                            if (title.text.isNullOrBlank() || title.text.toString() == category) {
                                title.setText(selectedItem.toString())
                            }
                        }
                    }
                }
            }
        }

        val calendar = Calendar.getInstance()

        vPublishDatePicker = binding.newsPublishDateEditText

        val publishDatePicker =
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateLabel(calendar, vPublishDatePicker)
            }

        vPublishDatePicker.setOnClickListener {
            DatePickerDialog(
                this.requireContext(),
                publishDatePicker,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                this.datePicker.minDate = (System.currentTimeMillis() - 1000)
            }.show()
        }

        vPublishTimePicker = binding.newsPublishTimeEditText

        val publishTimePicker = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            updateTimeLabel(calendar, vPublishTimePicker)
        }

        vPublishTimePicker.setOnClickListener {
            TimePickerDialog(
                this.requireContext(),
                publishTimePicker,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    private fun FragmentCreateEditNewsBinding.emptyFieldWarning() {
        newsCategoryTextInputLayout.isStartIconVisible =
            newsCategoryAutoComplete.text.isNullOrBlank()
        if (newsTitleEditText.text.isNullOrBlank()) {
            newsTitleTextInputLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
        } else {
            newsTitleTextInputLayout.endIconMode = TextInputLayout.END_ICON_NONE
        }
        if (newsPublishDateEditText.text.isNullOrBlank()) {
            newsPublishDateTextInputLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
        } else {
            newsPublishDateTextInputLayout.endIconMode = TextInputLayout.END_ICON_NONE
        }
        if (newsPublishTimeEditText.text.isNullOrBlank()) {
            newsPublishTimeTextInputLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
        } else {
            newsPublishTimeTextInputLayout.endIconMode = TextInputLayout.END_ICON_NONE
        }
        if (newsDescriptionEditText.text.isNullOrBlank()) {
            newsDescriptionTextInputLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
        } else {
            newsDescriptionTextInputLayout.endIconMode = TextInputLayout.END_ICON_NONE
        }
    }

    private fun showErrorToast(text: Int) {
        Toast.makeText(
            requireContext(),
            text,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun fillNewsItem() {
        with(binding) {
            val news = args.newsItemArg
            if (news != null) {
                val editedNews = News(
                    id = news.newsItem.id,
                    title = newsTitleEditText.text.toString(),
                    newsCategoryId = convertNewsCategory(
                        newsCategoryAutoComplete.text.toString()
                    ),
                    creatorName = news.newsItem.creatorName,
                    createDate = news.newsItem.createDate,
                    creatorId = news.newsItem.creatorId,
                    publishDate = saveDateTime(
                        newsPublishDateEditText.text.toString(),
                        newsPublishTimeEditText.text.toString()
                    ),
                    description = newsDescriptionEditText.text.toString(),
                    publishEnabled = newsActiveSwitch.isChecked
                )
                viewModel.edit(editedNews)
            } else {
                val createdNews = News(
                    id = null,
                    title = newsTitleEditText.text.toString().trim(),
                    newsCategoryId = convertNewsCategory(
                        newsCategoryAutoComplete.text.toString()
                    ),
                    creatorName = Utils.fullUserNameGenerator(
                        viewModel.currentUser.lastName,
                        viewModel.currentUser.firstName,
                        viewModel.currentUser.middleName
                    ),
                    createDate = LocalDateTime.now()
                        .toEpochSecond(ZoneId.of("Europe/Moscow").rules.getOffset(now())),
                    creatorId = viewModel.currentUser.id,
                    publishDate = saveDateTime(
                        newsPublishDateEditText.text.toString(),
                        newsPublishTimeEditText.text.toString()
                    ),
                    description = newsDescriptionEditText.text.toString().trim(),
                    publishEnabled = newsActiveSwitch.isChecked
                )
                viewModel.save(createdNews)
            }
        }
    }
}
