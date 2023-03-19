package com.skrash.book.presentation.bookInfoActivity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.skrash.book.R
import com.skrash.book.databinding.FragmentBookInfoBinding
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.BookApplication
import com.skrash.book.presentation.ViewModelFactory
import com.skrash.book.presentation.openBookActivity.OpenBookActivity
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

    private fun initCover(){
        viewModel.imgCover.observe(viewLifecycleOwner){
            binding.imCover.setImageBitmap(it)
        }
    }

    private fun parseParams() {
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
    ): View {
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
            if(viewModel.bookItem.value != null){
                startActivity(OpenBookActivity.newIntent(requireContext(), viewModel.bookItem.value!!.id))
            }
        }
        initCover()
        viewModel.bookItem.observe(viewLifecycleOwner){
            if (it != null){
                if (it.shareAccess){
                    binding.ivShareAccess.setImageResource(R.drawable.ic_baseline_cloud_upload_24_green)
                } else {
                    binding.ivShareAccess.setImageResource(R.drawable.ic_baseline_cloud_upload_24)
                }
                binding.ivShareAccess.visibility = View.VISIBLE
            }
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