package com.skrash.book.presentation

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.skrash.book.databinding.FragmentBookInfoBinding
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.presentation.bookInfoActivity.BookInfoViewModel
import javax.inject.Inject

class BookInfoFragment : Fragment() {

    private lateinit var viewModel: BookInfoViewModel

    private var _binding: FragmentBookInfoBinding? = null
    private val binding: FragmentBookInfoBinding
        get() = _binding ?: throw RuntimeException("FragmentBookInfoBinding is null")

    private var bookItemId: Int = BookItem.UNDEFINED_ID

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val component by lazy {
        (requireActivity().application as BookApplication).component
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseParams()
    }

    fun parseParams() {
        val args = requireArguments()
        if (!args.containsKey(BOOK_ITEM_ID)) {
            throw RuntimeException("Param book item id is absent")
        }
        bookItemId = args.getInt(BOOK_ITEM_ID, BookItem.UNDEFINED_ID)
    }

    override fun onAttach(context: Context) {
        component.inject(this)

        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBookInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory)[BookInfoViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.getBookItem(bookItemId)
        binding.btnOpen.setOnClickListener {
            TODO()
        }
    }

    companion object {

        private const val BOOK_ITEM_ID = "book_item_id"

        fun newInstanceOpenItem(bookItemId: Int): BookInfoFragment {
            return BookInfoFragment().apply {
                arguments = Bundle().apply {
                    putInt(BOOK_ITEM_ID, bookItemId)
                }
            }
        }
    }
}