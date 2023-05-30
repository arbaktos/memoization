package com.example.android.memoization.ui.features.addnewpair

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.android.memoization.utils.NewPairNavArgs
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class NewPairFragment : Fragment() {

    private val args: NewPairFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val newPairNavArgs: NewPairNavArgs = args.wpArg
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                when (newPairNavArgs) {
                    is NewPairNavArgs.NewWordPair -> AddNewPairScreen(
                        navController = findNavController(),
                        args = newPairNavArgs
                    )
                    is NewPairNavArgs.EditPair -> AddNewPairScreen(
                        navController = findNavController(),
                        args = newPairNavArgs
                    )
                }
            }
        }
    }
}