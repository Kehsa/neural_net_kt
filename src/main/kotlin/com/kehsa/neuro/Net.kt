package com.kehsa.neuro

import java.io.Serializable
import java.lang.Math.abs
import java.util.Random

typealias Num = Float
typealias NumArr = FloatArray

class Neuron(prev_size: Int): Serializable {
    var weights = NumArr(prev_size, { Rand.next })
    @Transient var output = .0f
    @Transient var delta = .0f

    /**
     * synapse
     */
    fun calc(prev_layer: Layer?) {
        if (prev_layer != null) {
            val n = prev_layer.neurons
            val arr = NumArr(weights.size)
            for (i in 0..weights.size-1) {
                arr[i] = n[i].output * weights[i]
            }
            output = sigm(arr.sum())
        }
    }

    /**
     * calculate error in output layer
     */
    fun err_out(target: Num): Num {
        return (target - output)
    }

    /**
     * calculate error in hidden layers
     */
    fun err_hidden(next_lay: Layer, pos: Int = -1): Num {
        val n = next_lay.neurons
        val arr = NumArr(n.size)
        for (i in 0..n.size-1) {
            arr[i] = n[i].weights[pos] * n[i].delta
        }
        return arr.sum()
    }

    fun regularization(coef: Num): Num {
        val accum = weights.indices
                .map { abs(weights[it]) }
                .sum()
        return coef * accum
    }

    companion object Rand {
        val rnd = Random()
        val next
            get() = (rnd.nextGaussian() / 3.0 + .1).toFloat()
    }
}

class Layer(var neurons: Array<Neuron>, var prev: Layer? = null, var nex: Layer? = null): Serializable

class Net(lay_conf: IntArray, val learning_rate: Num,val regulCoef: Num,
          val moment: Num): Serializable {

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

    /**
     * @return output layer (answer of net)
     */
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

    /**
     * mean squared error
     */
    fun mse(inD: List<NumArr>, outD: List<NumArr>): Pair<Num, Num> {
        val output_layer = layers[layers.lastIndex].neurons
        var accum = .0f
        var tru = 0
        for (i in inD.indices) {
            calc(inD[i])
            val arr = NumArr(output_layer.size)
            var pos_max = 0
            var max = output_layer[0].output
            for (n in 0..output_layer.size-1) {
                val output = output_layer[n].output
                arr[n] = Math.pow( (outD[i][n] - output).toDouble(), 2.0).toFloat()
                if (max < output) {
                    max = output
                    pos_max = n
                }
            }
            accum += arr.sum()
            if (outD[i][pos_max] == 1f) tru += 1
        }
        return Pair(accum / inD.size, tru.toFloat() / inD.size * 100.0f)
    }

    fun backpropagation(target: NumArr) {
        val lastIndex = layers.lastIndex
        for (il in lastIndex downTo 1) {
            val lay = layers[il]
            for(npos in lay.neurons.indices) {
                val neuron = lay.neurons[npos]
                if (il == lastIndex) {
                    if (regulCoef == 0f)
                        neuron.delta = sigd(neuron.output) * neuron.err_out(target[npos])
                    else
                        neuron.delta = sigd(neuron.output) * (neuron.err_out(target[npos]) + neuron.regularization(regulCoef))
                }
                else {
                    if (regulCoef == 0f)
                        neuron.delta = sigd(neuron.output) * neuron.err_hidden(lay.nex!!, npos)
                    else
                        neuron.delta = sigd(neuron.output) * (neuron.err_hidden(lay.nex!!, npos) + neuron.regularization(regulCoef))
                }
                val w = neuron.weights
                for (i in 0..w.size-1)// bias is last
                    w[i] = moment * w[i] + (learning_rate * neuron.delta * lay.prev!!.neurons[i].output)
            }
        }
    }
}

fun sigm(x: Num): Num = run {
    1 / (1 + Math.exp((-x).toDouble())).toFloat()
}
fun sigd(x: Num): Num = run {
    (1 - x) * x
}