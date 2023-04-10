package com.skrash.book.presentation.addBookActivity

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.skrash.book.BookApplication
import com.skrash.book.R
import com.skrash.book.databinding.FragmentAddBookItemBinding
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.FormatBook
import com.skrash.book.domain.entities.Genres
import com.skrash.book.presentation.RequestFileAccess
import com.skrash.book.presentation.ViewModelFactory
import com.skrash.book.service.SendTrackerWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject


class AddBookItemFragment : Fragment() {

    private var _binding: FragmentAddBookItemBinding? = null
    private val binding: FragmentAddBookItemBinding
        get() = _binding ?: throw RuntimeException("FragmentAddBookItemBinding == null")

    private var screenMode: String = UNKNOWN_MODE
    private var bookItemId: Int = BookItem.UNDEFINED_ID

    private lateinit var viewModel: AddBookItemViewModel
    private lateinit var onEditingFinishedListener: OnEditingFinishedListener

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val component by lazy {
        (requireActivity().application as BookApplication).component
    }

    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)

        if (context is OnEditingFinishedListener) {
            onEditingFinishedListener = context
        } else {
            throw RuntimeException("Activity must implement OnEditingFinishedListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseParams()
    }

    private fun parseParams() {
        val args = requireArguments()
        if (!args.containsKey(SCREEN_MODE)) {
            throw RuntimeException("Param screen mode is absent")
        }
        val mode = args.getString(SCREEN_MODE)
        if (mode != MODE_EDIT && mode != MODE_ADD) {
            throw RuntimeException("Unknown screen mode $mode")
        }
        screenMode = mode
        if (screenMode == MODE_EDIT) {
            if (!args.containsKey(BOOK_ITEM_ID)) {
                throw RuntimeException("Param shop item id is absent")
            }
            bookItemId = args.getInt(BOOK_ITEM_ID, BookItem.UNDEFINED_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddBookItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory)[AddBookItemViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        addTextChangeListeners()
        launchRightMode()
        observeViewModel()
    }

    private fun addTextChangeListeners() {
        binding.tiTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.resetErrorInputTitle()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        binding.tiAuthor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.resetErrorInputAuthor()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        binding.tiDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.resetErrorInputDescription()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        binding.tiGenres.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.resetErrorInputGenres()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        binding.tiGenres.setOnFocusChangeListener { view, b ->
            if (b) {
                popupMenuChangeGenre()
            }
        }
        binding.tiGenres.setOnClickListener {
            popupMenuChangeGenre()
        }
        binding.tiTags.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.resetErrorInputTags()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        binding.tiPath.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.resetErrorInputPath()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
            RequestFileAccess.requestFileAccessPermission(requireActivity() as AddBookActivity, {
                if (it != null) {
                    val dataPath =
                        requireContext().getExternalFilesDir(Environment.getDataDirectory().absolutePath)?.absolutePath
                            ?: throw RuntimeException("failed to create path to data directory")
                    val fileExtension = it.path?.split(".")?.last() ?: throw RuntimeException("failed get file extension from uri")
                    val cur = requireContext().contentResolver.query(it, null, null, null)
                    var fileName = ""
                    if (cur != null) {
                        cur.use { cur ->
                            if (cur.moveToFirst()) {
                                fileName = cur.getString(0)
                            }
                        }
                        fileName = fileName.split("/").last()
                        CoroutineScope(Dispatchers.IO).launch {
                            val openStream: InputStream = requireContext().contentResolver.openInputStream(it)
                                ?: throw RuntimeException("failed get output stream from file")
                            val dataFile = File("$dataPath/$fileName")
                            if (!dataFile.exists()){
                                dataFile.createNewFile()
                            }
                            val fileOutputStream = FileOutputStream(dataFile)
                            fileOutputStream.write(openStream.readBytes())
                            openStream.close()
                            fileOutputStream.close()
                            withContext(Dispatchers.Main){
                                autoPaste(
                                    dataFile.path,
                                    FormatBook.valueOf(fileExtension.uppercase())
                                )
                                binding.tiPath.setText(dataFile.path)
                            }
                        }
                    }
                }
            }) {
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.permission_file_access_denied),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        binding.tiPath.setOnFocusChangeListener { view, b ->
            if (b) {
                getContent.launch("*/*")
            }
        }
    }

    private fun popupMenuChangeGenre() {
        val popupMenu = android.widget.PopupMenu(requireContext(), binding.tiGenres)
        for (i in Genres.values()) {
            popupMenu.menu.add(Menu.NONE, i.ordinal, Menu.NONE, i.name)
        }
        popupMenu.setOnMenuItemClickListener { item: MenuItem? ->
            binding.tiGenres.setText(item?.title.toString())
            val imm =
                requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.tiGenres.windowToken, 0)
            true
        }
        popupMenu.inflate(R.menu.popup_menu_genres)
        popupMenu.show()
    }

    private fun autoPaste(path: String, formatBook: FormatBook) {
        when (formatBook) {
            FormatBook.PDF -> {
                val fileName = path.substringAfterLast("/")
                val regexAuthor = "[A-ZА-ЯЁa-zа-яё]+ ([A-ZА-ЯЁ]{1}[.]){1,2}".toRegex()
                val tryAuthor = regexAuthor.findAll(fileName)
                var authorString = ""
                var title = fileName
                for (i in tryAuthor) {
                    title = title.replace(i.value, "")
                    if (i.value != "") {
                        authorString += "${i.value},"
                    }
                }
                title = title.replace("." + title.substringAfterLast(".", ""), "")
                title = title.replace("[,.-]+".toRegex(), "")
                title = title.trim()
                binding.tiTitle.setText(title)
                if (authorString != "") {
                    binding.tiAuthor.setText(authorString)
                }
            }
            FormatBook.FB2 -> {

            }
        }
        viewModel.getCover(
            BookItem(
                id = -1,
                title = "",
                author = "",
                description = "",
                rating = 0.0f,
                popularity = 0.0f,
                genres = Genres.Other,
                tags = "",
                path = path,
                startOnPage = 0,
                fileExtension = path.substringAfterLast(".", "").uppercase(),
            ), COVER_SIZE, COVER_SIZE
        )
        viewModel.imageCover.observe(viewLifecycleOwner) {
            binding.ivCover.setImageBitmap(it)
        }
    }

    private fun observeViewModel() {
        viewModel.shouldCloseScreen.observe(viewLifecycleOwner) {
            onEditingFinishedListener.onEditingFinishedListener()
        }
        viewModel.itemIdManipulated.observe(viewLifecycleOwner) {
            if (it != -1 && it != 0) {
                val workSendToTracker = WorkManager.getInstance(requireContext().applicationContext)
                workSendToTracker.enqueueUniqueWork(
                    SendTrackerWorker.WORK_NAME,
                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                    SendTrackerWorker.makeRequest(it)
                )
            }
        }
    }

    private fun launchRightMode() {
        when (screenMode) {
            MODE_EDIT -> launchEditMode()
            MODE_ADD -> launchAddMode()
        }
    }

    private fun launchEditMode() {
        viewModel.getBookItem(bookItemId)
        binding.btnCancel.setOnClickListener {
            viewModel.finishWork()
        }
        binding.btnSave.setOnClickListener {
            viewModel.finishEditing(
                id = bookItemId,
                binding.tiTitle.text?.toString(),
                binding.tiAuthor.text?.toString(),
                binding.tiDescription.text?.toString(),
                binding.tiGenres.text?.toString(),
                binding.tiTags.text?.toString(),
                binding.tiPath.text?.toString(),
                binding.cbShareAccess.isChecked
            )
        }
    }

    private fun launchAddMode() {
        binding.btnCancel.setOnClickListener {
            viewModel.finishWork()
        }
        binding.btnSave.setOnClickListener {
            viewModel.finishEditing(
                title = binding.tiTitle.text?.toString(),
                author = binding.tiAuthor.text?.toString(),
                description = binding.tiDescription.text?.toString(),
                genres = binding.tiGenres.text?.toString(),
                tags = binding.tiTags.text?.toString(),
                path = binding.tiPath.text?.toString(),
                shareAccess = binding.cbShareAccess.isChecked
            )
        }
    }

    companion object {
        private const val COVER_SIZE = 300
        private const val SCREEN_MODE = "screen_mode"
        private const val MODE_ADD = "mode_add"
        private const val MODE_EDIT = "mode_edit"
        private const val UNKNOWN_MODE = ""
        private const val BOOK_ITEM_ID = "book_item_id"

        fun newInstanceAddItem(): AddBookItemFragment {
            return AddBookItemFragment().apply {
                arguments = Bundle().apply {
                    putString(SCREEN_MODE, MODE_ADD)
                }
            }
        }

        fun newInstanceEditItem(bookItemId: Int): AddBookItemFragment {
            return AddBookItemFragment().apply {
                arguments = Bundle().apply {
                    putString(SCREEN_MODE, MODE_EDIT)
                    putInt(BOOK_ITEM_ID, bookItemId)
                }
            }
        }
    }

    interface OnEditingFinishedListener {

        fun onEditingFinishedListener()
    }

}