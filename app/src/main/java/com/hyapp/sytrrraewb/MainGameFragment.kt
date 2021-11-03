package com.hyapp.sytrrraewb

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.hyapp.sytrrraewb.databinding.FragmentMainGameBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.Flow

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainGameFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainGameFragment : Fragment() {

    val bind : FragmentMainGameBinding by viewBinding()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_game, container, false)
    }
    private var endGame = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        bind.back.setOnClickListener {
            findNavController().navigate(MainGameFragmentDirections.actionMainGameFragmentToPlayFragment())
        }

        val sqr = initSqr()
        val texts = initTexts()
        val field = mutableListOf(
            1,2,3,4,5,6,7,0,8
        )
        field.shuffle()
        while (checkOk(field))
            field.shuffle()
        drawField(field,texts)
        fun move(zero : Int,i : Int){
            field[zero] = field[i]
            field[i] = 0
            drawField(field,texts)
            checkWin(field)
        }
        for (i in sqr.indices){
            sqr[i].setOnClickListener {
                if(!endGame) {
                    val zero = field.indexOf(0)
                    if (zero == i - 3 || zero == i + 3) {
                        move(zero, i)
                    }
                    if (zero == i - 1) if (zero != 2 && zero != 5) move(zero, i)
                    if (zero == i + 1) if (zero != 3 && zero != 6) move(zero, i)
                }
            }
        }

    }

    private fun checkOk(field: MutableList<Int>): Boolean {
        var sum = 0
        for (i in field.indices){
            var k = 0
            if(field[i] == 0) continue
            for(j in i until field.size){
                if(field[i] == 0) continue
                if (field[i] > field[j]) k += 1
            }
            sum += k
        }
        sum += (((field.indexOf(0)) / 3) + 1)
        return sum % 2 != 0
    }

    private fun checkWin(field: MutableList<Int>){
        if (field == mutableListOf(1,2,3,4,5,6,7,8,0)){
            endGame = true
            bind.back.visibility = View.VISIBLE
            bind.wintext.visibility = View.VISIBLE
        }
    }





    private fun drawField(field: MutableList<Int>, texts: List<TextView>) {
        for (i in field.indices){
            texts[i].text = if(field[i] != 0) field[i].toString() else ""
        }
    }

    private fun initSqr(): List<FrameLayout> {
        return listOf(
            bind.firstSqr,
            bind.secondSqr,
            bind.thirdSqr,
            bind.fourthSqr,
            bind.fifthSqr,
            bind.sixthSqr,
            bind.seventhSqr,
            bind.eightSqr,
            bind.ninethSqr,
        )
    }

    private fun initTexts(): List<TextView> {
        return listOf(
            bind.firstText,
            bind.secondText,
            bind.thirdText,
            bind.fourthText,
            bind.fifthText,
            bind.sixthText,
            bind.seventhText,
            bind.eightText,
            bind.clearText,
        )
    }


}