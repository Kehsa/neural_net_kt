package main.kotlin.neuro

import java.io.Serializable
import java.lang.Math.pow
import java.util.Random

typealias Num = Double
typealias NumArr = DoubleArray

class Neuron(prev_size: Int): Serializable {
    var weights = NumArr(prev_size, { Rand.next })
    @Transient var input = .0
    @Transient var output = .0
    @Transient var delta = .0

    fun calc(prev_layer: Layer?) {
        if (prev_layer != null) {
            val n = prev_layer.neurons
            val arr = NumArr(weights.size)
            for (i in 0..weights.size-1) { // last is bias
                arr[i] = n[i].output * weights[i]
            }
            input = arr.sum()// + weights.last()
            output = sigm(input)
        }
    }

    fun err_out(target: Num): Num {
        return (target - output)
    }
    fun err_hidden(next_lay: Layer, pos: Int = -1): Num {
        val n = next_lay.neurons
        val arr = NumArr(n.size)
        for (i in 0..n.size-1) {
            arr[i] = n[i].weights[pos] * n[i].delta
        }
        return arr.sum()
    }

    fun regularization(coef: Num): Num {
        return coef * weights.indices.sumByDouble { pow(weights[it], 2.0) }
    }

    companion object Rand {
        val rnd = Random()
        val next
            get() = rnd.nextGaussian() / 7 + .5f
    }
}

class Layer(var neurons: Array<Neuron>, var prev: Layer? = null, var nex: Layer? = null): Serializable

class Net(lay_conf: IntArray, val learning_rate: Num,val regulCoef: Num,
          val moment: Num, val err: Num): Serializable {

    var layers = mutableListOf<Layer>()
    init {
        var tmp: Layer? = null
        for (n in lay_conf) {
            val new_lay = Layer(Array(n,{
                if (tmp != null)
                    Neuron(tmp!!.neurons.size)
                else
                    Neuron(0)
            }), tmp)

            if (tmp != null)
                tmp.nex = new_lay
            layers.add(new_lay)
            tmp = new_lay
        }
    }

    fun process(input: NumArr): NumArr {
        calc(input)
        val neurons = layers[layers.lastIndex].neurons
        return NumArr(neurons.size, { neurons[it].output })
    }

    fun calc(input: NumArr) {
        val input_layer = layers[0].neurons

        for (i in input.indices) {
            input_layer[i].output = input[i]
        }
        for (l in 1..layers.size-1) {
            val lay = layers[l]
            val neurons = lay.neurons
            for(n in 0..neurons.size-1) {
                neurons[n].calc(lay.prev)
            }
        }
    }

    fun mse(inD: List<NumArr>, outD: List<NumArr>): Pair<Num, Num> {
        val output_layer = layers[layers.lastIndex].neurons
        var accum = .0
        var tru = 0
        for (i in inD.indices) {
            calc(inD[i])
            val arr = NumArr(output_layer.size)
            for (n in 0..output_layer.size-1) {
                arr[n] = Math.pow( (outD[i][n] - output_layer[n].output), 2.0)
            }
            val e = arr.sum()
            accum += e
            if (e < err) tru += 1
        }
        return Pair(accum / inD.size, tru / inD.size * 100.0)
    }

    fun backpropagation(target: NumArr) {
        val lastIndex = layers.lastIndex
        for (il in lastIndex downTo 1) {
            val lay = layers[il]
            for(npos in lay.neurons.indices) {
                val neuron = lay.neurons[npos]
                if (il == lastIndex)
                    neuron.delta = sigd(neuron.output) *
                            (neuron.err_out(target[npos]) )// + neuron.regularization(regulCoef))
                else
                    neuron.delta = sigd(neuron.output) *
                            (neuron.err_hidden(lay.nex!!, npos) )// + neuron.regularization(regulCoef))
                for (i in neuron.weights.indices) {
                    neuron.weights[i] = moment * neuron.weights[i] +
                            (learning_rate * neuron.delta * lay.prev!!.neurons[i].output)
                }
            }
        }
    }
}

fun sigm(x: Num): Num = run {
    1 / (1 + Math.exp((-x)))
    //maxOf(.1*x, x)
}
fun sigd(x: Num): Num = run {
    (1 - x) * x
    //if (x > 0) 1.0 else .1
}